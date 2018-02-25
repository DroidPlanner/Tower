package org.droidplanner.services.android.impl.core.helpers.geoTools;

import com.o3dr.services.android.lib.coordinate.LatLong;

public class LineLatLong {
	private final LatLong start;
	private final LatLong end;

	public LineLatLong(LatLong start, LatLong end) {
		this.start = start;
		this.end = end;
	}

	public LineLatLong(LineLatLong line) {
		this(line.start, line.end);
	}

	public LatLong getStart() {
		return start;
	}

	public LatLong getEnd() {
		return end;
	}

	public double getHeading() {
		return GeoTools.getHeadingFromCoordinates(this.start, this.end);
	}

	public LatLong getFarthestEndpointTo(LatLong point) {
		if (getClosestEndpointTo(point).equals(start)) {
			return end;
		} else {
			return start;
		}
	}

	public LatLong getClosestEndpointTo(LatLong point) {
		if (getDistanceToStart(point) < getDistanceToEnd(point)) {
			return start;
		} else {
			return end;
		}
	}

	private Double getDistanceToEnd(LatLong point) {
		return GeoTools.getAproximatedDistance(end, point);
	}

	private Double getDistanceToStart(LatLong point) {
		return GeoTools.getAproximatedDistance(start, point);
	}

	@Override
	public String toString() {
		return "from:" + start.toString() + "to:" + end.toString();
	}

}