package org.droidplanner.android.fragments.calibration.imu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.droidplanner.R;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.notifications.TTSNotificationProvider;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.variables.Calibration;
import org.droidplanner.core.model.Drone;

public class FragmentSetupIMU extends ApiListenerFragment implements OnDroneListener {

	private final static long TIMEOUT_MAX = 30000l; //ms
    private final static long UPDATE_TIMEOUT_PERIOD = 100l; //ms
    private static final String EXTRA_UPDATE_TIMESTAMP = "extra_update_timestamp";

    private String msg;

    private long updateTimestamp;

	private int calibration_step = 0;
	private TextView textViewStep;
	private TextView textViewOffset;
	private TextView textViewScaling;
	private TextView textViewTimeOut;
	private ProgressBar pbTimeOut;
	private String timeLeftStr;
	private Drawable drawableGood, drawableWarning, drawablePoor;

	private final Handler handler = new Handler();

    private Button btnStep;
    private TextView textDesc;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_setup_imu_main, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		textViewStep = (TextView) view.findViewById(R.id.textViewIMUStep);
		textViewOffset = (TextView) view.findViewById(R.id.TextViewIMUOffset);
		textViewScaling = (TextView) view.findViewById(R.id.TextViewIMUScaling);
		textViewTimeOut = (TextView) view.findViewById(R.id.textViewIMUTimeOut);
		pbTimeOut = (ProgressBar) view.findViewById(R.id.progressBarTimeOut);

        textDesc = (TextView) view.findViewById(R.id.textViewDesc);

