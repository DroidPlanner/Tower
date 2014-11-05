package org.droidplanner.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.analytics.HitBuilders;
import com.ox3dr.services.android.lib.drone.event.Event;
import com.ox3dr.services.android.lib.drone.property.Altitude;
import com.ox3dr.services.android.lib.drone.property.State;

import org.droidplanner.R;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.api.DroneApi;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.YesNoWithPrefsDialog;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.analytics.GAUtils;

/**
 * Provide functionality for flight action button specific to copters.
 */
public class CopterFlightActionsFragment extends ApiListenerFragment implements View.OnClickListener, FlightActionsFragment.SlidingUpHeader {

    private static final String ACTION_FLIGHT_ACTION_BUTTON = "Copter flight action button";
    private static final double TAKEOFF_ALTITUDE = 10.0;

    private static final IntentFilter eventFilter = new IntentFilter();
    static {
        eventFilter.addAction(Event.EVENT_ARMING);
        eventFilter.addAction(Event.EVENT_CONNECTED);
        eventFilter.addAction(Event.EVENT_DISCONNECTED);
        eventFilter.addAction(Event.EVENT_STATE);
        eventFilter.addAction(Event.EVENT_VEHICLE_MODE);
        eventFilter.addAction(Event.EVENT_FOLLOW_START);
        eventFilter.addAction(Event.EVENT_FOLLOW_STOP);
        eventFilter.addAction(Event.EVENT_FOLLOW_UPDATE);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(Event.EVENT_ARMING.equals(action)
                    || Event.EVENT_CONNECTED.equals(action)
                    || Event.EVENT_DISCONNECTED.equals(action)
                    || Event.EVENT_STATE.equals(action)){
                setupButtonsByFlightState();
            }
            else if(Event.EVENT_VEHICLE_MODE.equals(action)){
                updateFlightModeButtons();
            }
            else if(Event.EVENT_FOLLOW_START.equals(action)
                    || Event.EVENT_FOLLOW_STOP.equals(action)
                    || Event.EVENT_FOLLOW_UPDATE.equals(action)){
                updateFlightModeButtons();
                updateFollowButton();
            }
        }
    };

    private MissionProxy missionProxy;
    private Follow followMe;

    private View mDisconnectedButtons;
    private View mDisarmedButtons;
    private View mArmedButtons;
    private View mInFlightButtons;

    private Button followBtn;
    private Button homeBtn;
    private Button landBtn;
    private Button pauseBtn;
    private Button autoBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_copter_mission_control, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDisconnectedButtons = view.findViewById(R.id.mc_disconnected_buttons);
        mDisarmedButtons = view.findViewById(R.id.mc_disarmed_buttons);
        mArmedButtons = view.findViewById(R.id.mc_armed_buttons);
        mInFlightButtons = view.findViewById(R.id.mc_in_flight_buttons);

        final Button connectBtn = (Button) view.findViewById(R.id.mc_connectBtn);
        connectBtn.setOnClickListener(this);

        homeBtn = (Button) view.findViewById(R.id.mc_homeBtn);
        homeBtn.setOnClickListener(this);

        final Button armBtn = (Button) view.findViewById(R.id.mc_armBtn);
        armBtn.setOnClickListener(this);

        final Button disarmBtn = (Button) view.findViewById(R.id.mc_disarmBtn);
        disarmBtn.setOnClickListener(this);

        landBtn = (Button) view.findViewById(R.id.mc_land);
        landBtn.setOnClickListener(this);

        final Button takeoffBtn = (Button) view.findViewById(R.id.mc_takeoff);
        takeoffBtn.setOnClickListener(this);

        pauseBtn = (Button) view.findViewById(R.id.mc_pause);
        pauseBtn.setOnClickListener(this);

        autoBtn = (Button) view.findViewById(R.id.mc_autoBtn);
        autoBtn.setOnClickListener(this);

        final Button takeoffInAuto = (Button) view.findViewById(R.id.mc_TakeoffInAutoBtn);
        takeoffInAuto.setOnClickListener(this);

        followBtn = (Button) view.findViewById(R.id.mc_follow);
        followBtn.setOnClickListener(this);

        final Button dronieBtn = (Button) view.findViewById(R.id.mc_dronieBtn);
        dronieBtn.setOnClickListener(this);
    }

    @Override
    public void onApiConnected(DroneApi api) {
        missionProxy = api.getMissionProxy();

        setupButtonsByFlightState();
        updateFlightModeButtons();
        updateFollowButton();

        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public void onClick(View v) {
        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(GAUtils.Category.FLIGHT);

        switch (v.getId()) {
            case R.id.mc_connectBtn:
                ((SuperUI) getActivity()).toggleDroneConnection();
                break;

            case R.id.mc_armBtn:
                getArmingConfirmation();
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel("Arm");
                break;

            case R.id.mc_disarmBtn:
                getDroneApi().arm(false);
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel("Disarm");
                break;

            case R.id.mc_land:
                getDroneApi().changeVehicleMode(ApmModes.ROTOR_LAND);
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(ApmModes.ROTOR_LAND.getName());
                break;

            case R.id.mc_takeoff:
                getDroneApi().getState().doTakeoff(new Altitude(TAKEOFF_ALTITUDE));
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel("Takeoff");
                break;

            case R.id.mc_homeBtn:
                getDroneApi().changeVehicleMode(ApmModes.ROTOR_RTL);
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(ApmModes.ROTOR_RTL.getName());
                break;

            case R.id.mc_pause:
                if (followMe.isEnabled()) {
                    followMe.toggleFollowMeState();
                }

                getDroneApi().getGuidedPoint().pauseAtCurrentLocation();
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel("Pause");
                break;

            case R.id.mc_autoBtn:
                getDroneApi().changeVehicleMode(ApmModes.ROTOR_AUTO);
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(ApmModes.ROTOR_AUTO.getName());
                break;

            case R.id.mc_TakeoffInAutoBtn:
                getTakeOffInAutoConfirmation();
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(ApmModes.ROTOR_AUTO.getName());
                break;

            case R.id.mc_follow:
                followMe.toggleFollowMeState();
                String eventLabel = null;

                switch (followMe.getState()) {
                    case FOLLOW_START:
                        eventLabel = "FollowMe enabled";
                        break;

                    case FOLLOW_RUNNING:
                        eventLabel = "FollowMe running";
                        break;

                    case FOLLOW_END:
                        eventLabel = "FollowMe disabled";
                        break;

                    case FOLLOW_INVALID_STATE:
                        eventLabel = "FollowMe error: invalid state";
                        break;

                    case FOLLOW_DRONE_DISCONNECTED:
                        eventLabel = "FollowMe error: drone not connected";
                        break;

                    case FOLLOW_DRONE_NOT_ARMED:
                        eventLabel = "FollowMe error: drone not armed";
                        break;
                }

                if (eventLabel != null) {
                    eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(eventLabel);
                    Toast.makeText(getActivity(), eventLabel, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.mc_dronieBtn:
                getDronieConfirmation();
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel("Dronie uploaded");
                break;

            default:
                eventBuilder = null;
                break;
        }

        if (eventBuilder != null) {
            GAUtils.sendEvent(eventBuilder);
        }

    }

    private void getDronieConfirmation() {
        YesNoWithPrefsDialog ynd = YesNoWithPrefsDialog.newInstance(getActivity()
                .getApplicationContext(), getString(R.string.pref_dronie_creation_title),
                getString(R.string.pref_dronie_creation_message), new YesNoDialog.Listener() {
            @Override
            public void onYes() {
                final float bearing = missionProxy.makeAndUploadDronie();
                if(bearing >= 0){
                    final FlightActivity flightActivity = (FlightActivity) getActivity();
                    if(flightActivity != null){
                        flightActivity.updateMapBearing(bearing);
                    }
                }
            }

            @Override
            public void onNo() {
            }
        }, getString(R.string.pref_warn_on_dronie_creation_key));

        if(ynd != null){
            ynd.show(getChildFragmentManager(), "Confirm dronie creation");
        }
    }

    private void getTakeOffInAutoConfirmation() {
        YesNoWithPrefsDialog ynd = YesNoWithPrefsDialog.newInstance(getActivity()
                        .getApplicationContext(), getString(R.string.dialog_confirm_take_off_in_auto_title),
                getString(R.string.dialog_confirm_take_off_in_auto_msg), new YesNoDialog.Listener() {
                    @Override
                    public void onYes() {
                        DroneApi droneApi = getDroneApi();
                        droneApi.doTakeoff(new Altitude(TAKEOFF_ALTITUDE));
                        droneApi.changeVehicleMode(ApmModes.ROTOR_AUTO);
                    }

                    @Override
                    public void onNo() {
                    }
                }, getString(R.string.pref_warn_on_takeoff_in_auto_key));

        if(ynd != null){
            ynd.show(getChildFragmentManager(), "Confirm take off in auto");
        }
    }

    private void getArmingConfirmation() {
        YesNoWithPrefsDialog ynd = YesNoWithPrefsDialog.newInstance(getActivity().getApplicationContext(),
                getString(R.string.dialog_confirm_arming_title),
                getString(R.string.dialog_confirm_arming_msg), new YesNoDialog.Listener() {
                    @Override
                    public void onYes() {
                        getDroneApi().arm(true);
                    }

                    @Override
                    public void onNo() {}
                }, getString(R.string.pref_warn_on_arm_key));

        if(ynd != null) {
            ynd.show(getChildFragmentManager(), "Confirm arming");
        }
    }



    private void updateFlightModeButtons() {
        resetFlightModeButtons();

        final ApmModes flightMode = getDroneApi().getState().getMode();
        switch (flightMode) {
            case ROTOR_AUTO:
                autoBtn.setActivated(true);
                break;

            case ROTOR_GUIDED:
                if (getDroneApi().getGuidedPoint().isInitialized() && !followMe.isEnabled()) {
                    pauseBtn.setActivated(true);
                }
                break;

            case ROTOR_RTL:
                homeBtn.setActivated(true);
                break;

            case ROTOR_LAND:
                landBtn.setActivated(true);
                break;
            default:
                break;
        }
    }

    private void resetFlightModeButtons() {
        homeBtn.setActivated(false);
        landBtn.setActivated(false);
        pauseBtn.setActivated(false);
        autoBtn.setActivated(false);
    }

    private void updateFollowButton() {
        switch (followMe.getState()) {
            case FOLLOW_START:
                followBtn.setBackgroundColor(Color.RED);
                break;
            case FOLLOW_RUNNING:
                followBtn.setActivated(true);
                followBtn.setBackgroundResource(R.drawable.flight_action_row_bg_selector);
                break;
            default:
                followBtn.setActivated(false);
                followBtn.setBackgroundResource(R.drawable.flight_action_row_bg_selector);
                break;
        }
    }

    private void resetButtonsContainerVisibility() {
        mDisconnectedButtons.setVisibility(View.GONE);
        mDisarmedButtons.setVisibility(View.GONE);
        mArmedButtons.setVisibility(View.GONE);
        mInFlightButtons.setVisibility(View.GONE);
    }

    private void setupButtonsByFlightState() {
        if (getDroneApi().isConnected()) {
            if (getDroneApi().getState().isArmed()) {
                if (getDroneApi().getState().isFlying()) {
                    setupButtonsForFlying();
                } else {
                    setupButtonsForArmed();
                }
            } else {
                setupButtonsForDisarmed();
            }
        } else {
            setupButtonsForDisconnected();
        }
    }

    private void setupButtonsForDisconnected() {
        resetButtonsContainerVisibility();
        mDisconnectedButtons.setVisibility(View.VISIBLE);
    }

    private void setupButtonsForDisarmed() {
        resetButtonsContainerVisibility();
        mDisarmedButtons.setVisibility(View.VISIBLE);
    }

    private void setupButtonsForArmed() {
        resetButtonsContainerVisibility();
        mArmedButtons.setVisibility(View.VISIBLE);
    }

    private void setupButtonsForFlying() {
        resetButtonsContainerVisibility();
        mInFlightButtons.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isSlidingUpPanelEnabled(DroneApi api) {
        final State droneState = api.getState();
        return api.isConnected() && droneState.isArmed() && droneState.isFlying();
    }
}
