package org.droidplanner.android.activities;

import org.droidplanner.R;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.activities.interfaces.HelpProvider;
import org.droidplanner.android.widgets.adapterViews.NavigationDrawerAdapter;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

/**
 * This abstract activity provides its children access to a navigation drawer
 * interface.
 */
public abstract class DrawerNavigationUI extends SuperUI implements
		HelpProvider {

	/**
	 * Activates the navigation drawer when the home button is clicked.
	 */
	private ActionBarDrawerToggle mDrawerToggle;

	/**
	 * Navigation drawer used to access the different sections of the app.
	 */
	private DrawerLayout mDrawerLayout;

	/**
	 * Expandable listview used as layout for the app sections.
	 */
	private ExpandableListView mNavHubView;

	/**
	 * Adapter used to populate the expandable list view.
	 */
	private NavigationDrawerAdapter mNavDrawerAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Retrieve the drawer layout container.
		mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(
				R.layout.activity_drawer_navigation_ui, null);

		mNavHubView = (ExpandableListView) mDrawerLayout
				.findViewById(R.id.nav_drawer_container);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close);

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
		}

		// Set the adapter for the list view
		mNavDrawerAdapter = new NavigationDrawerAdapter(this);
		mNavDrawerAdapter.attachExpandableListView();
	}

	/**
	 * Intercepts the call to 'setContentView', and wrap the passed layout
	 * within a DrawerLayout object. This way, the children of this class don't
	 * have to do anything to benefit from the navigation drawer.
	 * 
	 * @param layoutResID
	 *            layout resource for the activity view
	 */
	@Override
	public void setContentView(int layoutResID) {
		final View contentView = getLayoutInflater().inflate(layoutResID,
				mDrawerLayout, false);
		mDrawerLayout.addView(contentView, 0);
		setContentView(mDrawerLayout);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (mDrawerToggle != null)
			mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		if (mDrawerToggle != null) {
			// Sync the toggle state after onRestoreInstanceState has occurred.
			mDrawerToggle.syncState();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mNavDrawerAdapter.refreshHubView();
	}

	public DrawerLayout getDrawerLayout() {
		return mDrawerLayout;
	}

	public ExpandableListView getNavHubView() {
		return mNavHubView;
	}
}
