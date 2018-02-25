package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Stores the gopro constants
 * Created by Fredia Huya-Kouadio on 10/15/15.
 *
 * @since 2.6.8
 */
public class SoloGoproConstants {

    //Private constructor to prevent instantiation.
    private SoloGoproConstants() {
    }

    @IntDef({STOP_RECORDING, START_RECORDING, TOGGLE_RECORDING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RecordCommand {
    }

    public static final int STOP_RECORDING = 0;
    public static final int START_RECORDING = 1;
    public static final int TOGGLE_RECORDING = 2;

    @IntDef({STATUS_NO_GOPRO, STATUS_INCOMPATIBLE_GOPRO, STATUS_GOPRO_CONNECTED, STATUS_ERROR_OVER_TEMPERATURE, STATUS_ERROR_NO_STORAGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GoproStatus {
    }

    public static final byte STATUS_NO_GOPRO = 0;
    public static final byte STATUS_INCOMPATIBLE_GOPRO = 1;
    public static final byte STATUS_GOPRO_CONNECTED = 2;
    public static final byte STATUS_ERROR_OVER_TEMPERATURE = 3;
    public static final byte STATUS_ERROR_NO_STORAGE = 4;


    @IntDef({RECORDING_OFF, RECORDING_ON})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RecordingStatus {
    }

    public static final byte RECORDING_OFF = 0;
    public static final byte RECORDING_ON = 1;


    @IntDef({CAPTURE_MODE_VIDEO, CAPTURE_MODE_PHOTO, CAPTURE_MODE_BURST, CAPTURE_MODE_TIME_LAPSE,
            CAPTURE_MODE_MULTI_SHOT, CAPTURE_MODE_PLAYBACK, CAPTURE_MODE_SETUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CaptureMode {
    }

    /**
     * Camera video mode.
     */
    public static final byte CAPTURE_MODE_VIDEO = 0;

    /**
     * Camera photo mode.
     */
    public static final byte CAPTURE_MODE_PHOTO = 1;

    /**
     * Camera photo burst mode.
     */
    public static final byte CAPTURE_MODE_BURST = 2;

    /**
     * Camera time lapse mode.
     */
    public static final byte CAPTURE_MODE_TIME_LAPSE = 3;

    public static final byte CAPTURE_MODE_MULTI_SHOT = 4;
    public static final byte CAPTURE_MODE_PLAYBACK = 5;
    public static final byte CAPTURE_MODE_SETUP = 6;
}
