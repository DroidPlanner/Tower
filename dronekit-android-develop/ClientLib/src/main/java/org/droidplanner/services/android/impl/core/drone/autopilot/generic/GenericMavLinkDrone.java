package org.droidplanner.services.android.impl.core.drone.autopilot.generic;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Surface;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_ekf_status_report;
import com.MAVLink.common.msg_attitude;
import com.MAVLink.common.msg_global_position_int;
import com.MAVLink.common.msg_gps_raw_int;
import com.MAVLink.common.msg_heartbeat;
import com.MAVLink.common.msg_mission_current;
import com.MAVLink.common.msg_mission_item;
import com.MAVLink.common.msg_mission_item_reached;
import com.MAVLink.common.msg_nav_controller_output;
import com.MAVLink.common.msg_radio_status;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.common.msg_vibration;
import com.MAVLink.enums.MAV_MODE_FLAG;
import com.MAVLink.enums.MAV_STATE;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.action.CapabilityActions;
import com.o3dr.services.android.lib.drone.action.ControlActions;
import com.o3dr.services.android.lib.drone.action.ExperimentalActions;
import com.o3dr.services.android.lib.drone.action.StateActions;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.mission.action.MissionActions;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.drone.property.Vibration;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;
import com.o3dr.services.android.lib.util.MathUtils;

import org.droidplanner.services.android.impl.communication.model.DataLink;
import org.droidplanner.services.android.impl.core.MAVLink.MavLinkCommands;
import org.droidplanner.services.android.impl.core.MAVLink.MavLinkWaypoint;
import org.droidplanner.services.android.impl.core.MAVLink.WaypointManager;
import org.droidplanner.services.android.impl.core.drone.DroneEvents;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces;
import org.droidplanner.services.android.impl.core.drone.LogMessageListener;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.APMConstants;
import org.droidplanner.services.android.impl.core.drone.profiles.ParameterManager;
import org.droidplanner.services.android.impl.core.drone.variables.ApmModes;
import org.droidplanner.services.android.impl.core.drone.variables.Camera;
import org.droidplanner.services.android.impl.core.drone.variables.GuidedPoint;
import org.droidplanner.services.android.impl.core.drone.variables.HeartBeat;
import org.droidplanner.services.android.impl.core.drone.variables.MissionStats;
import org.droidplanner.services.android.impl.core.drone.variables.State;
import org.droidplanner.services.android.impl.core.drone.variables.StreamRates;
import org.droidplanner.services.android.impl.core.drone.variables.Type;
import org.droidplanner.services.android.impl.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.services.android.impl.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.services.android.impl.core.firmware.FirmwareType;
import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.model.AutopilotWarningParser;
import org.droidplanner.services.android.impl.utils.CommonApiUtils;
import org.droidplanner.services.android.impl.utils.video.VideoManager;

/**
 * Base drone implementation.
 * Supports mavlink messages belonging to the common set: https://pixhawk.ethz.ch/mavlink/
 * <p/>
 * Created by Fredia Huya-Kouadio on 9/10/15.
 */
public class GenericMavLinkDrone implements MavLinkDrone {

    /** Fixed SYSTEM_ID - {@value}, for SiK Telemetry Radio (aka 3DR Telemetry Radio) .*/
    public static final int SiK_RADIO_FIXED_SYSID = 0x33;  // '3' 0x33 51
    /** Fixed COMPONENT_ID - {@value}, for SiK Telemetry Radio (aka 3DR Telemetry Radio) .*/
    public static final int SiK_RADIO_FIXED_COMPID = 0x44; // 'D' 0x44 68

    private final DataLink.DataLinkProvider<MAVLinkMessage> mavClient;

    protected final VideoManager videoMgr;

    private final DroneEvents events;
    protected final Type type;
    private final State state;
    private final HeartBeat heartbeat;
    private final StreamRates streamRates;
    private final ParameterManager parameterManager;
    private final LogMessageListener logListener;
    private final MissionStats missionStats;

    private DroneInterfaces.AttributeEventListener attributeListener;

    private final Home vehicleHome = new Home();
    private final Gps vehicleGps = new Gps();
    private final Parameters parameters = new Parameters();
    protected final Altitude altitude = new Altitude();
    protected final Speed speed = new Speed();
    protected final Battery battery = new Battery();
    protected final Signal signal = new Signal();
    protected final Attitude attitude = new Attitude();
    protected final Vibration vibration = new Vibration();

