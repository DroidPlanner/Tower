package org.droidplanner.android.widgets.scatterplot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ScatterPlot extends View {

	Paint chartLines, pointsPaint;
	private float halfWidth, halfHeight, halfScale;

	private Float[] points = new Float[] {};

	public ScatterPlot(Context context, AttributeSet attrs) {
		super(context, attrs);
		chartLines = new Paint(Paint.ANTI_ALIAS_FLAG);
		chartLines.setColor(Color.GRAY);

		pointsPaint = new Paint();
		pointsPaint.setColor(Color.WHITE);
		pointsPaint.setStrokeWidth(3f);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		halfWidth = w / 2f;
		halfHeight = h / 2f;
		halfScale = (halfHeight > halfWidth) ? halfWidth : halfHeight;
	}

	public void newDataSet(Float[] array) {
		points = array;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw the graph lines
		canvas.drawLine(halfWidth, 0, halfWidth, halfHeight * 2, chartLines);
		canvas.drawLine(0, halfHeight, halfWidth * 2, halfHeight, chartLines);

		// Draw the points
		for (int i = 0; i < points.length; i += 2) {
			float x = halfScale * points[i + 0] + halfWidth;
			float y = -halfScale * points[i + 1] + halfHeight;
			canvas.drawPoint(x, y, pointsPaint);
		}
	}

}
