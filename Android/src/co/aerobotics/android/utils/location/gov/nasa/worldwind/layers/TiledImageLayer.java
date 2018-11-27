/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.layers;

import android.graphics.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.cache.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.globes.Earth;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.render.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.retrieve.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;
import org.w3c.dom.*;

import javax.xml.xpath.XPath;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author dcollins
 * @version $Id$
 */
// TODO: implement isAtMaxResolution for Android.
// TODO: apply layer opacity during rendering
public abstract class TiledImageLayer extends AbstractLayer implements Tile.TileFactory
{
    protected static final double DEFAULT_DETAIL_HINT_ORIGIN = 2.8;
    protected static final int DEFAULT_REQUEST_QUEUE_SIZE = 200;

    protected LevelSet levels;
    protected double detailHintOrigin = DEFAULT_DETAIL_HINT_ORIGIN;
    protected double detailHint;
    protected List<Tile> topLevelTiles = new ArrayList<Tile>();
    protected GpuTextureFactory textureFactory;

    protected boolean forceLevelZeroLoads = false;
    protected boolean levelZeroLoaded = false;
    protected boolean retainLevelZeroTiles = false;
    protected String tileCountName;
    protected boolean useMipMaps = true;
    protected boolean useTransparentTextures = false;
    protected ArrayList<String> supportedImageFormats = new ArrayList<String>();
    protected String textureFormat;

    // Stuff computed each frame
    protected List<GpuTextureTile> currentTiles = new ArrayList<GpuTextureTile>();
    protected GpuTextureTile currentAncestorTile;
    protected boolean atMaxResolution = false;
    protected PriorityBlockingQueue<Runnable> requestQ = new PriorityBlockingQueue<Runnable>(
        DEFAULT_REQUEST_QUEUE_SIZE);

    abstract protected void requestTile(DrawContext dc, GpuTextureTile tile);

    abstract protected void forceTextureLoad(GpuTextureTile tile);

