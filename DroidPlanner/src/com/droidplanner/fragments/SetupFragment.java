package com.droidplanner.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.activitys.ConfigurationActivity;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.fragments.calibration.FragmentCalibration;
import com.droidplanner.fragments.calibration.fs.FragmentSetupFS;
import com.droidplanner.fragments.calibration.imu.FragmentSetupIMU;
import com.droidplanner.fragments.calibration.mag.FragmentSetupMAG;

public class SetupFragment extends Fragment implements OnDroneListner, OnItemSelectedListener {
	private Drone drone;
	
	private ConfigurationActivity parent;
	private Spinner spinnerSetup;
	private TextView textViewTitle;
	
	private FragmentManager fragmentManager;
	private FragmentCalibration setupPanel;

	@Override
	public void onAttach(Activity activity) {
		parent = (ConfigurationActivity)activity;
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		fragmentManager = getFragmentManager();
		setupPanel = (FragmentCalibration) fragmentManager
				.findFragmentById(R.id.fragment_setup_mainpanel);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

  		View view = inflater.inflate(R.layout.fragment_setup, container,
				false);
		setupLocalViews(view);		
		setupFragmentPanel(view);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		this.drone = ((DroidPlannerApp) getActivity().getApplication()).drone;		
		super.onActivityCreated(savedInstanceState);
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
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		changeSetupPanel(arg2);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		// TODO Auto-generated method stub
		
	}

	private void setupLocalViews(View view) {
		textViewTitle = (TextView)view.findViewById(R.id.textViewSetupTitle);
		spinnerSetup = (Spinner)view.findViewById(R.id.spinnerSetupType);
		spinnerSetup.setOnItemSelectedListener(this);
		
		
		final ArrayAdapter<String> adapter=new ArrayAdapter<String>(parent, R.layout.spinner_setup);
		adapter.add("Acc Calibration");
		adapter.add("Compass Calibration");
		adapter.add("Failsafe setup");
		spinnerSetup.setAdapter(adapter);
	}

	private void setupFragmentPanel(View view) {
		if (setupPanel == null) {
			setupPanel = new FragmentSetupIMU();
			setupPanel.setParent(this);
			
			fragmentManager.beginTransaction()
					.add(R.id.fragment_setup_mainpanel, setupPanel).commit();
		} else {
//			cancel();
		}
	}
	
	public void changeSetupPanel(int step) {
		switch (step) {
		case 0:
				setupPanel = getIMUPanel();
			break;
		case 1:
				setupPanel = getMAGPanel();
			break;
		case 2:
				setupPanel = getFSPanel();
			break;
		}
		fragmentManager.beginTransaction()
				.replace(R.id.fragment_setup_mainpanel, setupPanel).commit();
	}

	private FragmentCalibration getFSPanel() {
		setupPanel = new FragmentSetupFS();
		setupPanel.setParent(this);
		textViewTitle.setText(R.string.setup_fs_title);
		return setupPanel;
	}

	private FragmentCalibration getMAGPanel() {
		setupPanel = new FragmentSetupMAG();
		setupPanel.setParent(this);
		textViewTitle.setText(R.string.setup_mag_title);
		return setupPanel;
	}

	private FragmentCalibration getIMUPanel() {
		setupPanel = new FragmentSetupIMU();
		setupPanel.setParent(this);
		textViewTitle.setText(R.string.setup_imu_title);
		return setupPanel;
	}

}
