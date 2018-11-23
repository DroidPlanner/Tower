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
public class Position extends LatLon
{
    public double elevation;

    public Position()
    {
    }

    public Position(Angle latitude, Angle longitude, double elevation)
    {
        super(latitude, longitude);
        this.elevation = elevation;
    }

    public static Position fromDegrees(double latitude, double longitude, double elevation)
    {
        return new Position(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), elevation);
    }

    public static Position fromRadians(double latitude, double longitude, double elevation)
    {
        return new Position(Angle.fromRadians(latitude), Angle.fromRadians(longitude), elevation);
    }

    public Position copy()
    {
        return new Position(this.latitude.copy(), this.longitude.copy(), this.elevation);
    }

    public Position set(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        super.set(position.latitude, position.longitude);
        this.elevation = position.elevation;

        return this;
    }

    public Position set(LatLon location, double elevation)
    {
        if (location == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        super.set(location.latitude, location.longitude);
        this.elevation = elevation;

        return this;
    }

    public Position set(Angle latitude, Angle longitude, double elevation)
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

        super.set(latitude, longitude);
        this.elevation = elevation;

        return this;
    }

    public Position setDegrees(double latitude, double longitude, double elevation)
    {
        super.setDegrees(latitude, longitude);
        this.elevation = elevation;

        return this;
    }

    public Position setRadians(double latitude, double longitude, double elevation)
    {
        super.setRadians(latitude, longitude);
        this.elevation = elevation;

        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        Position that = (Position) o;
        return this.elevation == that.elevation;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        long temp;
        temp = this.elevation != +0.0d ? Double.doubleToLongBits(this.elevation) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(this.latitude.toString()).append(", ");
        sb.append(this.longitude.toString()).append(", ");
        sb.append(this.elevation);
        sb.append(")");
        return sb.toString();
    }
}