    public TiledImageLayer(LevelSet levelSet)
    {
        if (levelSet == null)
        {
            String message = Logging.getMessage("nullValue.LevelSetIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        this.levels = new LevelSet(levelSet); // the caller's levelSet may change internally, so we copy it.
        this.setValue(AVKey.SECTOR, this.levels.getSector());

        this.setPickEnabled(false); // textures are assumed to be terrain unless specifically indicated otherwise.
        this.tileCountName = this.getName() + " Tiles";

        this.textureFactory = this.createTextureFactory();
    }

    @Override
    public Object setValue(String key, Object value)
    {
        // Offer it to the level set
        if (this.getLevels() != null)
            this.getLevels().setValue(key, value);

        return super.setValue(key, value);
    }

    @Override
    public Object getValue(String key)
    {
        Object value = super.getValue(key);

        return value != null ? value : this.getLevels().getValue(key); // see if the level set has it
    }

    public boolean isForceLevelZeroLoads()
    {
        return this.forceLevelZeroLoads;
    }

    public void setForceLevelZeroLoads(boolean forceLevelZeroLoads)
    {
        this.forceLevelZeroLoads = forceLevelZeroLoads;
    }

    public boolean isRetainLevelZeroTiles()
    {
        return retainLevelZeroTiles;
    }

    public void setRetainLevelZeroTiles(boolean retainLevelZeroTiles)
    {
        this.retainLevelZeroTiles = retainLevelZeroTiles;
    }

    /**
     * Indicates the layer's detail hint, which is described in {@link #setDetailHint(double)}.
     *
     * @return the detail hint
     *
     * @see #setDetailHint(double)
     */
    public double getDetailHint()
    {
        return this.detailHint;
    }

    /**
     * Modifies the default relationship of image resolution to screen resolution as the viewing altitude changes.
     * Values greater than 0 cause imagery to appear at higher resolution at greater altitudes than normal, but at an
     * increased performance cost. Values less than 0 decrease the default resolution at any given altitude. The default
     * value is 0. Values typically range between -0.5 and 0.5.
     * <p/>
     * Note: The resolution-to-height relationship is defined by a scale factor that specifies the approximate size of
     * discernible lengths in the image relative to eye distance. The scale is specified as a power of 10. A value of 3,
     * for example, specifies that 1 meter on the surface should be distinguishable from an altitude of 10^3 meters
     * (1000 meters). The default scale is 1/10^2.8, (1 over 10 raised to the power 2.8). The detail hint specifies
     * deviations from that default. A detail hint of 0.2 specifies a scale of 1/1000, i.e., 1/10^(2.8 + .2) = 1/10^3.
     * Scales much larger than 3 typically cause the applied resolution to be higher than discernible for the altitude.
     * Such scales significantly decrease performance.
     *
     * @param detailHint the degree to modify the default relationship of image resolution to screen resolution with
     *                   changing view altitudes. Values greater than 1 increase the resolution. Values less than zero
     *                   decrease the resolution. The default value is 0.
     */
    public void setDetailHint(double detailHint)
    {
        this.detailHint = detailHint;
    }

    protected LevelSet getLevels()
    {
        return levels;
    }

    protected PriorityBlockingQueue<Runnable> getRequestQ()
    {
        return requestQ;
    }

    @Override
    public boolean isMultiResolution()
    {
        return this.getLevels() != null && this.getLevels().getNumLevels() > 1;
    }

    /**
     * Returns the format used to store images in texture memory, or null if images are stored in their native format.
     *
     * @return the texture image format; null if images are stored in their native format.
     *
     * @see #setTextureFormat(String)
     */
    public String getTextureFormat()
    {
        return this.textureFormat;
    }

    /**
     * Specifies the format used to store images in texture memory, or null to store images in their native format.
     * Suppported texture formats are as follows: <ul> <li><code>image/dds</code> - Stores images in the compressed DDS
     * format. If the image is already in DDS format it's stored as-is.</li> </ul>
     *
     * @param textureFormat the texture image format; null to store images in their native format.
     */
    public void setTextureFormat(String textureFormat)
    {
        this.textureFormat = textureFormat;
    }

    public boolean isUseMipMaps()
    {
        return useMipMaps;
    }

    public void setUseMipMaps(boolean useMipMaps)
    {
        this.useMipMaps = useMipMaps;
    }

    public boolean isUseTransparentTextures()
    {
        return this.useTransparentTextures;
    }

    public void setUseTransparentTextures(boolean useTransparentTextures)
    {
        this.useTransparentTextures = useTransparentTextures;
    }

    /**
     * Specifies the time of the layer's most recent dataset update, beyond which cached data is invalid. If greater
     * than zero, the layer ignores and eliminates any in-memory or on-disk cached data older than the time specified,
     * and requests new information from the data source. If zero, the default, the layer applies any expiry times
     * associated with its individual levels, but only for on-disk cached data. In-memory cached data is expired only
     * when the expiry time is specified with this method and is greater than zero. This method also overwrites the
     * expiry times of the layer's individual levels if the value specified to the method is greater than zero.
     *
     * @param expiryTime the expiry time of any cached data, expressed as a number of milliseconds beyond the epoch. The
     *                   default expiry time is zero.
     *
     * @see System#currentTimeMillis() for a description of milliseconds beyond the epoch.
     */
    @Override
    public void setExpiryTime(long expiryTime) // Override this method to use intrinsic level-specific expiry times
    {
        super.setExpiryTime(expiryTime);

        if (expiryTime > 0)
            this.levels.setExpiryTime(expiryTime); // remove this in sub-class to use level-specific expiry times
    }

    public List<Tile> getTopLevels()
    {
        if (this.topLevelTiles == null)
            this.createTopLevelTiles();

        return topLevelTiles;
    }

    protected GpuTextureFactory createTextureFactory()
    {
        return (GpuTextureFactory) WorldWind.createConfigurationComponent(AVKey.GPU_TEXTURE_FACTORY);
    }

    protected void checkTextureExpiration(DrawContext dc, List<GpuTextureTile> tiles)
    {
        for (GpuTextureTile tile : tiles)
        {
            if (tile.isTextureExpired())
                this.requestTile(dc, tile);
        }
    }

    protected void sendRequests()
    {
        Runnable task = this.requestQ.poll();
        while (task != null)
        {
            if (!WorldWind.getTaskService().isFull())
            {
                WorldWind.getTaskService().runTask(task);
            }
            task = this.requestQ.poll();
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to return <code>false</code> when this layer's LevelSet is entirely outside of the current visible
     * sector. This provides an effective way to cull the entire layer before it performs any unnecessary work.
     */
    @Override
    public boolean isLayerInView(DrawContext dc)
    {
        return dc.getVisibleSector() == null || dc.getVisibleSector().intersects(this.levels.getSector());
    }

    // ============== Rendering ======================= //
    // ============== Rendering ======================= //
    // ============== Rendering ======================= //

    @Override
    protected void doRender(DrawContext dc)
    {
        if (this.forceLevelZeroLoads && !this.levelZeroLoaded)
            this.loadAllTopLevelTextures(dc);

        if (dc.getSurfaceGeometry() == null || dc.getSurfaceGeometry().size() < 1)
            return;

        this.assembleTiles(dc);

        if (!this.currentTiles.isEmpty())
        {
            // TODO: apply opacity and transparent texture support

            dc.addPerFrameStatistic(PerformanceStatistic.IMAGE_TILE_COUNT, this.tileCountName,
                this.currentTiles.size());
            dc.getSurfaceTileRenderer().renderTiles(dc, this.currentTiles);

            // Check texture expiration. Memory-cached textures are checked for expiration only when an explicit,
            // non-zero expiry time has been set for the layer. If none has been set, the expiry times of the layer's
            // individual levels are used, but only for images in the local file cache, not textures in memory. This is
            // to avoid incurring the overhead of checking expiration of in-memory textures, a very rarely used feature.
            if (this.getExpiryTime() > 0 && this.getExpiryTime() < System.currentTimeMillis())
                this.checkTextureExpiration(dc, this.currentTiles);

            this.currentTiles.clear();
        }

        this.sendRequests();
        this.requestQ.clear();

        // TODO: clear fallback tiles
    }

    public GpuTextureTile createTile(Sector sector, Level level, int row, int column)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (level == null)
        {
            String msg = Logging.getMessage("nullValue.LevelIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (row < 0)
        {
            String msg = Logging.getMessage("generic.RowIndexOutOfRange", row);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (column < 0)
        {
            String msg = Logging.getMessage("generic.ColumnIndexOutOfRange", column);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return new GpuTextureTile(sector, level, row, column, this.getTextureTileCache(), this.textureFactory);
    }

    protected void loadAllTopLevelTextures(DrawContext dc)
    {
        for (Tile tile : this.getTopLevels())
        {
            if (tile instanceof GpuTextureTile)
            {
                GpuTextureTile textureTile = (GpuTextureTile) tile;
                if (!textureTile.isTextureInMemory(dc.getGpuResourceCache()))
                {
                    this.forceTextureLoad(textureTile);
                }
            }
        }

        this.levelZeroLoaded = true;
    }

    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //

    protected void assembleTiles(DrawContext dc)
    {
        this.currentTiles.clear();

        if (this.topLevelTiles.isEmpty())
            this.createTopLevelTiles();

        for (Tile tile : this.topLevelTiles)
        {
            this.updateTileExtent(dc, (GpuTextureTile) tile);
            this.currentAncestorTile = null;

            if (this.isTileVisible(dc, (GpuTextureTile) tile))
                this.addTileOrDescendants(dc, (GpuTextureTile) tile);
        }
    }

    protected void createTopLevelTiles()
    {
        if (this.levels.getFirstLevel() == null)
        {
            Logging.warning(Logging.getMessage("generic.FirstLevelIsNull"));
            return;
        }

        this.topLevelTiles.clear();
        Tile.createTilesForLevel(this.levels.getFirstLevel(), this.levels.getSector(), this, this.topLevelTiles);
    }

    protected void addTileOrDescendants(DrawContext dc, GpuTextureTile tile)
    {
        this.updateTileExtent(dc, tile);

        if (this.meetsRenderCriteria(dc, tile))
        {
            this.addTile(dc, tile);
            return;
        }

        // The incoming tile does not meet the rendering criteria, so it must be subdivided and those subdivisions
        // tested against the criteria.

        // All tiles that meet the selection criteria are drawn, but some of those tiles will not have textures
        // associated with them because their texture isn't loaded yet. In this case the tiles use the texture of the
        // closest ancestor that has a texture loaded. The ancestor is called the currentAncestorTile. A texture
        // transform is applied during rendering to align the sector's texture coordinates with the appropriate region
        // of the ancestor's texture.

        MemoryCache cache = this.getTextureTileCache();
        GpuTextureTile ancestorTile = null;

        try
        {
            if (tile.isTextureInMemory(dc.getGpuResourceCache()) || tile.getLevelNumber() == 0)
            {
                ancestorTile = this.currentAncestorTile;
                this.currentAncestorTile = tile;
            }

            Tile[] subTiles = tile.subdivide(this.levels.getLevel(tile.getLevelNumber() + 1), cache, this);
            for (Tile child : subTiles)
            {
                // Put all sub-tiles in the terrain tile cache to avoid repeatedly allocating them each frame. Top level
                // tiles are not cached because they are held in the topLevelTiles list. Sub tiles are placed in the
                // cache here, and updated when their terrain geometry changes.
                if (!cache.contains(child.getTileKey()))
                    cache.put(child.getTileKey(), child);

                // Add descendant tiles that intersect the LevelSet's sector and are visible. If half or more of this
                // tile (in either latitude or longitude) extends beyond the LevelSet's sector, then two or three of its
                // children will be entirely outside the LevelSet's sector.
                if (this.levels.getSector().intersects(child.getSector())
                    && this.isTileVisible(dc, (GpuTextureTile) child))
                {
                    this.addTileOrDescendants(dc, (GpuTextureTile) child);
                }
            }
        }
        finally
        {
            if (ancestorTile != null)
                this.currentAncestorTile = ancestorTile;
        }
    }

    protected void addTile(DrawContext dc, GpuTextureTile tile)
    {
        tile.setFallbackTile(null);

        // If this tile's level is empty, just ignore it. When the view moves closer to the tile it is subdivided and
        // an non-empty child level is eventually added.
        if (tile.getLevel().isEmpty())
            return;

        // If the tile's texture is in memory, add it to the list of current tiles and return.
        if (tile.isTextureInMemory(dc.getGpuResourceCache()))
        {
            this.currentTiles.add(tile);
            return;
        }

        // The tile's texture is not in memory. Issue a request for the texture data if the tile is not already marked
        // as an absent resource. We ignore absent resources to avoid flooding the system with requests for resources
        // that are never resolved.
        if (!this.levels.isResourceAbsent(tile))
            this.requestTile(dc, tile);

        if (this.currentAncestorTile != null)
        {
            // If the current ancestor tile's texture is in memory, then use it as this tile's fallback tile and add
            // this tile to the list of current tiles. Otherwise, we check if the ancestor tile is a level zero tile and
            // if so issue a request to load it into memory. This is critical to correctly handling the case when an
            // application is resumed with the view close to the globe. In that case, the level zero tiles are never
            // initially loaded and the tile that meets the render criteria may have no data. By issuing a request for
            // level zero ancestor tiles, we ensure that something displays when the application resumes.

            if (this.currentAncestorTile.isTextureInMemory(dc.getGpuResourceCache()))
            {
                tile.setFallbackTile(this.currentAncestorTile);
                this.currentTiles.add(tile);
            }
            else if (this.currentAncestorTile.getLevelNumber() == 0)
            {
                if (!this.levels.isResourceAbsent(this.currentAncestorTile))
                    this.requestTile(dc, this.currentAncestorTile);
            }
        }
    }

    protected boolean isTileVisible(DrawContext dc, GpuTextureTile tile)
    {
        // TODO: compute extent every frame or periodically update
        if (tile.getExtent() == null)
            tile.setExtent(this.computeTileExtent(dc, tile));

        Sector visibleSector = dc.getVisibleSector();
        Extent extent = tile.getExtent();

        return (visibleSector == null || visibleSector.intersects(tile.getSector()))
            && (extent == null || dc.getView().getFrustumInModelCoordinates().intersects(extent));
    }

    protected boolean meetsRenderCriteria(DrawContext dc, GpuTextureTile tile)
    {
        return this.levels.isFinalLevel(tile.getLevelNumber()) || !this.needToSubdivide(dc, tile);
    }

    protected boolean needToSubdivide(DrawContext dc, GpuTextureTile tile)
    {
        return tile.mustSubdivide(dc, this.getDetailFactor());
    }

    @Override
    public Double getMinEffectiveAltitude(Double radius)
    {
        if (radius == null)
            radius = Earth.WGS84_EQUATORIAL_RADIUS;

        // Get the cell size for the highest-resolution level.
        double texelSize = this.getLevels().getLastLevel().getTexelSize();
        double cellHeight = radius * texelSize;

        // Compute altitude associated with the cell height at which it would switch if it had higher-res levels.
        return cellHeight * Math.pow(10, this.getDetailFactor());
    }

    @Override
    public Double getMaxEffectiveAltitude(Double radius)
    {
        if (radius == null)
            radius = Earth.WGS84_EQUATORIAL_RADIUS;

        // Find first non-empty level. Compute altitude at which it comes into effect.
        for (int i = 0; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
        {
            if (this.levels.isLevelEmpty(i))
                continue;

            // Compute altitude associated with the cell height at which it would switch if it had a lower-res level.
            // That cell height is twice that of the current lowest-res level.
            double texelSize = this.levels.getLevel(i).getTexelSize();
            double cellHeight = 2 * radius * texelSize;

            return cellHeight * Math.pow(10, this.getDetailFactor());
        }

        return null;
    }

    protected double getDetailFactor()
    {
        return this.detailHintOrigin + this.detailHint;
    }

    protected void updateTileExtent(DrawContext dc, GpuTextureTile tile)
    {
        // TODO: regenerate the tile extent and reference points whenever the underlying elevation model changes.
        // TODO: regenerate the tile extent and reference points whenever the vertical exaggeration changes.

        if (tile.getExtent() == null)
        {
            tile.setExtent(this.computeTileExtent(dc, tile));
        }

        // Update the tile's reference points.
        Vec4[] points = tile.getReferencePoints();
        if (points == null)
        {
            points = new Vec4[] {new Vec4(), new Vec4(), new Vec4(), new Vec4(), new Vec4()};
            tile.getSector().computeCornerPoints(dc.getGlobe(), dc.getVerticalExaggeration(), points);
            tile.getSector().computeCentroidPoint(dc.getGlobe(), dc.getVerticalExaggeration(), points[4]);
            tile.setReferencePoints(points);
        }
    }

    protected Extent computeTileExtent(DrawContext dc, GpuTextureTile tile)
    {
        return Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), tile.getSector());
    }

    /**
     * Returns the memory cache used to cache texture tiles, initializing the cache if it doesn't yet exist.
     *
     * @return the memory cache associated with texture tiles.
     */
    protected MemoryCache getTextureTileCache()
    {
        if (!WorldWind.getMemoryCacheSet().contains(GpuTextureTile.class.getName()))
        {
            long size = Configuration.getLongValue(AVKey.GPU_TEXTURE_TILE_CACHE_SIZE);
            MemoryCache cache = new BasicMemoryCache((long) (0.8 * size), size);
            cache.setName("Texture Tiles");
            WorldWind.getMemoryCacheSet().put(GpuTextureTile.class.getName(), cache);
        }

        return WorldWind.getMemoryCacheSet().get(GpuTextureTile.class.getName());
    }

    //**************************************************************//
    //********************  Configuration  *************************//
    //**************************************************************//

    /**
     * Creates a configuration document for a TiledImageLayer described by the specified params. The returned document
     * may be used as a construction parameter to {@link BasicTiledImageLayer}.
     *
     * @param params parameters describing the TiledImageLayer.
     *
     * @return a configuration document for the TiledImageLayer.
     */
    public static Document createTiledImageLayerConfigDocument(AVList params)
    {
        Document doc = WWXML.createDocumentBuilder(true).newDocument();

        Element root = WWXML.setDocumentElement(doc, "Layer");
        WWXML.setIntegerAttribute(root, "version", 1);
        WWXML.setTextAttribute(root, "layerType", "TiledImageLayer");

        createTiledImageLayerConfigElements(params, root);

        return doc;
    }

    /**
     * Appends TiledImageLayer configuration parameters as elements to the specified context. This appends elements for
     * the following parameters: <table> <tr><th>Parameter</th><th>Element Path</th><th>Type</th></tr> <tr><td>{@link
     * AVKey#SERVICE_NAME}</td><td>Service/@serviceName</td><td>String</td></tr> <tr><td>{@link
     * AVKey#IMAGE_FORMAT}</td><td>ImageFormat</td><td>String</td></tr> <tr><td>{@link
     * AVKey#AVAILABLE_IMAGE_FORMATS}</td><td>AvailableImageFormats/ImageFormat</td><td>String array</td></tr>
     * <tr><td>{@link AVKey#FORCE_LEVEL_ZERO_LOADS}</td><td>ForceLevelZeroLoads</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#RETAIN_LEVEL_ZERO_TILES}</td><td>RetainLevelZeroTiles</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#TEXTURE_FORMAT}</td><td>TextureFormat</td><td>String</td></tr> <tr><td>{@link
     * AVKey#USE_MIP_MAPS}</td><td>UseMipMaps</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#USE_TRANSPARENT_TEXTURES}</td><td>UseTransparentTextures</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#URL_CONNECT_TIMEOUT}</td><td>RetrievalTimeouts/ConnectTimeout/Time</td><td>Integer milliseconds</td></tr>
     * <tr><td>{@link AVKey#URL_READ_TIMEOUT}</td><td>RetrievalTimeouts/ReadTimeout/Time</td><td>Integer
     * milliseconds</td></tr> <tr><td>{@link AVKey#RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT}</td>
     * <td>RetrievalTimeouts/StaleRequestLimit/Time</td><td>Integer milliseconds</td></tr> </table> This also writes
     * common layer and LevelSet configuration parameters by invoking {@link gov.nasa.worldwind.layers.AbstractLayer#createLayerConfigElements(gov.nasa.worldwind.avlist.AVList,
     * org.w3c.dom.Element)} and {@link DataConfigurationUtils#createLevelSetConfigElements(gov.nasa.worldwind.avlist.AVList,
     * org.w3c.dom.Element)}.
     *
     * @param params  the key-value pairs which define the TiledImageLayer configuration parameters.
     * @param context the XML document root on which to append TiledImageLayer configuration elements.
     *
     * @return a reference to context.
     *
     * @throws IllegalArgumentException if either the parameters or the context are null.
     */
    public static Element createTiledImageLayerConfigElements(AVList params, Element context)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        XPath xpath = WWXML.makeXPath();

        // Common layer properties.
        createLayerConfigElements(params, context);

        // LevelSet properties.
        DataConfigurationUtils.createLevelSetConfigElements(params, context);

        // Service properties.
        // Try to get the SERVICE_NAME property, but default to "WWTileService".
        String s = AVListImpl.getStringValue(params, AVKey.SERVICE_NAME, "WWTileService");
        if (s != null && s.length() > 0)
        {
            // The service element may already exist, in which case we want to append to it.
            Element el = WWXML.getElement(context, "Service", xpath);
            if (el == null)
                el = WWXML.appendElementPath(context, "Service");
            WWXML.setTextAttribute(el, "serviceName", s);
        }

        WWXML.checkAndAppendBooleanElement(params, AVKey.RETRIEVE_PROPERTIES_FROM_SERVICE, context,
            "RetrievePropertiesFromService");

        // Image format properties.
        WWXML.checkAndAppendTextElement(params, AVKey.IMAGE_FORMAT, context, "ImageFormat");
        WWXML.checkAndAppendTextElement(params, AVKey.TEXTURE_FORMAT, context, "TextureFormat");

        Object o = params.getValue(AVKey.AVAILABLE_IMAGE_FORMATS);
        if (o != null && o instanceof String[])
        {
            String[] strings = (String[]) o;
            if (strings.length > 0)
            {
                // The available image formats element may already exists, in which case we want to append to it, rather
                // than create entirely separate paths.
                Element el = WWXML.getElement(context, "AvailableImageFormats", xpath);
                if (el == null)
                    el = WWXML.appendElementPath(context, "AvailableImageFormats");
                WWXML.appendTextArray(el, "ImageFormat", strings);
            }
        }

        // Optional behavior properties.
        WWXML.checkAndAppendBooleanElement(params, AVKey.FORCE_LEVEL_ZERO_LOADS, context, "ForceLevelZeroLoads");
        WWXML.checkAndAppendBooleanElement(params, AVKey.RETAIN_LEVEL_ZERO_TILES, context, "RetainLevelZeroTiles");
        WWXML.checkAndAppendBooleanElement(params, AVKey.USE_MIP_MAPS, context, "UseMipMaps");
        WWXML.checkAndAppendBooleanElement(params, AVKey.USE_TRANSPARENT_TEXTURES, context, "UseTransparentTextures");
        WWXML.checkAndAppendDoubleElement(params, AVKey.DETAIL_HINT, context, "DetailHint");

        // Retrieval properties.
        if (params.getValue(AVKey.URL_CONNECT_TIMEOUT) != null ||
            params.getValue(AVKey.URL_READ_TIMEOUT) != null ||
            params.getValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT) != null)
        {
            Element el = WWXML.getElement(context, "RetrievalTimeouts", xpath);
            if (el == null)
                el = WWXML.appendElementPath(context, "RetrievalTimeouts");

            WWXML.checkAndAppendTimeElement(params, AVKey.URL_CONNECT_TIMEOUT, el, "ConnectTimeout/Time");
            WWXML.checkAndAppendTimeElement(params, AVKey.URL_READ_TIMEOUT, el, "ReadTimeout/Time");
            WWXML.checkAndAppendTimeElement(params, AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, el,
                "StaleRequestLimit/Time");
        }

        return context;
    }

    /**
     * Parses TiledImageLayer configuration parameters from the specified DOM document. This writes output as key-value
     * pairs to params. If a parameter from the XML document already exists in params, that parameter is ignored.
     * Supported key and parameter names are: <table> <tr><th>Parameter</th><th>Element Path</th><th>Type</th></tr>
     * <tr><td>{@link AVKey#SERVICE_NAME}</td><td>Service/@serviceName</td><td>String</td></tr> <tr><td>{@link
     * AVKey#IMAGE_FORMAT}</td><td>ImageFormat</td><td>String</td></tr> <tr><td>{@link
     * AVKey#AVAILABLE_IMAGE_FORMATS}</td><td>AvailableImageFormats/ImageFormat</td><td>String array</td></tr>
     * <tr><td>{@link AVKey#FORCE_LEVEL_ZERO_LOADS}</td><td>ForceLevelZeroLoads</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#RETAIN_LEVEL_ZERO_TILES}</td><td>RetainLevelZeroTiles</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#TEXTURE_FORMAT}</td><td>TextureFormat</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#USE_MIP_MAPS}</td><td>UseMipMaps</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#USE_TRANSPARENT_TEXTURES}</td><td>UseTransparentTextures</td><td>Boolean</td></tr> <tr><td>{@link
     * AVKey#URL_CONNECT_TIMEOUT}</td><td>RetrievalTimeouts/ConnectTimeout/Time</td><td>Integer milliseconds</td></tr>
     * <tr><td>{@link AVKey#URL_READ_TIMEOUT}</td><td>RetrievalTimeouts/ReadTimeout/Time</td><td>Integer
     * milliseconds</td></tr> <tr><td>{@link AVKey#RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT}</td>
     * <td>RetrievalTimeouts/StaleRequestLimit/Time</td><td>Integer milliseconds</td></tr> </table> This also parses
     * common layer and LevelSet configuration parameters by invoking {@link gov.nasa.worldwind.layers.AbstractLayer#getLayerConfigParams(org.w3c.dom.Element,
     * gov.nasa.worldwind.avlist.AVList)} and {@link gov.nasa.worldwind.util.DataConfigurationUtils#getLevelSetConfigParams(org.w3c.dom.Element,
     * gov.nasa.worldwind.avlist.AVList)}.
     *
     * @param domElement the XML document root to parse for TiledImageLayer configuration parameters.
     * @param params     the output key-value pairs which receive the TiledImageLayer configuration parameters. A null
     *                   reference is permitted.
     *
     * @return a reference to params, or a new AVList if params is null.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    public static AVList getTiledImageLayerConfigParams(Element domElement, AVList params)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
            params = new AVListImpl();

        XPath xpath = WWXML.makeXPath();

        // Common layer properties.
        getLayerConfigParams(domElement, params);

        // LevelSet properties.
        DataConfigurationUtils.getLevelSetConfigParams(domElement, params);

        // Service properties.
        WWXML.checkAndSetStringParam(domElement, params, AVKey.SERVICE_NAME, "Service/@serviceName", xpath);
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.RETRIEVE_PROPERTIES_FROM_SERVICE,
            "RetrievePropertiesFromService", xpath);

        // Image format properties.
        WWXML.checkAndSetStringParam(domElement, params, AVKey.IMAGE_FORMAT, "ImageFormat", xpath);
        WWXML.checkAndSetStringParam(domElement, params, AVKey.TEXTURE_FORMAT, "TextureFormat", xpath);
        WWXML.checkAndSetUniqueStringsParam(domElement, params, AVKey.AVAILABLE_IMAGE_FORMATS,
            "AvailableImageFormats/ImageFormat", xpath);

        // Optional behavior properties.
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.FORCE_LEVEL_ZERO_LOADS, "ForceLevelZeroLoads", xpath);
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.RETAIN_LEVEL_ZERO_TILES, "RetainLevelZeroTiles", xpath);
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.USE_MIP_MAPS, "UseMipMaps", xpath);
        WWXML.checkAndSetBooleanParam(domElement, params, AVKey.USE_TRANSPARENT_TEXTURES, "UseTransparentTextures",
            xpath);
        WWXML.checkAndSetDoubleParam(domElement, params, AVKey.DETAIL_HINT, "DetailHint", xpath);
        //WWXML.checkAndSetColorArrayParam(domElement, params, AVKey.TRANSPARENCY_COLORS, "TransparencyColors/Color",
        //    xpath);

        // Retrieval properties. Convert the Long time values to Integers, because BasicTiledImageLayer is expecting
        // Integer values.
        WWXML.checkAndSetTimeParamAsInteger(domElement, params, AVKey.URL_CONNECT_TIMEOUT,
            "RetrievalTimeouts/ConnectTimeout/Time", xpath);
        WWXML.checkAndSetTimeParamAsInteger(domElement, params, AVKey.URL_READ_TIMEOUT,
            "RetrievalTimeouts/ReadTimeout/Time", xpath);
        WWXML.checkAndSetTimeParamAsInteger(domElement, params, AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT,
            "RetrievalTimeouts/StaleRequestLimit/Time", xpath);

        // Parse the legacy configuration parameters. This enables TiledImageLayer to recognize elements from previous
        // versions of configuration documents.
        getLegacyTiledImageLayerConfigParams(domElement, params);

        return params;
    }

    /**
     * Parses TiledImageLayer configuration parameters from previous versions of configuration documents. This writes
     * output as key-value pairs to params. If a parameter from the XML document already exists in params, that
     * parameter is ignored. Supported key and parameter names are: <table> <tr><th>Parameter</th><th>Element
     * Path</th><th>Type</th></tr> <tr><td>{@link AVKey#TEXTURE_FORMAT}</td><td>CompressTextures</td><td>"image/dds" if
     * CompressTextures is "true"; null otherwise</td></tr> </table>
     *
     * @param domElement the XML document root to parse for legacy TiledImageLayer configuration parameters.
     * @param params     the output key-value pairs which receive the TiledImageLayer configuration parameters. A null
     *                   reference is permitted.
     *
     * @return a reference to params, or a new AVList if params is null.
     *
     * @throws IllegalArgumentException if the document is null.
     */
    protected static AVList getLegacyTiledImageLayerConfigParams(Element domElement, AVList params)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
            params = new AVListImpl();

        XPath xpath = WWXML.makeXPath();

        Object o = params.getValue(AVKey.TEXTURE_FORMAT);
        if (o == null)
        {
            Boolean b = WWXML.getBoolean(domElement, "CompressTextures", xpath);
            if (b != null && b)
                params.setValue(AVKey.TEXTURE_FORMAT, "image/dds");
        }

        return params;
    }

    // ============== Image Composition ======================= //
    // ============== Image Composition ======================= //
    // ============== Image Composition ======================= //

    public List<String> getAvailableImageFormats()
    {
        return new ArrayList<String>(this.supportedImageFormats);
    }

    public boolean isImageFormatAvailable(String imageFormat)
    {
        return imageFormat != null && this.supportedImageFormats.contains(imageFormat);
    }

    public String getDefaultImageFormat()
    {
        return !this.supportedImageFormats.isEmpty() ? this.supportedImageFormats.get(0) : null;
    }

    protected void setAvailableImageFormats(String[] formats)
    {
        this.supportedImageFormats.clear();

        if (formats != null)
            this.supportedImageFormats.addAll(Arrays.asList(formats));
    }

    protected Bitmap requestImage(GpuTextureTile tile, String mimeType)
        throws URISyntaxException, InterruptedIOException, MalformedURLException
    {
        String pathBase = tile.getPathBase();
        String suffix = WWIO.makeSuffixForMimeType(mimeType);
        String path = pathBase + suffix;
        File f = new File(path);
        URL url;
        if (f.isAbsolute() && f.exists())
            url = f.toURI().toURL();
        else
            url = this.getDataFileStore().findFile(path, false);

        if (url == null) // image is not local
            return null;

        if (WWIO.isFileOutOfDate(url, tile.getLevel().getExpiryTime()))
        {
            // The file has expired. Delete it.
            this.getDataFileStore().removeFile(url);
            String message = Logging.getMessage("generic.DataFileExpired", url);
            Logging.verbose(message);
        }
        else
        {
            File imageFile = new File(url.toURI());
            Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            if (image == null)
            {
                // Assume that something is wrong with the file and delete it.
                this.getDataFileStore().removeFile(url);
                this.levels.markResourceAbsent(tile);
                Logging.info(Logging.getMessage("generic.DeletedCorruptDataFile", url));

                String message = Logging.getMessage("generic.ImageReadFailed", imageFile);
                throw new RuntimeException(message);
            }

            this.levels.unmarkResourceAbsent(tile);
            return image;
        }

        return null;
    }

    protected void downloadImage(GpuTextureTile tile, String mimeType, int timeout) throws Exception
    {
        if (this.getValue(AVKey.RETRIEVER_FACTORY_LOCAL) != null)
            this.retrieveLocalImage(tile, mimeType, timeout);
        else
            // Assume it's remote.
            this.retrieveRemoteImage(tile, mimeType, timeout);
    }

    protected void retrieveRemoteImage(final GpuTextureTile tile, String mimeType, int timeout) throws Exception
    {
        // TODO: apply retriever-factory pattern for remote retrieval case.
        final URL resourceURL = tile.getResourceURL(mimeType);
        if (resourceURL == null)
            return;

        Retriever retriever;

        String protocol = resourceURL.getProtocol();

        if ("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol))
        {
            retriever = new HTTPRetriever(resourceURL, new CompositionRetrievalPostProcessor(tile));
            retriever.setValue(URLRetriever.EXTRACT_ZIP_ENTRY, "true"); // supports legacy layers
        }
        else
        {
            String message = Logging.getMessage("layers.TextureLayer.UnknownRetrievalProtocol", resourceURL);
            throw new RuntimeException(message);
        }

        Logging.verbose("Retrieving " + resourceURL.toString());
        retriever.setConnectTimeout(10000);
        retriever.setReadTimeout(timeout);
        retriever.call();
    }

    protected void retrieveLocalImage(GpuTextureTile tile, String mimeType, int timeout) throws Exception
    {
        if (!WorldWind.getLocalRetrievalService().isAvailable())
            return;

        RetrieverFactory retrieverFactory = (RetrieverFactory) this.getValue(AVKey.RETRIEVER_FACTORY_LOCAL);
        if (retrieverFactory == null)
            return;

        AVListImpl avList = new AVListImpl();
        avList.setValue(AVKey.SECTOR, tile.getSector());
        avList.setValue(AVKey.WIDTH, tile.getWidth());
        avList.setValue(AVKey.HEIGHT, tile.getHeight());
        avList.setValue(AVKey.FILE_NAME, tile.getPath());
        avList.setValue(AVKey.IMAGE_FORMAT, mimeType);

        Retriever retriever = retrieverFactory.createRetriever(avList, new CompositionRetrievalPostProcessor(tile));

        Logging.verbose("Locally retrieving " + tile.getPath());
        retriever.setReadTimeout(timeout);
        retriever.call();
    }

    public int computeLevelForResolution(Sector sector, double resolution)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(message);
            throw new IllegalStateException(message);
        }

        // Find the first level exceeding the desired resolution
        double texelSize;
        Level targetLevel = this.levels.getLastLevel();
        for (int i = 0; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
        {
            if (this.levels.isLevelEmpty(i))
                continue;

            texelSize = this.levels.getLevel(i).getTexelSize();
            if (texelSize > resolution)
                continue;

            targetLevel = this.levels.getLevel(i);
            break;
        }

        // Choose the level closest to the resolution desired
        if (targetLevel.getLevelNumber() != 0 && !this.levels.isLevelEmpty(targetLevel.getLevelNumber() - 1))
        {
            Level nextLowerLevel = this.levels.getLevel(targetLevel.getLevelNumber() - 1);
            double dless = Math.abs(nextLowerLevel.getTexelSize() - resolution);
            double dmore = Math.abs(targetLevel.getTexelSize() - resolution);
            if (dless < dmore)
                targetLevel = nextLowerLevel;
        }

        Logging.verbose(Logging.getMessage("layers.TiledImageLayer.LevelSelection",
            targetLevel.getLevelNumber(), Double.toString(targetLevel.getTexelSize())));
        return targetLevel.getLevelNumber();
    }

    /**
     * Create an image for the portion of this layer lying within a specified sector. The image is created at a
     * specified aspect ratio within a canvas of a specified size. This returns the specified image if this layer has no
     * content in the specified sector.
     *
     * @param sector       the sector of interest.
     * @param canvasWidth  the width of the canvas.
     * @param canvasHeight the height of the canvas.
     * @param aspectRatio  the aspect ratio, width/height, of the window. If the aspect ratio is greater or equal to
     *                     one, the full width of the canvas is used for the image; the height used is proportional to
     *                     the inverse of the aspect ratio. If the aspect ratio is less than one, the full height of the
     *                     canvas is used, and the width used is proportional to the aspect ratio.
     * @param levelNumber  the target level of the tiled image layer.
     * @param mimeType     the type of image to create, e.g., "png" and "jpg".
     * @param abortOnError indicates whether to stop assembling the image if an error occurs. If false, processing
     *                     continues until all portions of the layer that intersect the specified sector have been added
     *                     to the image. Portions for which an error occurs will be blank.
     * @param image        if non-null, a {@link Bitmap} in which to place the image. If null, a new buffered image is
     *                     created. The image must be the width and height specified in the <code>canvasWidth</code> and
     *                     <code>canvasHeight</code> arguments.
     * @param timeout      The amount of time to allow for reading the image from the server.
     *
     * @return image        the assembled image, of size indicated by the <code>canvasWidth</code> and
     *         <code>canvasHeight</code>. If the specified aspect ratio is one, all pixels contain values. If the aspect
     *         ratio is greater than one, a full-width segment along the top of the canvas is blank. If the aspect ratio
     *         is less than one, a full-height segment along the right side of the canvas is blank. If the
     *         <code>image</code> argument was non-null, that buffered image is returned.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null.
     * @see ImageUtil#mergeImage(gov.nasa.worldwind.geom.Sector, gov.nasa.worldwind.geom.Sector, double,
     *      android.graphics.Bitmap, android.graphics.Bitmap)
     */
    public Bitmap composeImageForSector(Sector sector, int canvasWidth, int canvasHeight, double aspectRatio,
        int levelNumber, String mimeType, boolean abortOnError, Bitmap image, int timeout) throws Exception
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.levels.getSector().intersects(sector))
        {
            Logging.error(Logging.getMessage("generic.SectorRequestedOutsideCoverageArea", sector,
                this.levels.getSector()));
            return image;
        }

        Sector intersection = this.levels.getSector().intersection(sector);

        if (levelNumber < 0)
        {
            levelNumber = this.levels.getLastLevel().getLevelNumber();
        }
        else if (levelNumber > this.levels.getLastLevel().getLevelNumber())
        {
            Logging.warning(Logging.getMessage("generic.LevelRequestedGreaterThanMaxLevel",
                levelNumber, this.levels.getLastLevel().getLevelNumber()));
            levelNumber = this.levels.getLastLevel().getLevelNumber();
        }

        int numTiles = 0;
        GpuTextureTile[][] tiles = this.getTilesInSector(intersection, levelNumber);
        for (GpuTextureTile[] row : tiles)
        {
            numTiles += row.length;
        }

        if (tiles.length == 0 || tiles[0].length == 0)
        {
            Logging.error(Logging.getMessage("layers.TiledImageLayer.NoImagesAvailable"));
            return image;
        }

        if (image == null)
            image = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);

        double tileCount = 0;
        for (GpuTextureTile[] row : tiles)
        {
            for (GpuTextureTile tile : row)
            {
                if (tile == null)
                    continue;

                Bitmap tileImage;
                try
                {
                    tileImage = this.getImage(tile, mimeType, timeout);
                    if (Thread.currentThread().isInterrupted())
                    {
                        throw new InterruptedException();
                    }

                    if (tileImage != null)
                        ImageUtil.mergeImage(sector, tile.getSector(), aspectRatio, tileImage, image);

                    this.firePropertyChange(AVKey.PROGRESS, tileCount / numTiles, ++tileCount / numTiles);
                }
                catch (InterruptedException e)
                {
                    throw e;
                }
                catch (InterruptedIOException e)
                {
                    throw e;
                }
                catch (Exception e)
                {
                    if (abortOnError)
                        throw e;

                    String message = Logging.getMessage("generic.ExceptionWhileRequestingImage", tile.getPath());
                    Logging.warning(message, e);
                }
            }
        }

        return image;
    }

