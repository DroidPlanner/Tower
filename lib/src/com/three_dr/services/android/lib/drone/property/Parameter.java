package com.three_dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fhuya on 10/28/14.
 */
public class Parameter implements Parcelable {

    private final String name;
    private final double value;
    private final int type;

    public Parameter(String name, double value, int type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public int getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parameter)) return false;

        Parameter parameter = (Parameter) o;

        if (type != parameter.type) return false;
        if (Double.compare(parameter.value, value) != 0) return false;
        if (name != null ? !name.equals(parameter.name) : parameter.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        temp = Double.doubleToLongBits(value);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + type;
        return result;
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", type=" + type +
                '}';
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
    }

    private Parameter(Parcel in) {
        this.name = in.readString();
        this.value = in.readDouble();
        this.type = in.readInt();
    }

    public static final Parcelable.Creator<Parameter> CREATOR = new Parcelable.Creator<Parameter>() {
        public Parameter createFromParcel(Parcel source) {
            return new Parameter(source);
        }

        public Parameter[] newArray(int size) {
            return new Parameter[size];
        }
    };
}
