package sg.edu.nus.micphone;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.util.Log;

/** Construct : ServerConnection(Handler, AudioGroup<singleton>) -> */
public class ServerConnection {

	private static final String TAG = "ServerConnection";
	private static final AudioCodec CODEC = AudioCodec.GSM_EFR;

	private AudioGroup m_outAudio;
	private AudioStream mIncoming;

	private int m_Port = -1;

	public ServerConnection(AudioGroup outAudio) {

		this.m_outAudio = outAudio;

		try {
			InetAddress inet = this.getInetAddress();
			mIncoming = new AudioStream(inet);
			setLocalPort(mIncoming.getLocalPort());
			mIncoming.setCodec(CODEC);
			mIncoming.setMode(RtpStream.MODE_RECEIVE_ONLY);
			//mIncoming.join(m_outAudio);
			Log.d(TAG, "Joined AudioGroup");
		} catch (IOException ioe) {
			Log.e(TAG, "Error creating ServerSocket: ", ioe);
			ioe.printStackTrace();
		}

	}

	public void tearDown() {
		mIncoming.join(null);
	}

	public int getLocalPort() {
		return m_Port;
	}

	public void setLocalPort(int port) {
		m_Port = port;
	}

	private InetAddress getInetAddress() {
		try {
			List<NetworkInterface> netInt = Collections.list(NetworkInterface
					.getNetworkInterfaces());
			for (NetworkInterface net : netInt) {
				if (net.isUp() && !net.isLoopback()) {
					List<InterfaceAddress> inetInt = net
							.getInterfaceAddresses();
					for (InterfaceAddress inet : inetInt) {
						if (inet.getAddress() != null
								&& inet.getAddress().toString().contains(".")) {
							return inet.getAddress();
						}
					}
				}
			}
		} catch (SocketException se) {
			se.printStackTrace();
		}
		return null;
	}
}
