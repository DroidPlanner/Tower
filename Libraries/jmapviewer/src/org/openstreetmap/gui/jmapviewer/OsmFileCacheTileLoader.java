// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.gui.jmapviewer.interfaces.CachedTileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileClearController;
import org.openstreetmap.gui.jmapviewer.interfaces.TileJob;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource.TileUpdate;

/**
 * A {@link TileLoader} implementation that loads tiles from OSM via HTTP and
 * saves all loaded files in a directory located in the temporary directory.
 * If a tile is present in this file cache it will not be loaded from OSM again.
 *
 * @author Jan Peter Stotz
 * @author Stefan Zeller
 */
public class OsmFileCacheTileLoader extends OsmTileLoader implements CachedTileLoader {

    private static final Logger log = Logger.getLogger(OsmFileCacheTileLoader.class.getName());

    private static final String ETAG_FILE_EXT = ".etag";
    private static final String TAGS_FILE_EXT = ".tags";

    private static final Charset TAGS_CHARSET = Charset.forName("UTF-8");

    public static final long FILE_AGE_ONE_DAY = 1000 * 60 * 60 * 24;
    public static final long FILE_AGE_ONE_WEEK = FILE_AGE_ONE_DAY * 7;

    protected String cacheDirBase;

    protected final Map<TileSource, File> sourceCacheDirMap;

    protected long maxCacheFileAge = FILE_AGE_ONE_WEEK;
    protected long recheckAfter = FILE_AGE_ONE_DAY;

