package com.droidplanner.widgets.adapterViews;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.droidplanner.R;
import com.droidplanner.dialogs.parameters.DialogParameterInfo;
import com.droidplanner.dialogs.parameters.DialogParameterValues;
import com.droidplanner.drone.variables.Parameters;
import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterMetadata;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParamRow extends TableRow implements
        TextWatcher, View.OnClickListener, View.OnFocusChangeListener {
	private TextView nameView;
    private TextView displayNameView;
	private EditText valueView;
	private Parameter param;
    private ParameterMetadata metadata;

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
            return Parameter.getFormat().parse(valueView.getText().toString()).doubleValue();
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
            valueView.setTypeface(null, Typeface.NORMAL);
        } else {
            final Validation validation = validateValue(newValue);
            if (validation == Validation.VALID) {
                color = Color.GREEN;
            } else if (validation == Validation.INVALID) {
                color = Color.RED;
            } else {
                color = Color.YELLOW;
            }
            valueView.setTypeface(null, Typeface.BOLD);
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

        } else if(metadata.getValues() != null) {
            return validateInValues(value);

        } else {
            return Validation.NA;
        }
    }

    private Validation validateInRange(String value) {
        try {
            final double dval = Parameter.getFormat().parse(value).doubleValue();
            final double[] range = metadata.parseRange();
            return (dval >= range[ParameterMetadata.RANGE_LOW] && dval <= range[ParameterMetadata.RANGE_HIGH]) ?
                    Validation.VALID : Validation.INVALID;
        } catch (ParseException ex) {
            return Validation.NA;
        }
    }

    private Validation validateInValues(String value) {
        try {
            final double dval = Parameter.getFormat().parse(value).doubleValue();
            final Map<Double, String> values = metadata.parseValues();
            if (values.keySet().contains(dval)) {
                return Validation.VALID;
            }
            else {
                return Validation.INVALID;
            }
        } catch (ParseException ex) {
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
    public void onFocusChange(View view, boolean hasFocus) {
        if(!hasFocus) {
            // refresh value on leaving view - show results of rounding etc.
            valueView.setText(Parameter.getFormat().format(getParamValue()));
        }
    }

    @Override
    public void onClick(View view) {
        if(metadata == null || !metadata.hasInfo())
            return;

        final AlertDialog.Builder builder = DialogParameterInfo.build(metadata, getContext());

        // add edit button if metadata supplies known values
        if(metadata.getValues() != null)
            addEditValuesButton(builder);

        builder.show();
    }

    private AlertDialog.Builder addEditValuesButton(AlertDialog.Builder builder) {
        return builder.setPositiveButton(R.string.parameter_row_edit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DialogParameterValues.build(param.name, metadata, valueView.getText().toString(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        try {
                            final List<Double> values = new ArrayList<Double>(metadata.parseValues().keySet());
                            valueView.setText(Parameter.getFormat().format(values.get(which)));
                            dialogInterface.dismiss();
                        } catch (ParseException ex) {
                            // nop
                        }
                    }
                }, getContext()).show();
            }
        });
    }
}
