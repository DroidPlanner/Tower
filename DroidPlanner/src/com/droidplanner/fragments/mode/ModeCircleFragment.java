package com.droidplanner.fragments.mode;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.OnStateListner;

public class ModeCircleFragment extends Fragment implements
		OnClickListener, OnStateListner {

	/*public interface OnModeCircleInteraction {
		public void onJoystickSelected();

		public void onPlanningSelected();
	}*/

	//private Drone drone;
	//private OnModeCircleInteraction listner;
	//private Button homeBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_mode_circle, container, false);
		setupViews(view);
		setupListner();

		//drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		//drone.state.addFlightStateListner(this);
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		//listner = (OnModeCircleInteraction) activity;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//drone.state.removeFlightStateListner(this);
	}

	private void setupViews(View parentView) {
		/*
		missionBtn = (Button) parentView.findViewById(R.id.mc_planningBtn);
		joystickBtn = (Button) parentView.findViewById(R.id.mc_joystickBtn);
		homeBtn = (Button) parentView.findViewById(R.id.mc_homeBtn);
		landBtn = (Button) parentView.findViewById(R.id.mc_land);
		takeoffBtn = (Button) parentView.findViewById(R.id.mc_takeoff);
		loiterBtn = (Button) parentView.findViewById(R.id.mc_loiter);
		followBtn = (Button) parentView.findViewById(R.id.mc_follow);
		setToLandedState();
		*/
	}

	private void setupListner() {
		/*
		missionBtn.setOnClickListener(this);
		joystickBtn.setOnClickListener(this);
		homeBtn.setOnClickListener(this);
		landBtn.setOnClickListener(this);
		takeoffBtn.setOnClickListener(this);
		loiterBtn.setOnClickListener(this);
		followBtn.setOnClickListener(this);
		*/
	}

	@Override
	public void onClick(View v) {
		/*
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
		*/
	}

	@Override
	public void onFlightStateChanged() {
		/*
		if (drone.state.isFlying()) {
			setToFlyingState();
		}else{
			setToLandedState();
		}
		*/
	}

	private void setToLandedState() {
		/*
		takeoffBtn.setVisibility(View.VISIBLE);
		landBtn.setVisibility(View.GONE);
		homeBtn.setVisibility(View.GONE);
		loiterBtn.setVisibility(View.GONE);
		followBtn.setVisibility(View.GONE);
		*/
	}

	private void setToFlyingState() {
		/*
		landBtn.setVisibility(View.VISIBLE);
		homeBtn.setVisibility(View.VISIBLE);
		loiterBtn.setVisibility(View.VISIBLE);
		takeoffBtn.setVisibility(View.GONE);
		*/
	}

	@Override
	public void onArmChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFailsafeChanged() {
		// TODO Auto-generated method stub

	}

}