    public static File getDefaultCacheDir() throws SecurityException {
        String tempDir = null;
        String userName = System.getProperty("user.name");
        try {
            tempDir = System.getProperty("java.io.tmpdir");
        } catch (SecurityException e) {
            log.log(Level.WARNING,
                    "Failed to access system property ''java.io.tmpdir'' for security reasons. Exception was: "
                    + e.toString());
            throw e; // rethrow
        }
        try {
            if (tempDir == null)
                throw new IOException("No temp directory set");
            String subDirName = "JMapViewerTiles";
            // On Linux/Unix systems we do not have a per user tmp directory.
            // Therefore we add the user name for getting a unique dir name.
            if (userName != null && userName.length() > 0) {
                subDirName += "_" + userName;
            }
            File cacheDir = new File(tempDir, subDirName);
            return cacheDir;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Create a OSMFileCacheTileLoader with given cache directory.
     * If cacheDir is not set or invalid, IOException will be thrown.
     * @param map the listener checking for tile load events (usually the map for display)
     * @param cacheDir directory to store cached tiles
     */
    public OsmFileCacheTileLoader(TileLoaderListener map, File cacheDir) throws IOException  {
        super(map);
        if (cacheDir == null || (!cacheDir.exists() && !cacheDir.mkdirs()))
            throw new IOException("Cannot access cache directory");

        log.finest("Tile cache directory: " + cacheDir);
        cacheDirBase = cacheDir.getAbsolutePath();
        sourceCacheDirMap = new HashMap<>();
    }

    /**
     * Create a OSMFileCacheTileLoader with system property temp dir.
     * If not set an IOException will be thrown.
     * @param map the listener checking for tile load events (usually the map for display)
     */
    public OsmFileCacheTileLoader(TileLoaderListener map) throws SecurityException, IOException {
        this(map, getDefaultCacheDir());
    }

    @Override
    public TileJob createTileLoaderJob(final Tile tile) {
        return new FileLoadJob(tile);
    }

    protected File getSourceCacheDir(TileSource source) {
        File dir = sourceCacheDirMap.get(source);
        if (dir == null) {
            dir = new File(cacheDirBase, source.getName().replaceAll("[\\\\/:*?\"<>|]", "_"));
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        return dir;
    }

    protected class FileLoadJob implements TileJob {
        InputStream input = null;

        Tile tile;
        File tileCacheDir;
        File tileFile = null;
        long fileAge = 0;
        boolean fileTilePainted = false;

        public FileLoadJob(Tile tile) {
            this.tile = tile;
        }

        @Override
        public Tile getTile() {
            return tile;
        }

        @Override
        public void run() {
            synchronized (tile) {
                if ((tile.isLoaded() && !tile.hasError()) || tile.isLoading())
                    return;
                tile.loaded = false;
                tile.error = false;
                tile.loading = true;
            }
            tileCacheDir = getSourceCacheDir(tile.getSource());
            if (loadTileFromFile()) {
                return;
            }
            if (fileTilePainted) {
                TileJob job = new TileJob() {

                    @Override
                    public void run() {
                        loadOrUpdateTile();
                    }
                    @Override
                    public Tile getTile() {
                        return tile;
                    }
                };
                JobDispatcher.getInstance().addJob(job);
            } else {
                loadOrUpdateTile();
            }
        }

        protected void loadOrUpdateTile() {
            try {
                URLConnection urlConn = loadTileFromOsm(tile);
                if (tileFile != null) {
                    switch (tile.getSource().getTileUpdate()) {
                    case IfModifiedSince:
                        urlConn.setIfModifiedSince(fileAge);
                        break;
                    case LastModified:
                        if (!isOsmTileNewer(fileAge)) {
                            log.finest("LastModified test: local version is up to date: " + tile);
                            tile.setLoaded(true);
                            tileFile.setLastModified(System.currentTimeMillis() - maxCacheFileAge + recheckAfter);
                            return;
                        }
                        break;
                    }
                }
                if (tile.getSource().getTileUpdate() == TileUpdate.ETag || tile.getSource().getTileUpdate() == TileUpdate.IfNoneMatch) {
                    String fileETag = tile.getValue("etag");
                    if (fileETag != null) {
                        switch (tile.getSource().getTileUpdate()) {
                        case IfNoneMatch:
                            urlConn.addRequestProperty("If-None-Match", fileETag);
                            break;
                        case ETag:
                            if (hasOsmTileETag(fileETag)) {
                                tile.setLoaded(true);
                                tileFile.setLastModified(System.currentTimeMillis() - maxCacheFileAge
                                        + recheckAfter);
                                return;
                            }
                        }
                    }
                    tile.putValue("etag", urlConn.getHeaderField("ETag"));
                }
                if (urlConn instanceof HttpURLConnection && ((HttpURLConnection)urlConn).getResponseCode() == 304) {
                    // If we are isModifiedSince or If-None-Match has been set
                    // and the server answers with a HTTP 304 = "Not Modified"
                    log.finest("ETag test: local version is up to date: " + tile);
                    tile.setLoaded(true);
                    tileFile.setLastModified(System.currentTimeMillis() - maxCacheFileAge + recheckAfter);
                    return;
                }

                loadTileMetadata(tile, urlConn);
                saveTagsToFile();

                if ("no-tile".equals(tile.getValue("tile-info")))
                {
                    tile.setError("No tile at this zoom level");
                    listener.tileLoadingFinished(tile, true);
                } else {
                    for(int i = 0; i < 5; ++i) {
                        if (urlConn instanceof HttpURLConnection && ((HttpURLConnection)urlConn).getResponseCode() == 503) {
                            Thread.sleep(5000+(new Random()).nextInt(5000));
                            continue;
                        }
                        byte[] buffer = loadTileInBuffer(urlConn);
                        if (buffer != null) {
                            tile.loadImage(new ByteArrayInputStream(buffer));
                            tile.setLoaded(true);
                            listener.tileLoadingFinished(tile, true);
                            saveTileToFile(buffer);
                            break;
                        }
                    }
                }
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

        protected boolean loadTileFromFile() {
            try {
                tileFile = getTileFile();
                if (!tileFile.exists())
                    return false;

                loadTagsFromFile();
                if ("no-tile".equals(tile.getValue("tile-info"))) {
                    tile.setError("No tile at this zoom level");
                    if (tileFile.exists()) {
                        tileFile.delete();
                    }
                    tileFile = getTagsFile();
                } else {
                    try (FileInputStream fin = new FileInputStream(tileFile)) {
                        if (fin.available() == 0)
                            throw new IOException("File empty");
                        tile.loadImage(fin);
                    }
                }

                fileAge = tileFile.lastModified();
                boolean oldTile = System.currentTimeMillis() - fileAge > maxCacheFileAge;
                if (!oldTile) {
                    tile.setLoaded(true);
                    listener.tileLoadingFinished(tile, true);
                    fileTilePainted = true;
                    return true;
                }
                listener.tileLoadingFinished(tile, true);
                fileTilePainted = true;
            } catch (Exception e) {
                tileFile.delete();
                tileFile = null;
                fileAge = 0;
            }
            return false;
        }

        protected byte[] loadTileInBuffer(URLConnection urlConn) throws IOException {
            input = urlConn.getInputStream();
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream(input.available());
                byte[] buffer = new byte[2048];
                boolean finished = false;
                do {
                    int read = input.read(buffer);
                    if (read >= 0) {
                        bout.write(buffer, 0, read);
                    } else {
                        finished = true;
                    }
                } while (!finished);
                if (bout.size() == 0)
                    return null;
                return bout.toByteArray();
            } finally {
                input.close();
                input = null;
            }
        }

        /**
         * Performs a <code>HEAD</code> request for retrieving the
         * <code>LastModified</code> header value.
         *
         * Note: This does only work with servers providing the
         * <code>LastModified</code> header:
         * <ul>
         * <li>{@link org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource.CycleMap} - supported</li>
         * <li>{@link org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource.Mapnik} - not supported</li>
         * </ul>
         *
         * @param fileAge time of the
         * @return <code>true</code> if the tile on the server is newer than the
         *         file
         * @throws IOException
         */
        protected boolean isOsmTileNewer(long fileAge) throws IOException {
            URL url;
            url = new URL(tile.getUrl());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            prepareHttpUrlConnection(urlConn);
            urlConn.setRequestMethod("HEAD");
            urlConn.setReadTimeout(30000); // 30 seconds read timeout
            // System.out.println("Tile age: " + new
            // Date(urlConn.getLastModified()) + " / "
            // + new Date(fileAge));
            long lastModified = urlConn.getLastModified();
            if (lastModified == 0)
                return true; // no LastModified time returned
            return (lastModified > fileAge);
        }

        protected boolean hasOsmTileETag(String eTag) throws IOException {
            URL url;
            url = new URL(tile.getUrl());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            prepareHttpUrlConnection(urlConn);
            urlConn.setRequestMethod("HEAD");
            urlConn.setReadTimeout(30000); // 30 seconds read timeout
            // System.out.println("Tile age: " + new
            // Date(urlConn.getLastModified()) + " / "
            // + new Date(fileAge));
            String osmETag = urlConn.getHeaderField("ETag");
            if (osmETag == null)
                return true;
            return (osmETag.equals(eTag));
        }

        protected File getTileFile() {
            return new File(tileCacheDir + "/" + tile.getZoom() + "_" + tile.getXtile() + "_" + tile.getYtile() + "."
                    + tile.getSource().getTileType());
        }

        protected File getTagsFile() {
            return new File(tileCacheDir + "/" + tile.getZoom() + "_" + tile.getXtile() + "_" + tile.getYtile()
                    + TAGS_FILE_EXT);
        }

        protected void saveTileToFile(byte[] rawData) {
            try (
                FileOutputStream f = new FileOutputStream(tileCacheDir + "/" + tile.getZoom() + "_" + tile.getXtile()
                        + "_" + tile.getYtile() + "." + tile.getSource().getTileType())
            ) {
                f.write(rawData);
            } catch (Exception e) {
                System.err.println("Failed to save tile content: " + e.getLocalizedMessage());
            }
        }

        protected void saveTagsToFile() {
            File tagsFile = getTagsFile();
            if (tile.getMetadata() == null) {
                tagsFile.delete();
                return;
            }
            try (PrintWriter f = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tagsFile), TAGS_CHARSET))) {
                for (Entry<String, String> entry : tile.getMetadata().entrySet()) {
                    f.println(entry.getKey() + "=" + entry.getValue());
                }
            } catch (Exception e) {
                System.err.println("Failed to save tile tags: " + e.getLocalizedMessage());
            }
        }

        /** Load backward-compatiblity .etag file and if it exists move it to new .tags file*/
        private void loadOldETagfromFile() {
            File etagFile = new File(tileCacheDir, tile.getZoom() + "_"
                    + tile.getXtile() + "_" + tile.getYtile() + ETAG_FILE_EXT);
            if (!etagFile.exists()) return;
            try (FileInputStream f = new FileInputStream(etagFile)) {
                byte[] buf = new byte[f.available()];
                f.read(buf);
                String etag = new String(buf, TAGS_CHARSET.name());
                tile.putValue("etag", etag);
                if (etagFile.delete()) {
                    saveTagsToFile();
                }
            } catch (IOException e) {
                System.err.println("Failed to load compatiblity etag: " + e.getLocalizedMessage());
            }
        }

        protected void loadTagsFromFile() {
            loadOldETagfromFile();
            File tagsFile = getTagsFile();
            try (BufferedReader f = new BufferedReader(new InputStreamReader(new FileInputStream(tagsFile), TAGS_CHARSET))) {
                for (String line = f.readLine(); line != null; line = f.readLine()) {
                    final int i = line.indexOf('=');
                    if (i == -1 || i == 0) {
                        System.err.println("Malformed tile tag in file '" + tagsFile.getName() + "':" + line);
                        continue;
                    }
                    tile.putValue(line.substring(0,i),line.substring(i+1));
                }
            } catch (FileNotFoundException e) {
            } catch (Exception e) {
                System.err.println("Failed to load tile tags: " + e.getLocalizedMessage());
            }
        }
    }

    public long getMaxFileAge() {
        return maxCacheFileAge;
    }

    /**
     * Sets the maximum age of the local cached tile in the file system. If a
     * local tile is older than the specified file age
     * {@link OsmFileCacheTileLoader} will connect to the tile server and check
     * if a newer tile is available using the mechanism specified for the
     * selected tile source/server.
     *
     * @param maxFileAge
     *            maximum age in milliseconds
     * @see #FILE_AGE_ONE_DAY
     * @see #FILE_AGE_ONE_WEEK
     * @see TileSource#getTileUpdate()
     */
    public void setCacheMaxFileAge(long maxFileAge) {
        this.maxCacheFileAge = maxFileAge;
    }

    public String getCacheDirBase() {
        return cacheDirBase;
    }

    public void setTileCacheDir(String tileCacheDir) {
        File dir = new File(tileCacheDir);
        dir.mkdirs();
        this.cacheDirBase = dir.getAbsolutePath();
    }

    @Override
    public void clearCache(TileSource source) {
        clearCache(source, null);
    }

    @Override
    public void clearCache(TileSource source, TileClearController controller) {
        File dir = getSourceCacheDir(source);
        if (dir != null) {
            if (controller != null) controller.initClearDir(dir);
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (controller != null) controller.initClearFiles(files);
                for (File file : files) {
                    if (controller != null && controller.cancel()) return;
                    file.delete();
                    if (controller != null) controller.fileDeleted(file);
                }
            }
            dir.delete();
        }
        if (controller != null) controller.clearFinished();
    }
}
