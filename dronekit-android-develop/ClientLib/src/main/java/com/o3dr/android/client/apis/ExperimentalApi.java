package com.o3dr.android.client.apis;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.utils.connection.IpConnectionListener;
import com.o3dr.android.client.utils.connection.UdpConnection;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.action.ExperimentalActions;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.action.CameraActions.EXTRA_VIDEO_TAG;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_SEND_MAVLINK_MESSAGE;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_SET_RELAY;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_SET_ROI;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_SET_SERVO;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.ACTION_TRIGGER_CAMERA;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_IS_RELAY_ON;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_MAVLINK_MESSAGE;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_RELAY_NUMBER;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_SERVO_CHANNEL;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_SERVO_PWM;
import static com.o3dr.services.android.lib.drone.action.ExperimentalActions.EXTRA_SET_ROI_LAT_LONG_ALT;

/**
 * Contains drone commands with no defined interaction model yet.
 */
public class ExperimentalApi extends Api {
    private static final ConcurrentHashMap<Drone, ExperimentalApi> experimentalApiCache = new ConcurrentHashMap<>();
    private static final Builder<ExperimentalApi> apiBuilder = new Builder<ExperimentalApi>() {
        @Override
        public ExperimentalApi build(Drone drone) {
            return new ExperimentalApi(drone);
        }
    };

    private final CapabilityApi capabilityChecker;

    private final VideoStreamObserver videoStreamObserver;

    /**
     * Retrieves an ExperimentalApi instance.
     *
     * @param drone target vehicle.
     * @return a ExperimentalApi instance.
     */
    public static ExperimentalApi getApi(final Drone drone) {
        return getApi(drone, experimentalApiCache, apiBuilder);
    }

    private final Drone drone;

    private ExperimentalApi(Drone drone) {
        this.drone = drone;
        this.capabilityChecker = CapabilityApi.getApi(drone);

        videoStreamObserver = new VideoStreamObserver(drone.getHandler());
    }

    /**
     * Triggers the camera.
     */
    public void triggerCamera() {
        drone.performAsyncAction(new Action(ACTION_TRIGGER_CAMERA));
    }

    /**
     * Specify a region of interest for the vehicle to point at.
     *
     * @param roi Region of interest coordinate.
     */
    public void setROI(LatLongAlt roi) {
        setROI(roi, null);
    }

