package com.droidplanner.widgets.graph.series;


public class DynamicSeries extends ChartSeries {

	public int newestData = 0;
	
	public DynamicSeries(int bufferSize) {
		super(bufferSize);
	}
	
	public void newData(double d) {
		if (data.length > 0) {
			newestData = (newestData + 1) % data.length;
			data[newestData] = d;
		}
	}

	@Override
	public int getFirstIndex() {
		return newestData;
	}

}
