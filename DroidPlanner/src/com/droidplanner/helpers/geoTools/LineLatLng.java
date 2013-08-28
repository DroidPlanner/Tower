package com.droidplanner.helpers.geoTools;

import com.google.android.gms.maps.model.LatLng;

public class LineLatLng {
	public LatLng p1;
	public LatLng p2;

	public LineLatLng(LatLng p1, LatLng p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	public LineLatLng(LineLatLng line) {
		this(line.p1, line.p2);
	}

	public LatLng getFarthestEndpointTo(LatLng point) {
		if (getClosestEndpointTo(point).equals(p1)) {
			return p2;
		} else {
			return p1;
		}
	}

	public LatLng getClosestEndpointTo(LatLng point) {
		if (getDistanceToStart(point) < getDistanceToEnd(point)) {
			return p1;
		} else {
			return p2;
		}
	}

	private Double getDistanceToEnd(LatLng point) {
		return GeoTools.getAproximatedDistance(p2, point);
	}

	private Double getDistanceToStart(LatLng point) {
		return GeoTools.getAproximatedDistance(p1, point);
	}

}