package com.ox3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhuya on 10/28/14.
 */
public class Parameters implements Parcelable {

    private List<Parameter> parametersList = new ArrayList<Parameter>();

    public Parameters(List<Parameter> parameterList) {
        this.parametersList = parameterList;
    }

    public List<Parameter> getParameters(){
        return parametersList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(parametersList);
    }

    private Parameters(Parcel in) {
        in.readTypedList(parametersList, Parameter.CREATOR);
    }

    public static final Creator<Parameters> CREATOR = new Creator<Parameters>() {
        public Parameters createFromParcel(Parcel source) {
            return new Parameters(source);
        }

        public Parameters[] newArray(int size) {
            return new Parameters[size];
        }
    };
}