    /**
     * Specify a region of interest for the vehicle to point at.
     *
     * @param roi      Region of interest coordinate.
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void setROI(LatLongAlt roi, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_SET_ROI_LAT_LONG_ALT, roi);
        Action epmAction = new Action(ACTION_SET_ROI, params);
        drone.performAsyncActionOnDroneThread(epmAction, listener);
    }

    /**
     * This is an advanced/low-level method to send raw mavlink to the vehicle.
     * <p/>
     * This method is included as an ‘escape hatch’ to allow developers to make progress if we’ve
     * somehow missed providing some essential operation in the rest of this API. Callers do
     * not need to populate sysId/componentId/crc in the packet, this method will take care of that
     * before sending.
     * <p/>
     * If you find yourself needing to use this method please contact the drone-platform google
     * group and we’ll see if we can support the operation you needed in some future revision of
     * the API.
     *
     * @param messageWrapper A MAVLinkMessage wrapper instance. No need to fill in
     *                       sysId/compId/seqNum - the API will take care of that.
     */
    public void sendMavlinkMessage(MavlinkMessageWrapper messageWrapper) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_MAVLINK_MESSAGE, messageWrapper);
        drone.performAsyncAction(new Action(ACTION_SEND_MAVLINK_MESSAGE, params));
    }

    /**
     * Set a Relay pin’s voltage high or low
     *
     * @param relayNumber
     * @param enabled     true for relay to be on, false for relay to be off.
     */
    public void setRelay(int relayNumber, boolean enabled) {
        setRelay(relayNumber, enabled, null);
    }

    /**
     * Set a Relay pin’s voltage high or low
     *
     * @param relayNumber
     * @param enabled     true for relay to be on, false for relay to be off.
     * @param listener    Register a callback to receive update of the command execution state.
     */
    public void setRelay(int relayNumber, boolean enabled, AbstractCommandListener listener) {
        Bundle params = new Bundle(2);
        params.putInt(EXTRA_RELAY_NUMBER, relayNumber);
        params.putBoolean(EXTRA_IS_RELAY_ON, enabled);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_RELAY, params), listener);
    }

    /**
     * Move a servo to a particular pwm value
     *
     * @param channel the output channel the servo is attached to
     * @param pwm     PWM value to output to the servo. Servo’s generally accept pwm values between 1000 and 2000
     */
    public void setServo(int channel, int pwm) {
        setServo(channel, pwm, null);
    }

    /**
     * Move a servo to a particular pwm value
     *
     * @param channel  the output channel the servo is attached to
     * @param pwm      PWM value to output to the servo. Servo’s generally accept pwm values between 1000 and 2000
     * @param listener Register a callback to receive update of the command execution state.
     */
    public void setServo(int channel, int pwm, AbstractCommandListener listener) {
        Bundle params = new Bundle(2);
        params.putInt(EXTRA_SERVO_CHANNEL, channel);
        params.putInt(EXTRA_SERVO_PWM, pwm);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SET_SERVO, params), listener);
    }

    /**
     * Attempt to grab ownership and get a lock for the video stream. Can fail if
     * the video stream is already owned by another client.
     *
     * @param tag       Video tag.
     * @param callback  Video stream observer callback.
     *
     * @since 2.8.1
     */
    public void startVideoStream(final String tag, final IVideoStreamCallback callback) {
        if (callback == null) {
            throw new NullPointerException("Video stream callback can't be null");
        }

        capabilityChecker.checkFeatureSupport(CapabilityApi.FeatureIds.SOLO_VIDEO_STREAMING,
                new CapabilityApi.FeatureSupportListener() {
                    @Override
                    public void onFeatureSupportResult(String featureId, int result, Bundle resultInfo) {
                        final AbstractCommandListener listener = new AbstractCommandListener() {
                            @Override
                            public void onSuccess() {
                                // Start VideoStreamObserver to connect to vehicle video stream and receive
                                // video stream packets.
                                videoStreamObserver.setCallback(callback);
                                videoStreamObserver.start();

                                videoStreamObserver.getCallback().onVideoStreamConnecting();
                            }

                            @Override
                            public void onError(int executionError) {
                                videoStreamObserver.getCallback().onError(executionError);
                            }

                            @Override
                            public void onTimeout() {
                                videoStreamObserver.getCallback().onTimeout();
                            }
                        };

                        switch (result) {
                            case CapabilityApi.FEATURE_SUPPORTED:
                                startVideoStreamForObserver(tag, listener);
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
     * Release ownership of the video stream.
     *
     * @param tag   Video tag.
     *
     * @since 2.8.1
     */
    public void stopVideoStream(final String tag) {
        capabilityChecker.checkFeatureSupport(CapabilityApi.FeatureIds.SOLO_VIDEO_STREAMING,
                new CapabilityApi.FeatureSupportListener() {
                    @Override
                    public void onFeatureSupportResult(String featureId, int result, Bundle resultInfo) {
                        final AbstractCommandListener listener = new AbstractCommandListener() {
                            @Override
                            public void onSuccess() {
                                videoStreamObserver.getCallback().onVideoStreamDisconnecting();

                                videoStreamObserver.stop();
                            }

                            @Override
                            public void onError(int executionError) {
                                videoStreamObserver.getCallback().onError(executionError);
                            }

                            @Override
                            public void onTimeout() {
                                videoStreamObserver.getCallback().onTimeout();
                            }
                        };

                        switch (result) {
                            case CapabilityApi.FEATURE_SUPPORTED:
                                stopVideoStreamForObserver(tag, listener);
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
     * Prepending 'observer' to the tag for differentiation
     * @param tag
     * @return
     */
    private String getObserverTag(String tag){
        return "observer"  + (TextUtils.isEmpty(tag) ? "" : "." + tag);
    }

    /**
     * Attempt to grab ownership and start the video stream from the connected drone. Can fail if
     * the video stream is already owned by another client.
     *
     * @param tag       Video tag.
     * @param listener  Register a listener to receive update of the command execution status.
     * @since 2.8.1
     */
    private void startVideoStreamForObserver(final String tag, AbstractCommandListener listener) {
        final Bundle params = new Bundle();
        params.putString(EXTRA_VIDEO_TAG, getObserverTag(tag));

        drone.performAsyncActionOnDroneThread(new Action(ExperimentalActions.ACTION_START_VIDEO_STREAM_FOR_OBSERVER,
            params), listener);
    }

    /**
     * Stop the video stream from the connected drone, and release ownership.
     *
     * @param tag      Video tag.
     * @param listener Register a listener to receive update of the command execution status.
     * @since 2.8.1
     */
    private void stopVideoStreamForObserver(final String tag, AbstractCommandListener listener) {
        final Bundle params = new Bundle();
        params.putString(EXTRA_VIDEO_TAG, getObserverTag(tag));

        drone.performAsyncActionOnDroneThread(new Action(ExperimentalActions.ACTION_STOP_VIDEO_STREAM_FOR_OBSERVER, params),
                listener);
    }

    /**
     * Observer for vehicle video stream.
     */
    private static class VideoStreamObserver implements IpConnectionListener {
        private final String TAG = VideoStreamObserver.class.getSimpleName();

        private static final int UDP_BUFFER_SIZE = 1500;
        private static final long RECONNECT_COUNTDOWN_IN_MILLIS = 1000l;
        private static final int SOLO_STREAM_UDP_PORT = 5600;

        private UdpConnection linkConn;
        private final Handler handler;

        private final Runnable onVideoStreamConnected = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(this);
                if(callback != null)
                    callback.onVideoStreamConnected();
            }
        };

        private final Runnable onVideoStreamDisconnected = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(this);
                if(callback != null){
                    callback.onVideoStreamDisconnected();
                }
            }
        };

        private IVideoStreamCallback callback;

        public VideoStreamObserver(Handler handler) {
            this.handler = handler;
        }

        public void setCallback(IVideoStreamCallback callback) {
            this.callback = callback;
        }

        private IVideoStreamCallback getCallback() {
            return callback;
        }

        private final Runnable reconnectTask = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(reconnectTask);
                if (linkConn != null)
                    linkConn.connect(null);
            }
        };

        public void start() {
            if (this.linkConn == null) {
                this.linkConn = new UdpConnection(handler, SOLO_STREAM_UDP_PORT,
                    UDP_BUFFER_SIZE, true, 42);
                this.linkConn.setIpConnectionListener(this);
            }

            handler.removeCallbacks(reconnectTask);

            Log.d(TAG, "Connecting to video stream...");
            this.linkConn.connect(null);
        }

        public void stop() {
            Log.d(TAG, "Stopping video manager");

            handler.removeCallbacks(reconnectTask);

            if (this.linkConn != null) {
                // Break the link.
                this.linkConn.disconnect();
                this.linkConn = null;
            }
        }

        @Override
        public void onIpConnected() {
            Log.d(TAG, "Connected to video stream");

            handler.post(onVideoStreamConnected);
            handler.removeCallbacks(reconnectTask);
        }

        @Override
        public void onIpDisconnected() {
            Log.d(TAG, "Video stream disconnected");

            handler.post(onVideoStreamDisconnected);
            handler.postDelayed(reconnectTask, RECONNECT_COUNTDOWN_IN_MILLIS);
        }

        @Override
        public void onPacketReceived(final ByteBuffer packetBuffer) {
            callback.onAsyncVideoStreamPacketReceived(packetBuffer.array(), packetBuffer.limit());
        }
    }

    /**
     * Callback for directly observing video stream.
     */
    public interface IVideoStreamCallback {
        /**
         * Invoked when opening the connection to the video stream endpoint
         */
        void onVideoStreamConnecting();

        /**
         * Invoked when connected to the video stream endpoint
         */
        void onVideoStreamConnected();

        /**
         * Invoked when closing the connection to the video stream endpoint
         */
        void onVideoStreamDisconnecting();

        /**
         * Invoked when disconnected from the video stream endpoint
         */
        void onVideoStreamDisconnected();

        /**
         * Invoked when detecting an error while connecting to the video stream endpoint
         * @param executionError
         */
        void onError(int executionError);

        /**
         * Invoked when the connection to the video stream endpoint times out
         */
        void onTimeout();

        /**
         * Invoked upon receipt of the video stream data packet.
         * This callback will be invoked on a background thread to avoid blocking the main thread while processing the received data
         * @param data      Video stream data packet
         * @param dataSize  Size of the video stream data
         */
        void onAsyncVideoStreamPacketReceived(byte[] data, int dataSize);
    }
}
