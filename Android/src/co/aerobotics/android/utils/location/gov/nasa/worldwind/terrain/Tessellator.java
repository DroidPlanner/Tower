/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.terrain;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.WWObject;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.render.DrawContext;

/**
 * @author dcollins
 * @version $Id$
 */
public interface Tessellator extends WWObject
{
    SectorGeometryList tessellate(DrawContext dc);
}
