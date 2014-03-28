package org.droidplanner.android.fragments.calibration.mag;

import org.droidplanner.R;
import org.droidplanner.android.fragments.calibration.SetupMainPanel;
import org.droidplanner.android.fragments.calibration.SetupSidePanel;

import android.view.View;

public class FragmentSetupMAG extends SetupMainPanel {

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_mag_main;
	}

	@Override
	public SetupSidePanel getSidePanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setupLocalViews(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doCalibrationStep(int step) {
		// TODO Auto-generated method stub

	}
}
