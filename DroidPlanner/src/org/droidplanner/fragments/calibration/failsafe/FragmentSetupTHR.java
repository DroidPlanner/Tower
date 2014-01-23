package org.droidplanner.fragments.calibration.failsafe;

import org.droidplanner.R;
import org.droidplanner.calibration.CalParameters;
import org.droidplanner.calibration.THR_CalParameters;
import org.droidplanner.fragments.calibration.FragmentSetupSend;
import org.droidplanner.fragments.calibration.SetupSidePanel;
import org.droidplanner.fragments.helpers.SuperSetupMainPanel;
import org.droidplanner.helpers.ValueKey;
import org.droidplanner.helpers.ValueKey.ValueKeyData;

import android.view.View;
import android.widget.ArrayAdapter;

public class FragmentSetupTHR extends SuperSetupMainPanel{

	private ValueKeyData options;
	
	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_fs_thr_main;
	}


	@Override
	protected SetupSidePanel getDefaultPanel() {
		sidePanel = new FragmentSetupSend();
		sidePanel.updateTitle(R.string.setup_fs_thr_side_title);
		sidePanel.updateDescription(R.string.setup_fs_thr_side_desc);
		return sidePanel;
	}
	
	@Override
	public SetupSidePanel getSidePanel() {
		return getDefaultPanel();
	}

	@Override
	protected CalParameters getParameterHandler() {		
		return new THR_CalParameters(drone);
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
		options = ValueKey.getOptions(parentActivity, R.array.FailSafe_Throttle_Options);

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				parentActivity, R.layout.spinner_setup_item, options.keys);
		adapter.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);

	}

}
