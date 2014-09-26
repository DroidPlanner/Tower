package org.droidplanner.android.widgets.adapterViews;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Map;

import org.droidplanner.core.parameters.Parameter;
import org.droidplanner.core.parameters.ParameterMetadata;

/**
 * Date: 2013-12-09 Time: 1:32 AM
 */
public class ParamsAdapterItem implements Serializable {
	public enum Validation {
		NA, INVALID, VALID
	}

	private static final DecimalFormat formatter = Parameter.getFormat();

	private final Parameter parameter;
	private ParameterMetadata metadata;

	private String dirtyValue;
	private Validation validation;

	public ParamsAdapterItem(Parameter parameter, ParameterMetadata metadata) {
		this.parameter = parameter;
		this.metadata = metadata;
	}

	public Parameter getParameter() {
		if (dirtyValue == null)
			return parameter;

		try {
			final double dval = formatter.parse(dirtyValue).doubleValue();
			return new Parameter(parameter.name, dval, parameter.type);

		} catch (ParseException e) {
			return parameter;
		}
	}

	public ParameterMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(ParameterMetadata metadata) {
		this.metadata = metadata;
	}

	public void setDirtyValue(String value) {
		// dirty if different from original value, set validation if dirty
		dirtyValue = (parameter.getValue().equals(value)) ? null : value;
		if (dirtyValue != null)
			validation = validateValue(dirtyValue);
	}

	public void commit() {
		try {
			parameter.value = formatter.parse(dirtyValue).doubleValue();
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
		if (metadata == null) {
			return Validation.NA;

		} else if (metadata.getRange() != null) {
			return validateInRange(value);

		} else if (metadata.getValues() != null) {
			return validateInValues(value);

		} else {
			return Validation.NA;
		}
	}

	private Validation validateInRange(String value) {
		try {
			final double dval = formatter.parse(value).doubleValue();
			final double[] range = metadata.parseRange();
			return (dval >= range[ParameterMetadata.RANGE_LOW] && dval <= range[ParameterMetadata.RANGE_HIGH]) ? Validation.VALID
					: Validation.INVALID;
		} catch (ParseException ex) {
			return Validation.NA;
		}
	}

	private Validation validateInValues(String value) {
		try {
			final double dval = formatter.parse(value).doubleValue();
			final Map<Double, String> values = metadata.parseValues();
			if (values.keySet().contains(dval)) {
				return Validation.VALID;
			} else {
				return Validation.INVALID;
			}
		} catch (ParseException ex) {
			return Validation.NA;
		}
	}
}
