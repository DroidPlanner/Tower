package com.droidplanner.fragments.calibration.imu;

import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.fragments.calibration.FragmentCalibration;
import com.droidplanner.fragments.calibration.FragmentSetupSidePanel;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FragmentSetupIMU extends FragmentCalibration implements
		OnDroneListner {
	private static String msg;
	private static int TIMEOUT_MAX = 300;
	boolean calibrationPassed;
	private static long timeCount, timeLeft;
	private static int calibration_step = 0;
	private static View view;
	private static TextView textViewStep;
	private static TextView textViewOffset;
	private static TextView textViewScaling;
	private static TextView textViewTimeOut;
	private static ProgressBar pbTimeOut;
	private static String timeLeftStr;
	private static Drawable drawableGood, drawableWarning, drawablePoor;
	private Handler h = new Handler();

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
		view = inflater.inflate(R.layout.fragment_setup_imu_main, container,
				false);
		return view;
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
		timeLeftStr = getResources().getString(R.string.setup_imu_timeleft);
		drawableGood = getResources().getDrawable(R.drawable.pstate_good);
		drawableWarning = getResources().getDrawable(R.drawable.pstate_warning);
		drawablePoor = getResources().getDrawable(R.drawable.pstate_poor);

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
		if (step == 0) {
			startCalibration();
			timeCount = 0;
		} else if (step > 0 && step < 7) {
			sendAck(step);
			if (step == 6) {
				textViewOffset.setVisibility(View.VISIBLE);
				textViewScaling.setVisibility(View.VISIBLE);
			}
		} else {
			calibration_step = 0;
			textViewStep.setText(R.string.setup_imu_step);

			textViewOffset.setVisibility(View.INVISIBLE);
			textViewScaling.setVisibility(View.INVISIBLE);

			((FragmentSetupIMUCalibrate) sidePanel)
					.updateTitle(calibration_step);
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
		} else if (event == DroneEventsType.CALIBRATION_TIMEOUT) {
			if (parent.getDrone() != null) {
				parent.getDrone().tts.speak(msg);
				//TODO: Check if msg is empty - reset the Calibrating isCalibrating flag
			}
		}
	}

	private void processMAVMessage(String message) {
		if (message.contains("Place") || message.contains("Calibration"))
			processOrientation(message);
		else if (message.contains("Offsets")) {
			textViewOffset.setText(message);
		} else if (message.contains("Scaling")) {
			textViewScaling.setText(message);
		}
	}

	private void processOrientation(String message) {
		if (message.contains("level"))
			calibration_step = 1;
		else if (message.contains("LEFT"))
			calibration_step = 2;
		else if (message.contains("RIGHT"))
			calibration_step = 3;
		else if (message.contains("DOWN"))
			calibration_step = 4;
		else if (message.contains("UP"))
			calibration_step = 5;
		else if (message.contains("BACK"))
			calibration_step = 6;
		else if (message.contains("Calibration"))
			calibration_step = 7;

		msg = message.replace("any key.", "'Next'");

		textViewStep.setText(msg);

		((FragmentSetupIMUCalibrate) sidePanel).updateTitle(calibration_step);

		if (calibration_step == 7) {
			if (parent != null && parent.getDrone() != null) {
				parent.getDrone().tts.speak(msg);
			}
			h.removeCallbacks(runnable);

			pbTimeOut.setVisibility(View.INVISIBLE);
			textViewTimeOut.setVisibility(View.INVISIBLE);
		} else {
			h.removeCallbacks(runnable);
			timeCount = 0;
			textViewTimeOut.setVisibility(View.VISIBLE);
			pbTimeOut.setVisibility(View.VISIBLE);
			h.postDelayed(runnable, 100);
		}
	}

	
	private Runnable runnable = new Runnable() {
		   @Override
		   public void run() {
			   updateTimeOutProgress();
		      h.postDelayed(this, 100);
		   }
		};
	protected static void updateTimeOutProgress() {
		timeLeft = (int) (TIMEOUT_MAX - timeCount);

		if (timeLeft >= 0) {
			timeCount++;
			int secLeft = (int) (timeLeft / 10) + 1;

			pbTimeOut.setMax(TIMEOUT_MAX);
			pbTimeOut.setProgress((int) timeLeft);
			textViewTimeOut
					.setText(timeLeftStr + String.valueOf(secLeft) + "s");
			if (secLeft > 15)
				pbTimeOut.setProgressDrawable(drawableGood);
			else if (secLeft <= 15 && secLeft > 5)
				pbTimeOut.setProgressDrawable(drawableWarning);
			else if (secLeft == 5)
				pbTimeOut.setProgressDrawable(drawablePoor);

		} else {
			textViewTimeOut.setText(timeLeftStr + "0s");

		}

	}
}
