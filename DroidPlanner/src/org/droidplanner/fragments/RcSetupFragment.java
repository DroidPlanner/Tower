package org.droidplanner.fragments;

import org.droidplanner.DroidPlannerApp;
import org.droidplanner.MAVLink.MavLinkStreamRates;
import org.droidplanner.calibration.CH_CalParameters;
import org.droidplanner.calibration.CalParameters;
import org.droidplanner.calibration.RC_CalParameters;
import org.droidplanner.calibration.CalParameters.OnCalibrationEvent;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.fragments.calibration.FragmentSetupProgress;
import org.droidplanner.fragments.calibration.rc.FragmentSetupRCCompleted;
import org.droidplanner.fragments.calibration.rc.FragmentSetupRCMenu;
import org.droidplanner.fragments.calibration.rc.FragmentSetupRCMiddle;
import org.droidplanner.fragments.calibration.rc.FragmentSetupRCMinMax;
import org.droidplanner.fragments.calibration.rc.FragmentSetupRCOptions;
import org.droidplanner.widgets.FillBar.FillBar;
import org.droidplanner.widgets.RcStick.RcStick;
import android.widget.TextView;

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
public class RcSetupFragment extends Fragment implements OnDroneListener, OnCalibrationEvent {

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

	private static final String[] RCStr = { "CH 1 ", "CH 2 ", "CH 3 ", "CH 4 ", "CH 5", "CH 6", "CH 7", "CH 8" };

	private Drone drone;

	private RC_CalParameters rcParameters;
	private CH_CalParameters chParameters;
	private CalParameters currParameters;

	private FillBar 	bar1;
	private FillBar		bar2;
	private FillBar 	bar3;
	private FillBar 	bar4;
	private FillBar 	bar5;
	private FillBar 	bar6;
	private FillBar 	bar7;
	private FillBar 	bar8;
;
	private TextView	roll_pitch_text;
	private TextView	thr_yaw_text;
	private TextView	ch_5_text;
	private TextView	ch_6_text;
	private TextView	ch_7_text;
	private TextView	ch_8_text;

	private RcStick 	stickLeft;
	private RcStick 	stickRight;

	private FragmentManager fragmentManager;
	private Fragment 		setupPanel;

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
		bar1.setValue(data[0]);
		bar2.setValue(data[1]);
		bar3.setValue(data[2]);
		bar4.setValue(data[3]);
		bar5.setValue(data[4]);
		bar6.setValue(data[5]);
		bar7.setValue(data[6]);
		bar8.setValue(data[7]);

		roll_pitch_text.setText("Roll: " + Integer.toString(data[0]) + "\nPitch: " + Integer.toString(data[1]));
		thr_yaw_text.setText("Throttle: " + Integer.toString(data[2]) + "\nYaw: " + Integer.toString(data[3]));
		ch_5_text.setText("CH 5: " + Integer.toString(data[4]));
		ch_6_text.setText("CH 6: " + Integer.toString(data[5]));
		ch_7_text.setText("CH 7: " + Integer.toString(data[6]));
		ch_8_text.setText("CH 8: " + Integer.toString(data[7]));

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

		bar1 = (FillBar) view.findViewById(R.id.fillBar1);
		bar2 = (FillBar) view.findViewById(R.id.fillBar2);
		bar3 = (FillBar) view.findViewById(R.id.fillBar3);
		bar4 = (FillBar) view.findViewById(R.id.fillBar4);
		bar5   = (FillBar) view.findViewById(R.id.fillBar5);
		bar6   = (FillBar) view.findViewById(R.id.fillBar6);
		bar7   = (FillBar) view.findViewById(R.id.fillBar7);
		bar8   = (FillBar) view.findViewById(R.id.fillBar8);
		bar2.invertBar(true);

		roll_pitch_text    = (TextView) view.findViewById(R.id.roll_pitch_text);
		thr_yaw_text   = (TextView) view.findViewById(R.id.thr_yaw_text);
		ch_5_text   = (TextView) view.findViewById(R.id.ch_5_text);
		ch_6_text   = (TextView) view.findViewById(R.id.ch_6_text);
		ch_7_text   = (TextView) view.findViewById(R.id.ch_7_text);
		ch_8_text   = (TextView) view.findViewById(R.id.ch_8_text);


		bar1.setup(RC_MAX, RC_MIN);
		bar2.setup(RC_MAX, RC_MIN);
		bar3.setup(RC_MAX, RC_MIN);
		bar4.setup(RC_MAX, RC_MIN);
		bar5.setup(RC_MAX, RC_MIN);
		bar6.setup(RC_MAX, RC_MIN);
		bar7.setup(RC_MAX, RC_MIN);
		bar8.setup(RC_MAX, RC_MIN);
	}

	private void setFillBarShowMinMax(boolean b) {
		bar1.setShowMinMax(b);
		bar2.setShowMinMax(b);
		bar3.setShowMinMax(b);
		bar4.setShowMinMax(b);
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
                : new FragmentSetupProgress();

		return setupPanel;
	}

	private Fragment getRCCalibrationPanel() {
		if (!rcParameters.isParameterDownloaded()) {
			setupPanel = new FragmentSetupProgress();
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
			setupPanel = new FragmentSetupProgress();
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

		cMin[0] = bar1.getMinValue();
		cMin[1] = bar2.getMinValue();
		cMin[2] = bar3.getMinValue();
		cMin[3] = bar4.getMinValue();
		cMin[4] = bar5.getMinValue();
		cMin[5] = bar6.getMinValue();
		cMin[6] = bar7.getMinValue();
		cMin[7] = bar8.getMinValue();

		cMax[0] = bar1.getMaxValue();
		cMax[1] = bar2.getMaxValue();
		cMax[2] = bar3.getMaxValue();
		cMax[3] = bar4.getMaxValue();
		cMax[4] = bar5.getMaxValue();
		cMax[5] = bar6.getMaxValue();
		cMax[6] = bar7.getMaxValue();
		cMax[7] = bar8.getMaxValue();

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
			((FragmentSetupProgress) setupPanel).updateProgress(index, count, title);
		}
	}
}
