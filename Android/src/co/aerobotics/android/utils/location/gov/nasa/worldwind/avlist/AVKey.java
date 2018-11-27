/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist;

/**
 * @author Tom Gaskins
 * @version $Id$
 */
public interface AVKey
{
    final String AVERAGE_TILE_SIZE = "gov.nasa.worldwind.avkey.AverageTileSize";
    final String AVAILABLE_IMAGE_FORMATS = "gov.nasa.worldwind.avkey.AvailableImageFormats";

    final String BIG_ENDIAN = "gov.nasa.worldwind.avkey.BigEndian";
    final String BYTE_ORDER = "gov.nasa.worldwind.avkey.ByteOrder";

    final String CONSTRUCTION_PARAMETERS = "gov.nasa.worldwind.avkey.ConstructionParameters";

    final String DATA_CACHE_NAME = "gov.nasa.worldwind.avkey.DataCacheName";
    final String DATA_FILE_STORE_CLASS_NAME = "gov.nasa.worldwind.avkey.DataFileStoreClassName";
    final String DATA_FILE_STORE_CONFIGURATION_FILE_NAME
        = "gov.nasa.worldwind.avkey.DataFileStoreConfigurationFileName";
    final String DATASET_NAME = "gov.nasa.worldwind.avkey.DatasetNameKey";
    /**
     * Indicates the primitive data type of a dataset or a buffer of data. When used as a key, the corresponding value
     * may be one of the following: <code>INT8</code>, <code>INT16</code>, <code>INT32</code>, <code>INT64</code>,
     * <code>FLOAT32</code>, or <code>FLOAT64</code>.
     */
    final String DATA_TYPE = "gov.nasa.worldwind.avkey.DataType";
    final String DELETE_CACHE_ON_EXIT = "gov.nasa.worldwind.avkey.DeleteCacheOnExit";
    final String DETAIL_HINT = "gov.nasa.worldwind.avkey.DetailHint";
    final String DISPLAY_NAME = "gov.nasa.worldwind.avkey.DisplayName";

    final String EARTH_ELEVATION_MODEL_CONFIG_FILE = "gov.nasa.worldwind.avkey.EarthElevationModelConfigFile";
    final String ELEVATION_EXTREMES_FILE = "gov.nasa.worldwind.avkey.ElevationExtremesFileKey";
    final String ELEVATION_EXTREMES_LOOKUP_CACHE_SIZE = "gov.nasa.worldwind.avkey.ElevationExtremesLookupCacheSize";
    final String ELEVATION_MAX = "gov.nasa.worldwind.avkey.ElevationMax";
    final String ELEVATION_MIN = "gov.nasa.worldwind.avkey.ElevationMin";
    final String ELEVATION_MODEL = "gov.nasa.worldwind.avkey.ElevationModel";
    final String ELEVATION_MODEL_FACTORY = "gov.nasa.worldwind.avkey.ElevationModelFactory";
    final String ELEVATION_TILE_CACHE_SIZE = "gov.nasa.worldwind.avkey.ElevationTileCacheSize";
    final String EXPIRY_TIME = "gov.nasa.worldwind.avkey.ExpiryTime";

    final String FILE_NAME = "gov.nasa.worldwind.avkey.FileName";
    final String FILE_STORE_LOCATION = "gov.nasa.worldwind.avkey.FileStoreLocation";
    final String FLOAT32 = "gov.nasa.worldwind.avkey.Float32";
    final String FORCE_LEVEL_ZERO_LOADS = "gov.nasa.worldwind.avkey.ForceLevelZeroLoads";
    final String FORMAT_SUFFIX = "gov.nasa.worldwind.avkey.FormatSuffixKey";

    final String GET_CAPABILITIES_URL = "gov.nasa.worldwind.avkey.GetCapabilitiesURL";
    final String GET_MAP_URL = "gov.nasa.worldwind.avkey.GetMapURL";
    final String GENERATE_MIPMAP = "gov.nasa.worldwind.avkey.GenerateMipmap";
    final String GLOBE = "gov.nasa.worldwind.avkey.Globe";
    final String GLOBE_CLASS_NAME = "gov.nasa.worldwind.avkey.GlobeClassName";
    final String GPU_RESOURCE_CACHE_SIZE = "gov.nasa.worldwind.avkey.GpuResourceCacheSize";
    final String GPU_TEXTURE_FACTORY = "gov.nasa.worldwind.avkey.GpuTextureFactory";
    final String GPU_TEXTURE_TILE_CACHE_SIZE = "gov.nasa.worldwind.avkey.GpuTextureTileCacheSize";

    final String HEIGHT = "gov.nasa.worldwind.avkey.Height";

