package org.droidplanner.fragments.calibration.rc;

import org.droidplanner.calibration.CalParameters;
import org.droidplanner.calibration.CalParameters.OnCalibrationEvent;
import org.droidplanner.calibration.RC_CalParameters;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListner;
import org.droidplanner.fragments.SetupRadioFragment;
import org.droidplanner.fragments.calibration.FragmentSetupProgress;
import org.droidplanner.fragments.calibration.SetupMainPanel;
import org.droidplanner.fragments.calibration.SetupSidePanel;
import org.droidplanner.widgets.FillBar.FillBar;
import org.droidplanner.widgets.RcStick.RcStick;

import org.droidplanner.R;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class FragmentSetupRC extends SetupMainPanel implements OnDroneListner,
		OnCalibrationEvent {

	/**
	 * Minimum threshold for the RC value.
	 */
	private static final int RC_MIN = 900;

	/**
	 * Maximum threshold for the RC value.
	 */
	private static final int RC_MAX = 2100;

	private static final String[] RCStr = { "CH 1 ", "CH 2 ", "CH 3 ", "CH 4 ",
			"CH 5", "CH 6", "CH 7", "CH 8" };

	private Drone drone;

	private RC_CalParameters rcParameters;

	private FillBar bar1;
	private FillBar bar2;
	private FillBar bar3;
	private FillBar bar4;
	private FillBar bar5;
	private FillBar bar6;
	private FillBar bar7;
	private FillBar bar8;;
	private TextView roll_pitch_text;
	private TextView thr_yaw_text;
	private TextView ch_5_text;
	private TextView ch_6_text;
	private TextView ch_7_text;
	private TextView ch_8_text;

	private RcStick stickLeft;
	private RcStick stickRight;

	private int data[] = new int[8];
	private int cMin[] = new int[8];
	private int cMid[] = new int[8];
	private int cMax[] = new int[8];

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_rc_main;
	}

	@Override
	public SetupSidePanel getSidePanel() {
		return new FragmentSetupRCCalibrate();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		this.drone = parentActivity.drone;
		rcParameters = new RC_CalParameters(drone);
	}

	@Override
	public void onStart() {
		super.onStart();
		drone.events.addDroneListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		drone.events.removeDroneListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (rcParameters != null) {
			rcParameters.setOnCalibrationEventListener(this);
		}

		Log.d("CAL", "RC Setup");
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case RC_IN:
			onNewInputRcData();
			break;

		case PARAMETER:
			if (rcParameters != null) {
				rcParameters.processReceivedParam();
			}
			break;

		case RC_OUT:
		default:
			break;
		}
	}

	@Override
	public void onReadCalibration(CalParameters calParameters) {
		doCalibrationStep(0);// show progress sidepanel
	}

	@Override
	public void onSentCalibration(CalParameters calParameters) {
		doCalibrationStep(-1);
	}

	@Override
	public void onCalibrationData(CalParameters calParameters, int index,
			int count, boolean isSending) {
		if (sidePanel != null && rcParameters != null) {
			String title;
			if (isSending) {
					title = "Uploading RC calibration data";
			} else {
					title = "Downloading RC calibration data";
			}

			((FragmentSetupProgress) sidePanel).updateProgress(index, count,
					title);
		}
	}

	@Override
	public void setupLocalViews(View view) {
		stickLeft = (RcStick) view.findViewById(R.id.stickLeft);
		stickRight = (RcStick) view.findViewById(R.id.stickRight);

		bar1 = (FillBar) view.findViewById(R.id.fillBar1);
		bar2 = (FillBar) view.findViewById(R.id.fillBar2);
		bar3 = (FillBar) view.findViewById(R.id.fillBar3);
		bar4 = (FillBar) view.findViewById(R.id.fillBar4);
		bar5 = (FillBar) view.findViewById(R.id.fillBar5);
		bar6 = (FillBar) view.findViewById(R.id.fillBar6);
		bar7 = (FillBar) view.findViewById(R.id.fillBar7);
		bar8 = (FillBar) view.findViewById(R.id.fillBar8);
		bar2.invertBar(true);

		roll_pitch_text = (TextView) view.findViewById(R.id.roll_pitch_text);
		thr_yaw_text = (TextView) view.findViewById(R.id.thr_yaw_text);
		ch_5_text = (TextView) view.findViewById(R.id.ch_5_text);
		ch_6_text = (TextView) view.findViewById(R.id.ch_6_text);
		ch_7_text = (TextView) view.findViewById(R.id.ch_7_text);
		ch_8_text = (TextView) view.findViewById(R.id.ch_8_text);

		bar1.setup(RC_MAX, RC_MIN);
		bar2.setup(RC_MAX, RC_MIN);
		bar3.setup(RC_MAX, RC_MIN);
		bar4.setup(RC_MAX, RC_MIN);
		bar5.setup(RC_MAX, RC_MIN);
		bar6.setup(RC_MAX, RC_MIN);
		bar7.setup(RC_MAX, RC_MIN);
		bar8.setup(RC_MAX, RC_MIN);
	}

	@Override
	public void doCalibrationStep(int step) {
		switch (step) {
		case 0: // Get MinMax
			sidePanel = getMinMaxPanel();
			break;
		case 1: // Get Mid
			sidePanel = getMidPanel();
			break;
		case 2: // Get MinMax
			sidePanel = getCompletedPanel();
			break;
		case 3: // Upload calibration data
			updateCalibrationData();
			break;
		default:
			sidePanel = getInitialPanel();
		}
	}

	private SetupSidePanel getInitialPanel() {
		setFillBarShowMinMax(false);
		sidePanel = ((SetupRadioFragment) getParentFragment())
				.changeSidePanel(new FragmentSetupRCCalibrate());
		return sidePanel;
	}

	private SetupSidePanel getCompletedPanel() {
		Bundle args = new Bundle();
		args.putString(FragmentSetupRCCompleted.EXTRA_TEXT_SUMMARY,
				getCalibrationStr());
		sidePanel = ((SetupRadioFragment) getParentFragment())
				.changeSidePanel(new FragmentSetupRCCompleted());
		if (sidePanel != null) {
			sidePanel.setArguments(args);
		}
		return sidePanel;
	}

	private SetupSidePanel getMidPanel() {
		return ((SetupRadioFragment) getParentFragment())
				.changeSidePanel(new FragmentSetupRCMiddle());
	}

	private SetupSidePanel getProgressPanel() {
		return ((SetupRadioFragment) getParentFragment())
				.changeSidePanel(new FragmentSetupProgress());
	}

	private SetupSidePanel getMinMaxPanel() {
		if (!rcParameters.isParameterDownloaded()) {
			getProgressPanel();
			sidePanel.updateTitle(R.string.progress_title_downloading);
			sidePanel.updateDescription(R.string.progress_desc_downloading);
			rcParameters.getCalibrationParameters(drone);
		} else {
			setFillBarShowMinMax(true);
			((SetupRadioFragment) getParentFragment())
					.changeSidePanel(new FragmentSetupRCMinMax());
		}
		return sidePanel;
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

		roll_pitch_text.setText("Roll: " + Integer.toString(data[0])
				+ "\nPitch: " + Integer.toString(data[1]));
		thr_yaw_text.setText("Throttle: " + Integer.toString(data[2])
				+ "\nYaw: " + Integer.toString(data[3]));
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
		for (int i = 0; i < 8; i++) {
			rcParameters.setParamValueByName("RC" + String.valueOf(i + 1)
					+ "_MIN", cMin[i]);
			rcParameters.setParamValueByName("RC" + String.valueOf(i + 1)
					+ "_MAX", cMax[i]);
			rcParameters.setParamValueByName("RC" + String.valueOf(i + 1)
					+ "_TRIM", cMid[i]);
		}

		setFillBarShowMinMax(false);
		getProgressPanel();
		sidePanel.updateTitle(R.string.progress_title_uploading);
		sidePanel.updateDescription(R.string.progress_desc_uploading);

		rcParameters.sendCalibrationParameters();
	}

}
