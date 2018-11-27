/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.AVList;

import java.beans.PropertyChangeListener;

/**
 * An interface provided by the major World Wind components to provide attribute-value list management and property
 * change management. Classifies implementers as property-change listeners, allowing them to receive property-change
 * events.
 *
 * @author dcollins
 * @version $Id$
 */
public interface WWObject extends AVList, PropertyChangeListener
{
}
