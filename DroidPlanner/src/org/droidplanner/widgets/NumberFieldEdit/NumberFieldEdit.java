package org.droidplanner.widgets.NumberFieldEdit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.droidplanner.R;

public class NumberFieldEdit extends LinearLayout implements OnTouchListener,OnEditorActionListener {

	private TextView titleText;
	private TextView separatorText;
	private EditText editText;
	private LinearLayout buttonLayout;
	private ImageButton buttonPlus;
	private ImageButton buttonMinus;
	private double min = 0;
	private double inc = 1;
	private double max = 100;
	private double value = 0.0;
	private double fastInc = 5.0;
	private String title = "Title";
	private String unit = "";
	private String separator = ";";
	private String formatString = "%2.1f";
	private short delay = 1000;
	private boolean countUp, fastCount;
	private boolean waitingForLongPress = false;
	private int startX, startY;

	public NumberFieldEdit(Context context) {
		super(context);
		createViews(context);
	}

	public NumberFieldEdit(Context context, AttributeSet attrs) {
		super(context, attrs);
		createViews(context);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.NumberFieldEdit, 0, 0);

		try {
			setTitle(a.getString(R.styleable.NumberFieldEdit_Title));
			setUnit(a.getString(R.styleable.NumberFieldEdit_Unit));
			setMinMaxInc(a.getFloat(R.styleable.NumberFieldEdit_Min, 0),
					a.getFloat(R.styleable.NumberFieldEdit_Max, 100),
					a.getFloat(R.styleable.NumberFieldEdit_Inc, 1),
					a.getFloat(R.styleable.NumberFieldEdit_FastInc, 2));
			setFormat(a.getString(R.styleable.NumberFieldEdit_Format));
			setSeparator(a.getString(R.styleable.NumberFieldEdit_Separator));

			
			if (a.hasValue(R.styleable.NumberFieldEdit_TextAppearance)) {
				int at = a.getResourceId(R.styleable.NumberFieldEdit_TextAppearance, android.R.style.TextAppearance);
				setTextAppearance(context,at);
			}
			setTextColor(a.getColor(R.styleable.NumberFieldEdit_TextColor,
					Color.BLACK));
			
			setValue(this.value);
		} finally {
			a.recycle();
		}
	}
	private void setTextAppearance(Context context, int data) {
		titleText.setTextAppearance(context, data);
		separatorText.setTextAppearance(context, data);
		titleText.setTextAppearance(context, data);
		editText.setTextAppearance(context, data);
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

		titleText = new TextView(context);
		separatorText = new TextView(context);
		editText = new EditText(context);
		buttonPlus = new ImageButton(context);
		buttonMinus = new ImageButton(context);
		buttonLayout = new LinearLayout(context);

		titleText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		separatorText.setGravity(Gravity.CENTER_HORIZONTAL
				| Gravity.CENTER_VERTICAL);
		editText.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

		titleText.setTextSize(16);
		separatorText.setTextSize(16);
		editText.setTextSize(16);

		separatorText.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		titleText.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT));
		editText.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT,10));
		buttonLayout.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		buttonLayout.setFocusable(true);
		buttonLayout.setFocusableInTouchMode(true);
		
		titleText.setMinEms(3);
		editText.setMinEms(3);
		
		editText.setMinWidth(80);
		editText.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_FLAG_DECIMAL);
		editText.setOnEditorActionListener(this);
		editText.clearFocus();

		LayoutParams p = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		p.setMargins(5, 0, 0, 0);

		buttonPlus.setBackgroundResource(R.drawable.button_mode_options);
		buttonPlus.setImageResource(R.drawable.ic_plus);
		buttonPlus.setLayoutParams(p);

		buttonMinus.setBackgroundResource(R.drawable.button_mode_options);
		buttonMinus.setImageResource(R.drawable.ic_minus);
		buttonMinus.setLayoutParams(p);

		buttonPlus.setOnTouchListener(this);
		buttonMinus.setOnTouchListener(this);

		buttonLayout.addView(buttonPlus);
		buttonLayout.addView(buttonMinus);

		addView(titleText);
		addView(separatorText);
		addView(editText);
		addView(buttonLayout);

		editText.clearFocus();
		buttonLayout.requestFocus();
	}

	private void setTextColor(int color) {
		if (color != 0) {
			titleText.setTextColor(color);
			separatorText.setTextColor(color);
			editText.setTextColor(color);
		}
	}

	public void setMinMaxInc(double min, double max, double inc, float fastinc) {
		this.min = min;
		this.inc = inc;
		this.max = max;
		this.fastInc = fastinc;
	}

	public void setUnit(String unit) {
		if (unit != null) {
			this.unit = unit;
			updateTitle();
		}
	}

	public void setTitle(CharSequence text) {
		if (text != null) {
			this.title = (String) text;
			updateTitle();
		}
	}

	public void setSeparator(CharSequence text) {
		if (text != null) {
			this.separator = (String) text;
			updateSeparator();
		}
	}

	private void updateSeparator() {
		String t = separator;
		if (t.isEmpty()) {
			separatorText.setVisibility(GONE);
		} else {

			separatorText.setVisibility(VISIBLE);
			separatorText.setText(separator);
		}
	}

	private void updateTitle() {
		String t = title;
		t += unit.isEmpty() ? "" : String.format(" (%s)", unit);
		titleText.setText(t);
	}

	public double getValue() {
		return Double.valueOf(editText.getEditableText().toString());
	}

	public void setValue(double value) {
		if (value >= max)
			value = max;
		else if (value <= min)
			value = min;

		this.value = value;
		editText.setText(String.format(formatString, this.value));
	}

	public void setAbsValue(double value) {
		if (value < 0)
			value *= -1.0;

		this.value = value;
		editText.setText(String.format(formatString, this.value));
	}

	private void updateValue() {
		double incVal = fastCount ? fastInc : inc;
		if (countUp)
			setValue(this.value + incVal);
		else
			setValue(this.value - incVal);
	}

	final Handler handler = new Handler();
	Runnable mLongPressed = new Runnable() {
		public void run() {
			delay = 50;
			updateValue();
			handler.postDelayed(this, delay);
		}
	};

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.equals(buttonMinus) || v.equals(buttonPlus)) {
			countUp = v.equals(buttonPlus);

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (!waitingForLongPress) {
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					waitingForLongPress = true;
					handler.postDelayed(mLongPressed, delay);
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				waitingForLongPress = false;
				fastCount = false;
				handler.removeCallbacks(mLongPressed);
				delay = 1000;
				updateValue();
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				fastCount = (Math.abs(event.getRawX() - startX) > 20)
						|| (Math.abs(event.getRawY() - startY) > 2);
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(v.equals(editText)){
			double tmp = 0;
			
			try {
				tmp = Double.valueOf(editText.getText().toString());
			} catch (NumberFormatException e) {
				tmp = 0;
				e.printStackTrace();
			}
			setValue(tmp);
		}
		return false;
	}

}
