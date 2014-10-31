// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.gui.jmapviewer.interfaces.TileJob;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;

/**
 * A {@link TileLoader} implementation that loads tiles from OSM.
 *
 * @author Jan Peter Stotz
 */
public class OsmTileLoader implements TileLoader {

    /**
     * Holds the HTTP headers. Insert e.g. User-Agent here when default should not be used.
     */
    public Map<String, String> headers = new HashMap<>();

    public int timeoutConnect = 0;
    public int timeoutRead = 0;

    protected TileLoaderListener listener;

    public OsmTileLoader(TileLoaderListener listener) {
        headers.put("Accept", "text/html, image/png, image/jpeg, image/gif, */*");
        this.listener = listener;
    }

    public TileJob createTileLoaderJob(final Tile tile) {
        return new TileJob() {

            InputStream input = null;

            public void run() {
                synchronized (tile) {
                    if ((tile.isLoaded() && !tile.hasError()) || tile.isLoading())
                        return;
                    tile.loaded = false;
                    tile.error = false;
                    tile.loading = true;
                }
                try {
                    URLConnection conn = loadTileFromOsm(tile);
                    loadTileMetadata(tile, conn);
                    if ("no-tile".equals(tile.getValue("tile-info"))) {
                        tile.setError("No tile at this zoom level");
                    } else {
                        input = conn.getInputStream();
                        try {
                            tile.loadImage(input);
                        } finally {
                            input.close();
                            input = null;
                        }
                    }
                    tile.setLoaded(true);
                    listener.tileLoadingFinished(tile, true);
                } catch (Exception e) {
                    tile.setError(e.getMessage());
                    listener.tileLoadingFinished(tile, false);
                    if (input == null) {
                        try {
                            System.err.println("Failed loading " + tile.getUrl() +": " + e.getMessage());
                        } catch(IOException i) {
                        }
                    }
                } finally {
                    tile.loading = false;
                    tile.setLoaded(true);
                }
            }

            public Tile getTile() {
                return tile;
            }
        };
    }

    protected URLConnection loadTileFromOsm(Tile tile) throws IOException {
        URL url;
        url = new URL(tile.getUrl());
        URLConnection urlConn = url.openConnection();
        if (urlConn instanceof HttpURLConnection) {
            prepareHttpUrlConnection((HttpURLConnection)urlConn);
        }
        urlConn.setReadTimeout(30000); // 30 seconds read timeout
        return urlConn;
    }

    protected void loadTileMetadata(Tile tile, URLConnection urlConn) {
        String str = urlConn.getHeaderField("X-VE-TILEMETA-CaptureDatesRange");
        if (str != null) {
            tile.putValue("capture-date", str);
        }
        str = urlConn.getHeaderField("X-VE-Tile-Info");
        if (str != null) {
            tile.putValue("tile-info", str);
        }
    }

    protected void prepareHttpUrlConnection(HttpURLConnection urlConn) {
        for(Entry<String, String> e : headers.entrySet()) {
            urlConn.setRequestProperty(e.getKey(), e.getValue());
        }
        if(timeoutConnect != 0)
            urlConn.setConnectTimeout(timeoutConnect);
        if(timeoutRead != 0)
            urlConn.setReadTimeout(timeoutRead);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
