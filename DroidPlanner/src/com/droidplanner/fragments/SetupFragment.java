package com.droidplanner.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.droidplanner.fragments.calibration.imu.FragmentSetupIMU;
import com.droidplanner.fragments.calibration.mag.FragmentSetupMAG;

/**
 * This fragment is used to calibrate the drone's compass, and accelerometer.
 */
public class SetupFragment extends Fragment implements OnDroneListner, OnItemSelectedListener {

    public static abstract class SetupCalibration extends Fragment {
        public abstract void doCalibrationStep();

        public abstract SetupSidePanel getSidePanel();
    }

    public static abstract class SetupSidePanel extends Fragment {
        public abstract void updateTitle(int calibrationStep);
    }

	private Drone drone;
	
	private ConfigurationActivity parent;
	private Spinner spinnerSetup;
	private TextView textViewTitle;
	
	private FragmentManager fragmentManager;
	private SetupCalibration setupPanel;
    private SetupSidePanel sidePanel;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        if(!(activity instanceof ConfigurationActivity)){
            throw new IllegalStateException("Parent activity must be " + ConfigurationActivity
                    .class.getName());
        }

        parent = (ConfigurationActivity)activity;
	}

    @Override
    public void onDetach(){
        super.onDetach();
        parent = null;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

  		final View view = inflater.inflate(R.layout.fragment_setup, container, false);

        setupLocalViews(view);

        fragmentManager = getChildFragmentManager();
        setupPanel = (SetupCalibration) fragmentManager.findFragmentById(R.id
                .fragment_setup_mainpanel);

        if (setupPanel == null) {
            setupPanel = new FragmentSetupIMU();

            fragmentManager.beginTransaction()
                    .add(R.id.fragment_setup_mainpanel, setupPanel)
                    .commit();
        }

        sidePanel =  (SetupSidePanel) fragmentManager.findFragmentById(R.id.fragment_setup_sidepanel);
        if (sidePanel == null) {
            sidePanel = setupPanel.getSidePanel();
            if (sidePanel != null) {
                fragmentManager.beginTransaction()
                        .add(R.id.fragment_setup_sidepanel, sidePanel)
                        .commit();
            }
        }

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		this.drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
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
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
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
		adapter.add("ACC Calibration");
		adapter.add("Compass Calibration");
		spinnerSetup.setAdapter(adapter);
	}

	public void changeSetupPanel(int step) {
		switch (step) {
		case 0:
            setupPanel = getIMUPanel();
            sidePanel = setupPanel.getSidePanel();
			break;

		case 1:
            setupPanel = getMAGPanel();
            sidePanel = setupPanel.getSidePanel();
			break;
		}

        final FragmentTransaction ft = fragmentManager.beginTransaction();
        if(setupPanel != null){
            ft.replace(R.id.fragment_setup_mainpanel, setupPanel);
        }

        if(sidePanel != null){
            ft.replace(R.id.fragment_setup_sidepanel, sidePanel);
        }

		ft.commit();
	}

	private SetupCalibration getMAGPanel() {
		setupPanel = new FragmentSetupMAG();
		textViewTitle.setText(R.string.setup_mag_title);
		return setupPanel;
	}

	private SetupCalibration getIMUPanel() {
		setupPanel = new FragmentSetupIMU();
		textViewTitle.setText(R.string.setup_imu_title);
		return setupPanel;
	}

    public void doCalibrationStep(){
        if(setupPanel != null){
            setupPanel.doCalibrationStep();
        }
    }

    public void updateSidePanelTitle(int calibrationStep){
        if(sidePanel != null){
            sidePanel.updateTitle(calibrationStep);
        }
    }

}
