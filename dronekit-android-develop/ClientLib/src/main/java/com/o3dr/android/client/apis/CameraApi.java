package com.o3dr.android.client.apis;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Surface;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.action.CameraActions.ACTION_START_VIDEO_STREAM;
import static com.o3dr.services.android.lib.drone.action.CameraActions.ACTION_STOP_VIDEO_STREAM;
import static com.o3dr.services.android.lib.drone.action.CameraActions.EXTRA_VIDEO_DISPLAY;
import static com.o3dr.services.android.lib.drone.action.CameraActions.EXTRA_VIDEO_ENABLE_LOCAL_RECORDING;
import static com.o3dr.services.android.lib.drone.action.CameraActions.EXTRA_VIDEO_LOCAL_RECORDING_FILENAME;
import static com.o3dr.services.android.lib.drone.action.CameraActions.EXTRA_VIDEO_PROPS_UDP_PORT;
import static com.o3dr.services.android.lib.drone.action.CameraActions.EXTRA_VIDEO_TAG;
import static com.o3dr.services.android.lib.drone.action.CameraActions.EXTRA_VIDEO_PROPERTIES;

/**
 * Provides support to control generic camera functionality
 * Created by Fredia Huya-Kouadio on 10/11/15.
 *
 * @since 2.6.8
 */
public class CameraApi extends Api {
    private static final ConcurrentHashMap<Drone, CameraApi> apiCache = new ConcurrentHashMap<>();
    private static final Builder<CameraApi> apiBuilder = new Builder<CameraApi>() {
        @Override
        public CameraApi build(Drone drone) {
            return new CameraApi(drone);
        }
    };

    /**
     * Used to specify the udp port from which to access the streamed video.
     */
    public static final String VIDEO_PROPS_UDP_PORT = EXTRA_VIDEO_PROPS_UDP_PORT;

    /**
     * Key to specify whether to enable/disable local recording of the video stream.
     * @since 2.7.0
     */
    public static final String VIDEO_ENABLE_LOCAL_RECORDING = EXTRA_VIDEO_ENABLE_LOCAL_RECORDING;

    /**
     * Key to specify the filename to use for the local recording.
     * @since 2.7.0
     */
    public static final String VIDEO_LOCAL_RECORDING_FILENAME = EXTRA_VIDEO_LOCAL_RECORDING_FILENAME;

    /**
     * Retrieves a camera api instance
     *
     * @param drone
     * @return
     */
    public static CameraApi getApi(final Drone drone) {
        return getApi(drone, apiCache, apiBuilder);
    }

    private final Drone drone;

    private CameraApi(Drone drone) {
        this.drone = drone;
    }

    /**
     * Attempt to grab ownership and start the video stream from the connected drone. Can fail if
     * the video stream is already owned by another client.
     *
     * @param surface       Surface object onto which the video is decoded.
     * @param tag           Video tag.
     * @param videoProps    Non-null video properties. @see VIDEO_PROPS_UDP_PORT
     * @param listener      Register a callback to receive update of the command execution status.
     * @since 2.6.8
     */
    public void startVideoStream(@NonNull final Surface surface, final String tag,
                                 @NonNull Bundle videoProps, final AbstractCommandListener listener) {
        if (surface == null || videoProps == null) {
            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
            return;
        }

        final Bundle params = new Bundle();
        params.putParcelable(EXTRA_VIDEO_DISPLAY, surface);
        params.putString(EXTRA_VIDEO_TAG, tag);
        params.putBundle(EXTRA_VIDEO_PROPERTIES, videoProps);

        drone.performAsyncActionOnDroneThread(new Action(ACTION_START_VIDEO_STREAM, params), listener);
    }

    /**
     * Stop the video stream from the connected drone, and release ownership.
     *
     * @param tag      Video tag.
     * @param listener Register a callback to receive update of the command execution status.
     * @since 2.6.8
     */
    public void stopVideoStream(final String tag, final AbstractCommandListener listener) {
        final Bundle params = new Bundle();
        params.putString(EXTRA_VIDEO_TAG, tag);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_STOP_VIDEO_STREAM, params), listener);
    }
}
