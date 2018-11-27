/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.retrieve;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.logging.Level;

/**
 * Retrieves resources identified by a jar url, which has the form jar:<url>!/{entry}, as in:
 * jar:http://www.foo.com/bar/baz.jar!/COM/foo/Quux.class. See {@link java.net.JarURLConnection} for a full description
 * of jar URLs.
 *
 * @author tag
 * @version $Id$
 */
public class JarRetriever extends URLRetriever
{
    private int responseCode;
    private String responseMessage;

    public JarRetriever(URL url, RetrievalPostProcessor postProcessor)
    {
        super(url, postProcessor);
    }

    public int getResponseCode()
    {
        return this.responseCode;
    }

    public String getResponseMessage()
    {
        return this.responseMessage;
    }

    @Override
    protected ByteBuffer doRead(URLConnection connection) throws Exception
    {
        if (connection == null)
        {
            String msg = Logging.getMessage("nullValue.ConnectionIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        JarURLConnection htpc = (JarURLConnection) connection;
        this.responseCode = htpc.getContentLength() >= 0 ? HttpURLConnection.HTTP_OK : -1;
        this.responseMessage = this.responseCode >= 0 ? "OK" : "FAILED";

        String contentType = connection.getContentType();

        String msg = Logging.getMessage("HTTPRetriever.ResponseInfo", this.responseCode, connection.getContentLength(),
            contentType != null ? contentType : "content type not returned", connection.getURL());
        Logging.verbose(msg);

        if (this.responseCode == HttpURLConnection.HTTP_OK) // intentionally re-using HTTP constant
            return super.doRead(connection);

        return null;
    }
}
