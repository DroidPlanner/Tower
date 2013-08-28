package com.droidplanner.survey.grid;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.helpers.geoTools.GeoTools;
import com.google.android.gms.maps.model.LatLng;

public class GridTrim {

	/**
	 * Trims a grid of lines for points outside a polygon
	 * 
	 * @param waypoints2
	 *            Polygon vertices
	 * @param grid
	 *            Array with Grid lines
	 * @return array with the trimmed grid lines
	 */
	static List<LineLatLng> trimGridLines(List<LatLng> waypoints2,
			List<LineLatLng> grid) {
		List<LineLatLng> hatchLines = new ArrayList<LineLatLng>();
		// find intersections
		for (LineLatLng gridLine : grid) {
			double closestDistance = Double.MAX_VALUE;
			double farestDistance = Double.MIN_VALUE;
	
			LatLng closestPoint = null;
			LatLng farestPoint = null;
	
			int crosses = 0;
	
			for (int b = 0; b < waypoints2.size(); b++) {
				LatLng newlatlong;
				if (b != waypoints2.size() - 1) {
					newlatlong = GeoTools.FindLineIntersection(
							waypoints2.get(b), waypoints2.get(b + 1),
							gridLine.p1, gridLine.p2);
				} else { // Don't forget the last polygon line
					newlatlong = GeoTools.FindLineIntersection(
							waypoints2.get(b), waypoints2.get(0), gridLine.p1,
							gridLine.p2);
				}
	
				if (newlatlong != null) {
					crosses++;
					if (closestDistance > GeoTools.getAproximatedDistance(gridLine.p1,
							newlatlong)) {
						closestPoint = new LatLng(newlatlong.latitude,
								newlatlong.longitude);
						closestDistance = GeoTools.getAproximatedDistance(gridLine.p1,
								newlatlong);
					}
					if (farestDistance < GeoTools.getAproximatedDistance(gridLine.p1,
							newlatlong)) {
						farestPoint = new LatLng(newlatlong.latitude,
								newlatlong.longitude);
						farestDistance = GeoTools.getAproximatedDistance(gridLine.p1,
								newlatlong);
					}
				}
			}
	
			switch (crosses) {
			case 0:
			case 1:
				break;
			default: // TODO handle multiple crossings in a better way
			case 2:
				hatchLines.add(new LineLatLng(closestPoint, farestPoint));
				break;
			}
		}
		return hatchLines;
	}

}
