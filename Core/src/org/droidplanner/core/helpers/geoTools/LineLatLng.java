package org.droidplanner.core.helpers.geoTools;

import org.droidplanner.core.helpers.coordinates.Coord2D;

public class LineLatLng {
	public Coord2D p1;
	public Coord2D p2;

	public LineLatLng(Coord2D coord2d, Coord2D coord2d2) {
		this.p1 = coord2d;
		this.p2 = coord2d2;
	}

	public LineLatLng(LineLatLng line) {
		this(line.p1, line.p2);
	}

	public Coord2D getFarthestEndpointTo(Coord2D point) {
		if (getClosestEndpointTo(point).equals(p1)) {
			return p2;
		} else {
			return p1;
		}
	}

	public Coord2D getClosestEndpointTo(Coord2D point) {
		if (getDistanceToStart(point) < getDistanceToEnd(point)) {
			return p1;
		} else {
			return p2;
		}
	}

	private Double getDistanceToEnd(Coord2D point) {
		return GeoTools.getAproximatedDistance(p2, point);
	}

	private Double getDistanceToStart(Coord2D point) {
		return GeoTools.getAproximatedDistance(p1, point);
	}

	@Override
	public String toString() {
		return "from:"+p1.toString()+ "to:"+p2.toString();
	}

}