package com.droidplanner.widgets.graph;

import java.util.ArrayList;
import java.util.List;

public class ChartData {
	public List<ChartSeries> series = new ArrayList<ChartSeries>();

	public ChartData() {
		series.add(new ChartSeries(800));
		series.add(new ChartSeries(800));
		series.add(new ChartSeries(800));
	}

	void newData(double[] d) {		
		series.get(0).newData(d[0]);
		series.get(1).newData(d[1]);
		series.get(2).newData(d[2]);
	}

	public int enableEntry(Chart chart, int i) {
		series.get(i).entryEnabled = true;
		return chart.dataRender.availableColors[i].getColor();
	}

	public void disableEntry(int i) {
			series.get(i).entryEnabled = false;
	}

	public boolean isActive(int i) {
			return series.get(i).entryEnabled;
	}
}