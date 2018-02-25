package org.droidplanner.services.android.impl.core.helpers.coordinates;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import junit.framework.TestCase;

public class Coord3DTest extends TestCase {

    public void testConstructor() {
        double alt = (50.0);
        LatLongAlt point = new LatLongAlt(10, -5.6, alt);
        assertEquals(10.0, point.getLatitude());
        assertEquals(-5.6, point.getLongitude());
        assertEquals(alt, point.getAltitude());
    }

    public void testConstFrom2dCoord() {
        double alt = (5.0);
        LatLong point2d = new LatLong(1, -0.6);
        LatLongAlt point = new LatLongAlt(point2d, alt);

        assertEquals(1.0, point.getLatitude());
        assertEquals(-0.6, point.getLongitude());
        assertEquals(alt, point.getAltitude());
    }

    public void testSet() {
        double alt = (50.0);
        LatLongAlt point = new LatLongAlt(10, -5.6, alt);
        point.setLatitude(0);
        point.setLongitude(0);
        point.setAltitude(alt);
        assertEquals(0.0, point.getLatitude());
        assertEquals(0.0, point.getLongitude());
        assertEquals(alt, point.getAltitude());
    }

}
