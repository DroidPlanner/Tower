package org.droidplanner.android.fragments.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.analytics.HitBuilders;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.utils.analytics.GAUtils;

/**
 * Created by Fredia Huya-Kouadio on 3/4/15.
 */
public class RoverFlightControlFragment extends BaseFlightControlFragment {

    private static final String ACTION_FLIGHT_ACTION_BUTTON = "Rover flight action button";

    private static final IntentFilter intentFilter = new IntentFilter();

    static {
        intentFilter.addAction(AttributeEvent.STATE_ARMING);
        intentFilter.addAction(AttributeEvent.STATE_CONNECTED);
        intentFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        intentFilter.addAction(AttributeEvent.STATE_UPDATED);
        intentFilter.addAction(AttributeEvent.STATE_VEHICLE_MODE);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.STATE_ARMING:
                case AttributeEvent.STATE_CONNECTED:
                case AttributeEvent.STATE_DISCONNECTED:
                case AttributeEvent.STATE_UPDATED:
                    setupButtonsByFlightState();
                    break;

                case AttributeEvent.STATE_VEHICLE_MODE:
                    updateFlightModeButtons();
                    break;

            }
        }
    };

    private View mDisconnectedButtons;
    private View mActiveButtons;

    private Button homeBtn;
    private Button pauseBtn;
    private Button autoBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rover_mission_control, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDisconnectedButtons = view.findViewById(R.id.mc_disconnected_buttons);
        mActiveButtons = view.findViewById(R.id.mc_connected_buttons);

        final View connectBtn = view.findViewById(R.id.mc_connectBtn);
        connectBtn.setOnClickListener(this);

        homeBtn = (Button) view.findViewById(R.id.mc_homeBtn);
        homeBtn.setOnClickListener(this);

        pauseBtn = (Button) view.findViewById(R.id.mc_pause);
        pauseBtn.setOnClickListener(this);

        autoBtn = (Button) view.findViewById(R.id.mc_autoBtn);
        autoBtn.setOnClickListener(this);
    }

    private void updateFlightModeButtons() {
        resetFlightModeButtons();

        final Drone drone = getDrone();
        final State droneState = drone.getAttribute(AttributeType.STATE);
        final VehicleMode flightMode = droneState.getVehicleMode();
        if (flightMode != null) {
            switch (flightMode) {
                case ROVER_AUTO:
                    autoBtn.setActivated(true);
                    break;

                case ROVER_HOLD:
                case ROVER_GUIDED:
                    pauseBtn.setActivated(true);
                    break;

                case ROVER_RTL:
                    homeBtn.setActivated(true);
                    break;
            }
        }
    }

    private void resetFlightModeButtons() {
        homeBtn.setActivated(false);
        pauseBtn.setActivated(false);
        autoBtn.setActivated(false);
    }

    private void setupButtonsByFlightState() {
        final State droneState = getDrone().getAttribute(AttributeType.STATE);
        if (droneState != null && droneState.isConnected()) {
            setupButtonsForConnected();
        } else {
            setupButtonsForDisconnected();
        }
    }

    private void resetButtonsContainerVisibility() {
        mDisconnectedButtons.setVisibility(View.GONE);
        mActiveButtons.setVisibility(View.GONE);
    }

    private void setupButtonsForDisconnected() {
        resetButtonsContainerVisibility();
        mDisconnectedButtons.setVisibility(View.VISIBLE);
    }

    private void setupButtonsForConnected() {
        resetButtonsContainerVisibility();
        mActiveButtons.setVisibility(View.VISIBLE);
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();
        setupButtonsByFlightState();
        updateFlightModeButtons();
        getBroadcastManager().registerReceiver(eventReceiver, intentFilter);
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public boolean isSlidingUpPanelEnabled(Drone drone) {
        final State droneState = drone.getAttribute(AttributeType.STATE);
        return droneState.isConnected();
    }

    @Override
    public void onClick(View v) {
        final Drone drone = getDrone();
        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(GAUtils.Category.FLIGHT);

        switch (v.getId()) {
            case R.id.mc_connectBtn:
                ((SuperUI) getActivity()).toggleDroneConnection();
                break;

            case R.id.mc_homeBtn:
                VehicleApi.getApi(drone).setVehicleMode(VehicleMode.ROVER_RTL);
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(VehicleMode.ROVER_RTL.getLabel());
                break;

            case R.id.mc_pause: {
                VehicleApi.getApi(drone).setVehicleMode(VehicleMode.ROVER_HOLD);
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(VehicleMode.ROVER_HOLD.getLabel());
                break;
            }

            case R.id.mc_autoBtn:
                VehicleApi.getApi(drone).setVehicleMode(VehicleMode.ROVER_AUTO);
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(VehicleMode.ROVER_AUTO.getLabel());
                break;

            default:
                eventBuilder = null;
                break;
        }

        if (eventBuilder != null) {
            GAUtils.sendEvent(eventBuilder);
        }
    }
}
