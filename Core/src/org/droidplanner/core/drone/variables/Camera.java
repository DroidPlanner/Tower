package org.droidplanner.core.drone.variables;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.survey.CameraInfo;
import org.droidplanner.core.survey.Footprint;

import com.MAVLink.Messages.ardupilotmega.msg_camera_feedback;

public class Camera extends DroneVariable {
	private CameraInfo camera = new CameraInfo();
	private List<Footprint> footprints = new ArrayList<Footprint>();

	public Camera(Drone myDrone) {
		super(myDrone);
	}

	public void newImageLocation(msg_camera_feedback msg) {
		footprints.add(new Footprint(camera,msg));
		myDrone.notifyDroneEvent(DroneEventsType.FOOTPRINT);
	}

	public Footprint getLastFootprint() {
		return footprints.get(footprints.size()-1);
	}
	
	public CameraInfo getCamera(){
		return camera;
	}

	public Footprint getCurrentFieldOfView() {
		return new Footprint(camera, myDrone .getGps()
				.getPosition(), myDrone.getAltitude().getAltitude(), myDrone.getOrientation()
				.getPitch(), myDrone.getOrientation().getRoll(), myDrone.getOrientation()
				.getYaw());
	}

}
