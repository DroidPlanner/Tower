package org.droidplanner.services.android.impl.core.drone.variables;

import android.os.Handler;
import android.os.RemoteException;

import org.droidplanner.services.android.impl.core.MAVLink.MavLinkCommands;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.services.android.impl.core.drone.DroneVariable;
import org.droidplanner.services.android.impl.core.drone.autopilot.Drone;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import timber.log.Timber;

public class GuidedPoint extends DroneVariable implements OnDroneListener<MavLinkDrone> {

    private GuidedStates state = GuidedStates.UNINITIALIZED;
    private LatLong coord = new LatLong(0, 0);
    private double altitude = 0.0; //altitude in meters

    private Runnable mPostInitializationTask;

    private final Handler handler;

    public enum GuidedStates {
        UNINITIALIZED, IDLE, ACTIVE
    }

    public GuidedPoint(MavLinkDrone myDrone, Handler handler) {
        super(myDrone);
        this.handler = handler;
        myDrone.addDroneListener(this);
    }

    @Override
    public void onDroneEvent(DroneEventsType event, MavLinkDrone drone) {
        switch (event) {
            case HEARTBEAT_FIRST:
            case HEARTBEAT_RESTORED:
            case MODE:
                if (isGuidedMode(myDrone)) {
                    initialize();
                } else {
                    disable();
                }
                break;

            case DISCONNECTED:
            case HEARTBEAT_TIMEOUT:
                disable();

            default:
                break;
        }
    }

    public static boolean isGuidedMode(MavLinkDrone drone) {
        if (drone == null)
            return false;

        final int droneType = drone.getType();
        final ApmModes droneMode = drone.getState().getMode();

        if (Type.isCopter(droneType)) {
            return droneMode == ApmModes.ROTOR_GUIDED;
        }

        if (Type.isPlane(droneType)) {
            return droneMode == ApmModes.FIXED_WING_GUIDED;
        }

        if (Type.isRover(droneType)) {
            return droneMode == ApmModes.ROVER_GUIDED || droneMode == ApmModes.ROVER_HOLD;
        }

        return false;
    }

    public void pauseAtCurrentLocation(ICommandListener listener) {
        if (state == GuidedStates.UNINITIALIZED) {
            changeToGuidedMode(myDrone, listener);
        } else {
            newGuidedCoord(getGpsPosition());
            state = GuidedStates.IDLE;
        }
    }

    private LatLong getGpsPosition() {
        return getGpsPosition(myDrone);
    }

    private static LatLong getGpsPosition(Drone drone) {
        final Gps droneGps = (Gps) drone.getAttribute(AttributeType.GPS);
        return droneGps == null ? null : droneGps.getPosition();
    }

    public static void changeToGuidedMode(MavLinkDrone drone, ICommandListener listener) {
        final State droneState = drone.getState();
        final int droneType = drone.getType();

        if (Type.isCopter(droneType)) {
            droneState.changeFlightMode(ApmModes.ROTOR_GUIDED, listener);
        } else if (Type.isPlane(droneType)) {
            //You have to send a guided point to the plane in order to trigger guided mode.
            forceSendGuidedPoint(drone, getGpsPosition(drone), getDroneAltConstrained(drone));
        } else if (Type.isRover(droneType)) {
            droneState.changeFlightMode(ApmModes.ROVER_GUIDED, listener);
        }
    }