    protected final Handler handler;

    private final String droneId;

    public GenericMavLinkDrone(String droneId, Context context, Handler handler, DataLink.DataLinkProvider<MAVLinkMessage> mavClient,
                               AutopilotWarningParser warningParser, LogMessageListener logListener) {
        this.droneId = droneId;
        this.handler = handler;
        this.mavClient = mavClient;

        this.logListener = logListener;

        events = new DroneEvents(this);
        heartbeat = initHeartBeat(handler);
        this.type = new Type(this);
        this.missionStats = new MissionStats(this);
        this.streamRates = new StreamRates(this);
        this.state = new State(this, handler, warningParser);
        parameterManager = new ParameterManager(this, context, handler);

        this.videoMgr = new VideoManager(context, handler, mavClient);
    }

    @Override
    public String getId(){
        return droneId;
    }

    @Override
    public void setAttributeListener(DroneInterfaces.AttributeEventListener attributeListener) {
        this.attributeListener = attributeListener;
    }

    @Override
    public MissionStats getMissionStats() {
        return missionStats;
    }

    @Override
    public MissionImpl getMission() {
        //TODO: complete implementation
        return null;
    }

    @Override
    public Camera getCamera() {
        //TODO: complete implementation
        return null;
    }

    @Override
    public GuidedPoint getGuidedPoint() {
        //TODO: complete implementation
        return null;
    }

    @Override
    public AccelCalibration getCalibrationSetup() {
        //TODO: complete implementation
        return null;
    }

    @Override
    public WaypointManager getWaypointManager() {
        //TODO: complete implementation
        return null;
    }

    @Override
    public MagnetometerCalibrationImpl getMagnetometerCalibration() {
        //TODO: complete implementation
        return null;
    }

    @Override
    public void destroy(){
        ParameterManager parameterManager = getParameterManager();
        if (parameterManager != null)
            parameterManager.setParameterListener(null);

        MagnetometerCalibrationImpl magnetometer = getMagnetometerCalibration();
        if (magnetometer != null)
            magnetometer.setListener(null);
    }

    protected HeartBeat initHeartBeat(Handler handler) {
        return new HeartBeat(this, handler);
    }

    @Override
    public FirmwareType getFirmwareType() {
        return FirmwareType.GENERIC;
    }

    @Override
    public String getFirmwareVersion() {
        return type.getFirmwareVersion();
    }

    protected void setFirmwareVersion(String message) {
        type.setFirmwareVersion(message);
    }

    @Override
    public ParameterManager getParameterManager() {
        return parameterManager;
    }

