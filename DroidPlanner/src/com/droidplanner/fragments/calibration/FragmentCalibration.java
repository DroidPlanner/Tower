package com.droidplanner.fragments.calibration;

import com.droidplanner.fragments.SetupFragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

public abstract class FragmentCalibration extends Fragment {
	protected SetupFragment parent;
	protected FragmentManager fragmentManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		fragmentManager = getFragmentManager();
		super.onCreate(savedInstanceState);
	}

	public SetupFragment getParent() {
		return parent;
	}

	public void setParent(SetupFragment parent) {
		this.parent = parent;
	}

	protected abstract void setupSidePanel();
}
