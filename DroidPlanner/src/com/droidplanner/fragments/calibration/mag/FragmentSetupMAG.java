package com.droidplanner.fragments.calibration.mag;

import com.droidplanner.R;
import com.droidplanner.fragments.calibration.FragmentCalibration;
import com.droidplanner.fragments.calibration.FragmentSetupSidePanel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentSetupMAG extends FragmentCalibration {

	@Override
	protected View getView(LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.fragment_setup_mag_main, container,
				false);
	}

	@Override
	protected FragmentSetupSidePanel getSidePanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setupLocalViews(View view) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initSidePanel() {
/*		sidePanel = (FragmentSetupIMUCalibrate) fragmentManager
				.findFragmentById(R.id.fragment_setup_sidepanel);
*/	}

	@Override
	protected void updateSidePanel() {
		// TODO Auto-generated method stub
		
	}
}
