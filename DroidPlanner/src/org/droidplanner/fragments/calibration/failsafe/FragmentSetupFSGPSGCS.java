package org.droidplanner.fragments.calibration.failsafe;

import org.droidplanner.R;
import org.droidplanner.calibration.CalParameters;
import org.droidplanner.calibration.FSG_CalParameters;
import org.droidplanner.fragments.calibration.FragmentSetupSend;
import org.droidplanner.fragments.calibration.SetupSidePanel;
import org.droidplanner.fragments.helpers.SuperSetupMainPanel;
import org.droidplanner.helpers.ValueKey;
import org.droidplanner.helpers.ValueKey.ValueKeyData;
import org.droidplanner.widgets.NumberFieldEdit.NumberFieldEdit;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class FragmentSetupFSGPSGCS extends SuperSetupMainPanel {

	private ValueKeyData optionsGPS, optionsGCS;
	private Spinner spinnerGPS, spinnerGCS;
	private NumberFieldEdit numberFieldGPS, numberFieldGCS;

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_fs_gpsgcs_main;
	}

	@Override
	protected SetupSidePanel getDefaultPanel() {
		sidePanel = new FragmentSetupSend();
		sidePanel.updateTitle(R.string.setup_fs_gpsgcs_side_title);
		sidePanel.updateDescription(R.string.setup_fs_gpsgcs_side_desc);
		return sidePanel;
	}

	@Override
	public SetupSidePanel getSidePanel() {
		return getDefaultPanel();
	}

	@Override
	protected CalParameters getParameterHandler() {
		return new FSG_CalParameters(drone);
	}

	@Override
	protected void updateCalibrationData() {
		parameters.setParamValueByName("FS_GPS_ENABLE",
				optionsGPS.values[spinnerGPS.getSelectedItemPosition()]);

		parameters.setParamValueByName("FS_GCS_ENABLE",
				optionsGCS.values[spinnerGCS.getSelectedItemPosition()]);
		
		parameters.setParamValueByName("GPS_HDOP_GOOD",
				numberFieldGPS.getValue());
		
		parameters.setParamValueByName("GCS_HDOP_GOOD",
				numberFieldGCS.getValue());
	}

	@Override
	protected void updatePanelInfo() {
		if (parameters == null)
			return;

		spinnerGPS.setSelection(
				getSpinnerIndexFromValue(
						(int) parameters.getParamValueByName("FS_GPS_ENABLE"),
						optionsGPS.values), true);
		spinnerGCS.setSelection(
				getSpinnerIndexFromValue(
						(int) parameters.getParamValueByName("FS_GCS_ENABLE"),
						optionsGCS.values), true);

		numberFieldGPS
				.setValue(parameters.getParamValueByName("GPS_HDOP_GOOD"));
		numberFieldGPS.setValue(parameters.getParamValueByName("GCS_SYSID"));
	}

	@Override
	public void setupLocalViews(View v) {
		spinnerGPS = (Spinner) v.findViewById(R.id.spinnerGPS);
		spinnerGCS = (Spinner) v.findViewById(R.id.spinnerGCS);

		numberFieldGPS = (NumberFieldEdit) v
				.findViewById(R.id.numberFieldEditGPS);
		numberFieldGCS = (NumberFieldEdit) v
				.findViewById(R.id.numberFieldEditGCS);
		setupSpinners();
	}

	private void setupSpinners() {
		optionsGPS = ValueKey.getOptions(parentActivity,
				R.array.FailSafe_GPS_Options);
		optionsGCS = ValueKey.getOptions(parentActivity,
				R.array.FailSafe_GCS_Options);

		final ArrayAdapter<String> adapterGPS = new ArrayAdapter<String>(
				parentActivity, R.layout.spinner_setup_item, optionsGPS.keys);
		adapterGPS
				.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);

		final ArrayAdapter<String> adapterGCS = new ArrayAdapter<String>(
				parentActivity, R.layout.spinner_setup_item, optionsGCS.keys);
		adapterGCS
				.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);

		spinnerGPS.setAdapter(adapterGPS);
		spinnerGCS.setAdapter(adapterGCS);

	}

}
