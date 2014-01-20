package org.droidplanner.fragments.calibration.ch;

import org.droidplanner.R;
import org.droidplanner.calibration.CH_CalParameters;
import org.droidplanner.calibration.CalParameters;
import org.droidplanner.calibration.CalParameters.OnCalibrationEvent;
import org.droidplanner.drone.Drone;
import org.droidplanner.fragments.SetupRadioFragment;
import org.droidplanner.fragments.calibration.FragmentSetupProgress;
import org.droidplanner.fragments.calibration.FragmentSetupSend;
import org.droidplanner.fragments.calibration.SetupMainPanel;
import org.droidplanner.fragments.calibration.SetupSidePanel;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class FragmentSetupCH extends SetupMainPanel implements
		OnCalibrationEvent {

	private int[] valueCH6;
	private int[] valueCH;
	private String[] stringCH6;
	private String[] stringCH;

	private Spinner spinnerCH6, spinnerCH7, spinnerCH8;
	private EditText editTuneH, editTuneL;

	private Drone drone;
	private CH_CalParameters chParameters;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.drone = parentActivity.drone;
	}

	@Override
	public void onStart() {
		super.onStart();
		downloadCalibrationData();
	}

	@Override
	public void onReadCalibration(CalParameters calParameters) {
		doCalibrationStep(0);
		updatePanelInfo();

	}

	@Override
	public void onSentCalibration(CalParameters calParameters) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCalibrationData(CalParameters calParameters, int index,
			int count, boolean isSending) {
		if (sidePanel != null && chParameters != null) {
			String title;
			if (isSending) {
				title = getResources().getString(
						R.string.setup_ch_desc_uploading);
			} else {
				title = getResources().getString(
						R.string.setup_ch_desc_downloading);
			}

			((FragmentSetupProgress) sidePanel).updateProgress(index + 1,
					count, title);
		}
	}

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_ch_main;
	}

	@Override
	public SetupSidePanel getSidePanel() {
		return getInitialPanel();
	}

	@Override
	public void setupLocalViews(View v) {
		editTuneL = (EditText) v.findViewById(R.id.editTextTuneL);
		editTuneH = (EditText) v.findViewById(R.id.editTextTuneH);

		spinnerCH6 = (Spinner) v.findViewById(R.id.spinnerCH6);
		spinnerCH7 = (Spinner) v.findViewById(R.id.spinnerCH7);
		spinnerCH8 = (Spinner) v.findViewById(R.id.spinnerCH8);

		setupSpinners();
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
		sidePanel = new FragmentSetupSend();
		sidePanel.updateTitle(R.string.setup_ch_side_title);
		sidePanel.updateDescription(R.string.setup_ch_side_desc);

		return sidePanel;
	}

	private SetupSidePanel getProgressPanel(boolean isSending) {
		sidePanel = ((SetupRadioFragment) getParentFragment())
				.changeSidePanel(new FragmentSetupProgress());

		if (isSending) {
			sidePanel.updateTitle(R.string.progress_title_uploading);
			sidePanel.updateDescription(R.string.progress_desc_uploading);
		} else {
			sidePanel.updateTitle(R.string.progress_title_downloading);
			sidePanel.updateDescription(R.string.progress_desc_downloading);
		}

		return sidePanel;
	}

	private void uploadCalibrationData() {
		if (chParameters == null || !drone.MavClient.isConnected())
			return;

		sidePanel = getProgressPanel(true);

		// TODO setParameterValues here
		chParameters.sendCalibrationParameters();
	}

	private void downloadCalibrationData() {
		if (chParameters == null || !drone.MavClient.isConnected())
			return;
		sidePanel = getProgressPanel(false);
		chParameters.getCalibrationParameters(drone);
	}

	private void updatePanelInfo() {
		if (chParameters == null)
			return;

		editTuneL.setText(String.format("%d", chParameters.getParamValueByName("TUNE_LOW")));
		editTuneH.setText(String.format("%d", chParameters.getParamValueByName("TUNE_HIGH")));

		spinnerCH6.setSelection(getSpinnerIndexFromValue(
				(int) chParameters.getParamValueByName("TUNE"), valueCH6));
		spinnerCH7.setSelection(getSpinnerIndexFromValue(
				(int) chParameters.getParamValueByName("CH7_OPT"), valueCH));
		spinnerCH7.setSelection(getSpinnerIndexFromValue(
				(int) chParameters.getParamValueByName("CH8_OPT"), valueCH));
	}

	private void setupSpinners() {
		getCH6Options();
		getCHOptions();

		final ArrayAdapter<String> adapterCH = new ArrayAdapter<String>(
				parentActivity, R.layout.spinner_setup_item, stringCH);
		final ArrayAdapter<String> adapterTune = new ArrayAdapter<String>(
				parentActivity, R.layout.spinner_setup_item, stringCH6);

		adapterCH.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);
		adapterTune
				.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);

		spinnerCH6.setAdapter(adapterTune);
		spinnerCH7.setAdapter(adapterCH);
		spinnerCH8.setAdapter(adapterCH);
	}

	private void getCHOptions() {
		String pairs[] = getResources().getStringArray(R.array.CH_Options);
		valueCH = null;
		valueCH = new int[pairs.length];
		stringCH = null;
		stringCH = new String[pairs.length];

		int i = 0;
		for (String item : pairs) {
			String pair[] = item.split(";");
			valueCH[i] = Integer.parseInt(pair[0]);
			stringCH[i] = pair[1];
			i++;
		}
	}

	private void getCH6Options() {
		String pairs[] = getResources().getStringArray(R.array.CH6_Options);
		valueCH6 = null;
		valueCH6 = new int[pairs.length];
		stringCH6 = null;
		stringCH6 = new String[pairs.length];

		int i = 0;
		for (String item : pairs) {
			String pair[] = item.split(";");
			valueCH6[i] = Integer.parseInt(pair[0]);
			stringCH6[i] = pair[1];
			i++;
		}
	}

	private int getSpinnerIndexFromValue(int value, int[] valueList) {
		for (int i = 0; i < valueList.length; i++) {
			if (valueList[i] == value)
				return i;
		}
		return -1;
	}
}
