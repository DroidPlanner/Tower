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
import com.droidplanner.calibration.RC_CalParameters;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.fragments.calibration.FragmentSetupRCCompleted;
import com.droidplanner.fragments.calibration.FragmentSetupRCMenu;
import com.droidplanner.fragments.calibration.FragmentSetupRCMiddle;
import com.droidplanner.fragments.calibration.FragmentSetupRCMinMax;
import com.droidplanner.fragments.calibration.FragmentSetupRCOptions;
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
	private FragmentManager fragmentManager;
	private RC_CalParameters rcParameters;

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

	private Fragment setupPanel;

	private int[] data;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentManager = getFragmentManager();
		setupPanel = fragmentManager.findFragmentById(R.id.fragment_setup_rc);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		View view = inflater.inflate(R.layout.fragment_setup_rc, container,
				false);

		Fragment defPanel = fragmentManager
				.findFragmentById(R.id.fragment_setup_rc);
		if (defPanel == null) {
			defPanel = new FragmentSetupRCMenu();
			((FragmentSetupRCMenu) defPanel).rcSetupFragment = this;

			fragmentManager.beginTransaction()
					.add(R.id.fragment_setup_rc, defPanel).commit();

		} else {
			cancel();
		}

		setupLocalViews(view);
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		drone.events.addDroneListener(this);
		rcParameters = new RC_CalParameters(drone);
		setupDataStreamingForRcSetup();
	}

	@Override
	public void onStop() {
		super.onStop();
		drone.events.removeDroneListener(this);
		MavLinkStreamRates
				.setupStreamRatesFromPref((DroidPlannerApp) getActivity()
						.getApplication());
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

	private void setupDataStreamingForRcSetup() {
		MavLinkStreamRates.setupStreamRates(drone.MavClient, 1, 0, 1, 1, 1,
				RC_MSG_RATE, 0, 0);
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
			rcParameters.processReceivedParam();
			break;
		default:
			break;
		}

	}

	public void onNewInputRcData() {
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


	public void changeSetupPanel(int step) {
		switch (step) {
		case 0:
			setupPanel = new FragmentSetupRCMenu();
			((FragmentSetupRCMenu) setupPanel).rcSetupFragment = this;
			break;
		case 1:
			rcParameters.getCaliberationParameters(drone);
			setFillBarShowMinMax(true);
			setupPanel = new FragmentSetupRCMinMax();
			((FragmentSetupRCMinMax) setupPanel).rcSetupFragment = this;
			break;
		case 2:
			setupPanel = new FragmentSetupRCMiddle();
			((FragmentSetupRCMiddle) setupPanel).rcSetupFragment = this;
			break;
		case 3:
			readTrimData();
			setupPanel = new FragmentSetupRCCompleted();
			((FragmentSetupRCCompleted) setupPanel).rcSetupFragment = this;
			((FragmentSetupRCCompleted) setupPanel)
					.setText(getCalibrationStr());

			break;
		case 4:
			setupPanel = new FragmentSetupRCOptions();
			((FragmentSetupRCOptions) setupPanel).rcSetupFragment = this;
			break;
		}
		fragmentManager.beginTransaction()
				.replace(R.id.fragment_setup_rc, setupPanel).commit();
	}

	private void readTrimData() {
		// TODO Auto-generated method stub

	}

	public void readMinMaxData() {

	}

	private String getCalibrationStr() {
		int[] cMin = new int[8], cMid = new int[8], cMax = new int[8];
		String txt = "RC #\tMIN\t\tMID\t\tMAX";

		cMin[0] = barAIL.getMin();
		cMin[1] = barELE.getMin();
		cMin[2] = barTHR.getMin();
		cMin[3] = barYAW.getMin();
		cMin[4] = bar5.getMin();
		cMin[5] = bar6.getMin();
		cMin[6] = bar7.getMin();
		cMin[7] = bar8.getMin();

		cMax[0] = barAIL.getMax();
		cMax[1] = barELE.getMax();
		cMax[2] = barTHR.getMax();
		cMax[3] = barYAW.getMax();
		cMax[4] = bar5.getMax();
		cMax[5] = bar6.getMax();
		cMax[6] = bar7.getMax();
		cMax[7] = bar8.getMax();

		if(data!=null)
			cMid = data;

		for (int i = 0; i < 8; i++) {
			txt += "\nRC " + String.valueOf(i) + "\t";
			txt += String.valueOf(cMin[i]) + "\t\t";
			txt += String.valueOf(cMid[i]) + "\t\t";
			txt += String.valueOf(cMax[i]);
		}
		return txt;
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

	public void cancel() {
		// TODO Auto-generated method stub
		changeSetupPanel(0);
		setFillBarShowMinMax(false);
	}

	public void updateCalibrationData() {
		// TODO Auto-generated method stub
		changeSetupPanel(0);
		setFillBarShowMinMax(false);

	}

	public void updateFailsafaData() {
		// TODO Auto-generated method stub
		changeSetupPanel(0);
	}

	public void updateRCOptionsData() {
		// TODO Auto-generated method stub
		changeSetupPanel(0);
	}

	public void done() {
		// TODO Auto-generated method stub
		changeSetupPanel(0);
	}

}
