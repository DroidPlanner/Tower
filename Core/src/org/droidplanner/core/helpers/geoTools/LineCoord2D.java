package org.droidplanner.core.helpers.geoTools;

import org.droidplanner.core.helpers.coordinates.Coord2D;

public class LineCoord2D {
	private final Coord2D start;
	private final Coord2D end;

	public LineCoord2D(Coord2D start, Coord2D end) {
		this.start = start;
		this.end = end;
	}

	public LineCoord2D(LineCoord2D line) {
		this(line.start, line.end);
	}

	public Coord2D getStart() {
		return start;
	}

	public Coord2D getEnd() {
		return end;
	}

	public double getHeading() {
		return GeoTools.getHeadingFromCoordinates(this.start, this.end);
	}

	public Coord2D getFarthestEndpointTo(Coord2D point) {
		if (getClosestEndpointTo(point).equals(start)) {
			return end;
		} else {
			return start;
		}
	}

	public Coord2D getClosestEndpointTo(Coord2D point) {
		if (getDistanceToStart(point) < getDistanceToEnd(point)) {
			return start;
		} else {
			return end;
		}
	}

	private Double getDistanceToEnd(Coord2D point) {
		return GeoTools.getAproximatedDistance(end, point);
	}

	private Double getDistanceToStart(Coord2D point) {
		return GeoTools.getAproximatedDistance(start, point);
	}

	@Override
	public String toString() {
		return "from:" + start.toString() + "to:" + end.toString();
	}

}