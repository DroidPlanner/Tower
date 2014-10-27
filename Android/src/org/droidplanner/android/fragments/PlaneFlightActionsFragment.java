package org.droidplanner.android.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.analytics.HitBuilders;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.fragments.helpers.ApiSubscriberFragment;
import org.droidplanner.android.helpers.ApiInterface;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.State;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.model.Drone;

/**
 * Provides functionality for flight action buttons specific to planes.
 */
public class PlaneFlightActionsFragment extends ApiSubscriberFragment implements View
        .OnClickListener, DroneInterfaces.OnDroneListener, FlightActionsFragment.SlidingUpHeader {

	private static final String ACTION_FLIGHT_ACTION_BUTTON = "Copter flight action button";

	private Drone drone;
	private Follow followMe;

	private View mDisconnectedButtons;
	private View mConnectedButtons;

	private Button followBtn;
	private Button homeBtn;
	private Button pauseBtn;
	private Button autoBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_plane_mission_control, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mDisconnectedButtons = view.findViewById(R.id.mc_disconnected_buttons);
		mConnectedButtons = view.findViewById(R.id.mc_connected_buttons);

		final Button connectBtn = (Button) view.findViewById(R.id.mc_connectBtn);
		connectBtn.setOnClickListener(this);

		homeBtn = (Button) view.findViewById(R.id.mc_homeBtn);
		homeBtn.setOnClickListener(this);

		pauseBtn = (Button) view.findViewById(R.id.mc_pause);
		pauseBtn.setOnClickListener(this);

		autoBtn = (Button) view.findViewById(R.id.mc_autoBtn);
		autoBtn.setOnClickListener(this);

		followBtn = (Button) view.findViewById(R.id.mc_follow);
		followBtn.setOnClickListener(this);
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

	private void updateFlightModeButtons() {
		resetFlightModeButtons();

		final ApmModes flightMode = drone.getState().getMode();
		switch (flightMode) {
		case FIXED_WING_AUTO:
			autoBtn.setActivated(true);
			break;

		case FIXED_WING_GUIDED:
			if (drone.getGuidedPoint().isIdle() && !followMe.isEnabled()) {
				pauseBtn.setActivated(true);
			}
			break;

		case FIXED_WING_RTL:
			homeBtn.setActivated(true);
			break;
		}
	}

	private void resetFlightModeButtons() {
		homeBtn.setActivated(false);
		pauseBtn.setActivated(false);
		autoBtn.setActivated(false);
	}

	private void setupButtonsByFlightState() {
		if (drone.getMavClient().isConnected()) {
			mDisconnectedButtons.setVisibility(View.GONE);
			mConnectedButtons.setVisibility(View.VISIBLE);
		} else {
			mConnectedButtons.setVisibility(View.GONE);
			mDisconnectedButtons.setVisibility(View.VISIBLE);
		}
	}

    @Override
    protected void onApiConnectedImpl(DroidPlannerApi dpApi) {
        drone = dpApi.getDrone();
        followMe = dpApi.getFollowMe();

        setupButtonsByFlightState();
        updateFlightModeButtons();
        updateFollowButton();
        drone.addDroneListener(this);
    }

    @Override
    protected void onApiDisconnectedImpl() {
        drone.removeDroneListener(this);
    }

    @Override
	public void onClick(View v) {
        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(GAUtils.Category.FLIGHT);

        switch(v.getId()){
            case R.id.mc_connectBtn:
                ((SuperUI) getActivity()).toggleDroneConnection();
                break;

            case R.id.mc_homeBtn:
                drone.getState().changeFlightMode(ApmModes.FIXED_WING_RTL);
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(ApmModes.FIXED_WING_RTL.getName());
                break;

            case R.id.mc_pause:
                if (followMe.isEnabled()) {
                    followMe.toggleFollowMeState();
                }

                drone.getGuidedPoint().pauseAtCurrentLocation();
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel("Pause");
                break;

            case R.id.mc_autoBtn:
                drone.getState().changeFlightMode(ApmModes.FIXED_WING_AUTO);
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(ApmModes.FIXED_WING_AUTO.getName());
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

            default:
                eventBuilder = null;
                break;
        }

        if (eventBuilder != null) {
            GAUtils.sendEvent(eventBuilder);
        }
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		switch (event) {
		case CONNECTED:
		case DISCONNECTED:
		case STATE:
			setupButtonsByFlightState();
			break;

		case MODE:
			updateFlightModeButtons();
			break;

		case FOLLOW_START:
		case FOLLOW_STOP:
		case FOLLOW_UPDATE:
			updateFlightModeButtons();
			updateFollowButton();
			break;
		}
	}

    @Override
    public boolean isSlidingUpPanelEnabled(DroidPlannerApi api) {
        final State droneState = api.getState();
        return api.isConnected() && droneState.isArmed();
    }
}
