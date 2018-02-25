package org.droidplanner.services.android.impl.core.drone.autopilot.apm;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_camera_feedback;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;
import com.MAVLink.ardupilotmega.msg_mount_configure;
import com.MAVLink.ardupilotmega.msg_mount_status;
import com.MAVLink.ardupilotmega.msg_radio;
import com.MAVLink.common.msg_named_value_int;
import com.MAVLink.common.msg_raw_imu;
import com.MAVLink.common.msg_rc_channels_raw;
import com.MAVLink.common.msg_servo_output_raw;
import com.MAVLink.common.msg_statustext;
import com.MAVLink.common.msg_sys_status;
import com.MAVLink.common.msg_vfr_hud;
import com.MAVLink.enums.MAV_MOUNT_MODE;
import com.MAVLink.enums.MAV_SYS_STATUS_SENSOR;
import com.github.zafarkhaja.semver.Version;

import org.droidplanner.services.android.impl.communication.model.DataLink;
import org.droidplanner.services.android.impl.core.MAVLink.MavLinkParameters;
import org.droidplanner.services.android.impl.core.MAVLink.WaypointManager;
import org.droidplanner.services.android.impl.core.MAVLink.command.doCmd.MavLinkDoCmds;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces;
import org.droidplanner.services.android.impl.core.drone.LogMessageListener;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.variables.APMHeartBeat;
import org.droidplanner.services.android.impl.core.drone.autopilot.generic.GenericMavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.variables.ApmModes;
import org.droidplanner.services.android.impl.core.drone.variables.Camera;
import org.droidplanner.services.android.impl.core.drone.variables.GuidedPoint;
import org.droidplanner.services.android.impl.core.drone.variables.HeartBeat;
import org.droidplanner.services.android.impl.core.drone.variables.Magnetometer;
import org.droidplanner.services.android.impl.core.drone.variables.RC;
import org.droidplanner.services.android.impl.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.services.android.impl.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.model.AutopilotWarningParser;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.action.ControlActions;
import com.o3dr.services.android.lib.drone.action.ExperimentalActions;
import com.o3dr.services.android.lib.drone.action.GimbalActions;
import com.o3dr.services.android.lib.drone.action.ParameterActions;
import com.o3dr.services.android.lib.drone.action.StateActions;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.mission.action.MissionActions;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.action.CalibrationActions;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;
import org.droidplanner.services.android.impl.utils.CommonApiUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Base class for the ArduPilot autopilots
 */
public abstract class ArduPilot extends GenericMavLinkDrone {
    public static final int AUTOPILOT_COMPONENT_ID = 1;
    public static final int ARTOO_COMPONENT_ID = 0;

    public static final String FIRMWARE_VERSION_NUMBER_REGEX = "\\d+(\\.\\d{1,2})?";

    private final org.droidplanner.services.android.impl.core.drone.variables.RC rc;
    private final MissionImpl missionImpl;
    private final GuidedPoint guidedPoint;
    private final AccelCalibration accelCalibrationSetup;
    private final WaypointManager waypointManager;
    private final Magnetometer mag;
    private final Camera footprints;

    private final MagnetometerCalibrationImpl magCalibration;

    protected Version firmwareVersionNumber = Version.forIntegers(0, 0, 0);
    
    public ArduPilot(String droneId, Context context, DataLink.DataLinkProvider<MAVLinkMessage> mavClient,
                     Handler handler, AutopilotWarningParser warningParser,
                     LogMessageListener logListener) {

        super(droneId, context, handler, mavClient, warningParser, logListener);

        this.waypointManager = new WaypointManager(this, handler);

        rc = new RC(this);
        this.missionImpl = new MissionImpl(this);
        this.guidedPoint = new GuidedPoint(this, handler);
        this.accelCalibrationSetup = new AccelCalibration(this, handler);
        this.magCalibration = new MagnetometerCalibrationImpl(this);
        this.mag = new Magnetometer(this);
        this.footprints = new Camera(this);
    }

