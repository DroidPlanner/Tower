/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.geom;

/**
 * Represents a volume enclosing one or more objects or collections of points. Primarily used to test intersections with
 * other objects.
 *
 * @author Tom Gaskins
 * @version $Id$
 */
public interface Extent
{
    /**
     * Returns the extent's center point.
     *
     * @return the extent's center point.
     */
    Vec4 getCenter();

    /**
     * Returns the extent's radius. The computation of the radius depends on the implementing class. See the
     * documentation for the individual classes to determine how they compute a radius.
     *
     * @return the extent's radius.
     */
    double getRadius();

    /**
     * Computes the effective radius of the extent relative to a specified plane.
     *
     * @param plane the plane.
     *
     * @return the effective radius, or 0 if the plane is null.
     */
    double getEffectiveRadius(Plane plane);

    /**
     * Determines whether or not this <code>Extent</code> intersects <code>frustum</code>. Returns true if any part of
     * these two objects intersect, including the case where either object wholly contains the other, false otherwise.
     *
     * @param frustum the <code>Frustum</code> with which to test for intersection.
     *
     * @return true if there is an intersection, false otherwise.
     */
    boolean intersects(Frustum frustum);
}
