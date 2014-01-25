package sg.edu.nus.micphone;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.net.rtp.AudioGroup;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link ServerFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link ServerFragment#newInstance} factory method
 * to create an instance of this fragment.
 * 
 */
public class ServerFragment extends Fragment {
	private static AudioGroup mOutAudio;

	private OnFragmentInteractionListener mListener;
	private ServerNsd mServerNsd;
	private ServerConnection mServerConn;
	private boolean broadcasting = false;

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @return A new instance of fragment ServerFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static ServerFragment newInstance(String param1, String param2) {
		ServerFragment fragment = new ServerFragment();
		return fragment;
	}

	public ServerFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup AudioGroup for speakers
		if (ServerFragment.mOutAudio == null) {
			mOutAudio = new AudioGroup();
			mOutAudio.setMode(AudioGroup.MODE_MUTED);
		}

		mServerNsd = new ServerNsd(getActivity());
		mServerNsd.initializeNsd();
		mServerNsd.initializeRegistrationListener();

		this.startBroadCast();

	}

	private void startBroadCast() {
		if (!broadcasting) {
			mServerConn = new ServerConnection(mOutAudio);
			mServerNsd.registerService(mServerConn.getLocalPort());
			broadcasting = true;
		} else {
			// TODO if its already broadcasting then do something
		}
	}

	private void stopBroadCast() {
		if (broadcasting) {
			mServerNsd.tearDown();
			mServerConn.tearDown();
		}else{
			//TODO do we need to do anything its not a valid choice?
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_server, container, false);
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
