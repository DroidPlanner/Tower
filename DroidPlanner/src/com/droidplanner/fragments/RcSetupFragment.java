package com.droidplanner.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.MAVLink.MavLinkStreamRates;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.OnRcDataChangedListner;
import com.droidplanner.widgets.FillBar.FillBarWithText;
import com.droidplanner.widgets.RcStick.RcStick;

public class RcSetupFragment extends Fragment implements OnRcDataChangedListner {
	private static final int RC_MIN = 1000;
	private static final int RC_MAX = 2000;

	// Extreme RC update rate in this screen
	private static final int RC_MSG_RATE = 50;

	private Drone drone;
	private TextView textViewThrottle, textViewYaw, textViewRoll, textViewPitch;

	private FillBarWithText bar5;
	private FillBarWithText bar6;
	private FillBarWithText bar7;
	private FillBarWithText bar8;

	private RcStick stickLeft;

	private RcStick stickRight;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		View view = inflater.inflate(R.layout.fragment_setup_rc, container,
				false);
		setupLocalViews(view);

		drone.RC.setListner(this);
		return view;
	}

	private void setupLocalViews(View view) {
		stickLeft = (RcStick) view.findViewById(R.id.stickLeft);
		stickRight = (RcStick) view.findViewById(R.id.stickRight);
		bar5 = (FillBarWithText) view.findViewById(R.id.fillBar5);
		bar6 = (FillBarWithText) view.findViewById(R.id.fillBar6);
		bar7 = (FillBarWithText) view.findViewById(R.id.fillBar7);
		bar8 = (FillBarWithText) view.findViewById(R.id.fillBar8);

		bar5.setup("CH 5", RC_MAX, RC_MIN);
		bar6.setup("CH 6", RC_MAX, RC_MIN);
		bar7.setup("CH 7", RC_MAX, RC_MIN);
		bar8.setup("CH 8", RC_MAX, RC_MIN);

		textViewRoll 		= (TextView) view.findViewById(R.id.RCRollPWM);
		textViewPitch 		= (TextView) view.findViewById(R.id.RCPitchPWM);
		textViewThrottle 	= (TextView) view.findViewById(R.id.RCThrottlePWM);
		textViewYaw 		= (TextView) view.findViewById(R.id.RCYawPWM);
	}

	@Override
	public void onStart() {
		super.onStart();
		setupDataStreamingForRcSetup();
	}

	private void setupDataStreamingForRcSetup() {
		MavLinkStreamRates.setupStreamRates(drone.MavClient, 1, 0, 1, 1, 1,
				RC_MSG_RATE, 0, 0);
	}

	@Override
	public void onStop() {
		super.onStop();
		MavLinkStreamRates
				.setupStreamRatesFromPref((DroidPlannerApp) getActivity()
						.getApplication());
	}

	@Override
	public void onNewInputRcData() {
		int[] data = drone.RC.in;
		bar5.setValue(data[4]);
		bar6.setValue(data[5]);
		bar7.setValue(data[6]);
		bar8.setValue(data[7]);

		float x,y;
		x = (data[3] - RC_MIN) / ((float) (RC_MAX - RC_MIN))*2-1;
		y = (data[2] - RC_MIN) / ((float) (RC_MAX - RC_MIN))*2-1;
		stickLeft.setPosition(x, y);

		x = (data[0] - RC_MIN) / ((float) (RC_MAX - RC_MIN))*2-1;
		y = (data[1] - RC_MIN) / ((float) (RC_MAX - RC_MIN))*2-1;
		stickRight.setPosition(x, -y);

		textViewRoll.setText(Integer.toString(data[0]));
		textViewPitch.setText(Integer.toString(data[1]));
		textViewThrottle.setText(Integer.toString(data[2]));
		textViewYaw.setText(Integer.toString(data[3]));
	}

	@Override
	public void onNewOutputRcData() {
		// TODO Auto-generated method stub
	}

}
