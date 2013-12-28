package com.droidplanner.fragments.calibration.imu;

import com.droidplanner.R;
import com.droidplanner.fragments.calibration.FragmentCalibration;
import com.droidplanner.fragments.calibration.FragmentSetupSidePanel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentSetupIMUCalibrate extends FragmentSetupSidePanel implements
		OnClickListener {
	private FragmentSetupIMU parent;
	private Button btnStep;
	private TextView textDesc;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_imu_calibrate,
				container, false);
		setupLocalViews(view);

		return view;
	}

	private void setupLocalViews(View view) {
		textDesc = (TextView) view.findViewById(R.id.textViewDesc);
		btnStep = (Button) view.findViewById(R.id.buttonNext);
		btnStep.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (parent != null) {
			parent.doCalibrationStep();
			updateTitle(parent.calibration_step);
		}
	}

	public void updateTitle(int calibration_step) {
		int id = -1;
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

	@Override
	public void setParent(FragmentCalibration parent) {
		this.parent = (FragmentSetupIMU) parent;
	}

	public void setButtonCaption(int id) {
		if (btnStep != null)
			btnStep.setText(id);
	}

	public void setDescription(int id) {
		if (textDesc != null)
			textDesc.setText(id);
	}

}
