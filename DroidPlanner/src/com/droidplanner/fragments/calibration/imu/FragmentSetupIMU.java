package com.droidplanner.fragments.calibration.imu;

import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.fragments.calibration.FragmentCalibration;
import com.droidplanner.fragments.calibration.FragmentSetupSidePanel;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentSetupIMU extends FragmentCalibration implements
		OnDroneListner {
	private static String msg, offset, scaling;
	boolean calibrationPassed;
	private static int calibration_step = 0;
	private static TextView tv;

	@Override
	public void onPause() {
		if (parent != null) {
			parent.getDrone().events.addDroneListener(this);
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		if (parent != null) {
			parent.getDrone().events.removeDroneListener(this);
		}
		super.onResume();
	}

	@Override
	protected View getView(LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.fragment_setup_imu_main, container,
				false);
	}

	@Override
	protected void setupLocalViews(View view) {
		tv = (TextView) view.findViewById(R.id.textView1);

	}

	@Override
	protected FragmentSetupSidePanel getSidePanel() {
		return new FragmentSetupIMUCalibrate();
	}

	@Override
	protected void initSidePanel() {
		sidePanel = (FragmentSetupIMUCalibrate) fragmentManager
				.findFragmentById(R.id.fragment_setup_sidepanel);
	}

	@Override
	protected void updateSidePanel() {
		sidePanel = new FragmentSetupIMUCalibrate();
		sidePanel.setParent(this);
	}

	public void doCalibrationStep() {
		processCalibrationStep(calibration_step);
	}

	private void processCalibrationStep(int step) {
		if (step == 0)
			startCalibration();
		else if (step > 0 && step < 7)
			sendAck(step);
		else{
			calibration_step = 0;
			tv.setText(msg);
		}
	}

	private void sendAck(int step) {
		if (parent.getDrone() != null) {
			parent.getDrone().calibrationSetup.sendAckk(step);
		}
	}

	private void startCalibration() {
		if (parent.getDrone() != null) {
			parent.getDrone().calibrationSetup.startCalibration();
		}
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		if (event == DroneEventsType.CALIBRATION_IMU) {
			processMAVMessage(drone.calibrationSetup.getMessage());
		}
	}

	private void processMAVMessage(String message) {
		Log.d("CAL", message);
		if(message.contains("level"))
			calibration_step = 1;
		else if(message.contains("LEFT"))
			calibration_step = 2;
		else if(message.contains("RIGHT"))
			calibration_step = 3;
		else if(message.contains("DOWN"))
			calibration_step = 4;
		else if(message.contains("UP"))
			calibration_step = 5;
		else if(message.contains("BACK"))
			calibration_step = 6;
		else if(message.contains("offset")){
			offset = message;
			return;
		}
		else if(message.contains("scaling")){
			scaling = message;
			return;
		}
		else if(message.contains("Calibration")){
			calibration_step=7;
				msg = message;
		}
		
		
		
		((FragmentSetupIMUCalibrate) sidePanel).updateTitle(calibration_step);
	}
}
