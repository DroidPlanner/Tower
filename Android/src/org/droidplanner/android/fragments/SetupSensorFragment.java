package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.fragments.calibration.SetupMainPanel;
import org.droidplanner.android.fragments.calibration.imu.FragmentSetupIMU;
import org.droidplanner.android.fragments.calibration.mag.FragmentSetupMAG;
import org.droidplanner.android.fragments.helpers.SuperSetupFragment;

/**
 * This fragment is used to calibrate the drone's compass, and accelerometer.
 */
public class SetupSensorFragment extends SuperSetupFragment {

	@Override
	public SetupMainPanel initMainPanel() {
		return new FragmentSetupIMU();
	}

	@Override
	public int getSpinnerItems() {
		return R.array.Setup_Sensor_Menu;
	}

	@Override
	public SetupMainPanel getMainPanel(int index) {
		switch (index) {
		case 0:
			return new FragmentSetupIMU();
		case 1:
			return new FragmentSetupMAG();

		default:
			return null;
		}
	}

}
