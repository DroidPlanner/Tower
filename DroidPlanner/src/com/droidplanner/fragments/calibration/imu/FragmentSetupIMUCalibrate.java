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

public class FragmentSetupIMUCalibrate extends FragmentSetupSidePanel implements OnClickListener{
	private FragmentSetupIMU parent;
	private Button btnStep;
	private TextView textDesc;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setup_imu_calibrate, container,
				false);
		setupLocalViews(view);

		return view;
	}

	private void setupLocalViews(View view) {
		textDesc = (TextView)view.findViewById(R.id.textViewDesc);
		btnStep = (Button)view.findViewById(R.id.buttonNext);
		btnStep.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParent(FragmentCalibration parent) {
		this.parent = (FragmentSetupIMU) parent;
	}

	public void setButtonCaption(int id){
		if(btnStep!=null)
			btnStep.setText(id);
	}
	
	public void setDescription(int id){
		if(textDesc!=null)
			textDesc.setText(id);
	}

}
