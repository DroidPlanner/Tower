package com.droidplanner.activitys.helpers;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;
import com.droidplanner.DroidPlannerApp.ConnectionStateListner;
import com.droidplanner.R;
import com.droidplanner.fragments.helpers.OfflineMapFragment;
import com.droidplanner.utils.Utils;
import com.droidplanner.widgets.adapterViews.NavigationHubAdapter;

import static com.droidplanner.utils.Constants.*;

/**
 * Parent activity for all ui related activities.
 */
public abstract class SuperUI extends SuperActivity implements ConnectionStateListner {

    /**
     * Application title.
     * Used to update the action bar when the navigation drawer opens/closes.
     *
     * @since 1.2.0
     */
    private static final int LABEL_RESOURCE = R.string.app_title;

    private ScreenOrientation screenOrientation = new ScreenOrientation(this);
    private InfoMenu infoMenu;

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
     * Menu drawer containing actions for the current activity.
     * @since 1.2.0
     */
    protected View mNavMenuView;

    /**
     * Checks if the navigation drawer was initialized in onCreate() by children of this class.
     *
     * @since 1.2.0
     */
    private boolean mIsNavDrawerSet = false;

    /**
     * Filter for the intents this activity is listening to.
     * @since 1.2.0
     */
    protected final IntentFilter mIntentFilter = new IntentFilter();

    /**
     * Handle to the map fragment used to update the map padding when the navigation drawer
     * opens/closes.
     * @since 1.2.0
     */
    protected OfflineMapFragment mMapFragment;

