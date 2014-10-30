package com.three_dr.services.android.lib.drone.property;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Map;

/**
 * Created by fhuya on 10/28/14.
 */
public class Parameters implements Parcelable {

    private Bundle parametersBundle = new Bundle();

    public Parameters(List<Parameter> parameterList) {
        if(!parameterList.isEmpty()){
            for(Parameter parameter: parameterList){
                parametersBundle.putParcelable(parameter.getName(), parameter);
            }
        }
    }

    public Parameter getParameter(String parameterName){
        return parametersBundle.getParcelable(parameterName);
    }

    public Bundle getParameters(){
        return parametersBundle;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(parametersBundle);
    }

    private Parameters(Parcel in) {
        parametersBundle = in.readBundle();
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
