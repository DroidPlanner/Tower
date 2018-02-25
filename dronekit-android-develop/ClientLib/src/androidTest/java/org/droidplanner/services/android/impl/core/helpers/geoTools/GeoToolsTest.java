package org.droidplanner.services.android.impl.core.helpers.geoTools;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import junit.framework.TestCase;

import org.droidplanner.services.android.impl.core.polygon.Polygon;


public class GeoToolsTest extends TestCase {

    LatLongAlt p1 = new LatLongAlt(37.85363485683941, -122.4204097390123, (250.0));
    LatLongAlt p2 = new LatLongAlt(37.85130335221235, -122.4142645673542, (0.0));
    double polygonArea = 502915;
    double dist2DP1toP2 = 599.26;
    double dist3DP1toP2 = 649.32;
    double headingP1toP2 = 115.7;

    public void testGetDistance() {
        double tolerance = dist2DP1toP2 * 0.0005;
        assertEquals(dist2DP1toP2, GeoTools.getDistance(p1, p2), tolerance);
        assertEquals(dist2DP1toP2, GeoTools.getDistance(p2, p1), tolerance);
    }

    public void testGet3DDistance() {
        double tolerance = dist2DP1toP2 * 0.0005;
        assertEquals(dist3DP1toP2, GeoTools.get3DDistance(p1, p2), tolerance);
        assertEquals(dist3DP1toP2, GeoTools.get3DDistance(p2, p1), tolerance);
    }

    public void testGetHeadingFromCoordinates() {
        double tolerance = headingP1toP2 * 0.0005;
        assertEquals(headingP1toP2, GeoTools.getHeadingFromCoordinates(p1, p2), tolerance);
    }

    public void testGetArea() {
        double tolerance = polygonArea * 0.001;
        Polygon poly = new Polygon();
        poly.addPoint(new LatLong(51.0282781100882, 2.16705322265625));
        poly.addPoint(new LatLong(51.0317878663996, 2.16293267905712));
        poly.addPoint(new LatLong(51.0313285768174, 2.16915607452393));
        poly.addPoint(new LatLong(51.0349709823585, 2.17203207314014));
        poly.addPoint(new LatLong(51.0318104275059, 2.17568457126617));
        poly.addPoint(new LatLong(51.0326774403918, 2.18168333172798));
        poly.addPoint(new LatLong(51.0285218722540, 2.18014307320118));

        assertEquals(polygonArea, GeoTools.getArea(poly).valueInSqMeters(), tolerance);
        poly.reversePoints();
        assertEquals(polygonArea, GeoTools.getArea(poly).valueInSqMeters(), tolerance);
    }

}
