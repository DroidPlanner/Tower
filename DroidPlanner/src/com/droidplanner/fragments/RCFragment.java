package com.droidplanner.fragments;

import java.util.Locale;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
	private TextView textViewThrottle, textViewRudder, textViewAileron, textViewElevator;
	private ToggleButton activeButton;
	private Button quickModeButtonLeft, quickModeButtonRight;

	private DroidPlannerApp app;
	private Drone drone;
	private RcOutput rcOutput;
	private boolean rcActivated = false;
	private boolean rcIsMode1 = false;
	private String quickModeLeft = "", quickModeRight = "";
	private double lLastPan = 0, lLastTilt = 0, rLastPan = 0, rLastTilt = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_rc, container, false);

		app = (DroidPlannerApp) getActivity().getApplication();
		drone = app.drone;
		rcOutput = new RcOutput(drone, app);

		textViewThrottle = (TextView) view.findViewById(R.id.textViewRCThrottle);
		textViewThrottle.setText("(Thrt: 0%)");
		textViewRudder = (TextView) view.findViewById(R.id.textViewRCRudder);
		textViewRudder.setText("(Rudd: 0%)");
		textViewElevator = (TextView) view.findViewById(R.id.textViewRCElevator);
		textViewElevator.setText("(Elev: 0%)");
		textViewAileron = (TextView) view.findViewById(R.id.textViewRCAileron);
		textViewAileron.setText("(Ail: 0%)");

		quickModeButtonLeft = (Button) view
				.findViewById(R.id.buttonRCQuickLeft);
		quickModeButtonLeft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ApmModes mode = ApmModes.getMode(quickModeLeft, drone.type.getType());
				drone.state.setMode(mode);
			}
		});

		quickModeButtonRight = (Button) view
				.findViewById(R.id.buttonRCQuickRight);
		quickModeButtonRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ApmModes mode = ApmModes.getMode(quickModeRight,
						drone.type.getType());
				drone.state.setMode(mode);
			}
		});

		joystickL = (JoystickView) view.findViewById(R.id.joystickViewL);
		joystickR = (JoystickView) view.findViewById(R.id.joystickViewR);
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
	public void onResume () {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app.getApplicationContext());
		quickModeLeft = prefs.getString("pref_rc_quickmode_left", "Loiter");
		quickModeRight = prefs.getString("pref_rc_quickmode_right", "Stabilize");
		quickModeButtonLeft.setText(quickModeLeft);
		quickModeButtonRight.setText(quickModeRight);
		rcIsMode1 = prefs.getString("pref_rc_mode", "MODE2").equalsIgnoreCase("MODE1");
		if (rcIsMode1) {
			joystickL.setAxisAutoReturnToCenter(true, true);
			joystickR.setAxisAutoReturnToCenter(prefs.getBoolean("pref_rc_throttle_returntocenter", false), true);
			joystickL.setYAxisInverted(prefs.getBoolean("pref_rc_elevator_reverse", false));
			joystickL.setXAxisInverted(prefs.getBoolean("pref_rc_rudder_reverse", false));
			joystickR.setYAxisInverted(prefs.getBoolean("pref_rc_throttle_reverse", false));
			joystickR.setXAxisInverted(prefs.getBoolean("pref_rc_aileron_reverse", false));
		} else { //else Mode2
			joystickL.setAxisAutoReturnToCenter(prefs.getBoolean("pref_rc_throttle_returntocenter", false), true);
			joystickR.setAxisAutoReturnToCenter(true, true);
			joystickL.setYAxisInverted(prefs.getBoolean("pref_rc_throttle_reverse", false));
			joystickL.setXAxisInverted(prefs.getBoolean("pref_rc_rudder_reverse", false));
			joystickR.setYAxisInverted(prefs.getBoolean("pref_rc_elevator_reverse", false));
			joystickR.setXAxisInverted(prefs.getBoolean("pref_rc_aileron_reverse", false));
		}
		
		super.onResume();
	}

	@Override
	public void onStop() {
		disableRCOverride();
		super.onDestroyView();
	}

	public boolean isRcOverrideActive() {
		return rcActivated;
	}
	
	public boolean physicalJoyMoved(MotionEvent ev){
		//Tested only for wikipad controller. Probably works with most game controllers.
		if((ev.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
			 lJoystick.OnMoved((double) ev.getAxisValue(MotionEvent.AXIS_X), (double) ev.getAxisValue(MotionEvent.AXIS_Y));
			 rJoystick.OnMoved((double) ev.getAxisValue(MotionEvent.AXIS_Z), (double) ev.getAxisValue(MotionEvent.AXIS_RZ));
			 return true;
		}
		return false;
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
				textViewRudder.setText(String.format("Rudd: %.0f%%", pan * 100));
				if (rcIsMode1) {
					rcOutput.setRcChannel(RcOutput.ELEVATOR, tilt);
					textViewElevator.setText(String.format("Elev: %.0f%%", tilt * 100));
				} else {
					rcOutput.setRcChannel(RcOutput.TROTTLE, tilt);
					textViewThrottle.setText(String.format("Thrt: %.0f%%", tilt * 100));
				}
			} else {
				textViewRudder.setText(String.format("(Rudd: %.0f%%)", pan * 100));
				if (rcIsMode1) {
					textViewElevator.setText(String.format("Elev: %.0f%%", tilt * 100));
				} else {
					textViewThrottle.setText(String.format("(Thrt: %.0f%%)",tilt * 100));
				}
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
				textViewAileron.setText(String.format("Ail: %.0f%%", pan * 100));
				if (rcIsMode1) {
					rcOutput.setRcChannel(RcOutput.TROTTLE, tilt);
					textViewThrottle.setText(String.format("Thrt: %.0f%%", tilt * 100));
				} else {
					rcOutput.setRcChannel(RcOutput.ELEVATOR, tilt);
					textViewElevator.setText(String.format("Elev: %.0f%%", tilt * 100));
				}
			} else {
				textViewAileron.setText(String.format("(Ail: %.0f%%)", pan * 100));
				if (rcIsMode1) {
					textViewThrottle.setText(String.format("(Thrt: %.0f%%)",tilt * 100));
				} else {
					textViewElevator.setText(String.format("Elev: %.0f%%", tilt * 100));
				}
			}
		}
	};

}
