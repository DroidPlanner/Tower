package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
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

public class FlightActionsFragment extends Fragment implements OnClickListener {

	public interface OnMissionControlInteraction {
		public void onJoystickSelected();

		public void onPlanningSelected();
	}

	private Drone drone;
	private OnMissionControlInteraction listener;
	private Button homeBtn;
	private Button missionBtn;
	private Button joystickBtn;
	private Button landBtn;
	private Button loiterBtn;
	private Button takeoffBtn;
	private Button followBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mission_control,
				container, false);
		setupViews(view);
		setupListener();
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = (OnMissionControlInteraction) activity;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void setupViews(View parentView) {
		missionBtn = (Button) parentView.findViewById(R.id.mc_planningBtn);
		joystickBtn = (Button) parentView.findViewById(R.id.mc_joystickBtn);
		homeBtn = (Button) parentView.findViewById(R.id.mc_homeBtn);
		landBtn = (Button) parentView.findViewById(R.id.mc_land);
		takeoffBtn = (Button) parentView.findViewById(R.id.mc_takeoff);
		loiterBtn = (Button) parentView.findViewById(R.id.mc_loiter);
		followBtn = (Button) parentView.findViewById(R.id.mc_follow);
	}

	private void setupListener() {
		missionBtn.setOnClickListener(this);
		joystickBtn.setOnClickListener(this);
		homeBtn.setOnClickListener(this);
		landBtn.setOnClickListener(this);
		takeoffBtn.setOnClickListener(this);
		loiterBtn.setOnClickListener(this);
		followBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mc_planningBtn:
			listener.onPlanningSelected();
			break;
		case R.id.mc_joystickBtn:
			listener.onJoystickSelected();
			break;
		case R.id.mc_land:
			drone.state.changeFlightMode(ApmModes.ROTOR_LAND);
			break;
		case R.id.mc_takeoff:
			//drone.state.changeFlightMode(ApmModes.ROTOR_TAKEOFF); //TODO there isn`t a takeoff mode on ArduCopter
			break;
		case R.id.mc_homeBtn:
			drone.state.changeFlightMode(ApmModes.ROTOR_RTL);
			break;
		case R.id.mc_loiter:
			drone.state.changeFlightMode(ApmModes.ROTOR_LOITER);
			break;
		}
	}

}
