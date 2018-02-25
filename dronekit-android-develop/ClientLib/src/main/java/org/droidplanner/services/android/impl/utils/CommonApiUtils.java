package org.droidplanner.services.android.impl.utils;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Surface;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_ekf_status_report;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;
import com.MAVLink.enums.MAG_CAL_STATUS;
import com.MAVLink.enums.MAV_TYPE;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationProgress;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationResult;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationStatus;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.o3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.property.CameraProxy;
import com.o3dr.services.android.lib.drone.property.EkfStatus;
import com.o3dr.services.android.lib.drone.property.FootPrint;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.drone.property.Vibration;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.mavlink.MavlinkMessageWrapper;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.services.android.impl.core.MAVLink.MavLinkCommands;
import org.droidplanner.services.android.impl.core.MAVLink.command.doCmd.MavLinkDoCmds;
import org.droidplanner.services.android.impl.core.drone.autopilot.Drone;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.autopilot.apm.ArduPilot;
import org.droidplanner.services.android.impl.core.drone.autopilot.generic.GenericMavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.profiles.ParameterManager;
import org.droidplanner.services.android.impl.core.drone.variables.ApmModes;
import org.droidplanner.services.android.impl.core.drone.variables.Camera;
import org.droidplanner.services.android.impl.core.drone.variables.GuidedPoint;
import org.droidplanner.services.android.impl.core.drone.variables.calibration.AccelCalibration;
import org.droidplanner.services.android.impl.core.drone.variables.calibration.MagnetometerCalibrationImpl;
import org.droidplanner.services.android.impl.core.firmware.FirmwareType;
import org.droidplanner.services.android.impl.core.gcs.follow.Follow;
import org.droidplanner.services.android.impl.core.gcs.follow.FollowAlgorithm;
import org.droidplanner.services.android.impl.core.mission.MissionImpl;
import org.droidplanner.services.android.impl.core.mission.MissionItemImpl;
import org.droidplanner.services.android.impl.core.mission.survey.SplineSurveyImpl;
import org.droidplanner.services.android.impl.core.mission.survey.SurveyImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.StructureScannerImpl;
import org.droidplanner.services.android.impl.core.survey.Footprint;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 3/23/15.
 */
public class CommonApiUtils {

    //Private to prevent instantiation
    private CommonApiUtils() {
    }

    public static void postSuccessEvent(ICommandListener listener) {
        if (listener != null) {
            try {
                listener.onSuccess();
            } catch (RemoteException e) {
                Timber.e(e, e.getMessage());
            }
        }
    }

    public static void postErrorEvent(int errorCode, ICommandListener listener) {
        if (listener != null) {
            try {
                listener.onError(errorCode);
            } catch (RemoteException e) {
                Timber.e(e, e.getMessage());
            }
        }
    }

    public static void postTimeoutEvent(ICommandListener listener) {
        if (listener != null) {
            try {
                listener.onTimeout();
            } catch (RemoteException e) {
                Timber.e(e, e.getMessage());
            }
        }
    }

