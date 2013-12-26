package com.droidplanner.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;

public class IMUSetupFragment extends Fragment implements OnDroneListner {
	private Drone drone;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		this.drone = ((DroidPlannerApp) getActivity().getApplication()).drone;		
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_imu, container,
				false);
		setupLocalViews(view);		

		return view;	
	}

	@Override
	public void onStart() {
		drone.events.addDroneListener(this);
		super.onStart();
	}

	@Override
	public void onStop() {
		drone.events.removeDroneListener(this);
		super.onStop();
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		// TODO Auto-generated method stub
		
	}

	private void setupLocalViews(View view) {
		// TODO Auto-generated method stub
		
	}

}
