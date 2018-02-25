package org.droidplanner.services.android.impl.core.drone.variables.calibration;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.ardupilotmega.msg_mag_cal_progress;
import com.MAVLink.ardupilotmega.msg_mag_cal_report;

import org.droidplanner.services.android.impl.core.MAVLink.MavLinkCalibration;
import org.droidplanner.services.android.impl.core.drone.DroneInterfaces;
import org.droidplanner.services.android.impl.core.drone.DroneVariable;
import org.droidplanner.services.android.impl.core.drone.autopilot.MavLinkDrone;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Fredia Huya-Kouadio on 5/3/15.
 */
public class MagnetometerCalibrationImpl extends DroneVariable implements DroneInterfaces.OnDroneListener<MavLinkDrone> {

    public interface OnMagnetometerCalibrationListener {
        void onCalibrationCancelled();

        void onCalibrationProgress(msg_mag_cal_progress progress);

        void onCalibrationCompleted(msg_mag_cal_report result);
    }

    private final HashMap<Short, Info> magCalibrationTracker = new HashMap<>();

    private OnMagnetometerCalibrationListener listener;

    private AtomicBoolean cancelled = new AtomicBoolean(false);

    public MagnetometerCalibrationImpl(MavLinkDrone myDrone) {
        super(myDrone);
        myDrone.addDroneListener(this);
    }

    public void setListener(OnMagnetometerCalibrationListener listener) {
        this.listener = listener;
    }

    public void startCalibration(boolean retryOnFailure, boolean saveAutomatically, int startDelay) {
        magCalibrationTracker.clear();
        cancelled.set(false);
        MavLinkCalibration.startMagnetometerCalibration(myDrone, retryOnFailure, saveAutomatically, startDelay, null);
    }

    public void cancelCalibration() {
        MavLinkCalibration.cancelMagnetometerCalibration(myDrone, null);

        cancelled.set(true);

        if (listener != null)
            listener.onCalibrationCancelled();
    }

    public void acceptCalibration() {
        MavLinkCalibration.acceptMagnetometerCalibration(myDrone, null);
    }

    public void processCalibrationMessage(MAVLinkMessage message) {
        switch (message.msgid) {
            case msg_mag_cal_progress.MAVLINK_MSG_ID_MAG_CAL_PROGRESS: {
                msg_mag_cal_progress progress = (msg_mag_cal_progress) message;
                Info info = magCalibrationTracker.get(progress.compass_id);
                if (info == null) {
                    info = new Info();
                    magCalibrationTracker.put(progress.compass_id, info);
                }

                info.calProgress = progress;

                if (listener != null)
                    listener.onCalibrationProgress(progress);
                break;
            }

            case msg_mag_cal_report.MAVLINK_MSG_ID_MAG_CAL_REPORT: {
                msg_mag_cal_report report = (msg_mag_cal_report) message;
                Info info = magCalibrationTracker.get(report.compass_id);
                if (info == null) {
                    info = new Info();
                    magCalibrationTracker.put(report.compass_id, info);
                }

                info.calReport = report;

                if (listener != null)
                    listener.onCalibrationCompleted((msg_mag_cal_report) message);
                break;
            }
        }
    }

    public HashMap<Short, Info> getMagCalibrationTracker() {
        return magCalibrationTracker;
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public static class Info {
        msg_mag_cal_progress calProgress;
        msg_mag_cal_report calReport;

        public msg_mag_cal_progress getCalProgress() {
            return calProgress;
        }

        public msg_mag_cal_report getCalReport() {
            return calReport;
        }
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, MavLinkDrone drone) {
        switch(event){
            case HEARTBEAT_TIMEOUT:
            case DISCONNECTED:
                cancelCalibration();
                break;
        }
    }
}
