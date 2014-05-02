package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.core.MAVLink.MavLinkStreamRates;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.android.fragments.calibration.SetupMainPanel;
import org.droidplanner.android.fragments.calibration.ch.FragmentSetupCH;
import org.droidplanner.android.fragments.calibration.flightmodes.FragmentSetupFM;
import org.droidplanner.android.fragments.calibration.rc.FragmentSetupRC;
import org.droidplanner.android.fragments.calibration.sf.FragmentSetupSF;
import org.droidplanner.android.fragments.helpers.SuperSetupFragment;

import android.os.Bundle;

/**
 * This fragment is used to calibrate the drone's radio related functionalities.
 */
public class SetupRadioFragment extends SuperSetupFragment {
	// Extreme RC update rate in this screen
	private static final int RC_MSG_RATE = 50;

	private Drone drone;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.drone = parentActivity.drone;
	}

	@Override
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
	public void onResume() {
		super.onResume();
		setupDataStreamingForRcSetup();
	}

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
		SetupMainPanel setupPanel;
		switch (index) {
		case 1:
			setupPanel = new FragmentSetupFM();
			break;
		case 2:
			setupPanel = new FragmentSetupCH();
			break;
		case 3:
			setupPanel = new FragmentSetupSF();
			break;
		case 0:
		default:
			setupPanel = new FragmentSetupRC();
		}

		return setupPanel;
	}

	public void setupDataStreamingForRcSetup() {
		MavLinkStreamRates.setupStreamRates(drone.MavClient, 1, 0, 1, 1, 1,
				RC_MSG_RATE, 0, 0);
	}
}