    protected void logMessage(int logLevel, String message) {
        if (logListener != null)
            logListener.onMessageLogged(logLevel, message);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public boolean isConnected() {
        return mavClient.isConnected() && heartbeat.hasHeartbeat();
    }

    @Override
    public boolean isConnectionAlive() {
        return heartbeat.isConnectionAlive();
    }

    @Override
    public short getSysid() {

        return heartbeat.getSysid();
    }

    @Override
    public short getCompid() {
        return heartbeat.getCompid();
    }

    @Override
    public int getMavlinkVersion() {
        return heartbeat.getMavlinkVersion();
    }

    @Override
    public void addDroneListener(DroneInterfaces.OnDroneListener listener) {
        events.addDroneListener(listener);
    }

    @Override
    public StreamRates getStreamRates() {
        return streamRates;
    }

    @Override
    public void removeDroneListener(DroneInterfaces.OnDroneListener listener) {
        events.removeDroneListener(listener);
    }

    public void startVideoStream(Bundle videoProps, String appId, String newVideoTag, Surface videoSurface,
                                 ICommandListener listener) {
        videoMgr.startVideoStream(videoProps, appId, newVideoTag, videoSurface, listener);
    }

    public void stopVideoStream(String appId, String currentVideoTag, ICommandListener listener) {
        videoMgr.stopVideoStream(appId, currentVideoTag, listener);
    }

    public void startVideoStreamForObserver(String appId, String newVideoTag, ICommandListener listener) {
        videoMgr.startVideoStreamForObserver(appId, newVideoTag, listener);
    }

    public void stopVideoStreamForObserver(String appId, String currentVideoTag, ICommandListener listener) {
        videoMgr.stopVideoStreamForObserver(appId, currentVideoTag, listener);
    }

    /**
     * Stops the video stream if the current owner is the passed argument.
     *
     * @param appId
     */
    public void tryStoppingVideoStream(String appId) {
        videoMgr.tryStoppingVideoStream(appId);
    }

    protected void notifyAttributeListener(String attributeEvent) {
        notifyAttributeListener(attributeEvent, null);
    }

    protected void notifyAttributeListener(String attributeEvent, Bundle eventInfo) {
        if (attributeListener != null) {
            if(eventInfo == null){
                eventInfo = new Bundle();
            }
            eventInfo.putString(AttributeEventExtra.EXTRA_VEHICLE_ID, getId());

            attributeListener.onAttributeEvent(attributeEvent, eventInfo);
        }
    }

    @Override
    public void notifyDroneEvent(DroneInterfaces.DroneEventsType event) {
        switch (event) {
            case DISCONNECTED:
                signal.setValid(false);
                break;
        }

        events.notifyDroneEvent(event);
    }

    @Override
    public DataLink.DataLinkProvider<MAVLinkMessage> getMavClient() {
        return mavClient;
    }

    @Override
    public boolean executeAsyncAction(Action action, ICommandListener listener) {
        String type = action.getType();
        Bundle data = action.getData();

        switch (type) {
            //MISSION ACTIONS
            case MissionActions.ACTION_GENERATE_DRONIE:
                float bearing = CommonApiUtils.generateDronie(this);
                if (bearing != -1) {
                    Bundle bundle = new Bundle(1);
                    bundle.putFloat(AttributeEventExtra.EXTRA_MISSION_DRONIE_BEARING, bearing);
                    notifyAttributeListener(AttributeEvent.MISSION_DRONIE_CREATED, bundle);
                }
                return true;

            case MissionActions.ACTION_GOTO_WAYPOINT:
                int missionItemIndex = data.getInt(MissionActions.EXTRA_MISSION_ITEM_INDEX);
                CommonApiUtils.gotoWaypoint(this, missionItemIndex, listener);
                return true;

            case MissionActions.ACTION_CHANGE_MISSION_SPEED:
                float missionSpeed = data.getFloat(MissionActions.EXTRA_MISSION_SPEED);
                MavLinkCommands.changeMissionSpeed(this, missionSpeed, listener);
                return true;

            // STATE ACTIONS
            case StateActions.ACTION_ARM:
                return performArming(data, listener);

            case StateActions.ACTION_SET_VEHICLE_MODE:
                return setVehicleMode(data, listener);

            case StateActions.ACTION_UPDATE_VEHICLE_DATA_STREAM_RATE:
                return updateVehicleDataStreamRate(data, listener);

            // CONTROL ACTIONS
            case ControlActions.ACTION_DO_GUIDED_TAKEOFF:
                return performTakeoff(data, listener);

            case ControlActions.ACTION_SEND_BRAKE_VEHICLE:
                return brakeVehicle(listener);

            case ControlActions.ACTION_SET_CONDITION_YAW:
                // Retrieve the yaw turn speed.
                float turnSpeed = 2; // Default turn speed.

                ParameterManager parameterManager = getParameterManager();
                if (parameterManager != null) {
                    Parameter turnSpeedParam = parameterManager.getParameter("ACRO_YAW_P");
                    if (turnSpeedParam != null) {
                        turnSpeed = (float) turnSpeedParam.getValue();
                    }
                }

                float targetAngle = data.getFloat(ControlActions.EXTRA_YAW_TARGET_ANGLE);
                float yawRate = data.getFloat(ControlActions.EXTRA_YAW_CHANGE_RATE);
                boolean isClockwise = yawRate >= 0;
                boolean isRelative = data.getBoolean(ControlActions.EXTRA_YAW_IS_RELATIVE);

                MavLinkCommands.setConditionYaw(this, targetAngle, Math.abs(yawRate) * turnSpeed, isClockwise, isRelative, listener);
                return true;

            case ControlActions.ACTION_SET_VELOCITY:
                return setVelocity(data, listener);

            case ControlActions.ACTION_ENABLE_MANUAL_CONTROL:
                return enableManualControl(data, listener);

            // EXPERIMENTAL ACTIONS
            case ExperimentalActions.ACTION_SEND_MAVLINK_MESSAGE:
                data.setClassLoader(MavlinkMessageWrapper.class.getClassLoader());
                MavlinkMessageWrapper messageWrapper = data.getParcelable(ExperimentalActions.EXTRA_MAVLINK_MESSAGE);
                CommonApiUtils.sendMavlinkMessage(this, messageWrapper);
                return true;

            // INTERNAL DRONE ACTIONS
            case ACTION_REQUEST_HOME_UPDATE:
                requestHomeUpdate();
                return true;

            //**************** CAPABILITY ACTIONS **************//
            case CapabilityActions.ACTION_CHECK_FEATURE_SUPPORT:
                return checkFeatureSupport(data, listener);

            default:
                CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                return true;
        }
    }

    private boolean updateVehicleDataStreamRate(Bundle data, ICommandListener listener) {
        StreamRates streamRates = getStreamRates();
        if(streamRates != null){
            int rate = data.getInt(StateActions.EXTRA_VEHICLE_DATA_STREAM_RATE, -1);
            if(rate != -1) {
                StreamRates.Rates rates = new StreamRates.Rates(rate);
                streamRates.setRates(rates);
            }
            CommonApiUtils.postSuccessEvent(listener);
            return true;
        }

        CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
        return false;
    }

    private boolean checkFeatureSupport(Bundle data, ICommandListener listener) {
            String featureId = data.getString(CapabilityActions.EXTRA_FEATURE_ID);
            if (!TextUtils.isEmpty(featureId)) {
                if(isFeatureSupported(featureId)){
                    CommonApiUtils.postSuccessEvent(listener);
                }else{
                    CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
                }
            }
            else{
                CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
            }

        return true;
    }

    protected boolean isFeatureSupported(String featureId){
        return false;
    }

    protected boolean enableManualControl(Bundle data, ICommandListener listener) {
        boolean enable = data.getBoolean(ControlActions.EXTRA_DO_ENABLE);
        if (enable) {
            CommonApiUtils.postSuccessEvent(listener);
        } else {
            CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
        }
        return true;
    }

    protected boolean performArming(Bundle data, ICommandListener listener) {
        boolean doArm = data.getBoolean(StateActions.EXTRA_ARM);
        boolean emergencyDisarm = data.getBoolean(StateActions.EXTRA_EMERGENCY_DISARM);

        if (!doArm && emergencyDisarm) {
            MavLinkCommands.sendFlightTermination(this, listener);
        } else {
            MavLinkCommands.sendArmMessage(this, doArm, false, listener);
        }
        return true;
    }

    protected boolean setVehicleMode(Bundle data, ICommandListener listener) {
        data.setClassLoader(VehicleMode.class.getClassLoader());
        VehicleMode newMode = data.getParcelable(StateActions.EXTRA_VEHICLE_MODE);
        if (newMode != null) {
            switch (newMode) {
                case COPTER_LAND:
                    MavLinkCommands.sendNavLand(this, listener);
                    break;

                case COPTER_RTL:
                    MavLinkCommands.sendNavRTL(this, listener);
                    break;

                case COPTER_GUIDED:
                    MavLinkCommands.sendPause(this, listener);
                    break;

                case COPTER_AUTO:
                    MavLinkCommands.startMission(this, listener);
                    break;
            }
        }
        return true;
    }

    protected boolean setVelocity(Bundle data, ICommandListener listener) {
        float xAxis = data.getFloat(ControlActions.EXTRA_VELOCITY_X);
        short x = (short) (xAxis * 1000);

        float yAxis = data.getFloat(ControlActions.EXTRA_VELOCITY_Y);
        short y = (short) (yAxis * 1000);

        float zAxis = data.getFloat(ControlActions.EXTRA_VELOCITY_Z);
        short z = (short) (zAxis * 1000);

        MavLinkCommands.sendManualControl(this, x, y, z, (short) 0, 0, listener);
        return true;
    }

    protected boolean performTakeoff(Bundle data, ICommandListener listener) {
        double takeoffAltitude = data.getDouble(ControlActions.EXTRA_ALTITUDE);
        MavLinkCommands.sendTakeoff(this, takeoffAltitude, listener);
        return true;
    }

    protected boolean brakeVehicle(ICommandListener listener) {
        getGuidedPoint().pauseAtCurrentLocation(listener);
        return true;
    }

    @Override
    public DroneAttribute getAttribute(String attributeType) {
        if (TextUtils.isEmpty(attributeType))
            return null;

        switch (attributeType) {
            case AttributeType.SPEED:
                return speed;

            case AttributeType.BATTERY:
                return battery;

            case AttributeType.SIGNAL:
                return signal;

            case AttributeType.ATTITUDE:
                return attitude;

            case AttributeType.ALTITUDE:
                return altitude;

            case AttributeType.STATE:
                return CommonApiUtils.getState(this, isConnected(), vibration);

            case AttributeType.GPS:
                return vehicleGps;

            case AttributeType.HOME:
                return vehicleHome;

            case AttributeType.PARAMETERS:
                ParameterManager paramMgr = getParameterManager();
                if (paramMgr != null) {
                    parameters.setParametersList(paramMgr.getParameters().values());
                }

                return parameters;

            case AttributeType.TYPE:
                return CommonApiUtils.getType(this);
        }

        return null;
    }

    private void onHeartbeat(MAVLinkMessage msg) {
        heartbeat.onHeartbeat(msg);
    }

    // Check if message should be allowed to pass even if sysid/compid mismatch
    protected boolean isMavLinkMessageException(MAVLinkMessage message){

        // Allows SiK Radio Messages through. // TODO Make it a configurable setting
        if (message.sysid == SiK_RADIO_FIXED_SYSID && message.compid == SiK_RADIO_FIXED_COMPID) {
            return true;
        }
        return false;
    }

    @Override
    public void onMavLinkMessageReceived(MAVLinkMessage message) {

        if ( (message.sysid != this.getSysid()) && !isMavLinkMessageException(message) ) {
            // Reject messages that are not for this drone's system id
            return;
        }

        onHeartbeat(message);

        switch (message.msgid) {
            case msg_radio_status.MAVLINK_MSG_ID_RADIO_STATUS:
                msg_radio_status m_radio_status = (msg_radio_status) message;
                processSignalUpdate(m_radio_status.rxerrors, m_radio_status.fixed, m_radio_status.rssi,
                        m_radio_status.remrssi, m_radio_status.txbuf, m_radio_status.noise, m_radio_status.remnoise);
                break;

            case msg_attitude.MAVLINK_MSG_ID_ATTITUDE:
                msg_attitude m_att = (msg_attitude) message;
                processAttitude(m_att);
                break;

            case msg_heartbeat.MAVLINK_MSG_ID_HEARTBEAT:
                msg_heartbeat msg_heart = (msg_heartbeat) message;
                processHeartbeat(msg_heart);
                break;

            case msg_vibration.MAVLINK_MSG_ID_VIBRATION:
                msg_vibration vibrationMsg = (msg_vibration) message;
                processVibrationMessage(vibrationMsg);
                break;

            //*************** EKF State handling ******************//
            case msg_ekf_status_report.MAVLINK_MSG_ID_EKF_STATUS_REPORT:
                processEfkStatus((msg_ekf_status_report) message);
                break;

            case msg_sys_status.MAVLINK_MSG_ID_SYS_STATUS:
                msg_sys_status m_sys = (msg_sys_status) message;
                processSysStatus(m_sys);
                break;

            case msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT:
                processGlobalPositionInt((msg_global_position_int) message);
                break;

            case msg_gps_raw_int.MAVLINK_MSG_ID_GPS_RAW_INT:
                processGpsState((msg_gps_raw_int) message);
                break;

            case msg_mission_item.MAVLINK_MSG_ID_MISSION_ITEM:
                processHomeUpdate((msg_mission_item) message);
                break;

            case msg_mission_current.MAVLINK_MSG_ID_MISSION_CURRENT:
                missionStats.setWpno(((msg_mission_current) message).seq);
                break;

            case msg_mission_item_reached.MAVLINK_MSG_ID_MISSION_ITEM_REACHED:
                missionStats.setLastReachedWaypointNumber(((msg_mission_item_reached) message).seq);
                break;

            case msg_nav_controller_output.MAVLINK_MSG_ID_NAV_CONTROLLER_OUTPUT:
                msg_nav_controller_output m_nav = (msg_nav_controller_output) message;
                setDisttowpAndSpeedAltErrors(m_nav.wp_dist, m_nav.alt_error, m_nav.aspd_error);
                break;
        }
    }

    protected void processSysStatus(msg_sys_status m_sys) {
        processBatteryUpdate(m_sys.voltage_battery / 1000.0, m_sys.battery_remaining,
            m_sys.current_battery / 100.0);
    }

    private void processHeartbeat(msg_heartbeat msg_heart) {
        setType(msg_heart.type);
        checkIfFlying(msg_heart);
        processState(msg_heart);
        processVehicleMode(msg_heart);
    }

    private void processVehicleMode(msg_heartbeat msg_heart) {
        ApmModes newMode = ApmModes.getMode(msg_heart.custom_mode, getType());
        state.setMode(newMode);
    }

    private void processState(msg_heartbeat msg_heart) {
        checkArmState(msg_heart);
        checkFailsafe(msg_heart);
    }

    private void checkFailsafe(msg_heartbeat msg_heart) {
        boolean failsafe2 = msg_heart.system_status == MAV_STATE.MAV_STATE_CRITICAL
                || msg_heart.system_status == MAV_STATE.MAV_STATE_EMERGENCY;

        if (failsafe2) {
            state.repeatWarning();
        }
    }

    private void checkArmState(msg_heartbeat msg_heart) {
        state.setArmed(
                (msg_heart.base_mode & MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED) == MAV_MODE_FLAG.MAV_MODE_FLAG_SAFETY_ARMED);
    }

    private void checkIfFlying(msg_heartbeat msg_heart) {
        short systemStatus = msg_heart.system_status;
        boolean wasFlying = state.isFlying();

        boolean isFlying = systemStatus == MAV_STATE.MAV_STATE_ACTIVE
                || (wasFlying
                && (systemStatus == MAV_STATE.MAV_STATE_CRITICAL || systemStatus == MAV_STATE.MAV_STATE_EMERGENCY));

        state.setIsFlying(isFlying);
    }

    private void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error, double aspd_error) {
        missionStats.setDistanceToWp(disttowp);

        this.altitude.setTargetAltitude(this.altitude.getAltitude() + alt_error);
        notifyDroneEvent(DroneInterfaces.DroneEventsType.ORIENTATION);
    }

