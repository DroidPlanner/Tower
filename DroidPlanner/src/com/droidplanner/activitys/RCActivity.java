package com.droidplanner.activitys;

import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.Menu;
import android.view.MenuItem;
import com.droidplanner.R;
import com.droidplanner.fragments.RCFragment;

public class RCActivity extends SuperActivity {
	
	private RCFragment rcFragment;
	
	private MenuItem bTogleRC;
	//private TextView textViewLPan, textViewLTilt, textViewRPan, textViewRTilt;

	MenuItem connectButton;

	@Override
	int getNavigationItem() {
		return 2;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.rc);
		
		rcFragment = ((RCFragment)getFragmentManager().findFragmentById(R.id.rcFragment));
		
	}
	
	@Override
	protected void onDestroy() {
		disableRCOverride();
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		disableRCOverride();
		super.onPause();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_rc, menu);
		connectButton = menu.findItem(R.id.menu_connect);
		bTogleRC = menu.findItem(R.id.menu_rc_enable);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_rc_enable:
			toggleRcOverride();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}


	private void toggleRcOverride() {
		if (rcFragment.isRcOverrideActive()) {
			disableRCOverride();
		} else {
			enableRCOverride();
		}
	}

	private void enableRCOverride() {
		rcFragment.setRcOverrideActive(true);
		if (rcFragment.isRcOverrideActive()) {
			bTogleRC.setTitle(R.string.disable);
		} else {
			bTogleRC.setTitle(R.string.enable);
		}
	}

	private void disableRCOverride() {
		rcFragment.setRcOverrideActive(false);
		if (rcFragment.isRcOverrideActive()) {
			bTogleRC.setTitle(R.string.disable);
		} else {
			bTogleRC.setTitle(R.string.enable);
		}
	}

	@SuppressWarnings("unused")
	private void printInputDevicesToLog() {
		int[] inputIds = InputDevice.getDeviceIds();
		Log.d("DEV", "Found " + inputIds.length);
		for (int i = 0; i < inputIds.length; i++) {
			InputDevice inputDevice = InputDevice.getDevice(inputIds[i]);
			Log.d("DEV","name:"+inputDevice.getName()+" Sources:"+inputDevice.getSources());	
		}
	}

}