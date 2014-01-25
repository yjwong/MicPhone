package sg.edu.nus.micphone;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

/** Call Constructor: ServerNsd(Context context) -> Call initializeNsd -> registerService(int port) */
public class ServerNsd {

	Context m_Context;

	NsdManager m_NsdManager;
	NsdManager.RegistrationListener m_RegistrationListener;

	public static final String TAG = "ServerNsd";
	private static final String SERVICE_TYPE = "_rtp._udp.";
	private String m_ServiceName = "KboxService";

	NsdServiceInfo m_Service;

	public ServerNsd(Context context) {
		m_Context = context;
		m_NsdManager = (NsdManager) context
				.getSystemService(Context.NSD_SERVICE);
	}

	public void initializeNsd() {
		initializeRegistrationListener();
	}

	public void initializeRegistrationListener() {
		m_RegistrationListener = new NsdManager.RegistrationListener() {

			@Override
			public void onServiceRegistered(NsdServiceInfo info) {
				m_ServiceName = info.getServiceName();
				Log.d(TAG, info.getServiceName() + "registered.");
			}

			@Override
			public void onRegistrationFailed(NsdServiceInfo info, int errorCode) {
				Log.e(TAG, info.getServiceName()
						+ "registration failed. Error Code : " + errorCode
						+ ".");
			}

			@Override
			public void onServiceUnregistered(NsdServiceInfo info) {
				Log.d(TAG, info.getServiceName() + "unregistered.");
			}

			@Override
			public void onUnregistrationFailed(NsdServiceInfo info,
					int errorCode) {
				Log.e(TAG, info.getServiceName()
						+ "unregistration failed. Error Code : " + errorCode
						+ ".");
			}

		};
	}

	public void registerService(int port) {
		NsdServiceInfo serviceInfo = new NsdServiceInfo();
		serviceInfo.setPort(port);
		serviceInfo.setServiceName(m_ServiceName);
		serviceInfo.setServiceType(SERVICE_TYPE);

		m_NsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD,
				m_RegistrationListener);
	}

	public NsdServiceInfo getChosenServiceInfo() {
		return m_Service;
	}

	public void tearDown() {
		m_NsdManager.unregisterService(m_RegistrationListener);
	}
}
