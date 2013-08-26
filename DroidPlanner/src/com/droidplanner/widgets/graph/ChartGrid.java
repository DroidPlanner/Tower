package com.droidplanner.widgets.graph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ChartGrid {
	public Paint grid_paint = new Paint();
	public Paint grid_paint_center_line = new Paint();

	public ChartGrid() {
		grid_paint.setColor(Color.rgb(100, 100, 100));
		grid_paint_center_line.setColor(Color.rgb(100, 100, 100));
		grid_paint_center_line.setStrokeWidth(5f);
	}

	void drawGrid(Canvas canvas) {
		// clear screen
		canvas.drawColor(Color.rgb(20, 20, 20));

		for (int vertical = 1; vertical < 10; vertical++) {
			int spacing = vertical * (canvas.getWidth() / 10) + 1;
			canvas.drawLine(spacing, 1, spacing, canvas.getHeight() + 1,
					grid_paint);

		}

		for (int horizontal = 1; horizontal < 10; horizontal++) {
			Paint paint;
			if (horizontal == 5) {
				paint = grid_paint_center_line;
			} else {
				paint = grid_paint;
			}
			int spacing = horizontal * (canvas.getHeight() / 10) + 1;
			canvas.drawLine(1, spacing, canvas.getWidth() + 1, spacing, paint);

		}
	}
}