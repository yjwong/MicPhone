package sg.edu.nus.micphone;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends Activity implements
		ClientFragment.OnFragmentInteractionListener,
		AboutFragment.OnFragmentInteractionListener {
	
	/** A debug tag used to filter messages from LogCat */
	private static final String TAG = "MainActivity";
	
	/** Keys and default values for shared preferences */
	private static final String SELECTED_DRAWER_ITEM_KEY = "selectedDrawerItem";
	private static final int SELECTED_DRAWER_ITEM_DEFAULT = 0;
	
	/** Variables used for the application drawer */
	private String[] mDrawerItems;
	@ViewById(R.id.drawer_layout)
	protected DrawerLayout mDrawerLayout;
	@ViewById(R.id.left_drawer)
	protected ListView mDrawerList;
	
	/** Variable for the Android shared preference API */  
	private SharedPreferences mSharedPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Obtain the shared preference.
		mSharedPreferences = getSharedPreferences("MicPhone", Context.MODE_PRIVATE);
	}
	
	/**
	 * Initializes the application drawer.
	 * 
	 * Fetches the list of items that should go into the drawer from
	 * drawer_item.xml, and restores the last shown drawer item.
	 */
	@AfterViews
	public void initializeDrawer() {
		// Initialize the application drawer.
		mDrawerItems = getResources().getStringArray(R.array.drawer_items);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_item, mDrawerItems));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
		// Set a custom shadow that overlays the main content when the
		// drawer opens.
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		// Select the default drawer.
		int selectedDrawerItem = mSharedPreferences.getInt(SELECTED_DRAWER_ITEM_KEY, SELECTED_DRAWER_ITEM_DEFAULT);
		selectDrawerItem(selectedDrawerItem);
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
		if (position == 0) {
			fragment = new ClientFragment();
		} else if (position == 3) {
			fragment = new AboutFragment();
		} else {
			Log.e(TAG, "Menu item at " + position + "could not be found");
			return;
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
}
