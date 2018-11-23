/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.util;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.globes.Globe;

import java.util.*;

/**
 * A collection of useful math methods, all static.
 *
 * @author dcollins
 * @version $Id$
 */
public class WWMath
{
    public static final double SECOND_TO_MILLIS = 1000.0;
    public static final double MINUTE_TO_MILLIS = 60.0 * SECOND_TO_MILLIS;
    public static final double HOUR_TO_MILLIS = 60.0 * MINUTE_TO_MILLIS;
    public static final double DAY_TO_MILLIS = 24.0 * HOUR_TO_MILLIS;

    // Temporary properties used to avoid constant reallocation of primitive types.
    protected static Vec4 origin = new Vec4();
    protected static Vec4 direction = new Vec4();
    protected static Vec4 nearPoint = new Vec4();
    protected static Vec4 farPoint = new Vec4();
    protected static Matrix invMatrix = Matrix.fromIdentity();
    protected static Matrix mvpMatrix = Matrix.fromIdentity();

    /**
     * Converts time in seconds to time in milliseconds.
     *
     * @param seconds time in seconds.
     *
     * @return time in milliseconds.
     */
    public static double convertSecondsToMillis(double seconds)
    {
        return (seconds * SECOND_TO_MILLIS);
    }

    /**
     * Converts time in minutes to time in milliseconds.
     *
     * @param minutes time in minutes.
     *
     * @return time in milliseconds.
     */
    public static double convertMinutesToMillis(double minutes)
    {
        return (minutes * MINUTE_TO_MILLIS);
    }

    /**
     * Converts time in hours to time in milliseconds.
     *
     * @param hours time in hours.
     *
     * @return time in milliseconds.
     */
    public static double convertHoursToMillis(double hours)
    {
        return (hours * HOUR_TO_MILLIS);
    }

    /**
     * Computes the current distance to the horizon from a viewer at the specified elevation. Only the globe's ellipsoid
     * is considered; terrain elevations are not incorporated.
     *
     * @param globe     the globe to compute a horizon distance for.
     * @param elevation the viewer's elevation, in meters relative to mean sea level.
     *
     * @return the distance to the horizon, in meters.
     *
     * @throws IllegalArgumentException if the globe is <code>null</code>.
     */
    public static double computeHorizonDistance(Globe globe, double elevation)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (elevation <= 0)
            return 0;

