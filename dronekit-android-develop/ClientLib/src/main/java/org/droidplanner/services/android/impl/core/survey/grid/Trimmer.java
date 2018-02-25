package org.droidplanner.services.android.impl.core.survey.grid;

import org.droidplanner.services.android.impl.core.helpers.geoTools.LineLatLong;
import org.droidplanner.services.android.impl.core.helpers.geoTools.LineTools;
import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.ArrayList;
import java.util.List;

public class Trimmer {
	List<LineLatLong> trimedGrid = new ArrayList<LineLatLong>();

	public Trimmer(List<LineLatLong> grid, List<LineLatLong> polygon) {
		for (LineLatLong gridLine : grid) {
			ArrayList<LatLong> crosses = findCrossings(polygon, gridLine);
			processCrossings(crosses, gridLine);
		}
	}

	private ArrayList<LatLong> findCrossings(List<LineLatLong> polygon, LineLatLong gridLine) {

		ArrayList<LatLong> crossings = new ArrayList<LatLong>();
		for (LineLatLong polyLine : polygon) {
            LatLong intersection = LineTools.FindLineIntersection(polyLine, gridLine);
            if(intersection != null)
                crossings.add(intersection);
		}

		return crossings;
	}

	private void processCrossings(ArrayList<LatLong> crosses, LineLatLong gridLine) {
		switch (crosses.size()) {
		case 0:
		case 1:
			break;
		case 2:
			trimedGrid.add(new LineLatLong(crosses.get(0), crosses.get(1)));
			break;
		default: // TODO handle multiple crossings in a better way
			trimedGrid.add(LineTools.findExternalPoints(crosses));
		}
	}

	public List<LineLatLong> getTrimmedGrid() {
		return trimedGrid;
	}

}
