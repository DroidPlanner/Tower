package org.droidplanner.android.activities;

import org.droidplanner.R;
import org.droidplanner.android.activities.helpers.SuperUI;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * This abstract activity provides its children access to a navigation drawer
 * interface.
 */
public abstract class DrawerNavigationUI extends SuperUI {

	/**
	 * Activates the navigation drawer when the home button is clicked.
	 */
	private ActionBarDrawerToggle mDrawerToggle;

	/**
	 * Navigation drawer used to access the different sections of the app.
	 */
	private DrawerLayout mDrawerLayout;

	private NavDrawerViewHolder mNavViewsHolder;

	/**
	 * Clicking on an entry in the open navigation drawer updates this intent.
	 * When the navigation drawer closes, the intent is used to navigate to the
	 * desired location.
	 */
	private Intent mNavigationIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Retrieve the drawer layout container.
		mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(
				R.layout.activity_drawer_navigation_ui, null);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
				R.string.drawer_open, R.string.drawer_close) {

			@Override
			public void onDrawerClosed(View drawerView) {
				if (mNavigationIntent != null) {
					startActivity(mNavigationIntent);
					mNavigationIntent = null;
				}
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
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
		final View contentView = getLayoutInflater().inflate(layoutResID, mDrawerLayout, false);
		mDrawerLayout.addView(contentView, 0);
		setContentView(mDrawerLayout);

		initNavigationDrawer();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (mDrawerToggle != null)
			mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

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
		updateNavigationDrawer();
	}

	/**
	 * Initializes the navigation drawer.
	 */
	private void initNavigationDrawer() {
		final View containerView = findViewById(R.id.nav_drawer_container);
		if (containerView != null) {
			mNavViewsHolder = new NavDrawerViewHolder(containerView);
		}
	}

	private void updateNavigationDrawer() {
		if (mNavViewsHolder == null) {
			return;
		}

		final Context context = getApplicationContext();
		final int navDrawerEntryId = getNavigationDrawerEntryId();

		setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mFlightData, new Intent(context,
				FlightActivity.class));

		setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mEditor, new Intent(context,
				EditorActivity.class));

		setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mLocator, new Intent(context,
				LocatorActivity.class));

		setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mSettings, new Intent(context,
				SettingsActivity.class));

		setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mCalibration, new Intent(context,
				ConfigurationActivity.class).putExtra(ConfigurationActivity.EXTRA_CONFIG_SCREEN_ID,
				R.id.navigation_calibration));

		setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mChecklist, new Intent(context,
				ConfigurationActivity.class).putExtra(ConfigurationActivity.EXTRA_CONFIG_SCREEN_ID,
				R.id.navigation_checklist));

		setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mParams, new Intent(context,
				ConfigurationActivity.class).putExtra(ConfigurationActivity.EXTRA_CONFIG_SCREEN_ID,
				R.id.navigation_params));

	}

	private void setupNavigationEntry(int currentEntryId, TextView navView, final Intent clickIntent) {
		if (navView == null) {
			return;
		}

		if (currentEntryId == navView.getId()) {
			// Bold the entry label
			navView.setTypeface(null, Typeface.BOLD);
			navView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mDrawerLayout.closeDrawer(Gravity.START);
				}
			});
		} else {
			navView.setTypeface(null, Typeface.NORMAL);
			navView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (clickIntent != null) {
						mNavigationIntent = clickIntent;
					}
					mDrawerLayout.closeDrawer(Gravity.START);
				}
			});
		}
	}

	protected abstract int getNavigationDrawerEntryId();

	/**
	 * Holder class for the navigation entry views in the navigation drawer.
	 * They are stored here to avoid re-instantiating through 'findViewById'
	 * which can be a bit costly.
	 */
	private static class NavDrawerViewHolder {
		final TextView mFlightData;
		final TextView mEditor;
		final TextView mLocator;

		final TextView mSettings;

		final TextView mParams;
		final TextView mChecklist;
		final TextView mCalibration;

		private NavDrawerViewHolder(View containerView) {
			mFlightData = (TextView) containerView.findViewById(R.id.navigation_flight_data);
			mEditor = (TextView) containerView.findViewById(R.id.navigation_editor);
			mLocator = (TextView) containerView.findViewById(R.id.navigation_locator);
			mSettings = (TextView) containerView.findViewById(R.id.navigation_settings);
			mParams = (TextView) containerView.findViewById(R.id.navigation_params);
			mChecklist = (TextView) containerView.findViewById(R.id.navigation_checklist);
			mCalibration = (TextView) containerView.findViewById(R.id.navigation_calibration);
		}
	}
}
