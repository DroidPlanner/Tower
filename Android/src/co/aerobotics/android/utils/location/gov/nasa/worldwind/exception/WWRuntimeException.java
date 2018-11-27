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
public class WWRuntimeException extends RuntimeException
{
    /**
     * Construct an exception with a message string.
     *
     * @param msg the message.
     */
    public WWRuntimeException(String msg)
    {
        super(msg);
    }

    /**
     * Construct an exception from an initial-cause exception.
     *
     * @param throwable the exception causing this exception.
     */
    public WWRuntimeException(Throwable throwable)
    {
        super(throwable);
    }

    /**
     * Construct an exception with a message string and a initial-cause exception.
     *
     * @param msg       the message.
     * @param throwable the exception causing this exception.
     */
    public WWRuntimeException(String msg, Throwable throwable)
    {
        super(msg, throwable);
    }
}
