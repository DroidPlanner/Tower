package com.droidplanner.widgets.tableRow;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.droidplanner.dialogs.parameters.DialogParameterInfo;
import com.droidplanner.drone.variables.Parameters;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterMetadata;

import java.text.DecimalFormat;
import java.text.ParseException;

public class ParamRow extends TableRow implements
        TextWatcher, View.OnClickListener, View.OnFocusChangeListener {
	private TextView nameView;
    private TextView displayNameView;
	private EditText valueView;
	private Parameter param;
    private ParameterMetadata metadata;

    private final static DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
    static { format.applyPattern(Parameter.DECIMAL_PATTERN); }

    private enum Validation { NA, INVALID, VALID }


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
		valueView.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL |
                InputType.TYPE_NUMBER_FLAG_SIGNED);
		valueView.setWidth(220);
		valueView.setGravity(Gravity.RIGHT);
        valueView.addTextChangedListener(this);
        valueView.setOnFocusChangeListener(this);
        addView(valueView);
    }

	public Parameter getParameterFromRow() {
		return (new Parameter(param.name, getParamValue(), param.type));
	}

	public double getParamValue() {
        try {
            return format.parse(valueView.getText().toString()).doubleValue();
        } catch (ParseException ex) {
            throw new NumberFormatException(ex.getMessage());
        }
    }

	public String getParamName() {
		return param.name;
	}

	@Override
	public void afterTextChanged(Editable s) {
        final String newValue = valueView.getText().toString();

        final int color;
        if(isValueEqualToDroneParam(newValue)) {
            color = Color.WHITE;
        } else {
            final Validation validation = validateValue(newValue);
            if (validation == Validation.VALID) {
                color = Color.GREEN;
            } else if (validation == Validation.INVALID) {
                color = Color.RED;
            } else {
                color = Color.YELLOW;
            }
        }
        valueView.setTextColor(color);
    }

	public boolean isNewValueEqualToDroneParam() {
		return isValueEqualToDroneParam(valueView.getText().toString());
	}

    private boolean isValueEqualToDroneParam(String value) {
        return param.getValue().equals(value);
    }

    /*
     * Return TRUE if valid or unable to validate
     */
    private Validation validateValue(String value) {
        if(metadata == null) {
            return Validation.NA;

        } else if(metadata.getRange() != null) {
            return validateInRange(value);

        } else {
            return Validation.NA;
        }
    }

    private Validation validateInRange(String value) {
        final String part[] = metadata.getRange().split(" ");
        if(part.length != 2)
            return Validation.NA;

        try {
            final double dval = format.parse(value).doubleValue();
            final double low = format.parse(part[0]).doubleValue();
            final double high = format.parse(part[1]).doubleValue();
            return (dval >= low && dval <= high) ? Validation.VALID : Validation.INVALID;
        } catch (ParseException e) {
            return Validation.NA;
        }
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

    @Override
    public void onFocusChange(View view, boolean b) {
        valueView.setText(format.format(getParamValue()));
    }
}