    @Override
    protected HeartBeat initHeartBeat(Handler handler) {
        return new APMHeartBeat(this, handler);
    }

    protected void setAltitudeGroundAndAirSpeeds(double altitude, double groundSpeed, double airSpeed, double climb) {
        if (this.altitude.getAltitude() != altitude) {
            this.altitude.setAltitude(altitude);
            notifyDroneEvent(DroneInterfaces.DroneEventsType.ALTITUDE);
        }

        if (speed.getGroundSpeed() != groundSpeed || speed.getAirSpeed() != airSpeed || speed.getVerticalSpeed() != climb) {
            speed.setGroundSpeed(groundSpeed);
            speed.setAirSpeed(airSpeed);
            speed.setVerticalSpeed(climb);

            notifyDroneEvent(DroneInterfaces.DroneEventsType.SPEED);
        }
    }

    @Override
    public WaypointManager getWaypointManager() {
        return waypointManager;
    }

    @Override
    public MissionImpl getMission() {
        return missionImpl;
    }

    @Override
    public GuidedPoint getGuidedPoint() {
        return guidedPoint;
    }

    @Override
    public AccelCalibration getCalibrationSetup() {
        return accelCalibrationSetup;
    }

    @Override
    public MagnetometerCalibrationImpl getMagnetometerCalibration() {
        return magCalibration;
    }

    public Camera getCamera() {
        return footprints;
    }

    @Override
    public DroneAttribute getAttribute(String attributeType) {
        if (!TextUtils.isEmpty(attributeType)) {
            switch (attributeType) {

                case AttributeType.MISSION:
                    return CommonApiUtils.getMission(this);

                case AttributeType.GUIDED_STATE:
                    return CommonApiUtils.getGuidedState(this);

                case AttributeType.MAGNETOMETER_CALIBRATION_STATUS:
                    return CommonApiUtils.getMagnetometerCalibrationStatus(this);
            }
        }

        return super.getAttribute(attributeType);
    }

