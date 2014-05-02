package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.widgets.joystick.JoystickMovedListener;
import org.droidplanner.android.widgets.joystick.JoystickView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RCFragment extends Fragment {

	private JoystickView joystickL, joystickR;
	private TextView textViewThrottle, textViewRudder, textViewAileron,
			textViewElevator;
	private boolean rcIsMode1 = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_rc, container, false);

		textViewThrottle = (TextView) view
				.findViewById(R.id.textViewRCThrottle);
		textViewThrottle.setText("(Thrt: 0%)");

		textViewRudder = (TextView) view.findViewById(R.id.textViewRCRudder);
		textViewRudder.setText("(Rudd: 0%)");

		textViewElevator = (TextView) view
				.findViewById(R.id.textViewRCElevator);
		textViewElevator.setText("(Elev: 0%)");

		textViewAileron = (TextView) view.findViewById(R.id.textViewRCAileron);
		textViewAileron.setText("(Ail: 0%)");

		joystickL = (JoystickView) view.findViewById(R.id.joystickViewL);
		joystickR = (JoystickView) view.findViewById(R.id.joystickViewR);
		joystickL.setOnJostickMovedListener(lJoystick);
		joystickR.setOnJostickMovedListener(rJoystick);
		return view;
	}

	@Override
	public void onResume() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity()
						.getApplicationContext());
		rcIsMode1 = prefs.getString("pref_rc_mode", "MODE2").equalsIgnoreCase(
				"MODE1");
		if (rcIsMode1) {
			joystickL.setAxisAutoReturnToCenter(true, true);
			joystickR.setAxisAutoReturnToCenter(
					prefs.getBoolean("pref_rc_throttle_returntocenter", false),
					true);
			joystickL.setYAxisInverted(prefs.getBoolean(
					"pref_rc_elevator_reverse", false));
			joystickL.setXAxisInverted(prefs.getBoolean(
					"pref_rc_rudder_reverse", false));
			joystickR.setYAxisInverted(prefs.getBoolean(
					"pref_rc_throttle_reverse", false));
			joystickR.setXAxisInverted(prefs.getBoolean(
					"pref_rc_aileron_reverse", false));
		} else { // else Mode2
			joystickL.setAxisAutoReturnToCenter(
					prefs.getBoolean("pref_rc_throttle_returntocenter", false),
					true);
			joystickR.setAxisAutoReturnToCenter(true, true);
			joystickL.setYAxisInverted(prefs.getBoolean(
					"pref_rc_throttle_reverse", false));
			joystickL.setXAxisInverted(prefs.getBoolean(
					"pref_rc_rudder_reverse", false));
			joystickR.setYAxisInverted(prefs.getBoolean(
					"pref_rc_elevator_reverse", false));
			joystickR.setXAxisInverted(prefs.getBoolean(
					"pref_rc_aileron_reverse", false));
		}
		super.onResume();
	}

	@Override
	public void onStop() {
		super.onDestroyView();
	}

	public boolean physicalJoyMoved(MotionEvent ev) {
		// Tested only for wikipad controller. Probably works with most game
		// controllers.
		if ((ev.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
			lJoystick.OnMoved((double) ev.getAxisValue(MotionEvent.AXIS_X),
					(double) ev.getAxisValue(MotionEvent.AXIS_Y));
			rJoystick.OnMoved((double) ev.getAxisValue(MotionEvent.AXIS_Z),
					(double) ev.getAxisValue(MotionEvent.AXIS_RZ));
			return true;
		}
		return false;
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
			textViewRudder.setText(String.format("Rudd: %.0f%%", pan * 100));
			if (rcIsMode1) {
				textViewElevator.setText(String.format("Elev: %.0f%%",
						tilt * 100));
			} else {
				textViewThrottle.setText(String.format("Thrt: %.0f%%",
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
			textViewAileron.setText(String.format("Ail: %.0f%%", pan * 100));
			if (rcIsMode1) {
				textViewThrottle.setText(String.format("Thrt: %.0f%%",
						tilt * 100));
			} else {
				textViewElevator.setText(String.format("Elev: %.0f%%",
						tilt * 100));
			}

		}
	};

}
