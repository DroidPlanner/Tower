package org.droidplanner.fragments;

import org.droidplanner.R;
import org.droidplanner.fragments.calibration.SetupMainPanel;
import org.droidplanner.fragments.calibration.rc.FragmentSetupRC;
import org.droidplanner.fragments.helpers.SuperSetupFragment;

/**
 * This fragment is used to calibrate the drone's compass, and accelerometer.
 */
public class SetupRadioFragment extends SuperSetupFragment {

	@Override
	public SetupMainPanel initMainPanel() {
		return new FragmentSetupRC();
	}

	@Override
	public int getSpinnerItems() {
		return R.array.Setup_Radio_Menu;
	}

	@Override
	public SetupMainPanel getMainPanel(int index) {
		updateTitle(R.string.setup_radio_title);
		return new FragmentSetupRC();
	}

	@Override
	public void updateTitle(int id) {
		super.updateTitle(id);
	}
}
