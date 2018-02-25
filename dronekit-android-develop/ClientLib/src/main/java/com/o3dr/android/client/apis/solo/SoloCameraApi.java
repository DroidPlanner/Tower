package com.o3dr.android.client.apis.solo;

import android.os.Bundle;
import android.view.Surface;

import com.MAVLink.enums.GOPRO_COMMAND;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.CameraApi;
import com.o3dr.android.client.apis.CapabilityApi;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproConstants;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproRecord;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproSetExtendedRequest;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproSetRequest;
import com.o3dr.services.android.lib.model.AbstractCommandListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides access to the solo video specific functionality
 * Created by Fredia Huya-Kouadio on 7/12/15.
 *
 * @since 2.5.0
 */
public class SoloCameraApi extends SoloApi {
    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);

    private static final ConcurrentHashMap<Drone, SoloCameraApi> soloCameraApiCache = new ConcurrentHashMap<>();
    private static final Builder<SoloCameraApi> apiBuilder = new Builder<SoloCameraApi>() {
        @Override
        public SoloCameraApi build(Drone drone) {
            return new SoloCameraApi(drone);
        }
    };

    private static final int SOLO_STREAM_UDP_PORT = 5600;

    /**
     * Retrieves a sololink api instance.
     *
     * @param drone target vehicle
     * @return a SoloCameraApi instance.
     */
    public static SoloCameraApi getApi(final Drone drone) {
        return getApi(drone, soloCameraApiCache, apiBuilder);
    }

    private final CapabilityApi capabilityChecker;
    private final CameraApi cameraApi;

    private SoloCameraApi(Drone drone) {
        super(drone);
        this.capabilityChecker = CapabilityApi.getApi(drone);
        this.cameraApi = CameraApi.getApi(drone);
    }

    /**
     * Take a photo with the connected gopro.
     *
     * @param listener Register a callback to receive update of the command execution status.
     * @since 2.5.0
     */
    public void takePhoto(final AbstractCommandListener listener) {
        //Set the gopro to photo mode
        switchCameraCaptureMode(SoloGoproConstants.CAPTURE_MODE_PHOTO, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                //Send the command to take a picture.
                final SoloGoproRecord photoRecord = new SoloGoproRecord(SoloGoproConstants.START_RECORDING);
                sendMessage(photoRecord, listener);
            }

            @Override
            public void onError(int executionError) {
                if (listener != null) {
                    listener.onError(executionError);
                }
            }

            @Override
            public void onTimeout() {
                if (listener != null) {
                    listener.onTimeout();
                }
            }
        });
    }

    /**
     * Toggle video recording on the connected gopro.
     *
     * @param listener Register a callback to receive update of the command execution status.
     * @since 2.5.0
     */
    public void toggleVideoRecording(final AbstractCommandListener listener) {
        sendVideoRecordingCommand(SoloGoproConstants.TOGGLE_RECORDING, listener);
    }

    /**
     * Starts video recording on the connected gopro.
     *
     * @param listener Register a callback to receive update of the command execution status.
     * @since 2.5.0
     */
    public void startVideoRecording(final AbstractCommandListener listener) {
        sendVideoRecordingCommand(SoloGoproConstants.START_RECORDING, listener);
    }

    /**
     * Stops video recording on the connected gopro.
     *
     * @param listener Register a callback to receive update of the command execution status.
     * @since 2.5.0
     */
    public void stopVideoRecording(final AbstractCommandListener listener) {
        sendVideoRecordingCommand(SoloGoproConstants.STOP_RECORDING, listener);
    }

    private void sendVideoRecordingCommand(@SoloGoproConstants.RecordCommand final int recordCommand,
                                           final AbstractCommandListener listener) {
        //Set the gopro to video mode
        switchCameraCaptureMode(SoloGoproConstants.CAPTURE_MODE_VIDEO, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                //Send the command to toggle video recording
                final SoloGoproRecord videoToggle = new SoloGoproRecord(recordCommand);
                sendMessage(videoToggle, listener);
            }

            @Override
            public void onError(int executionError) {
                if (listener != null) {
                    listener.onError(executionError);
                }
            }

            @Override
            public void onTimeout() {
                if (listener != null) {
                    listener.onTimeout();
                }
            }
        });
    }

    public void startVideoStream(final Surface surface, final String tag,
                                 final AbstractCommandListener listener) {
        startVideoStream(surface, tag, false, listener);
    }

    /**
     * Attempt to grab ownership and start the video stream from the connected drone. Can fail if
     * the video stream is already owned by another client.
     *
     * @param surface  Surface object onto which the video is decoded.
     * @param tag      Video tag.
     * @param enableLocalRecording  Set to true to enable local recording, false to disable it.
     * @param listener Register a callback to receive update of the command execution status.
     *
     * @since 2.5.0
     */
    public void startVideoStream(final Surface surface, final String tag, final boolean enableLocalRecording,
                                 final AbstractCommandListener listener) {
        if (surface == null) {
            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
            return;
        }

        capabilityChecker.checkFeatureSupport(CapabilityApi.FeatureIds.SOLO_VIDEO_STREAMING,
            new CapabilityApi.FeatureSupportListener() {
                @Override
                public void onFeatureSupportResult(String featureId, int result, Bundle resultInfo) {
                    switch (result) {

                        case CapabilityApi.FEATURE_SUPPORTED:
                            final Bundle videoProps = new Bundle();
                            videoProps.putInt(CameraApi.VIDEO_PROPS_UDP_PORT, SOLO_STREAM_UDP_PORT);

                            videoProps.putBoolean(CameraApi.VIDEO_ENABLE_LOCAL_RECORDING, enableLocalRecording);
                            if(enableLocalRecording){
                                String localRecordingFilename = "solo_stream_" + FILE_DATE_FORMAT.format(new Date());
                                videoProps.putString(CameraApi.VIDEO_LOCAL_RECORDING_FILENAME, localRecordingFilename);
                            }

                            cameraApi.startVideoStream(surface, tag, videoProps, listener);
                            break;

                        case CapabilityApi.FEATURE_UNSUPPORTED:
                            postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                            break;

                        default:
                            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                            break;
                    }
                }
            });
    }

    /**
     * Attempt to grab ownership and start the video stream from the connected drone. Can fail if
     * the video stream is already owned by another client.
     *
     * @param surface  Surface object onto which the video is decoded.
     * @param listener Register a callback to receive update of the command execution status.
     *
     * @since 2.5.0
     */
    public void startVideoStream(final Surface surface, final AbstractCommandListener listener) {
        startVideoStream(surface, "", listener);
    }

    /**
     * Stop the video stream from the connected drone, and release ownership.
     *
     * @param listener Register a callback to receive update of the command execution status.
     *
     * @since 2.5.0
     */
    public void stopVideoStream(final AbstractCommandListener listener) {
        stopVideoStream("", listener);
    }

    /**
     * Stop the video stream from the connected drone, and release ownership.
     *
     * @param tag      Video tag.
     * @param listener Register a callback to receive update of the command execution status.
     *
     * @since 2.5.0
     */
    public void stopVideoStream(final String tag, final AbstractCommandListener listener) {
        capabilityChecker.checkFeatureSupport(CapabilityApi.FeatureIds.SOLO_VIDEO_STREAMING,
            new CapabilityApi.FeatureSupportListener() {
                @Override
                public void onFeatureSupportResult(String featureId, int result, Bundle resultInfo) {
                    switch (result) {

                        case CapabilityApi.FEATURE_SUPPORTED:
                            cameraApi.stopVideoStream(tag, listener);
                            break;

                        case CapabilityApi.FEATURE_UNSUPPORTED:
                            postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                            break;

                        default:
                            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                            break;
                    }
                }
            });
    }

    /**
     * Switches the camera capture mode.
     *
     * @param captureMode One of {@link SoloGoproConstants#CAPTURE_MODE_VIDEO},
     *                    {@link SoloGoproConstants#CAPTURE_MODE_PHOTO},
     *                    {@link SoloGoproConstants#CAPTURE_MODE_BURST},
     *                    {@link SoloGoproConstants#CAPTURE_MODE_TIME_LAPSE}
     * @param listener    Register a callback to receive update of the command execution status.
     * @since 2.6.8
     */
    public void switchCameraCaptureMode(@SoloGoproConstants.CaptureMode byte captureMode,
                                        final AbstractCommandListener listener) {
        final SoloGoproSetRequest captureModeRequest =
            new SoloGoproSetRequest((short) GOPRO_COMMAND.GOPRO_COMMAND_CAPTURE_MODE, captureMode);
        sendMessage(captureModeRequest, listener);
    }

    private void sendExtendedRequest(AbstractCommandListener listener, int command, byte value1,
                                     byte value2, byte value3, byte value4){
        byte[] values = {value1, value2, value3, value4};
        SoloGoproSetExtendedRequest extendedRequest = new SoloGoproSetExtendedRequest((short) command, values);
        sendMessage(extendedRequest, listener);
    }

    private void sendExtendedRequest(AbstractCommandListener listener, int command, byte value) {
        sendExtendedRequest(listener, command, value, (byte) 0, (byte) 0, (byte) 0);
    }

    /**
     * Updates the camera video settings.
     * @since 2.7.0
     * @param resolution
     * @param frameRate
     * @param fieldOfView
     * @param flags
     */
    public void updateVideoSettings(byte resolution, byte frameRate, byte fieldOfView, byte flags,
                                    AbstractCommandListener listener){
        sendExtendedRequest(listener, GOPRO_COMMAND.GOPRO_COMMAND_VIDEO_SETTINGS, resolution, frameRate,
            fieldOfView, flags);
    }

    public void setCameraPhotoResolution(byte photoResolution, AbstractCommandListener listener){
        sendExtendedRequest(listener, GOPRO_COMMAND.GOPRO_COMMAND_PHOTO_RESOLUTION, photoResolution);
    }

    public void enableCameraLowLight(boolean enable, AbstractCommandListener listener){
        sendExtendedRequest(listener, GOPRO_COMMAND.GOPRO_COMMAND_LOW_LIGHT, enable ? (byte) 1 : (byte) 0);
    }

    public void setCameraPhotoBurstRate(byte burstRate, AbstractCommandListener listener){
        sendExtendedRequest(listener, GOPRO_COMMAND.GOPRO_COMMAND_PHOTO_BURST_RATE, burstRate);
    }

    public void enableCameraProtune(boolean enable, AbstractCommandListener listener){
        sendExtendedRequest(listener, GOPRO_COMMAND.GOPRO_COMMAND_PROTUNE, enable ? (byte)1 : (byte) 0);
    }

    public void setCameraProtuneWhiteBalance(byte whiteBalance, AbstractCommandListener listener) {
        sendExtendedRequest(listener, GOPRO_COMMAND.GOPRO_COMMAND_PROTUNE_WHITE_BALANCE, whiteBalance);
    }

    public void setCameraProtuneColour(byte colour, AbstractCommandListener listener) {
        sendExtendedRequest(listener, GOPRO_COMMAND.GOPRO_COMMAND_PROTUNE_COLOUR, colour);
    }

    public void setCameraProtuneGain(byte gain, AbstractCommandListener listener) {
        sendExtendedRequest(listener, GOPRO_COMMAND.GOPRO_COMMAND_PROTUNE_GAIN, gain);
    }

    public void setCameraProtuneSharpness(byte sharpness, AbstractCommandListener listener) {
        sendExtendedRequest(listener, GOPRO_COMMAND.GOPRO_COMMAND_PROTUNE_SHARPNESS, sharpness);
    }

    public void setCameraProtuneExposure(byte exposure, AbstractCommandListener listener) {
        sendExtendedRequest(listener, GOPRO_COMMAND.GOPRO_COMMAND_PROTUNE_EXPOSURE, exposure);
    }
}