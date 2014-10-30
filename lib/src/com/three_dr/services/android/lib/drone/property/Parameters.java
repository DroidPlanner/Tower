package com.three_dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 10/28/14.
 */
public class Parameters implements Parcelable {

    private List<Parameter> parameterList = new ArrayList<Parameter>();

    public Parameters(List<Parameter> parameterList) {
        this.parameterList = parameterList;
    }

    public List<Parameter> getParameterList() {
        return parameterList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parameters)) return false;

        Parameters that = (Parameters) o;

        if (parameterList != null ? !parameterList.equals(that.parameterList) : that.parameterList != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return parameterList != null ? parameterList.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Parameters{" +
                "parameterList=" + parameterList +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(parameterList);
    }

    private Parameters(Parcel in) {
        in.readTypedList(parameterList, Parameter.CREATOR);
    }

    public static final Parcelable.Creator<Parameters> CREATOR = new Parcelable.Creator<Parameters>() {
        public Parameters createFromParcel(Parcel source) {
            return new Parameters(source);
        }

        public Parameters[] newArray(int size) {
            return new Parameters[size];
        }
    };
}
