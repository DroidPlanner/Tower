/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.layers;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.exception.WWRuntimeException;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.exception.WWUnrecognizedException;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.AVKey;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.exception.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.ogc.OGCConstants;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import java.util.*;

/**
 * A factory that creates {@link gov.nasa.worldwind.layers.Layer} instances.
 *
 * @author dcollins
 * @version $Id$
 */
public class BasicLayerFactory extends BasicFactory
{
    /** Creates an instance of BasicLayerFactory; otherwise does nothing. */
    public BasicLayerFactory()
    {
    }

    /**
     * Creates a layer or layer list from a general configuration source. The source can be one of the following: <ul>
     * <li>a {@link java.net.URL}</li> <li>a {@link java.io.File}</li> <li>a {@link java.io.InputStream}</li> <li>{@link
     * org.w3c.dom.Element}</li> <li>a {@link String} holding a file name, a name of a resource on the classpath, or a
     * string representation of a URL</li> </ul>
     * <p/>
     * For tiled image layers, this maps the <code>serviceName</code> attribute of the <code>Layer/Service</code>
     * element of the XML configuration file to the appropriate base tiled image layer type. Service types recognized
     * are: <ul> <li>"WMS" for layers that draw their data from a WMS web service.</li> <li>"WWTileService" for layers
     * that draw their data from a World Wind tile service.</li> <li>"Offline" for layers that draw their data only from
     * the local cache.</li> </ul>
     *
     * @param configSource the configuration source. See above for supported types.
     *
     * @return a layer or layer list.
     *
     * @throws IllegalArgumentException if the configuration file name is null or an empty string.
     * @throws WWUnrecognizedException
     *                                  if the layer service type is unrecognized.
     * @throws WWRuntimeException
     *                                  if object creation fails. The exception indicating the source of the failure is
     *                                  included as the {@link Exception#initCause(Throwable)}.
     */
    public Object createFromConfigSource(Object configSource)
    {
        Object layerOrLists = super.createFromConfigSource(configSource);

        if (layerOrLists == null)
        {
            String msg = Logging.getMessage("generic.SourceTypeUnrecognized", configSource);
            throw new WWUnrecognizedException(msg);
        }

        return layerOrLists;
    }

    /**
     * Create the objects described in an XML element containing layer and/or layer-list descriptions. Nested layer
     * lists and included layers are created recursively.
     *
     * @param domElement an XML element describing the layers and/or layer lists.
     *
     * @return a <code>Layer</code>, <code>LayerList</code> or array of <code>LayerList</code>s, as described by the
     *         specified description.
     *
     * @throws Exception if an exception occurs during creation. Exceptions occurring during creation of internal layers
     *                   or layer lists are not re-thrown but are logged. The layer or layer list associated with the
     *                   exception is not contained in the returned object.
     */
    @Override
    protected Object doCreateFromElement(Element domElement) throws Exception
    {
        XPath xpath = WWXML.makeXPath();

        List<Element> elements = WWXML.getElements(domElement, "//LayerList", xpath);
        if (elements != null && elements.size() > 0)
            return createLayerLists(elements);

        elements = WWXML.getElements(domElement, "./Layer", xpath);
        if (elements != null && elements.size() > 1)
            return createLayerList(elements);

        if (elements != null && elements.size() == 1)
            return this.createFromLayerDocument(elements.get(0));

        String localName = WWXML.getUnqualifiedName(domElement);
        if (localName != null && localName.equals("Layer"))
            return this.createFromLayerDocument(domElement);

        return null;
    }

