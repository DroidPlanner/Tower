package org.droidplanner.widgets.SeekBarWithText;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.droidplanner.R;

public class SeekBarWithText extends LinearLayout implements
		OnSeekBarChangeListener {

	public interface OnTextSeekBarChangedListener {
		public void onSeekBarChanged();
	}

	private TextView textView;
	private SeekBar seekBar;
	private double min = 0;
	private double inc = 1;
	private String title = "";
	private String unit = "";
	private String formatString = "%2.1f";
	private OnTextSeekBarChangedListener listener;

	public SeekBarWithText(Context context) {
		super(context);
		createViews(context);
	}

	public SeekBarWithText(Context context, AttributeSet attrs) {
		super(context, attrs);
		createViews(context);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.SeekBarWithText, 0, 0);

		try {
			setTitle(a.getString(R.styleable.SeekBarWithText_title));
			setUnit(a.getString(R.styleable.SeekBarWithText_unit));
			setMinMaxInc(a.getFloat(R.styleable.SeekBarWithText_min, 0),
					a.getFloat(R.styleable.SeekBarWithText_max, 100),
					a.getFloat(R.styleable.SeekBarWithText_inc, 1));
			setFormat(a.getString(R.styleable.SeekBarWithText_formatString));
		} finally {
			a.recycle();
		}
	}

	private void setFormat(String string) {
		if (string!=null) {
			formatString = string;
		}
	}

	private void createViews(Context context) {
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		setOrientation(VERTICAL);
		textView = new TextView(context);
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
		if (unit != null) {
			this.unit = unit;
		}
	}

	public void setTitle(CharSequence text) {
		if (text != null) {
			title = text.toString();
			updateTitle();
		}
	}

	private void updateTitle() {
		textView.setText(String.format("%s\t"+formatString+" %s", title, getValue(), unit));
	}

	public double getValue() {
		return (seekBar.getProgress() * inc + min);
	}

	public void setValue(double value) {
		seekBar.setProgress((int) ((value - min) / inc));
	}

	public void setAbsValue(double value) {
		if(value<0)
			value *= -1.0;
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
		if (listener != null) {
			listener.onSeekBarChanged();
		}
	}

	public void setOnChangedListener(OnTextSeekBarChangedListener listener) {
		this.listener = listener;
	}

}
