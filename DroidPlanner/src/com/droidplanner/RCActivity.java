package com.droidplanner;

import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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
		return 3;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.rc);
		
		DualJoystickView joystick = (DualJoystickView)findViewById(R.id.joystickView);
        
        joystick.setOnJostickMovedListener(lJoystick, rJoystick);

		bTogleRC = (Button) findViewById(R.id.bTogleRC);
		bTogleRC.setOnClickListener(this);

		
		//TODO reimplement  rcOutput = new RcOutput(MAVClient,this);
	}
	
	@Override
	protected void onResume() {
		super.onRestart();
		//TODO reimplement MAVClient.init();
	}

	@Override
	protected void onStop() {
		super.onDestroy();
		//TODO reimplement MAVClient.onDestroy();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_pid, menu);
		connectButton = menu.findItem(R.id.menu_connect);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			//TODO reimplement MAVClient.sendConnectMessage();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
	
	//TODO reimplement
	/*
	private MAVLinkClient MAVClient = new MAVLinkClient(this) {

		@Override
		public void notifyReceivedData(MAVLinkMessage m) {

		}

		@Override
		public void notifyConnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_disconnect));
		}

		@Override
		public void notifyDisconnected() {
			connectButton.setTitle(getResources().getString(
					R.string.menu_connect));
		}
	};
	*/

	@Override
	public void onClick(View v) {
		//printInputDevicesToLog();
		
		if (v == bTogleRC) {
			if (rcOutput.isRcOverrided()) {
				rcOutput.disableRcOverride();
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