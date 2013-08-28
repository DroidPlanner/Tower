package com.droidplanner.survey.grid;

import com.droidplanner.helpers.geoTools.GeoTools;
import com.google.android.gms.maps.model.LatLng;

public class LineLatLng {
	public LatLng p1;
	public LatLng p2;

	public LineLatLng(LatLng p1, LatLng p2) {
		this.p1 = p1;
		this.p2 = p2;
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