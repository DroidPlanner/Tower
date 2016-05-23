package org.droidplanner.android.fragments.calibration.compass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationProgress;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationResult;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by fredia on 5/22/16.
 */
public class FragmentSetupCompass extends ApiListenerFragment {

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

        updateUI(STEP_BEGIN_CALIBRATION);
    }

    @Override
    public void onApiConnected() {

    }

    @Override
    public void onApiDisconnected() {

    }

    public static CharSequence getTitle(Context context) {
        return context.getText(R.string.setup_mag_title);
    }

    private static class MagCalibrationStatus {
        int percentage;
        boolean isComplete;
        boolean isSuccessful;
    }
}
