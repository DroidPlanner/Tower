/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.terrain;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.exception.WWRuntimeException;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.retrieve.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;
import org.w3c.dom.*;

import java.io.*;
import java.net.*;
import java.util.List;

/**
 * @author tag
 * @version $Id: WMSBasicElevationModel.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class WMSBasicElevationModel extends BasicElevationModel
{
    public WMSBasicElevationModel(AVList params)
    {
        super(params);
    }

    public WMSBasicElevationModel(Element domElement, AVList params)
    {
        this(wmsGetParamsFromDocument(domElement, params));
    }

    // TODO implement on Android
//    public WMSBasicElevationModel(WMSCapabilities caps, AVList params)
//    {
//        this(wmsGetParamsFromCapsDoc(caps, params));
//    }

    public WMSBasicElevationModel(String restorableStateInXml)
    {
        super(wmsRestorableStateToParams(restorableStateInXml));

        RestorableSupport rs;
        try
        {
            rs = RestorableSupport.parse(restorableStateInXml);
        }
        catch (Exception e)
        {
            // Parsing the document specified by stateInXml failed.
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", restorableStateInXml);
            Logging.error(message);
            throw new IllegalArgumentException(message, e);
        }

        this.doRestoreState(rs, null);
    }

    protected static AVList wmsGetParamsFromDocument(Element domElement, AVList params)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
            params = new AVListImpl();

        DataConfigurationUtils.getWMSLayerConfigParams(domElement, params);
        getBasicElevationModelConfigParams(domElement, params);
        wmsSetFallbacks(params);

        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder(params.getStringValue(AVKey.WMS_VERSION), params));

        return params;
    }

    protected static void wmsSetFallbacks(AVList params)
    {
        if (params.getValue(AVKey.LEVEL_ZERO_TILE_DELTA) == null)
        {
            Angle delta = Angle.fromDegrees(20);
            params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(delta, delta));
        }

        if (params.getValue(AVKey.TILE_WIDTH) == null)
            params.setValue(AVKey.TILE_WIDTH, 150);

        if (params.getValue(AVKey.TILE_HEIGHT) == null)
            params.setValue(AVKey.TILE_HEIGHT, 150);

        if (params.getValue(AVKey.FORMAT_SUFFIX) == null)
            params.setValue(AVKey.FORMAT_SUFFIX, ".bil");

        if (params.getValue(AVKey.MISSING_DATA_SIGNAL) == null)
            params.setValue(AVKey.MISSING_DATA_SIGNAL, -9999d);

        if (params.getValue(AVKey.NUM_LEVELS) == null)
            params.setValue(AVKey.NUM_LEVELS, 19); // approximately 0.1 meters per pixel

        if (params.getValue(AVKey.NUM_EMPTY_LEVELS) == null)
            params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
    }

    // TODO: consolidate common code in WMSTiledImageLayer.URLBuilder and WMSBasicElevationModel.URLBuilder
    protected static class URLBuilder implements TileUrlBuilder
    {
        protected static final String MAX_VERSION = "1.3.0";

        protected final String layerNames;
        protected final String styleNames;
        protected final String imageFormat;
        protected final String bgColor;
        protected final String wmsVersion;
        protected final String crs;
        protected String URLTemplate = null;

        protected URLBuilder(String version, AVList params)
        {
            Double d = (Double) params.getValue(AVKey.MISSING_DATA_SIGNAL);

            this.layerNames = params.getStringValue(AVKey.LAYER_NAMES);
            this.styleNames = params.getStringValue(AVKey.STYLE_NAMES);
            this.imageFormat = params.getStringValue(AVKey.IMAGE_FORMAT);
            this.bgColor = (d != null) ? d.toString() : null;

            if (version == null || version.compareTo(MAX_VERSION) >= 0)
            {
                this.wmsVersion = MAX_VERSION;
//                this.crs = "&crs=CRS:84";
                this.crs = "&crs=EPSG:4326"; // TODO: what's the correct CRS value for these versions?
            }
            else
            {
                this.wmsVersion = version;
                this.crs = "&srs=EPSG:4326";
            }
        }

        public URL getURL(co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Tile tile, String altImageFormat) throws MalformedURLException
        {
            StringBuffer sb;
            if (this.URLTemplate == null)
            {
                sb = new StringBuffer(tile.getLevel().getService());

                if (!sb.toString().toLowerCase().contains("service=wms"))
                    sb.append("service=WMS");
                sb.append("&request=GetMap");
                sb.append("&version=");
                sb.append(this.wmsVersion);
                sb.append(this.crs);
                sb.append("&layers=");
                sb.append(this.layerNames);
                sb.append("&styles=");
                sb.append(this.styleNames != null ? this.styleNames : "");
                sb.append("&format=");
                if (altImageFormat == null)
                    sb.append(this.imageFormat);
                else
                    sb.append(altImageFormat);
                if (this.bgColor != null)
                {
                    sb.append("&bgColor=");
                    sb.append(this.bgColor);
                }

                this.URLTemplate = sb.toString();
            }
            else
            {
                sb = new StringBuffer(this.URLTemplate);
            }

            sb.append("&width=");
            sb.append(tile.getWidth());
            sb.append("&height=");
            sb.append(tile.getHeight());

            Sector s = tile.getSector();
            sb.append("&bbox=");
            sb.append(s.minLongitude.degrees);
            sb.append(",");
            sb.append(s.minLatitude.degrees);
            sb.append(",");
            sb.append(s.maxLongitude.degrees);
            sb.append(",");
            sb.append(s.maxLatitude.degrees);
            sb.append("&"); // terminate the query string

            return new java.net.URL(sb.toString().replace(" ", "%20"));
        }
    }

    //**************************************************************//
    //********************  Configuration  *************************//
    //**************************************************************//

    /**
     * Parses WMSBasicElevationModel configuration parameters from a specified WMS Capabilities source. This writes
     * output as key-value pairs to params. Supported key and parameter names are: <table>
     * <th><td>Parameter</td><td>Value</td><td>Type</td></th> <tr><td>{@link AVKey#ELEVATION_MAX}</td><td>WMS layer's
     * maximum extreme elevation</td><td>Double</td></tr> <tr><td>{@link AVKey#ELEVATION_MIN}</td><td>WMS layer's
     * minimum extreme elevation</td><td>Double</td></tr> <tr><td>{@link AVKey#DATA_TYPE}</td><td>Translate WMS layer's
     * image format to a matching data type</td><td>String</td></tr> </table> This also parses common WMS layer
     * parameters by invoking {@link DataConfigurationUtils#getWMSLayerConfigParams(gov.nasa.worldwind.ogc.wms.WMSCapabilities,
     * String[], gov.nasa.worldwind.avlist.AVList)}.
     *
     * @param caps                  the WMS Capabilities source to parse for WMSBasicElevationModel configuration
     *                              parameters.
     * @param formatOrderPreference an ordered array of preferred image formats, or null to use the default format.
     * @param params                the output key-value pairs which recieve the WMSBasicElevationModel configuration
     *                              parameters.
     *
     * @return a reference to params.
     *
     * @throws IllegalArgumentException if either the document or params are null, or if params does not contain the
     *                                  required key-value pairs.
     * @throws WWRuntimeException
     *                                  if the Capabilities document does not contain any of the required information.
     */
    // TODO implement on Android
