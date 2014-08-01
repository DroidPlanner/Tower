// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.interfaces;

import java.io.IOException;

import org.openstreetmap.gui.jmapviewer.JMapViewer;

/**
 *
 * @author Jan Peter Stotz
 */
public interface TileSource extends Attributed {

    /**
     * Specifies the different mechanisms for detecting updated tiles
     * respectively only download newer tiles than those stored locally.
     *
     * <ul>
     * <li>{@link #IfNoneMatch} Server provides ETag header entry for all tiles
     * and <b>supports</b> conditional download via <code>If-None-Match</code>
     * header entry.</li>
     * <li>{@link #ETag} Server provides ETag header entry for all tiles but
     * <b>does not support</b> conditional download via
     * <code>If-None-Match</code> header entry.</li>
     * <li>{@link #IfModifiedSince} Server provides Last-Modified header entry
     * for all tiles and <b>supports</b> conditional download via
     * <code>If-Modified-Since</code> header entry.</li>
     * <li>{@link #LastModified} Server provides Last-Modified header entry for
     * all tiles but <b>does not support</b> conditional download via
     * <code>If-Modified-Since</code> header entry.</li>
     * <li>{@link #None} The server does not support any of the listed
     * mechanisms.</li>
     * </ul>
     *
     */
    public enum TileUpdate {
        IfNoneMatch, ETag, IfModifiedSince, LastModified, None
    }

    /**
     * Specifies the maximum zoom value. The number of zoom levels is [0..
     * {@link #getMaxZoom()}].
     *
     * @return maximum zoom value that has to be smaller or equal to
     *         {@link JMapViewer#MAX_ZOOM}
     */
    int getMaxZoom();

    /**
     * Specifies the minimum zoom value. This value is usually 0.
     * Only for maps that cover a certain region up to a limited zoom level
     * this method should return a value different than 0.
     *
     * @return minimum zoom value - usually 0
     */
    int getMinZoom();

    /**
     * @return The supported tile update mechanism
     * @see TileUpdate
     */
    TileUpdate getTileUpdate();

    /**
     * A tile layer name has to be unique and has to consist only of characters
     * valid for filenames.
     *
     * @return Name of the tile layer
     */
    String getName();

    /**
     * Constructs the tile url.
     *
     * @param zoom
     * @param tilex
     * @param tiley
     * @return fully qualified url for downloading the specified tile image
     */
    String getTileUrl(int zoom, int tilex, int tiley) throws IOException;

    /**
     * Specifies the tile image type. For tiles rendered by Mapnik or
     * Osmarenderer this is usually <code>"png"</code>.
     *
     * @return file extension of the tile image type
     */
    String getTileType();

    /**
     * Specifies how large each tile is.
     * @return The size of a single tile in pixels.
     */
    int getTileSize();

    /**
     * Gets the distance using Spherical law of cosines.
     *  @return the distance, m.
     */
    double getDistance(double la1, double lo1, double la2, double lo2);

    /**
     * Transform longitude to pixelspace.
     * @return [0..2^Zoomlevel*TILE_SIZE[
     */
    int LonToX(double aLongitude, int aZoomlevel);

    /**
     * Transforms latitude to pixelspace.
     * @return [0..2^Zoomlevel*TILE_SIZE[
     */
    int LatToY(double aLat, int aZoomlevel);

    /**
     * Transforms pixel coordinate X to longitude
     * @return ]-180..180[
     */
    double XToLon(int aX, int aZoomlevel);

    /**
     * Transforms pixel coordinate Y to latitude.
     * @return [MIN_LAT..MAX_LAT]
     */
    double YToLat(int aY, int aZoomlevel);

    /**
     * Transforms longitude to X tile coordinate.
     * @return [0..2^Zoomlevel[
     */
    double lonToTileX(double lon, int zoom);

    /**
     * Transforms latitude to Y tile coordinate.
     * @return [0..2^Zoomlevel[
     */
    double latToTileY(double lat, int zoom);

    /**
     * Transforms tile X coordinate to longitude.
     * @return ]-180..180[
     */
    double tileXToLon(int x, int zoom);

    /**
     * Transforms tile Y coordinate to latitude.
     * @return [MIN_LAT..MAX_LAT]
     */
    double tileYToLat(int y, int zoom);
}