    public void processHomeUpdate(msg_mission_item missionItem) {
        if (missionItem.seq != APMConstants.HOME_WAYPOINT_INDEX) {
            return;
        }

        float latitude = missionItem.x;
        float longitude = missionItem.y;
        float altitude = missionItem.z;
        boolean homeUpdated = false;

        LatLongAlt homeCoord = vehicleHome.getCoordinate();
        if (homeCoord == null) {
            vehicleHome.setCoordinate(new LatLongAlt(latitude, longitude, altitude));
            homeUpdated = true;
        } else {
            if (homeCoord.getLatitude() != latitude
                    || homeCoord.getLongitude() != longitude
                    || homeCoord.getAltitude() != altitude) {
                homeCoord.setLatitude(latitude);
                homeCoord.setLongitude(longitude);
                homeCoord.setAltitude(altitude);
                homeUpdated = true;
            }
        }

        if (homeUpdated) {
            notifyDroneEvent(DroneInterfaces.DroneEventsType.HOME);
        }
    }

    protected void processBatteryUpdate(double voltage, double remain, double current) {
        if (battery.getBatteryVoltage() != voltage || battery.getBatteryRemain() != remain || battery.getBatteryCurrent() != current) {
            battery.setBatteryVoltage(voltage);
            battery.setBatteryRemain(remain);
            battery.setBatteryCurrent(current);

            notifyDroneEvent(DroneInterfaces.DroneEventsType.BATTERY);
        }
    }

