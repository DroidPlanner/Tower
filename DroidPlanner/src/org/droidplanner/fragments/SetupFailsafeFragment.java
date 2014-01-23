package org.droidplanner.fragments;

import org.droidplanner.R;
import org.droidplanner.MAVLink.MavLinkStreamRates;
import org.droidplanner.fragments.calibration.SetupMainPanel;
import org.droidplanner.fragments.calibration.failsafe.FragmentSetupTHR;
import org.droidplanner.fragments.calibration.radio.FragmentSetupRC;
import org.droidplanner.fragments.helpers.SuperSetupFragment;

/**
 * This fragment is used to calibrate the drone's compass, and accelerometer.
 */
public class SetupFailsafeFragment extends SuperSetupFragment {
	// Extreme RC update rate in this screen
	private static final int RC_MSG_RATE = 50;

	public void onStart() {
		super.onStart();
		setupDataStreamingForRcSetup();
	}

	@Override
	public void onStop() {
		super.onStop();
		drone.streamRates.setupStreamRatesFromPref();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		setupDataStreamingForRcSetup();
	}

	@Override
	public SetupMainPanel initMainPanel() {
		return new FragmentSetupTHR();
	}

	@Override
	public int getSpinnerItems() {
		return R.array.Setup_Failsafe_Menu;
	}

	@Override
	public SetupMainPanel getMainPanel(int index) {
 		SetupMainPanel setupPanel = null;
		switch (index) {
		case 1:
//			updateTitle(R.string.setup_fm_title);
//			setupPanel = new FragmentSetupTHR();
			break;
		case 2:
//			updateTitle(R.string.setup_ch_title);
//			setupPanel = new FragmentSetupCH();
			break;
		case 3:
//			updateTitle(R.string.setup_sf_title);
//			setupPanel = new FragmentSetupSF();
			break;
		case 0:
		default:
			updateTitle(R.string.setup_fs_thr_title);
			setupPanel = new FragmentSetupTHR();
		}		
		
		return setupPanel;
	}

	@Override
	public void updateTitle(int id) {
		super.updateTitle(id);
	}

	public void setupDataStreamingForRcSetup() {
		MavLinkStreamRates.setupStreamRates(drone.MavClient, 1, 0, 1, 1, 1,
				RC_MSG_RATE, 0, 0);
	}
}