    /**
     * Broadcast receiver to handle received broadcast events.
     * @since 1.2.0
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final Bundle extras = intent.getExtras();
            if(ACTION_MENU_DRAWER_LOCK_UPDATE.equals(action)){
                boolean isMenuDrawerLocked = extras.getBoolean(EXTRA_MENU_DRAWER_LOCK,
                        DEFAULT_MENU_DRAWER_LOCK);
                updateMenuDrawerLockMode(isMenuDrawerLocked);
            }
        }
    };

    public SuperUI() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenOrientation.unlock();
        infoMenu = new InfoMenu(drone);

        //Add the broadcast intents to filter
        mIntentFilter.addAction(ACTION_MENU_DRAWER_LOCK_UPDATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsNavDrawerSet = false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Check if the navigation drawer was setup.
        if (!mIsNavDrawerSet)
            throw new IllegalStateException("setupNavDrawer() was not called in onCreate().");

        //Register the broadcast receiver
        registerReceiver(mBroadcastReceiver, mIntentFilter);

        app.conectionListner = this;
        drone.MavClient.queryConnectionState();
    }

    @Override
    protected void onStop(){
        super.onStop();

        //Unregister the broadcast receiver
        unregisterReceiver(mBroadcastReceiver);
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
    public void onResume(){
        super.onResume();
        updateHubSelection();
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
        //Retrieves the map fragment, if it exists
        mMapFragment = (OfflineMapFragment) getFragmentManager().findFragmentById(R.id
                .mapFragment);

        final View contentLayout = findViewById(R.id.activity_content_view);

        mNavMenuView = findViewById(R.id.nav_menu_drawer_container);
        mNavHubView = (ExpandableListView) findViewById(R.id.nav_drawer_container);

        mNavDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavDrawerLayout.setScrimColor(Color.TRANSPARENT);
        mNavDrawerLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mNavDrawerLayout.isDrawerVisible(mNavHubView))
                    return mNavDrawerLayout.onTouchEvent(event);

                return contentLayout.dispatchTouchEvent(event);
            }
        });

        //To prevent the drawer layout from intercepting the back button.
        mNavDrawerLayout.setFocusableInTouchMode(false);

        mDrawerToggle = new ActionBarDrawerToggle(this, mNavDrawerLayout, R.drawable.ic_drawer,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                updateActionBar(getLabelResource());

                switch(drawerView.getId()){
                    //Navigation drawer
                    case R.id.nav_drawer_container:
                        if(mNavMenuView != null){
                            //Open the menu drawer if it's in open lock mode.
                            if(mNavDrawerLayout.getDrawerLockMode(mNavMenuView) == DrawerLayout.LOCK_MODE_LOCKED_OPEN)
                                mNavDrawerLayout.openDrawer(mNavMenuView);
                        }

                        if(mMapFragment != null){
                            //Reset the map left padding
                            mMapFragment.setLeftPadding(0);
                        }
                        break;

                    //Menu drawer
                    case R.id.nav_menu_drawer_container:
                        if(mMapFragment != null){
                            //Reset the map right padding.
                            mMapFragment.setRightPadding(0);
                        }
                        break;
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                updateActionBar(LABEL_RESOURCE);

                switch(drawerView.getId()){
                    case R.id.nav_drawer_container:
                        if(mNavMenuView != null){
                            //Close the menu drawer if it's opened.
                            if(mNavDrawerLayout.isDrawerVisible(mNavMenuView))
                                mNavDrawerLayout.closeDrawer(mNavMenuView);
                        }

                        if(mMapFragment != null){
                            //Update the map left padding.
                            int leftPadding = drawerView.getLayoutParams().width;
                            mMapFragment.setLeftPadding(leftPadding);
                        }
                        break;

                    //Menu drawer
                    case R.id.nav_menu_drawer_container:
                        if(mMapFragment != null){
                            //Update the map right padding.
                            int rightPadding = drawerView.getLayoutParams().width;
                            mMapFragment.setRightPadding(rightPadding);
                        }
                        break;
                }
            }
        };

        mNavDrawerLayout.setDrawerListener(mDrawerToggle);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        //Set the adapter for the list view
        NavigationHubAdapter drawerAdapter = new NavigationHubAdapter(getApplicationContext(),
                getNavigationHubItem(), mNavDrawerLayout, mNavHubView);
        mNavHubView.setAdapter(drawerAdapter);

        updateMenuDrawerLockMode(Utils.isMenuDrawerLocked(getApplicationContext()));

        mIsNavDrawerSet = true;
    }

    protected abstract NavigationHubAdapter.HubItem getNavigationHubItem();

    private void updateHubSelection(){
        NavigationHubAdapter.HubItem hubItem = getNavigationHubItem();

        //Highlight the selected item
        NavigationHubAdapter.HubItem hubParent = hubItem.getParent();
        if (hubParent == null) {
            int position = mNavHubView.getFlatListPosition(ExpandableListView
                    .getPackedPositionForGroup(hubItem.getPosition()));
            mNavHubView.setItemChecked(position, true);
        }
        else {
            int groupPosition = hubParent.getPosition();
            mNavHubView.expandGroup(groupPosition);

            int position = mNavHubView.getFlatListPosition(ExpandableListView
                    .getPackedPositionForChild(groupPosition, hubItem.getPosition()));

            mNavHubView.setItemChecked(position, true);
        }
    }

    /**
     * Check the current user preference for the menu drawer, and update appropriately.
     *
     * @since 1.2.0
     */
    protected void updateMenuDrawerLockMode(boolean isMenuDrawerLocked) {
        if (mNavMenuView != null) {
            //Keep the menu open based on user preferences.
            if (isMenuDrawerLocked) {
                mNavDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN,
                        mNavMenuView);

                //Close the drawer if the navigation drawer is already opened.
                if(mNavHubView != null && mNavDrawerLayout.isDrawerOpen(mNavHubView)){
                    mNavDrawerLayout.closeDrawer(mNavMenuView);

                    //Update the map left padding
                    if (mMapFragment != null) {
                        int leftPadding = mNavHubView.getLayoutParams().width;
                        mMapFragment.setLeftPadding(leftPadding);
                    }
                }
                else {
                    //Update the map right padding
                    if (mMapFragment != null) {
                        int rightPadding = mNavMenuView.getLayoutParams().width;
                        mMapFragment.setRightPadding(rightPadding);
                    }
                }
            }
            else {
                mNavDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
                        mNavMenuView);

                //Reset the map right padding
                if(mMapFragment != null){
                    mMapFragment.setRightPadding(0);
                }
            }
        }
    }

    /**
     * Returns the activity label resource.
     * The label is used to update the action bar when the navigation drawer opens/closes.
     * This is done here because setting the label for the launcher activity overrides the label
     * for the application.
     *
     * @return activity label resource
     * @since 1.2.0
     */
    protected abstract int getLabelResource();

    /**
     * Updates the action bar title, and icon.
     *
     * @param actionBarTitleResource new action bar title resource
     * @since 1.2.0
     */
    private void updateActionBar(int actionBarTitleResource) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(actionBarTitleResource);
        }
        else {
            setTitle(actionBarTitleResource);
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