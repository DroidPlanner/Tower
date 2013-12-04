package com.droidplanner.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.OnRcDataChangedListner;

public class ModesSetupFragment extends Fragment implements OnRcDataChangedListner {

	// Extreme RC update rate in this screen
	//private static final int RC_MSG_RATE = 50;

	private Drone drone;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		View view = inflater.inflate(R.layout.fragment_mode_setup, container,false);

		setupLocalViews(view);
		drone.RC.setListner(this);
		return view;
	}

	private void setupLocalViews(View view) {
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onNewInputRcData() {
		int[] data = drone.RC.in;
	}

	@Override
	public void onNewOutputRcData() {
		// TODO Auto-generated method stub
	}

}
