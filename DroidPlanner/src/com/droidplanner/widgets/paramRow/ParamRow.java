package com.droidplanner.widgets.paramRow;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.droidplanner.MAVLink.parameters.Parameter;

public class ParamRow extends TableRow implements TextWatcher {
	private TextView nameView;
	private EditText valueView;
	private TextView typeView;
	private TextView indexView;
	private Parameter param;

	public ParamRow(Context context) {
		super(context);
		createRowViews(context);
	}

	public ParamRow(Context context, AttributeSet attrs) {
		super(context, attrs);
		createRowViews(context);
	}

	public void setParam(Parameter param) {
		this.param = param;
		nameView.setText(param.name);
		typeView.setText(Integer.toString(param.type));
		indexView.setText(Integer.toString(param.index));
		valueView.setText(param.getValue());
	}

	private void createRowViews(Context context) {
		nameView = new TextView(context);
		valueView = new EditText(context);
		typeView = new TextView(context);
		indexView = new TextView(context);

		valueView.setInputType(InputType.TYPE_CLASS_NUMBER);

		typeView.setGravity(Gravity.RIGHT);

		indexView.setWidth(50);
		nameView.setWidth(150);
		valueView.setWidth(100);
		typeView.setWidth(50);

		addView(indexView);
		addView(nameView);
		addView(valueView);
		addView(typeView);

		valueView.addTextChangedListener(this);
	}

	public Parameter getParameterFromRow(){
		return (new Parameter(param.name, getParamValue(), param.type, param.index));
	}

	public double getParamValue() {
		return Double.parseDouble(valueView.getText().toString());
	}
	
	public String getParamName(){
		return param.name;
	}
	
	@Override
	public void afterTextChanged(Editable s) {			
		if (isNewValueEqualToDroneParam()) {
			valueView.setTextColor(Color.WHITE);
		}else{			
			valueView.setTextColor(Color.RED);
		}
	}

	public boolean isNewValueEqualToDroneParam() {
		return param.getValue().equals(valueView.getText().toString());
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {		
	}

}