    @Override
    public boolean executeAsyncAction(Action action, final ICommandListener listener) {
        String type = action.getType();
        Bundle data = action.getData();
        if (data == null) {
            data = new Bundle();
        }

        switch (type) {
            // MISSION ACTIONS
            case MissionActions.ACTION_LOAD_WAYPOINTS:
                CommonApiUtils.loadWaypoints(this);
                return true;

            case MissionActions.ACTION_SET_MISSION:
                data.setClassLoader(com.o3dr.services.android.lib.drone.mission.Mission.class.getClassLoader());
                com.o3dr.services.android.lib.drone.mission.Mission mission = data.getParcelable(MissionActions.EXTRA_MISSION);
                boolean pushToDrone = data.getBoolean(MissionActions.EXTRA_PUSH_TO_DRONE);
                CommonApiUtils.setMission(this, mission, pushToDrone);
                return true;

            case MissionActions.ACTION_START_MISSION:
                boolean forceModeChange = data.getBoolean(MissionActions.EXTRA_FORCE_MODE_CHANGE);
                boolean forceArm = data.getBoolean(MissionActions.EXTRA_FORCE_ARM);
                CommonApiUtils.startMission(this, forceModeChange, forceArm, listener);
                return true;

            // EXPERIMENTAL ACTIONS
            case ExperimentalActions.ACTION_EPM_COMMAND:
                boolean release = data.getBoolean(ExperimentalActions.EXTRA_EPM_RELEASE);
                CommonApiUtils.epmCommand(this, release, listener);
                return true;

            case ExperimentalActions.ACTION_TRIGGER_CAMERA:
                CommonApiUtils.triggerCamera(this);
                return true;

            case ExperimentalActions.ACTION_SET_ROI:
                LatLongAlt roi = data.getParcelable(ExperimentalActions.EXTRA_SET_ROI_LAT_LONG_ALT);
                if (roi != null) {
                    MavLinkDoCmds.setROI(this, roi, listener);
                }
                return true;

            case ExperimentalActions.ACTION_SET_RELAY:
                int relayNumber = data.getInt(ExperimentalActions.EXTRA_RELAY_NUMBER);
                boolean isOn = data.getBoolean(ExperimentalActions.EXTRA_IS_RELAY_ON);
                MavLinkDoCmds.setRelay(this, relayNumber, isOn, listener);
                return true;

            case ExperimentalActions.ACTION_SET_SERVO:
                int channel = data.getInt(ExperimentalActions.EXTRA_SERVO_CHANNEL);
                int pwm = data.getInt(ExperimentalActions.EXTRA_SERVO_PWM);
                MavLinkDoCmds.setServo(this, channel, pwm, listener);
                return true;

            // CONTROL ACTIONS
            case ControlActions.ACTION_SEND_GUIDED_POINT: {
                data.setClassLoader(LatLong.class.getClassLoader());
                boolean force = data.getBoolean(ControlActions.EXTRA_FORCE_GUIDED_POINT);
                LatLong guidedPoint = data.getParcelable(ControlActions.EXTRA_GUIDED_POINT);
                CommonApiUtils.sendGuidedPoint(this, guidedPoint, force, listener);
                return true;
            }

            case ControlActions.ACTION_LOOK_AT_TARGET:
                boolean force = data.getBoolean(ControlActions.EXTRA_FORCE_GUIDED_POINT);
                LatLongAlt lookAtTarget = data.getParcelable(ControlActions.EXTRA_LOOK_AT_TARGET);
                CommonApiUtils.sendLookAtTarget(this, lookAtTarget, force, listener);
                return true;

            case ControlActions.ACTION_SET_GUIDED_ALTITUDE:
                double guidedAltitude = data.getDouble(ControlActions.EXTRA_ALTITUDE);
                CommonApiUtils.setGuidedAltitude(this, guidedAltitude);
                return true;

            // PARAMETER ACTIONS
            case ParameterActions.ACTION_REFRESH_PARAMETERS:
                CommonApiUtils.refreshParameters(this);
                return true;

            case ParameterActions.ACTION_WRITE_PARAMETERS:
                data.setClassLoader(com.o3dr.services.android.lib.drone.property.Parameters.class.getClassLoader());
                com.o3dr.services.android.lib.drone.property.Parameters parameters = data.getParcelable(ParameterActions.EXTRA_PARAMETERS);
                CommonApiUtils.writeParameters(this, parameters);
                return true;

            // DRONE STATE ACTIONS
            case StateActions.ACTION_SET_VEHICLE_HOME:
                LatLongAlt homeLoc = data.getParcelable(StateActions.EXTRA_VEHICLE_HOME_LOCATION);
                if (homeLoc != null) {
                    MavLinkDoCmds.setVehicleHome(this, homeLoc, new AbstractCommandListener() {
                        @Override
                        public void onSuccess() {
                            CommonApiUtils.postSuccessEvent(listener);
                            requestHomeUpdate();
                        }

                        @Override
                        public void onError(int executionError) {
                            CommonApiUtils.postErrorEvent(executionError, listener);
                            requestHomeUpdate();
                        }

                        @Override
                        public void onTimeout() {
                            CommonApiUtils.postTimeoutEvent(listener);
                            requestHomeUpdate();
                        }
                    });
                } else {
                    CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                }
                return true;

            //CALIBRATION ACTIONS
            case CalibrationActions.ACTION_START_IMU_CALIBRATION:
                CommonApiUtils.startIMUCalibration(this, listener);
                return true;

            case CalibrationActions.ACTION_SEND_IMU_CALIBRATION_ACK:
                int imuAck = data.getInt(CalibrationActions.EXTRA_IMU_STEP);
                CommonApiUtils.sendIMUCalibrationAck(this, imuAck);
                return true;

            case CalibrationActions.ACTION_START_MAGNETOMETER_CALIBRATION:
                boolean retryOnFailure = data.getBoolean(CalibrationActions.EXTRA_RETRY_ON_FAILURE, false);
                boolean saveAutomatically = data.getBoolean(CalibrationActions.EXTRA_SAVE_AUTOMATICALLY, true);
                int startDelay = data.getInt(CalibrationActions.EXTRA_START_DELAY, 0);
                CommonApiUtils.startMagnetometerCalibration(this, retryOnFailure, saveAutomatically, startDelay);
                return true;

            case CalibrationActions.ACTION_CANCEL_MAGNETOMETER_CALIBRATION:
                CommonApiUtils.cancelMagnetometerCalibration(this);
                return true;

            case CalibrationActions.ACTION_ACCEPT_MAGNETOMETER_CALIBRATION:
                CommonApiUtils.acceptMagnetometerCalibration(this);
                return true;

            //************ Gimbal ACTIONS *************//
            case GimbalActions.ACTION_SET_GIMBAL_ORIENTATION:
                float pitch = data.getFloat(GimbalActions.GIMBAL_PITCH);
                float roll = data.getFloat(GimbalActions.GIMBAL_ROLL);
                float yaw = data.getFloat(GimbalActions.GIMBAL_YAW);
                MavLinkDoCmds.setGimbalOrientation(this, pitch, roll, yaw, listener);
                return true;

            case GimbalActions.ACTION_RESET_GIMBAL_MOUNT_MODE:
            case GimbalActions.ACTION_SET_GIMBAL_MOUNT_MODE:
                int mountMode = data.getInt(GimbalActions.GIMBAL_MOUNT_MODE, MAV_MOUNT_MODE.MAV_MOUNT_MODE_RC_TARGETING);
                Timber.i("Setting gimbal mount mode: %d", mountMode);

                Parameter mountParam = getParameterManager().getParameter("MNT_MODE");
                if (mountParam == null) {
                    msg_mount_configure msg = new msg_mount_configure();
                    msg.target_system = getSysid();
                    msg.target_component = getCompid();
                    msg.mount_mode = (byte) mountMode;
                    msg.stab_pitch = 0;
                    msg.stab_roll = 0;
                    msg.stab_yaw = 0;
                    getMavClient().sendMessage(msg, listener);
                } else {
                    MavLinkParameters.sendParameter(this, "MNT_MODE", 1, mountMode);
                }
                return true;

            default:
                return super.executeAsyncAction(action, listener);
        }
    }

