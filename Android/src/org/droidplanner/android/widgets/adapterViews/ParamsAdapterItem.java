package org.droidplanner.android.widgets.adapterViews;

import com.ox3dr.services.android.lib.drone.property.Parameter;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Map;

/**
 * Date: 2013-12-09 Time: 1:32 AM
 */
public class ParamsAdapterItem implements Serializable {
	public enum Validation {
		NA, INVALID, VALID
	}

    private final static DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance();
    static {
        formatter.applyPattern("0.###");
    }

	private Parameter parameter;

	private String dirtyValue;
	private Validation validation;

	public ParamsAdapterItem(Parameter parameter) {
		this.parameter = parameter;
	}

	public Parameter getParameter() {
		if (dirtyValue == null)
			return parameter;

		try {
			final double dval = formatter.parse(dirtyValue).doubleValue();
			return new Parameter(parameter.getName(), dval, parameter.getType());

		} catch (ParseException e) {
			return parameter;
		}
	}

	public void setDirtyValue(String value) {
		// dirty if different from original value, set validation if dirty
		dirtyValue = (Double.toString(parameter.getValue()).equals(value)) ? null : value;
		if (dirtyValue != null)
			validation = validateValue(dirtyValue);
	}

	public void commit() {
		try {
			parameter.setValue(formatter.parse(dirtyValue).doubleValue());
			dirtyValue = null;
		} catch (ParseException e) {
			// nop
		}
	}

	public boolean isDirty() {
		return dirtyValue != null;
	}

	public Validation getValidation() {
		return validation;
	}

	private Validation validateValue(String value) {
		if (parameter.getRange() != null) {
			return validateInRange(value);

		} else if (parameter.getValues() != null) {
			return validateInValues(value);

		} else {
			return Validation.NA;
		}
	}

	private Validation validateInRange(String value) {
		try {
			final double dval = formatter.parse(value).doubleValue();
			final double[] range = parameter.parseRange();
			return (dval >= range[Parameter.RANGE_LOW] && dval <= range[Parameter.RANGE_HIGH])
                    ? Validation.VALID
					: Validation.INVALID;
		} catch (ParseException ex) {
			return Validation.NA;
		}
	}

	private Validation validateInValues(String value) {
		try {
			final double dval = formatter.parse(value).doubleValue();
			final Map<Double, String> values = parameter.parseValues();
			if (values.keySet().contains(dval)) {
				return Validation.VALID;
			} else {
				return Validation.INVALID;
			}
		} catch (ParseException ex) {
			return Validation.NA;
		}
	}

    @Override
    public String toString(){
        String toString = "";

        final Parameter param = getParameter();
        if (param != null) {
            toString = param.getName() + ": ";
            toString += parameter.getDisplayName();
        }

        return toString;
    }
}
