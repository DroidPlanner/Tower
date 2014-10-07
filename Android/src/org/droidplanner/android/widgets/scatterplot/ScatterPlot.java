package org.droidplanner.android.widgets.scatterplot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ScatterPlot extends View {

	private float halfWidth, halfHeight, halfScale;

	private Float[] points = new Float[] {};
	private Paint paintText, paintChartLines, paintPoints, paintEndPoint;
	private String title = "";

	public ScatterPlot(Context context, AttributeSet attrs) {
		super(context, attrs);
		paintChartLines = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintChartLines.setColor(Color.GRAY);

		paintPoints = new Paint();
		paintPoints.setColor(Color.WHITE);
		paintPoints.setStrokeWidth(3f);
		paintEndPoint = new Paint(paintPoints);
		paintEndPoint.setColor(Color.RED);
		paintEndPoint.setStrokeWidth(10f);

		paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintText.setTextSize(20f);
		paintText.setColor(Color.WHITE);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		halfWidth = w / 2f;
		halfHeight = h / 2f;
		halfScale = (halfHeight > halfWidth) ? halfWidth : halfHeight;
	}

	public void setTitle(String title) {
		this.title = title;
		invalidate();
	}

	public void newDataSet(Float[] array) {
		points = array;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawText(title, 0, paintText.getTextSize(), paintText);

		// Draw the graph lines
		canvas.drawLine(halfWidth, 0, halfWidth, halfHeight * 2, paintChartLines);
		canvas.drawLine(0, halfHeight, halfWidth * 2, halfHeight, paintChartLines);

		// Draw the points
		float x = 0, y = 0;
		for (int i = 0; i < points.length; i += 2) {
			x = halfScale * points[i + 0] + halfWidth;
			y = -halfScale * points[i + 1] + halfHeight;
			canvas.drawPoint(x, y, paintPoints);
		}
		canvas.drawPoint(x, y, paintEndPoint); // Redraw the endpoint with a
												// bigger dot
	}

}