    public long countImagesInSector(Sector sector)
    {
        long count = 0;
        for (int i = 0; i <= this.getLevels().getLastLevel().getLevelNumber(); i++)
        {
            if (!this.levels.isLevelEmpty(i))
                count += countImagesInSector(sector, i);
        }
        return count;
    }

    public long countImagesInSector(Sector sector, int levelNumber)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Level targetLevel = this.levels.getLastLevel();
        if (levelNumber >= 0)
        {
            for (int i = levelNumber; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
            {
                if (this.levels.isLevelEmpty(i))
                    continue;

                targetLevel = this.levels.getLevel(i);
                break;
            }
        }

        // Collect all the tiles intersecting the input sector.
        LatLon delta = targetLevel.getTileDelta();
        LatLon origin = this.levels.getTileOrigin();
        int nwRow = Tile.computeRow(delta.latitude, sector.maxLatitude, origin.latitude);
        int nwCol = Tile.computeColumn(delta.longitude, sector.minLongitude, origin.longitude);
        int seRow = Tile.computeRow(delta.latitude, sector.minLatitude, origin.latitude);
        int seCol = Tile.computeColumn(delta.longitude, sector.maxLongitude, origin.longitude);

        long numRows = nwRow - seRow + 1;
        long numCols = seCol - nwCol + 1;

        return numRows * numCols;
    }

