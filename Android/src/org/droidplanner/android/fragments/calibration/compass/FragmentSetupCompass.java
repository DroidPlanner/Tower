package org.droidplanner.android.fragments.calibration.compass;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.CalibrationApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationProgress;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationResult;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationStatus;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.ConfigurationActivity;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.utils.sound.SoundManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by fredia on 5/22/16.
 */
public class FragmentSetupCompass extends ApiListenerFragment {

    private static final String EXTRA_CALIBRATION_STEP = "extra_calibration_step";

    private static final int MAX_PROGRESS = 100;
    private static final String COMPASS_CAL_STARTED = "Compass Calibration Started";
    private static final String COMPASS_CAL_COMPLETED = "Compass Calibration Completed";
    private static final String COMPASS_CAL_FAILED = "Compass Calibration Failed";

    private static final long TIMEOUT_PERIOD = 30000l; //30 seconds

    @IntDef({STEP_BEGIN_CALIBRATION, STEP_CALIBRATION_WAITING_TO_START, STEP_CALIBRATION_STARTED,
        STEP_CALIBRATION_SUCCESSFUL, STEP_CALIBRATION_FAILED, STEP_CALIBRATION_CANCELLED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CompassCalibrationStep {
    }

    private static final int STEP_BEGIN_CALIBRATION = 0;
    private static final int STEP_CALIBRATION_WAITING_TO_START = 1;
    private static final int STEP_CALIBRATION_STARTED = 2;
    private static final int STEP_CALIBRATION_SUCCESSFUL = 3;
    private static final int STEP_CALIBRATION_FAILED = 4;
    private static final int STEP_CALIBRATION_CANCELLED = 5;

    private static final IntentFilter filter = new IntentFilter();

    static {
        filter.addAction(AttributeEvent.CALIBRATION_MAG_CANCELLED);
        filter.addAction(AttributeEvent.CALIBRATION_MAG_COMPLETED);
        filter.addAction(AttributeEvent.CALIBRATION_MAG_PROGRESS);

        filter.addAction(AttributeEvent.STATE_CONNECTED);
        filter.addAction(AttributeEvent.STATE_DISCONNECTED);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AttributeEvent.CALIBRATION_MAG_CANCELLED:
                    updateUI(STEP_CALIBRATION_CANCELLED);
                    stopTimeout();
                    break;

                case AttributeEvent.CALIBRATION_MAG_COMPLETED:
                    final MagnetometerCalibrationResult result = intent.getParcelableExtra(AttributeEventExtra
                        .EXTRA_CALIBRATION_MAG_RESULT);
                    handleMagResult(result);
                    stopTimeout();
                    break;

                case AttributeEvent.CALIBRATION_MAG_PROGRESS:
                    final MagnetometerCalibrationProgress progress = intent.getParcelableExtra(AttributeEventExtra
                        .EXTRA_CALIBRATION_MAG_PROGRESS);
                    handleMagProgress(progress);
                    break;

                case AttributeEvent.STATE_CONNECTED:
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                    cancelCalibration();
                    break;
            }
        }
    };

    private final Runnable stopCalibrationTask = new Runnable() {
        @Override
        public void run() {
            cancelCalibration();
        }
    };

    private final SparseArray<MagCalibrationStatus> calibrationTracker = new SparseArray<>();

    private final Handler handler = new Handler();

    private ConfigurationActivity parentActivity;

    @CompassCalibrationStep
    private int calibrationStep;

    private ProgressBar calibrationProgress;

    private View instructionsContainer;
    private TextView calibrationInstructions;
    private ImageView calibrationImage;

    private VideoView calibrationVideo;

    private TextView calibrationButton;
    private View advicesContainer;

    private MenuItem cancelMenuItem;
    private boolean isCancelMenuEnabled = false;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(!(activity instanceof ConfigurationActivity)){
            throw new IllegalStateException("Parent activity must be an instance of " + ConfigurationActivity.class.getName());
        }

        parentActivity = (ConfigurationActivity) activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        parentActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_setup_compass, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        calibrationProgress = (ProgressBar) view.findViewById(R.id.compass_calibration_progress);
        instructionsContainer = view.findViewById(R.id.compass_calibration_instructions_container);
        calibrationInstructions = (TextView) view.findViewById(R.id.compass_calibration_instructions);
        calibrationImage = (ImageView) view.findViewById(R.id.compass_calibration_image);

        calibrationVideo = (VideoView) view.findViewById(R.id.compass_calibration_video);
        calibrationVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        calibrationVideo.setVideoURI(Uri.parse("android.resource://" + getContext().getPackageName() + "/" +
            R.raw.compass_cal_white));

