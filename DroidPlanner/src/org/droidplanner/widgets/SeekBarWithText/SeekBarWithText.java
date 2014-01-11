package org.droidplanner.widgets.SeekBarWithText;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;

import org.droidplanner.R;

public class SeekBarWithText extends LinearLayout implements
		OnSeekBarChangeListener, OnEditorActionListener, OnFocusChangeListener {

	public interface OnTextSeekBarChangedListner {
		public void onSeekBarChanged();
	}

	private TextView textView;
	private SeekBar seekBar;
	private EditText editText;
	private LinearLayout innerLayout;
	private double min = 0;
	private double inc = 1;
	private String title = "";
	private String unit = "";
	private String formatString = "%2.1f";
	private OnTextSeekBarChangedListner listner;

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
		if (string != null) {
			formatString = string;
		}
	}

	private void createViews(Context context) {
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		setOrientation(HORIZONTAL);

		innerLayout = new LinearLayout(context);
		innerLayout.setLayoutParams(new LayoutParams(0,
				LayoutParams.MATCH_PARENT, 9));
		innerLayout.setOrientation(VERTICAL);
		innerLayout.setFocusable(true);
		innerLayout.setFocusableInTouchMode(true);

		textView = new TextView(context);
		editText = new EditText(context);
		editText.setLayoutParams(new LayoutParams(80, LayoutParams.MATCH_PARENT));
		editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL
				| InputType.TYPE_CLASS_NUMBER);
		editText.setOnEditorActionListener(this);

		seekBar = new SeekBar(context);
		seekBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		seekBar.setOnSeekBarChangeListener(this);

		innerLayout.addView(textView);
		innerLayout.addView(seekBar);
		addView(innerLayout);
		addView(editText);

		editText.clearFocus();
		innerLayout.requestFocus();
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
		textView.setText(String.format("%s\t" + formatString + " %s", title,
				getValue(), unit));
		editText.setText(String.format(formatString, (getValue())));
	}

	public double getValue() {
		return (seekBar.getProgress() * inc + min);
	}

	public void setValue(double value) {
		seekBar.setProgress((int) ((value - min) / inc));
		// editText.setText(String.valueOf(getValue()));
	}

	public void setAbsValue(double value) {
		if (value < 0)
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
		if (listner != null) {
			listner.onSeekBarChanged();
		}
	}

	public void setOnChangedListner(OnTextSeekBarChangedListner listner) {
		this.listner = listner;
	}

	private void getValueFromEditText() {
		double val;
		try {
			val = Double.parseDouble(editText.getText().toString());
		} catch (NumberFormatException e) {
			val = -1;
			e.printStackTrace();
		}
		val = val > seekBar.getMax() ? seekBar.getMax() : val;
		val = val < min ? min : val;
		setValue(val);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (v.equals(editText)) {
			if (actionId == EditorInfo.IME_ACTION_DONE
					|| actionId == EditorInfo.IME_ACTION_NEXT) {
				getValueFromEditText();
			}
		}
		return false;
	}

	@Override
	public void onFocusChange(View v, boolean isFocused) {
		if (v.equals(editText)&& isFocused!=true) {
			getValueFromEditText();
		}
	}

}
