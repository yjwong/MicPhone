package sg.edu.nus.micphone;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkUtils {
	private static final String TAG = "NetworkUtils";
	private static final String WIFI_AP_IP = "192.168.43.1";

	public NetworkUtils() {

	}

	public static InetAddress getLocalAddress(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		InetAddress ipAddress = null;
		if (isWiFiAPEnabled(context)) {
			// Hackish assumption! But seems like all have 192.168.43.1.
			try {
				ipAddress = InetAddress.getByName(WIFI_AP_IP);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			// We need to obtain the address to broadcast on this interface.
			int ipAddressInt = wifiManager.getConnectionInfo().getIpAddress();
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

		return ipAddress;
	}

	/**
	 * Utility function to check if WiFi AP is enabled on the device.
	 */
	public static boolean isWiFiAPEnabled(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		boolean isWiFiAPEnabled = false;
		Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
		for (Method method : wmMethods) {
			if (method.getName().equals("isWifiApEnabled")) {
				try {
					isWiFiAPEnabled = (Boolean) method.invoke(wifiManager);
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

	public static InetAddress getInetAddress() {
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
