/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.terrain;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.BasicFactory;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.exception.WWRuntimeException;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.exception.WWUnrecognizedException;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.ogc.OGCConstants;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import java.util.List;

/**
 * A factory that creates {@link gov.nasa.worldwind.terrain.ElevationModel} instances.
 *
 * @author dcollins
 * @version $Id$
 */
public class BasicElevationModelFactory extends BasicFactory
{
    /** Creates an instance of BasicElevationModelFactory; otherwise does nothing. */
    public BasicElevationModelFactory()
    {
    }

    /**
     * Creates an elevation model from a general configuration source. The source can be one of the following: <ul>
     * <li>a {@link java.net.URL}</li> <li>a {@link java.io.File}</li> <li>a {@link java.io.InputStream}</li> <li> an
     * {@link org.w3c.dom.Element}</li> <li>a {@link String} holding a file name, a name of a resource on the classpath,
     * or a string representation of a URL</li> </ul>
     * <p/>
     * For non-compound models, this method maps the <code>serviceName</code> attribute of the
     * <code>ElevationModel/Service</code> element of the XML configuration document to the appropriate elevation-model
     * type. Service types recognized are:" <ul> <li>"WMS" for elevation models that draw their data from a WMS web
     * service.</li> <li>"WWTileService" for elevation models that draw their data from a World Wind tile service.</li>
     * <li>"Offline" for elevation models that draw their data only from the local cache.</li> </ul>
     *
     * @param configSource the configuration source. See above for supported types.
     *
     * @return an elevation model.
     *
     * @throws IllegalArgumentException if the configuration file name is null or an empty string.
     * @throws WWUnrecognizedException
     *                                  if the source type is unrecognized or the requested elevation-model type is
     *                                  unrecognized.
     * @throws WWRuntimeException
     *                                  if object creation fails for other reasons. The exception identifying the source
     *                                  of the failure is included as the {@link Exception#initCause(Throwable)}.
     */
    @Override
    public Object createFromConfigSource(Object configSource)
    {
        ElevationModel model = (ElevationModel) super.createFromConfigSource(configSource);

        if (model == null)
        {
            String msg = Logging.getMessage("generic.SourceTypeUnrecognized", configSource);
            throw new WWUnrecognizedException(msg);
        }

        return model;
    }

    /**
     * Creates an elevation model from an XML description. An "href" link to an external elevation model description is
     * followed if it exists.
     *
     * @param domElement an XML element containing the elevation model description.
     *
     * @return the requested elevation model, or null if the specified element does not describe an elevation model.
     *
     * @throws Exception if a problem occurs during creation.
     * @see #createNonCompoundModel(org.w3c.dom.Element).
     */
    @Override
    protected ElevationModel doCreateFromElement(Element domElement) throws Exception
    {
        XPath xpath = WWXML.makeXPath();

        Element element = WWXML.getElement(domElement, ".", xpath);
        if (element == null)
            return null;

        String href = WWXML.getText(element, "@href", xpath);
        if (!WWUtil.isEmpty(href))
            return (ElevationModel) this.createFromConfigSource(href);

        List<Element> elements = WWXML.getElements(element, "./ElevationModel", xpath);

        String modelType = WWXML.getText(element, "@modelType", xpath);
        if (modelType != null && modelType.equalsIgnoreCase("compound"))
            return this.createCompoundModel(elements);

        String localName = WWXML.getUnqualifiedName(domElement);
        if (elements != null && elements.size() > 0)
            return this.createCompoundModel(elements);
        else if (localName != null && localName.equals("ElevationModel"))
            return this.createNonCompoundModel(domElement);

        return null;
    }

    /**
     * Creates a compound elevation model and populates it with a specified list of elevation models.
     * <p/>
     * Any exceptions occurring during creation of the elevation models are logged and not re-thrown. The elevation
     * models associated with the exceptions are not included in the returned compound model.
     *
     * @param elements the XML elements describing the models in the new elevation model.
     *
     * @return a compound elevation model populated with the specified elevation models. The compound model will contain
     *         no elevation models if none were specified or exceptions occurred for all that were specified.
     *
     * @see #createNonCompoundModel(org.w3c.dom.Element).
     */
    protected ElevationModel createCompoundModel(List<Element> elements)
    {
        CompoundElevationModel compoundModel = new CompoundElevationModel();

        if (elements == null || elements.size() == 0)
            return compoundModel;

        for (Element element : elements)
        {
            try
            {
                ElevationModel em = this.doCreateFromElement(element);
                if (em != null)
                    compoundModel.addElevationModel(em);
            }
            catch (Exception e)
            {
                String msg = Logging.getMessage("ElevationModel.ExceptionCreatingElevationModel");
                Logging.warning(msg, e);
            }
        }

        return compoundModel;
    }

    /**
     * Create a simple elevation model.
     *
     * @param domElement the XML element describing the elevation model to create.
     *
     * @return a new elevation model
     *
     * @throws WWUnrecognizedException if the service type given in the describing element is unrecognized.
     */
    protected ElevationModel createNonCompoundModel(Element domElement)
    {
        ElevationModel em;

        String serviceName = WWXML.getText(domElement, "Service/@serviceName");

        if (serviceName.equals("Offline"))
        {
            em = new BasicElevationModel(domElement, null);
        }
        else if (serviceName.equals("WWTileService"))
        {
            em = new BasicElevationModel(domElement, null);
        }
        else if (serviceName.equals(OGCConstants.WMS_SERVICE_NAME))
        {
            em = new WMSBasicElevationModel(domElement, null);
        }
        else
        {
            String msg = Logging.getMessage("generic.UnrecognizedServiceName", serviceName);
            throw new WWUnrecognizedException(msg);
        }

        return em;
    }
}
