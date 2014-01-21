package org.droidplanner.fragments.calibration.sf;

import org.droidplanner.R;
import org.droidplanner.calibration.CalParameters;
import org.droidplanner.calibration.SF_CalParameters;
import org.droidplanner.calibration.CalParameters.OnCalibrationEvent;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListner;
import org.droidplanner.fragments.SetupRadioFragment;
import org.droidplanner.fragments.calibration.FragmentSetupProgress;
import org.droidplanner.fragments.calibration.FragmentSetupSend;
import org.droidplanner.fragments.calibration.SetupMainPanel;
import org.droidplanner.fragments.calibration.SetupSidePanel;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class FragmentSetupSF extends SetupMainPanel implements
		OnCalibrationEvent, OnDroneListner {
	private int[] valueSF;
	private String[] stringSF;
	private Spinner[] spinnerSFs = new Spinner[6];

	private Drone drone;
	private SF_CalParameters sfParameters;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.drone = parentActivity.drone;
		sfParameters = new SF_CalParameters(drone);
		sfParameters.setOnCalibrationEventListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		doCalibrationStep(0);
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
		case PARAMETER:
			if (sfParameters != null) {
				sfParameters.processReceivedParam();
			}
		default:
			break;
		}
	}

	@Override
	public void onReadCalibration(CalParameters calParameters) {
		doCalibrationStep(0);
		updatePanelInfo();
	}

	@Override
	public void onSentCalibration(CalParameters calParameters) {
		doCalibrationStep(0);
	}

	@Override
	public void onCalibrationData(CalParameters calParameters, int index,
			int count, boolean isSending) {
		if (sidePanel != null && sfParameters != null) {
			String title;
			if (isSending) {
				title = getResources().getString(
						R.string.setup_sf_desc_uploading);
			} else {
				title = getResources().getString(
						R.string.setup_sf_desc_downloading);
			}

			((FragmentSetupProgress) sidePanel).updateProgress(index + 1,
					count, title);
		}
	}

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_sf_main;
	}

	@Override
	public SetupSidePanel getSidePanel() {
		sidePanel = new FragmentSetupSend();
		sidePanel.updateTitle(R.string.setup_sf_side_title);
		sidePanel.updateDescription(R.string.setup_sf_side_desc);
		return sidePanel;
	}

	@Override
	public void setupLocalViews(View v) {
		spinnerSFs[0] = (Spinner) v.findViewById(R.id.spinnerSF5);
		spinnerSFs[1] = (Spinner) v.findViewById(R.id.spinnerSF6);
		spinnerSFs[2] = (Spinner) v.findViewById(R.id.spinnerSF7);
		spinnerSFs[3] = (Spinner) v.findViewById(R.id.spinnerSF8);
		spinnerSFs[4] = (Spinner) v.findViewById(R.id.spinnerSF10);
		spinnerSFs[5] = (Spinner) v.findViewById(R.id.spinnerSF11);

		setupSpinners();
	}

	@Override
	public void doCalibrationStep(int step) {
		switch (step) {
		case 3:
			uploadCalibrationData();
			break;
		case 0:
		default:
			sidePanel = getInitialPanel();
		}
	}

	private SetupSidePanel getInitialPanel() {

		if (sfParameters != null && !sfParameters.isParameterDownloaded()
				&& drone.MavClient.isConnected()) {
			downloadCalibrationData();
		} else {
			sidePanel = ((SetupRadioFragment) getParentFragment())
					.changeSidePanel(new FragmentSetupSend());
			sidePanel.updateTitle(R.string.setup_sf_side_title);
			sidePanel.updateDescription(R.string.setup_sf_side_desc);
		}
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
		if (sfParameters == null || !drone.MavClient.isConnected())
			return;

		sidePanel = getProgressPanel(true);

		for (int i = 0; i < 6; i++) {
			sfParameters.setParamValue(i,
					valueSF[spinnerSFs[i].getSelectedItemPosition()]);
		}

		sfParameters.sendCalibrationParameters();
	}

	private void downloadCalibrationData() {
		if (sfParameters == null || !drone.MavClient.isConnected())
			return;
		sidePanel = getProgressPanel(false);
		sfParameters.getCalibrationParameters(drone);
	}

	private void updatePanelInfo() {
		if (sfParameters == null)
			return;

		for (int i = 0; i < 6; i++) {
			spinnerSFs[i].setSelection(
					getSpinnerIndexFromValue((int)sfParameters.getParamValue(i),
							valueSF), true);
		}
	}

	private void setupSpinners() {
		getSFOptions();

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				parentActivity, R.layout.spinner_setup_item, stringSF);
		adapter.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);

		for(Spinner spinner: spinnerSFs)
			spinner.setAdapter(adapter);
	}

	private int getSpinnerIndexFromValue(int value, int[] valueList) {
		for (int i = 0; i < valueList.length; i++) {
			if (valueList[i] == value)
				return i;
		}
		return -1;
	}

	private void getSFOptions() {
		String pairs[] = getResources().getStringArray(R.array.Servo_Functions);
		valueSF = null;
		valueSF = new int[pairs.length];
		stringSF = null;
		stringSF = new String[pairs.length];

		int i = 0;
		for (String item : pairs) {
			String pair[] = item.split(";");
			valueSF[i] = Integer.parseInt(pair[0]);
			stringSF[i] = pair[1];
			i++;
		}
	}
}
