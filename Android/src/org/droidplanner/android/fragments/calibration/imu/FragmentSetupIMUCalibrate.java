package org.droidplanner.android.fragments.calibration.imu;

import org.droidplanner.R;
import org.droidplanner.android.fragments.SetupSensorFragment;
import org.droidplanner.android.fragments.calibration.SetupSidePanel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentSetupIMUCalibrate extends SetupSidePanel {
	private Button btnStep;
	private TextView textDesc;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(
				R.layout.fragment_setup_imu_calibrate, container, false);

		final SetupSensorFragment setupFragment = (SetupSensorFragment) getParentFragment();

		textDesc = (TextView) view.findViewById(R.id.textViewDesc);
		btnStep = (Button) view.findViewById(R.id.buttonNext);
		btnStep.setOnClickListener(new OnClickListener() {
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
	public void updateDescription(int calibration_step) {
		int id;
		switch (calibration_step) {
		case 0:
			id = R.string.setup_imu_start;
			break;
		case 1:
			id = R.string.setup_imu_normal;
			break;
		case 2:
			id = R.string.setup_imu_left;
			break;
		case 3:
			id = R.string.setup_imu_right;
			break;
		case 4:
			id = R.string.setup_imu_nosedown;
			break;
		case 5:
			id = R.string.setup_imu_noseup;
			break;
		case 6:
			id = R.string.setup_imu_back;
			break;
		case 7:
			id = R.string.setup_imu_completed;
			break;
		default:
			return;
		}

		if (textDesc != null) {
			textDesc.setText(id);
		}
		if (btnStep != null) {
			if (calibration_step == 0)
				btnStep.setText(R.string.button_setup_calibrate);
			else if (calibration_step == 7)
				btnStep.setText(R.string.button_setup_done);
			else
				btnStep.setText(R.string.button_setup_next);
		}
	}

	public void setButtonCaption(int id) {
		if (btnStep != null)
			btnStep.setText(id);
	}

	public void setDescription(int id) {
		if (textDesc != null)
			textDesc.setText(id);
	}

	@Override
	public void updateTitle(int idTitle) {
		// TODO Auto-generated method stub

	}

}
