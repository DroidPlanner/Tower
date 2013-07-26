package com.droidplanner.widgets.graph;

public class ChartSeries {

	boolean entryEnabled = true;
	public double[] data;
	public int newestData = 0;
	
	public ChartSeries(int bufferSize) {
		this.data = new double[bufferSize];
	}
	
	public void newData(double d) {
		if (data.length>0) {		
			newestData = (newestData + 1) % data.length;
			data[newestData] = d;		
		}
	}
}
