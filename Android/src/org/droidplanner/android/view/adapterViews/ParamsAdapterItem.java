package org.droidplanner.android.view.adapterViews;

import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.drone.property.Parameter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Map;

/**
 * Date: 2013-12-09 Time: 1:32 AM
 */
public class ParamsAdapterItem implements Parcelable {
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
            Parameter copy = new Parameter(parameter.getName(), dval, parameter.getType());
            copy.setDescription(parameter.getDescription());
            copy.setUnits(parameter.getUnits());
            copy.setDisplayName(parameter.getDisplayName());
            copy.setRange(parameter.getRange());
            copy.setValues(parameter.getValues());
			return copy;

		} catch (ParseException e) {
			return parameter;
		}
	}

	public void setDirtyValue(String value) {
		
		setDirtyValue(value, false);
	}

    public void setDirtyValue(String value, boolean force){
        if(force){
            dirtyValue = value;
        }
        else{
            // dirty if different from original value, set validation if dirty
            dirtyValue = (parameter.getDisplayValue().equals(value)) ? null : value;
        }

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.parameter, 0);
        dest.writeString(this.dirtyValue);
        dest.writeInt(this.validation == null ? -1 : this.validation.ordinal());
    }

    private ParamsAdapterItem(Parcel in) {
        this.parameter = in.readParcelable(Parameter.class.getClassLoader());
        this.dirtyValue = in.readString();
        int tmpValidation = in.readInt();
        this.validation = tmpValidation == -1 ? null : Validation.values()[tmpValidation];
    }

    public static final Creator<ParamsAdapterItem> CREATOR = new Creator<ParamsAdapterItem>() {
        public ParamsAdapterItem createFromParcel(Parcel source) {
            return new ParamsAdapterItem(source);
        }

        public ParamsAdapterItem[] newArray(int size) {
            return new ParamsAdapterItem[size];
        }
    };
}