    private void processVibrationMessage(msg_vibration vibrationMsg) {
        boolean wasUpdated = false;

        if (vibration.getVibrationX() != vibrationMsg.vibration_x) {
            vibration.setVibrationX(vibrationMsg.vibration_x);
            wasUpdated = true;
        }

        if (vibration.getVibrationY() != vibrationMsg.vibration_y) {
            vibration.setVibrationY(vibrationMsg.vibration_y);
            wasUpdated = true;
        }

        if (vibration.getVibrationZ() != vibrationMsg.vibration_z) {
            vibration.setVibrationZ(vibrationMsg.vibration_z);
            wasUpdated = true;
        }

        if (vibration.getFirstAccelClipping() != vibrationMsg.clipping_0) {
            vibration.setFirstAccelClipping(vibrationMsg.clipping_0);
            wasUpdated = true;
        }

        if (vibration.getSecondAccelClipping() != vibrationMsg.clipping_1) {
            vibration.setSecondAccelClipping(vibrationMsg.clipping_1);
            wasUpdated = true;
        }

        if (vibration.getThirdAccelClipping() != vibrationMsg.clipping_2) {
            vibration.setThirdAccelClipping(vibrationMsg.clipping_2);
            wasUpdated = true;
        }

        if (wasUpdated) {
            notifyAttributeListener(AttributeEvent.STATE_VEHICLE_VIBRATION);
        }
    }