    @Override
    protected boolean enableManualControl(Bundle data, ICommandListener listener) {
        CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
        return true;
    }

    @Override
    protected boolean performArming(Bundle data, ICommandListener listener) {
        boolean doArm = data.getBoolean(StateActions.EXTRA_ARM);
        boolean emergencyDisarm = data.getBoolean(StateActions.EXTRA_EMERGENCY_DISARM);
        CommonApiUtils.arm(this, doArm, emergencyDisarm, listener);
        return true;
    }

    @Override
    protected boolean setVehicleMode(Bundle data, ICommandListener listener) {
        data.setClassLoader(VehicleMode.class.getClassLoader());
        VehicleMode newMode = data.getParcelable(StateActions.EXTRA_VEHICLE_MODE);
        CommonApiUtils.changeVehicleMode(this, newMode, listener);
        return true;
    }

    @Override
    protected boolean setVelocity(Bundle data, ICommandListener listener) {
        CommonApiUtils.postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
        return true;
    }

    @Override
    protected boolean performTakeoff(Bundle data, ICommandListener listener) {
        double takeoffAltitude = data.getDouble(ControlActions.EXTRA_ALTITUDE);
        CommonApiUtils.doGuidedTakeoff(this, takeoffAltitude, listener);
        return true;
    }

