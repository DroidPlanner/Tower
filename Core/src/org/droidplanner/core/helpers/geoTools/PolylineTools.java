package org.droidplanner.core.helpers.geoTools;

import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.units.Length;

public class PolylineTools {

	/**
	 * Total length of the polyline in meters
	 * 
	 * @param gridPoints
	 * @return
	 */
	public static Length getPolylineLength(List<Coord2D> gridPoints) {
		double lenght = 0;
		for (int i = 1; i < gridPoints.size(); i++) {
			final Coord2D to = gridPoints.get(i - 1);
			if (to == null) {
				continue;
			}

			lenght += GeoTools.getDistance(gridPoints.get(i), to).valueInMeters();
		}
		return new Length(lenght);
	}

}
