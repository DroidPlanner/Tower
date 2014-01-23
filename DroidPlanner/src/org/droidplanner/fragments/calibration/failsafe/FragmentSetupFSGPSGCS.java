package org.droidplanner.fragments.calibration.failsafe;

import org.droidplanner.R;
import org.droidplanner.calibration.CalParameters;
import org.droidplanner.calibration.FST_CalParameters;
import org.droidplanner.fragments.calibration.FragmentSetupSend;
import org.droidplanner.fragments.calibration.SetupSidePanel;
import org.droidplanner.fragments.helpers.SuperSetupMainPanel;
import org.droidplanner.helpers.ValueKey;
import org.droidplanner.helpers.ValueKey.ValueKeyData;

import android.view.View;
import android.widget.ArrayAdapter;

public class FragmentSetupFSGPSGCS extends SuperSetupMainPanel {

	private ValueKeyData optionsGPS, optionsGCS;

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
		return new FST_CalParameters(drone);
	}

	@Override
	protected void updateCalibrationData() {
	}

	@Override
	protected void updatePanelInfo() {
		if (parameters == null)
			return;
	}

	@Override
	public void setupLocalViews(View v) {
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

	}

}
