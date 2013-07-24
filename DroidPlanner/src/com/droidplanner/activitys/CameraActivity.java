package com.droidplanner.activitys;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.droidplanner.R;
import com.droidplanner.helpers.RcOutput;
import com.droidplanner.widgets.joystick.JoystickMovedListener;
import com.droidplanner.widgets.joystick.JoystickView;

public class CameraActivity extends SuperActivity {

	private MenuItem bTogleCamera;
	private RcOutput rcOutput;
	private TextView textViewLPan, textViewLTilt, textViewRPan, textViewRTilt;

	@Override
	int getNavigationItem() {
		return 4;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.camera);

		textViewLPan = (TextView) findViewById(R.id.textViewCamJoyLPan);
		textViewLPan.setText(" RC8: 0%");
		textViewLTilt = (TextView) findViewById(R.id.textViewCamJoyLTilt);
		textViewLTilt.setText("");
		textViewRPan = (TextView) findViewById(R.id.textViewCamJoyRPan);
		textViewRPan.setText("RC7: 0%");
		textViewRTilt = (TextView) findViewById(R.id.textViewCamJoyRTilt);
		textViewRTilt.setText("RC6: 0%");

		JoystickView joystickL = (JoystickView) findViewById(R.id.joystickViewCamL);
		JoystickView joystickR = (JoystickView) findViewById(R.id.joystickViewCamR);

		joystickL.setOnJostickMovedListener(lJoystick);
		joystickR.setOnJostickMovedListener(rJoystick);

		rcOutput = new RcOutput(drone, this);
	}

	@Override
	protected void onDestroy() {
		disableRcOverride();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		disableRcOverride();
		super.onPause();
	}

	@Override
	protected void onResume() {
		disableRcOverride();
		super.onResume();
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
			disableRcOverride();
		} else {
			enableRcOverride();
		}
	}

	private void enableRcOverride() {
		rcOutput.enableRcOverride();
		lJoystick.OnMoved(0f, 0f);
		rJoystick.OnMoved(0f, 0f);
		bTogleCamera.setTitle(R.string.disable);
	}

	private void disableRcOverride() {
		rcOutput.disableRcOverride();
		if (bTogleCamera != null) {
			bTogleCamera.setTitle(R.string.enable);
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
			rcOutput.setRcChannel(RcOutput.RC8, pan);
			textViewLPan.setText(String.format("RC8: %.0f%%", pan * 100));
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
			rcOutput.setRcChannel(RcOutput.RC6, tilt);
			rcOutput.setRcChannel(RcOutput.RC7, pan);
			textViewRPan.setText(String.format("RC7: %.0f%%", pan * 100));
			textViewRTilt.setText(String.format("RC6: %.0f%%", tilt * 100));
		}
	};
}