        btnStep = (Button) view.findViewById(R.id.buttonStep);
        btnStep.setEnabled(false);
        btnStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processCalibrationStep(calibration_step);
            }
        });

		pbTimeOut.setVisibility(View.INVISIBLE);
		textViewTimeOut.setVisibility(View.INVISIBLE);
		textViewOffset.setVisibility(View.INVISIBLE);
		textViewScaling.setVisibility(View.INVISIBLE);
		timeLeftStr = getString(R.string.setup_imu_timeleft);

		drawableGood = getResources().getDrawable(R.drawable.pstate_good);
		drawableWarning = getResources().getDrawable(R.drawable.pstate_warning);
		drawablePoor = getResources().getDrawable(R.drawable.pstate_poor);

        if(savedInstanceState != null){
            updateTimestamp = savedInstanceState.getLong(EXTRA_UPDATE_TIMESTAMP);
        }
	}

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_UPDATE_TIMESTAMP, updateTimestamp);
    }

    private void resetCalibration(){
        calibration_step = 0;
        updateDescription(calibration_step);
    }

    @Override
    public void onApiConnected(DroidPlannerApi api) {
        final Drone drone = api.getDrone();
        if (drone != null && api.isConnected() && !api.getState().isFlying()) {
            btnStep.setEnabled(true);
            if (drone.getCalibrationSetup().isCalibrating()) {
                processMAVMessage(drone.getCalibrationSetup().getMessage(), false);
            }
            else{
                resetCalibration();
            }
        } else {
            btnStep.setEnabled(false);
            resetCalibration();
        }

        api.addDroneListener(this);
    }

    @Override
    public void onApiDisconnected() {
            getApi().removeDroneListener(this);
    }

	private void processCalibrationStep(int step) {
		if (step == 0) {
			startCalibration();
            updateTimestamp = System.currentTimeMillis();
		} else if (step > 0 && step < 7) {
			sendAck(step);
		} else {
			calibration_step = 0;

            textViewStep.setText(R.string.setup_imu_step);

            textViewOffset.setVisibility(View.INVISIBLE);
            textViewScaling.setVisibility(View.INVISIBLE);

            updateDescription(calibration_step);
		}
	}

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

        if (calibration_step == 7 || calibration_step == 0) {
            handler.removeCallbacks(runnable);

            pbTimeOut.setVisibility(View.INVISIBLE);
            textViewTimeOut.setVisibility(View.INVISIBLE);
        } else {
            handler.removeCallbacks(runnable);

            textViewTimeOut.setVisibility(View.VISIBLE);
            pbTimeOut.setIndeterminate(true);
            pbTimeOut.setVisibility(View.VISIBLE);
            handler.postDelayed(runnable, UPDATE_TIMEOUT_PERIOD);
        }
    }

	private void sendAck(int step) {
        DroidPlannerApi dpApi = getApi();
		if (dpApi != null) {
			dpApi.getDrone().getCalibrationSetup().sendAckk(step);
		}
	}

	private void startCalibration() {
        DroidPlannerApi dpApi = getApi();
		if (dpApi != null) {
			boolean isCalibrating = dpApi.getDrone().getCalibrationSetup().startCalibration();
            if(!isCalibrating){
                Toast.makeText(getActivity(), R.string.failed_start_calibration_message,
                        Toast.LENGTH_LONG).show();
            }
		}
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
        switch(event){
            case CALIBRATION_IMU:
                processMAVMessage(drone.getCalibrationSetup().getMessage(), true);
                break;

            case CONNECTED:
                if(calibration_step == 0) {
                    //Reset the screen, and enable the calibration button
                    resetCalibration();
                    btnStep.setEnabled(true);
                }
                break;

            case DISCONNECTED:
                //Reset the screen, and disable the calibration button
                btnStep.setEnabled(false);
                resetCalibration();
                break;

            case CALIBRATION_TIMEOUT:
                if (drone != null) {
				/*
				 * here we will check if we are in calibration mode but if at
				 * the same time 'msg' is empty - then it is actually not doing
				 * calibration what we should do is to reset the calibration
				 * flag and re-trigger the HEARBEAT_TIMEOUT this however should
				 * not be happening
				 */
                    final Calibration calibration = drone.getCalibrationSetup();
                    if (calibration.isCalibrating() && TextUtils.isEmpty(msg)) {
                        calibration.setCalibrating(false);
                        drone.notifyDroneEvent(DroneEventsType.HEARTBEAT_TIMEOUT);
                    } else {
                        relayInstructions(msg);
                    }
                }
                break;
        }
	}

	private void processMAVMessage(String message, boolean updateTime) {
		if (message.contains("Place") || message.contains("Calibration")) {
            if(updateTime) {
                updateTimestamp = System.currentTimeMillis();
            }

            processOrientation(message);
        }
		else if (message.contains("Offsets")) {
            textViewOffset.setVisibility(View.VISIBLE);
			textViewOffset.setText(message);
		} else if (message.contains("Scaling")) {
            textViewScaling.setVisibility(View.VISIBLE);
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
        relayInstructions(msg);

		textViewStep.setText(msg);

		updateDescription(calibration_step);
	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
            handler.removeCallbacks(this);
			updateTimeOutProgress();
			handler.postDelayed(this, UPDATE_TIMEOUT_PERIOD);
		}
	};

    private void relayInstructions(String instructions){
        final Activity activity = getActivity();
        if(activity == null) return;

        final Context context = activity.getApplicationContext();

        LocalBroadcastManager.getInstance(context).sendBroadcast(new
                Intent(TTSNotificationProvider.ACTION_SPEAK_MESSAGE).putExtra
                (TTSNotificationProvider.EXTRA_MESSAGE_TO_SPEAK, instructions));

        Toast.makeText(context, instructions, Toast.LENGTH_LONG).show();
    }

	protected void updateTimeOutProgress() {
        final long timeElapsed = System.currentTimeMillis() - updateTimestamp;
		long timeLeft = (int) (TIMEOUT_MAX - timeElapsed);

		if (timeLeft >= 0) {
			int secLeft = (int) (timeLeft / 1000) + 1;

            pbTimeOut.setIndeterminate(false);
			pbTimeOut.setMax((int) TIMEOUT_MAX);
			pbTimeOut.setProgress((int) timeLeft);

			textViewTimeOut.setText(timeLeftStr + String.valueOf(secLeft) + "s");
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

	public static CharSequence getTitle(Context context) {
		return context.getText(R.string.setup_imu_title);
	}
}
