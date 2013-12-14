package com.droidplanner.widgets.graph;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.droidplanner.widgets.graph.ChartScaleHandler.OnScaleListner;
import com.droidplanner.widgets.graph.series.ChartSeries;
import com.droidplanner.widgets.helpers.RenderThread;
import com.droidplanner.widgets.helpers.RenderThread.canvasPainter;

/*
 * Widget for a Chart Originally copied from http://code.google.com/p/copter-gcs/
 */
public class Chart extends SurfaceView implements SurfaceHolder.Callback,
		canvasPainter, OnScaleListner {
	private RenderThread renderer;
	protected int width;
	protected int height;

	public ChartColorsStack colors = new ChartColorsStack();
	public ChartScaleHandler scale;
	private ChartGrid grid = new ChartGrid();
	public List<ChartSeries> series = new ArrayList<ChartSeries>();
	public ChartDataRender dataRender = new ChartDataRender();

	public Chart(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		getHolder().addCallback(this);

		scale = new ChartScaleHandler(context, this);
	}

	@Override
	public void onDraw(Canvas canvas) {
		grid.drawGrid(canvas,scale);
		for (ChartSeries serie : series) {
			dataRender.drawSeries(this, canvas, serie);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		this.width = width;
		this.height = height;
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

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// Let the ScaleGestureDetector inspect all events.
		super.onTouchEvent(ev);
		scale.onTouchEvent(ev);
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
