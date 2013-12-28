package com.droidplanner.fragments.calibration;

import com.droidplanner.fragments.SetupFragment;

import android.app.Fragment;

public abstract class FragmentCalibration extends Fragment {
	private SetupFragment parent;

	public SetupFragment getParent() {
		return parent;
	}

	public void setParent(SetupFragment parent) {
		this.parent = parent;
	}
}
