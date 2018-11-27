/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.exception;

/**
 * @author dcollins
 * @version $Id$
 */
public class WWUnrecognizedException extends WWRuntimeException
{
    /**
     * Construct an exception with a message string.
     *
     * @param msg the message.
     */
    public WWUnrecognizedException(String msg)
    {
        super(msg);
    }

    /**
     * Construct an exception with a message string and a initial-cause exception.
     *
     * @param msg the message.
     * @param t   the exception causing this exception.
     */
    public WWUnrecognizedException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
