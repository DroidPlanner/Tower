package org.droidplanner.services.android.impl.core.drone.variables.calibration;

import android.os.Handler;
import android.os.RemoteException;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.common.msg_statustext;

import org.droidplanner.services.android.impl.core.MAVLink.MavLinkCalibration;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.impl.core.drone.DroneVariable;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

public class AccelCalibration extends DroneVariable implements DroneInterfaces.OnDroneListener<MavLinkDrone> {

    private final Runnable onCalibrationStart = new Runnable() {
        @Override
        public void run() {
            final ICommandListener listener = listenerRef.getAndSet(null);
            if (listener != null) {
                try {
                    listener.onSuccess();
                } catch (RemoteException e) {
                    Timber.e(e, e.getMessage());
                }
            }
        }
    };

    private String mavMsg;
    private boolean calibrating;

    private final Handler handler;
    private final AtomicReference<ICommandListener> listenerRef = new AtomicReference<>(null);

    public AccelCalibration(MavLinkDrone drone, Handler handler) {
        super(drone);
        this.handler = handler;
        drone.addDroneListener(this);
    }

    public void startCalibration(ICommandListener listener) {
        if (calibrating) {
            if (listener != null) {
                try {
                    listener.onSuccess();
                } catch (RemoteException e) {
                    Timber.e(e, e.getMessage());
                }
            }
            return;
        }

        if (myDrone.getState().isFlying()) {
            calibrating = false;
        } else {
            calibrating = true;
            mavMsg = "";

            listenerRef.set(listener);
            MavLinkCalibration.startAccelerometerCalibration(myDrone, new SimpleCommandListener() {
                @Override
                public void onSuccess() {
                    final ICommandListener listener = listenerRef.getAndSet(null);
                    if (listener != null) {
                        try {
                            listener.onSuccess();
                        } catch (RemoteException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }
                }

                @Override
                public void onError(int executionError) {
                    final ICommandListener listener = listenerRef.getAndSet(null);
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
                    final ICommandListener listener = listenerRef.getAndSet(null);
                    if (listener != null) {
                        try {
                            listener.onTimeout();
                        } catch (RemoteException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }
                }
            });
        }
    }

    public void sendAck(int step) {
        if (calibrating)
            MavLinkCalibration.sendCalibrationAckMessage(myDrone, step);
    }

    public void processMessage(MAVLinkMessage msg) {
        if (calibrating && msg.msgid == msg_statustext.MAVLINK_MSG_ID_STATUSTEXT) {
            msg_statustext statusMsg = (msg_statustext) msg;
            final String message = statusMsg.getText();

            if (message != null && (message.startsWith("Place vehicle") || message.startsWith("Calibration"))) {
                handler.post(onCalibrationStart);

                mavMsg = message;
                if (message.startsWith("Calibration"))
                    calibrating = false;

                myDrone.notifyDroneEvent(DroneEventsType.CALIBRATION_IMU);
            }
        }
    }

    public String getMessage() {
        return mavMsg;
    }

    public boolean isCalibrating() {
        return calibrating;
    }

    @Override
    public void onDroneEvent(DroneEventsType event, MavLinkDrone drone) {
        switch (event) {
            case HEARTBEAT_TIMEOUT:
            case DISCONNECTED:
                if (calibrating)
                    cancelCalibration();
                break;
        }
    }

    public void cancelCalibration() {
        mavMsg = "";
        calibrating = false;
    }
}
