package sg.edu.nus.micphone.client;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import sg.edu.nus.micphone.NetworkUtils;

public class ClientService extends Service {
	private static final String TAG = "ClientService";

	private static final AudioCodec CODEC = AudioCodec.GSM_EFR;
	private final IBinder mBinder = new ClientBinder();
	
	private InetAddress mHost;
	private int mPort;
	
	public ClientService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");

		mHost = (InetAddress) intent.getSerializableExtra("host");
		mPort = intent.getIntExtra("port", -1);

        BeginAudioStreamTask task = new BeginAudioStreamTask(this);
        task.execute();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/** Methods for clients */
	protected void beginAudioStream() {

	}
	
	public InetAddress getHost() {
		return mHost;
	}
	
	public int getPort() {
		return mPort;
	}
	
	public class ClientBinder extends Binder {
		ClientService getService() {
			return ClientService.this;
		}
	}

    private class BeginAudioStreamTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;

        BeginAudioStreamTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "beginAudioStream to " + mHost.getHostAddress() + ":" + mPort);
            try {
                AudioStream micStream = new AudioStream(
                        NetworkUtils.getLocalAddress(mContext));
                int localPort = micStream.getLocalPort();

                try {
                    // Negotiate the RTP endpoints of the server.
                    Socket socket = new Socket(mHost, mPort);
                    OutputStream outputStream = socket.getOutputStream();
                    BufferedWriter outputWriter = new BufferedWriter(
                            new OutputStreamWriter(outputStream));
                    outputWriter.write(Integer.toString(localPort) + "\n");
                    outputWriter.flush();

                    InputStream inputStream = socket.getInputStream();
                    BufferedReader inputReader = new BufferedReader(
                            new InputStreamReader(inputStream));
                    String input = inputReader.readLine();

                    int remotePort = Integer.parseInt(input);
                    socket.close();

                    // Associate with server RTP endpoint.
                    AudioGroup streamGroup = new AudioGroup();
                    // streamGroup.setMode(AudioGroup.MODE_ECHO_SUPPRESSION);
                    streamGroup.setMode(AudioGroup.MODE_NORMAL);

                    micStream.setCodec(CODEC);
                    micStream.setMode(AudioStream.MODE_SEND_ONLY);
                    micStream.associate(mHost, remotePort);
                    micStream.join(streamGroup);

                    // Print debug information about group.
                    Log.d(TAG, "Local addr: " + micStream.getLocalAddress() + ":"
                            + micStream.getLocalPort());
                    Log.d(TAG, "Remote addr: " + micStream.getRemoteAddress() + ":"
                            + micStream.getRemotePort());

                    // Obtain an AudioManager.
                    AudioManager manager = (AudioManager) mContext
                            .getSystemService(Context.AUDIO_SERVICE);
                    manager.setMicrophoneMute(false);
                    manager.setSpeakerphoneOn(false);
                    manager.setMode(AudioManager.MODE_IN_COMMUNICATION);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }
    }
}
