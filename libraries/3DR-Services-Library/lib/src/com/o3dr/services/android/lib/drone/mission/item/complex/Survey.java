package com.o3dr.services.android.lib.drone.mission.item.complex;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.MissionItemType;
import com.o3dr.services.android.lib.util.MathUtils;

import java.util.List;

/**
 * TODO: not yet complete.
 */
public class Survey extends MissionItem implements MissionItem.ComplexItem {

    private SurveyDetail surveyDetail = new SurveyDetail();
    private double polygonArea;
    private List<LatLong> polygonPoints;
    private List<LatLong> gridPoints;
    private List<LatLong> cameraLocations;
    private boolean isValid;

    public Survey(){
        super(MissionItemType.SURVEY);
    }

    public void copy(Survey source){
        this.surveyDetail = source.surveyDetail;
        this.polygonArea = source.polygonArea;
        this.polygonPoints = source.polygonPoints;
        this.gridPoints = source.gridPoints;
        this.cameraLocations = source.cameraLocations;
        this.isValid = source.isValid;
    }

    public SurveyDetail getSurveyDetail() {
        return surveyDetail;
    }

    public void setSurveyDetail(SurveyDetail surveyDetail) {
        this.surveyDetail = surveyDetail;
    }

    public double getPolygonArea() {
        return polygonArea;
    }

    public void setPolygonArea(double polygonArea) {
        this.polygonArea = polygonArea;
    }

    public List<LatLong> getPolygonPoints() {
        return polygonPoints;
    }

    public void setPolygonPoints(List<LatLong> polygonPoints) {
        this.polygonPoints = polygonPoints;
    }

    public List<LatLong> getGridPoints() {
        return gridPoints;
    }

    public void setGridPoints(List<LatLong> gridPoints) {
        this.gridPoints = gridPoints;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public double getGridLength() {
        return MathUtils.getPolylineLength(gridPoints);
    }

    public int getNumberOfLines() {
        return gridPoints.size() / 2;
    }

    public List<LatLong> getCameraLocations() {
        return cameraLocations;
    }

    public void setCameraLocations(List<LatLong> cameraLocations) {
        this.cameraLocations = cameraLocations;
    }

    public int getCameraCount() {
        return getCameraLocations().size();
    }

    public static final Creator<Survey> CREATOR = new Creator<Survey>() {
        @Override
        public Survey createFromParcel(Parcel source) {
            return (Survey) source.readSerializable();
        }

        @Override
        public Survey[] newArray(int size) {
            return new Survey[size];
        }
    };
}
