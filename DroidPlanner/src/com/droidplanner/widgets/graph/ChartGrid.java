package com.droidplanner.widgets.graph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ChartGrid {
	public Paint grid_paint = new Paint();

	public ChartGrid() {
		grid_paint.setColor(Color.rgb(100, 100, 100));
	}

	void drawGrid(Chart chart, Canvas canvas) {
		// clear screen
		canvas.drawColor(Color.rgb(20, 20, 20));

		for (int vertical = 1; vertical < 10; vertical++) {
			canvas.drawLine(vertical * (chart.width / 10) + 1, 1, vertical
					* (chart.width / 10) + 1, chart.height + 1, grid_paint);

		}

		for (int horizontal = 1; horizontal < 10; horizontal++) {
			canvas.drawLine(1, horizontal * (chart.height / 10) + 1,
					chart.width + 1, horizontal * (chart.height / 10) + 1,
					grid_paint);

		}
	}
}