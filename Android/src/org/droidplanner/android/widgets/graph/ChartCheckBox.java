package org.droidplanner.android.widgets.graph;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ChartCheckBox extends CheckBox implements OnCheckedChangeListener {
	public ChartCheckBox(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private Chart chart;

	public ChartCheckBox(Context context, String label, Chart chart) {
		super(context);
		ChartSeries serie = new ChartSeries(800);
		this.chart = chart;
		this.chart.series.add(serie);
		setText(label);
		setChecked(serie.isActive());
		setGravity(Gravity.LEFT);
		setTag(serie);
		setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
		ChartSeries serie = (ChartSeries) getTag();
		if (isChecked) {
			serie.enable();
			Integer color = chart.colors.retriveColor();
			serie.setColor(color);
			setTextColor(color);
		} else {
			serie.disable();
			chart.colors.depositColor(serie.getColor());
			setTextColor(Color.WHITE);
		}
		chart.update();
	}
}
