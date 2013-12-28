package com.droidplanner.fragments.calibration.imu;

import com.droidplanner.R;
import com.droidplanner.fragments.calibration.FragmentCalibration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentSetupIMU extends FragmentCalibration {

	private FragmentSetupIMUCalibrate sidePanel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sidePanel = (FragmentSetupIMUCalibrate) fragmentManager
				.findFragmentById(R.id.fragment_setup_sidepanel);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_imu_main,
				container, false);
		setupLocalViews(view);
		setupSidePanel();
		return view;
	}

	private void setupLocalViews(View view) {
	}

	@Override
	protected void setupSidePanel() {
		if (sidePanel == null) {
			sidePanel = new FragmentSetupIMUCalibrate();
			sidePanel.setParent(this);
			fragmentManager.beginTransaction()
					.add(R.id.fragment_setup_sidepanel, sidePanel).commit();
		}
	}
}
