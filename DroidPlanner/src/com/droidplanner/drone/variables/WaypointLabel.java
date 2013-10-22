package com.droidplanner.drone.variables;

import android.content.Context;
import com.droidplanner.fragments.markers.MarkerManager;
import com.droidplanner.fragments.markers.WaypointLabelMarker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class WaypointLabel implements MarkerManager.MarkerSource {

    private short number;
    private final Double height;
    private final LatLng coord;
    private final boolean showOnMap;

    public WaypointLabel(waypoint waypoint) {
        number = waypoint.getNumber();
        height = waypoint.getHeight();
        coord = waypoint.getCoord();
        showOnMap = waypoint.getCmd().showOnMap();
    }

    public short getNumber() {
        return number;
    }

    public Double getHeight() {
        return height;
    }

    public LatLng getCoord() {
        return coord;
    }

    public boolean showOnMap() {
        return showOnMap;
    }

    @Override
    public MarkerOptions build(Context context) {
        return WaypointLabelMarker.build(this, context);
    }

    @Override
    public void update(Marker marker, Context context) {
        WaypointLabelMarker.update(marker, this, context);
    }
}
