package org.droidplanner.android.maps;

import android.content.res.Resources;
import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.core.helpers.coordinates.Coord2D;

import java.util.List;

/**
 * Defines the functionality expected from the map providers.
 */
public interface DPMap {

    public static final String PACKAGE_NAME = DPMap.class.getPackage().getName();

    public static final String EXTRA_MAX_FLIGHT_PATH_SIZE = PACKAGE_NAME + "" +
            ".EXTRA_MAX_FLIGHT_PATH_SIZE";

    public static final int FLIGHT_PATH_DEFAULT_COLOR = 0xfffd693f;
    public static final int FLIGHT_PATH_DEFAULT_WIDTH = 6;

    public static final int MISSION_PATH_DEFAULT_COLOR = Color.WHITE;
    public static final int MISSION_PATH_DEFAULT_WIDTH = 4;

    public static final int DRONE_LEASH_DEFAULT_COLOR = Color.WHITE;
    public static final int DRONE_LEASH_DEFAULT_WIDTH = 2;

    public static final String PREF_LAT = "lat";
    public static final String PREF_LNG = "lng";
    public static final String PREF_BEA = "bea";
    public static final String PREF_TILT = "tilt";
    public static final String PREF_ZOOM = "zoom";

    interface PathSource {
        public List<Coord2D> getPathPoints();
    }

    /**
     * Implemented by classes interested in map click events.
     */
    interface OnMapClickListener {
        /**
         * Triggered when the map is clicked.
         * @param coord location where the map was clicked.
         */
        void onMapClick(Coord2D coord);
    }

    /**
     * Implemented by classes interested in map long click events.
     */
    interface OnMapLongClickListener {
        /**
         * Triggered when the map is long clicked.
         * @param coord location where the map was long clicked.
         */
        void onMapLongClick(Coord2D coord);
    }

    /**
     * Implemented by classes interested in marker(s) click events.
     */
    interface OnMarkerClickListener {
        /**
         * Triggered when a marker is clicked.
         * @param markerInfo info about the clicked marker
         * @return true if the listener has consumed the event.
         */
        boolean onMarkerClick(MarkerInfo markerInfo);
    }

    /**
     * Callback interface for drag events on markers.
     */
    interface OnMarkerDragListener {
        /**
         * Called repeatedly while a marker is being dragged. The marker's location can be
         * accessed via {@link MarkerInfo#getPosition()}
         * @param markerInfo info about the marker that was dragged.
         */
        void onMarkerDrag(MarkerInfo markerInfo);

        /**
         * Called when a marker has finished being dragged. The marker's location can be accessed
         * via {@link MarkerInfo#getPosition()}
         * @param markerInfo info about the marker that was dragged.
         */
        void onMarkerDragEnd(MarkerInfo markerInfo);

        /**
         * Called when a marker starts being dragged. The marker's location can be accessed via
         * {@link MarkerInfo#getPosition()}; this position may be different to the position
         * prior to the start of the drag because the marker is popped up above the touch point.
         * @param markerInfo info about the marker that was dragged.
         */
        void onMarkerDragStart(MarkerInfo markerInfo);

    }

    /**
     * Adds a coordinate to the drone's flight path.
     *
     * @param coord drone's coordinate
     */
    public void addFlightPathPoint(Coord2D coord);

    /**
     * Remove all markers from the map.
     */
    public void cleanMarkers();

    /**
     * Clears the drone's flight path.
     */
    public void clearFlightPath();

    /**
     * @return this map's provider.
     */
    public DPMapProvider getProvider();

    /**
     * Restores the map's camera settings from preferences.
     */
    public void loadCameraPosition();

    public List<Coord2D> projectPathIntoMap(List<Coord2D> pathPoints);

    /**
     * Stores the map camera settings.
     */
    public void saveCameraPosition();

    /**
     * Adds padding around the edges of the map.
     *
     * @param left   the number of pixels of padding to be added on the left of the map.
     * @param top    the number of pixels of padding to be added on the top of the map.
     * @param right  the number of pixels of padding to be added on the right of the map.
     * @param bottom the number of pixels of padding to be added on the bottom of the map.
     */
    public void setMapPadding(int left, int top, int right, int bottom);

    /**
     * Sets a callback that's invoked when the map is tapped.
     * @param listener The callback that's invoked when the map is tapped. To unset the callback,
     *                 use null.
     */
    public void setOnMapClickListener(OnMapClickListener listener);

    /**
     * Sets a callback that's invoked when the map is long pressed.
     * @param listener The callback that's invoked when the map is long pressed. To unset the
     *                 callback, use null.
     */
    public void setOnMapLongClickListener(OnMapLongClickListener listener);

    /**
     * Sets a callback that's invoked when a marker is clicked.
     * @param listener The callback that's invoked when a marker is clicked. To unset the
     *                 callback, use null.
     */
    public void setOnMarkerClickListener(OnMarkerClickListener listener);

    /**
     * Sets a callback that's invoked when a marker is dragged.
     * @param listener The callback that's invoked on marker drag events. To unset the callback, use null.
     */
    public void setOnMarkerDragListener(OnMarkerDragListener listener);

    /**
     * Updates the map's center, and zoom level.
     * @param coord location for the map center
     * @param zoomLevel zoom level for the map
     */
    public void updateCamera(Coord2D coord, int zoomLevel);

    /**
     * Updates the drone leash path on the map.
     *
     * @param pathSource source to use to generate the drone leash path.
     */
    public void updateDroneLeashPath(PathSource pathSource);

    /**
     * Adds / updates the marker corresponding to the given marker info argument.
     *
     * @param markerInfo used to generate / update the marker
     */
    public void updateMarker(MarkerInfo markerInfo);

    /**
     * Adds / updates the marker corresponding to the given marker info argument.
     *
     * @param markerInfo  used to generate / update the marker
     * @param isDraggable overwrites markerInfo draggable preference
     */
    public void updateMarker(MarkerInfo markerInfo, boolean isDraggable);

    /**
     * Adds / updates the markers corresponding to the given list of markers infos.
     *
     * @param markersInfos source for the new markers to add/update
     */
    public void updateMarkers(List<MarkerInfo> markersInfos);

    /**
     * Adds / updates the markers corresponding to the given list of markers infos.
     *
     * @param markersInfos source for the new markers to add/update
     * @param isDraggable  overwrites markerInfo draggable preference
     */
    public void updateMarkers(List<MarkerInfo> markersInfos, boolean isDraggable);

    /**
     * Updates the mission path on the map.
     *
     * @param pathSource source to use to draw the mission path
     */
    public void updateMissionPath(PathSource pathSource);

}
