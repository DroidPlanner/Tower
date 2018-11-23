/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.wms;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

import java.net.*;

/**
 * @author tag
 * @version $Id: CapabilitiesRequest.java 1 2011-07-16 23:22:47Z dcollins $
 */
public final class CapabilitiesRequest extends Request
{
    /** Construct an OGC GetCapabilities request using the default service. */
    public CapabilitiesRequest()
    {
    }

    /**
     * Constructs a request for the default service, WMS, and a specified server.
     *
     * @param uri the address of the web service.
     *
     * @throws IllegalArgumentException if the uri is null.
     * @throws URISyntaxException       if the web service address is not a valid URI.
     */

    public CapabilitiesRequest(URI uri) throws URISyntaxException
    {
        super(uri, null);

        if (uri == null)
        {
            String message = Logging.getMessage("nullValue.URIIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Constructs a request for a specified service at a specified server.
     *
     * @param uri     the address of the web service.
     * @param service the service name. Common names are WMS, WFS, WCS, etc.
     *
     * @throws IllegalArgumentException if the uri or service name is null.
     * @throws URISyntaxException       if the web service address is not a valid URI.
     */
    public CapabilitiesRequest(URI uri, String service) throws URISyntaxException
    {
        super(uri, service);

        if (uri == null)
        {
            String message = Logging.getMessage("nullValue.URIIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (service == null)
        {
            String message = Logging.getMessage("nullValue.WMSServiceNameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    protected void initialize(String service)
    {
        super.initialize(service);
        this.setParam("REQUEST", "GetCapabilities");
        this.setParam("VERSION", "1.3.0");
    }
}