    final String IMAGE_FORMAT = "gov.nasa.worldwind.avkey.ImageFormat";
    final String INACTIVE_LEVELS = "gov.nasa.worldwind.avkey.InactiveLevels";
    final String INITIAL_LATITUDE = "gov.nasa.worldwind.avkey.InitialLatitude";
    final String INITIAL_LONGITUDE = "gov.nasa.worldwind.avkey.InitialLongitude";
    final String INITIAL_ALTITUDE = "gov.nasa.worldwind.avkey.InitialAltitude";
    final String INPUT_HANDLER_CLASS_NAME = "gov.nasa.worldwind.avkey.InputHandlerClassName";
    final String INSTALLED = "gov.nasa.worldwind.avkey.Installed";
    final String INT8 = "gov.nasa.worldwind.avkey.Int8";
    final String INT16 = "gov.nasa.worldwind.avkey.Int16";
    final String INT32 = "gov.nasa.worldwind.avkey.Int32";
    final String INT64 = "gov.nasa.worldwind.avkey.Int64";

    final String LAYER = "gov.nasa.worldwind.avkey.Layer";
    final String LAYERS = "gov.nasa.worldwind.avkey.Layers";
    final String LAYER_FACTORY = "gov.nasa.worldwind.avkey.LayerFactory";
    final String LAYER_NAMES = "gov.nasa.worldwind.avkey.LayerNames";
    final String LEVEL_NAME = "gov.nasa.worldwind.avkey.LevelName";
    final String LEVEL_NUMBER = "gov.nasa.worldwind.avkey.LevelNumber";
    final String LEVEL_ZERO_TILE_DELTA = "gov.nasa.worldwind.LevelZeroTileDelta";
    final String LITTLE_ENDIAN = "gov.nasa.worldwind.avkey.LittleEndian";
    final String LOGCAT_TAG = "gov.nasa.worldwind.avkey.LogcatTag";

    final String MAP_SCALE = "gov.nasa.worldwind.avkey.MapScale";
    /**
     * Describes the maximum number of attempts to make when downloading a resource before attempts are suspended.
     * Attempts are restarted after the interval specified by {@link #MIN_ABSENT_TILE_CHECK_INTERVAL}.
     *
     * @see #MIN_ABSENT_TILE_CHECK_INTERVAL
     */
    final String MAX_ABSENT_TILE_ATTEMPTS = "gov.nasa.worldwind.avkey.MaxAbsentTileAttempts";
    final String MAX_ACTIVE_ALTITUDE = "gov.nasa.worldwind.avkey.MaxActiveAltitude";
    final String MAX_MESSAGE_REPEAT = "gov.nasa.worldwind.avkey.MaxMessageRepeat";
    final String MEMORY_CACHE_SET_CLASS_NAME = "gov.nasa.worldwind.avkey.MemoryCacheSetClassName";
    final String MIN_ACTIVE_ALTITUDE = "gov.nasa.worldwind.avkey.MinActiveAltitude";
    /**
     * Describes the interval to wait before allowing further attempts to download a resource after the number of
     * attempts specified by {@link #MAX_ABSENT_TILE_ATTEMPTS} are made.
     *
     * @see #MAX_ABSENT_TILE_ATTEMPTS
     */
    final String MIN_ABSENT_TILE_CHECK_INTERVAL = "gov.nasa.worldwind.avkey.MinAbsentTileCheckInterval";

    // Implementation note: the keys MISSING_DATA_SIGNAL and MISSING_DATA_REPLACEMENT are intentionally different than
    // their actual string values. Legacy code is expecting the string values "MissingDataFlag" and "MissingDataValue",
    // respectively.
    final String MISSING_DATA_SIGNAL = "gov.nasa.worldwind.avkey.MissingDataFlag";
    final String MISSING_DATA_REPLACEMENT = "gov.nasa.worldwind.avkey.MissingDataValue";

    final String MODEL = "gov.nasa.worldwind.avkey.Model";
    final String MODEL_CLASS_NAME = "gov.nasa.worldwind.avkey.ModelClassName";

    final String NETWORK_RETRIEVAL_ENABLED = "gov.nasa.worldwind.avkey.NetworkRetrievalEnabled";
    final String NETWORK_STATUS_CLASS_NAME = "gov.nasa.worldwind.avkey.NetworkStatusClassName";
    final String NETWORK_STATUS_TEST_SITES = "gov.nasa.worldwind.avkey.NetworkStatusTestSites";
    final String NUM_EMPTY_LEVELS = "gov.nasa.worldwind.avkey.NumEmptyLevels";
    final String NUM_LEVELS = "gov.nasa.worldwind.avkey.NumLevels";

    final String OFFLINE_MODE = "gov.nasa.worldwind.avkey.OfflineMode";
    final String OPACITY = "gov.nasa.worldwind.avkey.Opacity";

    final String PICKED_OBJECT_PARENT_LAYER = "gov.nasa.worldwind.avkey.PickedObject.ParentLayer";
    final String POSITION = "gov.nasa.worldwind.avkey.Position";
    final String PROGRESS = "gov.nasa.worldwind.avkey.Progress";

