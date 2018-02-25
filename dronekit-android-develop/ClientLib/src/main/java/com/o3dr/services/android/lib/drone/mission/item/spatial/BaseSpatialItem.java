package com.o3dr.services.android.lib.drone.mission.item.spatial;

import android.os.Parcel;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public abstract class BaseSpatialItem extends MissionItem implements MissionItem.SpatialItem, android.os.Parcelable {

    private LatLongAlt coordinate;

    protected BaseSpatialItem(MissionItemType type) {
        this(type, null);
    }

    protected BaseSpatialItem(MissionItemType type, LatLongAlt coordinate){
        super(type);
        this.coordinate = coordinate;
    }

    protected BaseSpatialItem(BaseSpatialItem copy){
        this(copy.getType(), copy.coordinate == null ? null : new LatLongAlt(copy.coordinate));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseSpatialItem)) return false;
        if (!super.equals(o)) return false;

        BaseSpatialItem that = (BaseSpatialItem) o;

        return !(coordinate != null ? !coordinate.equals(that.coordinate) : that.coordinate != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (coordinate != null ? coordinate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BaseSpatialItem{" +
                "coordinate=" + coordinate +
                '}';
    }

    @Override
    public LatLongAlt getCoordinate() {
        return coordinate;
    }

    @Override
    public void setCoordinate(LatLongAlt coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.coordinate, flags);
    }

    protected BaseSpatialItem(Parcel in) {
        super(in);
        this.coordinate = in.readParcelable(LatLongAlt.class.getClassLoader());
    }
}
