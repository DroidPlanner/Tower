package com.droidplanner.activitys.helpers;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import com.droidplanner.DroidPlannerApp.ConnectionStateListner;
import com.droidplanner.R;
import com.droidplanner.widgets.adapterViews.NavigationHubAdapter;

public abstract class SuperUI extends SuperActivity implements ConnectionStateListner {
    private ScreenOrientation screenOrientation = new ScreenOrientation(this);
    private InfoMenu infoMenu;

    /**
     * Application title.
     * Used to update the action bar when the navigation drawer opens/closes.
     */
    private CharSequence mAppName;

    /**
     * Activity label, used to update the action bar when the navigation drawer opens/closes.
     *
     * @since 1.2.0
     */
    private CharSequence mActivityLabel;

    /**
     * Activates the navigation drawer when the home button is clicked.
     *
     * @since 1.2.0
     */
    protected ActionBarDrawerToggle mDrawerToggle;

    /**
     * Navigation drawer used to navigate through the different hubs of the application.
     *
     * @since 1.2.0
     */
    protected DrawerLayout mNavDrawerLayout;

    /**
     * Expandable listview used as layout for the hubs of the application.
     *
     * @since 1.2.0
     */
    protected ExpandableListView mNavHubView;

    /**
     * Checks if the navigation drawer was initialized in onCreate() by children of this class.
     *
     * @since 1.2.0
     */
    private boolean mIsNavDrawerSet = false;

    public SuperUI() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenOrientation.unlock();
        infoMenu = new InfoMenu(drone);

        mAppName = getString(R.string.app_title);
        mActivityLabel = getTitle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsNavDrawerSet = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
/*
        if(!mIsNavDrawerSet)
            throw new IllegalStateException("setupNavDrawer() was not called in onCreate().");
*/
        app.conectionListner = this;
        drone.MavClient.queryConnectionState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        infoMenu.inflateMenu(menu, getMenuInflater());
        infoMenu.setupModeSpinner(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        infoMenu.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null) {
            //Sync the toggle state after onRestoreInstanceState has occurred.
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mDrawerToggle != null)
            mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Set up the navigation drawer for the children of this class.
     */
    protected void setupNavDrawer() {
        mNavDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavHubView = (ExpandableListView) findViewById(R.id.nav_drawer_container);

        mDrawerToggle = new ActionBarDrawerToggle(this, mNavDrawerLayout, R.drawable.ic_drawer,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                updateActionBar(mActivityLabel, getIcon());
            }

            @Override
            public void onDrawerOpened(View view) {
                setTitle(R.string.app_title);
                updateActionBar(mAppName, R.drawable.ic_launcher);
            }
        };

        mNavDrawerLayout.setDrawerListener(mDrawerToggle);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        //Set the adapter for the list view
        NavigationHubAdapter drawerAdapter = new NavigationHubAdapter(this, mNavDrawerLayout,
                mNavHubView);
        mNavHubView.setAdapter(drawerAdapter);

        mIsNavDrawerSet = true;
    }

    /**
     * Returns the activity icon id.
     *
     * @return activity icon id
     * @since 1.2.0
     */
    protected int getIcon() {
        return R.drawable.ic_action_plane;
    }

    /**
     * Updates the action bar title, and icon.
     *
     * @param actionBarTitle  new action bar title
     * @param actionBarIconId new action bar icon
     * @since 1.2.0
     */
    private void updateActionBar(CharSequence actionBarTitle, int actionBarIconId) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(actionBarTitle);
            actionBar.setIcon(actionBarIconId);
        }
        else {
            setTitle(actionBarTitle);
        }
    }

    public void notifyDisconnected() {
        invalidateOptionsMenu();
        /*
		if(armButton != null){
			armButton.setEnabled(false);
		}*/
        screenOrientation.unlock();
    }

    public void notifyConnected() {
        invalidateOptionsMenu();
		
		/*
		if(armButton != null){
			armButton.setEnabled(true);
		}
		*/
        screenOrientation.requestLock();
    }

}