    @Override
    public void onMavLinkMessageReceived(MAVLinkMessage message) {

        if ((message.sysid != this.getSysid()) && !isMavLinkMessageException(message)) {
            // Reject Messages that are not for the system id
            return;
        }

        // Filter Components IDs to be specifically the IDs that can be processed
        int compId = message.compid;
        if (compId != AUTOPILOT_COMPONENT_ID
                && compId != ARTOO_COMPONENT_ID
                && compId != SiK_RADIO_FIXED_COMPID ){
            return;
        }

        if (!getParameterManager().processMessage(message)) {

            getWaypointManager().processMessage(message);
            getCalibrationSetup().processMessage(message);

            switch (message.msgid) {

                case msg_statustext.MAVLINK_MSG_ID_STATUSTEXT:
                    // These are any warnings sent from APM:Copter with
                    // gcs_send_text_P()
                    // This includes important thing like arm fails, prearm fails, low
                    // battery, etc.
                    // also less important things like "erasing logs" and
                    // "calibrating barometer"
                    msg_statustext msg_statustext = (msg_statustext) message;
                    processStatusText(msg_statustext);
                    break;

                case msg_vfr_hud.MAVLINK_MSG_ID_VFR_HUD:
                    processVfrHud((msg_vfr_hud) message);
                    break;

                case msg_raw_imu.MAVLINK_MSG_ID_RAW_IMU:
                    msg_raw_imu msg_imu = (msg_raw_imu) message;
                    mag.newData(msg_imu);
                    break;

                case msg_radio.MAVLINK_MSG_ID_RADIO:
                    msg_radio m_radio = (msg_radio) message;
                    processSignalUpdate(m_radio.rxerrors, m_radio.fixed, m_radio.rssi,
                            m_radio.remrssi, m_radio.txbuf, m_radio.noise, m_radio.remnoise);
                    break;

                case msg_rc_channels_raw.MAVLINK_MSG_ID_RC_CHANNELS_RAW:
                    rc.setRcInputValues((msg_rc_channels_raw) message);
                    break;

                case msg_servo_output_raw.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW:
                    rc.setRcOutputValues((msg_servo_output_raw) message);
                    break;

                case msg_camera_feedback.MAVLINK_MSG_ID_CAMERA_FEEDBACK:
                    getCamera().newImageLocation((msg_camera_feedback) message);
                    break;

                case msg_mount_status.MAVLINK_MSG_ID_MOUNT_STATUS:
                    processMountStatus((msg_mount_status) message);
                    break;

                case msg_named_value_int.MAVLINK_MSG_ID_NAMED_VALUE_INT:
                    processNamedValueInt((msg_named_value_int) message);
                    break;

                //*************** Magnetometer calibration messages handling *************//
                case msg_mag_cal_progress.MAVLINK_MSG_ID_MAG_CAL_PROGRESS:
                case msg_mag_cal_report.MAVLINK_MSG_ID_MAG_CAL_REPORT:
                    getMagnetometerCalibration().processCalibrationMessage(message);
                    break;

                default:
                    break;
            }
        }

        super.onMavLinkMessageReceived(message);
    }

    @Override
    protected void processSysStatus(msg_sys_status m_sys) {
        super.processSysStatus(m_sys);
        checkControlSensorsHealth(m_sys);
    }

    @Override
    protected final void setFirmwareVersion(String message) {
        super.setFirmwareVersion(message);
        setFirmwareVersionNumber(message);
    }

    protected Version getFirmwareVersionNumber() {
        return firmwareVersionNumber;
    }

    private void setFirmwareVersionNumber(String message) {
        firmwareVersionNumber = extractVersionNumber(message);
    }

    protected static Version extractVersionNumber(String firmwareVersion) {
        Version version = Version.forIntegers(0, 0, 0);

        Pattern pattern = Pattern.compile(FIRMWARE_VERSION_NUMBER_REGEX);
        Matcher matcher = pattern.matcher(firmwareVersion);
        if (matcher.find()) {
            String versionNumber = matcher.group(0) + ".0"; // Adding a default patch version number for successful parsing.

            try {
                version = Version.valueOf(versionNumber);
            } catch (Exception e){
                Timber.e(e, "Firmware version invalid");
            }
        }

        return version;
    }