    public void doGuidedTakeoff(final double alt, final ICommandListener listener) {
        if (Type.isCopter(myDrone.getType())) {
            coord = getGpsPosition();
            altitude = alt;
            state = GuidedStates.IDLE;

            changeToGuidedMode(myDrone, new SimpleCommandListener() {
                @Override
                public void onSuccess() {
                    MavLinkCommands.sendTakeoff(myDrone, alt, listener);
                    myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
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
        } else {
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            listener.onError(CommandExecutionError.COMMAND_UNSUPPORTED);
                        } catch (RemoteException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }
                });
            }
        }
    }

    public void newGuidedCoord(LatLong coord) {
        changeCoord(coord);
    }

    public void newGuidedPosition(double latitude, double longitude, double altitude) {
        MavLinkCommands.sendGuidedPosition(myDrone, latitude, longitude, altitude);
    }

    public void newGuidedVelocity(double xVel, double yVel, double zVel) {
        MavLinkCommands.sendGuidedVelocity(myDrone, xVel, yVel, zVel);
    }

    public void newGuidedCoordAndVelocity(LatLong coord, double xVel, double yVel, double zVel) {
        changeCoordAndVelocity(coord, xVel, yVel, zVel);
    }

    public void changeGuidedAltitude(double alt) {
        changeAlt(alt);
    }

    public void forcedGuidedCoordinate(final LatLong coord, final ICommandListener listener) {
        final Gps droneGps = (Gps) myDrone.getAttribute(AttributeType.GPS);
        if (!droneGps.has3DLock()) {
            postErrorEvent(handler, listener, CommandExecutionError.COMMAND_FAILED);
            return;
        }

        if (isInitialized()) {
            changeCoord(coord);
            postSuccessEvent(handler, listener);
        } else {
            mPostInitializationTask = new Runnable() {
                @Override
                public void run() {
                    changeCoord(coord);
                }
            };

            changeToGuidedMode(myDrone, listener);
        }
    }

    public void forcedGuidedCoordinate(final LatLong coord, final double alt, final ICommandListener listener) {
        final Gps droneGps = (Gps) myDrone.getAttribute(AttributeType.GPS);
        if (!droneGps.has3DLock()) {
            postErrorEvent(handler, listener, CommandExecutionError.COMMAND_FAILED);
            return;
        }

        if (isInitialized()) {
            changeCoord(coord);
            changeAlt(alt);
            postSuccessEvent(handler, listener);
        } else {
            mPostInitializationTask = new Runnable() {
                @Override
                public void run() {
                    changeCoord(coord);
                    changeAlt(alt);
                }
            };

            changeToGuidedMode(myDrone, listener);
        }
    }

    private void initialize() {
        if (state == GuidedStates.UNINITIALIZED) {
            coord = getGpsPosition();
            altitude = getDroneAltConstrained(myDrone);
            state = GuidedStates.IDLE;
            myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
        }

        if (mPostInitializationTask != null) {
            mPostInitializationTask.run();
            mPostInitializationTask = null;
        }
    }

    private void disable() {
        if (state == GuidedStates.UNINITIALIZED)
            return;

        state = GuidedStates.UNINITIALIZED;
        myDrone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
    }

    private void changeAlt(double alt) {
        switch (state) {
            case UNINITIALIZED:
                break;

            case IDLE:
                state = GuidedStates.ACTIVE;
                /** FALL THROUGH **/

            case ACTIVE:
                altitude = alt;
                sendGuidedPoint();
                break;
        }
    }

    private void changeCoord(LatLong coord) {
        switch (state) {
            case UNINITIALIZED:
                break;

            case IDLE:
                state = GuidedStates.ACTIVE;
                /** FALL THROUGH **/
            case ACTIVE:
                this.coord = coord;
                sendGuidedPoint();
                break;
        }
    }

    private void changeCoordAndVelocity(LatLong coord, double xVel, double yVel, double zVel) {
        switch (state) {
            case UNINITIALIZED:
                break;

            case IDLE:
                state = GuidedStates.ACTIVE;
                /** FALL THROUGH **/
            case ACTIVE:
                this.coord = coord;
                sendGuidedPointAndVelocity(xVel, yVel, zVel);
                break;
        }
    }

    private void sendGuidedPointAndVelocity(double xVel, double yVel, double zVel) {
        if (state == GuidedStates.ACTIVE) {
            forceSendGuidedPointAndVelocity(myDrone, coord, altitude, xVel, yVel, zVel);
        }
    }

    private void sendGuidedPoint() {
        if (state == GuidedStates.ACTIVE) {
            forceSendGuidedPoint(myDrone, coord, altitude);
        }
    }

    public static void forceSendGuidedPoint(MavLinkDrone drone, LatLong coord, double altitudeInMeters) {
        drone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
        if (coord != null) {
            MavLinkCommands.setGuidedMode(drone, coord.getLatitude(), coord.getLongitude(), altitudeInMeters);
        }
    }

    public static void forceSendGuidedPointAndVelocity(MavLinkDrone drone, LatLong coord, double altitudeInMeters,
                                                       double xVel, double yVel, double zVel) {
        drone.notifyDroneEvent(DroneEventsType.GUIDEDPOINT);
        if (coord != null) {
            MavLinkCommands.sendGuidedPositionAndVelocity(drone, coord.getLatitude(), coord.getLongitude(), altitudeInMeters, xVel,
                    yVel, zVel);
        }
    }

    private static double getDroneAltConstrained(MavLinkDrone drone) {
        final Altitude droneAltitude = (Altitude) drone.getAttribute(AttributeType.ALTITUDE);
        double alt = Math.floor(droneAltitude.getAltitude());
        return Math.max(alt, getDefaultMinAltitude(drone));
    }

    public LatLong getCoord() {
        return coord;
    }

    public double getAltitude() {
        return this.altitude;
    }

    public boolean isActive() {
        return (state == GuidedStates.ACTIVE);
    }

    public boolean isIdle() {
        return (state == GuidedStates.IDLE);
    }

    public boolean isInitialized() {
        return !(state == GuidedStates.UNINITIALIZED);
    }

    public GuidedStates getState() {
        return state;
    }

    public static float getDefaultMinAltitude(MavLinkDrone drone) {
        final int droneType = drone.getType();
        if (Type.isCopter(droneType)) {
            return 2f;
        } else if (Type.isPlane(droneType)) {
            return 15f;
        } else {
            return 0f;
        }
    }

}
