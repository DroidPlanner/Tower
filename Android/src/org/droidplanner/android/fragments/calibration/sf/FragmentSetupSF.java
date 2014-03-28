package org.droidplanner.android.fragments.calibration.sf;

import org.droidplanner.R;
import org.droidplanner.android.fragments.calibration.FragmentSetupSend;
import org.droidplanner.android.fragments.calibration.SetupSidePanel;
import org.droidplanner.android.fragments.helpers.SuperSetupMainPanel;
import org.droidplanner.android.helpers.calibration.CalParameters;
import org.droidplanner.android.helpers.calibration.SF_CalParameters;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class FragmentSetupSF extends SuperSetupMainPanel {
	int[] valueSF;
	private String[] stringSF;
	Spinner[] spinnerSFs = new Spinner[6];

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_sf_main;
	}

	@Override
	protected SetupSidePanel getDefaultPanel() {
		sidePanel = new FragmentSetupSend();
		sidePanel.updateTitle(R.string.setup_sf_side_title);
		sidePanel.updateDescription(R.string.setup_sf_side_desc);
		return sidePanel;
	}

	@Override
	public SetupSidePanel getSidePanel() {
		return getDefaultPanel();
	}

	@Override
	protected CalParameters getParameterHandler() {
		return new SF_CalParameters(drone);
	}

	@Override
	protected void updateCalibrationData() {
		for (int i = 0; i < 6; i++) {
			parameters.setParamValue(i,
					valueSF[spinnerSFs[i].getSelectedItemPosition()]);
		}
	}

	@Override
	protected void updatePanelInfo() {
		if (parameters == null)
			return;

		for (int i = 0; i < 6; i++) {
			spinnerSFs[i].setSelection(
					getSpinnerIndexFromValue((int) parameters.getParamValue(i),
							valueSF), true);
		}
	}

	@Override
	public void setupLocalViews(View v) {
		spinnerSFs[0] = (Spinner) v.findViewById(R.id.spinnerSF5);
		spinnerSFs[1] = (Spinner) v.findViewById(R.id.spinnerSF6);
		spinnerSFs[2] = (Spinner) v.findViewById(R.id.spinnerSF7);
		spinnerSFs[3] = (Spinner) v.findViewById(R.id.spinnerSF8);
		spinnerSFs[4] = (Spinner) v.findViewById(R.id.spinnerSF9);
		spinnerSFs[5] = (Spinner) v.findViewById(R.id.spinnerSF10);

		setupSpinners();
	}

	private void setupSpinners() {
		getSFOptions();

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				parentActivity, R.layout.spinner_setup_item, stringSF);
		adapter.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);

		for (Spinner spinner : spinnerSFs)
			spinner.setAdapter(adapter);
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
