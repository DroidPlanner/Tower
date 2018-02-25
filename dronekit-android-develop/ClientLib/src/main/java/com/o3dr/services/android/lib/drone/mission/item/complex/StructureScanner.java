package com.o3dr.services.android.lib.drone.mission.item.complex;

import android.os.Parcel;
import android.os.Parcelable;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.spatial.BaseSpatialItem;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class StructureScanner extends BaseSpatialItem implements MissionItem.ComplexItem<StructureScanner>, Parcelable {

    private double radius = 10;
    private double heightStep = 5;
    private int stepsCount = 2;
    private boolean crossHatch = false;
    private SurveyDetail surveyDetail = new SurveyDetail();
    private List<LatLong> path = new ArrayList<LatLong>();

    public StructureScanner(){
        super(MissionItemType.STRUCTURE_SCANNER);
    }

    public StructureScanner(StructureScanner copy){
        super(copy);
        copy(copy);
    }

    @Override
    public void copy(StructureScanner source){
        this.radius = source.radius;
        this.heightStep = source.heightStep;
        this.stepsCount = source.stepsCount;
        this.crossHatch = source.crossHatch;
        this.surveyDetail = new SurveyDetail(source.surveyDetail);
        this.path = copyPointsList(source.path);
    }

    private List<LatLong> copyPointsList(List<LatLong> copy){
        final List<LatLong> dest = new ArrayList<>();
        for(LatLong itemCopy : copy){
            dest.add(new LatLong(itemCopy));
        }

        return dest;
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

    public List<LatLong> getPath() {
        return path;
    }

    public void setPath(List<LatLong> points){
        this.path = points;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeDouble(this.radius);
        dest.writeDouble(this.heightStep);
        dest.writeInt(this.stepsCount);
        dest.writeByte(crossHatch ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.surveyDetail, 0);
        dest.writeTypedList(path);
    }

    private StructureScanner(Parcel in) {
        super(in);
        this.radius = in.readDouble();
        this.heightStep = in.readDouble();
        this.stepsCount = in.readInt();
        this.crossHatch = in.readByte() != 0;
        this.surveyDetail = in.readParcelable(SurveyDetail.class.getClassLoader());
        in.readTypedList(path, LatLong.CREATOR);
    }

    @Override
    public String toString() {
        return "StructureScanner{" +
                "crossHatch=" + crossHatch +
                ", radius=" + radius +
                ", heightStep=" + heightStep +
                ", stepsCount=" + stepsCount +
                ", surveyDetail=" + surveyDetail +
                ", path=" + path +
                ", " + super.toString() +
        '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StructureScanner)) return false;
        if (!super.equals(o)) return false;

        StructureScanner that = (StructureScanner) o;

        if (Double.compare(that.radius, radius) != 0) return false;
        if (Double.compare(that.heightStep, heightStep) != 0) return false;
        if (stepsCount != that.stepsCount) return false;
        if (crossHatch != that.crossHatch) return false;
        if (surveyDetail != null ? !surveyDetail.equals(that.surveyDetail) : that.surveyDetail != null)
            return false;
        return !(path != null ? !path.equals(that.path) : that.path != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(radius);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(heightStep);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + stepsCount;
        result = 31 * result + (crossHatch ? 1 : 0);
        result = 31 * result + (surveyDetail != null ? surveyDetail.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    @Override
    public MissionItem clone() {
        return new StructureScanner(this);
    }

    public static final Creator<StructureScanner> CREATOR = new Creator<StructureScanner>() {
        public StructureScanner createFromParcel(Parcel source) {
            return new StructureScanner(source);
        }

        public StructureScanner[] newArray(int size) {
            return new StructureScanner[size];
        }
    };
}
