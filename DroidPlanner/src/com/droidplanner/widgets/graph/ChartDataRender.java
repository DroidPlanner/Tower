package com.droidplanner.widgets.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

public class ChartDataRender {
	public int numPtsToDraw = 100;
	
	public ChartDataRender(Context context) {		
	}

	void drawSeries(Chart chart, Canvas canvas, ChartSeries serie) {
		// scale the data to +- 500
		// target 0-height
		// so D in the range +-500
		// (D + 500) / 1000 * height

		float delta = (float) chart.width / (float) numPtsToDraw;
				
		double[] chartData = serie.data;
		int length0 = chartData.length;
		boolean enabled = serie.entryEnabled;
		int newestData = serie.newestData;
		Paint color = serie.getPaint();
		
		double range = chart.scale.range;
		int height = chart.height;

		if (enabled & length0>0) {

			int start = (newestData - numPtsToDraw + length0) % length0;
			int pos = 0;
			for (int i = start; i < start + numPtsToDraw; i++) {

				double y_i = chartData[i % length0];
				y_i = (y_i + range) / (2 * range) * height;

				double y_i1 = chartData[(i + 1) % length0];
				y_i1 = (y_i1 + range) / (2 * range) * height;

				canvas.drawLine((float) pos * delta, (float) y_i,
						(float) (pos + 1) * delta, (float) y_i1, color);
				pos++;
			}
		}
	}

	public void setDrawRate(Chart chart, int p) {
		if (p > 0)
			numPtsToDraw = chart.width / p;
	}
}