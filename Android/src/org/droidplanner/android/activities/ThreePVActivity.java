package org.droidplanner.android.activities;

import android.os.Bundle;

import org.droidplanner.R;

public class ThreePVActivity extends DrawerNavigationUI{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_three_pv);
	}

	@Override
	public CharSequence[][] getHelpItems() {
		return new CharSequence[0][];
	}
}
