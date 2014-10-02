package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.model.Drone;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.analytics.HitBuilders;

public class FlightActionsFragment extends Fragment implements OnClickListener, OnDroneListener {

	private static final double TAKEOFF_ALTITUDE = 10.0;

	public interface OnMissionControlInteraction {
		public void onJoystickSelected();
	}

	private Drone drone;
	private OnMissionControlInteraction listener;

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
		View view = inflater.inflate(R.layout.fragment_mission_control, container, false);

		DroidPlannerApp droidPlannerApp = (DroidPlannerApp) getActivity().getApplication();
		drone = droidPlannerApp.getDrone();
		followMe = droidPlannerApp.followMe;
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mDisconnectedButtons = view.findViewById(R.id.mc_disconnected_buttons);
		mDisarmedButtons = view.findViewById(R.id.mc_disarmed_buttons);
		mArmedButtons = view.findViewById(R.id.mc_armed_buttons);
		mInFlightButtons = view.findViewById(R.id.mc_in_flight_buttons);

		final Button joystickBtn = (Button) view.findViewById(R.id.mc_joystickBtn);
		joystickBtn.setOnClickListener(this);

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

		drone.addDroneListener(this);
		setupButtonsByFlightState();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = (OnMissionControlInteraction) activity;
	}

	@Override
	public void onClick(View v) {
		HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
				.setCategory(GAUtils.Category.FLIGHT_DATA_ACTION_BUTTON);

		switch (v.getId()) {
		case R.id.mc_connectBtn:
			((SuperUI) getActivity()).toggleDroneConnection();
			break;

		case R.id.mc_joystickBtn:
			listener.onJoystickSelected();
			eventBuilder.setAction("Joystick selected").setLabel(
					getString(R.string.mission_control_control));
			break;

		case R.id.mc_armBtn:
			getArmingConfirmation();
			eventBuilder.setAction("Changed flight mode").setLabel("Arm");
			break;

		case R.id.mc_disarmBtn:
			MavLinkArm.sendArmMessage(drone, false);
			eventBuilder.setAction("Changed flight mode").setLabel("Disarm");
			break;

		case R.id.mc_land:
			drone.getState().changeFlightMode(ApmModes.ROTOR_LAND);
			eventBuilder.setAction("Changed flight mode").setLabel(ApmModes.ROTOR_LAND.getName());
			break;

		case R.id.mc_takeoff:
			drone.getState().doTakeoff(new Altitude(TAKEOFF_ALTITUDE));
			eventBuilder.setAction("Changed flight mode").setLabel("Takeoff");
			break;

		case R.id.mc_homeBtn:
			drone.getState().changeFlightMode(ApmModes.ROTOR_RTL);
			eventBuilder.setAction("Changed flight mode").setLabel(ApmModes.ROTOR_RTL.getName());
			break;

		case R.id.mc_pause:
			drone.getGuidedPoint().pauseAtCurrentLocation();
			eventBuilder.setAction("Changed flight mode").setLabel("Pause");
			break;

		case R.id.mc_autoBtn:
			drone.getState().changeFlightMode(ApmModes.ROTOR_AUTO);
			eventBuilder.setAction("Changed flight mode").setLabel(ApmModes.ROTOR_AUTO.getName());
			break;
			
		case R.id.mc_TakeoffInAutoBtn:
			drone.getState().doTakeoff(new Altitude(TAKEOFF_ALTITUDE));
			drone.getState().changeFlightMode(ApmModes.ROTOR_AUTO);
			eventBuilder.setAction("Changed flight mode").setLabel(ApmModes.ROTOR_AUTO.getName());
			break;

		case R.id.mc_follow:
			final int result = followMe.toggleFollowMeState();
			String eventLabel = null;
			switch (result) {
			case Follow.FOLLOW_START:
				eventLabel = "FollowMe enabled";
				break;

			case Follow.FOLLOW_END:
				eventLabel = "FollowMe disabled";
				break;

			case Follow.FOLLOW_INVALID_STATE:
				eventLabel = "FollowMe error: invalid state";
				break;

			case Follow.FOLLOW_DRONE_DISCONNECTED:
				eventLabel = "FollowMe error: drone not connected";
				break;

			case Follow.FOLLOW_DRONE_NOT_ARMED:
				eventLabel = "FollowMe error: drone not armed";
				break;
			}

			if (eventLabel != null) {
				eventBuilder.setAction("FollowMe selected").setLabel(eventLabel);
				Toast.makeText(getActivity(), eventLabel, Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.mc_dronieBtn:
			drone.getMission().makeAndUploadDronie();
			eventBuilder.setAction("Dronie").setLabel("Dronie");
			break;
		default:
			eventBuilder = null;
			break;
		}

		if (eventBuilder != null) {
			GAUtils.sendEvent(eventBuilder);
		}

	}

	private void getArmingConfirmation() {
		YesNoDialog ynd = YesNoDialog.newInstance(getString(R.string.dialog_confirm_arming_title),
				getString(R.string.dialog_confirm_arming_msg), new YesNoDialog.Listener() {
					@Override
					public void onYes() {
						MavLinkArm.sendArmMessage(drone, true);
					}

					@Override
					public void onNo() {
					}
				});

		ynd.show(getChildFragmentManager(), "Confirm arming");
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case ARMING:
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
			updateFollowButton();
			break;

		default:
			break;
		}
	}

	private void updateFlightModeButtons() {
		resetFlightModeButtons();

		final ApmModes flightMode = drone.getState().getMode();
		switch (flightMode) {
		case ROTOR_AUTO:
			autoBtn.setActivated(true);
			break;

		case ROTOR_GUIDED:
			if (drone.getGuidedPoint().isIdle()) {
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
		followBtn.setActivated(followMe.isEnabled());
	}

	private void resetButtonsContainerVisibility() {
		mDisconnectedButtons.setVisibility(View.GONE);
		mDisarmedButtons.setVisibility(View.GONE);
		mArmedButtons.setVisibility(View.GONE);
		mInFlightButtons.setVisibility(View.GONE);
	}

	private void setupButtonsByFlightState() {
		if (drone.getMavClient().isConnected()) {
			if (drone.getState().isArmed()) {
				if (drone.getState().isFlying()) {
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

}
