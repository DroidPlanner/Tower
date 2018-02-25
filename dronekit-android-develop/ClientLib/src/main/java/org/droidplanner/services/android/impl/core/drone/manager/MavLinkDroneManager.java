package org.droidplanner.services.android.impl.core.drone.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;
import com.MAVLink.common.msg_command_ack;
import com.google.android.gms.location.LocationRequest;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.action.GimbalActions;
import com.o3dr.services.android.lib.drone.action.StateActions;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.gcs.action.FollowMeActions;
import com.o3dr.services.android.lib.gcs.follow.FollowLocationSource;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.gcs.returnToMe.ReturnToMeState;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.services.android.impl.api.DroneApi;
import org.droidplanner.services.android.impl.communication.service.MAVLinkClient;
import org.droidplanner.services.android.impl.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces;
import org.droidplanner.services.android.impl.core.drone.DroneManager;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.ArduCopter;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.ArduPlane;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.ArduRover;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.ArduSolo;
import org.droidplanner.services.android.impl.core.drone.autopilot.generic.GenericMavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.autopilot.px4.Px4Native;
import org.droidplanner.services.android.impl.core.drone.profiles.ParameterManager;
import org.droidplanner.services.android.impl.core.drone.variables.StreamRates;
import org.droidplanner.services.android.impl.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.services.android.impl.core.firmware.FirmwareType;
import org.droidplanner.services.android.impl.core.gcs.GCSHeartbeat;
import org.droidplanner.services.android.impl.core.gcs.ReturnToMe;
import org.droidplanner.services.android.impl.core.gcs.follow.Follow;
import org.droidplanner.services.android.impl.core.gcs.follow.FollowAlgorithm;
import org.droidplanner.services.android.impl.core.gcs.location.FusedLocation;
import org.droidplanner.services.android.impl.utils.AndroidApWarningParser;
import org.droidplanner.services.android.impl.utils.CommonApiUtils;
import org.droidplanner.services.android.impl.utils.SoloApiUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 12/17/15.
 */
public class MavLinkDroneManager extends DroneManager<MavLinkDrone, MAVLinkPacket> implements MagnetometerCalibrationImpl.OnMagnetometerCalibrationListener {

    private static final int DEFAULT_STREAM_RATE = 2; //Hz

    private Follow followMe;
    private ReturnToMe returnToMe;

    private final MAVLinkClient mavClient;
    private final MavLinkMsgHandler mavLinkMsgHandler;
    private final DroneCommandTracker commandTracker;

    private final GCSHeartbeat gcsHeartbeat;

    private AtomicInteger droneStreamRate = new AtomicInteger(DEFAULT_STREAM_RATE); //Hz

    public MavLinkDroneManager(Context context, ConnectionParameter connParams, Handler handler) {
        super(context, connParams, handler);

        commandTracker = new DroneCommandTracker(handler);

        mavClient = new MAVLinkClient(context, this, connParams, commandTracker);

        this.gcsHeartbeat = new GCSHeartbeat(mavClient, 1);

        this.mavLinkMsgHandler = new MavLinkMsgHandler(this);
        updateDroneStreamRate(connParams);
    }

