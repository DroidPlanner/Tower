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

import com.google.android.gms.analytics.HitBuilders;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.event.Event;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

import org.droidplanner.R;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.utils.analytics.GAUtils;

/**
 * Provides functionality for flight action buttons specific to planes.
 */
public class PlaneFlightActionsFragment extends ApiListenerFragment implements
		View.OnClickListener, FlightActionsFragment.SlidingUpHeader {

	private static final String ACTION_FLIGHT_ACTION_BUTTON = "Copter flight action button";

	private View mDisconnectedButtons;
	private View mConnectedButtons;

	private Button followBtn;
	private Button homeBtn;
	private Button pauseBtn;
	private Button autoBtn;

    private static final IntentFilter eventFilter = new IntentFilter();
    static {
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
            if (Event.EVENT_CONNECTED.equals(action) || Event.EVENT_DISCONNECTED.equals(action)
                    || Event.EVENT_STATE.equals(action)) {
                setupButtonsByFlightState();
            } else if (Event.EVENT_VEHICLE_MODE.equals(action)) {
                updateFlightModeButtons();
            } else if (Event.EVENT_FOLLOW_START.equals(action)
                    || Event.EVENT_FOLLOW_STOP.equals(action)
                    || Event.EVENT_FOLLOW_UPDATE.equals(action)) {
                updateFlightModeButtons();
                updateFollowButton();

                if((Event.EVENT_FOLLOW_START.equals(action)
                        || Event.EVENT_FOLLOW_STOP.equals(action))) {
                    final FollowState followState = getDrone().getFollowState();
                    if (followState != null) {
                        String eventLabel = null;
                        switch (followState.getState()) {
                            case FollowState.STATE_START:
                                eventLabel = "FollowMe enabled";
                                break;

                            case FollowState.STATE_RUNNING:
                                eventLabel = "FollowMe running";
                                break;

                            case FollowState.STATE_END:
                                eventLabel = "FollowMe disabled";
                                break;

                            case FollowState.STATE_INVALID:
                                eventLabel = "FollowMe error: invalid state";
                                break;

                            case FollowState.STATE_DRONE_DISCONNECTED:
                                eventLabel = "FollowMe error: drone not connected";
                                break;

                            case FollowState.STATE_DRONE_NOT_ARMED:
                                eventLabel = "FollowMe error: drone not armed";
                                break;
                        }

                        if (eventLabel != null) {
                            HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                                    .setCategory(GAUtils.Category.FLIGHT)
                                    .setAction(ACTION_FLIGHT_ACTION_BUTTON)
                                    .setLabel(eventLabel);
                            GAUtils.sendEvent(eventBuilder);

                            Toast.makeText(getActivity(), eventLabel, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    };

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
		switch (getDrone().getFollowState().getState()) {
		case FollowState.STATE_START:
			followBtn.setBackgroundColor(Color.RED);
			break;
		case FollowState.STATE_RUNNING:
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

		final Drone drone = getDrone();
		final VehicleMode flightMode = drone.getState().getVehicleMode();
        if(flightMode != null) {
            switch (flightMode) {
                case PLANE_AUTO:
                    autoBtn.setActivated(true);
                    break;

                case PLANE_GUIDED:
                    if (drone.getGuidedState().isInitialized() && !drone.getFollowState().isEnabled()) {
                        pauseBtn.setActivated(true);
                    }
                    break;

                case PLANE_RTL:
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
		if (getDrone().isConnected()) {
			mDisconnectedButtons.setVisibility(View.GONE);
			mConnectedButtons.setVisibility(View.VISIBLE);
		} else {
			mConnectedButtons.setVisibility(View.GONE);
			mDisconnectedButtons.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onApiConnected() {
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
        final Drone drone = getDrone();
		HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
				.setCategory(GAUtils.Category.FLIGHT);

		switch (v.getId()) {
		case R.id.mc_connectBtn:
			((SuperUI) getActivity()).toggleDroneConnection();
			break;

		case R.id.mc_homeBtn:
			drone.changeVehicleMode(VehicleMode.PLANE_RTL);
			eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON)
                    .setLabel(VehicleMode.PLANE_RTL.getLabel());
			break;

		case R.id.mc_pause:
			if (drone.getFollowState().isEnabled()) {
				drone.disableFollowMe();
			}

			drone.pauseAtCurrentLocation();
			eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel("Pause");
			break;

		case R.id.mc_autoBtn:
			drone.changeVehicleMode(VehicleMode.PLANE_AUTO);
			eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON)
                    .setLabel(VehicleMode.PLANE_AUTO.getLabel());
			break;

		case R.id.mc_follow:
            if(drone.getFollowState().isEnabled())
                drone.disableFollowMe();
            else
                drone.enableFollowMe(FollowType.LEASH);
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
	public boolean isSlidingUpPanelEnabled(Drone api) {
		return api.isConnected() && api.getState().isArmed();
	}
}