    public static VehicleMode getVehicleMode(ApmModes mode) {
        switch (mode) {
            case FIXED_WING_MANUAL:
                return VehicleMode.PLANE_MANUAL;
            case FIXED_WING_CIRCLE:
                return VehicleMode.PLANE_CIRCLE;

            case FIXED_WING_STABILIZE:
                return VehicleMode.PLANE_STABILIZE;

            case FIXED_WING_TRAINING:
                return VehicleMode.PLANE_TRAINING;

            case FIXED_WING_ACRO:
                return VehicleMode.PLANE_ACRO;

            case FIXED_WING_FLY_BY_WIRE_A:
                return VehicleMode.PLANE_FLY_BY_WIRE_A;

            case FIXED_WING_FLY_BY_WIRE_B:
                return VehicleMode.PLANE_FLY_BY_WIRE_B;

            case FIXED_WING_CRUISE:
                return VehicleMode.PLANE_CRUISE;

            case FIXED_WING_AUTOTUNE:
                return VehicleMode.PLANE_AUTOTUNE;

            case FIXED_WING_AUTO:
                return VehicleMode.PLANE_AUTO;

            case FIXED_WING_RTL:
                return VehicleMode.PLANE_RTL;

            case FIXED_WING_LOITER:
                return VehicleMode.PLANE_LOITER;

            case FIXED_WING_GUIDED:
                return VehicleMode.PLANE_GUIDED;


            case ROTOR_STABILIZE:
                return VehicleMode.COPTER_STABILIZE;

            case ROTOR_ACRO:
                return VehicleMode.COPTER_ACRO;

            case ROTOR_ALT_HOLD:
                return VehicleMode.COPTER_ALT_HOLD;

            case ROTOR_AUTO:
                return VehicleMode.COPTER_AUTO;

            case ROTOR_GUIDED:
                return VehicleMode.COPTER_GUIDED;

            case ROTOR_LOITER:
                return VehicleMode.COPTER_LOITER;

            case ROTOR_RTL:
                return VehicleMode.COPTER_RTL;

            case ROTOR_CIRCLE:
                return VehicleMode.COPTER_CIRCLE;

            case ROTOR_LAND:
                return VehicleMode.COPTER_LAND;

            case ROTOR_TOY:
                return VehicleMode.COPTER_DRIFT;

            case ROTOR_SPORT:
                return VehicleMode.COPTER_SPORT;

            case ROTOR_AUTOTUNE:
                return VehicleMode.COPTER_AUTOTUNE;

            case ROTOR_POSHOLD:
                return VehicleMode.COPTER_POSHOLD;

            case ROTOR_BRAKE:
                return VehicleMode.COPTER_BRAKE;


            case ROVER_MANUAL:
                return VehicleMode.ROVER_MANUAL;

            case ROVER_LEARNING:
                return VehicleMode.ROVER_LEARNING;

            case ROVER_STEERING:
                return VehicleMode.ROVER_STEERING;

            case ROVER_HOLD:
                return VehicleMode.ROVER_HOLD;

            case ROVER_AUTO:
                return VehicleMode.ROVER_AUTO;

            case ROVER_RTL:
                return VehicleMode.ROVER_RTL;

            case ROVER_GUIDED:
                return VehicleMode.ROVER_GUIDED;

            case ROVER_INITIALIZING:
                return VehicleMode.ROVER_INITIALIZING;

            default:
            case UNKNOWN:
                return null;

        }
    }

    public static int getDroneProxyType(int originalType) {
        switch (originalType) {
            case MAV_TYPE.MAV_TYPE_TRICOPTER:
            case MAV_TYPE.MAV_TYPE_QUADROTOR:
            case MAV_TYPE.MAV_TYPE_HEXAROTOR:
            case MAV_TYPE.MAV_TYPE_OCTOROTOR:
            case MAV_TYPE.MAV_TYPE_HELICOPTER:
                return Type.TYPE_COPTER;

            case MAV_TYPE.MAV_TYPE_FIXED_WING:
                return Type.TYPE_PLANE;

            case MAV_TYPE.MAV_TYPE_GROUND_ROVER:
            case MAV_TYPE.MAV_TYPE_SURFACE_BOAT:
                return Type.TYPE_ROVER;

            default:
                return -1;
        }
    }

    public static FootPrint getProxyCameraFootPrint(Footprint footprint) {
        if (footprint == null) return null;

        return new FootPrint(footprint.getGSD(), footprint.getVertexInGlobalFrame());
    }

