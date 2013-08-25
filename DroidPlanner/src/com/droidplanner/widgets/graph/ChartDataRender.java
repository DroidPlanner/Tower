package com.droidplanner.widgets.graph;

import android.graphics.Canvas;

import com.droidplanner.widgets.graph.series.ChartSeries;

public class ChartDataRender {

	protected void drawSeries(Chart chart, Canvas canvas, ChartSeries serie) {
		
		float delta = (float) chart.width / (float) chart.scale.x.getRange();

		if (serie.isActive()) {

			int start = (int) ((serie.getFirstIndex() - chart.scale.x.getRange() + serie.data.length)
					% serie.data.length);
			int pos = 0;
			for (int i = start; i < start + chart.scale. x.getRange() -1; i++) {

				double y_i = -serie.data[i % serie.data.length];
				y_i = (y_i + chart.scale.y.getRange() - chart.scale.y.getOffset())
						/ (2 * chart.scale.y.getRange()) * chart.height;

				double y_i1 = -serie.data[(i + 1) % serie.data.length];
				y_i1 = (y_i1 + chart.scale.y.getRange() - chart.scale.y.getOffset())
						/ (2 * chart.scale.y.getRange()) * chart.height;

				canvas.drawLine((float) pos * delta, (float) y_i,
						(float) (pos + 1) * delta, (float) y_i1,
						serie.getPaint());
				pos++;
			}
		}
	}

}