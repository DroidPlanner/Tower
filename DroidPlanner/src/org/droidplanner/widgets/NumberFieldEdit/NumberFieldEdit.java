package org.droidplanner.widgets.NumberFieldEdit;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.droidplanner.R;

public class NumberFieldEdit extends LinearLayout {

	private TextView textView;
	private EditText editText;
	private LinearLayout buttonLayout;
	private ImageButton buttonPlus;
	private ImageButton buttonMinus;
	private double min = 0;
	private double inc = 1;
	private double max = 100;
	private double value = 0.0;
	private String title = "Title";
	private String unit = "";
	private String formatString = "%2.1f";

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
					a.getFloat(R.styleable.NumberFieldEdit_Inc, 1));
			setFormat(a.getString(R.styleable.NumberFieldEdit_Format));
			setValue(this.value);
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
		setOrientation(HORIZONTAL);
		
		textView = new TextView(context);
		editText = new EditText(context);
		buttonPlus = new ImageButton(context);
		buttonMinus = new ImageButton(context);
		buttonLayout = new LinearLayout(context);

		textView.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
		editText.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
		
		textView.setTextSize(16);
		editText.setTextSize(16);
		
		textView.setLayoutParams(new LayoutParams(0,LayoutParams.MATCH_PARENT,5));
		editText.setLayoutParams(new LayoutParams(0,LayoutParams.MATCH_PARENT,5));
		buttonLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT,2));
		
		editText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
		editText.clearFocus();
		
		LayoutParams p =new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		p.setMargins(5, 0, 0, 0);

		buttonPlus.setBackgroundResource(R.drawable.button_mode_options);
		buttonPlus.setImageResource(R.drawable.ic_plus);
		buttonPlus.setLayoutParams(p);

		
		buttonMinus.setBackgroundResource(R.drawable.button_mode_options);
		buttonMinus.setImageResource(R.drawable.ic_minus);
		buttonMinus.setLayoutParams(p);
		
		addView(textView);
		addView(editText);
		addView(buttonPlus);
		addView(buttonMinus);
	}

	public void setMinMaxInc(double min, double max, double inc) {
		this.min = min;
		this.inc = inc;
		this.max = max;
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

	private void updateTitle() {
		String t = title;
		t+= unit.isEmpty()?"":String.format(" (%s)",unit);
		textView.setText(t);
	}

	public double getValue() {
		return Double.valueOf(editText.getEditableText().toString());
	}

	public void setValue(double value) {
		this.value = value<min?min:value;
		this.value = value>max?max:value;
		editText.setText(String.format(formatString, value));
	}

	public void setAbsValue(double value) {
		if(value<0)
			value *= -1.0;
		editText.setText(String.format(formatString, value));
	}
}
