package com.droidplanner.widgets.graph;

public class ChartData {
	public double[][] data = new double[0][0];
	public int dataSize = 0;
	public int newestData = 0;
	boolean entryEnabled[] = { true, true };

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

	public int enableEntry(Chart chart, int i) {
		if (i < entryEnabled.length) {
			entryEnabled[i] = true;
			return chart.dataRender.availableColors[i].getColor();
	
		}
	
		return -1;
	}

	public void disableEntry(Chart chart, int i) {
		if (i < entryEnabled.length)
			entryEnabled[i] = false;
	
	}

	public boolean isActive(Chart chart, int i) {
		if (i < entryEnabled.length)
			return entryEnabled[i];
	
		return false;
	
	}

	public void setDataSize(Chart chart, int d) {
		dataSize = d;
		data = new double[dataSize][chart.width];
		entryEnabled = new boolean[dataSize];
	
	}
}