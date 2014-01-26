package sg.edu.nus.micphone;

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
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;

import sg.edu.nus.micphone.client.DiscoverDialogFragment.DiscoverDialogFragmentListener;
import sg.edu.nus.micphone.server.ServerFragment_;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

@EActivity
@OptionsMenu(R.menu.main)
public class MainActivity extends Activity implements
		ClientFragment.OnFragmentInteractionListener,
		AboutFragment.OnFragmentInteractionListener,
		ServerFragment_.OnFragmentInteractionListener,
		DiscoverDialogFragmentListener {
	
	/** A debug tag used to filter messages from LogCat */
	private static final String TAG = "MainActivity";
	
	/** Keys and default values for shared preferences */
	private static final String SELECTED_DRAWER_ITEM_KEY = "selectedDrawerItem";
	private static final int SELECTED_DRAWER_ITEM_DEFAULT = 0;
	
	/** Variables used for the application drawer */
	private String[] mDrawerItems;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private int mDrawerSelectedItem;
	
	/** Title to show on the ActionBar */
	private String mActionBarSubtitle;
	
	/** Variable for the Android shared preference API */  
	private SharedPreferences mSharedPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Obtain the shared preference.
		mSharedPreferences = getSharedPreferences("MicPhone", Context.MODE_PRIVATE);
		
		// Initialize the drawer.
		initializeDrawer();
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		// Pass any configuration changes to the drawer toggle.
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO: If the navigation drawer is open, hide the action items
		// related to the content view.
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		
		// TODO: Handle other action buttons.
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Initializes the application drawer.
	 * 
	 * Fetches the list of items that should go into the drawer from
	 * drawer_item.xml, and restores the last shown drawer item.
	 */
	public void initializeDrawer() {
		// Obtain the application drawer and its items.
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		
		// Initialize the application drawer.
		mDrawerItems = getResources().getStringArray(R.array.drawer_items);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_item, mDrawerItems));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
		// Set a custom shadow that overlays the main content when the
		// drawer opens.
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		// Select the default drawer.
		mDrawerSelectedItem = mSharedPreferences.getInt(SELECTED_DRAWER_ITEM_KEY, SELECTED_DRAWER_ITEM_DEFAULT);
		selectDrawerItem(mDrawerSelectedItem);
		
		// Set the correct subtitle on the action bar.
		setActionBarSubtitle();
		
		// Enables the action bar application icon to open the drawer.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		mDrawerToggle = new ActionBarDrawerToggle(
				this, mDrawerLayout,
				R.drawable.ic_drawer,
				R.string.drawer_open,
				R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				setActionBarSubtitle();
				invalidateOptionsMenu();
			}
			
			public void onDrawerOpened(View drawerView) {
				getActionBar().setSubtitle(null);
				invalidateOptionsMenu();
			}
		};
		
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}
	
	/**
	 * Sets the subtitle on the action bar.
	 */
	private void setActionBarSubtitle() {
		// Set the correct title on the action bar.
		mActionBarSubtitle = mDrawerItems[mDrawerSelectedItem];
		getActionBar().setSubtitle(mActionBarSubtitle);
	}
	
	/**
	 * Selects the drawer item at a certain position.
	 * 
	 * Based on the position given, prepares the appropriate fragment for the
	 * given menu item and shows it in the main content area. Once this is
	 * done, the item is marked as selected in the application, and the drawer
	 * is closed.
	 *  
	 * @param position
	 */
	private void selectDrawerItem(int position) {
		// Update the main content by replacing fragments.
		Fragment fragment = null;
		switch(position) {
		case 0:
			fragment = new ClientFragment_();
			break;
		case 1:
			fragment = new ServerFragment_();
			break;
		case 2:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return;
		case 3:
			fragment = new AboutFragment();
			break;
		default:
			Log.e(TAG, "Menu item at " + position + " could not be found");
		}
		
		FragmentManager manager = getFragmentManager();
		manager.beginTransaction()
			.replace(R.id.content_frame, fragment)
			.commit();
		
		// Update selected item and title, then close the drawer.
		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
		
		// Saves the currently shown drawer item into shared preferences.
		Editor editor = mSharedPreferences.edit();
		editor.putInt(SELECTED_DRAWER_ITEM_KEY, position);
		editor.apply();
		
		// This is used to update the subtitle in the action bar.
		mDrawerSelectedItem = position;
	}
	
	/**
	 * A listener that handles the event when a drawer item is selected.
	 * 
	 * Right now, this listener simply proxies the selected position to the
	 * selectDrawerItem method, since that method is used elsewhere.
	 */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.d(TAG, "Item " + position + " clicked.");
			selectDrawerItem(position);
		}
	}

	/**
	 * A method that handles interactions with fragments shown by this activity.
	 * This is not used for now.
	 */
	@Override
	public void onFragmentInteraction(Uri uri) {
		// TODO Auto-generated method stub		
	}
	

	@Background
	public void onDiscoverDialogFragmentInteraction(InetAddress host, int port) {
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
				streamGroup.setMode(AudioGroup.MODE_ECHO_SUPPRESSION);
				
				micStream.setCodec(AudioCodec.GSM_EFR);
				micStream.setMode(AudioStream.MODE_SEND_ONLY);
				micStream.associate(host, remotePort);
				micStream.join(streamGroup);
				
				// Print debug information about group.
				Log.d(TAG, "Local addr: " + micStream.getLocalAddress() + ":" + micStream.getLocalPort());
				Log.d(TAG, "Remote addr: " + micStream.getRemoteAddress() + ":" + micStream.getRemotePort());
				
				// Obtain an AudioManager.
				AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
