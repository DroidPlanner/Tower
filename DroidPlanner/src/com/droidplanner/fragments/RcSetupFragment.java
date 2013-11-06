package com.droidplanner.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.MAVLink.MavLinkStreamRates;
import com.droidplanner.drone.Drone;

public class RcSetupFragment extends Fragment {
	private Drone drone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		View view = inflater.inflate(R.layout.fragment_rc_setup, container,
				false);
		setupLocalViews(view);		
		return view;	
	}

	private void setupLocalViews(View view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStart() {
		super.onStart();
		setupDataStreamingForRcSetup(); 
	}

	private void setupDataStreamingForRcSetup() {
		// Sets the nav messages at 50Hz and other messages at a low rate 1Hz
		//TODO set correct values
		//MavLinkStreamRates.setupStreamRates(drone.MavClient, 1, 0, 1, 1, 1, 0, 0, NAV_MSG_RATE);
	}

	@Override
	public void onStop() {
		super.onStop();
		MavLinkStreamRates.setupStreamRatesFromPref((DroidPlannerApp) getActivity().getApplication());
	}
	
}
