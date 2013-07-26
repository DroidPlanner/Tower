package com.droidplanner.widgets.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.droidplanner.widgets.graph.ChartScale.OnScaleListner;
import com.droidplanner.widgets.helpers.RenderThread;
import com.droidplanner.widgets.helpers.RenderThread.canvasPainter;

/*
 * Widget for a Chart Originally copied from http://code.google.com/p/copter-gcs/
 */
public class Chart extends SurfaceView implements SurfaceHolder.Callback,
		canvasPainter, OnScaleListner {
	public RenderThread renderer;
	int width;
	int height;

	private Paint[] availableColors;
	private boolean entryEnabled[] = { true, true };

	private ChartScale scale;
	private ChartData chartData = new ChartData();
	// Number of entries to draw
	
	private int numPtsToDraw = 100;

	private ChartGrid grid = new ChartGrid();

	public Chart(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		getHolder().addCallback(this);

		scale = new ChartScale(context,this);


		int[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.CYAN,
				Color.MAGENTA, Color.YELLOW, 0xFF800000, 0xff008000,
				0xFF000080, 0xFF008080, 0xFF800080 };

		Paint[] p = new Paint[colors.length];
		for (int i = 0; i < p.length; i++) {
			p[i] = new Paint();
			p[i].setColor(colors[i]);
		}
		setColors(p);

	}

	public void setColors(Paint[] p) {

		if (p.length != chartData.dataSize)
			chartData.dataSize = 0;

		availableColors = p;

		for (Paint p1 : availableColors)
			p1.setTextSize(17.0f * getContext().getResources()
					.getDisplayMetrics().density);

	}

	public void setDataSize(int d) {
		chartData.dataSize = d;
		chartData.data = new double[chartData.dataSize][width];
		entryEnabled = new boolean[chartData.dataSize];

	}

	@Override
	public void onDraw(Canvas canvas) {

		grid.drawGrid(this, canvas);

		// scale the data to +- 500
		// target 0-height
		// so D in the range +-500
		// (D + 500) / 1000 * height

		float delta = (float) width / (float) numPtsToDraw;

		for (int k = 0; k < chartData.data.length; k++) {
			if (!entryEnabled[k])
				continue;

			if (chartData.data[k].length > 0) {
				int start = (chartData.newestData - numPtsToDraw + chartData.data[0].length)
						% chartData.data[0].length;
				int pos = 0;
				for (int i = start; i < start + numPtsToDraw; i++) {

					double y_i = chartData.data[k][i % chartData.data[0].length];
					y_i = (y_i + scale.range) / (2 * scale.range) * height;

					double y_i1 = chartData.data[k][(i + 1) % chartData.data[0].length];
					y_i1 = (y_i1 + scale.range) / (2 * scale.range) * height;

					canvas.drawLine((float) pos * delta, (float) y_i,
							(float) (pos + 1) * delta, (float) y_i1,
							availableColors[k]);
					pos++;
				}
			}
		}

	}

	public void newData(double[] d) {
		chartData.newData(d);
		update();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		this.width = width;
		this.height = height;
		chartData.data = new double[chartData.dataSize][width];

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		renderer = new RenderThread(getHolder(), this);
		if (!renderer.isRunning()) {
			renderer.setRunning(true);
			renderer.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		renderer.setRunning(false);
		while (retry) {
			try {
				renderer.join();
				renderer = null;
				retry = false;
			} catch (InterruptedException e) {
				// we will try it again and again...
			}
		}
	}

	public int getEntryColor(int i) {

		if (i < availableColors.length)
			return availableColors[i].getColor();

		return Color.WHITE;
	}

	public boolean isActive(int i) {
		if (i < entryEnabled.length)
			return entryEnabled[i];

		return false;

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// Let the ScaleGestureDetector inspect all events.
		super.onTouchEvent(ev);
		scale.scaleDetector.onTouchEvent(ev);
		return true;

	}

	public void disableEntry(int i) {
		if (i < entryEnabled.length)
			entryEnabled[i] = false;

	}

	public int enableEntry(int i) {
		if (i < entryEnabled.length) {
			entryEnabled[i] = true;
			return availableColors[i].getColor();

		}

		return -1;
	}

	public void setDrawRate(int p) {
		if (p > 0)
			numPtsToDraw = width / p;

	}

	public void update() {
		if (renderer != null)
			renderer.setDirty();
	}

	@Override
	public void onScaleListner() {
		update();		
	}
}