        calibrationButton = (TextView) view.findViewById(R.id.compass_calibration_button);
        calibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedWithCalibration(calibrationStep);
            }
        });

        advicesContainer = view.findViewById(R.id.compass_calibration_advices_container);

        @CompassCalibrationStep int currentStep = savedInstanceState == null
            ? STEP_BEGIN_CALIBRATION
            : savedInstanceState.getInt(EXTRA_CALIBRATION_STEP, STEP_BEGIN_CALIBRATION);

        calibrationStep = currentStep;
    }

    @Override
    public void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        outstate.putInt(EXTRA_CALIBRATION_STEP, calibrationStep);
    }

    @Override
    public void onApiConnected() {
        final Drone drone = getDrone();
        final MagnetometerCalibrationStatus calibrationStatus = drone.getAttribute(AttributeType.MAGNETOMETER_CALIBRATION_STATUS);
        if (calibrationStatus == null || calibrationStatus.isCalibrationCancelled()) {
            updateUI(STEP_CALIBRATION_CANCELLED);
        } else {
            updateUI(calibrationStep, true);
            final List<Integer> compassIds = calibrationStatus.getCompassIds();
            for (Integer compassId : compassIds)
                handleMagProgress(calibrationStatus.getCalibrationProgress(compassId));

            for (Integer compassId : compassIds)
                handleMagResult(calibrationStatus.getCalibrationResult(compassId));
        }

        getBroadcastManager().registerReceiver(receiver, filter);
    }

    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(receiver);
        if(parentActivity.isFinishing()
            || !parentActivity.hasWindowFocus()
            || parentActivity.getCurrentFragment() != this){
            cancelCalibration();
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_solo_mag_calibration, menu);
        cancelMenuItem = menu.findItem(R.id.solo_mag_cal_cancel);
        cancelMenuItem.setVisible(isCancelMenuEnabled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.solo_mag_cal_cancel:
                cancelCalibration();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void restartTimeout(){
        stopTimeout();
        handler.postDelayed(stopCalibrationTask, TIMEOUT_PERIOD);
    }

    private void stopTimeout(){
        handler.removeCallbacks(stopCalibrationTask);
    }

    private void handleMagProgress(MagnetometerCalibrationProgress progress) {
        if (progress == null)
            return;

        updateUI(STEP_CALIBRATION_STARTED);

        MagCalibrationStatus calStatus = calibrationTracker.get(progress.getCompassId());
        if (calStatus == null) {
            calStatus = new MagCalibrationStatus();
            calibrationTracker.append(progress.getCompassId(), calStatus);
        }

        calStatus.percentage = progress.getCompletionPercentage();

        int totalPercentage = 0;
        int calibrationsCount = calibrationTracker.size();
        for (int i = 0; i < calibrationsCount; i++) {
            totalPercentage += calibrationTracker.valueAt(i).percentage;
        }

        int calPercentage = calibrationsCount > 0 ? totalPercentage / calibrationsCount : 0;

        if (calibrationProgress.isIndeterminate()) {
            calibrationProgress.setIndeterminate(false);
            calibrationProgress.setMax(MAX_PROGRESS);
            calibrationProgress.setProgress(0);
        }

        if (calibrationProgress.getProgress() < calPercentage) {
            calibrationProgress.setProgress(calPercentage);
            restartTimeout();
        }
    }

    private void handleMagResult(MagnetometerCalibrationResult result) {
        if (result == null)
            return;

        MagCalibrationStatus reportStatus = calibrationTracker.get(result.getCompassId());
        if (reportStatus == null) {
            return;
        }

        reportStatus.percentage = 100;
        reportStatus.isComplete = true;
        reportStatus.isSuccessful = result.isCalibrationSuccessful();

        boolean areCalibrationsComplete = true;
        boolean areCalibrationsSuccessful = true;
        for (int i = 0; i < calibrationTracker.size(); i++) {
            final MagCalibrationStatus calStatus = calibrationTracker.valueAt(i);
            areCalibrationsComplete = areCalibrationsComplete && calStatus.isComplete;
            areCalibrationsSuccessful = areCalibrationsSuccessful && calStatus.isSuccessful;
        }

        if (areCalibrationsComplete) {
            if (areCalibrationsSuccessful)
                updateUI(STEP_CALIBRATION_SUCCESSFUL);
            else {
                updateUI(STEP_CALIBRATION_FAILED);
            }

            final Drone drone = getDrone();
            if (drone != null){
                CalibrationApi.getApi(drone).acceptMagnetometerCalibration();
            }
        }
    }

    private void cancelCalibration() {
        final Drone drone = getDrone();
        if (drone != null){
            CalibrationApi.getApi(getDrone()).cancelMagnetometerCalibration();
        }
    }

    private void proceedWithCalibration(@CompassCalibrationStep int step) {
        final Drone drone = getDrone();
        if(drone == null || !drone.isConnected()){
            //TODO: send a message to the notification handler for toast and voice
            Toast.makeText(getContext(), "Please connect drone before proceeding.", Toast.LENGTH_LONG).show();
            return;
        }

        switch (step) {
            case STEP_BEGIN_CALIBRATION:
            case STEP_CALIBRATION_FAILED:
            case STEP_CALIBRATION_CANCELLED:
                startCalibration();
                break;

            case STEP_CALIBRATION_SUCCESSFUL:
                startActivity(new Intent(getContext(), FlightActivity.class));
                break;

            case STEP_CALIBRATION_STARTED:
            case STEP_CALIBRATION_WAITING_TO_START:
            default:
                //nothing to do
                break;
        }
    }

    private void startCalibration() {
        CalibrationApi.getApi(getDrone()).startMagnetometerCalibration(false, false, 5);
        updateUI(STEP_CALIBRATION_WAITING_TO_START, true);
        restartTimeout();
    }

    private void updateUI(@CompassCalibrationStep int step) {
        updateUI(step, false);
    }

    private void updateUI(@CompassCalibrationStep int step, boolean force) {
        if(!isAdded())
            return;

        if (!force && step <= calibrationStep)
            return;

        calibrationStep = step;

        switch (step) {
            case STEP_BEGIN_CALIBRATION:
            case STEP_CALIBRATION_CANCELLED:
                enableCancelMenu(false);

                calibrationProgress.setVisibility(View.INVISIBLE);

                instructionsContainer.setVisibility(View.VISIBLE);
                calibrationInstructions.setText(R.string.instruction_compass_begin_calibration);
                calibrationImage.setImageLevel(0);
                calibrationImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

                calibrationVideo.setVisibility(View.GONE);

                calibrationButton.setVisibility(View.VISIBLE);
                calibrationButton.setTextColor(Color.WHITE);
                calibrationButton.setBackgroundResource(R.drawable.green_clickable_bg);

                advicesContainer.setVisibility(View.GONE);
                break;

            case STEP_CALIBRATION_STARTED:
                enableCancelMenu(true);

                if (!calibrationVideo.isPlaying())
                    calibrationVideo.start();

            case STEP_CALIBRATION_WAITING_TO_START:
                calibrationTracker.clear();

                calibrationVideo.setVisibility(View.VISIBLE);

                calibrationProgress.setVisibility(View.VISIBLE);
                calibrationProgress.setProgress(0);
                calibrationProgress.setIndeterminate(true);

                instructionsContainer.setVisibility(View.GONE);

                calibrationButton.setVisibility(View.GONE);

                advicesContainer.setVisibility(View.VISIBLE);

                break;

            case STEP_CALIBRATION_SUCCESSFUL:
                getSoundManager().play(SoundManager.UPDATE_SUCCESS);

                enableCancelMenu(false);

                calibrationProgress.setVisibility(View.VISIBLE);
                calibrationProgress.setIndeterminate(false);
                calibrationProgress.setMax(MAX_PROGRESS);
                calibrationProgress.setProgress(MAX_PROGRESS);

                instructionsContainer.setVisibility(View.VISIBLE);
                calibrationInstructions.setText(R.string.label_alright);
                calibrationImage.setImageLevel(1);
                calibrationImage.setScaleType(ImageView.ScaleType.CENTER);

                calibrationVideo.stopPlayback();
                calibrationVideo.setVisibility(View.GONE);

                calibrationButton.setVisibility(View.VISIBLE);
                calibrationButton.setBackgroundResource(R.drawable.settings_button_bg);
                calibrationButton.setTextColor(getResources().getColor(R.color.light_green));
                calibrationButton.setText(R.string.label_ready_to_fly);

                advicesContainer.setVisibility(View.GONE);
                break;

            case STEP_CALIBRATION_FAILED:
                enableCancelMenu(false);

                calibrationProgress.setVisibility(View.VISIBLE);
                calibrationProgress.setIndeterminate(false);
                calibrationProgress.setMax(MAX_PROGRESS);
                calibrationProgress.setProgress(MAX_PROGRESS);

                instructionsContainer.setVisibility(View.VISIBLE);
                calibrationInstructions.setText(R.string.label_compass_calibration_failed);
                calibrationImage.setImageLevel(2);
                calibrationImage.setScaleType(ImageView.ScaleType.CENTER);

                calibrationVideo.stopPlayback();
                calibrationVideo.setVisibility(View.GONE);

                calibrationButton.setVisibility(View.VISIBLE);
                calibrationButton.setBackgroundResource(R.drawable.settings_button_bg);
                calibrationButton.setTextColor(getResources().getColor(R.color.light_green));
                calibrationButton.setText(R.string.label_try_again);

                advicesContainer.setVisibility(View.GONE);
                break;
        }
    }

    private void enableCancelMenu(boolean enabled) {
        isCancelMenuEnabled = enabled;
        if (cancelMenuItem != null)
            cancelMenuItem.setVisible(enabled);
    }

    private static class MagCalibrationStatus {
        int percentage;
        boolean isComplete;
        boolean isSuccessful;
    }
}
