package com.o3dr.services.android.lib.util;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

import junit.framework.TestCase;

import java.util.Random;

/**
 * Unit tests for math utility functions.
 */
public class MathUtilsTest extends TestCase {
    private static final int MIN_LATITUDE = -90;
    private static final int MAX_LATITUDE = 90;

    private static final int MIN_LONGITUDE = -180;
    private static final int MAX_LONGITUDE = 180;

    private static final double MARGIN_OF_ERROR = 3; //3 Meters

    /**
     * The following constants where obtained from NOAA and are used to validate the
     * accuracy of our methods
     */

    //Using berkley as the origin
    private static final double originLat = 37.8715926;
    private static final double originLon = -122.27274699999998;

    //Points where obtained from google maps and distance was calculated from
    //http://www.nhc.noaa.gov/gccalc.shtml
    private static final double FIRST_LAT = 37.875074;
    private static final double FIRST_LON = -122.273347;
    private static final double FIRST_RESULT = 390.0146608686402;

    private static final double SECOND_LAT = 37.875311;
    private static final double SECOND_LON = -122.278068;
    private static final double SECOND_RESULT = 624.1308042182045;

    private static final double THIRD_LAT = 37.871686;
    private static final double THIRD_LON = -122.270451;
    private static final double THIRD_RESULT = 202.2891830637083;

    public void testGetDistance3D() throws Exception {
        //Test get distance altitude only.
        double distance = MathUtils.getDistance3D(new LatLongAlt(0.0, 0.0, 50.0), new LatLongAlt(0.0, 0.0, 100.0));
        assertEquals(distance, 50.0, MARGIN_OF_ERROR);
    }

    public void testGetDistance2D() throws Exception {
        //Test get distance with Noaa values
        LatLong origin = new LatLong(originLat, originLon);

        assertEquals(FIRST_RESULT, MathUtils.getDistance2D(origin, new LatLong(FIRST_LAT, FIRST_LON)), MARGIN_OF_ERROR);
        assertEquals(SECOND_RESULT, MathUtils.getDistance2D(origin, new LatLong(SECOND_LAT, SECOND_LON)), MARGIN_OF_ERROR);
        assertEquals(THIRD_RESULT, MathUtils.getDistance2D(origin, new LatLong(THIRD_LAT, THIRD_LON)), MARGIN_OF_ERROR);
    }

    public void testGetDistance() throws Exception {
        // Validate that both the 2D and 3D generate the same result when distance is disregarded.
        double distance1;
        double distance2;

        double fromLatitude;
        double fromLongitude;

        double toLatitude;
        double toLongitude;

        Random rand = new Random();

        //Generate 500 random locations and test both equations
        for (int index = 0; index < 500; index++){

            fromLatitude = randDouble(rand, MIN_LATITUDE, MAX_LATITUDE);
            fromLongitude = randDouble(rand, MIN_LONGITUDE, MAX_LONGITUDE);

            toLatitude = randDouble(rand, MIN_LATITUDE, MAX_LATITUDE);
            toLongitude = randDouble(rand, MIN_LONGITUDE, MAX_LONGITUDE);

            //3D Distance equation
            distance1 = MathUtils.getDistance3D(new LatLongAlt(fromLatitude, fromLongitude, 0.0), new LatLongAlt(toLatitude, toLongitude, 0.0));
            //2D Distance equation
            distance2 = MathUtils.getDistance2D(new LatLong(fromLatitude, fromLongitude), new LatLong(toLatitude, toLongitude));

            //Assert that the results from both equations are equal
            assertEquals(distance1, distance2, MARGIN_OF_ERROR);
        }
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Double between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    private double randDouble(Random rand, int min, int max) {
        // nextDouble is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return (rand.nextDouble() * (max - min)) + min;
    }

    public void testNormalize() {
        double max = 10;
        double min = 0;
        assertEquals(0.0, MathUtils.normalize(0, min, max));
        assertEquals(0.5, MathUtils.normalize(5, min, max));
        assertEquals(1.0, MathUtils.normalize(10, min, max));

        assertEquals(0.0, MathUtils.normalize(-1, min, max));
        assertEquals(1.0, MathUtils.normalize(100, min, max));
    }

    public void testIfCanCreateObject() {
        assertNotNull(new MathUtils());
    }

    public void testDCMmatrix(){
        double [][] dcm = MathUtils.dcmFromEuler(0,0,0);
        double [][] expected = new double[][] {{1,0,0},{0,1,0},{0,0,1}};

        for (int i = 0; i < dcm.length; i++) {
            for (int j = 0; j < dcm.length; j++) {
                assertEquals(expected[i][j], dcm[i][j],1e-10);
            }
        }
    }
}