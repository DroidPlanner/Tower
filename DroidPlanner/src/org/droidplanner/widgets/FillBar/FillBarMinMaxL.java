package org.droidplanner.widgets.FillBar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidplanner.R;

public class FillBarMinMaxL extends LinearLayout {

	private int max;
	private int min;
	private TextView title;
	private TextView value;
	private TextView minValue;
	private TextView maxValue;
	private FillBar bar;
	private boolean showMinMax;

	public FillBarMinMaxL(Context context, AttributeSet attrs) {
		super(context, attrs);

		setOrientation(VERTICAL);

		inflate(context, R.layout.subview_minmax_fillbar_left, this);

		title = (TextView) findViewById(R.id.textViewTitle);
		value = (TextView) findViewById(R.id.TextViewMid);
		minValue = (TextView) findViewById(R.id.TextViewMin);
		maxValue = (TextView) findViewById(R.id.TextViewMax);
		bar = (FillBar) findViewById(R.id.fillBar);
	}

	public void setup(String title, int max, int min) {
		this.max = max;
		this.min = min;
		this.title.setText(title);
	}

	public void setValue(int value) {
		this.value.setText(Integer.toString(value));
		this.bar.setPercentage((value - min) / ((float) (max - min)));
		int fmin = min + (int) (bar.getMin() * ((float) (max - min)));
		int fmax = min + (int) (bar.getMax() * ((float) (max - min)));
		maxValue.setText(String.valueOf(fmax));
		minValue.setText(String.valueOf(fmin));
	}

	public boolean isShowMinMax() {
		return showMinMax;
	}

	public void setShowMinMax(boolean showMinMax) {
		this.showMinMax = showMinMax;
		if (bar != null) {
			bar.setShowMinMax(showMinMax);
		}
	}

	public int getMin() {
		return min + (int) (bar.getMin()*((float)(max-min)));
	}

	public int getMax() {
		return min + (int) (bar.getMax()*((float)(max-min)));
	}
}
