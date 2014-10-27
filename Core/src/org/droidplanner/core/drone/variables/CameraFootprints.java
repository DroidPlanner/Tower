package org.droidplanner.core.drone.variables;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.survey.CameraInfo;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.survey.Footprint;

import com.MAVLink.Messages.ardupilotmega.msg_camera_feedback;

public class CameraFootprints extends DroneVariable {
	private CameraInfo camera = new CameraInfo();
	private List<Footprint> footprints = new ArrayList<Footprint>();

	public CameraFootprints(Drone myDrone) {
		super(myDrone);
	}

	public void newImageLocation(msg_camera_feedback msg) {
		footprints.add(new Footprint(camera,msg));
		myDrone.notifyDroneEvent(DroneEventsType.FOOTPRINT);
	}

	public Footprint getLastFootprint() {
		return footprints.get(footprints.size()-1);
	}

}
