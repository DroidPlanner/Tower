package com.droidplanner.widgets.graph;

import android.graphics.Canvas;

import com.droidplanner.widgets.graph.series.ChartSeries;

public class ChartDataRender {
	private int numPtsToDraw = 100;

	protected void drawSeries(Chart chart, Canvas canvas, ChartSeries serie) {
		// scale the data to +- 500
		// target 0-height
		// so D in the range +-500
		// (D + 500) / 1000 * height

		float delta = (float) chart.width / (float) numPtsToDraw;

		if (serie.isActive()) {

			int start = (serie.getFirstIndex() - numPtsToDraw + serie.data.length)
					% serie.data.length;
			int pos = 0;
			for (int i = start; i < start + numPtsToDraw -1; i++) {

				double y_i = -serie.data[i % serie.data.length];
				y_i = (y_i + chart.scale.scaleY.getRange(chart.scale))
						/ (2 * chart.scale.scaleY.getRange(chart.scale)) * chart.height;

				double y_i1 = -serie.data[(i + 1) % serie.data.length];
				y_i1 = (y_i1 + chart.scale.scaleY.getRange(chart.scale))
						/ (2 * chart.scale.scaleY.getRange(chart.scale)) * chart.height;

				canvas.drawLine((float) pos * delta, (float) y_i,
						(float) (pos + 1) * delta, (float) y_i1,
						serie.getPaint());
				pos++;
			}
		}
	}

	protected void setDrawRate(Chart chart, int p) {
		if (p > 0)
			numPtsToDraw = chart.width / p;
	}

	public void setNumberOfPointsToDraw(int i) {
		numPtsToDraw = i;
	}
}