    public GpuTextureTile[][] getTilesInSector(Sector sector, int levelNumber)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Level targetLevel = this.levels.getLastLevel();
        if (levelNumber >= 0)
        {
            for (int i = levelNumber; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
            {
                if (this.levels.isLevelEmpty(i))
                    continue;

                targetLevel = this.levels.getLevel(i);
                break;
            }
        }

        // Collect all the tiles intersecting the input sector.
        LatLon delta = targetLevel.getTileDelta();
        LatLon origin = this.levels.getTileOrigin();
        int nwRow = Tile.computeRow(delta.latitude, sector.maxLatitude, origin.latitude);
        int nwCol = Tile.computeColumn(delta.longitude, sector.minLongitude, origin.longitude);
        int seRow = Tile.computeRow(delta.latitude, sector.minLatitude, origin.latitude);
        int seCol = Tile.computeColumn(delta.longitude, sector.maxLongitude, origin.longitude);

        int numRows = nwRow - seRow + 1;
        int numCols = seCol - nwCol + 1;
        GpuTextureTile[][] sectorTiles = new GpuTextureTile[numRows][numCols];

        for (int row = nwRow; row >= seRow; row--)
        {
            for (int col = nwCol; col <= seCol; col++)
            {
                TileKey key = new TileKey(targetLevel.getLevelNumber(), row, col, targetLevel.getCacheName());
                Sector tileSector = this.levels.computeSectorForKey(key);
                sectorTiles[nwRow - row][col - nwCol] = this.createTile(tileSector, targetLevel, row, col);
            }
        }

        return sectorTiles;
    }

