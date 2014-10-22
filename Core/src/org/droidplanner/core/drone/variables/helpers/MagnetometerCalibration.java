package org.droidplanner.core.drone.variables.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.droidplanner.core.MAVLink.MavLinkStreamRates;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.variables.Magnetometer;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;

import ellipsoidFit.FitPoints;
import ellipsoidFit.ThreeSpacePoint;

public class MagnetometerCalibration implements OnDroneListener {

    private static final double ELLIPSOID_FITNESS_MIN = 0.97;
    public static final int MIN_POINTS_COUNT = 250;
    private static final int REFRESH_RATE = 50; //hz

    public interface OnMagCalibrationListener {
        public void newEstimation(FitPoints fit, List<ThreeSpacePoint> points);
        public void finished(FitPoints fit);
    }

    private final DroneInterfaces.Handler handler;
    private ExecutorService fitRunner;

	private final FitPoints ellipsoidFit = new FitPoints();
	private final ArrayList<ThreeSpacePoint> points = new ArrayList<ThreeSpacePoint>();

    private final Runnable newEstimationUpdate = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(this);
            if(listener != null){
                listener.newEstimation(ellipsoidFit, points);
            }
        }
    };

    private final Runnable finishedEstimationUpdate = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(this);
            if(listener != null){
                listener.finished(ellipsoidFit);
            }
        }
    };

    private final Runnable stopCalibration = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(this);
            stop();
        }
    };

	private boolean fitComplete =false;
	private OnMagCalibrationListener listener;
	private Drone drone;

	public MagnetometerCalibration(Drone drone, OnMagCalibrationListener listener, DroneInterfaces.Handler handler) {
		this.drone = drone;
		this.listener = listener;
        this.handler = handler;
	}

    public void start(List<? extends ThreeSpacePoint> newPoints){
        this.points.clear();
        if(newPoints != null && !newPoints.isEmpty()){
            this.points.addAll(newPoints);
        }

        if(this.fitRunner != null && !this.fitRunner.isShutdown()){
            this.fitRunner.shutdownNow();
        }

        this.fitRunner = Executors.newSingleThreadExecutor();
        MavLinkStreamRates.setupStreamRates(drone.getMavClient(), 0, 0, 0, 0, 0, 0, REFRESH_RATE,
                0);
        drone.addDroneListener(this);
    }

    public void stop(){
        drone.removeDroneListener(this);
        drone.getStreamRates().setupStreamRatesFromPref();

        if(this.fitRunner != null) {
            this.fitRunner.shutdownNow();
        }
    }

	@Override
	public void onDroneEvent(DroneEventsType event, final Drone drone) {
		switch (event) {

		case MAGNETOMETER:
			addpoint(drone);
            this.fitRunner.execute(new Runnable() {
                @Override
                public void run() {
                    fit();
                    MavLinkStreamRates.setupStreamRates(drone.getMavClient(), 0, 0, 0, 0, 0, 0,
                            REFRESH_RATE, 0);
                }
            });
			break;

		default:
			break;
		}
	}

    public List<ThreeSpacePoint> getPoints(){
        return points;
    }

	void addpoint(Drone drone) {
		final Magnetometer mag = drone.getMagnetometer();
		int[] offsets = mag.getOffsets();
		ThreeSpacePoint point = new ThreeSpacePoint(mag.getX()-offsets[0], mag.getY()-offsets[1], mag.getZ()-offsets[2]);
		points.add(point);
	}

	void fit() {
        if(points.isEmpty()){
            return;
        }

		ellipsoidFit.fitEllipsoid(points);
        if(listener != null) {
            handler.post(newEstimationUpdate);
        }

		if (!fitComplete && ellipsoidFit.getFitness() > ELLIPSOID_FITNESS_MIN && points.size() >
                MIN_POINTS_COUNT) {
			fitComplete  = true;
			if (listener !=null) {
                handler.post(finishedEstimationUpdate);
			}

            this.handler.post(stopCalibration);
		}
	}

	public void sendOffsets() throws Exception {
		Parameter offsetX = drone.getParameters().getParameter("COMPASS_OFS_X");
		Parameter offsetY = drone.getParameters().getParameter("COMPASS_OFS_Y");
		Parameter offsetZ = drone.getParameters().getParameter("COMPASS_OFS_Z");
		
		if (offsetX == null || offsetY == null || offsetZ == null) {
			throw new Exception("Parameters have not been loaded");
		}
		
		offsetX.value = -ellipsoidFit.center.getEntry(0);
		offsetY.value = -ellipsoidFit.center.getEntry(1);
		offsetZ.value = -ellipsoidFit.center.getEntry(2);
				
		drone.getParameters().sendParameter(offsetX); //TODO should probably do a check after sending the parameters
		drone.getParameters().sendParameter(offsetY);
		drone.getParameters().sendParameter(offsetZ);
	}
}