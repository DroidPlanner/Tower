package com.droidplanner.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.activitys.ConfigurationActivity;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;

public class SetupFragment extends Fragment implements OnDroneListner, OnItemSelectedListener {
	private Drone drone;
	private ConfigurationActivity parent;
	private Spinner spinnerSetup;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		this.drone = ((DroidPlannerApp) getActivity().getApplication()).drone;		
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

  		View view = inflater.inflate(R.layout.fragment_setup, container,
				false);
		setupLocalViews(view);		

		return view;	
	}

	@Override
	public void onAttach(Activity activity) {
		parent = (ConfigurationActivity)activity;
		super.onAttach(activity);
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
		spinnerSetup = (Spinner)view.findViewById(R.id.spinnerSetupType);
		spinnerSetup.setOnItemSelectedListener(this);
		
		final ArrayAdapter<String> adapter=new ArrayAdapter<String>(parent, R.layout.spinner_setup);
		adapter.add("Acc Calibration");
		adapter.add("Compass Calibration");
		adapter.add("Failsafe setup");
		spinnerSetup.setAdapter(adapter);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}
