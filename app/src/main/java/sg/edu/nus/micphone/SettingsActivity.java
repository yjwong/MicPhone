package sg.edu.nus.micphone;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
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


// Do not uncomment unless you know what is going on
// Seriously, I mean it
// Magic, DO NOT TOUCH
public class SettingsActivity extends PreferenceActivity //implements
//										ClientFragment.OnFragmentInteractionListener,
//										AboutFragment.OnFragmentInteractionListener,
//										ServerFragment.OnFragmentInteractionListener
{	
/*	private static final String TAG = "SettingActivity";

	private static final String SELECTED_DRAWER_ITEM_KEY = "selectedDrawerItem";
	private static final int SELECTED_DRAWER_ITEM_DEFAULT = 0;
	
	private String[] mDrawerItems;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private int mDrawerSelectedItem;
	
	private String mActionBarSubtitle;
	
	private SharedPreferences mSharedPreferences;
	
*/	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
//		mSharedPreferences = getSharedPreferences("MicPhone", Context.MODE_PRIVATE);
		
//		initializeDrawer();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
/*	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		
		mDrawerToggle.syncState();
	}

	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(mDrawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public void initializeDrawer()
	{
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		
		mDrawerItems = getResources().getStringArray(R.array.drawer_items);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_item, mDrawerItems));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		mDrawerSelectedItem = mSharedPreferences.getInt(SELECTED_DRAWER_ITEM_KEY, SELECTED_DRAWER_ITEM_DEFAULT);
		selectDrawerItem(mDrawerSelectedItem);
		
		setActionBarSubtitle();
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close)
		{
			public void onDrawerClosed(View view)
			{
				setActionBarSubtitle();
				invalidateOptionsMenu();
			}
			
			public void onDrawerOpened(View drawerView)
			{
				getActionBar().setSubtitle(null);
				invalidateOptionsMenu();
				
			}
		};
		
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
	}
	
	private void setActionBarSubtitle()
	{
		mActionBarSubtitle = mDrawerItems[mDrawerSelectedItem];
		getActionBar().setSubtitle(mActionBarSubtitle);
	}
	
	private void selectDrawerItem(int position)
	{
		Fragment fragment = null;
		
		switch(position)
		{
			case 0:
				fragment = new ClientFragment();
				break;
			case 1:
				fragment = new ServerFragment();
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
		manager.beginTransaction().replace(R.id.content_frame, fragment).commit();
		
		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
		
		Editor editor = mSharedPreferences.edit();
		editor.putInt(SELECTED_DRAWER_ITEM_KEY, position);
		editor.apply();
		mDrawerSelectedItem = position;
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parents, View view, int position, long id)
		{
			Log.d(TAG, "Item " + position + " clicked.");
			selectDrawerItem(position);
		}
	}

	@Override
	public void onFragmentInteraction(Uri uri) {
		// TODO Auto-generated method stub
		
	}
*/
}
	
