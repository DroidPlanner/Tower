package org.droidplanner.services.android.impl.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Surface;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.action.CameraActions;
import com.o3dr.services.android.lib.drone.action.ConnectionActions;
import com.o3dr.services.android.lib.drone.action.ExperimentalActions;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.action.MissionActions;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.ResetROI;
import com.o3dr.services.android.lib.drone.mission.item.spatial.RegionOfInterest;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.gcs.event.GCSEvent;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.gcs.link.LinkEvent;
import com.o3dr.services.android.lib.gcs.link.LinkEventExtra;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.IApiListener;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.IDroneApi;
import com.o3dr.services.android.lib.model.IMavlinkObserver;
import com.o3dr.services.android.lib.model.IObserver;
import com.o3dr.services.android.lib.model.action.Action;

import org.droidplanner.services.android.impl.communication.connection.SoloConnection;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces;
import org.droidplanner.services.android.impl.core.drone.DroneManager;
import org.droidplanner.services.android.impl.core.drone.autopilot.Drone;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.services.android.impl.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.services.android.impl.exception.ConnectionException;
import org.droidplanner.services.android.impl.utils.CommonApiUtils;
import org.droidplanner.services.android.impl.utils.MissionUtils;
import org.droidplanner.services.android.impl.utils.video.VideoManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

import timber.log.Timber;

import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_SET_MISSION;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.EXTRA_MISSION;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.EXTRA_PUSH_TO_DRONE;

/**
 * Implementation for the IDroneApi interface.
 */
