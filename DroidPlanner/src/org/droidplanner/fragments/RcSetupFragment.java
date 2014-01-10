package org.droidplanner.fragments;

import org.droidplanner.DroidPlannerApp;
import org.droidplanner.MAVLink.MavLinkStreamRates;
import org.droidplanner.calibration.CH_CalParameters;
import org.droidplanner.calibration.CalParameters;
import org.droidplanner.calibration.RC_CalParameters;
import org.droidplanner.calibration.CalParameters.OnCalibrationEvent;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListner;
import org.droidplanner.fragments.calibration.rc.FragmentSetupRCCompleted;
import org.droidplanner.fragments.calibration.rc.FragmentSetupRCMenu;
import org.droidplanner.fragments.calibration.rc.FragmentSetupRCMiddle;
import org.droidplanner.fragments.calibration.rc.FragmentSetupRCMinMax;
import org.droidplanner.fragments.calibration.rc.FragmentSetupRCOptions;
import org.droidplanner.fragments.calibration.rc.FragmentSetupRCProgress;
import org.droidplanner.widgets.FillBar.FillBarMinMaxL;
import org.droidplanner.widgets.FillBar.FillBarMinMaxR;
import org.droidplanner.widgets.FillBar.FillBarMinMaxText;
import org.droidplanner.widgets.RcStick.RcStick;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.R;

/**
 * This fragment is used to calibrate the drone's radio.
 */
public class RcSetupFragment extends Fragment implements OnDroneListner, OnCalibrationEvent {

    /**
     * Minimum threshold for the RC value.
     */
	private static final int RC_MIN = 900;

    /**
     * Maximum threshold for the RC value.
     */
	private static final int RC_MAX = 2100;

	// Extreme RC update rate in this screen
	private static final int RC_MSG_RATE = 50;

	private static final String[] RCStr = { "AIL ", "ELE ", "THR ", "RUD ",
			"CH 5", "CH 6", "CH 7", "CH 8" };

	private Drone drone;

	private RC_CalParameters rcParameters;
	private CH_CalParameters chParameters;
	private CalParameters currParameters;

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
	private Fragment setupPanel;