    private void checkControlSensorsHealth(msg_sys_status sysStatus) {
        boolean isRCFailsafe = (sysStatus.onboard_control_sensors_health & MAV_SYS_STATUS_SENSOR
                .MAV_SYS_STATUS_SENSOR_RC_RECEIVER) == 0;
        if (isRCFailsafe) {
            getState().parseAutopilotError("RC FAILSAFE");
        }
    }

    protected void processVfrHud(msg_vfr_hud vfrHud) {
        if (vfrHud == null)
            return;

        setAltitudeGroundAndAirSpeeds(vfrHud.alt, vfrHud.groundspeed, vfrHud.airspeed, vfrHud.climb);
    }

    protected void processMountStatus(msg_mount_status mountStatus) {
        footprints.updateMountOrientation(mountStatus);

        Bundle eventInfo = new Bundle(3);
        eventInfo.putFloat(AttributeEventExtra.EXTRA_GIMBAL_ORIENTATION_PITCH, mountStatus.pointing_a / 100f);
        eventInfo.putFloat(AttributeEventExtra.EXTRA_GIMBAL_ORIENTATION_ROLL, mountStatus.pointing_b / 100f);
        eventInfo.putFloat(AttributeEventExtra.EXTRA_GIMBAL_ORIENTATION_YAW, mountStatus.pointing_c / 100f);
        notifyAttributeListener(AttributeEvent.GIMBAL_ORIENTATION_UPDATED, eventInfo);
    }

    private void processNamedValueInt(msg_named_value_int message) {
        if (message == null)
            return;

        switch (message.getName()) {
            case "ARMMASK":
                //Give information about the vehicle's ability to arm successfully.
                ApmModes vehicleMode = getState().getMode();
                if (ApmModes.isCopter(vehicleMode.getType())) {
                    int value = message.value;
                    boolean isReadyToArm = (value & (1 << vehicleMode.getNumber())) != 0;
                    String armReadinessMsg = isReadyToArm ? "READY TO ARM" : "UNREADY FOR ARMING";
                    logMessage(Log.INFO, armReadinessMsg);
                }
                break;
        }
    }

    protected void processStatusText(msg_statustext statusText) {
        String message = statusText.getText();
        if (TextUtils.isEmpty(message))
            return;

        if (message.startsWith("ArduCopter") || message.startsWith("ArduPlane")
                || message.startsWith("ArduRover") || message.startsWith("Solo")
                || message.startsWith("APM:Copter") || message.startsWith("APM:Plane")
                || message.startsWith("APM:Rover")) {
            setFirmwareVersion(message);
        } else {

            //Try parsing as an error.
            if (!getState().parseAutopilotError(message)) {

                //Relay to the connected client.
                int logLevel;
                switch (statusText.severity) {
                    case APMConstants.Severity.SEVERITY_CRITICAL:
                        logLevel = Log.ERROR;
                        break;

                    case APMConstants.Severity.SEVERITY_HIGH:
                        logLevel = Log.WARN;
                        break;

                    case APMConstants.Severity.SEVERITY_MEDIUM:
                        logLevel = Log.INFO;
                        break;

                    default:
                    case APMConstants.Severity.SEVERITY_LOW:
                        logLevel = Log.VERBOSE;
                        break;

                    case APMConstants.Severity.SEVERITY_USER_RESPONSE:
                        logLevel = Log.DEBUG;
                        break;
                }

                logMessage(logLevel, message);
            }
        }
    }

    public Double getBattDischarge(double battRemain) {
        Parameter battCap = getParameterManager().getParameter("BATT_CAPACITY");
        if (battCap == null || battRemain == -1) {
            return null;
        }
        return (1 - battRemain / 100.0) * battCap.getValue();
    }

    @Override
    protected void processBatteryUpdate(double voltage, double remain, double current) {
        if (battery.getBatteryRemain() != remain) {
            battery.setBatteryDischarge(getBattDischarge(remain));
        }
        super.processBatteryUpdate(voltage, remain, current);
    }
}
