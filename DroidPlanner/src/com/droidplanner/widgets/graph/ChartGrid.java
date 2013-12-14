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

	void drawGrid(Canvas canvas, ChartScaleHandler scale) {
		drawBackground(canvas);
		drawVerticalLines(canvas, scale);
		drawHorizontalLines(canvas);
	}

	private void drawBackground(Canvas canvas) {
		canvas.drawColor(Color.rgb(20, 20, 20));
	}

	private void drawHorizontalLines(Canvas canvas) {
		for (int horizontal = 1; horizontal < 10; horizontal++) {
			Paint paint;
			if (horizontal == 5) {
				paint = grid_paint_center_line;
			} else {
				paint = grid_paint;
			}
			int spacingY = horizontal * (canvas.getHeight() / 10) + 1;
			canvas.drawLine(1, spacingY, canvas.getWidth() + 1, spacingY, paint);

		}
	}

	private void drawVerticalLines(Canvas canvas, ChartScaleHandler scale) {
		double spacingX = (canvas.getWidth() / scale.x.getGridSize());
		for (double vertical = -scale.x.getOffset() % spacingX; vertical < canvas
				.getWidth(); vertical += spacingX) {
			canvas.drawLine((int) vertical, 1, (int) vertical,
					canvas.getHeight() + 1, grid_paint);
		}
	}
}