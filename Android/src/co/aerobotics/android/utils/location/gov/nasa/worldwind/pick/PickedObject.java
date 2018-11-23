/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.pick;

import android.graphics.Point;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.Position;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.layers.Layer;

/**
 * @author lado
 * @version $Id$
 */
public class PickedObject extends AVListImpl
{
    protected final Point pickPoint;
    protected final int colorCode;
    protected final Object userObject;
    protected boolean isOnTop;
    protected boolean isTerrain;

    public PickedObject(int colorCode, Object userObject)
    {
        this.pickPoint = null;
        this.colorCode = colorCode;
        this.userObject = userObject;
        this.isOnTop = false;
        this.isTerrain = false;
    }

    public PickedObject(int colorCode, Object userObject, Position position, boolean isTerrain)
    {
        this.pickPoint = null;
        this.colorCode = colorCode;
        this.userObject = userObject;
        this.isOnTop = false;
        this.isTerrain = isTerrain;

        if (position != null)
            this.setPosition(position);
    }

    public PickedObject(Point pickPoint, int colorCode, Object userObject, Position position, boolean isTerrain)
    {
        this.pickPoint = pickPoint;
        this.colorCode = colorCode;
        this.userObject = userObject;
        this.isOnTop = false;
        this.isTerrain = isTerrain;

        if (position != null)
            this.setPosition(position);
    }

    public Point getPickPoint()
    {
        return pickPoint;
    }

    public int getColorCode()
    {
        return this.colorCode;
    }

    public Object getObject()
    {
        return userObject;
    }

    public boolean isOnTop()
    {
        return this.isOnTop;
    }

    public void setOnTop()
    {
        this.isOnTop = true;
    }

    public boolean isTerrain()
    {
        return this.isTerrain;
    }

    public Layer getParentLayer()
    {
        return (Layer) this.getValue(AVKey.PICKED_OBJECT_PARENT_LAYER);
    }

    public void setParentLayer(Layer layer)
    {
        this.setValue(AVKey.PICKED_OBJECT_PARENT_LAYER, layer);
    }

    public boolean hasPosition()
    {
        return this.hasKey(AVKey.POSITION);
    }

    public Position getPosition()
    {
        return (Position) this.getValue(AVKey.POSITION);
    }

    public void setPosition(Position position)
    {
        this.setValue(AVKey.POSITION, position);
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        PickedObject that = (PickedObject) o;

        if (this.colorCode != that.colorCode)
            return false;
        if (this.isOnTop != that.isOnTop)
            return false;
        //noinspection RedundantIfStatement
        if (this.userObject != null ? !this.userObject.equals(that.userObject) : that.userObject != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = this.colorCode;
        result = 31 * result + (this.userObject != null ? this.userObject.hashCode() : 0);
        result = 31 * result + (this.isOnTop ? 1 : 0);
        return result;
    }
}