public final class DroneApi extends IDroneApi.Stub implements DroneInterfaces.OnDroneListener, DroneInterfaces.AttributeEventListener,
    DroneInterfaces.OnParameterManagerListener, MagnetometerCalibrationImpl.OnMagnetometerCalibrationListener, IBinder.DeathRecipient {

    //The Reset ROI mission item was introduced in version 2.6.8. Any client library older than this do not support it.
    private final static int RESET_ROI_LIB_VERSION = 206080;

    private final Runnable eventsDispatcher = new Runnable() {
        private final LinkedHashMap<String, Bundle> eventsFilter = new LinkedHashMap<>();

        @Override
        public void run() {
            eventsFilter.clear();
            //Go through the events buffer and empty it
            EventInfo eventInfo = eventsBuffer.poll();
            while(eventInfo != null) {
                eventsFilter.put(eventInfo.event, eventInfo.extras);
                EventInfo.recycle(eventInfo);

                eventInfo = eventsBuffer.poll();
            }

            for(Map.Entry<String, Bundle> entry : eventsFilter.entrySet()){
                dispatchAttributeEvent(entry.getKey(), entry.getValue());
            }

            eventsFilter.clear();

            handler.removeCallbacks(this);
            if(isEventsBufferingEnabled()) {
                handler.postDelayed(this, connectionParams.getEventsDispatchingPeriod());
            }
        }
    };

    private final Context context;
    private final Handler handler;

    private final ConcurrentLinkedQueue<IObserver> observersList;
    private final ConcurrentLinkedQueue<IMavlinkObserver> mavlinkObserversList;
    private DroneManager droneMgr;
    private final IApiListener apiListener;

    private final String ownerId;
    private final ClientInfo clientInfo;

    private final DroidPlannerService service;

    private final ConcurrentLinkedQueue<EventInfo> eventsBuffer = new ConcurrentLinkedQueue<>();

    private ConnectionParameter connectionParams;

    DroneApi(DroidPlannerService dpService, IApiListener listener, String ownerId) {

        this.service = dpService;
        this.context = dpService.getApplicationContext();
        handler = new Handler(Looper.getMainLooper());

        this.ownerId = ownerId;

        observersList = new ConcurrentLinkedQueue<>();
        mavlinkObserversList = new ConcurrentLinkedQueue<>();

        this.apiListener = listener;
        int apiVersionCode = -1;
        int clientVersionCode = -1;
        try {
            this.apiListener.asBinder().linkToDeath(this, 0);
            checkForSelfRelease();

            apiVersionCode = apiListener.getApiVersionCode();
            clientVersionCode = apiListener.getClientVersionCode();
        } catch (RemoteException e) {
            Timber.e(e, e.getMessage());
            dpService.releaseDroneApi(this.ownerId);
        }

        this.clientInfo = new ClientInfo(this.ownerId, apiVersionCode, clientVersionCode);
    }

    void destroy() {
        Timber.d("Destroying drone api instance for %s", this.ownerId);
        this.observersList.clear();
        this.mavlinkObserversList.clear();

        try {
            this.apiListener.asBinder().unlinkToDeath(this, 0);
        } catch (NoSuchElementException e) {
            Timber.e(e, e.getMessage());
        }

        this.service.disconnectDroneManager(this.droneMgr, this.clientInfo);
    }

    public String getOwnerId() {
        return ownerId;
    }

    public DroneManager getDroneManager() {
        return this.droneMgr;
    }

    private Drone getDrone() {
        if (this.droneMgr == null) {
            return null;
        }

        return this.droneMgr.getDrone();
    }

    private boolean isEventsBufferingEnabled(){
        return connectionParams != null && connectionParams.getEventsDispatchingPeriod() > 0L;
    }

    @Override
    public Bundle getAttribute(String type) throws RemoteException {
        Bundle carrier = new Bundle();

        switch (type) {
            case AttributeType.CAMERA:
                carrier.putParcelable(type, CommonApiUtils.getCameraProxy(getDrone(), service.getCameraDetails()));
                break;

            default:
                if (droneMgr != null) {
                    DroneAttribute attribute = droneMgr.getAttribute(clientInfo, type);
                    if (attribute != null) {

                        //Check if the client supports the ResetROI mission item.
                        // Replace it with a RegionOfInterest with coordinate set to 0 if it doesn't.
                        if (clientInfo.clientVersionCode < RESET_ROI_LIB_VERSION && attribute instanceof Mission) {
                            Mission proxyMission = (Mission) attribute;
                            List<MissionItem> missionItems = proxyMission.getMissionItems();
                            int missionItemsCount = missionItems.size();
                            for (int i = 0; i < missionItemsCount; i++) {
                                MissionItem missionItem = missionItems.get(i);
                                if (missionItem instanceof ResetROI) {
                                    missionItems.remove(i);

                                    RegionOfInterest replacement = new RegionOfInterest();
                                    replacement.setCoordinate(new LatLongAlt(0, 0, 0));
                                    missionItems.add(i, replacement);
                                }
                            }
                        }

                        carrier.putParcelable(type, attribute);
                    }
                }
                break;
        }
        return carrier;
    }

    public boolean isConnected() {
        return droneMgr != null && droneMgr.isConnected();
    }

    private ConnectionParameter checkConnectionParameter(ConnectionParameter connParams) throws ConnectionException {
        if (connParams == null) {
            throw new ConnectionException("Invalid connection parameters");
        }

        if (SoloConnection.isUdpSoloConnection(context, connParams)) {
            ConnectionParameter update = SoloConnection.getSoloConnectionParameterFromUdp(context, connParams);
            if (update != null) {
                return update;
            }
        }
        return connParams;
    }

    public void connect(ConnectionParameter connParams) {
        try {
            //Validate the given connection parameter
            connParams = checkConnectionParameter(connParams);

            //Validate the current connection parameter for the drone
            ConnectionParameter currentConnParams = this.connectionParams == null
                ? this.connectionParams
                : checkConnectionParameter(this.connectionParams);

            if (!connParams.equals(currentConnParams)) {
                if (this.droneMgr != null) {
                    LinkConnectionStatus connectionStatus = LinkConnectionStatus
                        .newFailedConnectionStatus(LinkConnectionStatus.ADDRESS_IN_USE,
                            "Connection already started with different connection parameters");
                    onConnectionStatus(connectionStatus);
                    return;
                }

                this.connectionParams = connParams;
                this.droneMgr = service.connectDroneManager(this.connectionParams, ownerId, this);

                if(isEventsBufferingEnabled()) {
                    eventsBuffer.clear();
                    handler.postDelayed(eventsDispatcher, this.connectionParams.getEventsDispatchingPeriod());
                }
            }
        } catch (ConnectionException e) {
            LinkConnectionStatus connectionStatus = LinkConnectionStatus
                .newFailedConnectionStatus(LinkConnectionStatus.INVALID_CREDENTIALS, e.getMessage());
            onConnectionStatus(connectionStatus);
            disconnect();
        }
    }

    public void disconnect() {
        service.disconnectDroneManager(this.droneMgr, clientInfo);
        this.connectionParams = null;
        this.droneMgr = null;

        handler.removeCallbacks(eventsDispatcher);

    }

    private void checkForSelfRelease() {
        //Check if the apiListener is still connected instead.
        if (!apiListener.asBinder().pingBinder()) {
            Timber.w("Client is not longer available.");
            this.context.startService(new Intent(this.context, DroidPlannerService.class)
                .setAction(DroidPlannerService.ACTION_RELEASE_API_INSTANCE)
                .putExtra(DroidPlannerService.EXTRA_API_INSTANCE_APP_ID, this.ownerId));
        }
    }

    @Override
    public void addAttributesObserver(IObserver observer) throws RemoteException {
        if (observer != null) {
            Timber.d("Adding attributes observer.");
            observersList.add(observer);
        }
    }

    @Override
    public void removeAttributesObserver(IObserver observer) throws RemoteException {
        if (observer != null) {
            Timber.d("Removing attributes observer.");
            observersList.remove(observer);
            checkForSelfRelease();
        }
    }

    @Override
    public void addMavlinkObserver(IMavlinkObserver observer) throws RemoteException {
        if (observer != null) {
            mavlinkObserversList.add(observer);
        }
    }

    @Override
    public void removeMavlinkObserver(IMavlinkObserver observer) throws RemoteException {
        if (observer != null) {
            mavlinkObserversList.remove(observer);
            checkForSelfRelease();
        }
    }

    @Override
    public void executeAction(Action action, ICommandListener listener) throws RemoteException {
        if (action == null) {
            return;
        }

        String type = action.getType();
        if (type == null) {
            return;
        }

        Bundle data = action.getData();
        if (data != null) {
            data.setClassLoader(context.getClassLoader());
        }

        Drone drone = getDrone();
        switch (type) {
            // CONNECTION ACTIONS
            case ConnectionActions.ACTION_CONNECT:
                ConnectionParameter parameter = data.getParcelable(ConnectionActions.EXTRA_CONNECT_PARAMETER);
                connect(parameter);
                break;

            case ConnectionActions.ACTION_DISCONNECT:
                disconnect();
                break;

            // CAMERA ACTIONS
            case CameraActions.ACTION_START_VIDEO_STREAM: {
                Surface videoSurface = data.getParcelable(CameraActions.EXTRA_VIDEO_DISPLAY);
                String videoTag = data.getString(CameraActions.EXTRA_VIDEO_TAG, "");

                Bundle videoProps = data.getBundle(CameraActions.EXTRA_VIDEO_PROPERTIES);
                if (videoProps == null) {
                    //Only case where it's null is when interacting with a deprecated client version.
                    //In this case, we assume that the client is attempting to start a solo stream, since that's
                    //the only api that was exposed.
                    videoProps = new Bundle();
                    videoProps.putInt(CameraActions.EXTRA_VIDEO_PROPS_UDP_PORT, VideoManager.ARTOO_UDP_PORT);
                }

                CommonApiUtils.startVideoStream(drone, videoProps, ownerId, videoTag, videoSurface, listener);
                break;
            }

            case ExperimentalActions.ACTION_START_VIDEO_STREAM_FOR_OBSERVER: {
                String videoTag = data.getString(CameraActions.EXTRA_VIDEO_TAG, "");
                CommonApiUtils.startVideoStreamForObserver(drone, ownerId, videoTag, listener);
                break;
            }

            case CameraActions.ACTION_STOP_VIDEO_STREAM: {
                String videoTag = data.getString(CameraActions.EXTRA_VIDEO_TAG, "");
                CommonApiUtils.stopVideoStream(drone, ownerId, videoTag, listener);
                break;
            }

            case ExperimentalActions.ACTION_STOP_VIDEO_STREAM_FOR_OBSERVER: {
                String videoTag = data.getString(CameraActions.EXTRA_VIDEO_TAG, "");
                CommonApiUtils.stopVideoStreamForObserver(drone, ownerId, videoTag, listener);
                break;
            }

            // MISSION ACTIONS
            case MissionActions.ACTION_BUILD_COMPLEX_MISSION_ITEM:
                if (drone instanceof MavLinkDrone || drone == null) {
                    CommonApiUtils.buildComplexMissionItem((MavLinkDrone) drone, data);
                } else {
                    CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                }
                break;

            case MissionActions.ACTION_SAVE_MISSION: {
                Mission mission = data.getParcelable(MissionActions.EXTRA_MISSION);
                Uri saveUri = data.getParcelable(MissionActions.EXTRA_SAVE_MISSION_URI);
                if (saveUri == null) {
                    CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                } else {
                    MissionUtils.saveMission(context, mission, saveUri, listener);
                }
                break;
            }

            case MissionActions.ACTION_LOAD_MISSION: {
                Uri loadUri = data.getParcelable(MissionActions.EXTRA_LOAD_MISSION_URI);
                boolean setMission = data.getBoolean(MissionActions.EXTRA_SET_LOADED_MISSION, false);
                if (loadUri != null) {
                    Mission mission = MissionUtils.loadMission(context, loadUri);
                    if(mission != null){
                        // Going back to the caller.
                        data.putParcelable(MissionActions.EXTRA_MISSION, mission);

                        if(setMission){
                            Bundle params = new Bundle();
                            params.putParcelable(EXTRA_MISSION, mission);
                            params.putBoolean(EXTRA_PUSH_TO_DRONE, false);
                            executeAction(new Action(ACTION_SET_MISSION, params), listener);
                        }
                    }
                }
                break;
            }

            default:
                if (droneMgr != null) {
                    droneMgr.executeAsyncAction(clientInfo, action, listener);
                } else {
                    CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                }
                break;
        }
    }

    @Override
    public void executeAsyncAction(Action action, ICommandListener listener) throws RemoteException {
        executeAction(action, listener);
    }

    @Override
    public void performAction(Action action) throws RemoteException {
        executeAction(action, null);
    }

    @Override
    public void performAsyncAction(Action action) throws RemoteException {
        performAction(action);
    }

    private void notifyAttributeUpdate(List<Pair<String, Bundle>> attributesInfo) {
        if (observersList.isEmpty() || attributesInfo == null || attributesInfo.isEmpty()) {
            return;
        }

        for (Pair<String, Bundle> info : attributesInfo) {
            notifyAttributeUpdate(info.first, info.second);
        }
    }

    private void notifyAttributeUpdate(String attributeEvent, Bundle extrasBundle) {
        if (observersList.isEmpty() || attributeEvent == null) {
            return;
        }

        if(AttributeEvent.STATE_CONNECTED.equals(attributeEvent) ||
            AttributeEvent.STATE_DISCONNECTED.equals(attributeEvent) ||
            !isEventsBufferingEnabled()){
            //Dispatch the event immediately
            dispatchAttributeEvent(attributeEvent, extrasBundle);
        }
        else{
            eventsBuffer.add(EventInfo.obtain(attributeEvent, extrasBundle));
        }
    }

    private void dispatchAttributeEvent(String attributeEvent, Bundle extrasBundle){
        for (IObserver observer : observersList) {
            try {
                observer.onAttributeUpdated(attributeEvent, extrasBundle);
            } catch (RemoteException e) {
                Timber.e(e, e.getMessage());
                try {
                    removeAttributesObserver(observer);
                } catch (RemoteException e1) {
                    Timber.e(e, e1.getMessage());
                }
            }
        }
    }

    public void onReceivedMavLinkMessage(MAVLinkMessage msg) {
        if (mavlinkObserversList.isEmpty()) {
            return;
        }

        if (msg != null) {
            MavlinkMessageWrapper msgWrapper = new MavlinkMessageWrapper(msg);
            for (IMavlinkObserver observer : mavlinkObserversList) {
                try {
                    observer.onMavlinkMessageReceived(msgWrapper);
                } catch (RemoteException e) {
                    Timber.e(e, e.getMessage());
                    try {
                        removeMavlinkObserver(observer);
                    } catch (RemoteException e1) {
                        Timber.e(e1, e1.getMessage());
                    }
                }
            }
        }
    }

    public void onMessageLogged(int logLevel, String message) {
        Bundle args = new Bundle(2);
        args.putInt(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE_LEVEL, logLevel);
        args.putString(AttributeEventExtra.EXTRA_AUTOPILOT_MESSAGE, message);
        notifyAttributeUpdate(AttributeEvent.AUTOPILOT_MESSAGE, args);
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    @Override
    public void onAttributeEvent(String attributeEvent, Bundle eventInfo) {
        if (TextUtils.isEmpty(attributeEvent)) {
            return;
        }

        notifyAttributeUpdate(attributeEvent, eventInfo);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        final Bundle extrasBundle = new Bundle();
        String droneId = "";
        if (drone != null) {
            droneId = drone.getId();
        }

        extrasBundle.putString(AttributeEventExtra.EXTRA_VEHICLE_ID, droneId);

        String droneEvent = null;
        List<Pair<String, Bundle>> attributesInfo = new ArrayList<>();

        switch (event) {
            case DISCONNECTED:
                //Broadcast the disconnection with the vehicle.
                context.sendBroadcast(new Intent(GCSEvent.ACTION_VEHICLE_DISCONNECTION)
                    .putExtra(GCSEvent.EXTRA_APP_ID, ownerId));

                droneEvent = AttributeEvent.STATE_DISCONNECTED;

                //Empty the event buffer queue
                eventsBuffer.clear();
                break;

            case GUIDEDPOINT:
                droneEvent = AttributeEvent.GUIDED_POINT_UPDATED;
                break;

            case RADIO:
                droneEvent = AttributeEvent.SIGNAL_UPDATED;
                break;

            case RC_IN:
                break;
            case RC_OUT:
                break;

            case ARMING_STARTED:
            case ARMING:
                droneEvent = AttributeEvent.STATE_ARMING;
                break;

            case AUTOPILOT_WARNING:
                State droneState = (State) drone.getAttribute(AttributeType.STATE);
                if (droneState != null) {
                    extrasBundle.putString(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID, droneState.getAutopilotErrorId());
                }
                droneEvent = AttributeEvent.AUTOPILOT_ERROR;
                break;

            case MODE:
                droneEvent = AttributeEvent.STATE_VEHICLE_MODE;
                break;

            case ATTITUDE:
            case ORIENTATION:
                droneEvent = AttributeEvent.ATTITUDE_UPDATED;
                break;

            case SPEED:
                droneEvent = AttributeEvent.SPEED_UPDATED;
                break;

            case BATTERY:
                droneEvent = AttributeEvent.BATTERY_UPDATED;
                break;

            case STATE:
                droneEvent = AttributeEvent.STATE_UPDATED;
                break;

            case MISSION_UPDATE:
                droneEvent = AttributeEvent.MISSION_UPDATED;
                break;

            case MISSION_RECEIVED:
                droneEvent = AttributeEvent.MISSION_RECEIVED;
                break;

            case FIRMWARE:
            case TYPE:
                droneEvent = AttributeEvent.TYPE_UPDATED;
                break;

            case HOME:
                droneEvent = AttributeEvent.HOME_UPDATED;
                break;

            case CALIBRATION_IMU:
                if (drone instanceof MavLinkDrone) {
                    String calIMUMessage = ((MavLinkDrone) drone).getCalibrationSetup().getMessage();
                    extrasBundle.putString(AttributeEventExtra.EXTRA_CALIBRATION_IMU_MESSAGE, calIMUMessage);
                    droneEvent = AttributeEvent.CALIBRATION_IMU;
                }
                break;

            case CALIBRATION_TIMEOUT:
                if (drone instanceof MavLinkDrone) {
                /*
                 * here we will check if we are in calibration mode but if at
                 * the same time 'msg' is empty - then it is actually not doing
                 * calibration what we should do is to reset the calibration
                 * flag and re-trigger the HEARTBEAT_TIMEOUT this however should
                 * not be happening
                 */
                    AccelCalibration accelCalibration = ((MavLinkDrone) drone).getCalibrationSetup();
                    String message = accelCalibration.getMessage();
                    if (accelCalibration.isCalibrating() && TextUtils.isEmpty(message)) {
                        accelCalibration.cancelCalibration();
                        droneEvent = AttributeEvent.HEARTBEAT_TIMEOUT;
                    } else {
                        extrasBundle.putString(AttributeEventExtra.EXTRA_CALIBRATION_IMU_MESSAGE, message);
                        droneEvent = AttributeEvent.CALIBRATION_IMU_TIMEOUT;
                    }
                }
                break;

            case HEARTBEAT_TIMEOUT:
                droneEvent = AttributeEvent.HEARTBEAT_TIMEOUT;
                break;

            case CONNECTING:
                droneEvent = AttributeEvent.STATE_CONNECTING;
                break;

            case HEARTBEAT_FIRST:
                Bundle heartBeatExtras = new Bundle();
                heartBeatExtras.putString(AttributeEventExtra.EXTRA_VEHICLE_ID, drone.getId());
                if (drone instanceof MavLinkDrone) {
                    heartBeatExtras.putInt(AttributeEventExtra.EXTRA_MAVLINK_VERSION, ((MavLinkDrone) drone).getMavlinkVersion());
                }
                attributesInfo.add(Pair.create(AttributeEvent.HEARTBEAT_FIRST, heartBeatExtras));

            case CONNECTED:
                //Broadcast the vehicle connection.
                ConnectionParameter sanitizedParameter = connectionParams.clone();

                context.sendBroadcast(new Intent(GCSEvent.ACTION_VEHICLE_CONNECTION)
                    .putExtra(GCSEvent.EXTRA_APP_ID, ownerId)
                    .putExtra(GCSEvent.EXTRA_VEHICLE_CONNECTION_PARAMETER, sanitizedParameter));

                attributesInfo.add(Pair.<String, Bundle>create(AttributeEvent.STATE_CONNECTED, extrasBundle));
                break;

            case HEARTBEAT_RESTORED:
                if (drone instanceof MavLinkDrone) {
                    extrasBundle.putInt(AttributeEventExtra.EXTRA_MAVLINK_VERSION, ((MavLinkDrone) drone).getMavlinkVersion());
                }
                droneEvent = AttributeEvent.HEARTBEAT_RESTORED;
                break;

            case MISSION_SENT:
                droneEvent = AttributeEvent.MISSION_SENT;
                break;

            case INVALID_POLYGON:
                break;

            case MISSION_WP_UPDATE:
                if (drone instanceof MavLinkDrone) {
                    int currentWaypoint = ((MavLinkDrone) drone).getMissionStats().getCurrentWP();
                    extrasBundle.putInt(AttributeEventExtra.EXTRA_MISSION_CURRENT_WAYPOINT, currentWaypoint);
                    droneEvent = AttributeEvent.MISSION_ITEM_UPDATED;
                }
                break;

            case MISSION_WP_REACHED:
                if (drone instanceof MavLinkDrone) {
                    int lastReachedWaypoint = ((MavLinkDrone) drone).getMissionStats().getLastReachedWP();
                    extrasBundle.putInt(AttributeEventExtra.EXTRA_MISSION_LAST_REACHED_WAYPOINT, lastReachedWaypoint);
                    droneEvent = AttributeEvent.MISSION_ITEM_REACHED;
                }
                break;

            case ALTITUDE:
                droneEvent = AttributeEvent.ALTITUDE_UPDATED;
                break;

            case WARNING_SIGNAL_WEAK:
                droneEvent = AttributeEvent.SIGNAL_WEAK;
                break;

            case WARNING_NO_GPS:
                droneEvent = AttributeEvent.WARNING_NO_GPS;
                break;

            case MAGNETOMETER:
                break;

            case FOOTPRINT:
                droneEvent = AttributeEvent.CAMERA_FOOTPRINTS_UPDATED;
                break;

            case EKF_STATUS_UPDATE:
                droneEvent = AttributeEvent.STATE_EKF_REPORT;
                break;

            case EKF_POSITION_STATE_UPDATE:
                droneEvent = AttributeEvent.STATE_EKF_POSITION;
                break;
        }

        if (droneEvent != null) {
            notifyAttributeUpdate(droneEvent, extrasBundle);
        }

        if (!attributesInfo.isEmpty()) {
            notifyAttributeUpdate(attributesInfo);
        }
    }

    @Override
    public void onBeginReceivingParameters() {
        notifyAttributeUpdate(AttributeEvent.PARAMETERS_REFRESH_STARTED, null);
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        Bundle paramsBundle = new Bundle(4);
        paramsBundle.putInt(AttributeEventExtra.EXTRA_PARAMETER_INDEX, index);
        paramsBundle.putInt(AttributeEventExtra.EXTRA_PARAMETERS_COUNT, count);
        paramsBundle.putString(AttributeEventExtra.EXTRA_PARAMETER_NAME, parameter.getName());
        paramsBundle.putDouble(AttributeEventExtra.EXTRA_PARAMETER_VALUE, parameter.getValue());
        notifyAttributeUpdate(AttributeEvent.PARAMETER_RECEIVED, paramsBundle);
    }

    @Override
    public void onEndReceivingParameters() {
        notifyAttributeUpdate(AttributeEvent.PARAMETERS_REFRESH_COMPLETED, null);
    }

    public void onConnectionStatus(LinkConnectionStatus connectionStatus) {
        switch (connectionStatus.getStatusCode()) {
            case LinkConnectionStatus.FAILED:
                disconnect();
                checkForSelfRelease();
                break;

            case LinkConnectionStatus.DISCONNECTED:
                disconnect();
                checkForSelfRelease();
                break;
        }

        Bundle extras = new Bundle();
        extras.putParcelable(LinkEventExtra.EXTRA_CONNECTION_STATUS, connectionStatus);
        notifyAttributeUpdate(LinkEvent.LINK_STATE_UPDATED, extras);

    }

    @Override
    public void binderDied() {
        checkForSelfRelease();
    }

    @Override
    public void onCalibrationCancelled() {
        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_CANCELLED, null);
    }

    @Override
    public void onCalibrationProgress(msg_mag_cal_progress progress) {
        Bundle progressBundle = new Bundle(1);
        progressBundle.putParcelable(AttributeEventExtra.EXTRA_CALIBRATION_MAG_PROGRESS,
            CommonApiUtils.getMagnetometerCalibrationProgress(progress));

        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_PROGRESS, progressBundle);
    }

    @Override
    public void onCalibrationCompleted(msg_mag_cal_report report) {
        Bundle reportBundle = new Bundle(1);
        reportBundle.putParcelable(AttributeEventExtra.EXTRA_CALIBRATION_MAG_RESULT,
            CommonApiUtils.getMagnetometerCalibrationResult(report));

        notifyAttributeUpdate(AttributeEvent.CALIBRATION_MAG_COMPLETED, reportBundle);
    }

    public static class ClientInfo {

        public final String appId;
        public final int apiVersionCode;
        public final int clientVersionCode;

        public ClientInfo(String appId, int apiVersionCode, int clientVersionCode) {
            this.apiVersionCode = apiVersionCode;
            this.appId = appId;
            this.clientVersionCode = clientVersionCode;
        }
    }

    private static class EventInfo {

        private static final ConcurrentLinkedQueue<EventInfo> sPool = new ConcurrentLinkedQueue<>();

        String event;
        Bundle extras;

        static EventInfo obtain(String event, Bundle extras){
            EventInfo eventInfo = sPool.poll();
            if(eventInfo == null){
                eventInfo = new EventInfo();
            }

            eventInfo.event = event;
            eventInfo.extras = extras;
            return eventInfo;
        }

        static void recycle(EventInfo eventInfo){
            if(eventInfo != null) {
                eventInfo.event = null;
                eventInfo.extras = null;
                sPool.offer(eventInfo);
            }
        }
    }
}
