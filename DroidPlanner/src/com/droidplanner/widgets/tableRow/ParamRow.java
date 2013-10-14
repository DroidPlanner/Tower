package com.droidplanner.widgets.tableRow;

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

import com.droidplanner.drone.variables.Parameters;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterMetadata;

public class ParamRow extends TableRow implements TextWatcher {
	private TextView nameView;
    private TextView displayNameView;
	private EditText valueView;
	private Parameter param;

	public ParamRow(Context context) {
		super(context);
		createRowViews(context);
	}

	public ParamRow(Context context, AttributeSet attrs) {
		super(context, attrs);
		createRowViews(context);
	}

	public void setParam(Parameter param, Parameters parameters) {
		this.param = param;
		nameView.setText(param.name);
		valueView.setText(param.getValue());

        setParamMetadata(parameters);
    }

    public void setParamMetadata(Parameters parameters) {
        ParameterMetadata metadata = parameters.getMetadata(param.name);
        if(metadata != null) {
            // display-name (units)
            String displayName = metadata.getDisplayName();
            if(metadata.getUnits() != null)
                displayName += " (" + metadata.getUnits() + ")";
            displayNameView.setText(displayName);
        } else {
            displayNameView.setText("");
        }
    }

    private void createRowViews(Context context) {
        // name
        nameView = new TextView(context);
        nameView.setWidth(300);
        addView(nameView);

        // display
        displayNameView = new TextView(context);
        displayNameView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, (float) 1.0));
        displayNameView.setGravity(Gravity.CENTER_VERTICAL);
        addView(displayNameView);

        // value
		valueView = new EditText(context);
		valueView.setInputType(InputType.TYPE_CLASS_NUMBER);
		valueView.setWidth(220);
		valueView.setGravity(Gravity.RIGHT);
		addView(valueView);
		valueView.addTextChangedListener(this);
    }

	public Parameter getParameterFromRow() {
		return (new Parameter(param.name, getParamValue(), param.type));
	}

	public double getParamValue() {
		return Double.parseDouble(valueView.getText().toString());
	}

	public String getParamName() {
		return param.name;
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (isNewValueEqualToDroneParam()) {
			valueView.setTextColor(Color.WHITE);
		} else {
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