    public static FollowAlgorithm.FollowModes followTypeToMode(MavLinkDrone drone, FollowType followType) {
        FollowAlgorithm.FollowModes followMode;

        switch (followType) {
            case ABOVE:
                followMode = (drone.getFirmwareType() == FirmwareType.ARDU_SOLO)
                        ? FollowAlgorithm.FollowModes.SPLINE_ABOVE
                        : FollowAlgorithm.FollowModes.ABOVE;
                break;

            case LEAD:
                followMode = FollowAlgorithm.FollowModes.LEAD;
                break;

            default:
            case LEASH:
                followMode = (drone.getFirmwareType() == FirmwareType.ARDU_SOLO)
                        ? FollowAlgorithm.FollowModes.SPLINE_LEASH
                        : FollowAlgorithm.FollowModes.LEASH;
                break;

            case CIRCLE:
                followMode = FollowAlgorithm.FollowModes.CIRCLE;
                break;

            case LEFT:
                followMode = FollowAlgorithm.FollowModes.LEFT;
                break;

            case RIGHT:
                followMode = FollowAlgorithm.FollowModes.RIGHT;
                break;

            case GUIDED_SCAN:
                followMode = FollowAlgorithm.FollowModes.GUIDED_SCAN;
                break;

            case LOOK_AT_ME:
                followMode = FollowAlgorithm.FollowModes.LOOK_AT_ME;
                break;

            case SOLO_SHOT:
                followMode = FollowAlgorithm.FollowModes.SOLO_SHOT;
                break;
        }
        return followMode;
    }

    public static FollowType followModeToType(FollowAlgorithm.FollowModes followMode) {
        FollowType followType;

        switch (followMode) {
            default:
            case LEASH:
            case SPLINE_LEASH:
                followType = FollowType.LEASH;
                break;

            case LEAD:
                followType = FollowType.LEAD;
                break;

            case RIGHT:
                followType = FollowType.RIGHT;
                break;

            case LEFT:
                followType = FollowType.LEFT;
                break;

            case CIRCLE:
                followType = FollowType.CIRCLE;
                break;

            case ABOVE:
            case SPLINE_ABOVE:
                followType = FollowType.ABOVE;
                break;

            case GUIDED_SCAN:
                followType = FollowType.GUIDED_SCAN;
                break;

            case LOOK_AT_ME:
                followType = FollowType.LOOK_AT_ME;
                break;

            case SOLO_SHOT:
                followType = FollowType.SOLO_SHOT;
                break;
        }

        return followType;
    }

    public static CameraProxy getCameraProxy(Drone drone, List<CameraDetail> cameraDetails) {
        CameraDetail camDetail;
        FootPrint currentFieldOfView;
        List<FootPrint> proxyPrints = new ArrayList<>();

        if (!(drone instanceof MavLinkDrone)) {
            camDetail = new CameraDetail();
            currentFieldOfView = new FootPrint();
        } else {
            Camera droneCamera = ((MavLinkDrone) drone).getCamera();

            camDetail = ProxyUtils.getCameraDetail(droneCamera.getCamera());

            List<Footprint> footprints = droneCamera.getFootprints();
            for (Footprint footprint : footprints) {
                proxyPrints.add(CommonApiUtils.getProxyCameraFootPrint(footprint));
            }

            Gps droneGps = (Gps) drone.getAttribute(AttributeType.GPS);
            currentFieldOfView = droneGps != null && droneGps.isValid()
                    ? CommonApiUtils.getProxyCameraFootPrint(droneCamera.getCurrentFieldOfView())
                    : new FootPrint();
        }

        return new CameraProxy(camDetail, currentFieldOfView, proxyPrints, cameraDetails);
    }

    public static State getState(MavLinkDrone drone, boolean isConnected, Vibration vibration) {
        if (drone == null)
            return new State();

        org.droidplanner.services.android.impl.core.drone.variables.State droneState = drone.getState();
        ApmModes droneMode = droneState.getMode();
        AccelCalibration accelCalibration = drone.getCalibrationSetup();
        String calibrationMessage = accelCalibration != null && accelCalibration.isCalibrating()
                ? accelCalibration.getMessage()
                : null;

        return new State(isConnected, CommonApiUtils.getVehicleMode(droneMode), droneState.isArmed(),
            droneState.isFlying(), droneState.getErrorId(), drone.getMavlinkVersion(), calibrationMessage,
            droneState.getFlightStartTime(), generateEkfStatus(droneState.getEkfStatus()),
            isConnected && drone.isConnectionAlive(), vibration);
    }

