package com.droidplanner.widgets.graph.series;


public class DynamicSeries extends ChartSeries {

	public int newestData = 0;
	public DynamicSeries(int bufferSize) {
		this.data = new double[bufferSize];
	}

	public void newData(double d) {
		if (data.length > 0) {
			newestData = (newestData + 1) % data.length;
			data[newestData] = d;
		}
	}

}
