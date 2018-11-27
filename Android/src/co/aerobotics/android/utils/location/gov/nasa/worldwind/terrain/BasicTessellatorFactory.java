/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.terrain;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.BasicFactory;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.exception.WWUnrecognizedException;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;
import org.w3c.dom.Element;

/**
 * @author dcollins
 * @version $Id$
 */
public class BasicTessellatorFactory extends BasicFactory
{
    /** Creates an instance of BasicTessellatorFactory; otherwise does nothing. */
    public BasicTessellatorFactory()
    {
    }

    public Object createFromConfigSource(Object configSource)
    {
        Object tessellator = super.createFromConfigSource(configSource);

        if (tessellator == null)
        {
            String msg = Logging.getMessage("generic.SourceTypeUnrecognized", configSource);
            throw new WWUnrecognizedException(msg);
        }

        return tessellator;
    }

    /**
     * Creates an tessellator from an XML description. An "href" link to an external tessellator description is followed
     * if it exists.
     *
     * @param domElement an XML element containing the tessellator description.
     *
     * @return the requested tessellator, or null if the specified element does not describe a tessellator.
     *
     * @throws Exception if a problem occurs during creation.
     */
    @Override
    protected Tessellator doCreateFromElement(Element domElement) throws Exception
    {
        String href = WWXML.getText(domElement, "@href", null);
        if (href != null && href.length() > 0)
            return (Tessellator) this.createFromConfigSource(href);

        String tessellatorType = WWXML.getText(domElement, "@tessellatorType", null);
        if (tessellatorType != null && tessellatorType.equalsIgnoreCase("TiledTessellator"))
        {
            return this.createTiledTessellator(domElement);
        }
        else
        {
            String msg = Logging.getMessage("generic.TessellatorTypeUnrecognized", tessellatorType);
            throw new WWUnrecognizedException(msg);
        }
    }

    protected Tessellator createTiledTessellator(Element domElement)
    {
        return new TiledTessellator(domElement);
    }
}
