package sg.edu.nus.micphone.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import sg.edu.nus.micphone.NetworkUtils;
import android.content.Context;
import android.media.AudioManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.util.Log;

public class ConnectionHandler {
	private static final String TAG = "ConnectionHandler";
	
	private static final AudioCodec CODEC = AudioCodec.GSM_EFR;
	
	private ServerSocket mServerSocket;
	private boolean mRunning = false;

	private AudioGroup mAudioGroup;
	private Context mContext;

	/**
	 * Handles and negotiates potential RTP connections.
	 * 
	 * @throws IOException
	 */
	public ConnectionHandler(Context context, AudioGroup audioGroup)
			throws IOException {
		Log.d(TAG, "ConnectionHandler started");
		mServerSocket = new ServerSocket(0);
		mContext = context;
		mAudioGroup = audioGroup;
	}

	public void start() {
		mRunning = true;

		// Connection handling loop.
		while (true) {
			Socket singleConnection;
			try {
				singleConnection = mServerSocket.accept();
				SingleConnectionHandler handler = new SingleConnectionHandler(
						singleConnection);
				Thread thread = new Thread(handler);
				thread.start();

				// Stop the loop when requested.
				if (!mRunning) {
					try {
						thread.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					return;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public int getLocalPort() {
		return mServerSocket.getLocalPort();
	}

	public void terminate() {
		mRunning = false;
	}

	/**
	 * Handles a single connection.
	 */
	public class SingleConnectionHandler implements Runnable {
		private static final String TAG = "SingleConnectionHandler";
		private Socket mSocket;

		public SingleConnectionHandler(Socket socket) {
			mSocket = socket;
			Log.d(TAG, "Handling connection from " + socket.getInetAddress());
		}

		@Override
		public void run() {
			try {
				// Negotiate the RTP endpoints of the client.
				InputStream inputStream = mSocket.getInputStream();
				BufferedReader inputReader = new BufferedReader(
						new InputStreamReader(inputStream));
				String input = inputReader.readLine();

				InetAddress remoteAddress = mSocket.getInetAddress();
				int remotePort = Integer.parseInt(input);

				InetAddress localAddress = NetworkUtils.getInetAddress();
				AudioStream audioStream = new AudioStream(localAddress);
				int localPort = audioStream.getLocalPort();

				OutputStream outputStream = mSocket.getOutputStream();
				BufferedWriter outputWriter = new BufferedWriter(
						new OutputStreamWriter(outputStream));
				outputWriter.write(Integer.toString(localPort) + "\n");
				outputWriter.flush();

				mSocket.close();

				// Associate with client RTP endpoint.
				audioStream.setCodec(CODEC);
				audioStream.setMode(AudioStream.MODE_RECEIVE_ONLY);
				audioStream.associate(remoteAddress, remotePort);
				audioStream.join(mAudioGroup);

				// Print debug information about group.
				Log.d(TAG, "Local addr: " + audioStream.getLocalAddress() + ":"
						+ audioStream.getLocalPort());
				Log.d(TAG, "Remote addr: " + audioStream.getRemoteAddress()
						+ ":" + audioStream.getRemotePort());

				// Obtain an AudioManager.
				AudioManager manager = (AudioManager) mContext
						.getSystemService(Context.AUDIO_SERVICE);
				manager.setSpeakerphoneOn(true);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
