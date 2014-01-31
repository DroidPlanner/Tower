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
import android.widget.CheckBox;
import android.widget.Spinner;

public class FragmentSetupFSGPS extends SuperSetupMainPanel {

	private ValueKeyData optionsGPS;
	private Spinner spinnerGPS;
	private CheckBox chkbxGPSGLTEnable;
	private NumberFieldEdit numberFieldGPS, numberFieldGLTRadius,
			numberFieldGLTAccl;

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_fs_gps_main;
	}

	@Override
	protected SetupSidePanel getDefaultPanel() {
		sidePanel = new FragmentSetupSend();
		sidePanel.updateTitle(R.string.setup_fs_gps_side_title);
		sidePanel.updateDescription(R.string.setup_fs_gps_side_desc);
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

		parameters.setParamValueByName("GPS_HDOP_GOOD",
				numberFieldGPS.getValue());

		parameters.setParamValueByName("GPSGLITCH_ENABLE",
				chkbxGPSGLTEnable.isChecked() ? 1 : 0);

		parameters.setParamValueByName("GPSGLITCH_RADIUS",
				numberFieldGLTRadius.getValue());

		parameters.setParamValueByName("GPSGLITCH_ACCEL",
				numberFieldGLTAccl.getValue());
	}

	@Override
	protected void updatePanelInfo() {
		if (parameters == null)
			return;

		spinnerGPS.setSelection(
				getSpinnerIndexFromValue(
						(int) parameters.getParamValueByName("FS_GPS_ENABLE"),
						optionsGPS.values), true);
		
		numberFieldGPS
				.setValue(parameters.getParamValueByName("GPS_HDOP_GOOD"));

		numberFieldGLTRadius.setValue(parameters
				.getParamValueByName("GPSGLITCH_RADIUS"));

		numberFieldGLTAccl.setValue(parameters
				.getParamValueByName("GPSGLITCH_ACCEL"));
		
		chkbxGPSGLTEnable.setChecked((parameters.getParamValueByName("GPSGLITCH_ENABLE")>0?true:false));
	}

	@Override
	public void setupLocalViews(View v) {
		spinnerGPS = (Spinner) v.findViewById(R.id.spinnerGPS);

		numberFieldGPS = (NumberFieldEdit) v
				.findViewById(R.id.numberFieldEditGPS);
		numberFieldGLTAccl = (NumberFieldEdit) v
				.findViewById(R.id.numberFieldEditGPSACCL);
		numberFieldGLTRadius = (NumberFieldEdit) v
				.findViewById(R.id.numberFieldEditGPSRadius);
		chkbxGPSGLTEnable = (CheckBox)v.findViewById(R.id.chkbxGPSGlitch);
		setupSpinners();
	}

	private void setupSpinners() {
		optionsGPS = ValueKey.getOptions(parentActivity,
				R.array.FailSafe_GPS_Options);

		final ArrayAdapter<String> adapterGPS = new ArrayAdapter<String>(
				parentActivity, R.layout.spinner_setup_item, optionsGPS.keys);
		adapterGPS
				.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);

		spinnerGPS.setAdapter(adapterGPS);
	}

}
