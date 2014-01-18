package org.droidplanner.fragments.calibration.mag;

import org.droidplanner.fragments.SetupRadioFragment.SetupRCSidePanel;
import org.droidplanner.fragments.calibration.SetupMainPanel;
import org.droidplanner.fragments.calibration.SetupSidePanel;

import android.os.Bundle;
import org.droidplanner.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentSetupMAG extends SetupMainPanel {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_setup_mag_main, container, false);
	}

    @Override
    public void doCalibrationStep() {
        //TODO: complete implementation
    }

	@Override
	public SetupSidePanel getSidePanel() {
		// TODO Auto-generated method stub
		return null;
	}
}
