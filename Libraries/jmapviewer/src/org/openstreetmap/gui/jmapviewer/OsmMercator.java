// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer;

/**
 * This class implements the Mercator Projection as it is used by OpenStreetMap
 * (and google). It provides methods to translate coordinates from 'map space'
 * into latitude and longitude (on the WGS84 ellipsoid) and vice versa. Map
 * space is measured in pixels. The origin of the map space is the top left
 * corner. The map space origin (0,0) has latitude ~85 and longitude -180.
 */
public class OsmMercator {

    public static int TILE_SIZE = 256;
    public static final double MAX_LAT = 85.05112877980659;
    public static final double MIN_LAT = -85.05112877980659;
    private static double EARTH_RADIUS = 6378137; // equatorial earth radius for EPSG:3857 (Mercator)

    public static double radius(int aZoomlevel) {
        return (TILE_SIZE * (1 << aZoomlevel)) / (2.0 * Math.PI);
    }

    /**
     * Returns the absolut number of pixels in y or x, defined as: 2^Zoomlevel *
     * TILE_WIDTH where TILE_WIDTH is the width of a tile in pixels
     *
     * @param aZoomlevel zoom level to request pixel data
     * @return number of pixels
     */
    public static int getMaxPixels(int aZoomlevel) {
        return TILE_SIZE * (1 << aZoomlevel);
    }

    public static int falseEasting(int aZoomlevel) {
        return getMaxPixels(aZoomlevel) / 2;
    }

    public static int falseNorthing(int aZoomlevel) {
        return (-1 * getMaxPixels(aZoomlevel) / 2);
    }

    /**
     * Transform pixelspace to coordinates and get the distance.
     *
     * @param x1 the first x coordinate
     * @param y1 the first y coordinate
     * @param x2 the second x coordinate
     * @param y2 the second y coordinate
     *
     * @param zoomLevel the zoom level
     * @return the distance
     * @author Jason Huntley
     */
    public static double getDistance(int x1, int y1, int x2, int y2, int zoomLevel) {
        double la1 = YToLat(y1, zoomLevel);
        double lo1 = XToLon(x1, zoomLevel);
        double la2 = YToLat(y2, zoomLevel);
        double lo2 = XToLon(x2, zoomLevel);

        return getDistance(la1, lo1, la2, lo2);
    }

    /**
     * Gets the distance using Spherical law of cosines.
     *
     * @param la1 the Latitude in degrees
     * @param lo1 the Longitude in degrees
     * @param la2 the Latitude from 2nd coordinate in degrees
     * @param lo2 the Longitude from 2nd coordinate in degrees
     * @return the distance
     * @author Jason Huntley
     */
    public static double getDistance(double la1, double lo1, double la2, double lo2) {
        double aStartLat = Math.toRadians(la1);
        double aStartLong = Math.toRadians(lo1);
        double aEndLat =Math.toRadians(la2);
        double aEndLong = Math.toRadians(lo2);

        double distance = Math.acos(Math.sin(aStartLat) * Math.sin(aEndLat)
                + Math.cos(aStartLat) * Math.cos(aEndLat)
                * Math.cos(aEndLong - aStartLong));

        return (EARTH_RADIUS * distance);
    }

    /**
     * Transform longitude to pixelspace
     *
     * <p>
     * Mathematical optimization<br>
     * <code>
     * x = radius(aZoomlevel) * toRadians(aLongitude) + falseEasting(aZoomLevel)<br>
     * x = getMaxPixels(aZoomlevel) / (2 * PI) * (aLongitude * PI) / 180 + getMaxPixels(aZoomlevel) / 2<br>
     * x = getMaxPixels(aZoomlevel) * aLongitude / 360 + 180 * getMaxPixels(aZoomlevel) / 360<br>
     * x = getMaxPixels(aZoomlevel) * (aLongitude + 180) / 360<br>
     * </code>
     * </p>
     *
     * @param aLongitude
     *            [-180..180]
     * @return [0..2^Zoomlevel*TILE_SIZE[
     * @author Jan Peter Stotz
     */
    public static double LonToX(double aLongitude, int aZoomlevel) {
        int mp = getMaxPixels(aZoomlevel);
        double x = (mp * (aLongitude + 180l)) / 360l;
        return Math.min(x, mp - 1);
    }

    /**
     * Transforms latitude to pixelspace
     * <p>
     * Mathematical optimization<br>
     * <code>
     * log(u) := log((1.0 + sin(toRadians(aLat))) / (1.0 - sin(toRadians(aLat))<br>
     *
     * y = -1 * (radius(aZoomlevel) / 2 * log(u)))) - falseNorthing(aZoomlevel))<br>
     * y = -1 * (getMaxPixel(aZoomlevel) / 2 * PI / 2 * log(u)) - -1 * getMaxPixel(aZoomLevel) / 2<br>
     * y = getMaxPixel(aZoomlevel) / (-4 * PI) * log(u)) + getMaxPixel(aZoomLevel) / 2<br>
     * y = getMaxPixel(aZoomlevel) * ((log(u) / (-4 * PI)) + 1/2)<br>
     * </code>
     * </p>
     * @param aLat
     *            [-90...90]
     * @return [0..2^Zoomlevel*TILE_SIZE[
     * @author Jan Peter Stotz
     */
    public static double LatToY(double aLat, int aZoomlevel) {
        if (aLat < MIN_LAT)
            aLat = MIN_LAT;
        else if (aLat > MAX_LAT)
            aLat = MAX_LAT;
        double sinLat = Math.sin(Math.toRadians(aLat));
        double log = Math.log((1.0 + sinLat) / (1.0 - sinLat));
        int mp = getMaxPixels(aZoomlevel);
        double y = mp * (0.5 - (log / (4.0 * Math.PI)));
        return Math.min(y, mp - 1);
    }

    /**
     * Transforms pixel coordinate X to longitude
     *
     * <p>
     * Mathematical optimization<br>
     * <code>
     * lon = toDegree((aX - falseEasting(aZoomlevel)) / radius(aZoomlevel))<br>
     * lon = 180 / PI * ((aX - getMaxPixels(aZoomlevel) / 2) / getMaxPixels(aZoomlevel) / (2 * PI)<br>
     * lon = 180 * ((aX - getMaxPixels(aZoomlevel) / 2) / getMaxPixels(aZoomlevel))<br>
     * lon = 360 / getMaxPixels(aZoomlevel) * (aX - getMaxPixels(aZoomlevel) / 2)<br>
     * lon = 360 * aX / getMaxPixels(aZoomlevel) - 180<br>
     * </code>
     * </p>
     * @param aX
     *            [0..2^Zoomlevel*TILE_WIDTH[
     * @return ]-180..180[
     * @author Jan Peter Stotz
     */
    public static double XToLon(int aX, int aZoomlevel) {
        return ((360d * aX) / getMaxPixels(aZoomlevel)) - 180.0;
    }

    /**
     * Transforms pixel coordinate Y to latitude
     *
     * @param aY
     *            [0..2^Zoomlevel*TILE_WIDTH[
     * @return [MIN_LAT..MAX_LAT] is about [-85..85]
     */
    public static double YToLat(int aY, int aZoomlevel) {
        aY += falseNorthing(aZoomlevel);
        double latitude = (Math.PI / 2) - (2 * Math.atan(Math.exp(-1.0 * aY / radius(aZoomlevel))));
        return -1 * Math.toDegrees(latitude);
    }

}
