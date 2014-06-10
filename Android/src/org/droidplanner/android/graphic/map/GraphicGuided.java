package org.droidplanner.android.graphic.map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;

import org.droidplanner.R;
import org.droidplanner.android.maps.MarkerWithText;
import org.droidplanner.android.maps.DPMap.PathSource;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import java.util.ArrayList;
import java.util.List;

public class GraphicGuided extends MarkerInfo.SimpleMarkerInfo implements PathSource {

    private final static String TAG = GraphicGuided.class.getSimpleName();

    private GuidedPoint guidedPoint;
    private GPS GPS;

    public GraphicGuided(Drone drone) {
        guidedPoint = drone.guidedPoint;
        GPS = drone.GPS;
    }

    @Override
    public List<Coord2D> getPathPoints() {
        List<Coord2D> path = new ArrayList<Coord2D>();
        if (guidedPoint.isActive()) {
            path.add(GPS.getPosition());
            path.add(guidedPoint.getCoord());
        }
        return path;
    }

    @Override
    public boolean isVisible() {
        return guidedPoint.isActive();
    }

    @Override
    public float getAnchorU() {
        return 0.5f;
    }

    @Override
    public float getAnchorV() {
        return 0.5f;
    }

    @Override
    public Coord2D getPosition() {
        return guidedPoint.getCoord();
    }

    @Override
    public void setPosition(Coord2D coord) {
        try {
            guidedPoint.forcedGuidedCoordinate(coord);
        }
        catch (Exception e) {
            Log.e(TAG, "Unable to update guided point position.", e);
        }
    }

    @Override
    public Bitmap getIcon(Resources res) {
        return MarkerWithText.getMarkerWithTextAndDetail(R.drawable.ic_wp_map, "Guided", "", res);
    }

    @Override
    public boolean isDraggable() {
        return true;
    }
}