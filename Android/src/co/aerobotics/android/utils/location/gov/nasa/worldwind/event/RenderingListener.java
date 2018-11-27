/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.event;

import java.util.EventListener;

/**
 * @author tag
 * @version $Id$
 */
public interface RenderingListener extends EventListener
{
    void stageChanged(RenderingEvent event);
}
