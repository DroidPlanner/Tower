package org.droidplanner.mission.survey.grid;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.helpers.geoTools.LineLatLng;
import org.droidplanner.helpers.geoTools.LineTools;

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
				crossings.add(LineTools
						.FindLineIntersection(polyLine, gridLine));
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
			trimedGrid.add(LineTools.findExternalPoints(crosses));
		}
	}

	public List<LineLatLng> getTrimmedGrid() {
		return trimedGrid;
	}

}