//    public static AVList getWMSElevationModelConfigParams(WMSCapabilities caps, String[] formatOrderPreference,
//        AVList params)
//    {
//        if (caps == null)
//        {
//            String message = Logging.getMessage("nullValue.WMSCapabilities");
//            Logging.error(message);
//            throw new IllegalArgumentException(message);
//        }
//
//        if (params == null)
//        {
//            String message = Logging.getMessage("nullValue.ElevationModelConfigParams");
//            Logging.error(message);
//            throw new IllegalArgumentException(message);
//        }
//
//        // Get common WMS layer parameters.
//        DataConfigurationUtils.getWMSLayerConfigParams(caps, formatOrderPreference, params);
//
//        // Attempt to extract the WMS layer names from the specified parameters.
//        String layerNames = params.getStringValue(AVKey.LAYER_NAMES);
//        if (layerNames == null || layerNames.length() == 0)
//        {
//            String message = Logging.getMessage("nullValue.WMSLayerNames");
//            Logging.error(message);
//            throw new IllegalArgumentException(message);
//        }
//
//        String[] names = layerNames.split(",");
//        if (names == null || names.length == 0)
//        {
//            String message = Logging.getMessage("nullValue.WMSLayerNames");
//            Logging.error(message);
//            throw new IllegalArgumentException(message);
//        }
//
//        // Get the layer's extreme elevations.
//        Double[] extremes = caps.getLayerExtremeElevations(caps, names);
//
//        Double d = (Double) params.getValue(AVKey.ELEVATION_MIN);
//        if (d == null && extremes != null && extremes[0] != null)
//            params.setValue(AVKey.ELEVATION_MIN, extremes[0]);
//
//        d = (Double) params.getValue(AVKey.ELEVATION_MAX);
//        if (d == null && extremes != null && extremes[1] != null)
//            params.setValue(AVKey.ELEVATION_MAX, extremes[1]);
//
//        // Compute the internal pixel type from the image format.
//        if (params.getValue(AVKey.DATA_TYPE) == null && params.getValue(AVKey.IMAGE_FORMAT) != null)
//        {
//            String s = WWIO.makeDataTypeForMimeType(params.getValue(AVKey.IMAGE_FORMAT).toString());
//            if (s != null)
//                params.setValue(AVKey.DATA_TYPE, s);
//        }
//
//        // Use the default data type.
//        if (params.getValue(AVKey.DATA_TYPE) == null)
//            params.setValue(AVKey.DATA_TYPE, AVKey.INT16);
//
//        // Use the default byte order.
//        if (params.getValue(AVKey.BYTE_ORDER) == null)
//            params.setValue(AVKey.BYTE_ORDER, AVKey.LITTLE_ENDIAN);
//
//        return params;
//    }

    /**
     * Appends WMS basic elevation model configuration elements to the superclass configuration document.
     *
     * @param params configuration parameters describing this WMS basic elevation model.
     *
     * @return a WMS basic elevation model configuration document.
     */
    protected Document createConfigurationDocument(AVList params)
    {
        Document doc = super.createConfigurationDocument(params);
        if (doc == null || doc.getDocumentElement() == null)
            return doc;

        DataConfigurationUtils.createWMSLayerConfigElements(params, doc.getDocumentElement());

        return doc;
    }

    //**************************************************************//
    //********************  Composition  ***************************//
    //**************************************************************//

    protected static class ElevationCompositionTile extends ElevationTile
    {
        protected int width;
        protected int height;
        protected File file;

        public ElevationCompositionTile(Sector sector, Level level, int width, int height)
            throws IOException
        {
            super(sector, level, -1, -1); // row and column aren't used and need to signal that

            this.width = width;
            this.height = height;

            this.file = File.createTempFile(WWIO.DELETE_ON_EXIT_PREFIX, level.getFormatSuffix());
        }

        @Override
        public int getWidth()
        {
            return this.width;
        }

        @Override
        public int getHeight()
        {
            return this.height;
        }

        @Override
        public String getPath()
        {
            return this.file.getPath();
        }

        public File getFile()
        {
            return this.file;
        }

        @Override
        public long getSizeInBytes() {
            return 0;
        }
    }

    public void composeElevations(Sector sector, List<? extends LatLon> latlons, int tileWidth, double[] buffer)
        throws Exception
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (latlons == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonListIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (buffer == null)
        {
            String msg = Logging.getMessage("nullValue.ElevationsBufferIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (buffer.length < latlons.size() || tileWidth > latlons.size())
        {
            String msg = Logging.getMessage("ElevationModel.ElevationsBufferTooSmall", latlons.size());
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        ElevationCompositionTile tile = new ElevationCompositionTile(sector, this.getLevels().getLastLevel(),
            tileWidth, latlons.size() / tileWidth);

        this.downloadElevations(tile);
        tile.setElevations(this.readElevations(tile.getFile().toURI().toURL()));

        for (int i = 0; i < latlons.size(); i++)
        {
            LatLon ll = latlons.get(i);
            if (ll == null)
                continue;

            double value = this.lookupElevation(ll.latitude, ll.longitude, tile);

            // If an elevation at the given location is available, then write that elevation to the destination buffer.
            // Otherwise do nothing.
            if (value != this.getMissingDataSignal())
                buffer[i] = value;
        }
    }

    protected void downloadElevations(ElevationCompositionTile tile) throws Exception
    {
        URL url = tile.getResourceURL();

        Retriever retriever = new HTTPRetriever(url, new CompositionRetrievalPostProcessor(tile.getFile()));
        retriever.setConnectTimeout(10000);
        retriever.setReadTimeout(60000);
        retriever.call();
    }

    protected static class CompositionRetrievalPostProcessor extends AbstractRetrievalPostProcessor
    {
        // Note: Requested data is never marked as absent because the caller may want to continually re-try retrieval
        protected File outFile;

        public CompositionRetrievalPostProcessor(File outFile)
        {
            this.outFile = outFile;
        }

        protected File doGetOutputFile()
        {
            return this.outFile;
        }

        @Override
        protected boolean overwriteExistingFile()
        {
            return true;
        }

        @Override
        protected boolean isDeleteOnExit(File outFile)
        {
            return outFile.getPath().contains(WWIO.DELETE_ON_EXIT_PREFIX);
        }
    }

    //**************************************************************//
    //********************  Restorable Support  ********************//
    //**************************************************************//

    public void getRestorableStateForAVPair(String key, Object value,
        RestorableSupport rs, RestorableSupport.StateObject context)
    {
        if (value instanceof URLBuilder)
        {
            rs.addStateValueAsString(context, "wms.Version", ((URLBuilder) value).wmsVersion);
            rs.addStateValueAsString(context, "wms.Crs", ((URLBuilder) value).crs);
        }
        else
        {
            super.getRestorableStateForAVPair(key, value, rs, context);
        }
    }

    protected static AVList wmsRestorableStateToParams(String stateInXml)
    {
        if (stateInXml == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport rs;
        try
        {
            rs = RestorableSupport.parse(stateInXml);
        }
        catch (Exception e)
        {
            // Parsing the document specified by stateInXml failed.
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
            Logging.error(message);
            throw new IllegalArgumentException(message, e);
        }

        AVList params = new AVListImpl();
        wmsRestoreStateForParams(rs, null, params);
        return params;
    }

    protected static void wmsRestoreStateForParams(RestorableSupport rs, RestorableSupport.StateObject context,
        AVList params)
    {
        // Invoke the BasicElevationModel functionality.
        restoreStateForParams(rs, null, params);

        String s = rs.getStateValueAsString(context, AVKey.IMAGE_FORMAT);
        if (s != null)
            params.setValue(AVKey.IMAGE_FORMAT, s);

        s = rs.getStateValueAsString(context, AVKey.TITLE);
        if (s != null)
            params.setValue(AVKey.TITLE, s);

        s = rs.getStateValueAsString(context, AVKey.DISPLAY_NAME);
        if (s != null)
            params.setValue(AVKey.DISPLAY_NAME, s);

        RestorableSupport.adjustTitleAndDisplayName(params);

        s = rs.getStateValueAsString(context, AVKey.LAYER_NAMES);
        if (s != null)
            params.setValue(AVKey.LAYER_NAMES, s);

        s = rs.getStateValueAsString(context, AVKey.STYLE_NAMES);
        if (s != null)
            params.setValue(AVKey.STYLE_NAMES, s);

        s = rs.getStateValueAsString(context, "wms.Version");
        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder(s, params));
    }
}
