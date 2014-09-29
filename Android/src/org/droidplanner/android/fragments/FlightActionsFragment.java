package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.helpers.SuperUI;
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

	public interface OnMissionControlInteraction {
		public void onJoystickSelected();

		public void onPlanningSelected();
	}

	private Drone drone;
	private OnMissionControlInteraction listener;
	private Follow followMe;
	private Button missionBtn;
	private Button joystickBtn;
	private Button connectBtn;
	private Button homeBtn;
	private Button armBtn;
	private Button landBtn;
	private Button takeoffBtn;
	private Button loiterBtn;
	private Button followBtn;
	private Button autoBtn;
	private Button disarmBtn;
	private Button dronieBtn;

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

		missionBtn = (Button) view.findViewById(R.id.mc_planningBtn);
		missionBtn.setOnClickListener(this);

		joystickBtn = (Button) view.findViewById(R.id.mc_joystickBtn);
		joystickBtn.setOnClickListener(this);

		connectBtn = (Button) view.findViewById(R.id.mc_connectBtn);
		connectBtn.setOnClickListener(this);

		homeBtn = (Button) view.findViewById(R.id.mc_homeBtn);
		homeBtn.setOnClickListener(this);

		armBtn = (Button) view.findViewById(R.id.mc_armBtn);
		armBtn.setOnClickListener(this);
		
		disarmBtn = (Button) view.findViewById(R.id.mc_disarmBtn);
		disarmBtn.setOnClickListener(this);

		landBtn = (Button) view.findViewById(R.id.mc_land);
		landBtn.setOnClickListener(this);

		takeoffBtn = (Button) view.findViewById(R.id.mc_takeoff);
		takeoffBtn.setOnClickListener(this);

		loiterBtn = (Button) view.findViewById(R.id.mc_loiter);
		loiterBtn.setOnClickListener(this);

		autoBtn = (Button) view.findViewById(R.id.mc_autoBtn);
		autoBtn.setOnClickListener(this);

		followBtn = (Button) view.findViewById(R.id.mc_follow);
		followBtn.setOnClickListener(this);
		
		dronieBtn = (Button) view.findViewById(R.id.mc_dronieBtn);
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
		case R.id.mc_planningBtn:
			listener.onPlanningSelected();
			eventBuilder.setAction("Planning selected").setLabel(
					getString(R.string.mission_control_edit));
			break;

		case R.id.mc_connectBtn:
			((SuperUI) getActivity()).toggleDroneConnection();
			break;

		case R.id.mc_joystickBtn:
			listener.onJoystickSelected();
			eventBuilder.setAction("Joystick selected").setLabel(
					getString(R.string.mission_control_control));
			break;

		case R.id.mc_armBtn:
			MavLinkArm.sendArmMessage(drone, true);
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
			drone.getState().doTakeoff(new Altitude(10.0));
			eventBuilder.setAction("Changed flight mode").setLabel("Takeoff");
			break;

		case R.id.mc_homeBtn:
			drone.getState().changeFlightMode(ApmModes.ROTOR_RTL);
			eventBuilder.setAction("Changed flight mode").setLabel(ApmModes.ROTOR_RTL.getName());
			break;

		case R.id.mc_loiter:
			drone.getState().changeFlightMode(ApmModes.ROTOR_LOITER);
			eventBuilder.setAction("Changed flight mode").setLabel(ApmModes.ROTOR_LOITER.getName());
			break;

		case R.id.mc_autoBtn:
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

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case ARMING:
		case CONNECTED:
		case DISCONNECTED:
		case STATE:
			setupButtonsByFlightState();
			break;
		default:
			break;
		}
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
		missionBtn.setVisibility(View.VISIBLE);
		joystickBtn.setVisibility(View.GONE);
		connectBtn.setVisibility(View.VISIBLE);
		homeBtn.setVisibility(View.GONE);
		armBtn.setVisibility(View.GONE);
		disarmBtn.setVisibility(View.GONE);
		landBtn.setVisibility(View.GONE);
		takeoffBtn.setVisibility(View.GONE);
		loiterBtn.setVisibility(View.GONE);
		autoBtn.setVisibility(View.GONE);
		followBtn.setVisibility(View.GONE);
		dronieBtn.setVisibility(View.GONE);
	}

	private void setupButtonsForDisarmed() {
		missionBtn.setVisibility(View.VISIBLE);
		joystickBtn.setVisibility(View.GONE);
		connectBtn.setVisibility(View.GONE);
		homeBtn.setVisibility(View.GONE);
		armBtn.setVisibility(View.VISIBLE);
		disarmBtn.setVisibility(View.GONE);
		landBtn.setVisibility(View.GONE);
		takeoffBtn.setVisibility(View.GONE);
		loiterBtn.setVisibility(View.GONE);
		autoBtn.setVisibility(View.GONE);
		followBtn.setVisibility(View.GONE);
		dronieBtn.setVisibility(View.VISIBLE);
	}

	private void setupButtonsForArmed() {
		missionBtn.setVisibility(View.VISIBLE);
		joystickBtn.setVisibility(View.GONE);
		connectBtn.setVisibility(View.GONE);
		homeBtn.setVisibility(View.GONE);
		armBtn.setVisibility(View.GONE);
		disarmBtn.setVisibility(View.VISIBLE);
		landBtn.setVisibility(View.GONE);
		takeoffBtn.setVisibility(View.VISIBLE);
		loiterBtn.setVisibility(View.GONE);
		autoBtn.setVisibility(View.GONE);
		followBtn.setVisibility(View.GONE);
		dronieBtn.setVisibility(View.GONE);
	}

	private void setupButtonsForFlying() {
		missionBtn.setVisibility(View.VISIBLE);
		joystickBtn.setVisibility(View.GONE);
		connectBtn.setVisibility(View.GONE);
		homeBtn.setVisibility(View.VISIBLE);
		armBtn.setVisibility(View.GONE);
		disarmBtn.setVisibility(View.GONE);
		landBtn.setVisibility(View.VISIBLE);
		takeoffBtn.setVisibility(View.GONE);
		loiterBtn.setVisibility(View.VISIBLE);
		autoBtn.setVisibility(View.VISIBLE);
		followBtn.setVisibility(View.VISIBLE);
		dronieBtn.setVisibility(View.GONE);
	}

}
