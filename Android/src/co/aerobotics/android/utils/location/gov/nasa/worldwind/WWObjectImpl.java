/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.AVListImpl;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

import java.beans.PropertyChangeEvent;

/**
 * Implements <code>WWObject</code> functionality. Meant to be either subclassed or aggregated by classes implementing
 * <code>WWObject</code>.
 *
 * @author dcollins
 * @version $Id$
 */
public class WWObjectImpl extends AVListImpl implements WWObject
{
    /** Constructs a new <code>WWObjectImpl</code>. */
    public WWObjectImpl()
    {
    }

    public WWObjectImpl(Object source)
    {
        super(source);
    }

    /**
     * The property change listener for <em>this</em> instance. Receives property change notifications that this
     * instance has registered with other property change notifiers.
     *
     * @param event the property change event.
     *
     * @throws IllegalArgumentException if <code>propertyChangeEvent</code> is null
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if (event == null)
        {
            String msg = Logging.getMessage("nullValue.EventIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Notify all *my* listeners of the change that I caught
        super.firePropertyChange(event);
    }
}
