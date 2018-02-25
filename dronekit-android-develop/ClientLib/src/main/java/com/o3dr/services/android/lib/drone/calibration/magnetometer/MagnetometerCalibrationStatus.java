package com.o3dr.services.android.lib.drone.calibration.magnetometer;

import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.drone.property.DroneAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Fredia Huya-Kouadio on 5/4/15.
 */
public class MagnetometerCalibrationStatus implements DroneAttribute {

    private final Map<Integer, MagnetometerCalibrationProgress> calibrationProgressTracker = new HashMap<>();
    private final Map<Integer, MagnetometerCalibrationResult> calibrationResultTracker = new HashMap<>();

    private final List<Integer> compassList = new ArrayList<>();

    private boolean calibrationCancelled;

    public MagnetometerCalibrationStatus() {
    }

    public void addCalibrationProgress(MagnetometerCalibrationProgress progress) {
        if (progress != null) {
            final int compassId = progress.getCompassId();
            calibrationProgressTracker.put(compassId, progress);
            compassList.add(compassId);
        }
    }

    public void addCalibrationResult(MagnetometerCalibrationResult result) {
        if (result != null) {
            final int compassId = result.getCompassId();
            calibrationResultTracker.put(result.getCompassId(), result);
            compassList.add(compassId);
        }
    }

    public List<Integer> getCompassIds() {
        return compassList;
    }

    public MagnetometerCalibrationProgress getCalibrationProgress(int compassId) {
        return calibrationProgressTracker.get(compassId);
    }

    public MagnetometerCalibrationResult getCalibrationResult(int compassId) {
        return calibrationResultTracker.get(compassId);
    }

    public boolean isCalibrationCancelled() {
        return calibrationCancelled;
    }

    public void setCalibrationCancelled(boolean calibrationCancelled) {
        this.calibrationCancelled = calibrationCancelled;
    }

    public boolean isCalibrationComplete(){
        for(Integer compassId: compassList){
            if(!calibrationResultTracker.containsKey(compassId))
                return false;
        }

        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final List<MagnetometerCalibrationProgress> progressList = new ArrayList<>(this.calibrationProgressTracker
                .values());
        dest.writeTypedList(progressList);

        final List<MagnetometerCalibrationResult> resultList = new ArrayList<>(this.calibrationResultTracker.values());
        dest.writeTypedList(resultList);

        dest.writeByte(calibrationCancelled ? (byte) 1 : (byte) 0);
    }

    private MagnetometerCalibrationStatus(Parcel in) {
        final List<MagnetometerCalibrationProgress> progressList = new ArrayList<>();
        in.readTypedList(progressList, MagnetometerCalibrationProgress.CREATOR);
        for (MagnetometerCalibrationProgress progress : progressList) {
            addCalibrationProgress(progress);
        }

        final List<MagnetometerCalibrationResult> resultList = new ArrayList<>();
        in.readTypedList(resultList, MagnetometerCalibrationResult.CREATOR);
        for (MagnetometerCalibrationResult result : resultList)
            addCalibrationResult(result);

        this.calibrationCancelled = in.readByte() != 0;
    }

    public static final Parcelable.Creator<MagnetometerCalibrationStatus> CREATOR = new Parcelable.Creator<MagnetometerCalibrationStatus>() {
        public MagnetometerCalibrationStatus createFromParcel(Parcel source) {
            return new MagnetometerCalibrationStatus(source);
        }

        public MagnetometerCalibrationStatus[] newArray(int size) {
            return new MagnetometerCalibrationStatus[size];
        }
    };
}
