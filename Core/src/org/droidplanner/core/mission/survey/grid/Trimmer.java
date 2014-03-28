package org.droidplanner.core.mission.survey.grid;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.LineLatLng;
import org.droidplanner.core.helpers.geoTools.LineTools;

public class Trimmer {
	List<LineLatLng> trimedGrid = new ArrayList<LineLatLng>();

	public Trimmer(List<LineLatLng> grid, List<LineLatLng> polygon) {
		for (LineLatLng gridLine : grid) {
			ArrayList<Coord2D> crosses = findCrossings(polygon, gridLine);
			processCrossings(crosses, gridLine);
		}
	}

	private ArrayList<Coord2D> findCrossings(List<LineLatLng> polygon,
			LineLatLng gridLine) {

		ArrayList<Coord2D> crossings = new ArrayList<Coord2D>();
		for (LineLatLng polyLine : polygon) {
			try {
				crossings.add(LineTools
						.FindLineIntersection(polyLine, gridLine));
			} catch (Exception e) {
			}
		}

		return crossings;
	}

	private void processCrossings(ArrayList<Coord2D> crosses,
			LineLatLng gridLine) {
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
