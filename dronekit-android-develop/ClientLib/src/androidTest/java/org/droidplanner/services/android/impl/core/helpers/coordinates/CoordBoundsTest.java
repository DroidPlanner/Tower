package org.droidplanner.services.android.impl.core.helpers.coordinates;

import com.o3dr.services.android.lib.coordinate.LatLong;

import junit.framework.TestCase;

import java.util.ArrayList;

public class CoordBoundsTest extends TestCase {

    private LatLong origin;
    private LatLong point1;
    private LatLong point2;
    private LatLong point3;
    private LatLong point4;
    private ArrayList<LatLong> list;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        origin = new LatLong(0, 0);
        point1 = new LatLong(2.0, 2.0);
        point2 = new LatLong(-1.0, 3.0);
        point3 = new LatLong(0.0, -5.0);
        point4 = new LatLong(-6.0, -3.0);

        list = new ArrayList<LatLong>();
        list.add(origin);
        list.add(point1);
        list.add(point2);
        list.add(point3);
        list.add(point4);
    }

    public void testSinglePoint() {
        assertEquals(0.0, new CoordBounds(origin).getDiag());
        assertEquals(0.0, new CoordBounds(point1).getDiag());
    }

    public void testList() {
        CoordBounds bounds = new CoordBounds(list);

        assertEquals(2.0, bounds.ne_1quadrant.getLatitude());
        assertEquals(3.0, bounds.ne_1quadrant.getLongitude());
        assertEquals(-6.0, bounds.sw_3quadrant.getLatitude());
        assertEquals(-5.0, bounds.sw_3quadrant.getLongitude());
    }

}
