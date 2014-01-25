package sg.edu.nus.micphone.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;

import sg.edu.nus.micphone.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

@EFragment
public class DiscoverDialogFragment extends DialogFragment {
	private static final String TAG = "DiscoverDialogFragment";
	private static final String SERVICE_TYPE = "_rtp._udp.local.";
	private static String SERVICE_NAME = "KboxService";
	
	/** WiFi stuff */
	private WifiManager mWifiManager;
	private MulticastLock mMulticastLock;
	private static final String WIFI_LOCK_NAME = "KboxServiceWifiLock";
	
	/** JmDNS */
	private JmDNS mJmDNS;
	private ServiceListener mDiscoveryListener;
	private List<ServiceEvent> mDiscoveredServicesList;
	private ServiceEventListAdapter mDiscoveredServicesListAdapter;
	
	/** Audio management */
	private AudioStream mMicStream;
	private AudioGroup mStreamGroup;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Obtain the WiFi manager.
		mWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		
		// Create the discovery listener.
		mDiscoveredServicesList = new ArrayList<ServiceEvent>();
		mDiscoveredServicesListAdapter = new ServiceEventListAdapter(getActivity(), mDiscoveredServicesList);
		initializeServiceListener();
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the builder class for convenient dialog construction.
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.discovering)
			.setAdapter(mDiscoveredServicesListAdapter, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ServiceEvent ev = mDiscoveredServicesList.get(which);
					Log.d(TAG, "Selected server: " + ev.getInfo().getHostAddresses()[0]);
					
					// Obtain the endpoint port and host.
					int port = ev.getInfo().getPort();
					InetAddress host = ev.getInfo().getInetAddresses()[0];
					beginAudioStream(host, port);
				}
			});
		return builder.create();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Acquire a multicast lock for receiving multicast packets.
		Log.d(TAG, "Acquiring WiFi multicast lock");
		mMulticastLock = mWifiManager.createMulticastLock(WIFI_LOCK_NAME);
		mMulticastLock.setReferenceCounted(true);
		mMulticastLock.acquire();
		
		// Start discovering.
		startDiscovery();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// Stop service discovery.
		stopDiscovery();
		
		// Clear the list of discovered services.
		mDiscoveredServicesList.clear();
		
		// Release the multicast lock.
		Log.d(TAG, "Releasing WiFi multicast lock");
		mMulticastLock.release();
	}
	
	@Background
	protected void startDiscovery() {
		try {
			InetAddress ipAddress;
			if (isWiFiAPEnabled()) {
				// Hackish assumption! But seems like all have 192.168.43.1.
				ipAddress = InetAddress.getByName("192.168.43.1");
				
			} else {
				// We need to obtain the address to broadcast on this interface.
				int ipAddressInt = mWifiManager.getConnectionInfo().getIpAddress();
				if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
					ipAddressInt = Integer.reverseBytes(ipAddressInt);
				}
				
				byte[] ipByteArray = BigInteger.valueOf(ipAddressInt).toByteArray();
				try {
					ipAddress = InetAddress.getByAddress(ipByteArray);
				} catch (UnknownHostException e) {
					Log.e(TAG, "Unable to get host address");
					ipAddress = null;
				}
			}
			
			// Create a JMDNS instance. 
			mJmDNS = JmDNS.create(ipAddress, SERVICE_NAME);
			mJmDNS.addServiceListener(SERVICE_TYPE, mDiscoveryListener);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Background
	protected void stopDiscovery() {
		mJmDNS.removeServiceListener(SERVICE_TYPE, mDiscoveryListener);
		mJmDNS.unregisterAllServices();
		
		try {
			mJmDNS.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initializeServiceListener() {
		mDiscoveryListener = new ServiceListener() {
			@Override
			public void serviceAdded(ServiceEvent ev) {
				// A service was found!  Do something with it.
				Log.d(TAG, "Service added: " + ev);
				if (!ev.getType().equals(SERVICE_TYPE)) {
					// Service type is the string containing the protocol and
	                // transport layer for this service.
					Log.d(TAG, "Unknown service type: " + ev.getType());
				} else if (ev.getName().startsWith(SERVICE_NAME)) {
					mJmDNS.requestServiceInfo(ev.getType(), ev.getName(), 1);
				}
			}

			@Override
			public void serviceRemoved(ServiceEvent ev) {
				Log.d(TAG, "Service lost: " + ev);
				for (ServiceEvent event : mDiscoveredServicesList) {
					if (event.getName().equals(ev.getName())) {
						mDiscoveredServicesList.remove(event);
					}
				}
				
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mDiscoveredServicesListAdapter.notifyDataSetChanged();
					}
				});
			}

			@Override
			public void serviceResolved(ServiceEvent ev) {
				Log.d(TAG, "Service resolved: " + ev);
				
				// Check for existing service in the list.
				for (ServiceEvent event : mDiscoveredServicesList) {
					if (event.getName().equals(ev.getName())) {
						mDiscoveredServicesList.remove(event);
					}
				}
				
				mDiscoveredServicesList.add(ev);
				
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mDiscoveredServicesListAdapter.notifyDataSetChanged();
					}
				});
			}
		};
	}
	
	/**
	 * Utility function to check if WiFi AP is enabled on the device.
	 */
	private boolean isWiFiAPEnabled() {
		boolean isWiFiAPEnabled = false;
		Method[] wmMethods = mWifiManager.getClass().getDeclaredMethods();
        for (Method method : wmMethods){
            if(method.getName().equals("isWifiApEnabled")) {  
                try {
                	isWiFiAPEnabled = (Boolean) method.invoke(mWifiManager);
                } catch (IllegalArgumentException e) {
                  e.printStackTrace();
                } catch (IllegalAccessException e) {
                  e.printStackTrace();
                } catch (InvocationTargetException e) {
                  e.printStackTrace();
                }
            }
        }
        
        return isWiFiAPEnabled;
	}
	

	@Background
	protected void beginAudioStream(InetAddress host, int port) {
		Log.d(TAG, "beginAudioStream to " + host.getHostAddress() + ":" + port);
		try {
			try {
				//mMicStream = new AudioStream(host);
				//mMicStream = new AudioStream(InetAddress.getLocalHost());
				mMicStream = new AudioStream(InetAddress.getByName("0.0.0.0"));
				Log.d(TAG, "InetAddress.getLocalHost is " + InetAddress.getLocalHost());
				
				// Create the stream group.
				mStreamGroup = new AudioGroup();
				mStreamGroup.setMode(AudioGroup.MODE_NORMAL);
				
				// Connecting and sending stream.
				mMicStream.setMode(RtpStream.MODE_SEND_ONLY);
				mMicStream.associate(host, port);
				mMicStream.setCodec(AudioCodec.GSM_EFR);
				mMicStream.join(mStreamGroup);
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public class ServiceEventListAdapter extends ArrayAdapter<ServiceEvent> {
		private Context mContext;
		private List<ServiceEvent> mValues;

		public ServiceEventListAdapter(Context context, List<ServiceEvent> values) {
			super(context, android.R.layout.simple_list_item_1);
			mContext = context;
			mValues = values;
		}
		
		@Override
		public int getCount() {
			return mValues.size();
		}
		
		@Override
		public ServiceEvent getItem(int position) {
			return mValues.get(position);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
			
			TextView textView = (TextView) rowView.findViewById(android.R.id.text1);
			ServiceEvent serviceInfo = mValues.get(position);
			
			// Sample format: KboxService 2 (192.168.1.1:32132)
			// The \032 is actually a space (base 10 ASCII), but it doesn't
			// seem to be processed correctly by Android API.
			textView.setText(
					serviceInfo.getName() + " (" +
					mValues.get(position).getInfo().getHostAddresses()[0] + ":" +
					mValues.get(position).getInfo().getPort() + ")");
			return rowView;
		}
	}
}
