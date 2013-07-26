package com.droidplanner.widgets.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.droidplanner.widgets.helpers.RenderThread;
import com.droidplanner.widgets.helpers.RenderThread.canvasPainter;

/*
 * Widget for a Chart Originally copied from http://code.google.com/p/copter-gcs/
 */
public class Chart extends SurfaceView implements SurfaceHolder.Callback, canvasPainter {
	public RenderThread renderer;
	private int width;
	private int height;
	
	private double[][] data = {{0,1,2,0},{3,2,1,0}};
	private Paint[] availableColors = {new Paint(Color.RED), new Paint(Color.BLUE),new Paint(Color.GREEN),new Paint(Color.YELLOW)};
	private boolean entryEnabled[] = {true,true};
	
	private int dataSize = 2;
	
	private ScaleGestureDetector scaleDetector;
	
	// Loop counter for circular buffer
	private int newestData = 0;
	
	// Number of entries to draw
	private int numPtsToDraw = 100;

	// range values to display
	private double range = 10;
	
	private Paint grid_paint = new Paint();
		
	public Chart(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		getHolder().addCallback(this);
		
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener());

		grid_paint.setColor(Color.rgb(100, 100, 100));
		
	}

	public void setColors(Paint[] p){
		
		if( p.length != dataSize)
			dataSize = 0;
		
		availableColors = p;
		
		for( Paint p1 : availableColors)		
			p1.setTextSize(17.0f * getContext().getResources().getDisplayMetrics().density);
		
	}
	
	public void setDataSize( int d){
		dataSize = d;
		data = new double[dataSize][width];
		entryEnabled = new boolean[dataSize];
				
	}
	
	@Override
	public void onDraw(Canvas canvas) {

		// clear screen
		canvas.drawColor(Color.rgb(20, 20, 20));
		
	    drawGrid(canvas);	
	    
	    // scale the data to +- 500
	    // target 0-height
	    // so D in the range +-500
	    // (D + 500) / 1000 * height
	    
	    float delta = (float)width / (float)numPtsToDraw;
	    
	    for(int k= 0; k<data.length; k++){
	    	if(!entryEnabled[k])
	    		continue;
	    
	    	if( data[k].length > 0){
	    		int start = (newestData - numPtsToDraw + data[0].length) % data[0].length;
	    		int pos = 0;
			    for(int i = start; i < start+numPtsToDraw; i++){
			    	
			    	double y_i = data[k][i % data[0].length];
			    		y_i = (y_i + range) / (2*range) * height;
			    	
			    	double y_i1 = data[k][(i+1)% data[0].length];
			    		y_i1 = (y_i1 + range) / (2*range) * height;
			    	
			    	canvas.drawLine((float)pos*delta, (float)y_i, 
			    					(float)(pos+1)*delta, (float)y_i1, availableColors[k]);
			    	pos++;
			    }	    
	    	}
	    }
	    

	}


	
	private void drawGrid(Canvas canvas) {
		for(int vertical = 1; vertical<10; vertical++){
	    	canvas.drawLine(
	    			vertical*(width/10)+1, 1,
	    			vertical*(width/10)+1, height+1,
	    			grid_paint);
	    
	    }	    	
	    
	    for(int horizontal = 1; horizontal<10; horizontal++){
	    	canvas.drawLine(
	    			1, horizontal*(height/10)+1,
	    			width+1, horizontal*(height/10)+1,
	    			grid_paint);
	    	
	    }
	}

	public void newFlightData(double[] d) {
		if( d.length != data.length){
			Log.d("Scope", "Incopatible data sizes");
			return;
		}		
		
		if( data.length > 0){
			int newIndex = (newestData+1)% data[0].length;
			
			for( int i = 0; i < data.length; i++)
				if( data[i].length > newIndex)
					data[i][newIndex] = d[i];
			
			newestData = newIndex;
			
		}		
		
		update();
	}
		
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		this.width = width;
		this.height = height;
		data = new double[dataSize][width];	
	
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
		
		if( i < availableColors.length)
			return availableColors[i].getColor();
		
		return Color.WHITE;
	}

	public boolean isActive(int i) {
		if( i < entryEnabled.length)
			return entryEnabled[i];
		
		return false;
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
	    // Let the ScaleGestureDetector inspect all events.
		super.onTouchEvent(ev);
	    scaleDetector.onTouchEvent(ev);
	    return true;
	    
	}
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	    @Override
	    public boolean onScale(ScaleGestureDetector detector) {
	        range /= detector.getScaleFactor();
	        
	        Log.d("Scale", range+"");	       
	        range = Math.max(0.1, Math.min(range, 180));

	        return true;
	    }
	}

	public void disableEntry(int i) {
		if( i < entryEnabled.length)
			entryEnabled[i] = false;
		
	}

	public int enableEntry(int i) {
		if( i < entryEnabled.length){
			entryEnabled[i] = true;
			return availableColors[i].getColor();
		
		}
		
		return -1;
	}

	public void setDrawRate(int p) {
		if( p > 0)
			numPtsToDraw = width/p;
		
	}
	
	public void update() {
		if (renderer != null)
			renderer.setDirty();
	}
}
