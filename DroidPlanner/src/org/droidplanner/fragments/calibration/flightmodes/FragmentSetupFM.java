package org.droidplanner.fragments.calibration.flightmodes;

import org.droidplanner.calibration.FM_CalParameters;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListner;
import org.droidplanner.fragments.SetupRadioFragment;
import org.droidplanner.fragments.calibration.SetupMainPanel;
import org.droidplanner.fragments.calibration.SetupSidePanel;
import org.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;
import org.droidplanner.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class FragmentSetupFM extends SetupMainPanel implements OnDroneListner {

	private int[] pwm = { 1230, 1360, 1490, 1620, 1750 };
	private int[] flightModeValue = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13 };
	private int[] flightModeIndex = { 1, 2, 4, 8, 16, 32 };
	private double dataFM[] = new double[8];

	private String[] listPWM;
	private CheckBox[] chkbxSimple = new CheckBox[6];
	private CheckBox[] chkbxSuperSimple = new CheckBox[6];;
	private Spinner[] pwmSpinners = new Spinner[6];;
	private TextView[] textPWM = new TextView[6];
	private LinearLayout[] layoutPWM = new LinearLayout[6];

	private TextView textPWMRange, textPWMCurrent;
	private FM_CalParameters fmParameters;
	private Drone drone;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listPWM = getResources().getStringArray(R.array.FligthMode_PWM_Range);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.drone = parentActivity.drone;
		fmParameters = new FM_CalParameters(drone);
	}

	@Override
	public void onResume() {
		super.onResume();
		drone.events.addDroneListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		drone.events.removeDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case RC_IN:
			updatePWMPanels();
			break;
		case RC_OUT:
		default:
			break;
		}
	}

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_fm_main;
	}

	@Override
	public SetupSidePanel getSidePanel() {
		return new FragmentSetupFMConfiguration();
	}

	@Override
	public void setupLocalViews(View v) {
		textPWMRange = (TextView) v.findViewById(R.id.textViewPWMRange);
		textPWMCurrent = (TextView) v.findViewById(R.id.textViewPWM);

		textPWM[0] = (TextView) v.findViewById(R.id.textViewFM1);
		textPWM[1] = (TextView) v.findViewById(R.id.textViewFM2);
		textPWM[2] = (TextView) v.findViewById(R.id.textViewFM3);
		textPWM[3] = (TextView) v.findViewById(R.id.textViewFM4);
		textPWM[4] = (TextView) v.findViewById(R.id.textViewFM5);
		textPWM[5] = (TextView) v.findViewById(R.id.textViewFM6);

		pwmSpinners[0] = (Spinner) v.findViewById(R.id.spinnerFM1);
		pwmSpinners[1] = (Spinner) v.findViewById(R.id.spinnerFM2);
		pwmSpinners[2] = (Spinner) v.findViewById(R.id.spinnerFM3);
		pwmSpinners[3] = (Spinner) v.findViewById(R.id.spinnerFM4);
		pwmSpinners[4] = (Spinner) v.findViewById(R.id.spinnerFM5);
		pwmSpinners[5] = (Spinner) v.findViewById(R.id.spinnerFM6);

		chkbxSimple[0] = (CheckBox) v.findViewById(R.id.checkBoxFM1);
		chkbxSimple[1] = (CheckBox) v.findViewById(R.id.checkBoxFM2);
		chkbxSimple[2] = (CheckBox) v.findViewById(R.id.checkBoxFM3);
		chkbxSimple[3] = (CheckBox) v.findViewById(R.id.checkBoxFM4);
		chkbxSimple[4] = (CheckBox) v.findViewById(R.id.checkBoxFM5);
		chkbxSimple[5] = (CheckBox) v.findViewById(R.id.checkBoxFM6);

		chkbxSuperSimple[0] = (CheckBox) v.findViewById(R.id.checkBoxFMS1);
		chkbxSuperSimple[1] = (CheckBox) v.findViewById(R.id.checkBoxFMS2);
		chkbxSuperSimple[2] = (CheckBox) v.findViewById(R.id.checkBoxFMS3);
		chkbxSuperSimple[3] = (CheckBox) v.findViewById(R.id.checkBoxFMS4);
		chkbxSuperSimple[4] = (CheckBox) v.findViewById(R.id.checkBoxFMS5);
		chkbxSuperSimple[5] = (CheckBox) v.findViewById(R.id.checkBoxFMS6);

		layoutPWM[0] = (LinearLayout) v.findViewById(R.id.layoutFM1);
		layoutPWM[1] = (LinearLayout) v.findViewById(R.id.layoutFM2);
		layoutPWM[2] = (LinearLayout) v.findViewById(R.id.layoutFM3);
		layoutPWM[3] = (LinearLayout) v.findViewById(R.id.layoutFM4);
		layoutPWM[4] = (LinearLayout) v.findViewById(R.id.layoutFM5);
		layoutPWM[5] = (LinearLayout) v.findViewById(R.id.layoutFM6);

		setupSpinners();

	}

	private void setupSpinners() {
		final ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(parentActivity,
						R.array.FligthMode_CopterV3_1,
						R.layout.spinner_setup_item);

		adapter.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);

		for (Spinner spinner : pwmSpinners) {
			spinner.setAdapter(adapter);
		}
	}

	@Override
	public void doCalibrationStep(int step) {
		switch (step) {
		case 1:
			uploadCalibrationData();
			break;
		case 0:
		default:
			sidePanel = getInitialPanel();
		}
	}

	private SetupSidePanel getInitialPanel() {
		sidePanel = ((SetupRadioFragment) getParentFragment())
				.changeSidePanel(new FragmentSetupFMConfiguration());
		return sidePanel;
	}

	private void uploadCalibrationData() {
		int cnt = 0;

		// Read all spinners value
		for (Spinner spinner : pwmSpinners) {
			dataFM[cnt] = spinner.getSelectedItemPosition();
			cnt++;
		}

		// read SIMPLE MODE check boxes and create bit value
		cnt = 0;
		for (CheckBox chkbx : chkbxSimple) {
			if (chkbx.isChecked()) {
				dataFM[6] += flightModeIndex[cnt];
			}
			cnt++;
		}

		// read SUPER SIMPLE MODE check boxes and create bit value
		cnt = 0;
		for (CheckBox chkbx : chkbxSuperSimple) {
			if (chkbx.isChecked()) {
				dataFM[7] += flightModeIndex[cnt];
			}
			cnt++;
		}

	}

	private void updatePWMPanels() {
		int pwmIn = drone.RC.in[4];
		int pwmId = getPWMRangeIndex(pwmIn);

		textPWMCurrent.setText(String.format("PWM in : %d", pwmIn));
		textPWMRange.setText("Flight Mode #" + String.valueOf(pwmId + 1) + " ("
				+ listPWM[pwmId] + ")");
		updateLayout(pwmId);

	}

	private void updateLayout(int pwmId) {
		int cnt = 0;
		for (LinearLayout layout : layoutPWM) {
			if (cnt == pwmId)
				layout.setBackgroundColor(getResources().getColor(
						R.color.air_speed_label));
			else
				layout.setBackgroundColor(0);
			cnt++;
		}
	}

	private int getPWMRangeIndex(int pwmValue) {

		if (pwmValue <= pwm[0])
			return 0;
		else if (pwmValue > pwm[0] && pwmValue <= pwm[1])
			return 1;
		else if (pwmValue > pwm[1] && pwmValue <= pwm[2])
			return 2;
		else if (pwmValue > pwm[2] && pwmValue <= pwm[3])
			return 3;
		else if (pwmValue > pwm[3] && pwmValue <= pwm[4])
			return 4;
		else if (pwmValue >= pwm[4])
			return 5;

		return -1;
	}

}
