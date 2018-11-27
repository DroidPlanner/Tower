/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.globes;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.AVKey;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.terrain.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.WWUtil;

/**
 * @author dcollins
 * @version $Id$
 */
public class Earth extends EllipsoidalGlobe
{
    public static final double WGS84_EQUATORIAL_RADIUS = 6378137.0; // ellipsoid equatorial getRadius, in meters
    protected static final double WGS84_POLAR_RADIUS = 6356752.3; // ellipsoid polar getRadius, in meters
    protected static final double WGS84_ES = 0.00669437999013; // eccentricity squared, semi-major axis

    public Earth()
    {
        super(WGS84_EQUATORIAL_RADIUS, WGS84_POLAR_RADIUS, WGS84_ES, createElevationModel());
    }

    protected static ElevationModel createElevationModel()
    {
        String configFile = Configuration.getStringValue(AVKey.EARTH_ELEVATION_MODEL_CONFIG_FILE);
        if (WWUtil.isEmpty(configFile))
            return new ZeroElevationModel();

        return (ElevationModel) BasicFactory.create(AVKey.ELEVATION_MODEL_FACTORY, configFile);
    }

    public String toString()
    {
        return "Earth";
    }
}
