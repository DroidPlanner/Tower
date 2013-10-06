package com.droidplanner.activitys;

import android.view.Menu;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;

public abstract class NewSuperUI extends SuperActivity {

	public NewSuperUI() {
		super();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_newui, menu);
		return super.onCreateOptionsMenu(menu);
	}

}