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
public class Frustum
{
    protected final Plane left;
    protected final Plane right;
    protected final Plane bottom;
    protected final Plane top;
    protected final Plane near;
    protected final Plane far;
    /** Holds all six frustum planes in an array in the order left, right, bottom, top, near, far. */
    protected final Plane[] allPlanes;

    /** Constructs a frustum two meters wide centered at the origin. Primarily used for testing. */
    public Frustum()
    {
        this(
            new Plane(1, 0, 0, 1), // Left
            new Plane(-1, 0, 0, 1), // Right
            new Plane(0, 1, 0, 1), // Bottom
            new Plane(0, -1, 0, 1), // Top
            new Plane(0, 0, -1, 1), // Near
            new Plane(0, 0, 1, 1)); // Far
    }

    /**
     * Create a frustum from six {@link gov.nasa.worldwind.geom.Plane}s defining the frustum boundaries.
     * <p/>
     * None of the arguments may be null.
     *
     * @param near   the near plane
     * @param far    the far plane
     * @param left   the left plane
     * @param right  the right plane
     * @param top    the top plane
     * @param bottom the bottom plane
     *
     * @throws IllegalArgumentException if any argument is null.
     */
    public Frustum(Plane left, Plane right, Plane bottom, Plane top, Plane near, Plane far)
    {
        if (left == null)
        {
            String msg = Logging.getMessage("nullValue.LeftIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (right == null)
        {
            String msg = Logging.getMessage("nullValue.RightIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (bottom == null)
        {
            String msg = Logging.getMessage("nullValue.BottomIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (top == null)
        {
            String msg = Logging.getMessage("nullValue.TopIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (near == null)
        {
            String msg = Logging.getMessage("nullValue.NearIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (far == null)
        {
            String msg = Logging.getMessage("nullValue.FarIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.near = near;
        this.far = far;

        this.allPlanes = new Plane[] {this.left, this.right, this.bottom, this.top, this.near, this.far};
    }

    public Frustum copy()
    {
        return new Frustum(
            this.left.copy(),
            this.right.copy(),
            this.bottom.copy(),
            this.top.copy(),
            this.near.copy(),
            this.far.copy());
    }

    public Frustum set(Frustum frustum)
    {
        if (frustum == null)
        {
            String msg = Logging.getMessage("nullValue.FrustumIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.left.set(frustum.left);
        this.right.set(frustum.right);
        this.bottom.set(frustum.bottom);
        this.top.set(frustum.top);
        this.near.set(frustum.near);
        this.far.set(frustum.far);

        return this;
    }

    public Frustum set(Plane left, Plane right, Plane bottom, Plane top, Plane near, Plane far)
    {
        if (left == null)
        {
            String msg = Logging.getMessage("nullValue.LeftIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (right == null)
        {
            String msg = Logging.getMessage("nullValue.RightIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (bottom == null)
        {
            String msg = Logging.getMessage("nullValue.BottomIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (top == null)
        {
            String msg = Logging.getMessage("nullValue.TopIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (near == null)
        {
            String msg = Logging.getMessage("nullValue.NearIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (far == null)
        {
            String msg = Logging.getMessage("nullValue.FarIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.left.set(left);
        this.right.set(right);
        this.bottom.set(bottom);
        this.top.set(top);
        this.near.set(near);
        this.far.set(far);

        return this;
    }

    /**
     * Creates a frustum by extracting the six frustum planes from a projection matrix.
     *
     * @param matrix the projection matrix to extract the frustum planes from.
     *
     * @return a frustum defined by the extracted planes.
     *
     * @throws IllegalArgumentException if the projection matrix is null.
     */
    public Frustum setProjection(Matrix matrix)
    {
        //noinspection UnnecessaryLocalVariable
        Matrix m = matrix;
        if (m == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Extract the six clipping planes from the projection-matrix.
        this.left.set(m.m[12] + m.m[0], m.m[13] + m.m[1], m.m[14] + m.m[2], m.m[15] + m.m[3]).normalizeAndSet();
        this.right.set(m.m[12] - m.m[0], m.m[13] - m.m[1], m.m[14] - m.m[2], m.m[15] - m.m[3]).normalizeAndSet();
        this.bottom.set(m.m[12] + m.m[4], m.m[13] + m.m[5], m.m[14] + m.m[6], m.m[15] + m.m[7]).normalizeAndSet();
        this.top.set(m.m[12] - m.m[4], m.m[13] - m.m[5], m.m[14] - m.m[6], m.m[15] - m.m[7]).normalizeAndSet();
        this.near.set(m.m[12] + m.m[8], m.m[13] + m.m[9], m.m[14] + m.m[10], m.m[15] + m.m[11]).normalizeAndSet();
        this.far.set(m.m[12] - m.m[8], m.m[13] - m.m[9], m.m[14] - m.m[10], m.m[15] - m.m[11]).normalizeAndSet();

        return this;
    }

    /**
     * Creates a <code>Frustum</code> from a horizontal field-of-view, viewport aspect ratio and distance to near and
     * far depth clipping planes. The near plane must be closer than the far plane, and both near and far values must be
     * positive.
     *
     * @param horizontalFieldOfView horizontal field-of-view angle in the range (0, 180)
     * @param viewportWidth         the width of the viewport in screen pixels
     * @param viewportHeight        the height of the viewport in screen pixels
     * @param near                  distance to the near depth clipping plane
     * @param far                   distance to far depth clipping plane
     *
     * @return Frustum configured from the specified perspective parameters.
     *
     * @throws IllegalArgumentException if fov is not in the range (0, 180), if either near or far are negative, or near
     *                                  is greater than or equal to far
     */
    public Frustum setPerspective(Angle horizontalFieldOfView, double viewportWidth, double viewportHeight, double near,
        double far)
    {
        if (horizontalFieldOfView == null)
        {
            String msg = Logging.getMessage("nullValue.FieldOfViewIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (horizontalFieldOfView.degrees <= 0 || horizontalFieldOfView.degrees > 180)
        {
            String msg = Logging.getMessage("generic.FieldOfViewIsInvalid", horizontalFieldOfView);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewportWidth < 0)
        {
            String msg = Logging.getMessage("generic.WidthIsInvalid", viewportWidth);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewportHeight < 0)
        {
            String msg = Logging.getMessage("generic.HeightIsInvalid", viewportHeight);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (near <= 0 || near > far)
        {
            String msg = Logging.getMessage("generic.ClipDistancesAreInvalid", near, far);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewportWidth == 0)
            viewportWidth = 1;

        if (viewportHeight == 0)
            viewportHeight = 1;

        if (near == far)
            far = near + 1;

        double focalLength = 1d / horizontalFieldOfView.tanHalfAngle();
        double aspect = viewportHeight / viewportWidth;
        double lrLen = Math.sqrt(focalLength * focalLength + 1);
        double btLen = Math.sqrt(focalLength * focalLength + aspect * aspect);

        this.left.set(focalLength / lrLen, 0, -1d / lrLen, 0);
        this.right.set(-focalLength / lrLen, 0, -1d / lrLen, 0);
        this.bottom.set(0, focalLength / btLen, -aspect / btLen, 0);
        this.top.set(0, -focalLength / btLen, -aspect / btLen, 0);
        this.near.set(0, 0, -1, -near);
        this.far.set(0, 0, 1, far);

        return this;
    }

    /**
     * Returns the left plane.
     *
     * @return the left plane.
     */
    public Plane getLeft()
    {
        return this.left;
    }

    /**
     * Returns the right plane.
     *
     * @return the right plane.
     */
    public Plane getRight()
    {
        return this.right;
    }

    /**
     * Returns the bottom plane.
     *
     * @return the bottom plane.
     */
    public Plane getBottom()
    {
        return this.bottom;
    }

    /**
     * Returns the top plane.
     *
     * @return the top plane.
     */
    public Plane getTop()
    {
        return this.top;
    }

    /**
     * Returns the near plane.
     *
     * @return the left plane.
     */
    public Plane getNear()
    {
        return this.near;
    }

    /**
     * Returns the far plane.
     *
     * @return the left plane.
     */
    public Plane getFar()
    {
        return this.far;
    }

    /**
     * Returns all the planes.
     *
     * @return an array of the frustum planes, in the order left, right, bottom, top, near, far.
     */
    public Plane[] getAllPlanes()
    {
        return this.allPlanes;
    }

    /**
     * Indicates whether a specified {@link Extent} intersects this frustum.
     *
     * @param extent the Extent to test.
     *
     * @return true if the extent intersects this frustum, otherwise false.
     *
     * @throws IllegalArgumentException if the extent is null.
     */
    public boolean intersects(Extent extent)
    {
        if (extent == null)
        {
            String msg = Logging.getMessage("nullValue.ExtentIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return extent.intersects(this);
    }

    public Frustum transformBy(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.left.getVector().transformBy4AndSet(matrix);
        this.right.getVector().transformBy4AndSet(matrix);
        this.bottom.getVector().transformBy4AndSet(matrix);
        this.top.getVector().transformBy4AndSet(matrix);
        this.near.getVector().transformBy4AndSet(matrix);
        this.far.getVector().transformBy4AndSet(matrix);

        return this;
    }

    public Frustum transformBy(Frustum frustum, Matrix matrix)
    {
        if (frustum == null)
        {
            String msg = Logging.getMessage("nullValue.FrustumIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.left.getVector().transformBy4AndSet(frustum.left.getVector(), matrix);
        this.right.getVector().transformBy4AndSet(frustum.right.getVector(), matrix);
        this.bottom.getVector().transformBy4AndSet(frustum.bottom.getVector(), matrix);
        this.top.getVector().transformBy4AndSet(frustum.top.getVector(), matrix);
        this.near.getVector().transformBy4AndSet(frustum.near.getVector(), matrix);
        this.far.getVector().transformBy4AndSet(frustum.far.getVector(), matrix);

        return this;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || this.getClass() != obj.getClass())
            return false;

        Frustum that = (Frustum) obj;
        return this.left.equals(that.left)
            && this.right.equals(that.right)
            && this.bottom.equals(that.bottom)
            && this.top.equals(that.top)
            && this.near.equals(that.near)
            && this.far.equals(that.far);
    }

    @Override
    public int hashCode()
    {
        int result;
        result = this.left.hashCode();
        result = 31 * result + this.right.hashCode();
        result = 19 * result + this.bottom.hashCode();
        result = 23 * result + this.top.hashCode();
        result = 17 * result + this.near.hashCode();
        result = 19 * result + this.far.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("left=").append(this.left);
        sb.append(", right=").append(this.right);
        sb.append(", bottom=").append(this.bottom);
        sb.append(", top=").append(this.top);
        sb.append(", near=").append(this.near);
        sb.append(", far=").append(this.far);
        sb.append(")");
        return sb.toString();
    }
}
