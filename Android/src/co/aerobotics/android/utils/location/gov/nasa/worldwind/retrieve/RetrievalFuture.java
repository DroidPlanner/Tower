/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.retrieve;

/**
 * @author Tom Gaskins
 * @version $Id$
 */
public interface RetrievalFuture extends java.util.concurrent.Future<Retriever>
{
    Retriever getRetriever();
}
