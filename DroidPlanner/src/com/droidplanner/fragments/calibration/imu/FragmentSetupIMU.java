package com.droidplanner.fragments.calibration.imu;

import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.fragments.calibration.FragmentCalibration;
import com.droidplanner.fragments.calibration.FragmentSetupSidePanel;

import android.provider.CalendarContract.Reminders;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FragmentSetupIMU extends FragmentCalibration implements
		OnDroneListner {
	private static String msg;
	private static String offset;
	private static String scaling;
	boolean calibrationPassed;
	private static int calibration_step = 0;
	private static TextView textViewStep;
	private static TextView textViewOffset;
	private static TextView textViewScaling;
	private static TextView textViewTimeOut;
	private static ProgressBar pbTimeOut;


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
		textViewStep = (TextView) view.findViewById(R.id.textViewIMUStep);
		textViewOffset = (TextView) view.findViewById(R.id.TextViewIMUOffset);
		textViewScaling = (TextView) view.findViewById(R.id.TextViewIMUScaling);
		textViewTimeOut = (TextView) view.findViewById(R.id.textViewIMUTimeOut);
		pbTimeOut = (ProgressBar) view.findViewById(R.id.progressBarTimeOut);
		
		pbTimeOut.setVisibility(View.INVISIBLE);
		textViewTimeOut.setVisibility(View.INVISIBLE);
		textViewOffset.setVisibility(View.INVISIBLE);
		textViewScaling.setVisibility(View.INVISIBLE);
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
		else
		{
			calibration_step = 0;
			textViewStep.setText(R.string.setup_imu_step);
			((FragmentSetupIMUCalibrate) sidePanel).updateTitle(calibration_step);			
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
		else if(event == DroneEventsType.CALIBRATION_TIMEOUT){
			if(parent.getDrone()!=null){
				Log.d("CAL", "remind :" + msg);
				parent.getDrone().tts.speak(msg);
			}
		}
	}

	private void processMAVMessage(String message) {
		if(message.contains("Place")||message.contains("Calibration"))
			processOrientation(message);
		else if(message.contains("offset"))
			offset = message;
		else if(message.contains("scaling"))
			scaling = message;
	}

	private void processOrientation(String message) {
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
		else if(message.contains("Calibration"))
			calibration_step=7;
		
		msg = message.replace("any key.", "'Next'");
		
		textViewStep.setText(msg);
		
		((FragmentSetupIMUCalibrate) sidePanel).updateTitle(calibration_step);
	}
}
