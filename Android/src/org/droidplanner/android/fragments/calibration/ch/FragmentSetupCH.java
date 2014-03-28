package org.droidplanner.android.fragments.calibration.ch;

import org.droidplanner.R;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.android.fragments.calibration.FragmentSetupSend;
import org.droidplanner.android.fragments.calibration.SetupSidePanel;
import org.droidplanner.android.fragments.helpers.SuperSetupMainPanel;
import org.droidplanner.android.helpers.calibration.CH_CalParameters;
import org.droidplanner.android.helpers.calibration.CalParameters;
import org.droidplanner.android.helpers.calibration.CalParameters.OnCalibrationEvent;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class FragmentSetupCH extends SuperSetupMainPanel implements
		OnCalibrationEvent, OnDroneListener {

	private int[] valueCH6;
	private int[] valueCH;
	private String[] stringCH6;
	private String[] stringCH;

	private Spinner spinnerCH6, spinnerCH7, spinnerCH8;
	private EditText editTuneH, editTuneL;

	@Override
	protected CalParameters getParameterHandler() {
		return new CH_CalParameters(drone);
	}

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_ch_main;
	}

	@Override
	public SetupSidePanel getSidePanel() {
		return getDefaultPanel();
	}

	@Override
	protected SetupSidePanel getDefaultPanel() {
		sidePanel = new FragmentSetupSend();
		sidePanel.updateTitle(R.string.setup_ch_side_title);
		sidePanel.updateDescription(R.string.setup_ch_side_desc);
		return sidePanel;
	}

	@Override
	protected void updateCalibrationData() {
		parameters.setParamValueByName("CH7_OPT",
				valueCH[spinnerCH7.getSelectedItemPosition()]);
		parameters.setParamValueByName("CH8_OPT",
				valueCH[spinnerCH8.getSelectedItemPosition()]);
		parameters.setParamValueByName("TUNE",
				valueCH6[spinnerCH6.getSelectedItemPosition()]);
		parameters.setParamValueByName("TUNE_LOW",
				Integer.parseInt(editTuneL.getText().toString()));
		parameters.setParamValueByName("TUNE_HIGH",
				Integer.parseInt(editTuneH.getText().toString()));
	}

	@Override
	protected void updatePanelInfo() {
		if (parameters == null)
			return;

		editTuneL.setText(String.format("%d",
				(int) parameters.getParamValueByName("TUNE_LOW")));
		editTuneH.setText(String.format("%d",
				(int) parameters.getParamValueByName("TUNE_HIGH")));

		spinnerCH6.setSelection(getSpinnerIndexFromValue(
				(int) parameters.getParamValueByName("TUNE"), valueCH6));
		spinnerCH7.setSelection(getSpinnerIndexFromValue(
				(int) parameters.getParamValueByName("CH7_OPT"), valueCH));
		spinnerCH8.setSelection(getSpinnerIndexFromValue(
				(int) parameters.getParamValueByName("CH8_OPT"), valueCH));
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
}
