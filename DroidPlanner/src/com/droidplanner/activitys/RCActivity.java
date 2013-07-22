package com.droidplanner.activitys;

import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.droidplanner.R;
import com.droidplanner.helpers.RcOutput;
import com.droidplanner.widgets.joystick.JoystickMovedListener;
import com.droidplanner.widgets.joystick.JoystickView;

public class RCActivity extends SuperActivity {
	private MenuItem bTogleRC;
	private TextView textViewLPan, textViewLTilt, textViewRPan, textViewRTilt;

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
		
		textViewLPan = (TextView) findViewById(R.id.textViewRCJoyLPan);
		textViewLPan.setText("");
		textViewLTilt = (TextView) findViewById(R.id.textViewRCJoyLTilt);
		textViewLTilt.setText("");
		textViewRPan = (TextView) findViewById(R.id.textViewRCJoyRPan);
		textViewRPan.setText("");
		textViewRTilt = (TextView) findViewById(R.id.textViewRCJoyRTilt);
		textViewRTilt.setText("");
		
		JoystickView joystickL = (JoystickView)findViewById(R.id.joystickViewL);
		JoystickView joystickR = (JoystickView)findViewById(R.id.joystickViewR);
		
		joystickL.setAxisAutoReturnToCenter(false, true);
		joystickL.setOnJostickMovedListener(lJoystick);
		joystickR.setOnJostickMovedListener(rJoystick);
        
		rcOutput = new RcOutput(app.MAVClient,this);
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

	@Override
	protected void onResume() {
		disableRCOverride();
		super.onResume();
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
		if (rcOutput.isRcOverrided()) {
			disableRCOverride();
		} else {
			enableRCOverride();
		}
	}

	private void enableRCOverride() {
		rcOutput.enableRcOverride();
		bTogleRC.setTitle(R.string.disable);
	}

	private void disableRCOverride() {
		rcOutput.disableRcOverride();
		lJoystick.OnMoved(0f, 0f);
		rJoystick.OnMoved(0f, 0f);
		if (bTogleRC != null) {
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
			textViewLPan.setText(String.format("Rudd: %.0f%%", pan *100));
			textViewLTilt.setText(String.format("Thrt: %.0f%%", tilt *100));
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
			textViewRPan.setText(String.format("Ail: %.0f%%", pan *100));
			textViewRTilt.setText(String.format("Elev: %.0f%%", tilt *100));
		}
	};
}