package com.droidplanner.survey.grid;

import java.util.ArrayList;
import java.util.List;

import com.droidplanner.helpers.geoTools.GeoTools;
import com.droidplanner.polygon.PolyBounds;
import com.google.android.gms.maps.model.LatLng;

public class Trimmer {
	List<LineLatLng> trimedGrid = new ArrayList<LineLatLng>();

	public Trimmer(List<LineLatLng> grid, List<LineLatLng> polygon) {
		for (LineLatLng gridLine : grid) {
			ArrayList<LatLng> crosses = findCrossings(polygon, gridLine);
			processCrossings(crosses, gridLine);
		}
	}

	private ArrayList<LatLng> findCrossings(List<LineLatLng> polygon,
			LineLatLng gridLine) {

		ArrayList<LatLng> crossings = new ArrayList<LatLng>();
		for (LineLatLng polyLine : polygon) {
			try {
				crossings
						.add(GeoTools.FindLineIntersection(polyLine, gridLine));
			} catch (Exception e) {
			}
		}

		return crossings;
	}

	private void processCrossings(ArrayList<LatLng> crosses, LineLatLng gridLine) {
		switch (crosses.size()) {
		case 0:
		case 1:
			break;
		case 2:
			trimedGrid.add(new LineLatLng(crosses.get(0), crosses.get(1)));
			break;
		default: // TODO handle multiple crossings in a better way
			trimedGrid.add(new LineLatLng(findExternalPoints(crosses)));
		}
	}

	private LineLatLng findExternalPoints(ArrayList<LatLng> crosses) {
		LatLng meanCoord = new PolyBounds(crosses).getMiddle();		
		LatLng start = findFarthestPoint(crosses, meanCoord);
		LatLng end = findFarthestPoint(crosses, start);		
		return new LineLatLng(start, end);
	}

	private LatLng findFarthestPoint(ArrayList<LatLng> crosses, LatLng middle) {
		double farthestDistance = Double.NEGATIVE_INFINITY;
		LatLng farthestPoint = null;
		for (LatLng cross : crosses) {
			double distance = GeoTools.getAproximatedDistance(cross, middle);
			if (distance > farthestDistance) {
				farthestPoint = cross;
				farthestDistance = distance;
			}
		}
		return farthestPoint;
	}

	public List<LineLatLng> getTrimmedGrid() {
		return trimedGrid;
	}

}
