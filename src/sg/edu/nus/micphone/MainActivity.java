package sg.edu.nus.micphone;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
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
	
	private static final String TAG = "MainActivity";
	
	private String[] mDrawerItems;
	@ViewById(R.id.drawer_layout)
	protected DrawerLayout mDrawerLayout;
	@ViewById(R.id.left_drawer)
	protected ListView mDrawerList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@AfterViews
	public void initializeDrawer() {
		// Initialize the application drawer.
		mDrawerItems = getResources().getStringArray(R.array.drawer_items);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_item, mDrawerItems));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
		// Select the default drawer.
		// TODO: Save this choice to shared preference.
		selectDrawerItem(0);
	}
	
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
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.d(TAG, "Item " + position + " clicked.");
			selectDrawerItem(position);
		}
	}

	@Override
	public void onFragmentInteraction(Uri uri) {
		// TODO Auto-generated method stub		
	}
}
