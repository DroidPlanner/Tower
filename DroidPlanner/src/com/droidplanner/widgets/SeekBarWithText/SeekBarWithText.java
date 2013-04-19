package com.droidplanner.widgets.SeekBarWithText;

import android.R.attr;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarWithText extends LinearLayout implements
		OnSeekBarChangeListener {

	private TextView textView;
	private SeekBar seekBar;
	private double min = 0;
	private double inc = 1;
	private String title = "";
	private String unit = "";

	public SeekBarWithText(Context context) {
		super(context);
		createViews(context);
	}

	public SeekBarWithText(Context context, AttributeSet attrs) {
		super(context, attrs);
		createViews(context);
	}

	public SeekBarWithText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		createViews(context);
	}

	private void createViews(Context context) {
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		setOrientation(VERTICAL);
		textView = new TextView(context);
		textView.setTextAppearance(context, attr.textAppearanceMedium);
		seekBar = new SeekBar(context);
		seekBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		seekBar.setOnSeekBarChangeListener(this);
		addView(textView);
		addView(seekBar);
	}

	public void setMinMaxInc(double min, double max, double inc) {
		this.min = min;
		this.inc = inc;
		seekBar.setMax((int) ((max - min) / inc));
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public void setTitle(CharSequence text) {
		title = text.toString();
		updateTitle();
	}

	private void updateTitle() {
		textView.setText(String.format("%s\t%2.1f %s", title, getValue(), unit));
	}

	public double getValue() {
		return (seekBar.getProgress() * inc + min);
	}

	public void setValue(double value) {
		seekBar.setProgress((int) ((value - min) / inc));
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		updateTitle();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

}
