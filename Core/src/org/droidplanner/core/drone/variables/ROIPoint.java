package org.droidplanner.core.drone.variables;

import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneVariable;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;

public class ROIPoint extends DroneVariable {
	private Coord2D coord = new Coord2D(0, 0);
	private Altitude altitude = new Altitude(0.0);

	public ROIPoint(Drone myDrone) {
		super(myDrone);
	}

	public void newROICoord(Coord2D coord) {
		changeCoord(coord);
	}

	public void setROIAlt(Altitude alt) {
		this.altitude = alt;
	}

	private void changeCoord(Coord2D coord) {
		this.coord = coord;
		sendROIPoint();
	}

	private void sendROIPoint() {
		MavLinkROI.setROI(myDrone, new Coord3D(coord.getLat(), coord.getLng(),
				altitude));
	}

	public Coord2D getCoord() {
		return coord;
	}

	public Altitude getAltitude() {
		return this.altitude;
	}
}
