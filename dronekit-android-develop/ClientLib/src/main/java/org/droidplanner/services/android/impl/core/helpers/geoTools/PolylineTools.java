package org.droidplanner.services.android.impl.core.helpers.geoTools;

import com.o3dr.services.android.lib.coordinate.LatLong;

import java.util.List;

public class PolylineTools {

    /**
     * Total length of the polyline in meters
     *
     * @param gridPoints
     * @return
     */
    public static double getPolylineLength(List<LatLong> gridPoints) {
        double length = 0;
        for (int i = 1; i < gridPoints.size(); i++) {
            final LatLong to = gridPoints.get(i - 1);
            if (to == null) {
                continue;
            }

            length += GeoTools.getDistance(gridPoints.get(i), to);
        }
        return length;
    }

}
