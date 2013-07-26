package com.droidplanner.activitys;

import android.os.Bundle;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;

public class ChartActivity extends SuperActivity {

	@Override
	public int getNavigationItem() {
		return 6;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parameters);
	}

}