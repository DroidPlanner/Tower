package com.droidplanner.widgets.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ScopeDisplay extends SurfaceView implements SurfaceHolder.Callback {
	private ScopeThread renderer;
	private int width;
	private int height;
	
	double[][] data = null;
	Paint[] availableColors = null;
	boolean entryEnabled[];
	
	int dataSize = 0;
	
	ScaleGestureDetector scaleDetector;
	
	// Loop counter for circular buffer
	int newestData = 0;
	
	// Number of entries to draw
	int numPtsToDraw = 100;

	// range values to display
	int range = 700;
	
	private Paint grid_paint = new Paint();

	Integer[] colors;
	private String[] names = null;
		
	public ScopeDisplay(Context context, AttributeSet attributeSet) {
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

	public void setNames(String[] n){
		names = n;
		
	}
	
	public void setDataSize( int d){
		dataSize = d;
		data = new double[dataSize][width];
		entryEnabled = new boolean[dataSize];
				
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

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
	    
	    // lets draw us some labels

	    if( names != null){
		    canvas.translate(width/2, height/2);
		    
		    int pos = 0;
		    float textHeight = availableColors[0].getFontMetrics().bottom - availableColors[0].getFontMetrics().top;
		    int numEntries = 0;
		    for( int i = 0; i < names.length; i++)
		    	if(entryEnabled[i])
		    		numEntries++;
		    
			float offset = (float)numEntries * textHeight * 1.2f - textHeight*.2f;
			canvas.translate(0, -height/2.0f + offset/2.0f);

		    for( int i = 0; i < names.length; i++){
		    	if(entryEnabled[i]){
		    		drawText(canvas, pos, names[i], availableColors[i], true);
		    		pos ++;
		    	}
		    }
		    
	    }

	}

    // Draw text aligned correctly.
	private void drawText(Canvas canvas, int i, String text, Paint p, boolean left){
		Rect bounds = new Rect();
		p.getTextBounds(text, 0, text.length(), bounds);
		float textHeight = p.getFontMetrics().bottom - p.getFontMetrics().top;
		
		float y = (float) (height/2.0 - (float)i * textHeight * 1.2f - textHeight*.2); 
		
		
		canvas.drawText(text, (float)(-width/2.0 + textHeight*.2f), y, p);
		
		
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
	}
		
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		this.width = width;
		this.height = height;
		data = new double[dataSize][width];	
	
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		renderer = new ScopeThread(getHolder(), this);
		if(! renderer.isRunning()){
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

	
	private class ScopeThread extends Thread {
		private SurfaceHolder _surfaceHolder;
		private ScopeDisplay scope;
		private boolean running = false;

		public ScopeThread(SurfaceHolder surfaceHolder, ScopeDisplay panel) {
			_surfaceHolder = surfaceHolder;
			scope = panel;
		}

		public boolean isRunning(){
			return running;
			
		}
		public void setRunning(boolean run) {
			running = run;

		}

		@Override
		public void run() {
			Canvas c;
			while (running) {
				c = null;
				try {
					c = _surfaceHolder.lockCanvas(null);
					synchronized (_surfaceHolder) {
						scope.onDraw(c);
					}
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						_surfaceHolder.unlockCanvasAndPost(c);
					}
				}
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
	        range = Math.max(50, Math.min(range, 3000));

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
}
