package org.droidplanner.android.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.error.ErrorType;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.DroneMap;
import org.droidplanner.android.fragments.control.FlightControlManagerFragment;
import org.droidplanner.android.fragments.FlightMapFragment;
import org.droidplanner.android.fragments.TelemetryFragment;
import org.droidplanner.android.fragments.mode.FlightModePanel;
import org.droidplanner.android.utils.prefs.AutoPanMode;

import java.util.concurrent.atomic.AtomicBoolean;

public class FlightActivity extends DrawerNavigationUI {

    private static final String TAG = FlightActivity.class.getSimpleName();
    private static final int GOOGLE_PLAY_SERVICES_REQUEST_CODE = 101;

    private static final String EXTRA_IS_ACTION_DRAWER_OPENED = "extra_is_action_drawer_opened";
    private static final boolean DEFAULT_IS_ACTION_DRAWER_OPENED = true;

    /**
     * Determines how long the failsafe view is visible for.
     */
    private static final long WARNING_VIEW_DISPLAY_TIMEOUT = 10000l; //ms

    private static final IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.AUTOPILOT_ERROR);
        eventFilter.addAction(AttributeEvent.STATE_ARMING);
        eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_UPDATED);
        eventFilter.addAction(AttributeEvent.FOLLOW_START);
        eventFilter.addAction(AttributeEvent.MISSION_DRONIE_CREATED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.AUTOPILOT_ERROR:
                    String errorName = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID);
                    final ErrorType errorType = ErrorType.getErrorById(errorName);
                    onAutopilotError(errorType);
                    break;

                case AttributeEvent.STATE_ARMING:
                case AttributeEvent.STATE_CONNECTED:
                case AttributeEvent.STATE_DISCONNECTED:
                case AttributeEvent.STATE_UPDATED:
                    enableSlidingUpPanel(dpApp.getDrone());
                    break;

                case AttributeEvent.FOLLOW_START:
                    //Extend the sliding drawer if collapsed.
                    if (!mSlidingPanelCollapsing.get() && mSlidingPanel.isSlidingEnabled() &&
                            !mSlidingPanel.isPanelExpanded()) {
                        mSlidingPanel.expandPanel();
                    }
                    break;

                case AttributeEvent.MISSION_DRONIE_CREATED:
                    float dronieBearing = intent.getFloatExtra(AttributeEventExtra.EXTRA_MISSION_DRONIE_BEARING, -1);
                    if (dronieBearing != -1)
                        updateMapBearing(dronieBearing);
                    break;
            }
        }
    };

    private final AtomicBoolean mSlidingPanelCollapsing = new AtomicBoolean(false);

    private final SlidingUpPanelLayout.PanelSlideListener mDisablePanelSliding = new
            SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View view, float v) {
                }

                @Override
                public void onPanelCollapsed(View view) {
                    mSlidingPanel.setSlidingEnabled(false);
                    mSlidingPanel.setPanelHeight(mFlightActionsView.getHeight());
                    mSlidingPanelCollapsing.set(false);

                    //Remove the panel slide listener
                    mSlidingPanel.setPanelSlideListener(null);
                }

                @Override
                public void onPanelExpanded(View view) {
                }

                @Override
                public void onPanelAnchored(View view) {
                }

                @Override
                public void onPanelHidden(View view) {
                }
            };

    private final Runnable hideWarningView = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(this);

            if (warningView != null && warningView.getVisibility() != View.GONE)
                warningView.setVisibility(View.GONE);
        }
    };

    private final Handler handler = new Handler();

    private FragmentManager fragmentManager;

    private TextView warningView;

    private FlightMapFragment mapFragment;
    private FlightControlManagerFragment flightActions;
    private TelemetryFragment telemetryFragment;

    private SlidingUpPanelLayout mSlidingPanel;
    private View mFlightActionsView;

    private View mLocationButtonsContainer;
    private ImageButton mGoToMyLocation;
    private ImageButton mGoToDroneLocation;
    private ImageButton actionDrawerToggle;

    @Override
    public void onDrawerClosed() {
        super.onDrawerClosed();

        if(actionDrawerToggle != null)
            actionDrawerToggle.setActivated(false);

        if (telemetryFragment == null)
            return;
        final View telemetryView = telemetryFragment.getView();
        if (telemetryView != null) {
            final int slidingDrawerWidth = telemetryView.getWidth();
            final boolean isSlidingDrawerOpened = isActionDrawerOpened();
            updateLocationButtonsMargin(isSlidingDrawerOpened, slidingDrawerWidth);
        }
    }

    @Override
    public void onDrawerOpened() {
        super.onDrawerOpened();

        if(actionDrawerToggle != null)
            actionDrawerToggle.setActivated(true);

        if (telemetryFragment == null)
            return;

        final View telemetryView = telemetryFragment.getView();
        if (telemetryView != null) {
            final int slidingDrawerWidth = telemetryView.getWidth();
            final boolean isSlidingDrawerOpened = isActionDrawerOpened();
            updateLocationButtonsMargin(isSlidingDrawerOpened, slidingDrawerWidth);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);

        fragmentManager = getSupportFragmentManager();

        mSlidingPanel = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer);
        warningView = (TextView) findViewById(R.id.failsafeTextView);

        setupMapFragment();

        mLocationButtonsContainer = findViewById(R.id.location_button_container);
        mGoToMyLocation = (ImageButton) findViewById(R.id.my_location_button);
        mGoToDroneLocation = (ImageButton) findViewById(R.id.drone_location_button);
        actionDrawerToggle = (ImageButton) findViewById(R.id.toggle_action_drawer);
        actionDrawerToggle.setVisibility(View.VISIBLE);

        actionDrawerToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isActionDrawerOpened())
                    closeActionDrawer();
                else
                    openActionDrawer();
            }
        });

        mGoToMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED);
                }
            }
        });
        mGoToMyLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.USER);
                    return true;
                }
                return false;
            }
        });

        mGoToDroneLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED);
                }
            }
        });
        mGoToDroneLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DRONE);
                    return true;
                }
                return false;
            }
        });

        flightActions = (FlightControlManagerFragment) fragmentManager.findFragmentById(R.id.flightActionsFragment);
        if (flightActions == null) {
            flightActions = new FlightControlManagerFragment();
            fragmentManager.beginTransaction().add(R.id.flightActionsFragment, flightActions).commit();
        }

        mFlightActionsView = findViewById(R.id.flightActionsFragment);
        mFlightActionsView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!mSlidingPanelCollapsing.get()) {
                    mSlidingPanel.setPanelHeight(mFlightActionsView.getHeight());
                }
            }
        });

        // Add the telemetry fragment
        final int actionDrawerId = getActionDrawerId();
        telemetryFragment = (TelemetryFragment) fragmentManager.findFragmentById(actionDrawerId);
        if (telemetryFragment == null) {
            telemetryFragment = new TelemetryFragment();
            fragmentManager.beginTransaction()
                    .add(actionDrawerId, telemetryFragment)
                    .commit();
        }

        // Add the mode info panel fragment
        FlightModePanel flightModePanel = (FlightModePanel) fragmentManager.findFragmentById(R.id
                .sliding_drawer_content);
        if (flightModePanel == null) {
            flightModePanel = new FlightModePanel();
            fragmentManager.beginTransaction()
                    .add(R.id.sliding_drawer_content, flightModePanel)
                    .commit();
        }

        boolean isActionDrawerOpened = DEFAULT_IS_ACTION_DRAWER_OPENED;
        if (savedInstanceState != null) {
            isActionDrawerOpened = savedInstanceState.getBoolean(EXTRA_IS_ACTION_DRAWER_OPENED, isActionDrawerOpened);
        }

        if (isActionDrawerOpened)
            openActionDrawer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_IS_ACTION_DRAWER_OPENED, isActionDrawerOpened());
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_toolbar;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();
        enableSlidingUpPanel(dpApp.getDrone());
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();
        enableSlidingUpPanel(dpApp.getDrone());
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    protected int getNavigationDrawerEntryId() {
        return R.id.navigation_flight_data;
    }

    @Override
    protected boolean enableMissionMenus() {
        return true;
    }

    private void updateMapLocationButtons(AutoPanMode mode) {
        mGoToMyLocation.setActivated(false);
        mGoToDroneLocation.setActivated(false);

        if (mapFragment != null) {
            mapFragment.setAutoPanMode(mode);
        }

        switch (mode) {
            case DRONE:
                mGoToDroneLocation.setActivated(true);
                break;

            case USER:
                mGoToMyLocation.setActivated(true);
                break;
            default:
                break;
        }
    }

    public void updateMapBearing(float bearing) {
        if (mapFragment != null)
            mapFragment.updateMapBearing(bearing);
    }

    /**
     * Ensures that the device has the correct version of the Google Play
     * Services.
     *
     * @return true if the Google Play Services binary is valid
     */
    private boolean isGooglePlayServicesValid(boolean showErrorDialog) {
        // Check for the google play services is available
        final int playStatus = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getApplicationContext());
        final boolean isValid = playStatus == ConnectionResult.SUCCESS;

        if (!isValid && showErrorDialog) {
            final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(playStatus, this,
                    GOOGLE_PLAY_SERVICES_REQUEST_CODE, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    });

            if (errorDialog != null)
                errorDialog.show();
        }

        return isValid;
    }

    /**
     * Used to setup the flight screen map fragment. Before attempting to
     * initialize the map fragment, this checks if the Google Play Services
     * binary is installed and up to date.
     */
    private void setupMapFragment() {
        if (mapFragment == null && isGooglePlayServicesValid(true)) {
            mapFragment = (FlightMapFragment) fragmentManager.findFragmentById(R.id.flight_map_fragment);
            if (mapFragment == null) {
                mapFragment = new FlightMapFragment();
                fragmentManager.beginTransaction().add(R.id.flight_map_fragment, mapFragment).commit();
            }
        }
    }

    public void setGuidedClickListener(FlightMapFragment.OnGuidedClickListener listener){
        mapFragment.setGuidedClickListener(listener);
    }

    public void addMapMarkerProvider(DroneMap.MapMarkerProvider provider){
        mapFragment.addMapMarkerProvider(provider);
    }

    public void removeMapMarkerProvider(DroneMap.MapMarkerProvider provider){
        mapFragment.removeMapMarkerProvider(provider);
    }

    @Override
    public void onStart() {
        super.onStart();
        setupMapFragment();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateMapLocationButtons(mAppPrefs.getAutoPanMode());

        final View telemetryView = telemetryFragment.getView();
        if (telemetryView != null) {
            final int slidingDrawerWidth = telemetryView.getWidth();
            updateLocationButtonsMargin(isActionDrawerOpened(), slidingDrawerWidth);
        }
    }

    /**
     * Account for the various ui elements and update the map padding so that it
     * remains 'visible'.
     */
    private void updateLocationButtonsMargin(boolean isOpened, int drawerWidth) {

        // Update the right margin for the my location button
        final ViewGroup.MarginLayoutParams marginLp = (ViewGroup.MarginLayoutParams) mLocationButtonsContainer
                .getLayoutParams();
        final int rightMargin = isOpened ? marginLp.leftMargin + drawerWidth : marginLp.leftMargin;
        marginLp.setMargins(marginLp.leftMargin, marginLp.topMargin, rightMargin, marginLp.bottomMargin);
        mLocationButtonsContainer.requestLayout();
    }

    private void enableSlidingUpPanel(Drone api) {
        if (mSlidingPanel == null || api == null) {
            return;
        }

        final boolean isEnabled = flightActions != null && flightActions.isSlidingUpPanelEnabled
                (api);

        if (isEnabled) {
            mSlidingPanel.setSlidingEnabled(true);
        } else {
            if (!mSlidingPanelCollapsing.get()) {
                if (mSlidingPanel.isPanelExpanded()) {
                    mSlidingPanel.setPanelSlideListener(mDisablePanelSliding);
                    mSlidingPanel.collapsePanel();
                    mSlidingPanelCollapsing.set(true);
                } else {
                    mSlidingPanel.setSlidingEnabled(false);
                    mSlidingPanelCollapsing.set(false);
                }
            }
        }
    }

    private void onAutopilotError(ErrorType errorType) {
        if(errorType == null)
            return;

        final CharSequence errorLabel;
        switch(errorType){
            case NO_ERROR:
                errorLabel = null;
                break;

            default:
                errorLabel = errorType.getLabel(getApplicationContext());
                break;
        }

        if(!TextUtils.isEmpty(errorLabel)) {
            handler.removeCallbacks(hideWarningView);

            warningView.setText(errorLabel);
            warningView.setVisibility(View.VISIBLE);
            handler.postDelayed(hideWarningView, WARNING_VIEW_DISPLAY_TIMEOUT);
        }
    }
}
