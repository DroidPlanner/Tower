package com.ox3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by fhuya on 10/28/14.
 */
public class Parameter implements Parcelable {

    public static final int RANGE_LOW = 0;
    public static final int RANGE_HIGH = 1;

    private final static DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance();
    static {
        formatter.applyPattern("0.###");
    }

    private String name;
    private double value;
    private int type;

    private String displayName;
    private String description;

    private String units;
    private String range;
    private String values;

    public Parameter(String name, double value, int type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value){
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type){
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public boolean hasInfo() {
        return (description != null && !description.isEmpty())
                || (values != null && !values.isEmpty());
    }

    public static void checkParameterName(String name) throws Exception {
        if (name.equals("SYSID_SW_MREV")) {
            throw new Exception("ExludedName");
        } else if (name.contains("WP_TOTAL")) {
            throw new Exception("ExludedName");
        } else if (name.contains("CMD_TOTAL")) {
            throw new Exception("ExludedName");
        } else if (name.contains("FENCE_TOTAL")) {
            throw new Exception("ExludedName");
        } else if (name.contains("SYS_NUM_RESETS")) {
            throw new Exception("ExludedName");
        } else if (name.contains("ARSPD_OFFSET")) {
            throw new Exception("ExludedName");
        } else if (name.contains("GND_ABS_PRESS")) {
            throw new Exception("ExludedName");
        } else if (name.contains("GND_TEMP")) {
            throw new Exception("ExludedName");
        } else if (name.contains("CMD_INDEX")) {
            throw new Exception("ExludedName");
        } else if (name.contains("LOG_LASTFILE")) {
            throw new Exception("ExludedName");
        } else if (name.contains("FORMAT_VERSION")) {
            throw new Exception("ExludedName");
        }
    }

    public double[] parseRange() throws ParseException {
        final DecimalFormat format = formatter;

        final String[] parts = this.range.split(" ");
        if (parts.length != 2) {
            throw new IllegalArgumentException();
        }

        final double[] outRange = new double[2];
        outRange[RANGE_LOW] = format.parse(parts[RANGE_LOW]).doubleValue();
        outRange[RANGE_HIGH] = format.parse(parts[RANGE_HIGH]).doubleValue();

        return outRange;
    }

    public Map<Double, String> parseValues() throws ParseException {
        final DecimalFormat format = formatter;

        final Map<Double, String> outValues = new LinkedHashMap<Double, String>();
        if (values != null) {
            final String[] tparts = this.values.split(",");
            for (String tpart : tparts) {
                final String[] parts = tpart.split(":");
                if (parts.length != 2)
                    throw new IllegalArgumentException();
                outValues.put(format.parse(parts[0].trim()).doubleValue(), parts[1].trim());
            }
        }
        return outValues;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeDouble(this.value);
        dest.writeInt(this.type);
        dest.writeString(this.displayName);
        dest.writeString(this.description);
        dest.writeString(this.units);
        dest.writeString(this.range);
        dest.writeString(this.values);
    }

    private Parameter(Parcel in) {
        this.name = in.readString();
        this.value = in.readDouble();
        this.type = in.readInt();
        this.displayName = in.readString();
        this.description = in.readString();
        this.units = in.readString();
        this.range = in.readString();
        this.values = in.readString();
    }

    public static final Creator<Parameter> CREATOR = new Creator<Parameter>() {
        public Parameter createFromParcel(Parcel source) {
            return new Parameter(source);
        }

        public Parameter[] newArray(int size) {
            return new Parameter[size];
        }
    };
}