    /**
     * Create a collection of layer lists and their included layers described by an array of XML layer-list description
     * elements.
     * <p/>
     * Any exceptions occurring during creation of the layer lists or their included layers are logged and not
     * re-thrown. The layers associated with the exceptions are not included in the returned layer list.
     *
     * @param elements the XML elements describing the layer lists to create.
     *
     * @return an array containing the specified layer lists.
     */
    protected LayerList[] createLayerLists(List<? extends Element> elements)
    {
        ArrayList<LayerList> layerLists = new ArrayList<LayerList>();
        XPath xpath = WWXML.makeXPath();

        for (Element element : elements)
        {
            try
            {
                String href = WWXML.getText(element, "@href", xpath);
                if (!WWUtil.isEmpty(href))
                {
                    Object o = this.createFromConfigSource(href);
                    if (o == null)
                        continue;

                    if (o instanceof Layer)
                    {
                        LayerList ll = new LayerList();
                        ll.add((Layer) o);
                        o = ll;
                    }

                    if (o instanceof LayerList)
                    {
                        LayerList list = (LayerList) o;
                        if (list != null && list.size() > 0)
                            layerLists.add(list);
                    }
                    else if (o instanceof LayerList[])
                    {
                        LayerList[] lists = (LayerList[]) o;
                        if (lists != null && lists.length > 0)
                            layerLists.addAll(Arrays.asList(lists));
                    }
                    else
                    {
                        Logging.warning(
                            Logging.getMessage("LayerFactory.UnexpectedTypeForLayer", o.getClass().getName()));
                    }

                    continue;
                }

                String title = WWXML.getText(element, "@title", xpath);
                List<Element> children = WWXML.getElements(element, "./Layer", xpath);
                if (children != null && children.size() > 0)
                {
                    LayerList list = this.createLayerList(children);
                    if (list != null && list.size() > 0)
                    {
                        layerLists.add(list);
                        if (title != null && title.length() > 0)
                            list.setValue(AVKey.DISPLAY_NAME, title);
                    }
                }
            }
            catch (Exception e)
            {
                Logging.warning(e.getMessage(), e);
                // keep going to create other layers
            }
        }

        return layerLists.toArray(new LayerList[layerLists.size()]);
    }

    /**
     * Create a list of layers described by an array of XML layer description elements.
     * <p/>
     * Any exceptions occurring during creation of the layers are logged and not re-thrown. The layers associated with
     * the exceptions are not included in the returned layer list.
     *
     * @param layerElements the XML elements describing the layers to create.
     *
     * @return a layer list containing the specified layers.
     */
    protected LayerList createLayerList(List<? extends Element> layerElements)
    {
        LayerList layerList = new LayerList();

        for (Element element : layerElements)
        {
            try
            {
                layerList.add(this.createFromLayerDocument(element));
            }
            catch (Exception e)
            {
                Logging.warning(e.getMessage(), e);
                // keep going to create other layers
            }
        }

        return layerList;
    }

    /**
     * Create a layer described by an XML layer description.
     *
     * @param domElement the XML element describing the layer to create.
     *
     * @return a new layer
     *
     * @throws WWUnrecognizedException if the layer type or service type given in the describing element is
     *                                 unrecognized.
     */
    protected Layer createFromLayerDocument(Element domElement)
    {
        XPath xpath = WWXML.makeXPath();

        String className = WWXML.getText(domElement, "@className", xpath);
        if (!WWUtil.isEmpty(className))
        {
            Layer layer = (Layer) WorldWind.createComponent(className);
            String actuate = WWXML.getText(domElement, "@actuate", xpath);
            layer.setEnabled(WWUtil.isEmpty(actuate) || actuate.equals("onLoad"));
            WWXML.invokePropertySetters(layer, domElement);
            return layer;
        }

        Layer layer;
        String href = WWXML.getText(domElement, "@href", xpath);
        if (href != null && href.length() > 0)
        {
            Object o = this.createFromConfigSource(href);
            if (o == null)
                return null;
            else if (!(o instanceof Layer))
            {
                String msg = Logging.getMessage("LayerFactory.UnexpectedTypeForLayer", o.getClass().getName());
                throw new WWRuntimeException(msg);
            }

            layer = (Layer) o;
        }
        else
        {
            String layerType = WWXML.getText(domElement, "@layerType", xpath);
            if (layerType != null && layerType.equals("TiledImageLayer"))
            {
                layer = this.createTiledImageLayer(domElement);
            }
            else
            {
                String msg = Logging.getMessage("generic.LayerTypeUnrecognized", layerType);
                throw new WWUnrecognizedException(msg);
            }
        }

        String actuate = WWXML.getText(domElement, "@actuate", xpath);
        layer.setEnabled(WWUtil.isEmpty(actuate) || actuate.equals("onLoad"));
        WWXML.invokePropertySetters(layer, domElement);

        return layer;
    }

    /**
     * Create a {@link TiledImageLayer} layer described by an XML layer description.
     *
     * @param domElement the XML element describing the layer to create.
     *
     * @return a new layer
     *
     * @throws WWUnrecognizedException if the service type given in the describing element is unrecognized.
     */
    protected Layer createTiledImageLayer(Element domElement)
    {
        String serviceName = WWXML.getText(domElement, "Service/@serviceName", null);

        Layer layer;

        if (OGCConstants.WMS_SERVICE_NAME.equals(serviceName))
        {
            layer = new WMSTiledImageLayer(domElement, null);
        }
        else
        {
            String msg = Logging.getMessage("generic.UnrecognizedServiceName", serviceName);
            Logging.warning(msg);

            throw new WWUnrecognizedException(msg);
        }

        return layer;
    }
}
