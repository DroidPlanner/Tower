package com.droidplanner.adapters;

import com.droidplanner.parameters.Parameter;
import com.droidplanner.parameters.ParameterMetadata;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Map;

/**
* Date: 2013-12-09
* Time: 1:32 AM
*/
public class ParamsAdapterItem implements Serializable {
    public enum Validation { NA, INVALID, VALID }

    private final Parameter parameter;
    private final ParameterMetadata metadata;

    private String dirtyValue;
    private Validation validation;


    public ParamsAdapterItem(Parameter parameter, ParameterMetadata metadata) {
        this.parameter = parameter;
        this.metadata = metadata;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public ParameterMetadata getMetadata() {
        return metadata;
    }

    public void setDirtyValue(String value) {
        // dirty if different from original value, set validation if dirty
        dirtyValue = (parameter.getValue().equals(value)) ? null : value;
        if(dirtyValue != null)
            validation = validateValue(dirtyValue);
    }

    public String getValue() {
        return (dirtyValue != null) ? dirtyValue : parameter.getValue();
    }

    public boolean isDirty() {
        return dirtyValue != null;
    }

    public Validation getValidation() {
        return validation;
    }


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
}
