package com.droidplanner.widgets.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

	ChartScale scale;
	public ChartData chartData = new ChartData();
	// Number of entries to draw
	
	ChartDataRender dataRender;
	private ChartGrid grid = new ChartGrid();

	public Chart(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		getHolder().addCallback(this);

		scale = new ChartScale(context,this);
		dataRender = new ChartDataRender(this);
	}

	@Override
	public void onDraw(Canvas canvas) {
		grid.drawGrid(this, canvas);
		dataRender.drawData(this, canvas);
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

		if (i < dataRender.availableColors.length)
			return dataRender.availableColors[i].getColor();

		return Color.WHITE;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// Let the ScaleGestureDetector inspect all events.
		super.onTouchEvent(ev);
		scale.scaleDetector.onTouchEvent(ev);
		return true;

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
