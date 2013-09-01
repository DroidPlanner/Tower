package com.droidplanner.widgets.graph.series;

import java.util.List;

public class StaticSeries extends ChartSeries {

	public StaticSeries(List<Integer> values) {
		super(values.size());
		for (int i = 0; i < values.size(); i++) {
			data[i] = values.get(i);
		}
		enable();
	}

	@Override
	public int getFirstIndex() {
		return 0;
	}

	public int getSize() {
		return data.length;
	}

}
