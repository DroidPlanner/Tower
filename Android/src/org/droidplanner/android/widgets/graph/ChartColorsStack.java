package org.droidplanner.android.widgets.graph;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

public class ChartColorsStack {
	List<Integer> avaliableColors = new ArrayList<Integer>();

	public ChartColorsStack() {
		avaliableColors.add(Color.RED);
		avaliableColors.add(Color.BLUE);
		avaliableColors.add(Color.GREEN);
		avaliableColors.add(Color.YELLOW);
		avaliableColors.add(Color.MAGENTA);
		avaliableColors.add(Color.CYAN);
	}

	public Integer retriveColor() {
		if (avaliableColors.size() > 0) {
			Integer color = avaliableColors.get(0);
			avaliableColors.remove(0);
			return color;
		} else {
			return Color.WHITE;
		}
	}

	public void depositColor(Integer color) {
		avaliableColors.add(0, color);
	}
}
