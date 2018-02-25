package org.droidplanner.services.android.impl.core.drone.variables;

import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;

import com.MAVLink.ardupilotmega.msg_ekf_status_report;
import com.MAVLink.enums.EKF_STATUS_FLAGS;

import org.droidplanner.services.android.impl.core.MAVLink.MavLinkCommands;
import org.droidplanner.services.android.impl.core.MAVLink.WaypointManager;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.impl.core.drone.DroneVariable;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.impl.core.drone.autopilot.generic.GenericMavLinkDrone;
import org.droidplanner.services.android.impl.core.model.AutopilotWarningParser;
import com.o3dr.services.android.lib.drone.attribute.error.CommandExecutionError;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import timber.log.Timber;

public class State extends DroneVariable<GenericMavLinkDrone> {
    private static final long ERROR_TIMEOUT = 5000l;

    private final static Action requestHomeUpdateAction = new Action(MavLinkDrone.ACTION_REQUEST_HOME_UPDATE);

    private final AutopilotWarningParser warningParser;

    private msg_ekf_status_report ekfStatus;
    private boolean isEkfPositionOk;

    private String errorId;
    private boolean armed = false;
    private boolean isFlying = false;
    private ApmModes mode = ApmModes.UNKNOWN;

    // flightTimer
    // ----------------
    private long startTime = 0;

    private final Handler handler;
    private final Runnable watchdogCallback = new Runnable() {
        @Override
        public void run() {
            resetWarning();
        }
    };

    public State(GenericMavLinkDrone myDrone, Handler handler, AutopilotWarningParser warningParser) {
        super(myDrone);
        this.handler = handler;
        this.warningParser = warningParser;
        this.errorId = warningParser.getDefaultWarning();
        resetFlightStartTime();
    }

    public boolean isArmed() {
        return armed;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public ApmModes getMode() {
        return mode;
    }

    public String getErrorId() {
        return errorId;
    }

    public void setIsFlying(boolean newState) {
        if (newState != isFlying) {
            isFlying = newState;
            myDrone.notifyDroneEvent(DroneEventsType.STATE);

            if (isFlying) {
                resetFlightStartTime();
            }
        }
    }

    public boolean parseAutopilotError(String errorMsg) {
        String parsedError = warningParser.parseWarning(myDrone, errorMsg);
        if (parsedError == null || parsedError.trim().isEmpty())
            return false;

        if (!parsedError.equals(this.errorId)) {
            this.errorId = parsedError;
            myDrone.notifyDroneEvent(DroneEventsType.AUTOPILOT_WARNING);
        }

        handler.removeCallbacks(watchdogCallback);
        this.handler.postDelayed(watchdogCallback, ERROR_TIMEOUT);
        return true;
    }

    public void repeatWarning() {
        if (errorId == null || errorId.length() == 0 || errorId.equals(warningParser.getDefaultWarning()))
            return;

        handler.removeCallbacks(watchdogCallback);
        this.handler.postDelayed(watchdogCallback, ERROR_TIMEOUT);
    }

    public void setArmed(boolean newState) {
        if (this.armed != newState) {
            this.armed = newState;
            myDrone.notifyDroneEvent(DroneEventsType.ARMING);

            if (newState) {
                WaypointManager waypointManager = myDrone.getWaypointManager();
                if(waypointManager != null) {
                    waypointManager.getWaypoints();
                }
            }
        }

        checkEkfPositionState(this.ekfStatus);
    }

    public void setMode(ApmModes mode) {
        if (this.mode != mode) {
            this.mode = mode;
            myDrone.notifyDroneEvent(DroneEventsType.MODE);
        }
    }

    public void changeFlightMode(ApmModes mode, final ICommandListener listener) {
        if (this.mode == mode) {
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            listener.onSuccess();
                        } catch (RemoteException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }
                });
            }
            return;
        }

        if (ApmModes.isValid(mode)) {
            MavLinkCommands.changeFlightMode(myDrone, mode, listener);
        } else {
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            listener.onError(CommandExecutionError.COMMAND_FAILED);
                        } catch (RemoteException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }
                });
            }
        }
    }

    private void resetWarning() {
        String defaultWarning = warningParser.getDefaultWarning();
        if (defaultWarning == null)
            defaultWarning = "";

        if (!defaultWarning.equals(this.errorId)) {
            this.errorId = defaultWarning;
            myDrone.notifyDroneEvent(DroneEventsType.AUTOPILOT_WARNING);
        }
    }

    // flightTimer
    // ----------------

    private void resetFlightStartTime() {
        startTime = SystemClock.elapsedRealtime();
    }

    public long getFlightStartTime() {
        return startTime;
    }

    public msg_ekf_status_report getEkfStatus() {
        return ekfStatus;
    }

    public void setEkfStatus(msg_ekf_status_report ekfState) {
        if (this.ekfStatus == null || !areEkfStatusEquals(this.ekfStatus, ekfState)) {
            this.ekfStatus = ekfState;
            myDrone.notifyDroneEvent(DroneEventsType.EKF_STATUS_UPDATE);
        }
    }

    private void checkEkfPositionState(msg_ekf_status_report ekfStatus) {
        if (ekfStatus == null)
            return;

        int flags = ekfStatus.flags;

        boolean isOk = this.armed
                ? (flags & EKF_STATUS_FLAGS.EKF_POS_HORIZ_ABS) != 0
                && (flags & EKF_STATUS_FLAGS.EKF_CONST_POS_MODE) == 0
                : (flags & EKF_STATUS_FLAGS.EKF_POS_HORIZ_ABS) != 0
                || (flags & EKF_STATUS_FLAGS.EKF_PRED_POS_HORIZ_ABS) != 0;

        if (isEkfPositionOk != isOk) {
            isEkfPositionOk = isOk;
            myDrone.notifyDroneEvent(DroneEventsType.EKF_POSITION_STATE_UPDATE);

            if(isEkfPositionOk){
                myDrone.executeAsyncAction(requestHomeUpdateAction, null);
            }
        }
    }

    private static boolean areEkfStatusEquals(msg_ekf_status_report one, msg_ekf_status_report two) {
        return one == two || !(one == null || two == null) && one.toString().equals(two.toString());
    }

    public boolean isEkfPositionOk() {
        return isEkfPositionOk;
    }
}