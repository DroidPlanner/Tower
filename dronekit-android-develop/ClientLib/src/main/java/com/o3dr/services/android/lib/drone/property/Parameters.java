package com.o3dr.services.android.lib.drone.property;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by fhuya on 10/28/14.
 */
public class Parameters implements DroneAttribute {

    private final List<Parameter> parametersList = new ArrayList<>();

    public Parameters(){
    }

    public Parameters(Collection<Parameter> parameterList) {
        setParametersList(parameterList);
    }

    public List<Parameter> getParameters(){
        return parametersList;
    }

    public @Nullable Parameter getParameter(String name){
        if(TextUtils.isEmpty(name))
            return null;

        for(Parameter param: parametersList){
            if(param.getName().equalsIgnoreCase(name))
                return param;
        }

        return null;
    }

    public void setParametersList(Collection<Parameter> parametersList) {
        this.parametersList.clear();
        if(parametersList != null && !parametersList.isEmpty()) {
            this.parametersList.addAll(parametersList);
        }
    }

    /**
     * Adds a parameter to the parameters set.
     * @param parameter
     * @since 2.8.0
     */
    public void addParameter(@NonNull Parameter parameter){
        if(parameter == null)
            throw new NullPointerException("Invalid parameter argument.");

        this.parametersList.add(parameter);
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
