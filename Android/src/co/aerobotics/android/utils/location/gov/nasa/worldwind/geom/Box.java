/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package co.aerobotics.android.utils.location.gov.nasa.worldwind.geom;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;

/**
 * An arbitrarily oriented box, typically used as a oriented bounding volume for a collection of points or shapes. A
 * <code>Box</code> is defined by three orthogonal axes and two positions along each of those axes. Each of the
 * positions specifies the location of a box side along the respective axis. The three axes are named by convention "R",
 * "S" and "T", and are ordered by decreasing length -- R is the longest axis, followed by S and then T.
 *
 * @author tag
 * @version $Id$
 */
public class Box implements Extent
{
    protected final Vec4 bottomCenter; // point at center of box's longest axis
    protected final Vec4 topCenter; // point at center of box's longest axis
    protected final Vec4 center; // center of box
    protected final Vec4 r; // longest axis
    protected final Vec4 s; // next longest axis
    protected final Vec4 t; // shortest axis
    protected final Vec4 ru; // r axis unit normal
    protected final Vec4 su; // s axis unit normal
    protected final Vec4 tu; // t axis unit normal
    protected final double rLength; // length of r axis
    protected final double sLength; // length of s axis
    protected final double tLength; // length of t axis
    protected final Plane[] planes; // the six planes, with positive normals facing outwards
    // Temporary variables used in the high-frequency intersects method to avoid constant Vec4 allocations.
    protected Vec4 tmp1 = new Vec4();
    protected Vec4 tmp2 = new Vec4();
    protected Vec4 tmp3 = new Vec4();

    protected Box(Vec4 bottomCenter, Vec4 topCenter, Vec4 center, Vec4 r, Vec4 s, Vec4 t, Vec4 ru, Vec4 su, Vec4 tu,
        double rlength, double sLength, double tLength, Plane[] planes)
    {
        this.bottomCenter = bottomCenter;
        this.topCenter = topCenter;
        this.center = center;
        this.r = r;
        this.s = s;
        this.t = t;
        this.ru = ru;
        this.su = su;
        this.tu = tu;
        this.rLength = rlength;
        this.sLength = sLength;
        this.tLength = tLength;
        this.planes = planes;
    }

