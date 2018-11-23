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
public class Vec4
{
    public double x;
    public double y;
    public double z;
    public double w;

    public Vec4()
    {
        this.w = 1;
    }

    public Vec4(double x, double y)
    {
        this.x = x;
        this.y = y;
        this.w = 1;
    }

    public Vec4(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = 1;
    }

    public Vec4(double x, double y, double z, double w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4 copy()
    {
        return new Vec4(this.x, this.y, this.z, this.w);
    }

    public Vec4 set(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        this.w = vec.w;

        return this;
    }

    public Vec4 set(double x, double y)
    {
        this.x = x;
        this.y = y;
        this.z = 0;
        this.w = 1;

        return this;
    }

    public Vec4 set(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = 1;

        return this;
    }

    public Vec4 set(double x, double y, double z, double w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;

        return this;
    }

    public Vec4 setArray3f(float[] array, int offset)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (array.length < 3)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", array.length);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (offset < 0 || offset + 3 > array.length)
        {
            String msg = Logging.getMessage("generic.OffsetIsInvalid", offset);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x = array[offset];
        this.y = array[offset + 1];
        this.z = array[offset + 2];
        this.w = 1;

        return this;
    }

    public void setPointOnLine3(Vec4 origin, double t, Vec4 direction)
    {
        if (origin == null)
        {
            String msg = Logging.getMessage("nullValue.OriginIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (direction == null)
        {
            String msg = Logging.getMessage("nullValue.DirectionIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x = origin.x + direction.x * t;
        this.y = origin.y + direction.y * t;
        this.z = origin.z + direction.z * t;
    }

    public double getLength3()
    {
        return Math.sqrt(this.getLengthSquared3());
    }

    public double getLengthSquared3()
    {
        return this.x * this.x
            + this.y * this.y
            + this.z * this.z;
    }

    public Vec4 normalize3()
    {
        double length = this.getLength3();
        if (length == 0)
            return this; // Vector has zero length.

        return new Vec4(
            this.x / length,
            this.y / length,
            this.z / length);
    }

    public Vec4 normalize3AndSet()
    {
        double length = this.getLength3();
        if (length == 0)
            return this; // Vector has zero length.

        this.x /= length;
        this.y /= length;
        this.z /= length;

        return this;
    }

    public Vec4 normalize3AndSet(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        double length = vec.getLength3();
        if (length == 0)
        {
            this.x = vec.x;
            this.y = vec.y;
            this.z = vec.z;
        }
        else
        {
            this.x = vec.x / length;
            this.y = vec.y / length;
            this.z = vec.z / length;
        }

        return this;
    }

    public Vec4 invert3()
    {
        return new Vec4(
            -this.x,
            -this.y,
            -this.z);
    }

    public Vec4 invert3AndSet()
    {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;

        return this;
    }

    public Vec4 abs3()
    {
        return new Vec4(
            Math.abs(this.x),
            Math.abs(this.y),
            Math.abs(this.z));
    }

    public Vec4 abs3AndSet()
    {
        this.x = Math.abs(this.x);
        this.y = Math.abs(this.y);
        this.z = Math.abs(this.z);

        return this;
    }

    public double dot3(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.x * vec.x
            + this.y * vec.y
            + this.z * vec.z;
    }

    public double dot4(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.x * vec.x
            + this.y * vec.y
            + this.z * vec.z
            + this.w * vec.w;
    }

    public double dotSelf3()
    {
        return this.x * this.x
            + this.y * this.y
            + this.z * this.z;
    }

    public double dotSelf4()
    {
        return this.x * this.x
            + this.y * this.y
            + this.z * this.z
            + this.w * this.w;
    }

    public double distanceTo3(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return Math.sqrt(this.distanceToSquared3(vec));
    }

    public double distanceToSquared3(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        double tmp;
        double result = 0.0;
        tmp = this.x - vec.x;
        result += tmp * tmp;
        tmp = this.y - vec.y;
        result += tmp * tmp;
        tmp = this.z - vec.z;
        result += tmp * tmp;
        return result;
    }

    public Angle angleBetween3(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        double a_dot_b = this.dot3(vec);
        // Compute the sum of magnitudes.
        double length = this.getLength3() * vec.getLength3();
        // Normalize the dot product, if necessary.
        if (!(length == 0) && (length != 1.0))
            a_dot_b /= length;

        // The normalized dot product should be in the range [-1, 1]. Otherwise the result is an error from floating
        // point roundoff. So if a_dot_b is less than -1 or greater than +1, we treat it as -1 and +1 respectively.
        if (a_dot_b < -1.0)
            a_dot_b = -1.0;
        else if (a_dot_b > 1.0)
            a_dot_b = 1.0;

        // Angle is arc-cosine of normalized dot product.
        return Angle.fromRadians(Math.acos(a_dot_b));
    }

    public Vec4 add3(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x + vec.x,
            this.y + vec.y,
            this.z + vec.z);
    }

    public Vec4 add3(double x, double y, double z)
    {
        return new Vec4(
            this.x + x,
            this.y + y,
            this.z + z);
    }

    public Vec4 add3AndSet(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;

        return this;
    }

    public Vec4 add3AndSet(double x, double y, double z)
    {
        this.x += x;
        this.y += y;
        this.z += z;

        return this;
    }

    public Vec4 add3AndSet(Vec4 lhs, Vec4 rhs)
    {
        if (lhs == null)
        {
            String msg = Logging.getMessage("nullValue.LhsIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (rhs == null)
        {
            String msg = Logging.getMessage("nullValue.RhsIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x = lhs.x + rhs.x;
        this.y = lhs.y + rhs.y;
        this.z = lhs.z + rhs.z;

        return this;
    }

    public Vec4 subtract3(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x - vec.x,
            this.y - vec.y,
            this.z - vec.z);
    }

    public Vec4 subtract3(double x, double y, double z)
    {
        return new Vec4(
            this.x - x,
            this.y - y,
            this.z - z);
    }

    public Vec4 subtract3AndSet(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;

        return this;
    }

    public Vec4 subtract3AndSet(double x, double y, double z)
    {
        this.x -= x;
        this.y -= y;
        this.z -= z;

        return this;
    }

    public Vec4 subtract3AndSet(Vec4 lhs, Vec4 rhs)
    {
        if (lhs == null)
        {
            String msg = Logging.getMessage("nullValue.LhsIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (rhs == null)
        {
            String msg = Logging.getMessage("nullValue.RhsIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x = lhs.x - rhs.x;
        this.y = lhs.y - rhs.y;
        this.z = lhs.z - rhs.z;

        return this;
    }

    public Vec4 multiply3(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x * vec.x,
            this.y * vec.y,
            this.z * vec.z);
    }

    public Vec4 multiply3(double value)
    {
        return new Vec4(
            this.x * value,
            this.y * value,
            this.z * value);
    }

    public Vec4 multiply3AndSet(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x *= vec.x;
        this.y *= vec.y;
        this.z *= vec.z;

        return this;
    }

    public Vec4 multiply3AndSet(double value)
    {
        this.x *= value;
        this.y *= value;
        this.z *= value;

        return this;
    }

    public Vec4 multiply3AndSet(Vec4 lhs, Vec4 rhs)
    {
        if (lhs == null)
        {
            String msg = Logging.getMessage("nullValue.LhsIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (rhs == null)
        {
            String msg = Logging.getMessage("nullValue.RhsIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x = lhs.x * rhs.x;
        this.y = lhs.y * rhs.y;
        this.z = lhs.z * rhs.z;

        return this;
    }

    public Vec4 divide3(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x / vec.x,
            this.y / vec.y,
            this.z / vec.z);
    }

    public Vec4 divide3(double value)
    {
        if (value == 0)
        {
            String msg = Logging.getMessage("generic.DivideByZero");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x / value,
            this.y / value,
            this.z / value);
    }

    public Vec4 divide3AndSet(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x /= vec.x;
        this.y /= vec.y;
        this.z /= vec.z;

        return this;
    }

    public Vec4 divide3AndSet(double value)
    {
        this.x /= value;
        this.y /= value;
        this.z /= value;

        return this;
    }

    public Vec4 divide3AndSet(Vec4 lhs, Vec4 rhs)
    {
        if (lhs == null)
        {
            String msg = Logging.getMessage("nullValue.LhsIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (rhs == null)
        {
            String msg = Logging.getMessage("nullValue.RhsIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x = lhs.x / rhs.x;
        this.y = lhs.y / rhs.y;
        this.z = lhs.z / rhs.z;

        return this;
    }

    /**
     * Returns the arithmetic mean of the x, y, z coordinates of the specified points Iterable. This returns null if the
     * Iterable contains no points, or if all of the points are null.
     *
     * @param points the Iterable of points which define the returned arithmetic mean.
     *
     * @return the arithmetic mean point of the specified points Iterable, or null if the Iterable is empty or contains
     *         only null points.
     *
     * @throws IllegalArgumentException if the Iterable is null.
     */
    public static Vec4 computeAverage3(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String msg = Logging.getMessage("nullValue.PointListIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        int count = 0;
        double x = 0;
        double y = 0;
        double z = 0;
        double w = 0;

        for (Vec4 vec : points)
        {
            if (vec == null)
                continue;

            count++;
            x += vec.x;
            y += vec.y;
            z += vec.z;
            w += vec.w;
        }

        if (count == 0)
            return null;

        return new Vec4(
            x / (double) count,
            y / (double) count,
            z / (double) count,
            w / (double) count);
    }

    /**
     * Computes the arithmetic mean of the x, y, z coordinates of the specified points Iterable and sets this vector to
     * the computed result. This does nothing if the Iterable contains no points, or if all of the points are null.
     *
     * @param points the Iterable of points which define the returned arithmetic mean.
     *
     * @return a reference to this vector.
     *
     * @throws IllegalArgumentException if the Iterable is null.
     */
    public Vec4 computeAverage3AndSet(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String msg = Logging.getMessage("nullValue.PointListIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        int count = 0;
        double x = 0;
        double y = 0;
        double z = 0;

        for (Vec4 vec : points)
        {
            if (vec == null)
                continue;

            count++;
            x += vec.x;
            y += vec.y;
            z += vec.z;
        }

        if (count > 0)
            return this;

        this.x = x / (double) count;
        this.y = y / (double) count;
        this.z = z / (double) count;

        return this;
    }

    public Vec4 cross3(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (this.y * vec.z) - (this.z * vec.y),
            (this.z * vec.x) - (this.x * vec.z),
            (this.x * vec.y) - (this.y * vec.x));
    }

    public Vec4 cross3AndSet(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.LhsIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        double x = this.x;
        double y = this.y;
        double z = this.z;

        this.x = (y * vec.z) - (z * vec.y);
        this.y = (z * vec.x) - (x * vec.z);
        this.z = (x * vec.y) - (y * vec.x);

        return this;
    }

    public Vec4 cross3AndSet(Vec4 lhs, Vec4 rhs)
    {
        if (lhs == null)
        {
            String msg = Logging.getMessage("nullValue.LhsIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (rhs == null)
        {
            String msg = Logging.getMessage("nullValue.RhsIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x = (lhs.y * rhs.z) - (lhs.z * rhs.y);
        this.y = (lhs.z * rhs.x) - (lhs.x * rhs.z);
        this.z = (lhs.x * rhs.y) - (lhs.y * rhs.x);

        return this;
    }

    public Vec4 transformBy4(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (matrix.m[0] * this.x) + (matrix.m[1] * this.y) + (matrix.m[2] * this.z) + (matrix.m[3] * this.w),
            (matrix.m[4] * this.x) + (matrix.m[5] * this.y) + (matrix.m[6] * this.z) + (matrix.m[7] * this.w),
            (matrix.m[8] * this.x) + (matrix.m[9] * this.y) + (matrix.m[10] * this.z) + (matrix.m[11] * this.w),
            (matrix.m[12] * this.x) + (matrix.m[13] * this.y) + (matrix.m[14] * this.z) + (matrix.m[15] * this.w));
    }

    public Vec4 transformBy4AndSet(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        double x = this.x;
        double y = this.y;
        double z = this.z;
        double w = this.w;

        this.x = (matrix.m[0] * x) + (matrix.m[1] * y) + (matrix.m[2] * z) + (matrix.m[3] * w);
        this.y = (matrix.m[4] * x) + (matrix.m[5] * y) + (matrix.m[6] * z) + (matrix.m[7] * w);
        this.z = (matrix.m[8] * x) + (matrix.m[9] * y) + (matrix.m[10] * z) + (matrix.m[11] * w);
        this.w = (matrix.m[12] * x) + (matrix.m[13] * y) + (matrix.m[14] * z) + (matrix.m[15] * w);

        return this;
    }

    public Vec4 transformBy4AndSet(Vec4 vec, Matrix matrix)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.x = (matrix.m[0] * vec.x) + (matrix.m[1] * vec.y) + (matrix.m[2] * vec.z) + (matrix.m[3] * vec.w);
        this.y = (matrix.m[4] * vec.x) + (matrix.m[5] * vec.y) + (matrix.m[6] * vec.z) + (matrix.m[7] * vec.w);
        this.z = (matrix.m[8] * vec.x) + (matrix.m[9] * vec.y) + (matrix.m[10] * vec.z) + (matrix.m[11] * vec.w);
        this.w = (matrix.m[12] * vec.x) + (matrix.m[13] * vec.y) + (matrix.m[14] * vec.z) + (matrix.m[15] * vec.w);

        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        Vec4 that = (Vec4) o;
        return this.x == that.x
            && this.y == that.y
            && this.z == that.z
            && this.w == that.w;
    }

    @Override
    public int hashCode()
    {
        int result;
        long tmp;
        tmp = Double.doubleToLongBits(this.x);
        result = (int) (tmp ^ (tmp >>> 32));
        tmp = Double.doubleToLongBits(this.y);
        result = 29 * result + (int) (tmp ^ (tmp >>> 32));
        tmp = Double.doubleToLongBits(this.z);
        result = 29 * result + (int) (tmp ^ (tmp >>> 32));
        tmp = Double.doubleToLongBits(this.w);
        result = 29 * result + (int) (tmp ^ (tmp >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(this.x).append(", ");
        sb.append(this.y).append(", ");
        sb.append(this.z).append(", ");
        sb.append(this.w);
        sb.append(")");
        return sb.toString();
    }

    public void toArray3f(float[] array, int offset)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (array.length < 3)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", array.length);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (offset < 0 || offset + 3 > array.length)
        {
            String msg = Logging.getMessage("generic.OffsetIsInvalid", offset);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        array[offset] = (float) this.x;
        array[offset + 1] = (float) this.y;
        array[offset + 2] = (float) this.z;
    }
}
