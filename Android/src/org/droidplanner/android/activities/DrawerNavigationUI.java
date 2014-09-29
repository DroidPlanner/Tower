package org.droidplanner.android.activities;

import org.droidplanner.R;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.activities.interfaces.HelpProvider;
import org.droidplanner.android.fragments.helpers.HelpDialogFragment;

import android.app.ActionBar;
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
public abstract class DrawerNavigationUI extends SuperUI implements HelpProvider {

	/**
	 * Activates the navigation drawer when the home button is clicked.
	 */
	private ActionBarDrawerToggle mDrawerToggle;

	/**
	 * Navigation drawer used to access the different sections of the app.
	 */
	private DrawerLayout mDrawerLayout;

    /**
     * Clicking on an entry in the open navigation drawer updates this intent.
     * When the navigation drawer closes, the intent is used to navigate to the desired location.
     */
    private Intent mNavigationIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Retrieve the drawer layout container.
		mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(
				R.layout.activity_drawer_navigation_ui, null);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
				R.string.drawer_open, R.string.drawer_close){

            @Override
            public void onDrawerClosed(View drawerView){
                if(mNavigationIntent != null){
                    startActivity(mNavigationIntent);
                    mNavigationIntent = null;
                }
            }
        };

		mDrawerLayout.setDrawerListener(mDrawerToggle);


        //TODO: check if needed.
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
		}
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

        setupNavigationDrawer();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (mDrawerToggle != null)
			mDrawerToggle.onConfigurationChanged(newConfig);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_navigation_hub, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item))
			return true;

        if(item.getItemId() == R.id.menu_help){
            final HelpDialogFragment helpDialog = HelpDialogFragment.newInstance();
            helpDialog.show(getSupportFragmentManager(), "Help Dialog");
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
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        setupNavigationDrawer();
    }

	public DrawerLayout getDrawerLayout() {
		return mDrawerLayout;
	}

    /**
     * Initializes the navigation drawer.
     */
    public void setupNavigationDrawer(){
        final Context context = getApplicationContext();
        final int navDrawerEntryId = getNavigationDrawerEntryId();

        setupNavigationEntry(navDrawerEntryId, R.id.navigation_flight_data, new Intent(context,
                FlightActivity.class));

        setupNavigationEntry(navDrawerEntryId, R.id.navigation_editor, new Intent(context,
                EditorActivity.class));

        setupNavigationEntry(navDrawerEntryId, R.id.navigation_settings, new Intent(context,
                SettingsActivity.class));

        setupNavigationEntry(navDrawerEntryId, R.id.navigation_tuning, new Intent(context,
                ConfigurationActivity.class).putExtra(ConfigurationActivity.EXTRA_CONFIG_SCREEN_ID,
                R.id.navigation_tuning));

        setupNavigationEntry(navDrawerEntryId, R.id.navigation_radio, new Intent(context,
                ConfigurationActivity.class).putExtra(ConfigurationActivity
                .EXTRA_CONFIG_SCREEN_ID, R.id.navigation_radio));

        setupNavigationEntry(navDrawerEntryId, R.id.navigation_calibration, new Intent(context,
                ConfigurationActivity.class).putExtra(ConfigurationActivity
                .EXTRA_CONFIG_SCREEN_ID, R.id.navigation_calibration));

        setupNavigationEntry(navDrawerEntryId, R.id.navigation_checklist, new Intent(context,
                ConfigurationActivity.class).putExtra(ConfigurationActivity
                .EXTRA_CONFIG_SCREEN_ID, R.id.navigation_checklist));

        setupNavigationEntry(navDrawerEntryId, R.id.navigation_params, new Intent(context,
                ConfigurationActivity.class).putExtra(ConfigurationActivity
                .EXTRA_CONFIG_SCREEN_ID, R.id.navigation_params));

    }

    private void setupNavigationEntry(int currentEntryId, int targetEntryId, final Intent clickIntent){
        final TextView navigationView = (TextView) findViewById(targetEntryId);
        if(navigationView == null){
            return;
        }

        if(currentEntryId == targetEntryId){
            //Bold the entry label
            navigationView.setTypeface(null, Typeface.BOLD);
            navigationView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.closeDrawer(Gravity.START);
                }
            });
        }
        else{
            navigationView.setTypeface(null, Typeface.NORMAL);
            navigationView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickIntent != null) {
                        mNavigationIntent = clickIntent;
                    }
                    mDrawerLayout.closeDrawer(Gravity.START);
                }
            });
        }
    }

    protected abstract int getNavigationDrawerEntryId();
}