        return Math.sqrt(elevation * (2 * globe.getRadius() + elevation));
    }

    /**
     * Returns an array of normalized vectors defining the three principal axes of the x-, y-, and z-coordinates from
     * the specified points Iterable, sorted from the most prominent axis to the least prominent. This returns null if
     * the points Iterable is empty, or if all of the points are null. The returned array contains three normalized
     * orthogonal vectors defining a coordinate system which best fits the distribution of the points Iterable about its
     * arithmetic mean.
     *
     * @param points the Iterable of points for which to compute the principal axes.
     *
     * @return the normalized principal axes of the points Iterable, sorted from the most prominent axis to the least
     *         prominent.
     *
     * @throws IllegalArgumentException if the points Iterable is null.
     */
    public static Vec4[] computePrincipalAxes(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String msg = Logging.getMessage("nullValue.PointListIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute the covariance matrix of the specified points Iterable. Note that Matrix.fromCovarianceOfVertices
        // returns null if the points Iterable is empty, or if all of the points are null.
        Matrix covariance = Matrix.fromCovarianceOfPoints(points);
        if (covariance == null)
            return null;

        // Compute the eigenvalues and eigenvectors of the covariance matrix. Since the covariance matrix is symmetric
        // by definition, we can safely use the method Matrix.computeEigensystemFromSymmetricMatrix3().
        final double[] eigenValues = new double[3];
        final Vec4[] eigenVectors = new Vec4[3];
        Matrix.computeEigensystemFromSymmetricMatrix3(covariance, eigenValues, eigenVectors);

        // Compute an index array who's entries define the order in which the eigenValues array can be sorted in
        // ascending order.
        Integer[] indexArray = {0, 1, 2};
        Arrays.sort(indexArray, new Comparator<Integer>()
        {
            public int compare(Integer a, Integer b)
            {
                return Double.compare(eigenValues[a], eigenValues[b]);
            }
        });

        // Return the normalized eigenvectors in order of decreasing eigenvalue. This has the effect of returning three
        // normalized orthognal vectors defining a coordinate system, which are sorted from the most prominent axis to
        // the least prominent.
        return new Vec4[]
            {
                eigenVectors[indexArray[2]].normalize3(),
                eigenVectors[indexArray[1]].normalize3(),
                eigenVectors[indexArray[0]].normalize3()
            };
    }

    /**
     * Computes a line in model coordinates that originates from the eye point and passes through the screen point (x,
     * y). The specified modelview, projection, and viewport define the properties used to transform from screen
     * coordinates to model coordinates. The screen point is relative to the lower left corner.
     *
     * @param x          the screen point's x-coordinate, relative to the lower left corner.
     * @param y          the screen point's y-coordinate, relative to the lower left corner.
     * @param modelview  the modelview matrix, transforms model coordinates to eye coordinates.
     * @param projection the projection matrix, transforms eye coordinates to clip coordinates.
     * @param viewport   the viewport rectangle, transforms clip coordinates to screen coordinates.
     * @param result     contains the line in model coordinates after this method returns. This value is not modified if
     *                   this returns <code>false</code>.
     *
     * @return <code>true</code> if a ray is successfully computed, and <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException if any of the modelview, projection, viewport, or result are <code>null</code>,
     *                                  or if either of the viewport width or height are less than or equal to zero.
     */
    public static boolean computeRayFromScreenPoint(double x, double y, Matrix modelview, Matrix projection,
        Rect viewport, Line result)
    {
        if (modelview == null)
        {
            String msg = Logging.getMessage("nullValue.ModelviewMatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (projection == null)
        {
            String msg = Logging.getMessage("nullValue.ProjectionMatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewport == null)
        {
            String msg = Logging.getMessage("nullValue.ViewportIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewport.width <= 0)
        {
            String msg = Logging.getMessage("generic.ViewportWidthIsInvalid", viewport.width);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewport.height <= 0)
        {
            String msg = Logging.getMessage("generic.ViewportHeightIsInvalid", viewport.height);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Taken from the "OpenGL Technical FAQ & Troubleshooting Guide", section 20.010.
        // "How can I know which primitive a user has selected with the mouse?"
        // http://www.opengl.org/resources/faq/technical/selection.htm#sele0010

        // Compute the combined modelview-projection matrix.
        mvpMatrix.multiplyAndSet(projection, modelview);

        // Compute the model coordinate point on the near clip plane that corresponds to the specified screen point.
        // Return false if this point cannot be computed for any reason. This method uses the point and matrix temporary
        // properties to compute the result. We must make this computation before doing anything that depends on the
        // values of the point and matrix properties.
        if (!unProject(x, y, 0, mvpMatrix, viewport, nearPoint))
            return false;

        // Compute the model coordinate point on the far clip plane that corresponds to the specified screen point.
        // Return false if this point cannot be computed for any reason. This method uses the point and matrix temporary
        // properties to compute the result. We must make this computation before doing anything that depends on the
        // values of the point and matrix properties.
        if (!unProject(x, y, 1, mvpMatrix, viewport, farPoint))
            return false;

        // Compute the ray origin as the eye point in model coordinates. We compute the eye point by transforming the
        // originby the inverse of the modelview matrix.
        invMatrix.invertTransformMatrix(modelview);
        origin.set(0, 0, 0).transformBy4AndSet(invMatrix);

        // Compute the ray direction as the vector pointing from the near clip plane to the far clip plane, and passing
        // through the specified screen point. We compute this vector buy subtracting the near point from the far point,
        // resulting in a vector that points from near to far.
        direction.subtract3AndSet(farPoint, nearPoint);
        direction.normalize3AndSet();

        // Set the line's origin and direction. Calling Line.set causes Line to copy the origin and direction into its
        // local properties.
        result.set(origin, direction);

        return true;
    }

    /**
     * Transforms the model coordinate point (x, y, z) to screen coordinates using the specified transform parameters.
     * The specified mvpMatrix and viewport define transformation from model coordinates to screen coordinates.
     * <p/>
     * After this method returns, the result's x and y values represent the point's screen coordinates relative to the
     * lower left corner. The result's z value represents the point's depth as a value in the range [0, 1], where 0
     * corresponds to the near clip plane and 1 corresponds to the far clip plane.
     *
     * @param x         the model point's x-coordinate.
     * @param y         the model point's y-coordinate.
     * @param z         the model point's z-coordinate.
     * @param mvpMatrix the modelview-projection matrix, transforms model coordinates to clip coordinates.
     * @param viewport  the viewport rectangle, transforms clip coordinates to screen coordinates.
     * @param result    contains the point in screen coordinates after this method returns. This value is not modified
     *                  if this returns <code>false</code>.
     *
     * @return <code>true</code> if the point is successfully transformed, and <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException if any of the mvpMatrix, viewport, or result are <code>null</code>, or if either
     *                                  of the viewport width or height are less than or equal to zero.
     */
    public static boolean project(double x, double y, double z, Matrix mvpMatrix, Rect viewport, Vec4 result)
    {
        if (mvpMatrix == null)
        {
            String msg = Logging.getMessage("nullValue.MVPMatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewport == null)
        {
            String msg = Logging.getMessage("nullValue.ViewportIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewport.width <= 0)
        {
            String msg = Logging.getMessage("generic.ViewportWidthIsInvalid", viewport.width);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewport.height <= 0)
        {
            String msg = Logging.getMessage("generic.ViewportHeightIsInvalid", viewport.height);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Set the point to the specified model coordinates, and set the w-coordinate to 1. The computations below
        // depend on a w-coordinate of 1.
        result.x = x;
        result.y = y;
        result.z = z;
        result.w = 1;

        // Transform the model coordinate point by the modelview matrix, then transform it by the projection matrix.
        // This transforms the point from model coordinates to clip coordinates, and is equivalent to transforming the
        // point by the concatenated modelview-projection matrix. This assumes that mvpMatrix has been computed by
        // multiplying the modelview and projection matrices together in the following order:
        // mvpMatrix = projection * modelview
        result.transformBy4AndSet(mvpMatrix);

        if (result.w == 0.0)
            return false;

        // Transform the point from clip coordinates in the range [-1, 1] to coordinates in the range [0, 1]. This
        // intermediate step makes the final step of transforming to screen coordinates easier.
        result.w = (1.0 / result.w) * 0.5;
        result.x = result.x * result.w + 0.5;
        result.y = result.y * result.w + 0.5;
        result.z = result.z * result.w + 0.5;

        // Transform the point to screen coordinates, and assign it to the caller's result parameter.
        result.x = (result.x * viewport.width) + viewport.x;
        result.y = (result.y * viewport.height) + viewport.y;

        return true;
    }

    /**
     * Transforms the screen coordinate point (x, y, z) to model coordinates, using the specified transform parameters.
     * The specified mvpMatrix and viewport define transformation from screen coordinates to model coordinates. The
     * screen point's x and y values represent its coordinates relative to the lower left corner. The screen point's z
     * value represents its depth as a value in the range [0, 1].
     * <p/>
     * After this method returns, the results x, y, and z coordinates represent the point's model coordinates.
     *
     * @param x         the screen point's x-coordinate, relative to the lower left corner.
     * @param y         the screen point's y-coordinate, relative to the lower left corner.
     * @param z         the screen point's z-coordinate, in the range [0, 1].
     * @param mvpMatrix the modelview-projection matrix, transforms model coordinates to clip coordinates.
     * @param viewport  the viewport rectangle, transforms clip coordinates to screen coordinates.
     * @param result    contains the point in model coordinates after this method returns. This value is not modified if
     *                  this returns <code>false</code>.
     *
     * @return <code>true</code> if the point is successfully transformed, and <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException if any of the mvpMatrix, viewport, or result are <code>null</code>, or if either
     *                                  of the viewport width or height are less than or equal to zero.
     */
    public static boolean unProject(double x, double y, double z, Matrix mvpMatrix, Rect viewport, Vec4 result)
    {
        if (mvpMatrix == null)
        {
            String msg = Logging.getMessage("nullValue.MVPMatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewport == null)
        {
            String msg = Logging.getMessage("nullValue.ViewportIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewport.width <= 0)
        {
            String msg = Logging.getMessage("generic.ViewportWidthIsInvalid", viewport.width);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewport.height <= 0)
        {
            String msg = Logging.getMessage("generic.ViewportHeightIsInvalid", viewport.height);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute a matrix that transforms a screen coordinate point by the inverse of the projection matrix then by
        // the inverse of the modelview matrix. This transforms a point from clip coordinates to model coordinates, and
        // is equivalent to transforming the point by the inverse of the concatenated projection-modelview matrix:
        // pmvInv = inverse(modelview * projection).
        if (invMatrix.invert(mvpMatrix) == null)
            return false;

        // Set the point to the specified screen coordinates, and set the w-coordinate to 1. The computations below
        // depend on a w-coordinate of 1.
        result.x = x;
        result.y = y;
        result.z = z;
        result.w = 1;

        // Transform the point from screen coordinates to coordinates in the range [0, 1].
        result.x = (result.x - viewport.x) / viewport.width;
        result.y = (result.y - viewport.y) / viewport.height;

        // Transform the point to clip coordinates in the range [-1, 1].
        result.x = (result.x * 2 - 1);
        result.y = (result.y * 2 - 1);
        result.z = result.z * 2 - 1;

        // Transform the point from clip coordinates to model coordinates.
        result.transformBy4AndSet(invMatrix);

        if (result.w == 0.0)
            return false;

        result.w = 1.0 / result.w;
        result.x *= result.w;
        result.y *= result.w;
        result.z *= result.w;

        return true;
    }

    /**
     * Convenience method to compute the log base 2 of a value.
     *
     * @param value the value to take the log of.
     *
     * @return the log base 2 of the specified value.
     */
    public static double logBase2(double value)
    {
        return Math.log(value) / Math.log(2d);
    }

    /**
     * Convenience method for testing whether a value is a power of two.
     *
     * @param value the value to test for power of 2
     *
     * @return true if power of 2, else false
     */
    public static boolean isPowerOfTwo(int value)
    {
        return (value == powerOfTwoCeiling(value));
    }

    /**
     * Returns the value that is the nearest power of 2 greater than or equal to the given value.
     *
     * @param reference the reference value. The power of 2 returned is greater than or equal to this value.
     *
     * @return the value that is the nearest power of 2 greater than or equal to the reference value
     */
    public static int powerOfTwoCeiling(int reference)
    {
        int power = (int) Math.ceil(Math.log(reference) / Math.log(2d));
        return (int) Math.pow(2d, power);
    }
}
