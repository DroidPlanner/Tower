/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.globes.Globe;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.layers.LayerList;

/**
 * @author dcollins
 * @version $Id$
 */
public interface Model extends WWObject
{
    Globe getGlobe();

    void setGlobe(Globe globe);

    LayerList getLayers();

    void setLayers(LayerList layers);
}