    protected void setType(int type) {
        this.type.setType(type);
    }

    @Override
    public int getType() {
        return type.getType();
    }

    private void processAttitude(msg_attitude m_att) {
        attitude.setRoll(Math.toDegrees(m_att.roll));
        attitude.setRollSpeed((float) Math.toDegrees(m_att.rollspeed));

        attitude.setPitch(Math.toDegrees(m_att.pitch));
        attitude.setPitchSpeed((float) Math.toDegrees(m_att.pitchspeed));

        attitude.setYaw(Math.toDegrees(m_att.yaw));
        attitude.setYawSpeed((float) Math.toDegrees(m_att.yawspeed));

        notifyDroneEvent(DroneInterfaces.DroneEventsType.ATTITUDE);
    }

    protected void processSignalUpdate(int rxerrors, int fixed, short rssi, short remrssi, short txbuf,
                                       short noise, short remnoise) {
        signal.setValid(true);
        signal.setRxerrors(rxerrors & 0xFFFF);
        signal.setFixed(fixed & 0xFFFF);
        signal.setRssi(SikValueToDB(rssi & 0xFF));
        signal.setRemrssi(SikValueToDB(remrssi & 0xFF));
        signal.setNoise(SikValueToDB(noise & 0xFF));
        signal.setRemnoise(SikValueToDB(remnoise & 0xFF));
        signal.setTxbuf(txbuf & 0xFF);

        signal.setSignalStrength(MathUtils.getSignalStrength(signal.getFadeMargin(), signal.getRemFadeMargin()));

        notifyDroneEvent(DroneInterfaces.DroneEventsType.RADIO);
    }

