package com.droidplanner;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.MAVLink.Messages.MAVLinkMessage;
import com.droidplanner.helpers.RcOutput;
import com.droidplanner.service.MAVLinkClient;
import com.droidplanner.widgets.joystick.DualJoystickView;
import com.droidplanner.widgets.joystick.JoystickMovedListener;

public class RCActivity extends SuperActivity implements
		OnSeekBarChangeListener, OnClickListener {

	private SeekBar ch1SeekBar;
	private TextView ch1TextView;
	private SeekBar ch2SeekBar;
	private TextView ch2TextView;

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

		ch1TextView = (TextView) findViewById(R.id.ch1TextView);
		ch1SeekBar = (SeekBar) findViewById(R.id.ch1SeekBar);
		ch1SeekBar.setOnSeekBarChangeListener(this);

		ch2TextView = (TextView) findViewById(R.id.ch2TextView);
		ch2SeekBar = (SeekBar) findViewById(R.id.ch2SeekBar);
		ch2SeekBar.setOnSeekBarChangeListener(this);

		bTogleRC = (Button) findViewById(R.id.bTogleRC);
		bTogleRC.setOnClickListener(this);

		MAVClient.init();

		rcOutput = new RcOutput(MAVClient);

	}

	@Override
	protected void onStop() {
		super.onDestroy();
		MAVClient.onDestroy();
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
		case R.id.menu_settings:
			return true;
		case R.id.menu_connect:
			MAVClient.sendConnectMessage();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

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

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (seekBar == ch1SeekBar) {
			rcOutput.setRcChannel(RcOutput.AILERON, (progress-500)/500.0);
			ch1TextView.setText(Integer.toString(progress + 1000));
		} else if (seekBar == ch2SeekBar) {
			rcOutput.setRcChannel(7,(progress-500)/500.0);
			ch2TextView.setText(Integer.toString(progress + 1000));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onClick(View v) {
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

	JoystickMovedListener lJoystick = new JoystickMovedListener() {
		@Override
		public void OnReturnedToCenter() {
		}
		@Override
		public void OnReleased() {
		}
		@Override
		public void OnMoved(int pan, int tilt) {
			rcOutput.setRcChannel(RcOutput.RUDDER, pan/10.0);
			rcOutput.setRcChannel(RcOutput.TROTTLE, -tilt/10.0);
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
		public void OnMoved(int pan, int tilt) {
			rcOutput.setRcChannel(RcOutput.AILERON, pan/10.0);
			rcOutput.setRcChannel(RcOutput.ELEVATOR, -tilt/10.0);
		}
	};
}