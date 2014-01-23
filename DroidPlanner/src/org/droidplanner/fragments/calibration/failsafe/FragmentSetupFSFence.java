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

public class FragmentSetupFSFence extends SuperSetupMainPanel {

	private ValueKeyData optionsType, optionsAction;

	@Override
	public int getPanelLayout() {
		return R.layout.fragment_setup_fs_fence_main;
	}

	@Override
	protected SetupSidePanel getDefaultPanel() {
		sidePanel = new FragmentSetupSend();
		sidePanel.updateTitle(R.string.setup_fs_fnc_side_title);
		sidePanel.updateDescription(R.string.setup_fs_fnc_side_desc);
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
		optionsType = ValueKey.getOptions(parentActivity,
				R.array.Fence_Type_Options);
		optionsAction = ValueKey.getOptions(parentActivity,
				R.array.Fence_Action_Options);

		final ArrayAdapter<String> adapterType = new ArrayAdapter<String>(
				parentActivity, R.layout.spinner_setup_item, optionsType.keys);
		adapterType.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);

		final ArrayAdapter<String> adapterAction = new ArrayAdapter<String>(
				parentActivity, R.layout.spinner_setup_item, optionsAction.keys);
		adapterAction.setDropDownViewResource(R.layout.spinner_setup_item_dropdown);

	}

}
