package org.droidplanner.android.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.fragments.SettingsFragment;
import org.droidplanner.android.fragments.control.BaseFlightControlFragment;
import org.droidplanner.android.view.SlidingDrawer;
import org.w3c.dom.Text;

/**
 * This abstract activity provides its children access to a navigation drawer
 * interface.
 */
public abstract class DrawerNavigationUI extends SuperUI implements SlidingDrawer.OnDrawerOpenListener, SlidingDrawer.OnDrawerCloseListener, NavigationView.OnNavigationItemSelectedListener {

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

    /**
     * Clicking on an entry in the open navigation drawer updates this intent.
     * When the navigation drawer closes, the intent is used to navigate to the desired location.
     */
    private Intent mNavigationIntent;

    /**
     * Navigation drawer view
     */
    private NavigationView navigationView;

    private TextView accountLabel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the drawer layout container.
        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer_navigation_ui, null);
        contentLayout = (FrameLayout) mDrawerLayout.findViewById(R.id.content_layout);

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
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

        navigationView = (NavigationView) findViewById(R.id.navigation_drawer_container);
        navigationView.setNavigationItemSelectedListener(this);

        accountLabel = (TextView) findViewById(R.id.account_screen_label);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        LinearLayout llAccount = (LinearLayout) findViewById(R.id.navigation_account);
        llAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AccountActivity.class));
                drawer.closeDrawer(GravityCompat.START);
            }
        });

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

    /**
     * Manage Navigation drawer menu items
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        int id = menuItem.getItemId();

        switch (id) {
            case R.id.navigation_flight_data:
                mNavigationIntent = new Intent(this, FlightActivity.class);
                break;

            case R.id.navigation_editor:
                mNavigationIntent = new Intent(this, EditorActivity.class);
                break;

            case R.id.navigation_locator:
                mNavigationIntent = new Intent(this, LocatorActivity.class);
                break;

            case R.id.navigation_params:
                mNavigationIntent = new Intent(this, ConfigurationActivity.class)
                        .putExtra(ConfigurationActivity.EXTRA_CONFIG_SCREEN_ID, id);
                break;

            case R.id.navigation_checklist:
                mNavigationIntent = new Intent(this, ConfigurationActivity.class)
                        .putExtra(ConfigurationActivity.EXTRA_CONFIG_SCREEN_ID, id);
                break;

            case R.id.navigation_calibration:
                mNavigationIntent = new Intent(this, ConfigurationActivity.class)
                        .putExtra(ConfigurationActivity.EXTRA_CONFIG_SCREEN_ID, id);
                break;

            case R.id.navigation_settings:
                mNavigationIntent = new Intent(this, SettingsActivity.class);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void onToolbarLayoutChange(int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

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
    public void onResume(){
        super.onResume();
        updateNavigationDrawer();
    }

    private void updateNavigationDrawer() {
        final int navDrawerEntryId = getNavigationDrawerMenuItemId();
        switch (navDrawerEntryId) {
            case R.id.navigation_account:
                accountLabel.setTypeface(null, Typeface.BOLD);
                break;

            default:
                navigationView.setCheckedItem(navDrawerEntryId);
                break;
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
            mDrawerToggle.syncState();
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

    protected abstract int getNavigationDrawerMenuItemId();
}