	private int data[] = new int[8];
    private int cMin[] = new int[8];
    private int cMid[] = new int[8];
    private int cMax[] = new int[8];

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_setup_rc, container,	false);
        setupLocalViews(view);

        fragmentManager = getChildFragmentManager();
        setupPanel = fragmentManager.findFragmentById(R.id.fragment_setup_rc_panel);
        if (setupPanel == null) {
            setupPanel = new FragmentSetupRCMenu();
            fragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_setup_rc_panel, setupPanel)
                    .commit();
        } else {
            cancel();
        }

        return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

		this.drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
        rcParameters = new RC_CalParameters(drone);
        chParameters = new CH_CalParameters(drone);
	}

	@Override
	public void onStart() {
        super.onStart();
		drone.events.addDroneListener(this);
		setupDataStreamingForRcSetup();
	}

	@Override
	public void onStop() {
        super.onStop();
		drone.events.removeDroneListener(this);
		drone.streamRates.setupStreamRatesFromPref();
	}


	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (rcParameters != null) {
			rcParameters.setOnCalibrationEventListener(this);
		}

		if (chParameters != null) {
			chParameters.setOnCalibrationEventListener(this);
		}

        Log.d("CAL", "RC Setup");
        setupDataStreamingForRcSetup();
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case RC_IN:
			onNewInputRcData();
			break;

		case PARAMETER:
			if (currParameters != null) {
				currParameters.processReceivedParam();
			}
			break;

        case RC_OUT:
		default:
			break;
		}
	}

	private void setupDataStreamingForRcSetup() {
		MavLinkStreamRates.setupStreamRates(drone.MavClient, 1, 0, 1, 1, 1,	RC_MSG_RATE, 0, 0);
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
			break;

		case 3:
            Bundle args = new Bundle();
            args.putString(FragmentSetupRCCompleted.EXTRA_TEXT_SUMMARY, getCalibrationStr());
			setupPanel = new FragmentSetupRCCompleted();
            setupPanel.setArguments(args);
			break;

		case 4:
			currParameters = chParameters;
			setupPanel = getCHCalibrationPanel();
			break;
		}
		fragmentManager.beginTransaction()
				.replace(R.id.fragment_setup_rc_panel, setupPanel).commit();
	}

	private Fragment getRCMenuPanel() {
        setupPanel = currParameters == null
                ? new FragmentSetupRCMenu()
                : new FragmentSetupRCProgress();

		return setupPanel;
	}

	private Fragment getRCCalibrationPanel() {
		if (!rcParameters.isParameterDownloaded()) {
			setupPanel = new FragmentSetupRCProgress();
			rcParameters.getCalibrationParameters(drone);
		}
        else {
			setFillBarShowMinMax(true);
			setupPanel = new FragmentSetupRCMinMax();
		}
		return setupPanel;
	}

	private Fragment getCHCalibrationPanel() {
		if (!chParameters.isParameterDownloaded()) {
			setupPanel = new FragmentSetupRCProgress();
			chParameters.getCalibrationParameters(drone);
		}
        else {
			setupPanel = new FragmentSetupRCOptions();
			((FragmentSetupRCOptions) setupPanel)
                    .setOptionCH7((int) chParameters.getParamValueByName("CH7_OPT"));
			((FragmentSetupRCOptions) setupPanel)
					.setOptionCH8((int) chParameters.getParamValueByName("CH8_OPT"));
		}
		return setupPanel;
	}

	private String getCalibrationStr() {
		String txt = "RC #\t\tMIN\t\tMID\t\tMAX";

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

		if (data != null)
			cMid = data;

		for (int i = 0; i < 8; i++) {
			txt += "\n" + RCStr[i] + "\t";
			txt += "\t" + String.valueOf(cMin[i]) + "\t";
			txt += "\t" + String.valueOf(cMid[i]) + "\t";
			txt += "\t" + String.valueOf(cMax[i]);
		}

		return txt;
	}

	public void updateCalibrationData() {
		currParameters = rcParameters;

		for (int i = 0; i < 8; i++) {
			rcParameters.setParamValueByName("RC" + String.valueOf(i + 1) + "_MIN", cMin[i]);
			rcParameters.setParamValueByName("RC" + String.valueOf(i + 1) + "_MAX", cMax[i]);
			rcParameters.setParamValueByName("RC" + String.valueOf(i + 1) + "_TRIM", cMid[i]);
		}

		setFillBarShowMinMax(false);
		changeSetupPanel(0);
		rcParameters.sendCalibrationParameters();
	}

	public void updateRCOptionsData() {
		currParameters = chParameters;
		int ch7 = ((FragmentSetupRCOptions) setupPanel).getOptionCH7();
		int ch8 = ((FragmentSetupRCOptions) setupPanel).getOptionCH8();
		chParameters.setParamValueByName("CH7_OPT", ch7);
		chParameters.setParamValueByName("CH8_OPT", ch8);

		changeSetupPanel(0);
		chParameters.sendCalibrationParameters();
	}

	public void cancel() {
		setFillBarShowMinMax(false);
		currParameters = null;
		changeSetupPanel(0);
	}

	@Override
	public void onReadCalibration(CalParameters calParameters) {
		if (currParameters.equals(rcParameters)) {
			changeSetupPanel(1);
		} else if (currParameters.equals(chParameters)) {
			changeSetupPanel(4);
		}
	}

	@Override
	public void onSentCalibration(CalParameters calParameters) {
		currParameters = null;
		changeSetupPanel(0);
	}

	@Override
	public void onCalibrationData(CalParameters calParameters, int index, int count,
                                  boolean isSending) {
		if (setupPanel != null && currParameters != null) {
			String title;
			if (isSending) {
				if (currParameters.equals(rcParameters))
					title = "Uploading RC calibration data";
				else
					title = "Uploading RC options data";
			} else {
				if (currParameters.equals(rcParameters))
					title = "Downloading RC calibration data";
				else
					title = "Downloading RC options data";
			}
			((FragmentSetupRCProgress) setupPanel).updateProgress(index, count, title);
		}
	}
}
