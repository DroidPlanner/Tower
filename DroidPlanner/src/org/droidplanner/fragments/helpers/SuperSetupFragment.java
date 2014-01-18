package org.droidplanner.fragments.helpers;

import org.droidplanner.DroidPlannerApp;
import org.droidplanner.activitys.ConfigurationActivity;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListner;
import org.droidplanner.fragments.calibration.SetupMainPanel;
import org.droidplanner.fragments.calibration.SetupSidePanel;
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

import org.droidplanner.R;

/**
 * This fragment is used to calibrate the drone's compass, and accelerometer.
 */
public abstract class SuperSetupFragment extends Fragment implements OnDroneListner, OnItemSelectedListener {

	private Drone drone;
	
	private ConfigurationActivity parent;
	private Spinner spinnerSetup;
	private TextView textViewTitle;
	
	private FragmentManager fragmentManager;
	private SetupMainPanel setupPanel;
    private SetupSidePanel sidePanel;

	public abstract void setupSpinnerItems(ArrayAdapter<String> adapter);
	public abstract SetupMainPanel getMainPanel(int index);
	public abstract SetupMainPanel initMainPanel();

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
        setupPanel = (SetupMainPanel) fragmentManager.findFragmentById(R.id
                .fragment_setup_mainpanel);

        if (setupPanel == null) {
            setupPanel = initMainPanel();

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
	
	public void updateTitle(int id){
		textViewTitle.setText(id);
	}
		
	private void setupLocalViews(View view) {
		textViewTitle = (TextView)view.findViewById(R.id.textViewSetupTitle);
		spinnerSetup = (Spinner)view.findViewById(R.id.spinnerSetupType);
		spinnerSetup.setOnItemSelectedListener(this);

		final ArrayAdapter<String> adapter=new ArrayAdapter<String>(parent, R.layout.spinner_setup);
		setupSpinnerItems(adapter);
		spinnerSetup.setAdapter(adapter);
	}

	public void changeSetupPanel(int step) {
		setupPanel = getMainPanel(step);
		sidePanel = setupPanel.getSidePanel();

		final FragmentTransaction ft = fragmentManager.beginTransaction();
        if(setupPanel != null){
            ft.replace(R.id.fragment_setup_mainpanel, setupPanel);
        }

        if(sidePanel != null){
            ft.replace(R.id.fragment_setup_sidepanel, sidePanel);
        }

		ft.commit();
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
