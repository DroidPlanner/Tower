package com.droidplanner.widgets.FillBar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidplanner.R;

public class FillBarWithText extends LinearLayout {

	private int max;
	private int min;
	private TextView title;
	private TextView value;
	private FillBar bar;

	public FillBarWithText(Context context, AttributeSet attrs) {
		super(context, attrs);

		setOrientation(VERTICAL);

		inflate(context, R.layout.subview_fillbar_with_text, this);

		title = (TextView) findViewById(R.id.textViewBarTitle);
		value = (TextView) findViewById(R.id.TextViewBarValue);
		bar = (FillBar) findViewById(R.id.fillBarSubview);
	}

	public void setup(String title, int max, int min) {
		this.max = max;
		this.min = min;
		this.title.setText(title);
	}

	public void setValue(int value) {
		this.value.setText(Integer.toString(value));
		this.bar.setPercentage((value - min) / ((float)(max - min)));
	}

}
