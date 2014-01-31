package org.droidplanner.fragments.calibration.failsafe;

import org.droidplanner.R;
import org.droidplanner.calibration.CalParameters;
import org.droidplanner.calibration.FSG_CalParameters;
import org.droidplanner.fragments.calibration.FragmentSetupSend;
import org.droidplanner.fragments.calibration.SetupSidePanel;
import org.droidplanner.fragments.helpers.SuperSetupMainPanel;
import org.droidplanner.helpers.ValueKey;
import org.droidplanner.helpers.ValueKey.ValueKeyData;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class FragmentSetupFSGCS extends SuperSetupMainPanel {

	private ValueKeyData optionsGCS;
	private Spinner spinnerGCS;

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_fs_gcs_main;
	}

	@Override
	protected SetupSidePanel getDefaultPanel() {
		sidePanel = new FragmentSetupSend();
		sidePanel.updateTitle(R.string.setup_fs_gcs_side_title);
		sidePanel.updateDescription(R.string.setup_fs_gcs_side_desc);
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
		parameters.setParamValueByName("FS_GCS_ENABLE",
				optionsGCS.values[spinnerGCS.getSelectedItemPosition()]);
	}

	@Override
	protected void updatePanelInfo() {
		if (parameters == null)
			return;

		spinnerGCS.setSelection(
				getSpinnerIndexFromValue(
						(int) parameters.getParamValueByName("FS_GCS_ENABLE"),
						optionsGCS.values), true);

	}

	@Override
	public void setupLocalViews(View v) {
		spinnerGCS = (Spinner) v.findViewById(R.id.spinnerGCS);

		setupSpinners();
	}

	private void setupSpinners() {
		optionsGCS = ValueKey.getOptions(parentActivity,
				R.array.FailSafe_GCS_Options);

		final ArrayAdapter<String> adapterGCS = new ArrayAdapter<String>(
				parentActivity, R.layout.spinner_setup_item, optionsGCS.keys);
		adapterGCS
				.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);

		spinnerGCS.setAdapter(adapterGCS);

	}

}
