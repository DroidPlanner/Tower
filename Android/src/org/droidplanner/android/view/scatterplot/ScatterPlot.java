package org.droidplanner.android.view.scatterplot;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ScatterPlot extends View {
	private static final float SCALE_FACTOR = 1 / 1000f;

    private final RectF reuseRectF = new RectF();
	private float halfWidth, halfHeight, halfScale;

	private ArrayList<Float> points = new ArrayList<Float>();
	private Paint paintText, paintChartLines, paintPoints, paintEndPoint,paintCircle;
	private String title = "";

	private int[] sphere;


	public ScatterPlot(Context context, AttributeSet attrs) {
		super(context, attrs);
		paintChartLines = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintChartLines.setColor(Color.GRAY);

		paintPoints = new Paint();
		paintPoints.setColor(Color.WHITE);
		paintPoints.setStrokeWidth(3f);
		paintEndPoint = new Paint(paintPoints);
		paintEndPoint.setColor(Color.RED);
		paintCircle = new Paint(paintPoints);
		paintCircle.setStyle(Paint.Style.STROKE);
		paintCircle.setColor(Color.BLUE);

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

    public void addData(float datum){
        points.add(datum);
    }

	public void updateSphere(int[] sphere) {
		this.sphere = sphere;
	}

    public void reset(){
        points.clear();
        this.sphere = null;
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
        final int pointsCount = points.size();
		for (int i = 0; i < pointsCount; i += 2) {
			x = mapToImgX(points.get(i + 0));
			y = mapToImgY(points.get(i + 1));
			canvas.drawPoint(x, y, paintPoints);
		}
		canvas.drawCircle(x, y, 10f, paintEndPoint); // Redraw the endpoint with a bigger dot
		
		// Draw the estimated Sphere
		if (sphere != null) {
			x = mapToImgX(sphere[0]);
			y = mapToImgY(sphere[1]);
			int width = (int) scale(sphere[2]);
			int height = (int) scale(sphere[3]);

            this.reuseRectF.set(x -width, y - height, x + width, y + height);
			canvas.drawOval(this.reuseRectF,paintCircle);
		}
	}
	
	private int mapToImgX(float coord) {
		return (int) (scale(coord) + halfWidth);
	}

	private int mapToImgY(float coord) {
		return (int) (-scale(coord) + halfHeight);
	}

	private float scale(float value) {
		return SCALE_FACTOR * halfScale * value;
	}

}
