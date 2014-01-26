package sg.edu.nus.micphone;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

/** Call Constructor: ServerNsd(Context context) -> Call initializeNsd -> registerService(int port) */
public class ServerNsd {

	Context mContext;

	NsdManager mNsdManager;
	NsdManager.RegistrationListener mRegistrationListener;

	public static final String TAG = "ServerNsd";
	private static final String SERVICE_TYPE = "_rtp._udp.";
	private String mServiceName = "KboxService";

	NsdServiceInfo mService;

	public ServerNsd(Context context) {
		mContext = context;
		mNsdManager = (NsdManager) context
				.getSystemService(Context.NSD_SERVICE);
	}

	public void initializeNsd() {
		initializeRegistrationListener();
	}

	public void initializeRegistrationListener() {
		mRegistrationListener = new NsdManager.RegistrationListener() {

			@Override
			public void onServiceRegistered(NsdServiceInfo info) {
				mServiceName = info.getServiceName();
				
				Log.d(TAG, info.getServiceName() + " registered.");
			}

			@Override
			public void onRegistrationFailed(NsdServiceInfo info, int errorCode) {
				Log.e(TAG, info.getServiceName()
						+ " registration failed. Error Code : " + errorCode
						+ " .");
			}

			@Override
			public void onServiceUnregistered(NsdServiceInfo info) {
				Log.d(TAG, info.getServiceName() + " unregistered.");
			}

			@Override
			public void onUnregistrationFailed(NsdServiceInfo info,
					int errorCode) {
				Log.e(TAG, info.getServiceName()
						+ " unregistration failed. Error Code : " + errorCode
						+ ".");
			}

		};
	}

	public void registerService(int port) {
		NsdServiceInfo serviceInfo = new NsdServiceInfo();
		Log.d(TAG, "NSD is running at " + port);
		serviceInfo.setPort(port);
		serviceInfo.setServiceName(mServiceName);
		serviceInfo.setServiceType(SERVICE_TYPE);

		mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD,
				mRegistrationListener);
	}

	public NsdServiceInfo getChosenServiceInfo() {
		return mService;
	}

	public void tearDown() {
		mNsdManager.unregisterService(mRegistrationListener);
	}
}
