package com.droidplanner.fragments;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.helpers.RcOutput;
import com.droidplanner.widgets.joystick.JoystickMovedListener;
import com.droidplanner.widgets.joystick.JoystickView;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RCFragment extends Fragment {
	
	private JoystickView joystickL, joystickR;
	private TextView textViewLPan, textViewLTilt, textViewRPan, textViewRTilt;
	
	private RcOutput rcOutput;
	private boolean rcActivated = false;
	private double lLastPan = 0, lLastTilt = 0, rLastPan = 0, rLastTilt = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.rc_fragment, container, false);
		
		textViewLPan = (TextView)view.findViewById(R.id.textViewRCJoyLPan);
		textViewLPan.setText("(Rudd: 0%)");
		textViewLTilt = (TextView)view.findViewById(R.id.textViewRCJoyLTilt);
		textViewLTilt.setText("(Thrt: 0%)");
		textViewRPan = (TextView)view.findViewById(R.id.textViewRCJoyRPan);
		textViewRPan.setText("(Ail: 0%)");
		textViewRTilt = (TextView)view.findViewById(R.id.textViewRCJoyRTilt);
		textViewRTilt.setText("(Elev: 0%)");
		
		joystickL = (JoystickView)view.findViewById(R.id.joystickViewL);
		joystickR = (JoystickView)view.findViewById(R.id.joystickViewR);
		
		joystickL.setAxisAutoReturnToCenter(false, true);
		joystickL.setOnJostickMovedListener(lJoystick);
		joystickR.setOnJostickMovedListener(rJoystick);
		
		DroidPlannerApp app = (DroidPlannerApp)getActivity().getApplication();
		rcOutput = new RcOutput(app.MAVClient,app);
		
		return view;
	}

	public boolean isRcOverrideActive() {
		return rcActivated;
	}
	
	public void setRcOverrideActive(boolean active) {
		if (active) {
			enableRCOverride();
		} else {
			disableRCOverride();
		}
	}
	
	private void enableRCOverride() {
		if (rcOutput != null) {
			rcOutput.enableRcOverride();
			rcActivated = true;
			lJoystick.OnMoved(lLastPan, lLastTilt);
			rJoystick.OnMoved(rLastPan, rLastTilt);
		}
	}
	
	private void disableRCOverride() {
		rcOutput.disableRcOverride();
		rcActivated = false;
		lJoystick.OnMoved(lLastPan, lLastTilt);
		rJoystick.OnMoved(rLastPan, rLastTilt);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// TODO Auto-generated method stub
	}

	@Override
	public void onDetach() {
		super.onDetach();
		// TODO 
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
				textViewLPan.setText(String.format("Rudd: %.0f%%", pan *100));
				textViewLTilt.setText(String.format("Thrt: %.0f%%", tilt *100));
			} else {
				textViewLPan.setText(String.format("(Rudd: %.0f%%)", pan *100));
				textViewLTilt.setText(String.format("(Thrt: %.0f%%)", tilt *100));
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
				textViewRPan.setText(String.format("Ail: %.0f%%", pan *100));
				textViewRTilt.setText(String.format("Elev: %.0f%%", tilt *100));
			} else {
				textViewRPan.setText(String.format("(Ail: %.0f%%)", pan *100));
				textViewRTilt.setText(String.format("(Elev: %.0f%%)", tilt *100));
			}
		}
	};

}
