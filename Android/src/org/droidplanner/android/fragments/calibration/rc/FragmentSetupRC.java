package org.droidplanner.android.fragments.calibration.rc;

import org.droidplanner.R;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.android.fragments.SetupRadioFragment;
import org.droidplanner.android.fragments.calibration.FragmentSetupNext;
import org.droidplanner.android.fragments.calibration.FragmentSetupProgress;
import org.droidplanner.android.fragments.calibration.FragmentSetupStart;
import org.droidplanner.android.fragments.calibration.FragmentSetupSummary;
import org.droidplanner.android.fragments.calibration.SetupSidePanel;
import org.droidplanner.android.fragments.helpers.SuperSetupMainPanel;
import org.droidplanner.android.helpers.calibration.CalParameters;
import org.droidplanner.android.helpers.calibration.RC_CalParameters;
import org.droidplanner.android.widgets.FillBar.FillBar;
import org.droidplanner.android.widgets.RcStick.RcStick;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class FragmentSetupRC extends SuperSetupMainPanel {

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

	private int calibrationStep = 0;

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
	protected CalParameters getParameterHandler() {
		return new RC_CalParameters(drone);
	}

	@Override
	public SetupSidePanel getSidePanel() {
		return getDefaultPanel();
	}

	@Override
	protected SetupSidePanel getDefaultPanel() {
		calibrationStep = 0;
		// setFillBarShowMinMax(false);
		sidePanel = new FragmentSetupStart();
		return sidePanel;
	}

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_rc_main;
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case RC_IN:
			updatePanelInfo();
			break;
		case RC_OUT:
		default:
			break;
		}
		super.onDroneEvent(event, drone);
	}

	@Override
	public void onReadCalibration(CalParameters calParameters) {
		doCalibrationStep(0);// show progress sidepanel
	}

	@Override
	public void setupLocalViews(View view) {
		stickLeft = (RcStick) view.findViewById(R.id.stickLeft);
		stickRight = (RcStick) view.findViewById(R.id.stickRight);

		bar1 = (FillBar) view.findViewById(R.id.fillBar_roll);
		bar2 = (FillBar) view.findViewById(R.id.fillBar_pitch);
		bar3 = (FillBar) view.findViewById(R.id.fillBar_throttle);
		bar4 = (FillBar) view.findViewById(R.id.fillBar_yaw);
		bar5 = (FillBar) view.findViewById(R.id.fillBar_ch_5);
		bar6 = (FillBar) view.findViewById(R.id.fillBar_ch_6);
		bar7 = (FillBar) view.findViewById(R.id.fillBar_ch_7);
		bar8 = (FillBar) view.findViewById(R.id.fillBar_ch_8);
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
		case 1:
		case 2:
			sidePanel = getNextPanel();
			break;
		case 3: // Upload calibration data
			updateCalibrationData();
			break;
		case 0:
		default:
			sidePanel = getInitialPanel();
		}
	}

	private SetupSidePanel getCompletedPanel() {
		calibrationStep = 0;
		Bundle args = new Bundle();
		args.putString(FragmentSetupSummary.EXTRA_TEXT_SUMMARY,
				getCalibrationStr());
		sidePanel = ((SetupRadioFragment) getParentFragment())
				.changeSidePanel(new FragmentSetupSummary());
		if (sidePanel != null) {
			sidePanel.setArguments(args);
		}
		return sidePanel;
	}

	private SetupSidePanel getNextPanel() {
		int textId = 0, descId = 0;

		switch (calibrationStep) {
		case 0:
			if (!parameters.isParameterDownloaded()
					&& drone.MavClient.isConnected()) {
				getProgressPanel(true);
				parameters.getCalibrationParameters(drone);
				return sidePanel;
			}
			setFillBarShowMinMax(true);
			textId = R.string.setup_radio_title_minmax;
			descId = R.string.setup_radio_desc_minmax;
			break;
		case 1:
			textId = R.string.setup_radio_title_middle;
			descId = R.string.setup_radio_desc_middle;
			break;
		case 3:
			sidePanel = getCompletedPanel();
			return sidePanel;
		}
		calibrationStep++;

		sidePanel = new FragmentSetupNext();
		sidePanel.updateTitle(textId);
		sidePanel.updateDescription(descId);

		return ((SetupRadioFragment) getParentFragment())
				.changeSidePanel(sidePanel);
	}

	private SetupSidePanel getProgressPanel(boolean isSending) {
		sidePanel = new FragmentSetupProgress();
		if (isSending) {
			sidePanel.updateTitle(R.string.progress_title_uploading);
			sidePanel.updateDescription(R.string.progress_desc_uploading);
		} else {
			sidePanel.updateTitle(R.string.progress_title_downloading);
			sidePanel.updateDescription(R.string.progress_desc_downloading);
		}

		return ((SetupRadioFragment) getParentFragment())
				.changeSidePanel(sidePanel);
	}

	@Override
	protected void updatePanelInfo() {
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
			parameters.setParamValueByName("RC" + String.valueOf(i + 1)
					+ "_MIN", cMin[i]);
			parameters.setParamValueByName("RC" + String.valueOf(i + 1)
					+ "_MAX", cMax[i]);
			parameters.setParamValueByName("RC" + String.valueOf(i + 1)
					+ "_TRIM", cMid[i]);
		}

		setFillBarShowMinMax(false);
		getProgressPanel(true);
		parameters.sendCalibrationParameters();
	}

}
