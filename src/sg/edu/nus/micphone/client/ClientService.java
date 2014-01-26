package sg.edu.nus.micphone.client;

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

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;

import sg.edu.nus.micphone.NetworkUtils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.os.IBinder;
import android.util.Log;

@EService
public class ClientService extends Service {
	private static final String TAG = "ClientService";
	private static final AudioCodec CODEC = AudioCodec.AMR;
	
	public ClientService() {
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		InetAddress host = (InetAddress) intent.getSerializableExtra("host");
		int port = intent.getIntExtra("port", -1);
		beginAudioStream(host, port);
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	/** Methods for clients */
	@Background
	protected void beginAudioStream(InetAddress host, int port) {
		Log.d(TAG, "beginAudioStream to " + host.getHostAddress() + ":" + port);
		try {
			AudioStream micStream = new AudioStream(NetworkUtils.getLocalAddress(this));
			int localPort = micStream.getLocalPort();
			
			try {
				// Negotiate the RTP endpoints of the server.
				Socket socket = new Socket(host, port);
				OutputStream outputStream = socket.getOutputStream();
				BufferedWriter outputWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
				outputWriter.write(Integer.toString(localPort) + "\n");
				outputWriter.flush();
				
				InputStream inputStream = socket.getInputStream();
				BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
				String input = inputReader.readLine();
				
				int remotePort = Integer.parseInt(input);
				socket.close();
				
				// Associate with server RTP endpoint.
				AudioGroup streamGroup = new AudioGroup();
				//streamGroup.setMode(AudioGroup.MODE_ECHO_SUPPRESSION);
				streamGroup.setMode(AudioGroup.MODE_NORMAL);
				
				micStream.setCodec(CODEC);
				micStream.setMode(AudioStream.MODE_SEND_ONLY);
				micStream.associate(host, remotePort);
				micStream.join(streamGroup);
				
				// Print debug information about group.
				Log.d(TAG, "Local addr: " + micStream.getLocalAddress() + ":" + micStream.getLocalPort());
				Log.d(TAG, "Remote addr: " + micStream.getRemoteAddress() + ":" + micStream.getRemotePort());
				
				// Obtain an AudioManager.
				AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
				manager.setMicrophoneMute(false);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
