package co.aerobotics.android.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.activities.helpers.SuperUI;
import co.aerobotics.android.fragments.SettingsFragment;
import co.aerobotics.android.fragments.control.BaseFlightControlFragment;
import co.aerobotics.android.utils.prefs.DroidPlannerPrefs;
import co.aerobotics.android.view.SlidingDrawer;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.CapabilityApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;

/**
 * This abstract activity provides its children access to a navigation drawer
 * interface.
 */
public abstract class DrawerNavigationUI extends SuperUI implements
        SlidingDrawer.OnDrawerOpenListener,
    SlidingDrawer.OnDrawerCloseListener,
    NavigationView.OnNavigationItemSelectedListener{

    public final static String TOGGLE_TELEMETRY = "toggle_telemetry_view";
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private static final IntentFilter filter = new IntentFilter();
    static {
        filter.addAction(AttributeEvent.TYPE_UPDATED);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case AttributeEvent.TYPE_UPDATED:
                    updateCompassCalibrationAvailability();
                    break;
            }
        }
    };

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

    /**
     * Compass calibration menu item. This is used to enable/disable access to compass calibration
     * based on the vehicle type.
     */
    private MenuItem compassCalibration;

    /**
     * Navigation view settings menu
     */
    private NavigationView settingsMenu;

    private TextView accountLabel;

    private MixpanelAPI mMixpanel;

    private Switch telemSwitch;

    private final CapabilityApi.FeatureSupportListener featureSupportListener = new CapabilityApi.FeatureSupportListener() {
        @Override
        public void onFeatureSupportResult(String featureId, int result, Bundle resultInfo) {
            switch(featureId) {
                case CapabilityApi.FeatureIds.COMPASS_CALIBRATION:
                    boolean isSupported = result == CapabilityApi.FEATURE_SUPPORTED;
                    compassCalibration.setVisible(isSupported);
                    compassCalibration.setEnabled(isSupported);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMixpanel = MixpanelAPI.getInstance(this, DroidPlannerApp.getInstance().getMixpanelToken());
        // Retrieve the drawer layout container.
        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(co.aerobotics.android.R.layout.activity_drawer_navigation_ui, null);
        contentLayout = (FrameLayout) mDrawerLayout.findViewById(co.aerobotics.android.R.id.content_layout);

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, co.aerobotics.android.R.string.drawer_open, co.aerobotics.android.R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                switch (drawerView.getId()) {
                    case co.aerobotics.android.R.id.navigation_drawer:
                        if (mNavigationIntent != null) {
                            startActivity(mNavigationIntent);
                            mNavigationIntent = null;
                        }
                        break;
                }
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        actionDrawer = (SlidingDrawer) mDrawerLayout.findViewById(co.aerobotics.android.R.id.action_drawer_container);
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

        navigationView = (NavigationView) findViewById(co.aerobotics.android.R.id.navigation_drawer_view);
        if (navigationView != null) {
            navigationView.inflateHeaderView(DroidPlannerPrefs.ENABLE_DRONESHARE_ACCOUNT
                ? co.aerobotics.android.R.layout.nav_header_droneshare
                : co.aerobotics.android.R.layout.nav_header_main);
            navigationView.setNavigationItemSelectedListener(this);
            Menu navigationMenu = navigationView.getMenu();
            compassCalibration = navigationMenu.findItem(co.aerobotics.android.R.id.navigation_compass_calibration);


            View navigationHeaderView = navigationView.getHeaderView(0);
            accountLabel = (TextView) navigationHeaderView.findViewById(co.aerobotics.android.R.id.account_screen_label);

            LinearLayout llAccount = (LinearLayout) navigationHeaderView.findViewById(co.aerobotics.android.R.id.navigation_account);
            if (llAccount != null) {
                llAccount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getApplicationContext(), AccountActivity.class));
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                    }
                });
            }

            MenuItem switchItem = navigationMenu.findItem(R.id.navigation_telem_switch);
            switchItem.setActionView(R.layout.switch_item);

            telemSwitch = (Switch) navigationMenu.findItem(R.id.navigation_telem_switch).getActionView().findViewById(R.id.nav_switch);
            if(DroidPlannerApp.getInstance().hideTelemetry){
                telemSwitch.setChecked(true);
            } else {
                telemSwitch.setChecked(false);
            }
            telemSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    if(checked){
                        DroidPlannerApp.getInstance().hideTelemetry = true;
                    } else {
                        DroidPlannerApp.getInstance().hideTelemetry = false;
                    }

                    notifyStatusChange();
                }
            });



        }

        settingsMenu = (NavigationView) findViewById(co.aerobotics.android.R.id.navigation_drawer_settings);
        if (settingsMenu != null) {
            settingsMenu.setNavigationItemSelectedListener(this);
        }
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 100);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(TOGGLE_TELEMETRY);
            LocalBroadcastManager.getInstance(DrawerNavigationUI.this).sendBroadcast(intent);
        }
    };

    @Override
    protected void onDroneConnected(){
        super.onDroneConnected();
        updateCompassCalibrationAvailability();
        getBroadcastManager().registerReceiver(receiver, filter);
    }

    @Override
    protected void onDroneDisconnected(){
        super.onDroneDisconnected();
        getBroadcastManager().unregisterReceiver(receiver);
        updateCompassCalibrationAvailability();
    }

    private void updateCompassCalibrationAvailability() {
        Drone drone = dpApp.getDrone();
        if(drone != null){
            CapabilityApi.getApi(drone).checkFeatureSupport(CapabilityApi.FeatureIds.COMPASS_CALIBRATION, featureSupportListener);
        }
        else{
            compassCalibration.setVisible(false);
            compassCalibration.setEnabled(false);
        }
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

            case co.aerobotics.android.R.id.navigation_editor:
                mNavigationIntent = new Intent(this, EditorActivity.class);
                break;

            case co.aerobotics.android.R.id.navigation_checklist:
                mNavigationIntent = new Intent(this, ConfigurationActivity.class)
                        .putExtra(ConfigurationActivity.EXTRA_CONFIG_SCREEN_ID, id);
                break;
            case R.id.navigation_farms:
                mNavigationIntent = new Intent(this, FarmManagerActivity.class);
                break;
            case co.aerobotics.android.R.id.navigation_settings:
                mNavigationIntent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.navigation_log_out:
                confirmUserLogOut(this);
                break;
            case R.id.navigation_tour:
                mNavigationIntent = new Intent(this, EditorActivity.class).putExtra("startTour", "start");
                break;

        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
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
            case co.aerobotics.android.R.id.navigation_account:
                if (accountLabel != null) {
                    accountLabel.setTypeface(null, Typeface.BOLD);
                }
                break;

            default:
                if (navigationView != null)
                    navigationView.setCheckedItem(navDrawerEntryId);
                break;
        }

        MenuItem settings = settingsMenu.getMenu().findItem(co.aerobotics.android.R.id.navigation_settings);
        if(settings != null){
            settings.setChecked(false);
        }

        MenuItem tour = settingsMenu.getMenu().findItem(R.id.navigation_tour);
        if(tour != null) {
            tour.setChecked(false);
        }

        MenuItem logout = settingsMenu.getMenu().findItem(R.id.navigation_log_out);
        if (logout != null){
            logout.setChecked(false);
        }

        if(DroidPlannerApp.getInstance().hideTelemetry){
            telemSwitch.setChecked(true);
        } else {
            telemSwitch.setChecked(false);
        }
    }

    private void accountLogOut(){

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        sharedPref.edit().clear().apply();
        DroidPlannerApp.getInstance().polygonMap.clear();
        DroidPlannerApp.getInstance().selectedPolygons.clear();
        DroidPlannerPrefs.getInstance(this).prefs.edit().clear().apply();
        dpApp.getMissionProxy().clear();
        Intent intent = new Intent(this, LoginActivity.class);
        this.startActivity(intent);
        finish();
        mMixpanel.track("FPA: UserLogOutSuccess");
    }

    private void confirmUserLogOut(Context context) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle("Account Log Out")
                .setMessage("Would you like to log out?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        accountLogOut();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateNavigationDrawer();
                        // do nothing
                    }
                })
                .setIcon(R.drawable.ic_aero_a)
                .show();
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
        return co.aerobotics.android.R.id.action_drawer_content;
    }

    /**
     * Called when the action drawer is opened.
     * Should be override by children as needed.
     */
    @Override
    public void onDrawerOpened() {
        //here
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

    public void setTelemSwitch(Boolean setChecked){
        if(setChecked){
            telemSwitch.setChecked(true);
        } else telemSwitch.setChecked(false);

    }

    protected abstract int getNavigationDrawerMenuItemId();
}
