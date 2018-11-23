/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.exception.WWRuntimeException;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;
import org.w3c.dom.*;

/**
 * A basic implementation of the {@link Factory} interface.
 *
 * @author dcollins
 * @version $Id$
 */
public class BasicFactory implements Factory
{
    public BasicFactory()
    {
    }

    /**
     * Static method to create an object from a factory and configuration source.
     *
     * @param key          the key identifying the factory in {@link Configuration}.
     * @param configSource the configuration source. May be any of the types listed for {@link
     *                     #createFromConfigSource(Object)}
     *
     * @return a new instance of the requested object.
     *
     * @throws IllegalArgumentException if the factory key is null, or if the configuration source is null or an empty
     *                                  string.
     */
    public static Object create(String key, Object configSource)
    {
        if (WWUtil.isEmpty(key))
        {
            String msg = Logging.getMessage("nullValue.KeyIsNull");
            throw new IllegalArgumentException(msg);
        }

        if (WWUtil.isEmpty(configSource))
        {
            String msg = Logging.getMessage("nullValue.SourceIsNull");
            throw new IllegalArgumentException(msg);
        }

        Factory factory = (Factory) WorldWind.createConfigurationComponent(key);
        return factory.createFromConfigSource(configSource);
    }

    /**
     * Creates an object from a general configuration source. The source can be one of the following: <ul> <li>{@link
     * java.net.URL}</li> <li>{@link java.io.File}</li> <li>{@link java.io.InputStream}</li> <li>{@link
     * org.w3c.dom.Element}</li> <li>{@link String} holding a file name, a name of a resource on the classpath, or a
     * string representation of a URL</li></ul>
     * <p/>
     *
     * @param configSource the configuration source. See above for supported types.
     *
     * @return the new object.
     *
     * @throws IllegalArgumentException if the configuration source is null or an empty string.
     * @throws gov.nasa.worldwind.exception.WWUnrecognizedException
     *                                  if the source type is unrecognized.
     * @throws gov.nasa.worldwind.exception.WWRuntimeException
     *                                  if object creation fails. The exception indicating the source of the failure is
     *                                  included as the {@link Exception#initCause(Throwable)}.
     */
    public Object createFromConfigSource(Object configSource)
    {
        if (WWUtil.isEmpty(configSource))
        {
            String msg = Logging.getMessage("nullValue.SourceIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Object o = null;

        try
        {
            if (configSource instanceof Element)
            {
                o = this.doCreateFromElement((Element) configSource);
            }
            else
            {
                Document doc = WWXML.openDocument(configSource);
                if (doc != null)
                    o = this.doCreateFromElement(doc.getDocumentElement());
            }
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("generic.CreationFromConfigFileFailed", configSource);
            throw new WWRuntimeException(msg, e);
        }

        return o;
    }

    protected Object doCreateFromElement(Element domElement) throws Exception
    {
        return null;
    }
}
