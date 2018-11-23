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
public class LatLon
{
    public final Angle latitude;
    public final Angle longitude;

    public LatLon()
    {
        this.latitude = new Angle();
        this.longitude = new Angle();
    }

    public LatLon(Angle latitude, Angle longitude)
    {
        if (latitude == null)
        {
            String msg = Logging.getMessage("nullValue.LatitudeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (longitude == null)
        {
            String msg = Logging.getMessage("nullValue.LongitudeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static LatLon fromDegrees(double latitude, double longitude)
    {
        return new LatLon(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude));
    }

    public static LatLon fromRadians(double latitude, double longitude)
    {
        return new LatLon(Angle.fromRadians(latitude), Angle.fromRadians(longitude));
    }

    public LatLon copy()
    {
        return new LatLon(this.latitude.copy(), this.longitude.copy());
    }

    public LatLon set(LatLon location)
    {
        if (location == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.latitude.set(location.latitude);
        this.longitude.set(location.longitude);

        return this;
    }

    public LatLon set(Angle latitude, Angle longitude)
    {
        if (latitude == null)
        {
            String msg = Logging.getMessage("nullValue.LatitudeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (longitude == null)
        {
            String msg = Logging.getMessage("nullValue.LongitudeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.latitude.set(latitude);
        this.longitude.set(longitude);

        return this;
    }

    public LatLon setDegrees(double latitude, double longitude)
    {
        this.latitude.setDegrees(latitude);
        this.longitude.setDegrees(longitude);

        return this;
    }

    public LatLon setRadians(double latitude, double longitude)
    {
        this.latitude.setRadians(latitude);
        this.longitude.setRadians(longitude);

        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        LatLon that = (LatLon) o;
        return this.latitude.equals(that.latitude) && this.longitude.equals(that.longitude);
    }

    @Override
    public int hashCode()
    {
        int result;
        result = this.latitude.hashCode();
        result = 29 * result + this.longitude.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(this.latitude.toString()).append(", ");
        sb.append(this.longitude.toString());
        sb.append(")");
        return sb.toString();
    }
}
