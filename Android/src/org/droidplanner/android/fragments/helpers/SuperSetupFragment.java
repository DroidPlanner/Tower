package org.droidplanner.android.fragments.helpers;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.ConfigurationActivity;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.android.fragments.calibration.SetupMainPanel;
import org.droidplanner.android.fragments.calibration.SetupSidePanel;

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

/**
 * This fragment is used to calibrate the drone's compass, and accelerometer.
 */
public abstract class SuperSetupFragment extends Fragment implements
		OnDroneListener, OnItemSelectedListener {

	private Drone drone;

	protected ConfigurationActivity parentActivity;
	private Spinner spinnerSetup;

	private FragmentManager fragmentManager;
	private SetupMainPanel setupPanel;
	private SetupSidePanel sidePanel;

	public abstract int getSpinnerItems();

	public abstract SetupMainPanel getMainPanel(int index);

	public abstract SetupMainPanel initMainPanel();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof ConfigurationActivity)) {
			throw new IllegalStateException("Parent activity must be "
					+ ConfigurationActivity.class.getName());
		}

		parentActivity = (ConfigurationActivity) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		parentActivity = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_setup, container,
				false);

		setupLocalViews(view);

		fragmentManager = getChildFragmentManager();
		setupPanel = (SetupMainPanel) fragmentManager
				.findFragmentById(R.id.fragment_setup_mainpanel);

		if (setupPanel == null) {
			setupPanel = initMainPanel();

			fragmentManager.beginTransaction()
					.add(R.id.fragment_setup_mainpanel, setupPanel).commit();
		}

		sidePanel = (SetupSidePanel) fragmentManager
				.findFragmentById(R.id.fragment_setup_sidepanel);
		if (sidePanel == null) {
			sidePanel = setupPanel.getSidePanel();
			if (sidePanel != null) {
				fragmentManager.beginTransaction()
						.add(R.id.fragment_setup_sidepanel, sidePanel).commit();
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
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		changeMainPanel(arg2);
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
		spinnerSetup = (Spinner) view.findViewById(R.id.spinnerSetupType);
		spinnerSetup.setOnItemSelectedListener(this);

		final ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(parentActivity, getSpinnerItems(),
						R.layout.spinner_setup);

		spinnerSetup.setAdapter(adapter);
	}

	public SetupMainPanel changeMainPanel(int step) {
		setupPanel = getMainPanel(step);
		sidePanel = setupPanel.getSidePanel();

		final FragmentTransaction ft = fragmentManager.beginTransaction();
		if (setupPanel != null) {
			ft.replace(R.id.fragment_setup_mainpanel, setupPanel);
		}

		if (sidePanel != null) {
			ft.replace(R.id.fragment_setup_sidepanel, sidePanel);
		}

		ft.commit();
		return setupPanel;
	}

	public SetupSidePanel changeSidePanel(SetupSidePanel sPanel) {
		sidePanel = sPanel;

		if (setupPanel != null && sidePanel != null)
			setupPanel.setSidePanel(sidePanel);

		final FragmentTransaction ft = fragmentManager.beginTransaction();
		if (sidePanel != null) {
			ft.replace(R.id.fragment_setup_sidepanel, sidePanel);
		}

		ft.commit();

		return sidePanel;
	}

	public void doCalibrationStep(int step) {
		if (setupPanel != null) {
			setupPanel.doCalibrationStep(step);
		}
	}

	public void updateSidePanelTitle(int calibrationStep) {
		if (sidePanel != null) {
			sidePanel.updateDescription(calibrationStep);
		}
	}

}
