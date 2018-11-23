package co.aerobotics.android.utils.location.gov.nasa.worldwind.geom;

/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

/**
 * Represents a <code>Plane</code> in Cartesian coordinates, defined by a normal vector to the plane and a signed scalar
 * value proportional to the distance of the plane from the origin. The sign of the value is relative to the direction
 * of the plane normal.
 *
 * @author Tom Gaskins
 * @version $Id$
 */
public class Plane
{
    // the plane normal and proportional distance. The vector is not necessarily a unit vector.
    protected final Vec4 vector;

    public Plane()
    {
        this.vector = new Vec4();
    }

    /**
     * Constructs a plane from a 4-D vector giving the plane normal vector and distance.
     *
     * @param vec a 4-D vector indicating the plane's normal vector and distance. The normal need not be unit length.
     *
     * @throws IllegalArgumentException if the vector is null.
     */
    public Plane(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (vec.getLengthSquared3() == 0)
        {
            String msg = Logging.getMessage("generic.VectorIsZero");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.vector = vec;
    }

    /**
     * Constructs a plane from four values giving the plane normal vector and distance.
     *
     * @param nx the X component of the plane normal vector.
     * @param ny the Y component of the plane normal vector.
     * @param nz the Z component of the plane normal vector.
     * @param d  the plane distance.
     *
     * @throws IllegalArgumentException if the normal vector components define the zero vector (all values are zero).
     */
    public Plane(double nx, double ny, double nz, double d)
    {
        if (nx == 0 && ny == 0 && nz == 0)
        {
            String msg = Logging.getMessage("generic.VectorIsZero");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.vector = new Vec4(nx, ny, nz, d);
    }

    public Plane copy()
    {
        return new Plane(this.vector.copy());
    }

    public Plane set(Plane plane)
    {
        if (plane == null)
        {
            String msg = Logging.getMessage("nullValue.PlaneIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.vector.set(plane.vector);

        return this;
    }

    public Plane set(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (vec.getLengthSquared3() == 0)
        {
            String msg = Logging.getMessage("generic.VectorIsZero");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.vector.set(vec);

        return this;
    }

    public Plane set(double nx, double ny, double nz, double d)
    {
        if (nx == 0 && ny == 0 && nz == 0)
        {
            String msg = Logging.getMessage("generic.VectorIsZero");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.vector.set(nx, ny, nz, d);

        return this;
    }

    public Vec4 getVector()
    {
        return this.vector;
    }

    /**
     * Returns the plane's normal vector.
     *
     * @return the plane's normal vector.
     */
    public Vec4 getNormal()
    {
        return this.vector;
    }

    /**
     * Returs the plane distance.
     *
     * @return the plane distance.
     */
    public double getDistance()
    {
        return this.vector.w;
    }

    /**
     * Returns a normalized version of this plane. The normalized plane's normal vector is unit length and its distance
     * is D/|N| where |N| is the length of this plane's normal vector.
     *
     * @return a normalized copy of this Plane.
     */
    public Plane normalize()
    {
        double length = this.vector.getLength3();
        if (length == 0) // should not happen, but check to be sure.
            return this;

        return new Plane(new Vec4(
            this.vector.x / length,
            this.vector.y / length,
            this.vector.z / length,
            this.vector.w / length));
    }

    public Plane normalizeAndSet()
    {
        double length = this.vector.getLength3();
        if (length == 0) // should not happen, but check to be sure.
            return this;

        this.vector.x /= length;
        this.vector.y /= length;
        this.vector.z /= length;
        this.vector.w /= length;

        return this;
    }

    /**
     * Calculates the 4-D dot product of this plane with a vector.
     *
     * @param vec the vector.
     *
     * @return the dot product of the plane and the vector.
     *
     * @throws IllegalArgumentException if the vector is null.
     */
    public double dot(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.vector.x * vec.x + this.vector.y * vec.y + this.vector.z * vec.z + this.vector.w * vec.w;
    }

    public double distanceTo(Vec4 point)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.vector.x * point.x + this.vector.y * point.y + this.vector.z * point.z + this.vector.w * point.w;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof Plane))
            return false;

        Plane that = (Plane) o;
        return this.vector != null ? this.vector.equals(that.vector) : that.vector == null;
    }

    @Override
    public int hashCode()
    {
        return this.vector != null ? this.vector.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return this.vector.toString();
    }
}
