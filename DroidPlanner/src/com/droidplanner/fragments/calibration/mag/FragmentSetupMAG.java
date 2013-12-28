package com.droidplanner.fragments.calibration.mag;

import com.droidplanner.R;
import com.droidplanner.fragments.SetupFragment;
import com.droidplanner.fragments.calibration.FragmentCalibration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class FragmentSetupMAG extends FragmentCalibration implements OnClickListener{
	private SetupFragment parent;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_mag_main, container,
				false);
		setupLocalViews(view);

		return view;
	}

	private void setupLocalViews(View view) {
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	public SetupFragment getParent() {
		return parent;
	}

	public void setParent(SetupFragment parent) {
		this.parent = parent;
	}
}