    final String RETAIN_LEVEL_ZERO_TILES = "gov.nasa.worldwind.avkey.RetainLevelZeroTiles";
    final String RETRIEVE_PROPERTIES_FROM_SERVICE = "gov.nasa.worldwind.avkey.RetrievePropertiesFromService";
    final String RETRIEVAL_POOL_SIZE = "gov.nasa.worldwind.avkey.RetrievalPoolSize";
    final String RETRIEVAL_QUEUE_SIZE = "gov.nasa.worldwind.avkey.RetrievalQueueSize";
    final String RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT = "gov.nasa.worldwind.avkey.RetrievalStaleRequestLimit";
    final String RETRIEVAL_SERVICE_CLASS_NAME = "gov.nasa.worldwind.avkey.RetrievalServiceClassName";
    final String RETRIEVER_FACTORY_LOCAL = "gov.nasa.worldwind.avkey.RetrieverFactoryLocal";
    final String RETRIEVER_STATE = "gov.nasa.worldwind.avkey.RetrieverState";
    final String RETRIEVAL_STATE_ERROR = "gov.nasa.worldwind.avkey.RetrievalStateError";
    final String RETRIEVAL_STATE_SUCCESSFUL = "gov.nasa.worldwind.avkey.RetrievalStateSuccessful";

    final String SCENE_CONTROLLER_CLASS_NAME = "gov.nasa.worldwind.avkey.SceneControllerClassName";
    final String SECTOR = "gov.nasa.worldwind.avkey.Sector";
    final String SECTOR_GEOMETRY_CACHE_SIZE = "gov.nasa.worldwind.avkey.SectorGeometryCacheSize";
    final String SECTOR_GEOMETRY_TILE_CACHE_SIZE = "gov.nasa.worldwind.avkey.SectorGeometryTileCacheSize";
    final String SECTOR_RESOLUTION_LIMITS = "gov.nasa.worldwind.avkey.SectorResolutionLimits";
    final String SERVICE = "gov.nasa.worldwind.avkey.ServiceURLKey";
    final String SERVICE_NAME = "gov.nasa.worldwind.avkey.ServiceName";
    final String STYLE_NAMES = "gov.nasa.worldwind.avkey.StyleNames";

    final String TESSELLATOR_FACTORY = "gov.nasa.worldwind.avkey.TessellatorFactory";
    final String TESSELLATOR_CONFIG_FILE = "gov.nasa.worldwind.avkey.TessellatorConfigFile";
    final String TASK_SERVICE_CLASS_NAME = "gov.nasa.worldwind.avkey.TaskServiceClassName";
    final String TASK_SERVICE_POOL_SIZE = "gov.nasa.worldwind.avkey.TaskServicePoolSize";
    final String TASK_SERVICE_QUEUE_SIZE = "gov.nasa.worldwind.avkey.TaskServiceQueueSize";
    final String TEXTURE_FORMAT = "gov.nasa.worldwind.avkey.TextureFormat";
    final String TILE_DELTA = "gov.nasa.worldwind.avkey.TileDelta";
    final String TILE_HEIGHT = "gov.nasa.worldwind.avkey.TileHeight";
    final String TILE_ORIGIN = "gov.nasa.worldwind.avkey.TileOrigin";
    final String TILE_URL_BUILDER = "gov.nasa.worldwind.avkey.TileURLBuilder";
    final String TILE_WIDTH = "gov.nasa.worldwind.avkey.TileWidth";
    final String TITLE = "gov.nasa.worldwind.avkey.Title";
    final String TRANSPARENCY_COLORS = "gov.nasa.worldwind.avkey.TransparencyColors";

    final String URL_CONNECT_TIMEOUT = "gov.nasa.worldwind.avkey.URLConnectTimeout";
    final String URL_PROXY_HOST = "gov.nasa.worldwind.avkey.UrlProxyHost";
    final String URL_PROXY_PORT = "gov.nasa.worldwind.avkey.UrlProxyPort";
    final String URL_PROXY_TYPE = "gov.nasa.worldwind.avkey.UrlProxyType";
    final String URL_READ_TIMEOUT = "gov.nasa.worldwind.avkey.URLReadTimeout";
    final String USE_MIP_MAPS = "gov.nasa.worldwind.avkey.UseMipMaps";
    final String USE_TRANSPARENT_TEXTURES = "gov.nasa.worldwind.avkey.UseTransparentTextures";

    final String VERTICAL_EXAGGERATION = "gov.nasa.worldwind.avkey.VerticalExaggeration";
    final String VIEW = "gov.nasa.worldwind.avkey.View";
    final String VIEW_CLASS_NAME = "gov.nasa.worldwind.avkey.ViewClassName";

    final String WIDTH = "gov.nasa.worldwind.avkey.Width";
    final String WMS_VERSION = "gov.nasa.worldwind.avkey.WMSVersion";
    final String WMS_BACKGROUND_COLOR = "gov.nasa.worldwind.avkey.BackgroundColor";
}
