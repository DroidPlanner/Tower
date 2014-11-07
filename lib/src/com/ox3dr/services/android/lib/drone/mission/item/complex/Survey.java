package com.ox3dr.services.android.lib.drone.mission.item.complex;

import android.os.Parcel;

import com.ox3dr.services.android.lib.coordinate.LatLong;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItem;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;

import java.util.List;

/**
 * TODO: not yet complete.
 */
public class Survey extends MissionItem implements MissionItem.ComplexItem {

    private SurveyDetail surveyDetail;
    private double polygonArea;
    private List<LatLong> polygonPoints;
    private List<LatLong> gridPoints;

    public Survey(){
        super(MissionItemType.SURVEY);
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
