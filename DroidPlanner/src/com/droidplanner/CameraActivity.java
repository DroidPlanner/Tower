package com.droidplanner;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.droidplanner.helpers.RcOutput;
import com.droidplanner.widgets.joystick.DualJoystickView;
import com.droidplanner.widgets.joystick.JoystickMovedListener;

public class CameraActivity extends SuperActivity {

	private MenuItem bTogleCamera;
	private RcOutput rcOutput;

	@Override
	int getNavigationItem() {
		return 3;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.camera);

		DualJoystickView joystick = (DualJoystickView) findViewById(R.id.joystickView);

		joystick.setOnJostickMovedListener(lJoystick, rJoystick);
		rcOutput = new RcOutput(app.MAVClient, this);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_camera, menu);
		bTogleCamera = menu.findItem(R.id.menu_camera_enable);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_camera_enable:
			toggleCameraOverride();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
	
	private void toggleCameraOverride() {
		if (rcOutput.isRcOverrided()) {
			rcOutput.disableRcOverride();
			bTogleCamera.setTitle("Enable");
		} else {
			rcOutput.enableRcOverride();
			bTogleCamera.setTitle("Disable");
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
			// rcOutput.setRcChannel(RcOutput.RUDDER, pan);
			// rcOutput.setRcChannel(RcOutput.TROTTLE, tilt);
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
			// rcOutput.setRcChannel(RcOutput.AILERON, pan);
			// rcOutput.setRcChannel(RcOutput.ELEVATOR, tilt);
		}
	};
}