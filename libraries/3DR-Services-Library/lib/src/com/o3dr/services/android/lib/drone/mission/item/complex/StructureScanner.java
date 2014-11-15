package com.o3dr.services.android.lib.drone.mission.item.complex;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.spatial.BaseSpatialItem;

import java.util.List;

/**
 *
 */
public class StructureScanner extends BaseSpatialItem implements MissionItem.ComplexItem {

    private double radius = 10;
    private double heightStep = 5;
    private int stepsCount = 2;
    private boolean crossHatch = false;
    private SurveyDetail surveyDetail = new SurveyDetail();
    private List<LatLong> path;

    public StructureScanner(){
        super(MissionItemType.STRUCTURE_SCANNER);
    }

    public void copy(StructureScanner source){
        this.radius = source.radius;
        this.heightStep = source.heightStep;
        this.stepsCount = source.stepsCount;
        this.crossHatch = source.crossHatch;
        this.surveyDetail = source.surveyDetail;
        this.path = source.path;
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

    public SurveyDetail getSurveyDetail() {
        return surveyDetail;
    }

    public void setSurveyDetail(SurveyDetail surveyDetail) {
        this.surveyDetail = surveyDetail;
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

    public List<LatLong> getPath() {
        return path;
    }

    public void setPath(List<LatLong> points){
        this.path = points;
    }
}