    /**
     * Scalling done at the Si1000 radio More info can be found at:
     * http://copter.ardupilot.com/wiki/common-using-the-3dr-radio-for-telemetry-with-apm-and-px4/#Power_levels
     */
    protected double SikValueToDB(int value) {
        return (value / 1.9) - 127;
    }

    /**
     * Used to update the vehicle location.
     *
     * @param gpi
     */
    protected void processGlobalPositionInt(msg_global_position_int gpi) {
        if (gpi == null)
            return;

        double newLat = gpi.lat / 1E7;
        double newLong = gpi.lon / 1E7;

        boolean positionUpdated = false;
        LatLong gpsPosition = vehicleGps.getPosition();
        if (gpsPosition == null) {
            gpsPosition = new LatLong(newLat, newLong);
            vehicleGps.setPosition(gpsPosition);
            positionUpdated = true;
        } else if (gpsPosition.getLatitude() != newLat || gpsPosition.getLongitude() != newLong) {
            gpsPosition.setLatitude(newLat);
            gpsPosition.setLongitude(newLong);
            positionUpdated = true;
        }

        if (positionUpdated) {
            notifyAttributeListener(AttributeEvent.GPS_POSITION);
        }
    }

    private void processEfkStatus(msg_ekf_status_report ekf_status_report) {
        state.setEkfStatus(ekf_status_report);

        vehicleGps.setVehicleArmed(state.isArmed());
        vehicleGps.setEkfStatus(CommonApiUtils.generateEkfStatus(ekf_status_report));

        notifyAttributeListener(AttributeEvent.GPS_POSITION);
    }

    private void processGpsState(msg_gps_raw_int gpsState) {
        if (gpsState == null)
            return;

        double newEph = gpsState.eph / 100.0; // convert from eph(cm) to gps_eph(m)
        if (vehicleGps.getSatellitesCount() != gpsState.satellites_visible
            || vehicleGps.getGpsEph() != newEph) {
            vehicleGps.setSatCount(gpsState.satellites_visible);
            vehicleGps.setGpsEph(newEph);
            notifyAttributeListener(AttributeEvent.GPS_COUNT);
        }

        if (vehicleGps.getFixType() != gpsState.fix_type) {
            vehicleGps.setFixType(gpsState.fix_type);
            notifyAttributeListener(AttributeEvent.GPS_FIX);
        }
    }

    protected void requestHomeUpdate() {
        requestHomeUpdate(this);
    }

    private static void requestHomeUpdate(MavLinkDrone drone) {
        MavLinkWaypoint.requestWayPoint(drone, APMConstants.HOME_WAYPOINT_INDEX);
    }

}
