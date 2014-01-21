package org.droidplanner.fragments.calibration.flightmodes;

import org.droidplanner.fragments.SetupRadioFragment;
import org.droidplanner.fragments.calibration.SetupSidePanel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import org.droidplanner.R;

public class FragmentSetupFMConfiguration extends SetupSidePanel {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final SetupRadioFragment setupFragment = (SetupRadioFragment) getParentFragment();

		final View view = inflater.inflate(R.layout.fragment_setup_fm_send,
				container, false);

		final Button btnSend = (Button) view.findViewById(R.id.ButtonSend);
		btnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (setupFragment != null) {
					setupFragment.doCalibrationStep(1);
				}
			}
		});

		final Button btnCancel = (Button) view.findViewById(R.id.ButtonReset);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (setupFragment != null) {
					setupFragment.doCalibrationStep(0);
				}
			}
		});
		return view;
	}

	@Override
	public void updateDescription(int idDescription) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateTitle(int idTitle) {
		// TODO Auto-generated method stub

	}
}
