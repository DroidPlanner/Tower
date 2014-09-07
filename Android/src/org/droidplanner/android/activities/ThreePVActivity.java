package org.droidplanner.android.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import org.droidplanner.R;
import org.droidplanner.android.fragments.ThreePVControlFragment;
import org.droidplanner.android.fragments.mode.ThreePVTuningFragment;

public class ThreePVActivity extends DrawerNavigationUI{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_three_pv);
		FragmentManager fm = getFragmentManager();
		Fragment controlFragment = fm.findFragmentById(R.id.three_pv_control_fragment);
		if (controlFragment == null) {
			controlFragment = new ThreePVControlFragment();
			fm.beginTransaction().add(R.id.three_pv_control_fragment, controlFragment).commit();
		}
		Fragment tuningFragment = fm.findFragmentById(R.id.three_pv_tuning_fragment);
		if (tuningFragment == null) {
			tuningFragment = new ThreePVTuningFragment();
			fm.beginTransaction().add(R.id.three_pv_tuning_fragment, tuningFragment).commit();
		}
	}

	@Override
	public CharSequence[][] getHelpItems() {
		return new CharSequence[][] { {}, {} };
	}
}
