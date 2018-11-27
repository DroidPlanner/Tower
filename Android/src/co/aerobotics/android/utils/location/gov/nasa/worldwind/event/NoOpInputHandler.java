/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.event;

import android.view.*;
import android.view.View;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.*;

/**
 * @author dcollins
 * @version $Id$
 */
public class NoOpInputHandler extends WWObjectImpl implements InputHandler
{
    public NoOpInputHandler()
    {
    }

    public WorldWindow getEventSource()
    {
        return null;
    }

    public void setEventSource(WorldWindow eventSource)
    {
    }

    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        return false;
    }
}
