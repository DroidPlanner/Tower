package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.gcs.follow.Follow;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.core.drone.Drone;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.ardupilotmega.msg_command_long;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_GOTO;
import com.google.android.gms.analytics.HitBuilders;
import org.droidplanner.core.drone.variables.Type.FirmwareType;

public class FlightActionsFragment extends Fragment implements OnClickListener {

	public interface OnMissionControlInteraction {
		public void onJoystickSelected();

		public void onPlanningSelected();
	}

	private Drone drone;
	private OnMissionControlInteraction listener;
	private Follow followMe;

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

		final Button missionBtn = (Button) view.findViewById(R.id.mc_planningBtn);
		missionBtn.setOnClickListener(this);

		final Button joystickBtn = (Button) view.findViewById(R.id.mc_joystickBtn);
		joystickBtn.setOnClickListener(this);

		final Button homeBtn = (Button) view.findViewById(R.id.mc_homeBtn);
		homeBtn.setOnClickListener(this);

		final Button landBtn = (Button) view.findViewById(R.id.mc_land);
		landBtn.setOnClickListener(this);

		final Button takeoffBtn = (Button) view.findViewById(R.id.mc_takeoff);
		takeoffBtn.setOnClickListener(this);
		if (drone.type.getFirmwareType() == FirmwareType.AR_DRONE) {
			takeoffBtn.setVisibility(View.VISIBLE);
		} else {
			takeoffBtn.setVisibility(View.GONE);
		}

		final Button loiterBtn = (Button) view.findViewById(R.id.mc_loiter);
		loiterBtn.setOnClickListener(this);

		final Button followBtn = (Button) view.findViewById(R.id.mc_follow);
		followBtn.setOnClickListener(this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = (OnMissionControlInteraction) activity;
	}

	@Override
	public void onClick(View v) {
		HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
				.setCategory(GAUtils.Category.FLIGHT_DATA_ACTION_BUTTON.toString());
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;

		switch (v.getId()) {
		case R.id.mc_planningBtn:
			listener.onPlanningSelected();
			eventBuilder.setAction("Planning selected").setLabel(
					getString(R.string.mission_control_edit));
			break;

		case R.id.mc_joystickBtn:
			listener.onJoystickSelected();
			eventBuilder.setAction("Joystick selected").setLabel(
					getString(R.string.mission_control_control));
			break;

		case R.id.mc_land:
			if (drone.type.getFirmwareType() == FirmwareType.AR_DRONE) {
				msg.command = (short) MAV_CMD.MAV_CMD_NAV_LAND;
				drone.MavClient.sendMavPacket(msg.pack());
			} else {
				drone.state.changeFlightMode(ApmModes.ROTOR_LAND);
				eventBuilder.setAction("Changed flight mode").setLabel(ApmModes.ROTOR_LAND.getName());
			}
			break;

		case R.id.mc_takeoff:
			// drone.state.changeFlightMode(ApmModes.ROTOR_TAKEOFF); //TODO
			// there isn`t a takeoff mode on ArduCopter
			// eventBuilder.setAction("Changed flight mode")
			// .setLabel(ApmModes.ROTOR_TAKEOFF.getName());

			if (drone.type.getFirmwareType() == FirmwareType.AR_DRONE) {
				msg.command = (short) MAV_CMD.MAV_CMD_NAV_TAKEOFF;
				drone.MavClient.sendMavPacket(msg.pack());
			}
			break;

		case R.id.mc_homeBtn:
			if (drone.type.getFirmwareType() == FirmwareType.AR_DRONE) {
				msg.command = (short) MAV_CMD.MAV_CMD_NAV_RETURN_TO_LAUNCH;
				drone.MavClient.sendMavPacket(msg.pack());
			} else {
				drone.state.changeFlightMode(ApmModes.ROTOR_RTL);
				eventBuilder.setAction("Changed flight mode").setLabel(ApmModes.ROTOR_RTL.getName());
			}
			break;

		case R.id.mc_loiter:
			if (drone.type.getFirmwareType() == FirmwareType.AR_DRONE) {
				msg.command = (short) MAV_CMD.MAV_CMD_OVERRIDE_GOTO;
				msg.param1 = MAV_GOTO.MAV_GOTO_DO_HOLD;
				msg.param2 = MAV_GOTO.MAV_GOTO_HOLD_AT_CURRENT_POSITION;
				drone.MavClient.sendMavPacket(msg.pack());
			} else {
				drone.state.changeFlightMode(ApmModes.ROTOR_LOITER);
				eventBuilder.setAction("Changed flight mode").setLabel(ApmModes.ROTOR_LOITER.getName());
			}
			break;

		case R.id.mc_follow:
			followMe.toggleFollowMeState();
			eventBuilder.setAction("FollowMe selected").setLabel(
					followMe.isEnabled() ? "FollowMe enabled" : "FollowMe disabled");
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
