package com.droidplanner.survey.grid;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.helpers.geoTools.GeoTools;
import com.google.android.gms.maps.model.LatLng;

public class Trimmer {
	List<LineLatLng> trimedGrid = new ArrayList<LineLatLng>();

	public Trimmer(List<LineLatLng> grid, List<LineLatLng> polygon) {

		// find intersections
		for (LineLatLng gridLine : grid) {
			double closestDistance = Double.MAX_VALUE;
			double farestDistance = Double.MIN_VALUE;

			LatLng closestPoint = null;
			LatLng farestPoint = null;

			int crosses = 0;

			for (LineLatLng polyLine : polygon) {
				LatLng newlatlong = GeoTools.FindLineIntersection(polyLine,gridLine);

				if (newlatlong != null) {
					crosses++;
					if (closestDistance > GeoTools.getAproximatedDistance(
							gridLine.p1, newlatlong)) {
						closestPoint = new LatLng(newlatlong.latitude,
								newlatlong.longitude);
						closestDistance = GeoTools.getAproximatedDistance(
								gridLine.p1, newlatlong);
					}
					if (farestDistance < GeoTools.getAproximatedDistance(
							gridLine.p1, newlatlong)) {
						farestPoint = new LatLng(newlatlong.latitude,
								newlatlong.longitude);
						farestDistance = GeoTools.getAproximatedDistance(
								gridLine.p1, newlatlong);
					}
				}
			}

			switch (crosses) {
			case 0:
			case 1:
				break;
			default: // TODO handle multiple crossings in a better way
			case 2:
				trimedGrid.add(new LineLatLng(closestPoint, farestPoint));
				break;
			}
		}
	}

	public List<LineLatLng> getTrimmedGrid() {
		return trimedGrid;
	}

}
