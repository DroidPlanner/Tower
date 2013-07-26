package com.droidplanner.widgets.graph;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

public class ChartData {
	public List<ChartSeries> series = new ArrayList<ChartSeries>();

	public ChartData() {
		series.add(new ChartSeries(800,Color.RED));
		series.add(new ChartSeries(800,Color.BLUE));
		series.add(new ChartSeries(800,Color.GREEN));
	}

	public void enableEntry(int i) {
		series.get(i).entryEnabled = true;
	}

	public void disableEntry(int i) {
			series.get(i).entryEnabled = false;
	}

	public boolean isActive(int i) {
			return series.get(i).entryEnabled;
	}
}