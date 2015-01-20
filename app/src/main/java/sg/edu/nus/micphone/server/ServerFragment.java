package sg.edu.nus.micphone.server;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.net.rtp.AudioGroup;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;

import sg.edu.nus.micphone.R;

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
	private AudioManager mAudioManager;
	private OnFragmentInteractionListener mListener;
	private ServerNsd mServerNsd;
	private boolean broadcasting = false;
	
	private ConnectionHandler mConnectionHandler;
	private Thread mConnectionHandlerThread;

	/** Buttons */
	private Button mButtonStartServer;
	private Button mButtonStopServer;

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

		mAudioManager = (AudioManager) getActivity().getSystemService(
				Context.AUDIO_SERVICE);

		mServerNsd = new ServerNsd(getActivity());
		mServerNsd.initializeNsd();
		mServerNsd.initializeRegistrationListener();

	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.stopBroadCast();
	}

	protected void startBroadCast() {
        StartBroadcastTask task = new StartBroadcastTask();
        task.execute();
	}

	private void stopBroadCast() {
		if (broadcasting) {
			mServerNsd.tearDown();
			mAudioManager.setSpeakerphoneOn(false);
			broadcasting = false;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_server, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		// Obtain references to the buttons.
		mButtonStartServer = (Button) getActivity().findViewById(
				R.id.start_server);
		mButtonStopServer = (Button) getActivity().findViewById(
				R.id.stop_server);

		// Assign appropriate events to buttons.
		mButtonStartServer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startBroadCast();
			}
		});

		mButtonStopServer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stopBroadCast();
			}
		});
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
		this.stopBroadCast();
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

    private class StartBroadcastTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (!broadcasting) {
                try {
                    mConnectionHandler = new ConnectionHandler(getActivity(), mOutAudio);
                    mConnectionHandlerThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mConnectionHandler.start();
                        }
                    });

                    mConnectionHandlerThread.start();
                    mServerNsd.registerService(mConnectionHandler.getLocalPort());
                    broadcasting = true;

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

}
