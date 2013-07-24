package com.droidplanner.fragments;

import java.util.Locale;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.MAVLink.Messages.ApmModes;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.helpers.RcOutput;
import com.droidplanner.widgets.joystick.JoystickMovedListener;
import com.droidplanner.widgets.joystick.JoystickView;

public class RCFragment extends Fragment {

	private JoystickView joystickL, joystickR;
	private TextView textViewLPan, textViewLTilt, textViewRPan, textViewRTilt;
	private ToggleButton activeButton;
	private Button quickModeButtonLeft, quickModeButtonRight;

	private DroidPlannerApp app;
	private Drone drone;
	private RcOutput rcOutput;
	private boolean rcActivated = false;
	private double lLastPan = 0, lLastTilt = 0, rLastPan = 0, rLastTilt = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.rc_fragment, container, false);

		app = (DroidPlannerApp) getActivity().getApplication();
		drone = app.drone;
		rcOutput = new RcOutput(drone, app);

		textViewLPan = (TextView) view.findViewById(R.id.textViewRCJoyLPan);
		textViewLPan.setText("(Rudd: 0%)");
		textViewLTilt = (TextView) view.findViewById(R.id.textViewRCJoyLTilt);
		textViewLTilt.setText("(Thrt: 0%)");
		textViewRPan = (TextView) view.findViewById(R.id.textViewRCJoyRPan);
		textViewRPan.setText("(Ail: 0%)");
		textViewRTilt = (TextView) view.findViewById(R.id.textViewRCJoyRTilt);
		textViewRTilt.setText("(Elev: 0%)");

		quickModeButtonLeft = (Button) view
				.findViewById(R.id.buttonRCQuickLeft);
		quickModeButtonLeft.setText("Loiter");
		quickModeButtonLeft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ApmModes mode = ApmModes.getMode("Loiter", drone.type.getType());
				drone.state.setMode(mode);
			}
		});

		quickModeButtonRight = (Button) view
				.findViewById(R.id.buttonRCQuickRight);
		quickModeButtonRight.setText("Stabilize");
		quickModeButtonRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ApmModes mode = ApmModes.getMode("Stabilize",
						drone.type.getType());
				drone.state.setMode(mode);
			}
		});

		joystickL = (JoystickView) view.findViewById(R.id.joystickViewL);
		joystickR = (JoystickView) view.findViewById(R.id.joystickViewR);

		joystickL.setAxisAutoReturnToCenter(false, true);
		joystickL.setOnJostickMovedListener(lJoystick);
		joystickR.setOnJostickMovedListener(rJoystick);

		activeButton = (ToggleButton) view
				.findViewById(R.id.toggleButtonRCActivate);
		activeButton.setTextOn(getString(R.string.rc_control) + "  [ "
				+ getString(R.string.on).toUpperCase(Locale.getDefault())
				+ " ]");
		activeButton.setTextOff(getString(R.string.rc_control) + "  [ "
				+ getString(R.string.off).toUpperCase(Locale.getDefault())
				+ " ]");
		activeButton.setChecked(rcOutput.isRcOverrided());
		activeButton
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							enableRCOverride();
						} else {
							disableRCOverride();
						}
						buttonView.setChecked(rcOutput.isRcOverrided());
					}
				});

		return view;
	}

	@Override
	public void onStop() {
		disableRCOverride();
		super.onDestroyView();
	}

	public boolean isRcOverrideActive() {
		return rcActivated;
	}

	private void enableRCOverride() {
		rcOutput.enableRcOverride();
		rcActivated = rcOutput.isRcOverrided();
		lJoystick.OnMoved(lLastPan, lLastTilt);
		rJoystick.OnMoved(rLastPan, rLastTilt);
	}

	private void disableRCOverride() {
		rcOutput.disableRcOverride();
		rcActivated = false;
		lJoystick.OnMoved(lLastPan, lLastTilt);
		rJoystick.OnMoved(rLastPan, rLastTilt);
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
			lLastPan = pan;
			lLastTilt = tilt;
			if (rcActivated) {
				rcOutput.setRcChannel(RcOutput.RUDDER, pan);
				rcOutput.setRcChannel(RcOutput.TROTTLE, tilt);
				textViewLPan.setText(String.format("Rudd: %.0f%%", pan * 100));
				textViewLTilt
						.setText(String.format("Thrt: %.0f%%", tilt * 100));
			} else {
				textViewLPan
						.setText(String.format("(Rudd: %.0f%%)", pan * 100));
				textViewLTilt.setText(String.format("(Thrt: %.0f%%)",
						tilt * 100));
			}
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
			rLastPan = pan;
			rLastTilt = tilt;
			if (rcActivated) {
				rcOutput.setRcChannel(RcOutput.AILERON, pan);
				rcOutput.setRcChannel(RcOutput.ELEVATOR, tilt);
				textViewRPan.setText(String.format("Ail: %.0f%%", pan * 100));
				textViewRTilt
						.setText(String.format("Elev: %.0f%%", tilt * 100));
			} else {
				textViewRPan.setText(String.format("(Ail: %.0f%%)", pan * 100));
				textViewRTilt.setText(String.format("(Elev: %.0f%%)",
						tilt * 100));
			}
		}
	};

}
