package com.droidplanner.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.MAVLink.MavLinkStreamRates;
import com.droidplanner.calibration.CH_CalParameters;
import com.droidplanner.calibration.CalParameters;
import com.droidplanner.calibration.RC_CalParameters;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.fragments.calibration.rc.FragmentSetupRCCompleted;
import com.droidplanner.fragments.calibration.rc.FragmentSetupRCMenu;
import com.droidplanner.fragments.calibration.rc.FragmentSetupRCMiddle;
import com.droidplanner.fragments.calibration.rc.FragmentSetupRCMinMax;
import com.droidplanner.fragments.calibration.rc.FragmentSetupRCPanel;
import com.droidplanner.fragments.calibration.rc.FragmentSetupRCProgress;
import com.droidplanner.widgets.FillBar.FillBarMinMaxL;
import com.droidplanner.widgets.FillBar.FillBarMinMaxR;
import com.droidplanner.widgets.FillBar.FillBarMinMaxText;
import com.droidplanner.widgets.RcStick.RcStick;

public class RcSetupFragment extends Fragment implements OnDroneListner {

	private static final int RC_MIN = 900;
	private static final int RC_MAX = 2100;
	// Extreme RC update rate in this screen
	private static final int RC_MSG_RATE = 50;

	private Drone drone;

	private RC_CalParameters rcParameters;
	private CH_CalParameters chParameters;
	private CalParameters currParameters = null;

	private FillBarMinMaxR barELE;
	private FillBarMinMaxL barTHR;
	private FillBarMinMaxText barYAW;
	private FillBarMinMaxText barAIL;
	private FillBarMinMaxText bar5;
	private FillBarMinMaxText bar6;
	private FillBarMinMaxText bar7;
	private FillBarMinMaxText bar8;

	private RcStick stickLeft;
	private RcStick stickRight;

	private FragmentManager fragmentManager;
	private FragmentSetupRCPanel setupPanel;