    public static EkfStatus generateEkfStatus(msg_ekf_status_report ekfStatus) {
        if (ekfStatus == null) {
            return null;
        }

        EkfStatus proxyEkfStatus = new EkfStatus(ekfStatus.flags, ekfStatus.compass_variance,
            ekfStatus.pos_horiz_variance, ekfStatus.terrain_alt_variance, ekfStatus.velocity_variance,
            ekfStatus.pos_vert_variance);

        return proxyEkfStatus;
    }

    public static Mission getMission(MavLinkDrone drone) {
        Mission proxyMission = new Mission();
        if (drone == null)
            return proxyMission;

        MissionImpl droneMissionImpl = drone.getMission();
        List<MissionItemImpl> droneMissionItemImpls = droneMissionImpl.getComponentItems();


        proxyMission.setCurrentMissionItem((short) drone.getMissionStats().getCurrentWP());
        if (!droneMissionItemImpls.isEmpty()) {
            for (MissionItemImpl item : droneMissionItemImpls) {
                proxyMission.addMissionItem(ProxyUtils.getProxyMissionItem(item));
            }
        }

        return proxyMission;
    }

    public static Type getType(MavLinkDrone drone) {
        if (drone == null)
            return new Type();

        return new Type(CommonApiUtils.getDroneProxyType(drone.getType()), drone.getFirmwareVersion());
    }

    public static GuidedState getGuidedState(MavLinkDrone drone) {
        if (drone == null)
            return new GuidedState();

        GuidedPoint guidedPoint = drone.getGuidedPoint();
        int guidedState;
        switch (guidedPoint.getState()) {
            default:
            case UNINITIALIZED:
                guidedState = GuidedState.STATE_UNINITIALIZED;
                break;

            case ACTIVE:
                guidedState = GuidedState.STATE_ACTIVE;
                break;

            case IDLE:
                guidedState = GuidedState.STATE_IDLE;
                break;
        }

        LatLong guidedCoord = guidedPoint.getCoord();
        if (guidedCoord == null) {
            guidedCoord = new LatLong(0, 0);
        }

        double guidedAlt = guidedPoint.getAltitude();
        return new GuidedState(guidedState, new LatLongAlt(guidedCoord, guidedAlt));
    }

    public static void changeVehicleMode(MavLinkDrone drone, VehicleMode newMode, ICommandListener listener) {
        if (drone == null)
            return;

        int mavType;
        switch (newMode.getDroneType()) {
            default:
            case Type.TYPE_COPTER:
                mavType = MAV_TYPE.MAV_TYPE_QUADROTOR;
                break;

            case Type.TYPE_PLANE:
                mavType = MAV_TYPE.MAV_TYPE_FIXED_WING;
                break;

            case Type.TYPE_ROVER:
                mavType = MAV_TYPE.MAV_TYPE_GROUND_ROVER;
                break;
        }

        drone.getState().changeFlightMode(ApmModes.getMode(newMode.getMode(), mavType), listener);
    }

    public static FollowState getFollowState(Follow followMe) {
        if (followMe == null)
            return new FollowState();

        int state;
        switch (followMe.getState()) {

            default:
            case FOLLOW_INVALID_STATE:
                state = FollowState.STATE_INVALID;
                break;

            case FOLLOW_DRONE_NOT_ARMED:
                state = FollowState.STATE_DRONE_NOT_ARMED;
                break;

            case FOLLOW_DRONE_DISCONNECTED:
                state = FollowState.STATE_DRONE_DISCONNECTED;
                break;

            case FOLLOW_START:
                state = FollowState.STATE_START;
                break;

            case FOLLOW_RUNNING:
                state = FollowState.STATE_RUNNING;
                break;

            case FOLLOW_END:
                state = FollowState.STATE_END;
                break;
        }

        FollowAlgorithm currentAlg = followMe.getFollowAlgorithm();
        Map<String, Object> modeParams = currentAlg.getParams();
        Bundle params = new Bundle();
        for (Map.Entry<String, Object> entry : modeParams.entrySet()) {
            switch (entry.getKey()) {
                case FollowType.EXTRA_FOLLOW_ROI_TARGET:
                    LatLongAlt target = (LatLongAlt) entry.getValue();
                    if (target != null) {
                        params.putParcelable(entry.getKey(), target);
                    }
                    break;

                case FollowType.EXTRA_FOLLOW_RADIUS:
                    Double radius = (Double) entry.getValue();
                    if (radius != null)
                        params.putDouble(entry.getKey(), radius);
                    break;
            }
        }
        return new FollowState(state, CommonApiUtils.followModeToType(currentAlg.getType()), params);
    }

