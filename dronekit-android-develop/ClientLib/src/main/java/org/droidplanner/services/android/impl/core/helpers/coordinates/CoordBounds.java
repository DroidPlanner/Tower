package org.droidplanner.services.android.impl.core.helpers.coordinates;

import org.droidplanner.services.android.impl.core.helpers.geoTools.GeoTools;
import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.List;

/**
 * Calculate a rectangle that bounds all inserted points
 */
public class CoordBounds {
	public LatLong sw_3quadrant;
	public LatLong ne_1quadrant;

	public CoordBounds(LatLong point) {
		include(point);
	}

	public CoordBounds(List<LatLong> points) {
		for (LatLong point : points) {
			include(point);
		}
	}

	public void include(LatLong point) {
		if ((sw_3quadrant == null) || (ne_1quadrant == null)) {
			ne_1quadrant = new LatLong(point);
			sw_3quadrant = new LatLong(point);
		} else {
			if (point.getLongitude() > ne_1quadrant.getLongitude()) {
				ne_1quadrant.setLongitude(point.getLongitude());
			}
			if (point.getLatitude() > ne_1quadrant.getLatitude()) {
				ne_1quadrant.setLatitude(point.getLatitude());
			}
			if (point.getLongitude() < sw_3quadrant.getLongitude()) {
				sw_3quadrant.setLongitude(point.getLongitude());
			}
			if (point.getLatitude() < sw_3quadrant.getLatitude()) {
				sw_3quadrant.setLatitude(point.getLatitude());
			}
		}
	}

	public double getDiag() {
		return GeoTools.latToMeters(GeoTools.getAproximatedDistance(ne_1quadrant, sw_3quadrant));
	}

	public LatLong getMiddle() {
		return (new LatLong((ne_1quadrant.getLatitude() + sw_3quadrant.getLatitude()) / 2,
				(ne_1quadrant.getLongitude() + sw_3quadrant.getLongitude()) / 2));

	}
}
