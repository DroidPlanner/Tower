package com.droidplanner.widgets.graph;

import android.graphics.Canvas;
import android.graphics.Paint;

public class ChartDataRender {
	public int numPtsToDraw;

	public ChartDataRender(int numPtsToDraw) {
		this.numPtsToDraw = numPtsToDraw;
	}

	void drawData(Chart chart, Canvas canvas) {
		// scale the data to +- 500
		// target 0-height
		// so D in the range +-500
		// (D + 500) / 1000 * height
	
		float delta = (float) chart.width / (float) numPtsToDraw;
	
		for (int k = 0; k < chart.chartData.data.length; k++) {
			if (!chart.chartData.entryEnabled[k])
				continue;
	
			if (chart.chartData.data[k].length > 0) {
				int start = (chart.chartData.newestData - numPtsToDraw + chart.chartData.data[0].length)
						% chart.chartData.data[0].length;
				int pos = 0;
				for (int i = start; i < start + numPtsToDraw; i++) {
	
					double y_i = chart.chartData.data[k][i % chart.chartData.data[0].length];
					y_i = (y_i + chart.scale.range) / (2 * chart.scale.range) * chart.height;
	
					double y_i1 = chart.chartData.data[k][(i + 1) % chart.chartData.data[0].length];
					y_i1 = (y_i1 + chart.scale.range) / (2 * chart.scale.range) * chart.height;
	
					canvas.drawLine((float) pos * delta, (float) y_i,
							(float) (pos + 1) * delta, (float) y_i1,
							chart.availableColors[k]);
					pos++;
				}
			}
		}
	}

	public void setDrawRate(Chart chart, int p) {
		if (p > 0)
			numPtsToDraw = chart.width / p;
	
	}

	public void setColors(Chart chart, Paint[] p) {
	
		if (p.length != chart.chartData.dataSize)
			chart.chartData.dataSize = 0;
	
		chart.availableColors = p;
	
		for (Paint p1 : chart.availableColors)
			p1.setTextSize(17.0f * chart.getContext().getResources()
					.getDisplayMetrics().density);
	
	}
}