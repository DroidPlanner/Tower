package org.droidplanner.fragments;

import org.droidplanner.DroidPlannerApp;
import org.droidplanner.drone.Drone;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.MAVLink.Messages.ApmModes;
import org.droidplanner.R;

public class FlightActionsFragment extends Fragment implements	OnClickListener {

	public interface OnMissionControlInteraction {
		public void onJoystickSelected();

		public void onPlanningSelected();
	}

	private Drone drone;
	private OnMissionControlInteraction listner;
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
		setupListner();
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listner = (OnMissionControlInteraction) activity;
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

	private void setupListner() {
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
			listner.onPlanningSelected();
			break;
		case R.id.mc_joystickBtn:
			listner.onJoystickSelected();
			break;
		case R.id.mc_land:
			drone.state.changeFlightMode(ApmModes.ROTOR_LAND);
			break;
		case R.id.mc_takeoff:
			drone.state.changeFlightMode(ApmModes.ROTOR_TAKEOFF);
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
