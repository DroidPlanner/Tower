/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.event;

import java.util.EventListener;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.retrieve.BulkRetrievable;

/**
 * Interface for listening for bulk-download events.
 *
 * @author tag
 * @version $Id: BulkRetrievalListener.java 1 2011-07-16 23:22:47Z dcollins $
 */
public interface BulkRetrievalListener extends EventListener
{
    /**
     * A bulk-download event occurred, either a succes, a failure or an extended event.
     *
     * @param event the event that occurred.
     * @see BulkRetrievable
     */
    void eventOccurred(BulkRetrievalEvent event);
}
