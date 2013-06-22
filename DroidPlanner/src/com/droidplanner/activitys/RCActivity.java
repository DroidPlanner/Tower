package com.droidplanner.activitys;

import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.droidplanner.R;
import com.droidplanner.helpers.RcOutput;
import com.droidplanner.widgets.joystick.DualJoystickView;
import com.droidplanner.widgets.joystick.JoystickMovedListener;

public class RCActivity extends SuperActivity implements
		 OnClickListener {


	private Button bTogleRC;

	MenuItem connectButton;

	private RcOutput rcOutput;

	@Override
	int getNavigationItem() {
		return 2;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.rc);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		DualJoystickView joystick = (DualJoystickView)findViewById(R.id.joystickView);
        
        joystick.setOnJostickMovedListener(lJoystick, rJoystick);
        joystick.setLeftAutoReturnToCenter(false, true);
        
		bTogleRC = (Button) findViewById(R.id.bTogleRC);
		bTogleRC.setOnClickListener(this);

		rcOutput = new RcOutput(app.MAVClient,this);
	}
	


	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_pid, menu);
		connectButton = menu.findItem(R.id.menu_connect);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}


	@Override
	public void onClick(View v) {
		//printInputDevicesToLog();
		
		if (v == bTogleRC) {
			if (rcOutput.isRcOverrided()) {
				rcOutput.disableRcOverride();
				lJoystick.OnMoved(0f, 0f);
				rJoystick.OnMoved(0f, 0f);
				bTogleRC.setText(R.string.enable_rc_control);
			} else {
				rcOutput.enableRcOverride();
				bTogleRC.setText(R.string.disable_rc_control);
			}
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

	JoystickMovedListener lJoystick = new JoystickMovedListener() {
		@Override
		public void OnReturnedToCenter() {
		}
		@Override
		public void OnReleased() {
		}
		@Override
		public void OnMoved(double pan, double tilt) {
			rcOutput.setRcChannel(RcOutput.RUDDER, pan);
			rcOutput.setRcChannel(RcOutput.TROTTLE, tilt);
		}
	};
	JoystickMovedListener rJoystick = new JoystickMovedListener() {
		@Override
		public void OnReturnedToCenter() {
		}
		@Override
		public void OnReleased() {
		}
		@Override
		public void OnMoved(double pan, double tilt) {
			rcOutput.setRcChannel(RcOutput.AILERON, pan);
			rcOutput.setRcChannel(RcOutput.ELEVATOR, tilt);
		}
	};
}