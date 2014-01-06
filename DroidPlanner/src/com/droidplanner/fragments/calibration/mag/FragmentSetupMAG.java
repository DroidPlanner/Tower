package com.droidplanner.fragments.calibration.mag;

import android.os.Bundle;
import com.droidplanner.R;
import com.droidplanner.fragments.SetupFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentSetupMAG extends SetupFragment.SetupCalibration {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_setup_mag_main, container, false);
	}

	@Override
	public SetupFragment.SetupSidePanel getSidePanel() {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public void doCalibrationStep() {
        //TODO: complete implementation
    }
}
