package com.droidplanner.widgets.graph;

import android.graphics.Color;
import android.graphics.Paint;

public class ChartSeries {

	boolean entryEnabled = true;
	public double[] data;
	public int newestData = 0;
	public Paint paint;
	
	public ChartSeries(int bufferSize,int color) {
		this.data = new double[bufferSize];
		this.paint = new Paint();
		this.paint.setColor(color);
	}
	
	public void newData(double d) {
		if (data.length>0) {		
			newestData = (newestData + 1) % data.length;
			data[newestData] = d;		
		}
	}

	public Paint getPaint() {
		if (entryEnabled) {
			return paint;			
		}else{
			return new Paint(Color.WHITE);
		}
	}
}
