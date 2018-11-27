/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.event;

import android.view.View;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.*;

/**
 * @author dcollins
 * @version $Id$
 */
public interface InputHandler extends WWObject, View.OnTouchListener
{
    WorldWindow getEventSource();

    void setEventSource(WorldWindow eventSource);
}
