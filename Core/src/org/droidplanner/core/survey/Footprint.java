package org.droidplanner.core.survey;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.mission.survey.CameraInfo;

import com.MAVLink.Messages.ardupilotmega.msg_camera_feedback;

public class Footprint {
	private List<Coord2D> vertex = new ArrayList<Coord2D>();
	private Coord2D center;

	public Footprint(CameraInfo camera, msg_camera_feedback msg) {
		System.out.println(msg);
		center = new Coord2D(msg.lat/1E7,msg.lng/1E7);
		float yaw = msg.yaw;
		vertex.add(GeoTools.newCoordFromBearingAndDistance(center, yaw-60, 30));
		vertex.add(GeoTools.newCoordFromBearingAndDistance(center, yaw+60, 30));
		vertex.add(GeoTools.newCoordFromBearingAndDistance(center, yaw+120, 30));
		vertex.add(GeoTools.newCoordFromBearingAndDistance(center, yaw-120, 30));
	}

	public Coord2D getCenter() {
		return vertex.get(0);
	}

	public List<Coord2D> getVertex() {
		return vertex;
	}	

}