    protected Bitmap getImage(GpuTextureTile tile, String mimeType, int timeout) throws Exception
    {
        // Read the image from disk.
        Bitmap image = this.requestImage(tile, mimeType);

        if (Thread.currentThread().isInterrupted())
        {
            throw new InterruptedException();
        }

        if (image != null)
            return image;

        // Retrieve it from the net since it's not on disk.
        this.downloadImage(tile, mimeType, timeout);

        // Try to read from disk again after retrieving it from the net.
        image = this.requestImage(tile, mimeType);

        if (Thread.currentThread().isInterrupted())
        {
            throw new InterruptedException();
        }

        if (image == null)
        {
            String message =
                Logging.getMessage("layers.TiledImageLayer.ImageUnavailable", tile.getPath());
            throw new RuntimeException(message);
        }

        return image;
    }

    protected class CompositionRetrievalPostProcessor extends AbstractRetrievalPostProcessor
    {
        protected GpuTextureTile tile;

        public CompositionRetrievalPostProcessor(GpuTextureTile tile)
        {
            this.tile = tile;
        }

        protected File doGetOutputFile()
        {
            String suffix = WWIO.makeSuffixForMimeType(this.getRetriever().getContentType());
            if (suffix == null)
            {
                Logging.error(Logging.getMessage("generic.UnknownContentType", this.getRetriever().getContentType()));
                return null;
            }

            String path = this.tile.getPathBase();
            path += suffix;

            File f = new File(path);
            File outFile = f.isAbsolute() ? f : getDataFileStore().newFile(path);
            if (outFile == null)
                return null;

            return outFile;
        }

        @Override
        protected boolean isDeleteOnExit(File outFile)
        {
            return outFile.getPath().contains(WWIO.DELETE_ON_EXIT_PREFIX);
        }

        @Override
        protected boolean overwriteExistingFile()
        {
            return true;
        }

        @Override
        protected void markResourceAbsent()
        {
            TiledImageLayer.this.levels.markResourceAbsent(tile);
        }

        @Override
        protected void handleUnsuccessfulRetrieval()
        {
            // Don't mark the tile as absent because the caller may want to try again.
        }
    }
}
