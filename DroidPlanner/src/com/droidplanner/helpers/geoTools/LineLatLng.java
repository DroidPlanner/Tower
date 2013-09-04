package com.droidplanner.helpers.geoTools;

import com.google.android.gms.maps.model.LatLng;

public class LineLatLng {
	private LatLng start;
	private LatLng end;

	public LineLatLng(LatLng p1, LatLng p2) {
		this.start = p1;
		this.end = p2;
	}

	public LineLatLng(LineLatLng line) {
		this(line.getStart(), line.getEnd());
	}

	public double getHeading() {
		return GeoTools.getHeadingFromCoordinates(getStart(), getEnd());		
	}
	
	public LatLng getFarthestEndpointTo(LatLng point) {
		if (getClosestEndpointTo(point).equals(getStart())) {
			return getEnd();
		} else {
			return getStart();
		}
	}

	public LatLng getClosestEndpointTo(LatLng point) {
		if (getDistanceToStart(point) < getDistanceToEnd(point)) {
			return getStart();
		} else {
			return getEnd();
		}
	}

	private Double getDistanceToEnd(LatLng point) {
		return GeoTools.getAproximatedDistance(getEnd(), point);
	}

	private Double getDistanceToStart(LatLng point) {
		return GeoTools.getAproximatedDistance(getStart(), point);
	}

	public LatLng getStart() {
		return start;
	}

	public LatLng getEnd() {
		return end;
	}	
}