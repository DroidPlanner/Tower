// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

/**
 * Holds one map tile. Additionally the code for loading the tile image and
 * painting it is also included in this class.
 *
 * @author Jan Peter Stotz
 */
public class Tile {

    /**
     * Hourglass image that is displayed until a map tile has been loaded
     */
    public static BufferedImage LOADING_IMAGE;
    public static BufferedImage ERROR_IMAGE;

    static {
        try {
            LOADING_IMAGE = ImageIO.read(JMapViewer.class.getResourceAsStream("images/hourglass.png"));
            ERROR_IMAGE = ImageIO.read(JMapViewer.class.getResourceAsStream("images/error.png"));
        } catch (Exception e1) {
            LOADING_IMAGE = null;
            ERROR_IMAGE = null;
        }
    }

    protected TileSource source;
    protected int xtile;
    protected int ytile;
    protected int zoom;
    protected BufferedImage image;
    protected String key;
    protected boolean loaded = false;
    protected boolean loading = false;
    protected boolean error = false;
    protected String error_message;

    /** TileLoader-specific tile metadata */
    protected Map<String, String> metadata;

    /**
     * Creates a tile with empty image.
     *
     * @param source
     * @param xtile
     * @param ytile
     * @param zoom
     */
    public Tile(TileSource source, int xtile, int ytile, int zoom) {
        super();
        this.source = source;
        this.xtile = xtile;
        this.ytile = ytile;
        this.zoom = zoom;
        this.image = LOADING_IMAGE;
        this.key = getTileKey(source, xtile, ytile, zoom);
    }

    public Tile(TileSource source, int xtile, int ytile, int zoom, BufferedImage image) {
        this(source, xtile, ytile, zoom);
        this.image = image;
    }

    /**
     * Tries to get tiles of a lower or higher zoom level (one or two level
     * difference) from cache and use it as a placeholder until the tile has
     * been loaded.
     */
    public void loadPlaceholderFromCache(TileCache cache) {
        BufferedImage tmpImage = new BufferedImage(source.getTileSize(), source.getTileSize(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) tmpImage.getGraphics();
        // g.drawImage(image, 0, 0, null);
        for (int zoomDiff = 1; zoomDiff < 5; zoomDiff++) {
            // first we check if there are already the 2^x tiles
            // of a higher detail level
            int zoom_high = zoom + zoomDiff;
            if (zoomDiff < 3 && zoom_high <= JMapViewer.MAX_ZOOM) {
                int factor = 1 << zoomDiff;
                int xtile_high = xtile << zoomDiff;
                int ytile_high = ytile << zoomDiff;
                double scale = 1.0 / factor;
                g.setTransform(AffineTransform.getScaleInstance(scale, scale));
                int paintedTileCount = 0;
                for (int x = 0; x < factor; x++) {
                    for (int y = 0; y < factor; y++) {
                        Tile tile = cache.getTile(source, xtile_high + x, ytile_high + y, zoom_high);
                        if (tile != null && tile.isLoaded()) {
                            paintedTileCount++;
                            tile.paint(g, x * source.getTileSize(), y * source.getTileSize());
                        }
                    }
                }
                if (paintedTileCount == factor * factor) {
                    image = tmpImage;
                    return;
                }
            }

            int zoom_low = zoom - zoomDiff;
            if (zoom_low >= JMapViewer.MIN_ZOOM) {
                int xtile_low = xtile >> zoomDiff;
                int ytile_low = ytile >> zoomDiff;
                int factor = (1 << zoomDiff);
                double scale = factor;
                AffineTransform at = new AffineTransform();
                int translate_x = (xtile % factor) * source.getTileSize();
                int translate_y = (ytile % factor) * source.getTileSize();
                at.setTransform(scale, 0, 0, scale, -translate_x, -translate_y);
                g.setTransform(at);
                Tile tile = cache.getTile(source, xtile_low, ytile_low, zoom_low);
                if (tile != null && tile.isLoaded()) {
                    tile.paint(g, 0, 0);
                    image = tmpImage;
                    return;
                }
            }
        }
    }

    public TileSource getSource() {
        return source;
    }

    /**
     * @return tile number on the x axis of this tile
     */
    public int getXtile() {
        return xtile;
    }

    /**
     * @return tile number on the y axis of this tile
     */
    public int getYtile() {
        return ytile;
    }

    /**
     * @return zoom level of this tile
     */
    public int getZoom() {
        return zoom;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void loadImage(InputStream input) throws IOException {
        image = ImageIO.read(input);
    }

    /**
     * @return key that identifies a tile
     */
    public String getKey() {
        return key;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getUrl() throws IOException {
        return source.getTileUrl(zoom, xtile, ytile);
    }

    /**
     * Paints the tile-image on the {@link Graphics} <code>g</code> at the
     * position <code>x</code>/<code>y</code>.
     *
     * @param g
     * @param x
     *            x-coordinate in <code>g</code>
     * @param y
     *            y-coordinate in <code>g</code>
     */
    public void paint(Graphics g, int x, int y) {
        if (image == null)
            return;
        g.drawImage(image, x, y, null);
    }

    @Override
    public String toString() {
        return "Tile " + key;
    }

    /**
     * Note that the hash code does not include the {@link #source}.
     * Therefore a hash based collection can only contain tiles
     * of one {@link #source}.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + xtile;
        result = prime * result + ytile;
        result = prime * result + zoom;
        return result;
    }

    /**
     * Compares this object with <code>obj</code> based on
     * the fields {@link #xtile}, {@link #ytile} and
     * {@link #zoom}.
     * The {@link #source} field is ignored.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tile other = (Tile) obj;
        if (xtile != other.xtile)
            return false;
        if (ytile != other.ytile)
            return false;
        if (zoom != other.zoom)
            return false;
        return true;
    }

    public static String getTileKey(TileSource source, int xtile, int ytile, int zoom) {
        return zoom + "/" + xtile + "/" + ytile + "@" + source.getName();
    }

    public String getStatus() {
        if (this.error)
            return "error";
        if (this.loaded)
            return "loaded";
        if (this.loading)
            return "loading";
        return "new";
    }

    public boolean hasError() {
        return error;
    }

    public String getErrorMessage() {
        return error_message;
    }

    public void setError(String message) {
        error = true;
        setImage(ERROR_IMAGE);
        error_message = message;
    }

    /**
     * Puts the given key/value pair to the metadata of the tile.
     * If value is null, the (possibly existing) key/value pair is removed from
     * the meta data.
     *
     * @param key
     * @param value
     */
    public void putValue(String key, String value) {
        if (value == null || value.isEmpty()) {
            if (metadata != null) {
                metadata.remove(key);
            }
            return;
        }
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    public String getValue(String key) {
        if (metadata == null) return null;
        return metadata.get(key);
    }

    public Map<String,String> getMetadata() {
        return metadata;
    }

    public void initLoading() {
        loaded = false;
        error = false;
        loading = true;
    }

    public void finishLoading() {
        loading = false;
        loaded = true;
    }
}
