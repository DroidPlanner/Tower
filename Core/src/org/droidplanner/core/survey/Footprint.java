package org.droidplanner.core.survey;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.survey.CameraInfo;

import com.MAVLink.Messages.ardupilotmega.msg_camera_feedback;

public class Footprint {
	List<Coord2D> vertex = new ArrayList<Coord2D>();

	public Footprint(CameraInfo camera, msg_camera_feedback msg) {
		vertex.add(new Coord2D(msg.lat/1E7,msg.lng/1E7));
	}

	public Coord2D getCenter() {
		return vertex.get(0);
	}	

}
