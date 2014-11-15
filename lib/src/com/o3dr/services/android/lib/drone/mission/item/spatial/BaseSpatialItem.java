package com.o3dr.services.android.lib.drone.mission.item.spatial;

import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.MissionItemType;

/**
 * Created by fhuya on 11/6/14.
 */
public abstract class BaseSpatialItem extends MissionItem implements MissionItem.SpatialItem{

    protected LatLongAlt coordinate;

    protected BaseSpatialItem(MissionItemType type) {
        super(type);
    }

    @Override
    public LatLongAlt getCoordinate() {
        return coordinate;
    }

    @Override
    public void setCoordinate(LatLongAlt coordinate) {
        this.coordinate = coordinate;
    }
}
