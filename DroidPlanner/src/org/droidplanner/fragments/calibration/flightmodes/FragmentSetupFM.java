package org.droidplanner.fragments.calibration.flightmodes;

import org.droidplanner.fragments.calibration.SetupMainPanel;
import org.droidplanner.fragments.calibration.SetupSidePanel;

import org.droidplanner.R;
import android.view.View;

public class FragmentSetupFM extends SetupMainPanel {

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_fm_main;
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
