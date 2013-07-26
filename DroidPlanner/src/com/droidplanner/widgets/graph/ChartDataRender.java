package com.droidplanner.widgets.graph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ChartDataRender {
	public int numPtsToDraw = 100;
	Paint[] availableColors;

	public ChartDataRender(Context context) {
		
		int[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.CYAN,
				Color.MAGENTA, Color.YELLOW, 0xFF800000, 0xff008000,
				0xFF000080, 0xFF008080, 0xFF800080 };

		Paint[] p = new Paint[colors.length];
		for (int i = 0; i < p.length; i++) {
			p[i] = new Paint();
			p[i].setColor(colors[i]);
		}
		setColors(context, p);
	}

	void drawData(Chart chart, Canvas canvas) {
		// scale the data to +- 500
		// target 0-height
		// so D in the range +-500
		// (D + 500) / 1000 * height
	
		float delta = (float) chart.width / (float) numPtsToDraw;
	
		for (int k = 0; k < chart.chartData.data.length; k++) {
			if (!chart.chartData.entryEnabled[k])
				continue;
	
			if (chart.chartData.data[k].length > 0) {
				int start = (chart.chartData.newestData - numPtsToDraw + chart.chartData.data[0].length)
						% chart.chartData.data[0].length;
				int pos = 0;
				for (int i = start; i < start + numPtsToDraw; i++) {
	
					double y_i = chart.chartData.data[k][i % chart.chartData.data[0].length];
					y_i = (y_i + chart.scale.range) / (2 * chart.scale.range) * chart.height;
	
					double y_i1 = chart.chartData.data[k][(i + 1) % chart.chartData.data[0].length];
					y_i1 = (y_i1 + chart.scale.range) / (2 * chart.scale.range) * chart.height;
	
					canvas.drawLine((float) pos * delta, (float) y_i,
							(float) (pos + 1) * delta, (float) y_i1,
							chart.dataRender.availableColors[k]);
					pos++;
				}
			}
		}
	}

	void drawSeries(Chart chart, Canvas canvas) {
		// scale the data to +- 500
		// target 0-height
		// so D in the range +-500
		// (D + 500) / 1000 * height

		float delta = (float) chart.width / (float) numPtsToDraw;
				
		double[] chartData = chart.chartData.series.get(0).data;
		int length0 = chartData.length;
		boolean enabled = chart.chartData.series.get(0).entryEnabled;
		int newestData = chart.chartData.series.get(0).newestData;
		Paint color = chart.dataRender.availableColors[0];
		
		double range = chart.scale.range;
		int height = chart.height;

		if (enabled & length0>0) {

			int start = (newestData - numPtsToDraw + length0) % length0;
			int pos = 0;
			for (int i = start; i < start + numPtsToDraw; i++) {

				double y_i = chartData[i % length0];
				y_i = (y_i + range) / (2 * range) * height;

				double y_i1 = chartData[(i + 1) % length0];
				y_i1 = (y_i1 + range) / (2 * range) * height;

				canvas.drawLine((float) pos * delta, (float) y_i,
						(float) (pos + 1) * delta, (float) y_i1, color);
				pos++;
			}
		}
	}

	public void setDrawRate(Chart chart, int p) {
		if (p > 0)
			numPtsToDraw = chart.width / p;
	
	}

	public void setColors(Context context, Paint[] p) {
		availableColors = p;
	
		for (Paint p1 : availableColors)
			p1.setTextSize(17.0f * context.getResources()
					.getDisplayMetrics().density);
	
	}

	public int getEntryColor(int i) {
	
		if (i < availableColors.length)
			return availableColors[i].getColor();
	
		return Color.WHITE;
	}
}