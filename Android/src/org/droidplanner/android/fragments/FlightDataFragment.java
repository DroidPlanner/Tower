package org.droidplanner.android.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.error.ErrorType;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.DrawerNavigationUI;
import org.droidplanner.android.fragments.control.FlightControlManagerFragment;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.fragments.mode.FlightModePanel;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.view.SlidingDrawer;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Fredia Huya-Kouadio on 8/27/15.
 */
public class FlightDataFragment extends ApiListenerFragment implements SlidingDrawer.OnDrawerOpenListener, SlidingDrawer.OnDrawerCloseListener {

    public static final String EXTRA_SHOW_ACTION_DRAWER_TOGGLE = "extra_show_action_drawer_toggle";
    private static final boolean DEFAULT_SHOW_ACTION_DRAWER_TOGGLE = false;

    private static final int GOOGLE_PLAY_SERVICES_REQUEST_CODE = 101;

    /**
     * Determines how long the failsafe view is visible for.
     */
    private static final long WARNING_VIEW_DISPLAY_TIMEOUT = 10000l; //ms

    private static final IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.AUTOPILOT_ERROR);
        eventFilter.addAction(AttributeEvent.AUTOPILOT_MESSAGE);
        eventFilter.addAction(AttributeEvent.STATE_ARMING);
        eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_UPDATED);
        eventFilter.addAction(AttributeEvent.TYPE_UPDATED);
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

                case AttributeEvent.AUTOPILOT_MESSAGE:
                    final int logLevel = intent.getIntExtra(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE_LEVEL, Log.VERBOSE);
                    final String message = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE);
                    onAutopilotError(logLevel, message);
                    break;

                case AttributeEvent.STATE_ARMING:
                case AttributeEvent.STATE_CONNECTED:
                case AttributeEvent.STATE_DISCONNECTED:
                case AttributeEvent.STATE_UPDATED:
                case AttributeEvent.TYPE_UPDATED:
                    enableSlidingUpPanel(getDrone());
                    break;

                case AttributeEvent.FOLLOW_START:
                    //Extend the sliding drawer if collapsed.
                    if (!mSlidingPanelCollapsing.get()
                            && mSlidingPanel.isEnabled()
                            && mSlidingPanel.getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED) {
                        mSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
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

    private final String disablePanelSlidingLabel = "disablingListener";
    private final SlidingUpPanelLayout.PanelSlideListener mDisablePanelSliding = new
            SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View view, float v) {
                }

                @Override
                public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                    switch(newState){
                        case COLLAPSED:
                            mSlidingPanel.setEnabled(false);
                            mSlidingPanelCollapsing.set(false);

                            //Remove the panel slide listener
                            slidingPanelListenerMgr.removePanelSlideListener(disablePanelSlidingLabel);
                            break;
                    }
                }
            };

    private final Runnable hideWarningViewCb = new Runnable() {
        @Override
        public void run() {
            hideWarningView();
        }
    };

    private final Handler handler = new Handler();

    private View actionbarShadow;

    private View warningContainer;
    private TextView warningText;

    private FlightMapFragment mapFragment;
    private FlightControlManagerFragment flightActions;

    private SlidingUpPanelLayout mSlidingPanel;

    private FloatingActionButton mGoToMyLocation;
    private FloatingActionButton mGoToDroneLocation;
    private FloatingActionButton actionDrawerToggle;

    private DrawerNavigationUI navActivity;

    private static class SlidingPanelListenerManager implements SlidingUpPanelLayout.PanelSlideListener {
        private final HashMap<String, SlidingUpPanelLayout.PanelSlideListener> panelListenerClients = new HashMap<>();

        public void addPanelSlideListener(String label, SlidingUpPanelLayout.PanelSlideListener listener){
            panelListenerClients.put(label, listener);
        }

        public void removePanelSlideListener(String label){
            panelListenerClients.remove(label);
        }

        @Override
        public void onPanelSlide(View view, float v) {
            for(SlidingUpPanelLayout.PanelSlideListener listener: panelListenerClients.values()){
                listener.onPanelSlide(view, v);
            }
        }

        @Override
        public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            for(SlidingUpPanelLayout.PanelSlideListener listener: panelListenerClients.values()){
                listener.onPanelStateChanged(panel, previousState, newState);
            }
        }
    }

    private final String parentActivityPanelListenerLabel = "parentListener";

    private final SlidingPanelListenerManager slidingPanelListenerMgr = new SlidingPanelListenerManager();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DrawerNavigationUI)
            navActivity = (DrawerNavigationUI) activity;

        if(activity instanceof SlidingUpPanelLayout.PanelSlideListener)
            slidingPanelListenerMgr.addPanelSlideListener(parentActivityPanelListenerLabel, (SlidingUpPanelLayout.PanelSlideListener) activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navActivity = null;
        slidingPanelListenerMgr.removePanelSlideListener(parentActivityPanelListenerLabel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flight_data, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Bundle arguments = getArguments();
        final boolean showActionDrawerToggle = arguments == null
                ? DEFAULT_SHOW_ACTION_DRAWER_TOGGLE
                : arguments.getBoolean(EXTRA_SHOW_ACTION_DRAWER_TOGGLE, DEFAULT_SHOW_ACTION_DRAWER_TOGGLE);

        actionbarShadow = view.findViewById(R.id.actionbar_shadow);

        final FragmentManager fm = getChildFragmentManager();

        mSlidingPanel = (SlidingUpPanelLayout) view.findViewById(R.id.slidingPanelContainer);
        mSlidingPanel.addPanelSlideListener(slidingPanelListenerMgr);

        warningText = (TextView) view.findViewById(R.id.failsafeTextView);
        warningContainer = view.findViewById(R.id.warningContainer);
        ImageView closeWarningView = (ImageView) view.findViewById(R.id.close_warning_view);
        closeWarningView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideWarningView();
            }
        });

        setupMapFragment();

        mGoToMyLocation = (FloatingActionButton) view.findViewById(R.id.my_location_button);
        mGoToDroneLocation = (FloatingActionButton) view.findViewById(R.id.drone_location_button);
        actionDrawerToggle = (FloatingActionButton) view.findViewById(R.id.toggle_action_drawer);

        if (showActionDrawerToggle) {
            actionDrawerToggle.setVisibility(View.VISIBLE);

            actionDrawerToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (navActivity == null)
                        return;

                    if (navActivity.isActionDrawerOpened())
                        navActivity.closeActionDrawer();
                    else
                        navActivity.openActionDrawer();
                }
            });
        }

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

        flightActions = (FlightControlManagerFragment) fm.findFragmentById(R.id.flightActionsFragment);
        if (flightActions == null) {
            flightActions = new FlightControlManagerFragment();
            fm.beginTransaction().add(R.id.flightActionsFragment, flightActions).commit();
        }

        // Add the mode info panel fragment
        FlightModePanel flightModePanel = (FlightModePanel) fm.findFragmentById(R.id.sliding_drawer_content);
        if (flightModePanel == null) {
            flightModePanel = new FlightModePanel();
            fm.beginTransaction()
                    .add(R.id.sliding_drawer_content, flightModePanel)
                    .commit();
        }
    }

    private void hideWarningView(){
        handler.removeCallbacks(hideWarningViewCb);

        if (warningContainer != null && warningContainer.getVisibility() != View.GONE)
            warningContainer.setVisibility(View.GONE);
    }

    public void updateActionbarShadow(int shadowHeight){
        if(actionbarShadow == null || actionbarShadow.getLayoutParams().height == shadowHeight)
            return;

        actionbarShadow.getLayoutParams().height = shadowHeight;
        actionbarShadow.requestLayout();
    }

    @Override
    public void onStart() {
        super.onStart();
        setupMapFragment();
        updateMapLocationButtons(getAppPrefs().getAutoPanMode());
    }

    @Override
    public void onApiConnected() {
        enableSlidingUpPanel(getDrone());
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        enableSlidingUpPanel(getDrone());
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public void onDrawerClosed() {
        if (actionDrawerToggle != null)
            actionDrawerToggle.setActivated(false);
    }

    @Override
    public void onDrawerOpened() {
        if (actionDrawerToggle != null)
            actionDrawerToggle.setActivated(true);
    }

    /**
     * Used to setup the flight screen map fragment. Before attempting to
     * initialize the map fragment, this checks if the Google Play Services
     * binary is installed and up to date.
     */
    private void setupMapFragment() {
        final FragmentManager fm = getChildFragmentManager();
        if (mapFragment == null && isGooglePlayServicesValid(true)) {
            mapFragment = (FlightMapFragment) fm.findFragmentById(R.id.flight_map_fragment);
            if (mapFragment == null) {
                mapFragment = new FlightMapFragment();
                fm.beginTransaction().add(R.id.flight_map_fragment, mapFragment).commit();
            }
        }
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

    private void enableSlidingUpPanel(Drone api) {
        if (mSlidingPanel == null || api == null) {
            return;
        }

        final boolean isEnabled = flightActions != null && flightActions.isSlidingUpPanelEnabled(api);

        if (isEnabled) {
            mSlidingPanel.setEnabled(true);
            mSlidingPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    SlidingUpPanelLayout.PanelState panelState = mSlidingPanel.getPanelState();
                    slidingPanelListenerMgr.onPanelStateChanged(mSlidingPanel, panelState, panelState);
                    mSlidingPanel.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });

        } else {
            if (!mSlidingPanelCollapsing.get()) {
                SlidingUpPanelLayout.PanelState panelState = mSlidingPanel.getPanelState();
                if (panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    slidingPanelListenerMgr.addPanelSlideListener(disablePanelSlidingLabel, mDisablePanelSliding);
                    mSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    mSlidingPanelCollapsing.set(true);
                } else {
                    mSlidingPanel.setEnabled(false);
                    mSlidingPanelCollapsing.set(false);
                }
            }
        }
    }

    private void onAutopilotError(ErrorType errorType) {
        if (errorType == null)
            return;

        final CharSequence errorLabel;
        switch (errorType) {
            case NO_ERROR:
                errorLabel = null;
                break;

            default:
                errorLabel = errorType.getLabel(getContext());
                break;
        }

        onAutopilotError(Log.ERROR, errorLabel);
    }

    private void onAutopilotError(int logLevel, CharSequence errorMsg) {
        if (TextUtils.isEmpty(errorMsg))
            return;

        switch (logLevel) {
            case Log.ERROR:
            case Log.WARN:
                handler.removeCallbacks(hideWarningViewCb);

                warningText.setText(errorMsg);
                warningContainer.setVisibility(View.VISIBLE);
                handler.postDelayed(hideWarningViewCb, WARNING_VIEW_DISPLAY_TIMEOUT);
                break;
        }
    }

    /**
     * Ensures that the device has the correct version of the Google Play
     * Services.
     *
     * @return true if the Google Play Services binary is valid
     */
    private boolean isGooglePlayServicesValid(boolean showErrorDialog) {
        // Check for the google play services is available
        final int playStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        final boolean isValid = playStatus == ConnectionResult.SUCCESS;

        if (!isValid && showErrorDialog) {
            final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(playStatus, getActivity(),
                    GOOGLE_PLAY_SERVICES_REQUEST_CODE, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (isAdded()) {
                                getActivity().finish();
                            }
                        }
                    });

            if (errorDialog != null)
                errorDialog.show();
        }

        return isValid;
    }

    public void setGuidedClickListener(FlightMapFragment.OnGuidedClickListener listener) {
        mapFragment.setGuidedClickListener(listener);
    }

    public void addMarker(MarkerInfo markerInfo){
        mapFragment.addMarker(markerInfo);
    }

    public void removeMarker(MarkerInfo markerInfo){
        mapFragment.removeMarker(markerInfo);
    }
}
