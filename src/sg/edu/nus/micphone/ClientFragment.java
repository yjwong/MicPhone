package sg.edu.nus.micphone;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.androidannotations.annotations.EFragment;

import sg.edu.nus.micphone.client.ClientService_;
import sg.edu.nus.micphone.client.ConnectWiFiDialogFragment_;
import sg.edu.nus.micphone.client.DiscoverDialogFragment_;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link ClientFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link ClientFragment#newInstance} factory method
 * to create an instance of this fragment.
 * 
 */
@EFragment
public class ClientFragment extends Fragment {
	private static final String TAG = "ClientFragment";
	
	/** Connectivity management */
	private ConnectivityManager mConnectivityManager;
	private IntentFilter mConnectivityChangeIntentFilter;
	private BroadcastReceiver mConnectivityChangeReceiver;
	private DialogFragment mConnectWiFiDialogFragment;
	
	/** Server discovery */
	private Button mSelectServerButton; 
	private static DialogFragment mDiscoverDialogFragment;

	/** Fragment management */
	private OnFragmentInteractionListener mListener;

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @return A new instance of fragment ClientFragment.
	 */
	public static ClientFragment newInstance() {
		ClientFragment fragment = new ClientFragment();
		return fragment;
	}

	public ClientFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Obtain required system services.
		mConnectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		
		// Register a broadcast receiver for network state changes.
		mConnectivityChangeIntentFilter = new IntentFilter();
		mConnectivityChangeIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		mConnectivityChangeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				checkConnectivity();
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment.
		return inflater.inflate(R.layout.fragment_client, container, false);
	}
	
	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);
		
		// Register the buttons to their event handlers.
		registerButtonEvents();
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Start network discovery.
		checkConnectivity();
		
		// Resume receiving network state changes.
		getActivity().registerReceiver(mConnectivityChangeReceiver, mConnectivityChangeIntentFilter);
		
		// Check if client service is running. If it is running, show the
		// server name and hide the select server button.
		if (isClientServiceRunning()) {
			Log.d(TAG, "Client service is running");
			onClientServiceStarted();
		} else {
			onClientServiceStopped();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// Hide all dialogs.
		if (mConnectWiFiDialogFragment != null && mConnectWiFiDialogFragment.isAdded()) {
			mConnectWiFiDialogFragment.dismiss();
		}
		
		// Stop receiving network state changes.
		getActivity().unregisterReceiver(mConnectivityChangeReceiver);
	}
	
	private boolean isClientServiceRunning() {
		// Check if service is running.
		ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (ClientService_.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		
		return false;
	}
	
	private void onClientServiceStarted() {
		getActivity().findViewById(R.id.welcome_to_micphone).setVisibility(View.GONE);
		getActivity().findViewById(R.id.get_started).setVisibility(View.GONE);
		getActivity().findViewById(R.id.select_server_button).setVisibility(View.GONE);
		
		getActivity().findViewById(R.id.you_are_connected).setVisibility(View.VISIBLE);
	}
	
	private void onClientServiceStopped() {
		getActivity().findViewById(R.id.welcome_to_micphone).setVisibility(View.VISIBLE);
		getActivity().findViewById(R.id.get_started).setVisibility(View.VISIBLE);
		getActivity().findViewById(R.id.select_server_button).setVisibility(View.VISIBLE);
		
		getActivity().findViewById(R.id.you_are_connected).setVisibility(View.GONE);
	}
	
	public void registerButtonEvents() {
		// Get a reference to the button.
		mSelectServerButton = (Button) getActivity().findViewById(R.id.select_server_button);
		
		// Select server button.
		mSelectServerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDiscoverDialogFragment == null) {
					mDiscoverDialogFragment = new DiscoverDialogFragment_();
				}

				if (!mDiscoverDialogFragment.isAdded()) {
					mDiscoverDialogFragment.show(getActivity().getFragmentManager(), "DiscoverDialogFragment");	
				}
			}
		});
	}
	
	/**
	 * Checks the connectivity of the device.
	 * 
	 * Since WiFi is required to use the functions of the application,
	 * we show a dialog that prompts the user to enable WiFi and connect
	 * to a network.
	 */
	private boolean checkConnectivity() {
		// Create the WiFi dialog fragment in case we need it.
		if (mConnectWiFiDialogFragment == null) {
			mConnectWiFiDialogFragment = new ConnectWiFiDialogFragment_();
		}
		
		// Check if we have WiFi connectivity.
		Log.d(TAG, "Checking WiFi connectivity...");
		NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (!networkInfo.isConnected() && !isWiFiAPEnabled()) {
			if (!mConnectWiFiDialogFragment.isAdded()) {
				mConnectWiFiDialogFragment.show(getActivity().getFragmentManager(), "ConnectWiFiDialogFragment");
			}
			
			return false;
		} else {
			// WiFi was reconnected, dismiss the dialog automatically.
			if (mConnectWiFiDialogFragment.isAdded()) {
				mConnectWiFiDialogFragment.dismiss();
			}
			
			if (isWiFiAPEnabled()) {
				Log.d(TAG, "WiFi hotspot is enabled");
			} else {
				Log.d(TAG, "WiFi is connected");
			}
			
			return true;
		}
	}
	
	/**
	 * Utility function to check if WiFi AP is enabled on the device.
	 */
	private boolean isWiFiAPEnabled() {
		boolean isWiFiAPEnabled = false;
		WifiManager wifi = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		
		Method[] wmMethods = wifi.getClass().getDeclaredMethods();
        for (Method method : wmMethods){
            if(method.getName().equals("isWifiApEnabled")) {  
                try {
                	isWiFiAPEnabled = (Boolean) method.invoke(wifi);
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

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(Uri uri);
	}

}
