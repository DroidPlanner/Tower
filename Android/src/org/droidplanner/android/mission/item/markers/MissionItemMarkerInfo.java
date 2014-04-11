package org.droidplanner.android.mission.item.markers;

import android.content.res.Resources;
import android.graphics.Bitmap;

import org.droidplanner.android.fragments.markers.helpers.MarkerWithText;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.mission.MissionProxy;
import org.droidplanner.android.mission.item.MissionItemProxy;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;

import java.io.Serializable;

/**
 * Template class and factory for a mission item's marker source.
 */
public abstract class MissionItemMarkerInfo extends MarkerInfo.SimpleMarkerInfo implements Serializable {

    protected final MissionItemProxy mMarkerOrigin;

    public static MissionItemMarkerInfo newInstance(MissionItemProxy origin) {
        MissionItemMarkerInfo markerInfo;
        switch (origin.getMissionItem().getType()) {
            case LAND:
                markerInfo = new LandMarkerInfo(origin);
                break;

            case LOITER:
            case LOITER_INF:
            case LOITERN:
            case LOITERT:
                markerInfo = new LoiterMarkerInfo(origin);
                break;

            case ROI:
                markerInfo = new ROIMarkerInfo(origin);
                break;

            case TAKEOFF:
                markerInfo = new TakeoffMarkerInfo(origin);
                break;

            case WAYPOINT:
                markerInfo = new WaypointMarkerInfo(origin);
                break;

            default:
                markerInfo = null;
                break;
        }

        return markerInfo;
    }

    protected MissionItemMarkerInfo(MissionItemProxy origin) {
        mMarkerOrigin = origin;
    }

    public MissionItemProxy getMarkerOrigin() {
        return mMarkerOrigin;
    }

    @Override
    public float getAnchorU(){
        return 0.5f;
    }

    @Override
    public float getAnchorV(){
        return 0.5f;
    }

    @Override
    public Coord2D getPosition(){
        return ((SpatialCoordItem) mMarkerOrigin.getMissionItem()).getCoordinate();
    }

    @Override
    public void setPosition(Coord2D coord){
        ((SpatialCoordItem) mMarkerOrigin.getMissionItem()).setPosition(coord);
    }

    @Override
    public boolean isDraggable(){
        return true;
    }

    @Override
    public boolean isVisible(){
        return true;
    }

    @Override
    public Bitmap getIcon(Resources res){
        int drawable;
        final MissionProxy missionProxy = mMarkerOrigin.getMissionRender();
        if (missionProxy.selectionContains(mMarkerOrigin)) {
            drawable = getSelectedIconResource();
        } else {
            drawable = getIconResource();
        }

        return MarkerWithText.getMarkerWithTextAndDetail(drawable,
                Integer.toString(missionProxy.getOrder(mMarkerOrigin)), getIconDetail(), res);
    }

    private String getIconDetail() {
        try {
            final MissionProxy missionProxy = mMarkerOrigin.getMissionRender();
            if (missionProxy.getAltitudeDiffFromPreviousItem(mMarkerOrigin).valueInMeters() ==
                    0) {
                return null;
            } else {
                return null; // altitude.toString();
            }
        } catch (Exception e) {
            return null;
        }
    }

    protected abstract int getSelectedIconResource();

    protected abstract int getIconResource();
}