    public static void disableFollowMe(Follow follow) {
        if(follow != null) {
            follow.disableFollowMe();
        }
    }

    public static void triggerCamera(MavLinkDrone drone) {
        if (drone == null)
            return;
        MavLinkDoCmds.triggerCamera(drone);
    }

    public static void epmCommand(MavLinkDrone drone, boolean release, ICommandListener listener) {
        if (drone == null)
            return;

        MavLinkDoCmds.empCommand(drone, release, listener);
    }

    public static void loadWaypoints(MavLinkDrone drone) {
        if (drone == null)
            return;
        drone.getWaypointManager().getWaypoints();
    }

    public static void refreshParameters(MavLinkDrone drone) {
        if (drone == null)
            return;
        drone.getParameterManager().refreshParameters();
    }

    public static void writeParameters(MavLinkDrone drone, Parameters parameters) {
        if (drone == null || parameters == null) return;

        List<Parameter> parametersList = parameters.getParameters();
        if (parametersList.isEmpty())
            return;

        ParameterManager droneParams = drone.getParameterManager();
        for (Parameter proxyParam : parametersList) {
            droneParams.sendParameter(proxyParam);
        }
    }

    public static void setMission(MavLinkDrone drone, Mission mission, boolean pushToDrone) {
        if (drone == null)
            return;

        MissionImpl droneMissionImpl = drone.getMission();
        droneMissionImpl.clearMissionItems();

        List<MissionItem> itemsList = mission.getMissionItems();
        for (MissionItem item : itemsList) {
            droneMissionImpl.addMissionItem(ProxyUtils.getMissionItemImpl(droneMissionImpl, item));
        }

        if (pushToDrone)
            droneMissionImpl.sendMissionToAPM();
    }

