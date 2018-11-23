/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.pick;

import java.util.ArrayList;

/**
 * @author tag
 * @version $Id$
 */
public class PickedObjectList extends ArrayList<PickedObject>
{
    public PickedObjectList()
    {
    }

    public PickedObjectList(PickedObjectList list) // clone a shallow copy
    {
        super(list);
    }

    public boolean hasNonTerrainObjects()
    {
        return this.size() > 1 || (this.size() == 1 && this.getTerrainObject() == null);
    }

    public Object getTopObject()
    {
        PickedObject po = this.getTopPickedObject();
        return po != null ? po.getObject() : null;
    }

    public PickedObject getTopPickedObject()
    {
        int size = this.size();

        if (1 < size)
        {
            for (PickedObject po : this)
            {
                if (po.isOnTop())
                    return po;
            }
        }

        if (0 < size)
        {   // if we are here, then no objects were mark as 'top'
            return this.get(0);
        }

        return null;
    }

    public PickedObject getTerrainObject()
    {
        for (PickedObject po : this)
        {
            if (po.isTerrain())
                return po;
        }

        return null;
    }

    public PickedObject getMostRecentPickedObject()
    {
        return this.size() > 0 ? this.get(this.size() - 1) : null;
    }
}
