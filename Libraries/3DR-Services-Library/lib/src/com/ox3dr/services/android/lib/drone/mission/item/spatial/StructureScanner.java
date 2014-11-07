package com.ox3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;
import com.ox3dr.services.android.lib.drone.mission.item.complex.CameraDetail;

/**
 *
 */
public class StructureScanner extends BaseSpatialItem {

    private double radius = 10;
    private double heightStep = 5;
    private int stepsCount = 2;
    private boolean crossHatch = false;
    private CameraDetail cameraDetail;

    public StructureScanner(){
        super(MissionItemType.STRUCTURE_SCANNER);
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getHeightStep() {
        return heightStep;
    }

    public void setHeightStep(double heightStep) {
        this.heightStep = heightStep;
    }

    public int getStepsCount() {
        return stepsCount;
    }

    public void setStepsCount(int stepsCount) {
        this.stepsCount = stepsCount;
    }

    public boolean isCrossHatch() {
        return crossHatch;
    }

    public void setCrossHatch(boolean crossHatch) {
        this.crossHatch = crossHatch;
    }

    public CameraDetail getCameraDetail() {
        return cameraDetail;
    }

    public void setCameraDetail(CameraDetail cameraDetail) {
        this.cameraDetail = cameraDetail;
    }

    public static final Creator<StructureScanner> CREATOR = new Creator<StructureScanner>() {
        @Override
        public StructureScanner createFromParcel(Parcel source) {
            return (StructureScanner) source.readSerializable();
        }

        @Override
        public StructureScanner[] newArray(int size) {
            return new StructureScanner[size];
        }
    };
}
