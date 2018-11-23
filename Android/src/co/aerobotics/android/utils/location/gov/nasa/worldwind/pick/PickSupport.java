/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.pick;

import android.graphics.Point;
import android.opengl.GLES20;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.Position;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.layers.Layer;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.render.DrawContext;

import java.util.*;

/**
 * @author tag
 * @version $Id$
 */
public class PickSupport
{
    protected Map<Integer, PickedObject> pickableObjects = new HashMap<Integer, PickedObject>();

    public PickSupport()
    {
    }

    public void clearPickList()
    {
        this.getPickableObjects().clear();
    }

    public void addPickableObject(int colorCode, Object o, Position position, boolean isTerrain)
    {
        this.getPickableObjects().put(colorCode, new PickedObject(colorCode, o, position, isTerrain));
    }

    public void addPickableObject(int colorCode, Object o, Position position)
    {
        this.getPickableObjects().put(colorCode, new PickedObject(colorCode, o, position, false));
    }

    public void addPickableObject(int colorCode, Object o)
    {
        this.getPickableObjects().put(colorCode, new PickedObject(colorCode, o));
    }

    public void addPickableObject(PickedObject po)
    {
        this.getPickableObjects().put(po.getColorCode(), po);
    }

    public PickedObject getTopObject(DrawContext dc, Point pickPoint)
    {
        if (this.getPickableObjects().isEmpty())
            return null;

        int colorCode = dc.getPickColor(pickPoint);
        if (colorCode == dc.getClearColor())
            return null;

        PickedObject pickedObject = getPickableObjects().get(colorCode);
        if (pickedObject == null)
            return null;

        return pickedObject;
    }

    public PickedObject resolvePick(DrawContext dc, Point pickPoint, Layer layer)
    {
        PickedObject pickedObject = this.getTopObject(dc, pickPoint);
        if (pickedObject != null)
        {
            if (layer != null)
                pickedObject.setParentLayer(layer);

            dc.addPickedObject(pickedObject);
        }

        this.clearPickList();

        return pickedObject;
    }

    protected Map<Integer, PickedObject> getPickableObjects()
    {
        return this.pickableObjects;
    }

    /**
     * Indicates whether two picked objects refer to the same user object.
     *
     * @param a the first picked object.
     * @param b the second picked object.
     *
     * @return true if both objects are not null and they refer to the same user object, otherwise false.
     */
    public static boolean areSelectionsTheSame(PickedObject a, PickedObject b)
    {
        return a != null && b != null && a.getObject() == b.getObject();
    }
}