    public void onVehicleTypeReceived(FirmwareType type) {
        if (drone != null) {
            return;
        }

        final String droneId = connectionParameter.getUniqueId() + ":" + type.getType();

        switch (type) {
            case ARDU_COPTER:
                if (isCompanionComputerEnabled()) {
                    onVehicleTypeReceived(FirmwareType.ARDU_SOLO);
                    return;
                }

                Timber.i("Instantiating ArduCopter autopilot.");
                this.drone = new ArduCopter(droneId, context, mavClient, handler, new AndroidApWarningParser(), this);
                break;

            case ARDU_SOLO:
                Timber.i("Instantiating ArduSolo autopilot.");
                this.drone = new ArduSolo(droneId, context, mavClient, handler, new AndroidApWarningParser(), this);
                break;

            case ARDU_PLANE:
                Timber.i("Instantiating ArduPlane autopilot.");
                this.drone = new ArduPlane(droneId, context, mavClient, handler, new AndroidApWarningParser(), this);
                break;

            case ARDU_ROVER:
                Timber.i("Instantiating ArduPlane autopilot.");
                this.drone = new ArduRover(droneId, context, mavClient, handler, new AndroidApWarningParser(), this);
                break;

            case PX4_NATIVE:
                Timber.i("Instantiating PX4 Native autopilot.");
                this.drone = new Px4Native(droneId, context, handler, mavClient, new AndroidApWarningParser(), this);
                break;

            case GENERIC:
                Timber.i("Instantiating Generic mavlink autopilot.");
                this.drone = new GenericMavLinkDrone(droneId, context, handler, mavClient, new AndroidApWarningParser(), this);
                break;
        }

        this.followMe = new Follow(this, handler, new FusedLocation(context, handler));
        this.returnToMe = new ReturnToMe(this, new FusedLocation(context, handler,
                LocationRequest.PRIORITY_HIGH_ACCURACY, 1000L, 1000L, ReturnToMe.UPDATE_MINIMAL_DISPLACEMENT), this);

        StreamRates streamRates = drone.getStreamRates();
        if (streamRates != null) {
            streamRates.setRates(new StreamRates.Rates(droneStreamRate.get()));
        }

        drone.addDroneListener(this);
        drone.setAttributeListener(this);

        ParameterManager parameterManager = drone.getParameterManager();
        if (parameterManager != null) {
            parameterManager.setParameterListener(this);
        }

        MagnetometerCalibrationImpl magnetometer = drone.getMagnetometerCalibration();
        if (magnetometer != null) {
            magnetometer.setListener(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (followMe != null && followMe.isEnabled())
            followMe.disableFollowMe();

        if (returnToMe != null)
            returnToMe.disable();
    }

    @Override
    protected void doConnect(String appId, DroneApi listener, ConnectionParameter connParams) {
        if (mavClient.isDisconnected()) {
            Timber.i("Opening connection for %s", appId);
            mavClient.openConnection();
        } else {
            if (isConnected()) {
                listener.onDroneEvent(DroneInterfaces.DroneEventsType.CONNECTED, drone);
                if (!drone.isConnectionAlive())
                    listener.onDroneEvent(DroneInterfaces.DroneEventsType.HEARTBEAT_TIMEOUT, drone);
            }
        }

        mavClient.registerForTLogLogging(appId, connParams.getTLogLoggingUri());

        updateDroneStreamRate(connParams);
    }

    private void updateDroneStreamRate(ConnectionParameter connParams) {
        long eventsDispatchingPeriod = connParams.getEventsDispatchingPeriod();
        if (eventsDispatchingPeriod <= 0) {
            // Keep the current rate.
            return;
        }

        int eventsDispatchingRate = Math.round(1000L / eventsDispatchingPeriod);

        boolean updateComplete;
        do {
            updateComplete = true;

            int currentRate = droneStreamRate.get();
            if (eventsDispatchingRate > currentRate) {
                updateComplete = droneStreamRate.compareAndSet(currentRate, eventsDispatchingRate);
                if (updateComplete && drone != null) {
                    StreamRates rates = drone.getStreamRates();
                    if (rates != null) {
                        rates.setRates(new StreamRates.Rates(droneStreamRate.get()));
                    }
                }
            }
        }while(!updateComplete);
    }

    @Override
    protected void doDisconnect(String appId, DroneApi listener) {
        if (drone instanceof GenericMavLinkDrone) {
            ((GenericMavLinkDrone) drone).tryStoppingVideoStream(appId);
        }

        if (listener != null) {
            mavClient.unregisterForTLogLogging(appId);
            if (isConnected()) {
                listener.onDroneEvent(DroneInterfaces.DroneEventsType.DISCONNECTED, drone);
            }
        }

        if (mavClient.isConnected() && connectedApps.isEmpty()) {
            //Reset the gimbal mount mode
            executeAsyncAction(new Action(GimbalActions.ACTION_RESET_GIMBAL_MOUNT_MODE), null);

            mavClient.closeConnection();
        }
    }


    private void handleCommandAck(msg_command_ack ack) {
        if (ack != null) {
            commandTracker.onCommandAck(msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK, ack);
        }
    }

    @Override
    public void notifyReceivedData(MAVLinkPacket packet) {
        MAVLinkMessage receivedMsg = packet.unpack();
        if (receivedMsg == null)
            return;

        if (receivedMsg.msgid == msg_command_ack.MAVLINK_MSG_ID_COMMAND_ACK) {
            msg_command_ack commandAck = (msg_command_ack) receivedMsg;
            handleCommandAck(commandAck);
        } else {
            this.mavLinkMsgHandler.receiveData(receivedMsg);
            if (this.drone != null) {
                this.drone.onMavLinkMessageReceived(receivedMsg);
            }
        }

        if (!connectedApps.isEmpty()) {
            for (DroneApi droneEventsListener : connectedApps.values()) {
                droneEventsListener.onReceivedMavLinkMessage(receivedMsg);
            }
        }
    }

    @Override
    public void onConnectionStatus(LinkConnectionStatus connectionStatus) {
        super.onConnectionStatus(connectionStatus);

        switch (connectionStatus.getStatusCode()) {
            case LinkConnectionStatus.DISCONNECTED:
                this.gcsHeartbeat.setActive(false);
                break;

            case LinkConnectionStatus.CONNECTED:
                this.gcsHeartbeat.setActive(true);
                break;
        }
    }

    @Override
    public DroneAttribute getAttribute(DroneApi.ClientInfo clientInfo, String attributeType) {
        switch (attributeType) {
            case AttributeType.FOLLOW_STATE:
                return CommonApiUtils.getFollowState(followMe);

            case AttributeType.RETURN_TO_ME_STATE:
                return returnToMe == null ? new ReturnToMeState() : returnToMe.getState();

            default:
                return super.getAttribute(clientInfo, attributeType);
        }
    }

    @Override
    protected boolean executeAsyncAction(Action action, ICommandListener listener) {
        String type = action.getType();
        Bundle data = action.getData();

        Timber.d("executeAsyncAction(): action=%s", type);

        switch (type) {
            //FOLLOW-ME ACTIONS
            case FollowMeActions.ACTION_ENABLE_FOLLOW_ME:
                data.setClassLoader(FollowType.class.getClassLoader());

                FollowLocationSource locationSource = data.getParcelable(FollowMeActions.EXTRA_LOCATION_SOURCE);

                // Default to internal GPS locations
                if(locationSource == null) {
                    locationSource = FollowLocationSource.INTERNAL;
                }

                FollowType followType = data.getParcelable(FollowMeActions.EXTRA_FOLLOW_TYPE);
                enableFollowMe(followType, locationSource, listener);
                return true;

            case FollowMeActions.ACTION_UPDATE_FOLLOW_PARAMS:
                if (followMe != null) {
                    data.setClassLoader(LatLong.class.getClassLoader());

                    FollowAlgorithm followAlgorithm = followMe.getFollowAlgorithm();
                    if (followAlgorithm != null) {
                        Map<String, Object> paramsMap = new HashMap<>();
                        Set<String> dataKeys = data.keySet();

                        for (String key : dataKeys) {
                            paramsMap.put(key, data.get(key));
                        }

                        followAlgorithm.updateAlgorithmParams(paramsMap);
                    }
                }
                return true;

            case FollowMeActions.ACTION_DISABLE_FOLLOW_ME:
                CommonApiUtils.disableFollowMe(followMe);
                return true;

            case FollowMeActions.ACTION_NEW_EXTERNAL_LOCATION:
                data.setClassLoader(android.location.Location.class.getClassLoader());
                if(followMe != null && data != null) {
                    android.location.Location loc = data.getParcelable(FollowMeActions.EXTRA_LOCATION);
                    if(loc != null) {
                        Timber.i("onNewLocation(%s)", loc);
                        followMe.onFollowNewLocation(loc);
                    }
                }
                return true;

            //************ RETURN TO ME ACTIONS *********//
            case StateActions.ACTION_ENABLE_RETURN_TO_ME:
                boolean isEnabled = data.getBoolean(StateActions.EXTRA_IS_RETURN_TO_ME_ENABLED, false);
                if (returnToMe != null) {
                    if (isEnabled) {
                        returnToMe.enable(listener);
                    } else {
                        returnToMe.disable();
                    }
                    CommonApiUtils.postSuccessEvent(listener);
                } else {
                    CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                }
                return true;

            default:
                return super.executeAsyncAction(action, listener);
        }
    }

    private void enableFollowMe(FollowType followType, FollowLocationSource source, ICommandListener listener) {
        Timber.d("enableFollowMe(): followType=%s source=%s", followType, source);

        FollowAlgorithm.FollowModes selectedMode = CommonApiUtils.followTypeToMode(drone, followType);

        if (selectedMode != null) {
            if (followMe == null) {
                Timber.d("enableFollowMe(): followMe is null");
                return;
            }

            Timber.d("CURRENT: followMe.enabled=%s followMe.state=%s source=%s",
                    followMe.isEnabled(), followMe.getState(), source);

            followMe.enableFollowMe(source);

            FollowAlgorithm currentAlg = followMe.getFollowAlgorithm();
            if (currentAlg.getType() != selectedMode) {
                if (selectedMode == FollowAlgorithm.FollowModes.SOLO_SHOT &&
                        !SoloApiUtils.isSoloLinkFeatureAvailable(drone, listener)) {
                    Timber.w("FollowType is SOLO_SHOT, but SoloLink is not available.");
                    return;
                }

                FollowAlgorithm algo = selectedMode.getAlgorithmType(this, handler);
                Timber.d("Setting followAlgorithm to %s", algo);
                followMe.setAlgorithm(algo);
                CommonApiUtils.postSuccessEvent(listener);
            }

            Timber.i("AFTER: followMe.state=%s type=%s", followMe.getState(), followType);
        }
    }

    @Override
    public void onCalibrationCancelled() {
        if (connectedApps.isEmpty())
            return;

        for (DroneApi listener : connectedApps.values())
            listener.onCalibrationCancelled();
    }

    @Override
    public void onCalibrationProgress(msg_mag_cal_progress progress) {
        if (connectedApps.isEmpty())
            return;

        for (DroneApi listener : connectedApps.values())
            listener.onCalibrationProgress(progress);
    }

    @Override
    public void onCalibrationCompleted(msg_mag_cal_report report) {
        if (connectedApps.isEmpty())
            return;

        for (DroneApi listener : connectedApps.values())
            listener.onCalibrationCompleted(report);
    }
}
