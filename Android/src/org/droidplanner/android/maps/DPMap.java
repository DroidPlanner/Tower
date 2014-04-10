package org.droidplanner.android.maps;

import android.content.res.Resources;
import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

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

    interface PathSource {
        public List<Coord2D> getPathPoints();
    }

    public void clearFlightPath();

    public void addFlightPathPoint(Coord2D coord);

    /**
     * Adds padding around the edges of the map.
     *
     * @param left   the number of pixels of padding to be added on the left of the map.
     * @param top    the number of pixels of padding to be added on the top of the map.
     * @param right  the number of pixels of padding to be added on the right of the map.
     * @param bottom the number of pixels of padding to be added on the bottom of the map.
     */
    public void setMapPadding(int left, int top, int right, int bottom);

    public void updateDroneLeashPath(PathSource pathSource);

    public void updateMissionPath(PathSource pathSource);

}
