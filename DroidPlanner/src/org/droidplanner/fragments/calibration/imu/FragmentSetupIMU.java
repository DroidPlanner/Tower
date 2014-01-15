package org.droidplanner.fragments.calibration.imu;

import org.droidplanner.activitys.ConfigurationActivity;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListner;
import org.droidplanner.drone.variables.Calibration;
import org.droidplanner.fragments.SetupFragment;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.droidplanner.R;

public class FragmentSetupIMU extends SetupFragment.SetupCalibration implements
		OnDroneListner {

	private final static int TIMEOUT_MAX = 300;

	private String msg;
	boolean calibrationPassed;
	private long timeCount, timeLeft;
	private int calibration_step = 0;
	private TextView textViewStep;
	private TextView textViewOffset;
	private TextView textViewScaling;
	private TextView textViewTimeOut;
	private ProgressBar pbTimeOut;
	private String timeLeftStr;
	private Drawable drawableGood, drawableWarning, drawablePoor;

	private final Handler handler = new Handler();
	private ConfigurationActivity parentActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof ConfigurationActivity)) {
			throw new IllegalStateException("Parent activity must be "
					+ ConfigurationActivity.class.getName());
		}

		parentActivity = (ConfigurationActivity) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstaneState) {
		final View view = inflater.inflate(R.layout.fragment_setup_imu_main,
				container, false);

		setupLocalViews(view);

		return view;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		parentActivity = null;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (parentActivity != null) {
			parentActivity.drone.events.removeDroneListener(this);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (parentActivity != null) {
			parentActivity.drone.events.addDroneListener(this);
		}
	}

	private void setupLocalViews(View view) {
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
	public SetupFragment.SetupSidePanel getSidePanel() {
		return new FragmentSetupIMUCalibrate();
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

			((SetupFragment) getParentFragment())
					.updateSidePanelTitle(calibration_step);
		}
	}

	private void sendAck(int step) {
		if (parentActivity.drone != null) {
			parentActivity.drone.calibrationSetup.sendAckk(step);
		}
	}

	private void startCalibration() {
		if (parentActivity.drone != null) {
			parentActivity.drone.calibrationSetup.startCalibration();
		}
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		if (event == DroneEventsType.CALIBRATION_IMU) {
			processMAVMessage(drone.calibrationSetup.getMessage());
		} else if (event == DroneEventsType.HEARTBEAT_TIMEOUT) {
			if (parentActivity.drone != null) {
				/* here we will check if we are in calibration mode
				 * but if at the same time 'msg' is empty - then it is actually not doing calibration
				 * what we should do is to reset the calibration flag and re-trigger the HEARBEAT_TIMEOUT
				 * this however should not be happening
				 */
				if (Calibration.isCalibrating() && msg.isEmpty()) {
					Calibration.setClibrating(false);
					parentActivity.drone.events
							.notifyDroneEvent(DroneEventsType.HEARTBEAT_TIMEOUT);
				} else {
					parentActivity.drone.tts.speak(msg);
				}
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

		((SetupFragment) getParentFragment())
				.updateSidePanelTitle(calibration_step);

		if (calibration_step == 7) {
			if (parentActivity != null && parentActivity.drone != null) {
				parentActivity.drone.tts.speak(msg);
			}
			handler.removeCallbacks(runnable);

			pbTimeOut.setVisibility(View.INVISIBLE);
			textViewTimeOut.setVisibility(View.INVISIBLE);
		} else {
			handler.removeCallbacks(runnable);
			timeCount = 0;
			textViewTimeOut.setVisibility(View.VISIBLE);
			pbTimeOut.setVisibility(View.VISIBLE);
			handler.postDelayed(runnable, 100);
		}
	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			updateTimeOutProgress();
			handler.postDelayed(this, 100);
		}
	};

	protected void updateTimeOutProgress() {
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
