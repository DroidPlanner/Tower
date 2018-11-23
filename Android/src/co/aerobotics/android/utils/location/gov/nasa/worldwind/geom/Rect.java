/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.geom;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id$
 */
public class Rect
{
    public double x;
    public double y;
    public double width;
    public double height;

    public Rect()
    {
    }

    public Rect(double x, double y, double width, double height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rect copy()
    {
        return new Rect(this.x, this.y, this.width, this.height);
    }

    public Rect set(Rect rect)
    {
        if (rect == null)
        {
            String msg = Logging.getMessage("nullValue.RectIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x = rect.x;
        this.y = rect.y;
        this.width = rect.width;
        this.height = rect.height;

        return this;
    }

    public Rect set(double x, double y, double width, double height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        return this;
    }
}