	int data[];


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rcParameters = new RC_CalParameters(drone);
		chParameters = new CH_CalParameters(drone);
		fragmentManager = getFragmentManager();
		setupPanel = (FragmentSetupRCPanel) fragmentManager
				.findFragmentById(R.id.fragment_setup_rc_panel);		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_rc, container,
				false);
		setupLocalViews(view);
		setupFragmentPanel(view);

		return view;
	}

	private void setupFragmentPanel(View view) {
		if (setupPanel == null) {
			setupPanel = new FragmentSetupRCMenu();
			((FragmentSetupRCMenu) setupPanel).rcSetupFragment = this;
		}
		fragmentManager.beginTransaction()
		.add(R.id.fragment_setup_rc_panel, setupPanel).commit();
		/*
		 * else { cancel(); }
		 */
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		this.drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		drone.events.addDroneListener(this);
		setupDataStreamingForRcSetup();
		super.onStart();
	}

	@Override
	public void onStop() {
		drone.events.removeDroneListener(this);
		MavLinkStreamRates
				.setupStreamRatesFromPref((DroidPlannerApp) getActivity()
						.getApplication());
		super.onStop();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(setupPanel!=null){
			setupPanel.rcSetupFragment = this;
		}
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case RC_IN:
			onNewInputRcData();
			break;
		case RC_OUT:
			break;
		case PARAMETER:
			if (currParameters != null)
				currParameters.processReceivedParam();
			break;
		default:
			break;
		}
	}

	private void setupDataStreamingForRcSetup() {
		MavLinkStreamRates.setupStreamRates(drone.MavClient, 1, 0, 1, 1, 1,
				RC_MSG_RATE, 0, 0);
	}

	private void onNewInputRcData() {
		data = drone.RC.in;
		barAIL.setValue(data[0]);
		barELE.setValue(data[1]);
		barTHR.setValue(data[2]);
		barYAW.setValue(data[3]);
		bar5.setValue(data[4]);
		bar6.setValue(data[5]);
		bar7.setValue(data[6]);
		bar8.setValue(data[7]);

		float x, y;
		x = (data[3] - RC_MIN) / ((float) (RC_MAX - RC_MIN)) * 2 - 1;
		y = (data[2] - RC_MIN) / ((float) (RC_MAX - RC_MIN)) * 2 - 1;
		stickLeft.setPosition(x, y);

		x = (data[0] - RC_MIN) / ((float) (RC_MAX - RC_MIN)) * 2 - 1;
		y = (data[1] - RC_MIN) / ((float) (RC_MAX - RC_MIN)) * 2 - 1;
		stickRight.setPosition(x, -y);
	}

	private void setupLocalViews(View view) {
		stickLeft = (RcStick) view.findViewById(R.id.stickLeft);
		stickRight = (RcStick) view.findViewById(R.id.stickRight);
		barTHR = (FillBarMinMaxL) view.findViewById(R.id.fillBarTHR);
		barELE = (FillBarMinMaxR) view.findViewById(R.id.fillBarELE);
		barYAW = (FillBarMinMaxText) view.findViewById(R.id.fillBarYAW);
		barAIL = (FillBarMinMaxText) view.findViewById(R.id.fillBarAIL);
		bar5 = (FillBarMinMaxText) view.findViewById(R.id.fillBar5);
		bar6 = (FillBarMinMaxText) view.findViewById(R.id.fillBar6);
		bar7 = (FillBarMinMaxText) view.findViewById(R.id.fillBar7);
		bar8 = (FillBarMinMaxText) view.findViewById(R.id.fillBar8);

		barAIL.setup("AILERON", RC_MAX, RC_MIN);
		barELE.setup("ELEVATOR", RC_MAX, RC_MIN);
		barTHR.setup("THROTTLE", RC_MAX, RC_MIN);
		barYAW.setup("RUDDER", RC_MAX, RC_MIN);
		bar5.setup("CH 5", RC_MAX, RC_MIN);
		bar6.setup("CH 6", RC_MAX, RC_MIN);
		bar7.setup("CH 7", RC_MAX, RC_MIN);
		bar8.setup("CH 8", RC_MAX, RC_MIN);
	}

	private void setFillBarShowMinMax(boolean b) {
		barAIL.setShowMinMax(b);
		barELE.setShowMinMax(b);
		barTHR.setShowMinMax(b);
		barYAW.setShowMinMax(b);
		bar5.setShowMinMax(b);
		bar6.setShowMinMax(b);
		bar7.setShowMinMax(b);
		bar8.setShowMinMax(b);
	}

	public void changeSetupPanel(int step) {
		switch (step) {
		case 0:
			setupPanel = getRCMenuPanel();
			break;
		case 1:
			currParameters = rcParameters;
			setupPanel = getRCCalibrationPanel();
			break;
		case 2:
			setupPanel = new FragmentSetupRCMiddle();
			((FragmentSetupRCMiddle) setupPanel).rcSetupFragment = this;
			break;
		case 3:
			setupPanel = new FragmentSetupRCCompleted();
			((FragmentSetupRCCompleted) setupPanel).rcSetupFragment = this;
//			((FragmentSetupRCCompleted) setupPanel)
//					.setText(getCalibrationStr());
			break;
		case 4:
			currParameters = chParameters;
//			setupPanel = getCHCalibrationPanel();
			break;
		}
		fragmentManager.beginTransaction()
				.replace(R.id.fragment_setup_rc_panel, setupPanel).commit();
	}

	private FragmentSetupRCPanel getRCMenuPanel() {
		if(currParameters != null){
			setupPanel = new FragmentSetupRCProgress();
			((FragmentSetupRCProgress) setupPanel).rcSetupFragment = this;			
		}
		else{
			setupPanel = new FragmentSetupRCMenu();
			((FragmentSetupRCMenu) setupPanel).rcSetupFragment = this;			
		}
		return setupPanel;
	}

	private FragmentSetupRCPanel getRCCalibrationPanel() {
		if (!rcParameters.isParameterDownloaded()) {
			setupPanel = new FragmentSetupRCProgress();
			((FragmentSetupRCProgress) setupPanel).rcSetupFragment = this;
			rcParameters.getCalibrationParameters(drone);
		} else {
			setFillBarShowMinMax(true);
			setupPanel = new FragmentSetupRCMinMax();
			((FragmentSetupRCMinMax) setupPanel).rcSetupFragment = this;
		}
		// TODO Auto-generated method stub
		return setupPanel;
	}

	public void cancel() {
		setFillBarShowMinMax(false);
		currParameters = null;
		changeSetupPanel(0);
	}
}