    public static void startMission(final ArduPilot drone, final boolean forceModeChange, boolean forceArm, final ICommandListener listener) {
        if (drone == null) {
            return;
        }

        final Runnable sendCommandRunnable = new Runnable() {
            @Override
            public void run() {
                MavLinkCommands.startMission(drone, listener);
            }
        };

        final Runnable modeCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (drone.getState().getMode() != ApmModes.ROTOR_AUTO) {
                    if (forceModeChange) {
                        changeVehicleMode(drone, VehicleMode.COPTER_AUTO, new AbstractCommandListener() {
                            @Override
                            public void onSuccess() {
                                sendCommandRunnable.run();
                            }

                            @Override
                            public void onError(int executionError) {
                                postErrorEvent(executionError, listener);
                            }

                            @Override
                            public void onTimeout() {
                                postTimeoutEvent(listener);
                            }
                        });
                    } else {
                        postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
                    }
                    return;
                } else {
                    sendCommandRunnable.run();
                }
            }
        };

        if (!drone.getState().isArmed()) {
            if (forceArm) {
                arm(drone, true, new AbstractCommandListener() {
                    @Override
                    public void onSuccess() {
                        modeCheckRunnable.run();
                    }

                    @Override
                    public void onError(int executionError) {
                        postErrorEvent(executionError, listener);
                    }

                    @Override
                    public void onTimeout() {
                        postTimeoutEvent(listener);
                    }
                });
            } else {
                postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
            }
            return;
        }

        modeCheckRunnable.run();
    }

    public static float generateDronie(MavLinkDrone drone) {
        if (drone == null)
            return -1;

        return (float) drone.getMission().makeAndUploadDronie();
    }

    public static void arm(ArduPilot drone, boolean arm, ICommandListener listener) {
        arm(drone, arm, false, listener);
    }

    public static void arm(final ArduPilot drone, final boolean arm, final boolean emergencyDisarm, final ICommandListener listener) {
        if (drone == null)
            return;

        if (!arm && emergencyDisarm) {
            if (org.droidplanner.services.android.impl.core.drone.variables.Type.isCopter(drone.getType()) && !isKillSwitchSupported(drone)) {

                changeVehicleMode(drone, VehicleMode.COPTER_STABILIZE, new AbstractCommandListener() {
                    @Override
                    public void onSuccess() {
                        MavLinkCommands.sendArmMessage(drone, arm, emergencyDisarm, listener);
                    }

                    @Override
                    public void onError(int executionError) {
                        if (listener != null) {
                            try {
                                listener.onError(executionError);
                            } catch (RemoteException e) {
                                Timber.e(e, e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onTimeout() {
                        if (listener != null) {
                            try {
                                listener.onTimeout();
                            } catch (RemoteException e) {
                                Timber.e(e, e.getMessage());
                            }
                        }
                    }
                });

                return;
            }
        }

        MavLinkCommands.sendArmMessage(drone, arm, emergencyDisarm, listener);
    }

    /**
     * Check if the kill switch feature is supported on the given drone
     *
     * @param drone
     * @return true if it's supported, false otherwise.
     */
    public static boolean isKillSwitchSupported(MavLinkDrone drone) {
        if (drone == null)
            return false;

        if (!org.droidplanner.services.android.impl.core.drone.variables.Type.isCopter(drone.getType()))
            return false;

        String firmwareVersion = drone.getFirmwareVersion();
        if (TextUtils.isEmpty(firmwareVersion))
            return false;

        return !(!firmwareVersion.startsWith("APM:Copter V3.3")
                && !firmwareVersion.startsWith("APM:Copter V3.4")
                && !firmwareVersion.startsWith("Solo"));

    }

    public static void startMagnetometerCalibration(MavLinkDrone drone, boolean retryOnFailure, boolean saveAutomatically, int
            startDelay) {
        if (drone == null)
            return;

        drone.getMagnetometerCalibration().startCalibration(retryOnFailure, saveAutomatically, startDelay);
    }

    public static void cancelMagnetometerCalibration(MavLinkDrone drone) {
        if (drone == null)
            return;

        drone.getMagnetometerCalibration().cancelCalibration();
    }

    public static void acceptMagnetometerCalibration(MavLinkDrone drone) {
        if (drone == null)
            return;

        drone.getMagnetometerCalibration().acceptCalibration();
    }

    public static void startIMUCalibration(MavLinkDrone drone, ICommandListener listener) {
        if (drone != null)
            drone.getCalibrationSetup().startCalibration(listener);
    }

    public static void sendIMUCalibrationAck(MavLinkDrone drone, int step) {
        if (drone == null)
            return;

        drone.getCalibrationSetup().sendAck(step);
    }

    public static void doGuidedTakeoff(MavLinkDrone drone, double altitude, ICommandListener listener) {
        if (drone == null)
            return;

        drone.getGuidedPoint().doGuidedTakeoff(altitude, listener);
    }

    public static void sendMavlinkMessage(MavLinkDrone drone, MavlinkMessageWrapper messageWrapper) {
        if (drone == null || messageWrapper == null)
            return;

        MAVLinkMessage message = messageWrapper.getMavLinkMessage();
        if (message == null)
            return;

        message.compid = drone.getCompid();
        message.sysid = drone.getSysid();

        //Set the target system and target component for MAVLink messages that support those
        //attributes.
        try {
            Class<?> tempMessage = message.getClass();
            Field target_system = tempMessage.getDeclaredField("target_system");
            Field target_component = tempMessage.getDeclaredField("target_component");

            target_system.setByte(message, (byte) message.sysid);
            target_component.setByte(message, (byte) message.compid);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException | ExceptionInInitializerError e) {
            Timber.e(e, e.getMessage());
        }

        drone.getMavClient().sendMessage(message, null);
    }

    public static void sendGuidedPoint(MavLinkDrone drone, LatLong point, boolean force, ICommandListener listener) {
        if (drone == null)
            return;

        GuidedPoint guidedPoint = drone.getGuidedPoint();
        if (guidedPoint.isInitialized()) {
            guidedPoint.newGuidedCoord(point);
        } else if (force) {
            try {
                guidedPoint.forcedGuidedCoordinate(point, listener);
            } catch (Exception e) {
                Timber.e(e, e.getMessage());
            }
        }
    }

    public static void sendLookAtTarget(final MavLinkDrone drone, final LatLongAlt target, final boolean force, final ICommandListener listener){
        if(drone == null)
            return;

        GuidedPoint guidedPoint = drone.getGuidedPoint();
        if(guidedPoint.isInitialized()){
            MavLinkDoCmds.setROI(drone, target, listener);
        }
        else if (force) {
            GuidedPoint.changeToGuidedMode(drone, new AbstractCommandListener() {
                @Override
                public void onSuccess() {
                    MavLinkDoCmds.setROI(drone, target, listener);
                }

                @Override
                public void onError(int executionError) {
                    postErrorEvent(executionError, listener);
                }

                @Override
                public void onTimeout() {
                    postTimeoutEvent(listener);
                }
            });
        }
    }

    public static void setGuidedAltitude(MavLinkDrone drone, double altitude) {
        if (drone == null)
            return;

        drone.getGuidedPoint().changeGuidedAltitude(altitude);
    }

    public static void gotoWaypoint(MavLinkDrone drone, int waypoint, ICommandListener listener) {
        if (drone == null)
            return;
        if (waypoint < 0) {
            postErrorEvent(CommandExecutionError.COMMAND_FAILED, listener);
            return;
        }
        MavLinkDoCmds.gotoWaypoint(drone, waypoint, listener);
    }

    public static void buildComplexMissionItem(MavLinkDrone drone, Bundle itemBundle) {
        MissionItem missionItem = MissionItemType.restoreMissionItemFromBundle(itemBundle);
        if (missionItem == null || !(missionItem instanceof MissionItem.ComplexItem))
            return;

        MissionItemType itemType = missionItem.getType();
        switch (itemType) {
            case SURVEY:
                Survey updatedSurvey = buildSurvey(drone, (Survey) missionItem);
                if (updatedSurvey != null)
                    itemType.storeMissionItem(updatedSurvey, itemBundle);
                break;

            case SPLINE_SURVEY:
                Survey updatedSplineSurvey = buildSplineSurvey(drone, (Survey) missionItem);
                if (updatedSplineSurvey != null)
                    itemType.storeMissionItem(updatedSplineSurvey, itemBundle);
                break;

            case STRUCTURE_SCANNER:
                StructureScanner updatedScanner = buildStructureScanner(drone, (StructureScanner) missionItem);
                if (updatedScanner != null)
                    itemType.storeMissionItem(updatedScanner, itemBundle);
                break;

            default:
                Timber.w("Unrecognized complex mission item.");
                break;
        }
    }

    public static Survey buildSurvey(MavLinkDrone drone, Survey survey) {
        MissionImpl droneMissionImpl = drone == null ? null : drone.getMission();
        SurveyImpl updatedSurveyImpl = (SurveyImpl) ProxyUtils.getMissionItemImpl
                (droneMissionImpl, survey);

        return (Survey) ProxyUtils.getProxyMissionItem(updatedSurveyImpl);
    }

    public static Survey buildSplineSurvey(MavLinkDrone drone, Survey survey) {
        MissionImpl droneMissionImpl = drone == null ? null : drone.getMission();
        SplineSurveyImpl updatedSplineSurvey = (SplineSurveyImpl)
                ProxyUtils.getMissionItemImpl(droneMissionImpl, survey);

        return (Survey) ProxyUtils.getProxyMissionItem(updatedSplineSurvey);
    }

    public static StructureScanner buildStructureScanner(MavLinkDrone drone, StructureScanner item) {
        MissionImpl droneMissionImpl = drone == null ? null : drone.getMission();
        StructureScannerImpl updatedScan = (StructureScannerImpl) ProxyUtils
                .getMissionItemImpl(droneMissionImpl, item);

        StructureScanner proxyScanner = (StructureScanner) ProxyUtils.getProxyMissionItem(updatedScan);
        return proxyScanner;
    }

    public static MagnetometerCalibrationStatus getMagnetometerCalibrationStatus(MavLinkDrone drone) {
        MagnetometerCalibrationStatus calStatus = new MagnetometerCalibrationStatus();
        if (drone != null) {
            MagnetometerCalibrationImpl magCalImpl = drone.getMagnetometerCalibration();
            calStatus.setCalibrationCancelled(magCalImpl.isCancelled());

            Collection<MagnetometerCalibrationImpl.Info> calibrationInfo = magCalImpl.getMagCalibrationTracker().values();
            for (MagnetometerCalibrationImpl.Info info : calibrationInfo) {
                calStatus.addCalibrationProgress(getMagnetometerCalibrationProgress(info.getCalProgress()));
                calStatus.addCalibrationResult(getMagnetometerCalibrationResult(info.getCalReport()));
            }
        }

        return calStatus;
    }

    public static MagnetometerCalibrationProgress getMagnetometerCalibrationProgress(msg_mag_cal_progress msgProgress) {
        if (msgProgress == null)
            return null;

        return new MagnetometerCalibrationProgress(msgProgress.compass_id, msgProgress.completion_pct,
                msgProgress.direction_x, msgProgress.direction_y, msgProgress.direction_z);
    }

    public static MagnetometerCalibrationResult getMagnetometerCalibrationResult(msg_mag_cal_report msgReport) {
        if (msgReport == null)
            return null;

        return new MagnetometerCalibrationResult(msgReport.compass_id,
                msgReport.cal_status == MAG_CAL_STATUS.MAG_CAL_SUCCESS, msgReport.autosaved == 1, msgReport.fitness,
                msgReport.ofs_x, msgReport.ofs_y, msgReport.ofs_z,
                msgReport.diag_x, msgReport.diag_y, msgReport.diag_z,
                msgReport.offdiag_x, msgReport.offdiag_y, msgReport.offdiag_z);
    }

    public static void startVideoStream(Drone drone, Bundle videoProps, String appId, String videoTag,
                                        Surface videoSurface, ICommandListener listener) {
        if (!(drone instanceof GenericMavLinkDrone)) {
            postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
            return;
        }

        GenericMavLinkDrone mavLinkDrone = (GenericMavLinkDrone) drone;
        mavLinkDrone.startVideoStream(videoProps, appId, videoTag, videoSurface, listener);
    }

    public static void stopVideoStream(Drone drone, String appId, String videoTag,
                                       ICommandListener listener) {
        if (!(drone instanceof GenericMavLinkDrone)) {
            postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
            return;
        }

        GenericMavLinkDrone mavLinkDrone = (GenericMavLinkDrone) drone;
        mavLinkDrone.stopVideoStream(appId, videoTag, listener);
    }

    public static void startVideoStreamForObserver(Drone drone, String appId, String videoTag,
                                        ICommandListener listener) {
        if (!(drone instanceof GenericMavLinkDrone)) {
            postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
            return;
        }

        GenericMavLinkDrone mavLinkDrone = (GenericMavLinkDrone) drone;
        mavLinkDrone.startVideoStreamForObserver(appId, videoTag, listener);
    }

    public static void stopVideoStreamForObserver(Drone drone, String appId, String videoTag,
                                       ICommandListener listener) {
        if (!(drone instanceof GenericMavLinkDrone)) {
            postErrorEvent(CommandExecutionError.COMMAND_UNSUPPORTED, listener);
            return;
        }

        GenericMavLinkDrone mavLinkDrone = (GenericMavLinkDrone) drone;
        mavLinkDrone.stopVideoStreamForObserver(appId, videoTag, listener);
    }
}
