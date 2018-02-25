package org.droidplanner.services.android.impl.utils;

import com.o3dr.services.android.lib.drone.attribute.error.ErrorType;

import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.impl.core.model.AutopilotWarningParser;

import java.util.Locale;

import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ALTITUDE_DISPARITY;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_COMPASS_CALIBRATION_RUNNING;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_GYRO_CALIBRATION_FAILED;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_LEANING;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_MODE_NOT_ARMABLE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_ROTOR_NOT_SPINNING;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_SAFETY_SWITCH;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_THROTTLE_BELOW_FAILSAFE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.ARM_THROTTLE_TOO_HIGH;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.AUTO_TUNE_FAILED;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.CRASH_DISARMING;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.EKF_VARIANCE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.LOW_BATTERY;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.NO_DATAFLASH_INSERTED;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.NO_ERROR;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PARACHUTE_TOO_LOW;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_ACCELEROMETERS_NOT_HEALTHY;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_ACRO_BAL_ROLL_PITCH;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_BAROMETER_NOT_HEALTHY;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_CHECK_ANGLE_MAX;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_CHECK_BOARD_VOLTAGE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_CHECK_FAILSAFE_THRESHOLD_VALUE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_CHECK_FENCE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_CHECK_MAGNETIC_FIELD;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_COMPASS_NOT_CALIBRATED;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_COMPASS_NOT_HEALTHY;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_COMPASS_OFFSETS_TOO_HIGH;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_DUPLICATE_AUX_SWITCH_OPTIONS;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_EKF_HOME_VARIANCE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_GPS_GLITCH;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_GYROS_NOT_HEALTHY;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_HIGH_GPS_HDOP;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_INCONSISTENT_ACCELEROMETERS;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_INCONSISTENT_COMPASSES;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_INCONSISTENT_GYROS;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_INS_NOT_CALIBRATED;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_NEED_GPS_LOCK;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.PRE_ARM_RC_NOT_CALIBRATED;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.RC_FAILSAFE;
import static com.o3dr.services.android.lib.drone.attribute.error.ErrorType.WAITING_FOR_NAVIGATION_ALIGNMENT;

/**
 * Autopilot error parser.
 * Created by fhuya on 12/16/14.
 */
public class AndroidApWarningParser implements AutopilotWarningParser {

    @Override
    public String getDefaultWarning() {
        return NO_ERROR.name();
    }

    /**
     * Maps the ArduPilot warnings set to the DroneKit-Android warnings set.
     *
     * @param warning warning originating from the ArduPilot autopilot
     * @return equivalent DroneKit-Android warning type
     */
    @Override
    public String parseWarning(MavLinkDrone drone, String warning) {
        if (android.text.TextUtils.isEmpty(warning))
            return null;

        ErrorType errorType = getErrorType(warning);
        if(errorType == null)
            return null;

        return errorType.name();
    }

    private ErrorType getErrorType(String warning){
        switch (warning.toLowerCase(Locale.US)) {
            case "arm: thr below fs":
            case "arm: throttle below failsafe":
                return ARM_THROTTLE_BELOW_FAILSAFE;

            case "arm: gyro calibration failed":
                return ARM_GYRO_CALIBRATION_FAILED;

            case "arm: mode not armable":
                return ARM_MODE_NOT_ARMABLE;

            case "arm: rotor not spinning":
                return ARM_ROTOR_NOT_SPINNING;

            case "arm: altitude disparity":
            case "prearm: altitude disparity":
                return ALTITUDE_DISPARITY;

            case "arm: leaning":
                return ARM_LEANING;

            case "arm: throttle too high":
                return ARM_THROTTLE_TOO_HIGH;

            case "arm: safety switch":
                return ARM_SAFETY_SWITCH;

            case "arm: compass calibration running":
                return ARM_COMPASS_CALIBRATION_RUNNING;


            case "prearm: rc not calibrated":
                return PRE_ARM_RC_NOT_CALIBRATED;

            case "prearm: barometer not healthy":
                return PRE_ARM_BAROMETER_NOT_HEALTHY;

            case "prearm: compass not healthy":
                return PRE_ARM_COMPASS_NOT_HEALTHY;

            case "prearm: compass not calibrated":
                return PRE_ARM_COMPASS_NOT_CALIBRATED;

            case "prearm: compass offsets too high":
                return PRE_ARM_COMPASS_OFFSETS_TOO_HIGH;

            case "prearm: check mag field":
                return PRE_ARM_CHECK_MAGNETIC_FIELD;

            case "prearm: inconsistent compasses":
                return PRE_ARM_INCONSISTENT_COMPASSES;

            case "prearm: check fence":
                return PRE_ARM_CHECK_FENCE;

            case "prearm: ins not calibrated":
                return PRE_ARM_INS_NOT_CALIBRATED;

            case "prearm: accelerometers not healthy":
                return PRE_ARM_ACCELEROMETERS_NOT_HEALTHY;

            case "prearm: inconsistent accelerometers":
                return PRE_ARM_INCONSISTENT_ACCELEROMETERS;

            case "prearm: gyros not healthy":
                return PRE_ARM_GYROS_NOT_HEALTHY;

            case "prearm: inconsistent gyros":
                return PRE_ARM_INCONSISTENT_GYROS;

            case "prearm: check board voltage":
                return PRE_ARM_CHECK_BOARD_VOLTAGE;

            case "prearm: duplicate aux switch options":
                return PRE_ARM_DUPLICATE_AUX_SWITCH_OPTIONS;

            case "prearm: check fs_thr_value":
                return PRE_ARM_CHECK_FAILSAFE_THRESHOLD_VALUE;

            case "prearm: check angle_max":
                return PRE_ARM_CHECK_ANGLE_MAX;

            case "prearm: acro_bal_roll/pitch":
                return PRE_ARM_ACRO_BAL_ROLL_PITCH;

            case "prearm: need 3d fix":
                return PRE_ARM_NEED_GPS_LOCK;

            case "prearm: ekf-home variance":
                return PRE_ARM_EKF_HOME_VARIANCE;

            case "prearm: high gps hdop":
                return PRE_ARM_HIGH_GPS_HDOP;

            case "prearm: gps glitch":
            case "prearm: bad velocity":
                return PRE_ARM_GPS_GLITCH;

            case "prearm: waiting for navigation alignment":
            case "arm: waiting for navigation alignment":
                return WAITING_FOR_NAVIGATION_ALIGNMENT;


            case "no dataflash inserted":
                return NO_DATAFLASH_INSERTED;

            case "low battery!":
                return LOW_BATTERY;

            case "autotune: failed":
                return AUTO_TUNE_FAILED;

            case "crash: disarming":
                return CRASH_DISARMING;

            case "parachute: too low":
                return PARACHUTE_TOO_LOW;

            case "ekf variance":
                return EKF_VARIANCE;

            case "rc failsafe":
                return RC_FAILSAFE;


            default:
                return null;
        }
    }
}
