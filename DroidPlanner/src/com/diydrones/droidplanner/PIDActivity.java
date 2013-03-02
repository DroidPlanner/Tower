package com.diydrones.droidplanner;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class PIDActivity extends Activity {

	@Override
	int getNavigationItem() {
		return 3;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pid);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_pid, menu);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			return true;
		case R.id.menu_connect:
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

}
