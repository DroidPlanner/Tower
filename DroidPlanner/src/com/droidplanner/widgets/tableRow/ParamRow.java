package com.droidplanner.widgets.tableRow;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.droidplanner.R;
import com.droidplanner.dialogs.parameters.DialogParameterInfo;
import com.droidplanner.drone.variables.Parameters;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterMetadata;

public class ParamRow extends TableRow implements
        TextWatcher, View.OnClickListener {
	private TextView nameView;
    private TextView displayNameView;
	private EditText valueView;
	private Parameter param;
    private ParameterMetadata metadata;

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
        metadata = parameters.getMetadata(param.name);

        String displayNameViewText = "";
        if(metadata != null) {
            // display-name (units)
            displayNameViewText = metadata.getDisplayName();
            if(metadata.getUnits() != null)
                displayNameViewText += " (" + metadata.getUnits() + ")";
        }
        displayNameView.setText(displayNameViewText);
    }

    private void createRowViews(Context context) {
        // name
        nameView = new TextView(context);
        nameView.setWidth(300);
        nameView.setOnClickListener(this);
        addView(nameView);

        // display
        displayNameView = new TextView(context);
        displayNameView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, (float) 1.0));
        displayNameView.setGravity(Gravity.CENTER_VERTICAL);
        displayNameView.setOnClickListener(this);
        addView(displayNameView);

        // value
		valueView = new EditText(context);
		valueView.setInputType(InputType.TYPE_CLASS_NUMBER);
		valueView.setWidth(220);
		valueView.setGravity(Gravity.RIGHT);
        valueView.addTextChangedListener(this);
        addView(valueView);
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

    @Override
    public void onClick(View view) {
        if(metadata == null || !metadata.hasInfo())
            return;

        DialogParameterInfo.build(metadata, getContext()).show();
    }
}
