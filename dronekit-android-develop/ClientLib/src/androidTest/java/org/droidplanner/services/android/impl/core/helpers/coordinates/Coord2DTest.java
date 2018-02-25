package org.droidplanner.services.android.impl.core.helpers.coordinates;

import com.o3dr.services.android.lib.coordinate.LatLong;

import junit.framework.TestCase;

public class Coord2DTest extends TestCase {

    public void testConstructor() {
        LatLong point = new LatLong(10, -5.6);
        assertEquals(10.0, point.getLatitude());
        assertEquals(-5.6, point.getLongitude());
    }

}
