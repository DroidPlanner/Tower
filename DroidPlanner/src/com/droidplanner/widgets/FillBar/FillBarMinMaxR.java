package com.droidplanner.widgets.FillBar;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidplanner.R;

public class FillBarMinMaxR extends LinearLayout {

	private int max;
	private int min;
	private TextView title;
	private TextView value;
	private TextView minValue;
	private TextView maxValue;
	private FillBar bar;

	public FillBarMinMaxR(Context context, AttributeSet attrs) {
		super(context, attrs);

		setOrientation(VERTICAL);

		inflate(context, R.layout.subview_minmax_fillbar_right, this);

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
		this.bar.setColorBar(Color.parseColor("#0EF80E"));
	}

	public void setValue(int value) {
		this.value.setText(Integer.toString(value));
		this.bar.setPercentage((value - min) / ((float)(max - min)));
		int fmin = min + (int) (bar.getMin()*((float)(max-min)));
		int fmax = min + (int) (bar.getMax()*((float)(max-min)));
		maxValue.setText(String.valueOf(fmax));
		minValue.setText(String.valueOf(fmin));
	}

}