    /**
     * Construct a box from three specified unit axes and the locations of the box faces relative to those axes. The box
     * faces are specified by two scalar locations along each axis, each location indicating a face. The non-unit length
     * of an axis is the distance between its respective two locations. The longest side is specified first, followed by
     * the second longest side and then the shortest side.
     * <p/>
     * The axes are normally principal axes computed from a collection of points in order to form an oriented bounding
     * volume. See {@link WWMath#computePrincipalAxes(Iterable)}.
     * <p/>
     * Note: No check is made to ensure the order of the face locations.
     *
     * @param axes the unit-length axes.
     * @param rMin the location along the first axis corresponding to the left-most box side relative to the axis.
     * @param rMax the location along the first axis corresponding to the right-most box side relative to the axis.
     * @param sMin the location along the second axis corresponding to the left-most box side relative to the axis.
     * @param sMax the location along the second axis corresponding to the right-most box side relative to the axis.
     * @param tMin the location along the third axis corresponding to the left-most box side relative to the axis.
     * @param tMax the location along the third axis corresponding to the right-most box side relative to the axis.
     *
     * @throws IllegalArgumentException if the axes array or one of its entries is null.
     */
    public Box(Vec4 axes[], double rMin, double rMax, double sMin, double sMax, double tMin, double tMax)
    {
        if (axes == null || axes[0] == null || axes[1] == null || axes[2] == null)
        {
            String msg = Logging.getMessage("nullValue.AxesIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.ru = axes[0];
        this.su = axes[1];
        this.tu = axes[2];

        this.r = this.ru.multiply3(rMax - rMin);
        this.s = this.su.multiply3(sMax - sMin);
        this.t = this.tu.multiply3(tMax - tMin);

        this.rLength = this.r.getLength3();
        this.sLength = this.s.getLength3();
        this.tLength = this.t.getLength3();

        // Plane normals point outward from the box.
        this.planes = new Plane[6];
        this.planes[0] = new Plane(-this.ru.x, -this.ru.y, -this.ru.z, +rMin);
        this.planes[1] = new Plane(+this.ru.x, +this.ru.y, +this.ru.z, -rMax);
        this.planes[2] = new Plane(-this.su.x, -this.su.y, -this.su.z, +sMin);
        this.planes[3] = new Plane(+this.su.x, +this.su.y, +this.su.z, -sMax);
        this.planes[4] = new Plane(-this.tu.x, -this.tu.y, -this.tu.z, +tMin);
        this.planes[5] = new Plane(+this.tu.x, +this.tu.y, +this.tu.z, -tMax);

        double a = 0.5 * (rMin + rMax);
        double b = 0.5 * (sMin + sMax);
        double c = 0.5 * (tMin + tMax);
        this.center = ru.multiply3(a).add3(su.multiply3(b)).add3(tu.multiply3(c));

        Vec4 rHalf = r.multiply3(0.5);
        this.topCenter = this.center.add3(rHalf);
        this.bottomCenter = this.center.subtract3(rHalf);
    }

    /**
     * Construct a unit-length cube centered at a specified point.
     *
     * @param point the center of the cube.
     *
     * @throws IllegalArgumentException if the point is null.
     */
    public Box(Vec4 point)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.ru = new Vec4(1, 0, 0, 1);
        this.su = new Vec4(0, 1, 0, 1);
        this.tu = new Vec4(0, 0, 1, 1);

        this.r = this.ru;
        this.s = this.su;
        this.t = this.tu;

        this.rLength = 1;
        this.sLength = 1;
        this.tLength = 1;

        // Plane normals point outwards from the box.
        this.planes = new Plane[6];
        double d = 0.5 * point.getLength3();
        this.planes[0] = new Plane(-this.ru.x, -this.ru.y, -this.ru.z, -(d + 0.5));
        this.planes[1] = new Plane(+this.ru.x, +this.ru.y, +this.ru.z, -(d + 0.5));
        this.planes[2] = new Plane(-this.su.x, -this.su.y, -this.su.z, -(d + 0.5));
        this.planes[3] = new Plane(+this.su.x, +this.su.y, +this.su.z, -(d + 0.5));
        this.planes[4] = new Plane(-this.tu.x, -this.tu.y, -this.tu.z, -(d + 0.5));
        this.planes[5] = new Plane(+this.tu.x, +this.tu.y, +this.tu.z, -(d + 0.5));

        this.center = ru.add3(su).add3(tu).multiply3(0.5);

        Vec4 rHalf = r.multiply3(0.5);
        this.topCenter = this.center.add3(rHalf);
        this.bottomCenter = this.center.subtract3(rHalf);
    }

    /**
     * Compute a <code>Box</code> that bounds a specified list of points. Principal axes are computed for the points and
     * used to form a <code>Box</code>.
     *
     * @param points the points for which to compute a bounding volume.
     *
     * @return the bounding volume, with axes lengths consistent with the conventions described in the overview.
     *
     * @throws IllegalArgumentException if the point list is null or empty.
     */
    public static Box computeBoundingBox(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String msg = Logging.getMessage("nullValue.PointListIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4[] axes = WWMath.computePrincipalAxes(points);
        if (axes == null)
        {
            String msg = Logging.getMessage("generic.PointListIsEmpty");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 r = axes[0];
        Vec4 s = axes[1];
        Vec4 t = axes[2];

        // Find the extremes along each axis.
        double minDotR = Double.MAX_VALUE;
        double maxDotR = -minDotR;
        double minDotS = Double.MAX_VALUE;
        double maxDotS = -minDotS;
        double minDotT = Double.MAX_VALUE;
        double maxDotT = -minDotT;

        for (Vec4 p : points)
        {
            if (p == null)
                continue;

            double pdr = p.dot3(r);
            if (pdr < minDotR)
                minDotR = pdr;
            if (pdr > maxDotR)
                maxDotR = pdr;

            double pds = p.dot3(s);
            if (pds < minDotS)
                minDotS = pds;
            if (pds > maxDotS)
                maxDotS = pds;

            double pdt = p.dot3(t);
            if (pdt < minDotT)
                minDotT = pdt;
            if (pdt > maxDotT)
                maxDotT = pdt;
        }

        if (maxDotR == minDotR)
            maxDotR = minDotR + 1;
        if (maxDotS == minDotS)
            maxDotS = minDotS + 1;
        if (maxDotT == minDotT)
            maxDotT = minDotT + 1;

        return new Box(axes, minDotR, maxDotR, minDotS, maxDotS, minDotT, maxDotT);
    }

    /**
     * Returns the box's center point.
     *
     * @return the box's center point.
     */
    public Vec4 getCenter()
    {
        return this.center;
    }

    /**
     * Returns the point corresponding to the center of the box side left-most along the R (first) axis.
     *
     * @return the bottom-center point.
     */
    public Vec4 getBottomCenter()
    {
        return this.bottomCenter;
    }

    /**
     * Returns the point corresponding to the center of the box side right-most along the R (first) axis.
     *
     * @return the top-center point.
     */
    public Vec4 getTopCenter()
    {
        return this.topCenter;
    }

    /**
     * Returns the R (first) axis. The axis length is the distance between the box sides perpendicular to the axis.
     *
     * @return the R axis.
     */
    public Vec4 getRAxis()
    {
        return this.r;
    }

    /**
     * Returns the S (second) axis. The axis length is the distance between the box sides perpendicular to the axis.
     *
     * @return the S axis.
     */
    public Vec4 getSAxis()
    {
        return this.s;
    }

    /**
     * Returns the T (third) axis. The axis length is the distance between the box sides perpendicular to the axis.
     *
     * @return the T axis.
     */
    public Vec4 getTAxis()
    {
        return this.t;
    }

    /**
     * Returns the R (first) axis in unit length.
     *
     * @return the unit R axis.
     */
    public Vec4 getUnitRAxis()
    {
        return this.ru;
    }

    /**
     * Returns the S (second) axis in unit length.
     *
     * @return the unit S axis.
     */
    public Vec4 getUnitSAxis()
    {
        return this.su;
    }

    /**
     * Returns the T (third) axis in unit length.
     *
     * @return the unit T axis.
     */
    public Vec4 getUnitTAxis()
    {
        return this.tu;
    }

    /**
     * Returns the six planes of the box. The plane normals are directed outwards from the box.
     *
     * @return the six box planes in the order R-min, R-max, S-min, S-max, T-min, T-max.
     */
    public Plane[] getPlanes()
    {
        return this.planes;
    }

    /**
     * Returns the length of the R axis.
     *
     * @return the length of the R axis.
     */
    public double getRLength()
    {
        return rLength;
    }

    /**
     * Returns the length of the S axis.
     *
     * @return the length of the S axis.
     */
    public double getSLength()
    {
        return sLength;
    }

    /**
     * Returns the length of the T axis.
     *
     * @return the length of the T axis.
     */
    public double getTLength()
    {
        return tLength;
    }

    /**
     * Returns the effective radius of the box as if it were a sphere. The length returned is half the square root of
     * the sum of the squares of axis lengths.
     *
     * @return the effetive radius of the box.
     */
    public double getRadius()
    {
        return 0.5 * Math.sqrt(this.rLength * this.rLength + this.sLength * this.sLength + this.tLength * this.tLength);
    }

    /** {@inheritDoc} */
    public double getEffectiveRadius(Plane plane)
    {
        if (plane == null)
            return 0;

        // Determine the effective radius of the box axis relative to the plane.
        Vec4 n = plane.getNormal();
        return 0.5 * (Math.abs(this.s.dot3(n)) + Math.abs(this.t.dot3(n)));
    }

    /** {@inheritDoc} */
    public boolean intersects(Frustum frustum)
    {
        if (frustum == null)
        {
            String msg = Logging.getMessage("nullValue.FrustumIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // FYI: this code is identical to that in Cylinder.intersects.

        double intersectionPoint;

        this.tmp1.set(this.bottomCenter);
        this.tmp2.set(this.topCenter);

        double effectiveRadius = this.getEffectiveRadius(frustum.getNear());
        intersectionPoint = this.intersectsAt(frustum.getNear(), effectiveRadius, this.tmp1, this.tmp2);
        if (intersectionPoint < 0)
            return false;

        // Near and far have the same effective radius.
        effectiveRadius = this.getEffectiveRadius(frustum.getFar());
        intersectionPoint = this.intersectsAt(frustum.getFar(), effectiveRadius, this.tmp1, this.tmp2);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius(frustum.getLeft());
        intersectionPoint = this.intersectsAt(frustum.getLeft(), effectiveRadius, this.tmp1, this.tmp2);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius(frustum.getRight());
        intersectionPoint = this.intersectsAt(frustum.getRight(), effectiveRadius, this.tmp1, this.tmp2);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius(frustum.getTop());
        intersectionPoint = this.intersectsAt(frustum.getTop(), effectiveRadius, this.tmp1, this.tmp2);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius(frustum.getBottom());
        intersectionPoint = this.intersectsAt(frustum.getBottom(), effectiveRadius, this.tmp1, this.tmp2);
        return intersectionPoint >= 0;
    }

    protected double intersectsAt(Plane plane, double effectiveRadius, Vec4 endpoint1, Vec4 endpoint2)
    {
        // Test the distance from the first end-point.
        double dq1 = plane.dot(endpoint1);
        boolean bq1 = dq1 <= -effectiveRadius;

        // Test the distance from the possibly reduced second end-point.
        double dq2 = plane.dot(endpoint2);
        boolean bq2 = dq2 <= -effectiveRadius;

        if (bq1 && bq2) // endpoints more distant from plane than effective radius; box is on neg. side of plane
            return -1;

        if (bq1 == bq2) // endpoints less distant from plane than effective radius; can't draw any conclusions
            return 0;

        // Compute and return the endpoints of the cylinder on the positive side of the plane.
        this.tmp3.subtract3AndSet(endpoint1, endpoint2);
        double t = (effectiveRadius + dq1) / plane.getNormal().dot3(this.tmp3);

        this.tmp3.subtract3AndSet(endpoint2, endpoint1).multiply3AndSet(t).add3AndSet(endpoint1);
        // truncate the line to only that in the positive halfspace (e.g., inside the frustum)
        if (bq1)
            endpoint1.set(this.tmp3);
        else
            endpoint2.set(this.tmp3);

        return t;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof Box))
            return false;

        Box that = (Box) o;
        return this.center != null ? this.center.equals(that.center) : that.center == null
            && this.r != null ? this.r.equals(that.r) : that.r == null
            && this.s != null ? this.s.equals(that.s) : that.s == null
            && this.t != null ? this.t.equals(that.t) : that.t == null;
    }

    @Override
    public int hashCode()
    {
        int result = this.center != null ? this.center.hashCode() : 0;
        result = 31 * result + (this.r != null ? this.r.hashCode() : 0);
        result = 31 * result + (this.s != null ? this.s.hashCode() : 0);
        result = 31 * result + (this.t != null ? this.t.hashCode() : 0);
        return result;
    }
}
