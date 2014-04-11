package org.droidplanner.android.mission.item.markers;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.droidplanner.android.fragments.markers.helpers.MarkerWithText;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.android.graphic.map.MarkerManager;
import org.droidplanner.android.mission.item.MissionItemRender;
import org.droidplanner.android.mission.MissionRender;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;

import java.io.Serializable;

/**
 * Template class and factory for a mission item's marker source.
 */
public abstract class MissionItemMarkerSource implements MarkerManager.MarkerSource, Serializable {

    protected final MissionItemRender mMarkerOrigin;

    public static MissionItemMarkerSource newInstance(MissionItemRender origin) {
        MissionItemMarkerSource markerSource;
        switch (origin.getMissionItem().getType()) {
            case LAND:
                markerSource = new LandMarkerSource(origin);
                break;

            case LOITER:
            case LOITER_INF:
            case LOITERN:
            case LOITERT:
                markerSource = new LoiterMarkerSource(origin);
                break;

            case ROI:
                markerSource = new ROIMarkerSource(origin);
                break;

            case TAKEOFF:
                markerSource = new TakeoffMarkerSource(origin);
                break;

            case WAYPOINT:
                markerSource = new WaypointMarkerSource(origin);
                break;

            default:
                markerSource = null;
                break;
        }

        return markerSource;
    }

    protected MissionItemMarkerSource(MissionItemRender origin) {
        mMarkerOrigin = origin;
    }

    public MissionItemRender getMarkerOrigin() {
        return mMarkerOrigin;
    }

    @Override
    public MarkerOptions build(Context context) {
        return new MarkerOptions().position(DroneHelper.CoordToLatLang(((SpatialCoordItem)
                mMarkerOrigin.getMissionItem()).getCoordinate())).draggable(true).anchor(0.5f,
                0.5f).icon(getIcon(context));
    }

    @Override
    public void update(Marker marker, Context context) {
        marker.setPosition(DroneHelper.CoordToLatLang(((SpatialCoordItem) mMarkerOrigin
                .getMissionItem()).getCoordinate()));
        marker.setIcon(getIcon(context));
    }

    protected BitmapDescriptor getIcon(Context context) {
        int drawable;
        final MissionRender missionRender = mMarkerOrigin.getMissionRender();
        if (missionRender.selection.selectionContains(mMarkerOrigin)) {
            drawable = getSelectedIconResource();
        } else {
            drawable = getIconResource();
        }
        Bitmap marker = MarkerWithText.getMarkerWithTextAndDetail(drawable,
                Integer.toString(missionRender.getOrder(mMarkerOrigin)), getIconDetail(), context);
        return BitmapDescriptorFactory.fromBitmap(marker);
    }

    private String getIconDetail() {
        try {
            final MissionRender missionRender = mMarkerOrigin.getMissionRender();
            if (missionRender.getAltitudeDiffFromPreviousItem(mMarkerOrigin).valueInMeters() ==
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
