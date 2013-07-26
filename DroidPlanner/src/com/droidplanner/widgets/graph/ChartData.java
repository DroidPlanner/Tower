package com.droidplanner.widgets.graph;

public class ChartData {
	public double[][] data = new double[0][0];
	public int dataSize = 0;
	public int newestData = 0;

	public ChartData() {
	}

	void newData(double[] d) {
		if (data.length > 0) {
			int newIndex = (newestData + 1) % data[0].length;
	
			for (int i = 0; i < data.length; i++)
				if (data[i].length > newIndex)
					data[i][newIndex] = d[i];
	
			newestData = newIndex;
	
		}
	}
}