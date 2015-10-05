package org.droidplanner.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.fragments.SettingsFragment;
import org.droidplanner.android.fragments.control.BaseFlightControlFragment;
import org.droidplanner.android.view.SlidingDrawer;

/**
 * This abstract activity provides its children access to a navigation drawer
 * interface.
 */
public abstract class DrawerNavigationUI extends SuperUI implements SlidingDrawer.OnDrawerOpenListener, SlidingDrawer.OnDrawerCloseListener {

    /**
     * Activates the navigation drawer when the home button is clicked.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * Navigation drawer used to access the different sections of the app.
     */
    private DrawerLayout mDrawerLayout;

    private SlidingDrawer actionDrawer;

    /**
     * Container for the activity content.
     */
    private FrameLayout contentLayout;

    private NavDrawerViewHolder mNavViewsHolder;

    /**
     * Clicking on an entry in the open navigation drawer updates this intent.
     * When the navigation drawer closes, the intent is used to navigate to the desired location.
     */
    private Intent mNavigationIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the drawer layout container.
        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer_navigation_ui, null);
        contentLayout = (FrameLayout) mDrawerLayout.findViewById(R.id.content_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                switch (drawerView.getId()) {
                    case R.id.navigation_drawer_container:
                        if (mNavigationIntent != null) {
                            startActivity(mNavigationIntent);
                            mNavigationIntent = null;
                        }
                        break;
                }
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        actionDrawer = (SlidingDrawer) mDrawerLayout.findViewById(R.id.action_drawer_container);
        actionDrawer.setOnDrawerCloseListener(this);
        actionDrawer.setOnDrawerOpenListener(this);
    }

    protected View getActionDrawer() {
        return actionDrawer;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode) {
            case BaseFlightControlFragment.FOLLOW_SETTINGS_UPDATE:
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(new Intent(SettingsFragment.ACTION_LOCATION_SETTINGS_UPDATED)
                                .putExtra(SettingsFragment.EXTRA_RESULT_CODE, resultCode));
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    /**
     * Intercepts the call to 'setContentView', and wrap the passed layout
     * within a DrawerLayout object. This way, the children of this class don't
     * have to do anything to benefit from the navigation drawer.
     *
     * @param layoutResID layout resource for the activity view
     */
    @Override
    public void setContentView(int layoutResID) {
        final View contentView = getLayoutInflater().inflate(layoutResID, mDrawerLayout, false);
        contentLayout.addView(contentView);
        setContentView(mDrawerLayout);

        initNavigationDrawer();
    }

    @Override
    protected void initToolbar(Toolbar toolbar) {
        super.initToolbar(toolbar);

        toolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                final float topMargin = getActionDrawerTopMargin();
                final int fullTopMargin = (int) (topMargin + (bottom - top));

                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) actionDrawer.getLayoutParams();
                if (lp.topMargin != fullTopMargin) {
                    lp.topMargin = fullTopMargin;
                    actionDrawer.requestLayout();
                }

                onToolbarLayoutChange(left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom);
            }
        });
    }

    protected void onToolbarLayoutChange(int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom){

    }

    protected float getActionDrawerTopMargin() {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
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
        final View containerView = findViewById(R.id.navigation_drawer_container);
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

        setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mAccount, new Intent(context, AccountActivity.class));

        setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mFlightData, new Intent(context, FlightActivity.class));

        setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mEditor, new Intent(context, EditorActivity.class));

        setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mLocator, new Intent(context, LocatorActivity.class));

        setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mSettings, new Intent(context, SettingsActivity.class));

        setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mCalibration, new Intent(context,
                ConfigurationActivity.class).putExtra(ConfigurationActivity
                .EXTRA_CONFIG_SCREEN_ID, R.id.navigation_calibration));

        setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mChecklist, new Intent(context,
                ConfigurationActivity.class).putExtra(ConfigurationActivity
                .EXTRA_CONFIG_SCREEN_ID, R.id.navigation_checklist));

        setupNavigationEntry(navDrawerEntryId, mNavViewsHolder.mParams, new Intent(context,
                ConfigurationActivity.class).putExtra(ConfigurationActivity
                .EXTRA_CONFIG_SCREEN_ID, R.id.navigation_params));

    }

    private void setupNavigationEntry(int currentEntryId, TextView navView, final Intent clickIntent) {
        if (navView == null) {
            return;
        }

        if (currentEntryId == navView.getId()) {
            //Bold the entry label
            navView.setTypeface(null, Typeface.BOLD);
            navView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
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
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
            });
        }
    }

    public boolean isActionDrawerOpened() {
        return actionDrawer.isOpened();
    }

    protected int getActionDrawerId() {
        return R.id.action_drawer_content;
    }

    /**
     * Called when the action drawer is opened.
     * Should be override by children as needed.
     */
    @Override
    public void onDrawerOpened() {
    }

    /**
     * Called when the action drawer is closed.
     * Should be override by children as needed.
     */
    @Override
    public void onDrawerClosed() {
    }

    public void openActionDrawer() {
        actionDrawer.animateOpen();
        actionDrawer.lock();
    }

    public void closeActionDrawer() {
        actionDrawer.animateClose();
        actionDrawer.lock();
    }

    protected abstract int getNavigationDrawerEntryId();

    /**
     * Holder class for the navigation entry views in the navigation drawer.
     * They are stored here to avoid re-instantiating through 'findViewById' which can be a bit
     * costly.
     */
    private static class NavDrawerViewHolder {
        final TextView mAccount;
        final TextView mFlightData;
        final TextView mEditor;
        final TextView mLocator;

        final TextView mSettings;

        final TextView mParams;
        final TextView mChecklist;
        final TextView mCalibration;

        private NavDrawerViewHolder(View containerView) {
            mAccount = (TextView) containerView.findViewById(R.id.navigation_account);
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
