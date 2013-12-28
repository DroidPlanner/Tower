package com.droidplanner.fragments.calibration.imu;

import com.droidplanner.R;
import com.droidplanner.fragments.calibration.FragmentCalibration;
import com.droidplanner.fragments.calibration.FragmentSetupSidePanel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentSetupIMU extends FragmentCalibration {

	public int calibration_step = 0;

	@Override
	protected View getView(LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.fragment_setup_imu_main,
				container, false);
	}

	@Override
	protected void setupLocalViews(View view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected FragmentSetupSidePanel getSidePanel() {
		return new FragmentSetupIMUCalibrate();
	}

	@Override

	protected void initSidePanel() {
		sidePanel = (FragmentSetupIMUCalibrate) fragmentManager
				.findFragmentById(R.id.fragment_setup_sidepanel);
	}

	@Override
	protected void updateSidePanel() {
		sidePanel = new FragmentSetupIMUCalibrate();
		sidePanel.setParent(this);
	}

	public void doCalibrationStep(){
		calibration_step++;
		if(calibration_step>7)
			calibration_step = 0;
		processCalibrationStep(calibration_step);
	}

	private void processCalibrationStep(int calibration_step2) {
		// TODO Auto-generated method stub
		
	}
}
