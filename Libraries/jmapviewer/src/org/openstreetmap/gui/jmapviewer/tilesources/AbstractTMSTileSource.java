// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tilesources;

import java.io.IOException;

import org.openstreetmap.gui.jmapviewer.OsmMercator;

public abstract class AbstractTMSTileSource extends AbstractTileSource {

    protected String name;
    protected String baseUrl;

    public AbstractTMSTileSource(String name, String base_url) {
        this.name = name;
        this.baseUrl = base_url;
        if(baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0,baseUrl.length()-1);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMaxZoom() {
        return 21;
    }

    @Override
    public int getMinZoom() {
        return 0;
    }

    public String getExtension() {
        return "png";
    }

    /**
     * @throws IOException when subclass cannot return the tile URL
     */
    public String getTilePath(int zoom, int tilex, int tiley) throws IOException {
        return "/" + zoom + "/" + tilex + "/" + tiley + "." + getExtension();
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    @Override
    public String getTileUrl(int zoom, int tilex, int tiley) throws IOException {
        return this.getBaseUrl() + getTilePath(zoom, tilex, tiley);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getTileType() {
        return "png";
    }

    /*
     * Most tilesources use OsmMercator projection.
     */
    @Override
    public int getTileSize() {
        return OsmMercator.TILE_SIZE;
    }

    @Override
    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        return OsmMercator.getDistance(lat1, lon1, lat2, lon2);
    }

    @Override
    public int LonToX(double lon, int zoom) {
        return (int )OsmMercator.LonToX(lon, zoom);
    }

    @Override
    public int LatToY(double lat, int zoom) {
        return (int )OsmMercator.LatToY(lat, zoom);
    }

    @Override
    public double XToLon(int x, int zoom) {
        return OsmMercator.XToLon(x, zoom);
    }

    @Override
    public double YToLat(int y, int zoom) {
        return OsmMercator.YToLat(y, zoom);
    }

    @Override
    public double latToTileY(double lat, int zoom) {
        return OsmMercator.LatToY(lat, zoom) / OsmMercator.TILE_SIZE;
    }

    @Override
    public double lonToTileX(double lon, int zoom) {
        return OsmMercator.LonToX(lon, zoom) / OsmMercator.TILE_SIZE;
    }

    @Override
    public double tileYToLat(int y, int zoom) {
        return OsmMercator.YToLat(y * OsmMercator.TILE_SIZE, zoom);
    }

    @Override
    public double tileXToLon(int x, int zoom) {
        return OsmMercator.XToLon(x * OsmMercator.TILE_SIZE, zoom);
    }
}
