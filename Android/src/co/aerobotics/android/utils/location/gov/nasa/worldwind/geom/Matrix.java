/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.geom;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

import java.util.Arrays;

/**
 * 4x4 matrix represented as a 16-value array in row-major order. The array indices correspond to matrix components as
 * follows:
 * <pre>
 *     m[0]  m[1]  m[2]  m[3]
 *     m[4]  m[5]  m[6]  m[7]
 *     m[8]  m[9]  m[10] m[11]
 *     m[12] m[13] m[14] m[15]
 * </pre>
 *
 * @author dcollins
 * @version $Id$
 */
public class Matrix
{
    public final double[] m = new double[16];

    public Matrix(double[] array, int offset)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (array.length < 16)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", array.length);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (offset < 0 || offset + 16 > array.length)
        {
            String msg = Logging.getMessage("generic.OffsetIsInvalid", offset);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        System.arraycopy(array, offset, m, 0, 16);
    }

    public Matrix(
        double m11, double m12, double m13, double m14,
        double m21, double m22, double m23, double m24,
        double m31, double m32, double m33, double m34,
        double m41, double m42, double m43, double m44)
    {
        // Row 1
        this.m[0] = m11;
        this.m[1] = m12;
        this.m[2] = m13;
        this.m[3] = m14;
        // Row 2
        this.m[4] = m21;
        this.m[5] = m22;
        this.m[6] = m23;
        this.m[7] = m24;
        // Row 3
        this.m[8] = m31;
        this.m[9] = m32;
        this.m[10] = m33;
        this.m[11] = m34;
        // Row 4
        this.m[12] = m41;
        this.m[13] = m42;
        this.m[14] = m43;
        this.m[15] = m44;
    }

    public static Matrix fromIdentity()
    {
        return new Matrix(
            1, 0, 0, 0, // Row 1
            0, 1, 0, 0, // Row 2
            0, 0, 1, 0, // Row 3
            0, 0, 0, 1); // Row 4
    }

    public static Matrix fromTranslation(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return Matrix.fromIdentity().setTranslation(vec);
    }

    public static Matrix fromTranslation(double x, double y, double z)
    {
        return Matrix.fromIdentity().setTranslation(x, y, z);
    }

    public static Matrix fromScale(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return Matrix.fromIdentity().setScale(vec);
    }

    public static Matrix fromScale(double x, double y, double z)
    {
        return Matrix.fromIdentity().setScale(x, y, z);
    }

    public static Matrix fromRotationX(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return Matrix.fromIdentity().setRotationX(angle);
    }

    public static Matrix fromRotationY(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return Matrix.fromIdentity().setRotationY(angle);
    }

    public static Matrix fromRotationZ(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return Matrix.fromIdentity().setRotationZ(angle);
    }

    public static Matrix fromAxisAngle(Angle angle, Vec4 axis)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (axis == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return Matrix.fromIdentity().setAxisAngle(angle, axis);
    }

    public static Matrix fromAxisAngle(Angle angle, double x, double y, double z)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return Matrix.fromIdentity().setAxisAngle(angle, x, y, z);
    }

    /**
     * Returns a viewing matrix in model coordinates defined by the specified View eye point, reference point indicating
     * the center of the scene, and up vector. The eye point, center point, and up vector are in model coordinates. The
     * returned viewing matrix maps the reference center point to the negative Z axis, and the eye point to the origin,
     * and the up vector to the positive Y axis. When this matrix is used to define an OGL viewing transform along with
     * a typical projection matrix such as {@link #setPerspective(Angle, double, double, double, double)} , this maps
     * the center of the scene to the center of the viewport, and maps the up vector to the viewoport's positive Y axis
     * (the up vector points up in the viewport). The eye point and reference center point must not be coincident, and
     * the up vector must not be parallel to the line of sight (the vector from the eye point to the reference center
     * point).
     *
     * @param eye    the eye point, in model coordinates.
     * @param center the scene's reference center point, in model coordinates.
     * @param up     the direction of the up vector, in model coordinates.
     *
     * @return a viewing matrix in model coordinates defined by the specified eye point, reference center point, and up
     *         vector.
     *
     * @throws IllegalArgumentException if any of the eye point, reference center point, or up vector are null, if the
     *                                  eye point and reference center point are coincident, or if the up vector and the
     *                                  line of sight are parallel.
     */
    public static Matrix fromLookAt(Vec4 eye, Vec4 center, Vec4 up)
    {
        if (eye == null)
        {
            String msg = Logging.getMessage("nullValue.EyeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (center == null)
        {
            String msg = Logging.getMessage("nullValue.CenterIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (up == null)
        {
            String msg = Logging.getMessage("nullValue.UpIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return Matrix.fromIdentity().setLookAt(eye, center, up);
    }

    public static Matrix fromPerspective(Angle horizontalFieldOfView, double viewportWidth, double viewportHeight,
        double near, double far)
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
            String msg = Logging.getMessage("generic.ViewportWidthIsInvalid", viewportWidth);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewportHeight < 0)
        {
            String msg = Logging.getMessage("generic.ViewportHeightIsInvalid", viewportHeight);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (near <= 0 || near > far)
        {
            String msg = Logging.getMessage("generic.ClipDistancesAreInvalid", near, far);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return Matrix.fromIdentity().setPerspective(horizontalFieldOfView, viewportWidth, viewportHeight, near, far);
    }

    public static Matrix fromPerspective(double left, double right, double bottom, double top, double near, double far)
    {
        if (left > right)
        {
            String msg = Logging.getMessage("generic.WidthIsInvalid", right - left);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (bottom > top)
        {
            String msg = Logging.getMessage("generic.HeightIsInvalid", top - bottom);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (near <= 0 || near > far)
        {
            String msg = Logging.getMessage("generic.ClipDistancesAreInvalid", near, far);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return Matrix.fromIdentity().setPerspective(left, right, bottom, top, near, far);
    }

    public static Matrix fromOrthographic(double left, double right, double bottom, double top, double near, double far)
    {
        if (left > right)
        {
            String msg = Logging.getMessage("generic.WidthIsInvalid", right - left);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (bottom > top)
        {
            String msg = Logging.getMessage("generic.HeightIsInvalid", top - bottom);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (near <= 0 || near > far)
        {
            String msg = Logging.getMessage("generic.ClipDistancesAreInvalid", near, far);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return Matrix.fromIdentity().setOrthographic(left, right, bottom, top, near, far);
    }

    /**
     * Computes a symmetric covariance Matrix from the x, y, z coordinates of the specified points Iterable. This
     * returns null if the points Iterable is empty, or if all of the points are null.
     * <p/>
     * The returned covariance matrix represents the correlation between each pair of x-, y-, and z-coordinates as
     * they're distributed about the point Iterable's arithmetic mean. Its layout is as follows:
     * <p/>
     * <code> C(x, x)  C(x, y)  C(x, z) <br/> C(x, y)  C(y, y)  C(y, z) <br/> C(x, z)  C(y, z)  C(z, z) </code>
     * <p/>
     * C(i, j) is the covariance of coordinates i and j, where i or j are a coordinate's dispersion about its mean
     * value. If any entry is zero, then there's no correlation between the two coordinates defining that entry. If the
     * returned matrix is diagonal, then all three coordinates are uncorrelated, and the specified point Iterable is
     * distributed evenly about its mean point.
     *
     * @param points the Iterable of points for which to compute a Covariance matrix.
     *
     * @return the covariance matrix for the iterable of 3D points.
     *
     * @throws IllegalArgumentException if the points Iterable is null.
     */
    public static Matrix fromCovarianceOfPoints(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String msg = Logging.getMessage("nullValue.PointListIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return Matrix.fromIdentity().setCovarianceOfPoints(points);
    }

    /**
     * Computes the eigensystem of the specified symmetric Matrix's upper 3x3 matrix. If the Matrix's upper 3x3 matrix
     * is not symmetric, this throws an IllegalArgumentException. This writes the eigensystem parameters to the
     * specified arrays <code>outEigenValues</code> and <code>outEigenVectors</code>, placing the eigenvalues in the
     * entries of array <code>outEigenValues</code>, and the corresponding eigenvectors in the entires of array
     * <code>outEigenVectors</code>. These arrays must be non-null, and have length three or greater.
     *
     * @param matrix             the symmetric Matrix for which to compute an eigensystem.
     * @param resultEigenvalues  the array which receives the three output eigenvalues.
     * @param resultEigenvectors the array which receives the three output eigenvectors.
     *
     * @throws IllegalArgumentException if the Matrix is null or is not symmetric, if the output eigenvalue array is
     *                                  null or has length less than 3, or if the output eigenvector is null or has
     *                                  length less than 3.
     */
    public static void computeEigensystemFromSymmetricMatrix3(Matrix matrix, double[] resultEigenvalues,
        Vec4[] resultEigenvectors)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (matrix.m[1] != matrix.m[4] || matrix.m[2] != matrix.m[8] || matrix.m[6] != matrix.m[9])
        {
            String msg = Logging.getMessage("Matrix.MatrixIsNotSymmetric", matrix);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (resultEigenvalues == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (resultEigenvalues.length < 3)
        {
            String msg = Logging.getMessage("generic.ResultArrayInvalidLength", resultEigenvalues.length);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (resultEigenvectors == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (resultEigenvectors.length < 3)
        {
            String msg = Logging.getMessage("generic.ResultArrayInvalidLength", resultEigenvectors.length);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Take from "Mathematics for 3D Game Programming and Computer Graphics, Second Edition" by Eric Lengyel,
        // Listing 14.6 (pages 441-444).

        final double EPSILON = 1.0e-10;
        final int MAX_SWEEPS = 32;

        // Since the Matrix is symmetric, m12=m21, m13=m31, and m23=m32. Therefore we can ignore the values m21, m31,
        // and m32.
        double m11 = matrix.m[0];
        double m12 = matrix.m[1];
        double m13 = matrix.m[2];
        double m22 = matrix.m[5];
        double m23 = matrix.m[6];
        double m33 = matrix.m[10];

        double[][] r = new double[3][3];
        r[0][0] = r[1][1] = r[2][2] = 1d;

        for (int a = 0; a < MAX_SWEEPS; a++)
        {
            // Exit if off-diagonal entries small enough
            if ((Math.abs(m12) < EPSILON) && (Math.abs(m13) < EPSILON) && (Math.abs(m23) < EPSILON))
                break;

            // Annihilate (1,2) entry
            if (m12 != 0d)
            {
                double u = (m22 - m11) * 0.5 / m12;
                double u2 = u * u;
                double u2p1 = u2 + 1d;
                double t = (u2p1 != u2) ?
                    ((u < 0d) ? -1d : 1d) * (Math.sqrt(u2p1) - Math.abs(u))
                    : 0.5 / u;
                double c = 1d / Math.sqrt(t * t + 1d);
                double s = c * t;

                m11 -= t * m12;
                m22 += t * m12;
                m12 = 0d;

                double temp = c * m13 - s * m23;
                m23 = s * m13 + c * m23;
                m13 = temp;

                for (int i = 0; i < 3; i++)
                {
                    temp = c * r[i][0] - s * r[i][1];
                    r[i][1] = s * r[i][0] + c * r[i][1];
                    r[i][0] = temp;
                }
            }

            // Annihilate (1,3) entry
            if (m13 != 0d)
            {
                double u = (m33 - m11) * 0.5 / m13;
                double u2 = u * u;
                double u2p1 = u2 + 1d;
                double t = (u2p1 != u2) ?
                    ((u < 0d) ? -1d : 1d) * (Math.sqrt(u2p1) - Math.abs(u))
                    : 0.5 / u;
                double c = 1d / Math.sqrt(t * t + 1d);
                double s = c * t;

                m11 -= t * m13;
                m33 += t * m13;
                m13 = 0d;

                double temp = c * m12 - s * m23;
                m23 = s * m12 + c * m23;
                m12 = temp;

                for (int i = 0; i < 3; i++)
                {
                    temp = c * r[i][0] - s * r[i][2];
                    r[i][2] = s * r[i][0] + c * r[i][2];
                    r[i][0] = temp;
                }
            }

            // Annihilate (2,3) entry
            if (m23 != 0d)
            {
                double u = (m33 - m22) * 0.5 / m23;
                double u2 = u * u;
                double u2p1 = u2 + 1d;
                double t = (u2p1 != u2) ?
                    ((u < 0d) ? -1d : 1d) * (Math.sqrt(u2p1) - Math.abs(u))
                    : 0.5 / u;
                double c = 1d / Math.sqrt(t * t + 1d);
                double s = c * t;

                m22 -= t * m23;
                m33 += t * m23;
                m23 = 0d;

                double temp = c * m12 - s * m13;
                m13 = s * m12 + c * m13;
                m12 = temp;

                for (int i = 0; i < 3; i++)
                {
                    temp = c * r[i][1] - s * r[i][2];
                    r[i][2] = s * r[i][1] + c * r[i][2];
                    r[i][1] = temp;
                }
            }
        }

        resultEigenvalues[0] = m11;
        resultEigenvalues[1] = m22;
        resultEigenvalues[2] = m33;

        resultEigenvectors[0] = new Vec4(r[0][0], r[1][0], r[2][0]);
        resultEigenvectors[1] = new Vec4(r[0][1], r[1][1], r[2][1]);
        resultEigenvectors[2] = new Vec4(r[0][2], r[1][2], r[2][2]);
    }

    public Matrix copy()
    {
        return new Matrix(this.m, 0);
    }

    public Matrix set(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        System.arraycopy(matrix.m, 0, this.m, 0, 16);

        return this;
    }

    public Matrix set(double[] array, int offset)
    {
        if (array == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (array.length < 16)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", array.length);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (offset < 0 || offset + 16 > array.length)
        {
            String msg = Logging.getMessage("generic.OffsetIsInvalid", offset);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        System.arraycopy(array, offset, m, 0, 16);

        return this;
    }

    public Matrix set(
        double m11, double m12, double m13, double m14,
        double m21, double m22, double m23, double m24,
        double m31, double m32, double m33, double m34,
        double m41, double m42, double m43, double m44)
    {
        // Row 1
        this.m[0] = m11;
        this.m[1] = m12;
        this.m[2] = m13;
        this.m[3] = m14;
        // Row 2
        this.m[4] = m21;
        this.m[5] = m22;
        this.m[6] = m23;
        this.m[7] = m24;
        // Row 3
        this.m[8] = m31;
        this.m[9] = m32;
        this.m[10] = m33;
        this.m[11] = m34;
        // Row 4
        this.m[12] = m41;
        this.m[13] = m42;
        this.m[14] = m43;
        this.m[15] = m44;

        return this;
    }

    public Matrix setIdentity()
    {
        // Row 1
        this.m[0] = 1;
        this.m[1] = 0;
        this.m[2] = 0;
        this.m[3] = 0;
        // Row 2
        this.m[4] = 0;
        this.m[5] = 1;
        this.m[6] = 0;
        this.m[7] = 0;
        // Row 3
        this.m[8] = 0;
        this.m[9] = 0;
        this.m[10] = 1;
        this.m[11] = 0;
        // Row 4
        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    public Matrix setTranslation(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.setTranslation(vec.x, vec.y, vec.z);
    }

    public Matrix setTranslation(double x, double y, double z)
    {
        // Row 1
        this.m[0] = 1;
        this.m[1] = 0;
        this.m[2] = 0;
        this.m[3] = x;
        // Row 2
        this.m[4] = 0;
        this.m[5] = 1;
        this.m[6] = 0;
        this.m[7] = y;
        // Row 3
        this.m[8] = 0;
        this.m[9] = 0;
        this.m[10] = 1;
        this.m[11] = z;
        // Row 4
        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    public Matrix setScale(Vec4 vec)
    {
        if (vec == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.setScale(vec.x, vec.y, vec.z);
    }

    public Matrix setScale(double x, double y, double z)
    {
        // Row 1
        this.m[0] = x;
        this.m[1] = 0;
        this.m[2] = 0;
        this.m[3] = 0;
        // Row 2
        this.m[4] = 0;
        this.m[5] = y;
        this.m[6] = 0;
        this.m[7] = 0;
        // Row 3
        this.m[8] = 0;
        this.m[9] = 0;
        this.m[10] = z;
        this.m[11] = 0;
        // Row 4
        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    public Matrix setRotationX(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        double c = angle.cos();
        double s = angle.sin();

        // Row 1
        this.m[0] = 1;
        this.m[1] = 0;
        this.m[2] = 0;
        this.m[3] = 0;
        // Row 2
        this.m[4] = 0;
        this.m[5] = c;
        this.m[6] = -s;
        this.m[7] = 0;
        // Row 3
        this.m[8] = 0;
        this.m[9] = s;
        this.m[10] = c;
        this.m[11] = 0;
        // Row 4
        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    public Matrix setRotationY(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        double c = angle.cos();
        double s = angle.sin();

        // Row 1
        this.m[0] = c;
        this.m[1] = 0;
        this.m[2] = s;
        this.m[3] = 0;
        // Row 2
        this.m[4] = 0;
        this.m[5] = 1;
        this.m[6] = 0;
        this.m[7] = 0;
        // Row 3
        this.m[8] = -s;
        this.m[9] = 0;
        this.m[10] = c;
        this.m[11] = 0;
        // Row 4
        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    public Matrix setRotationZ(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        double c = angle.cos();
        double s = angle.sin();

        // Row 1
        this.m[0] = c;
        this.m[1] = -s;
        this.m[2] = 0;
        this.m[3] = 0;
        // Row 2
        this.m[4] = s;
        this.m[5] = c;
        this.m[6] = 0;
        this.m[7] = 0;
        // Row 3
        this.m[8] = 0;
        this.m[9] = 0;
        this.m[10] = 1;
        this.m[11] = 0;
        // Row 4
        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    public Matrix setAxisAngle(Angle angle, Vec4 axis)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (axis == null)
        {
            String msg = Logging.getMessage("nullValue.VectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.setAxisAngle(angle, axis.x, axis.y, axis.z);
    }

    public Matrix setAxisAngle(Angle angle, double x, double y, double z)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        double c = angle.cos();
        double s = angle.sin();
        double one_minus_c = 1.0 - c;

        // Row 1
        this.m[0] = c + (one_minus_c * x * x);
        this.m[1] = (one_minus_c * x * y) - (s * z);
        this.m[2] = (one_minus_c * x * z) + (s * y);
        this.m[3] = 0;
        // Row 2
        this.m[4] = (one_minus_c * x * y) + (s * z);
        this.m[5] = c + (one_minus_c * y * y);
        this.m[6] = (one_minus_c * y * z) - (s * x);
        this.m[7] = 0;
        // Row 3
        this.m[8] = (one_minus_c * x * z) - (s * y);
        this.m[9] = (one_minus_c * y * z) + (s * x);
        this.m[10] = c + (one_minus_c * z * z);
        this.m[11] = 0;
        // Row 4
        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    /**
     * Returns a viewing matrix in model coordinates defined by the specified View eye point, reference point indicating
     * the center of the scene, and up vector. The eye point, center point, and up vector are in model coordinates. The
     * returned viewing matrix maps the reference center point to the negative Z axis, and the eye point to the origin,
     * and the up vector to the positive Y axis. When this matrix is used to define an OGL viewing transform along with
     * a typical projection matrix such as {@link #setPerspective(Angle, double, double, double, double)} , this maps
     * the center of the scene to the center of the viewport, and maps the up vector to the viewoport's positive Y axis
     * (the up vector points up in the viewport). The eye point and reference center point must not be coincident, and
     * the up vector must not be parallel to the line of sight (the vector from the eye point to the reference center
     * point).
     *
     * @param eye    the eye point, in model coordinates.
     * @param center the scene's reference center point, in model coordinates.
     * @param up     the direction of the up vector, in model coordinates.
     *
     * @return a viewing matrix in model coordinates defined by the specified eye point, reference center point, and up
     *         vector.
     *
     * @throws IllegalArgumentException if any of the eye point, reference center point, or up vector are null, if the
     *                                  eye point and reference center point are coincident, or if the up vector and the
     *                                  line of sight are parallel.
     */
    public Matrix setLookAt(Vec4 eye, Vec4 center, Vec4 up)
    {
        if (eye == null)
        {
            String msg = Logging.getMessage("nullValue.EyeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (center == null)
        {
            String msg = Logging.getMessage("nullValue.CenterIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (up == null)
        {
            String msg = Logging.getMessage("nullValue.UpIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute orthogonal forward, side, and up vectors from the specified eye, center, and up points. The computed
        // forward vector always points from the eye to the center, the side is the always orthogonal to the computed
        // forward and specified up vector, and the computed up vector is always orthogonal to the computed side and the
        // computed up. The computed up vector is not equivalent to the specified up vector if the specified up vector
        // is not orthogonal to the computed forward vector.

        Vec4 f = center.subtract3(eye);
        f.normalize3AndSet();

        Vec4 s = f.subtract3(up);
        s.normalize3AndSet();

        Vec4 u = s.cross3(f);
        u.normalize3AndSet();

        // Set this matrix to translate model coordinates into the eye's coordinate space. The eye's coordinate space
        // places the eye point at the origin, looking down the negative Z axis with the Y axis pointing up. This is
        // equivalent to:
        //
        // Matrix m = new Matrix(s.x, s.y, s.z, 0, u.x, u.y, u.z, 0, -f.x, -f.y, -f.z, 0, 0, 0, 0, 1);
        // Matrix eye = Matrix.fromIdentity().setTranslation(-eye.x, -eye.y, -eye.z);
        // Matrix lookAt = Matrix.fromIdentity();
        // this.multiplyAndSet(m, eye);
        //
        // We use the shorthand version below to avoid two matrix allocations and one matrix multiplication.

        // Row 1
        this.m[0] = s.x;
        this.m[1] = s.y;
        this.m[2] = s.z;
        this.m[3] = -s.x * eye.x - s.y * eye.y - s.z * eye.z;
        // Row 2
        this.m[4] = u.x;
        this.m[5] = u.y;
        this.m[6] = u.z;
        this.m[7] = -u.x * eye.x - u.y * eye.y - u.z * eye.z;
        // Row 3
        this.m[8] = -f.x;
        this.m[9] = -f.y;
        this.m[10] = -f.z;
        this.m[11] = f.x * eye.x + f.y * eye.y + f.z * eye.z;
        // Row 4
        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    public Matrix setPerspective(Angle horizontalFieldOfView, double viewportWidth, double viewportHeight, double near,
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
            String msg = Logging.getMessage("generic.ViewportWidthIsInvalid", viewportWidth);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (viewportHeight < 0)
        {
            String msg = Logging.getMessage("generic.ViewportHeightIsInvalid", viewportHeight);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (near <= 0 || near > far)
        {
            String msg = Logging.getMessage("generic.ClipDistancesAreInvalid", near, far);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Based on http://www.opengl.org/resources/faq/technical/transformations.htm#tran0085.
        // This method uses horizontal field-of-view here to describe the perspective viewing angle. This results in a
        // different set of clip plane distances than documented in sources using vertical field-of-view.

        if (viewportWidth == 0)
            viewportWidth = 1;

        if (viewportHeight == 0)
            viewportHeight = 1;

        if (near == far)
            far = near + 1;

        double right = near * horizontalFieldOfView.tanHalfAngle();
        double left = -right;
        double top = right * viewportHeight / viewportWidth;
        double bottom = -top;

        this.setPerspective(left, right, bottom, top, near, far);

        return this;
    }

    public Matrix setPerspective(double left, double right, double bottom, double top, double near, double far)
    {
        if (left > right)
        {
            String msg = Logging.getMessage("generic.WidthIsInvalid", right - left);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (bottom > top)
        {
            String msg = Logging.getMessage("generic.HeightIsInvalid", top - bottom);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (near <= 0 || near > far)
        {
            String msg = Logging.getMessage("generic.ClipDistancesAreInvalid", near, far);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Taken from "Mathematics for 3D Game Programming and Computer Graphics, Second Edition", chapter 4, page 130.

        if (left == right)
            right = left + 1;

        if (bottom == top)
            top = bottom + 1;

        if (near == far)
            far = near + 1;

        // Row 1
        this.m[0] = 2 * near / (right - left);
        this.m[1] = 0;
        this.m[2] = (right + left) / (right - left);
        this.m[3] = 0;
        // Row 2
        this.m[4] = 0;
        this.m[5] = 2 * near / (top - bottom);
        this.m[6] = (top + bottom) / (top - bottom);
        this.m[7] = 0;
        // Row 3
        this.m[8] = 0;
        this.m[9] = 0;
        this.m[10] = -(far + near) / (far - near);
        this.m[11] = -2 * near * far / (far - near);
        // Row 4
        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = -1;
        this.m[15] = 0;

        return this;
    }

    public Matrix setOrthographic(double left, double right, double bottom, double top, double near, double far)
    {
        if (left > right)
        {
            String msg = Logging.getMessage("generic.WidthIsInvalid", right - left);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (bottom > top)
        {
            String msg = Logging.getMessage("generic.HeightIsInvalid", top - bottom);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (near > far)
        {
            String msg = Logging.getMessage("generic.ClipDistancesAreInvalid", near, far);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Taken from "Mathematics for 3D Game Programming and Computer Graphics, Second Edition", chapter 4, page 126.

        if (left == right)
            right = left + 1;

        if (bottom == top)
            top = bottom + 1;

        if (near == far)
            far = near + 1;

        // Row 1
        this.m[0] = 2 / (right - left);
        this.m[1] = 0;
        this.m[2] = 0;
        this.m[3] = -(right + left) / (right - left);
        // Row 2
        this.m[4] = 0;
        this.m[5] = 2 / (top - bottom);
        this.m[6] = 0;
        this.m[7] = -(top + bottom) / (top - bottom);
        // Row 3
        this.m[8] = 0;
        this.m[9] = 0;
        this.m[10] = -2 / (far - near);
        this.m[11] = -(far + near) / (far - near);
        // Row 4
        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 1;

        return this;
    }

    /**
     * Computes a symmetric covariance Matrix from the x, y, z coordinates of the specified points Iterable. This
     * returns null if the points Iterable is empty, or if all of the points are null.
     * <p/>
     * The returned covariance matrix represents the correlation between each pair of x-, y-, and z-coordinates as
     * they're distributed about the point Iterable's arithmetic mean. Its layout is as follows:
     * <p/>
     * <code> C(x, x)  C(x, y)  C(x, z) <br/> C(x, y)  C(y, y)  C(y, z) <br/> C(x, z)  C(y, z)  C(z, z) </code>
     * <p/>
     * C(i, j) is the covariance of coordinates i and j, where i or j are a coordinate's dispersion about its mean
     * value. If any entry is zero, then there's no correlation between the two coordinates defining that entry. If the
     * returned matrix is diagonal, then all three coordinates are uncorrelated, and the specified point Iterable is
     * distributed evenly about its mean point.
     *
     * @param points the Iterable of points for which to compute a Covariance matrix.
     *
     * @return the covariance matrix for the iterable of 3D points.
     *
     * @throws IllegalArgumentException if the points Iterable is null.
     */
    public Matrix setCovarianceOfPoints(Iterable<? extends Vec4> points)
    {
        if (points == null)
        {
            String msg = Logging.getMessage("nullValue.PointListIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 mean = Vec4.computeAverage3(points);
        if (mean == null)
            return null;

        int count = 0;
        double c11 = 0d;
        double c22 = 0d;
        double c33 = 0d;
        double c12 = 0d;
        double c13 = 0d;
        double c23 = 0d;

        for (Vec4 vec : points)
        {
            if (vec == null)
                continue;

            count++;
            c11 += (vec.x - mean.x) * (vec.x - mean.x);
            c22 += (vec.y - mean.y) * (vec.y - mean.y);
            c33 += (vec.z - mean.z) * (vec.z - mean.z);
            c12 += (vec.x - mean.x) * (vec.y - mean.y); // c12 = c21
            c13 += (vec.x - mean.x) * (vec.z - mean.z); // c13 = c31
            c23 += (vec.y - mean.y) * (vec.z - mean.z); // c23 = c32
        }

        if (count == 0)
            return null;

        // Row 1
        this.m[0] = c11 / (double) count;
        this.m[1] = c12 / (double) count;
        this.m[2] = c13 / (double) count;
        this.m[3] = 0;
        // Row 2
        this.m[4] = c12 / (double) count;
        this.m[5] = c22 / (double) count;
        this.m[6] = c23 / (double) count;
        this.m[7] = 0;
        // Row 3
        this.m[8] = c13 / (double) count;
        this.m[9] = c23 / (double) count;
        this.m[10] = c33 / (double) count;
        this.m[11] = 0;
        // Row 4
        this.m[12] = 0;
        this.m[13] = 0;
        this.m[14] = 0;
        this.m[15] = 0;

        return this;
    }

    // Cartesian Android Model

    public final Angle getCAMRotationX()    // assumes the order of rotations is YXZ, positive CW
    {
        double xRadians = Math.asin(-this.m[6]);
        if (Double.isNaN(xRadians))
            return null;

        return Angle.fromRadians(-xRadians);    // negate to make angle CW
    }

    public final Angle getCAMRotationY()    // assumes the order of rotations is YXZ, positive CW
    {
        double xRadians = Math.asin(-this.m[6]);
        if (Double.isNaN(xRadians))
            return null;

        double yRadians;
        if (xRadians < Math.PI / 2)
        {
            if (xRadians > -Math.PI / 2)
            {
                yRadians = Math.atan2(this.m[2], this.m[10]);
            }
            else
            {
                yRadians = -Math.atan2(-this.m[1], this.m[0]);
            }
        }
        else
        {
            yRadians = Math.atan2(-this.m[1], this.m[0]);
        }

        if (Double.isNaN(yRadians))
            return null;

        return Angle.fromRadians(-yRadians);    // negate angle to make it CW
    }

    public final Angle getCAMRotationZ()    //  assumes the order of rotations is YXZ, positive CW
    {
        double xRadians = Math.asin(-this.m[6]);
        if (Double.isNaN(xRadians))
            return null;

        double zRadians;
        if (xRadians < Math.PI / 2 && xRadians > -Math.PI / 2)
        {
            zRadians = Math.atan2(this.m[4], this.m[5]);
        }
        else
        {
            zRadians = 0;
        }

        if (Double.isNaN(zRadians))
            return null;

        return Angle.fromRadians(-zRadians);    // negate angle to make it CW
    }

    /**
     * Inverts a generic 4x4 matrix in place. This returns <code>null</code> if this matrix is not invertible.
     *
     * @return a reference to this Matrix, or <code>null</code> if this matrix is not invertible.
     */
    public Matrix invert()
    {
        return this.invert(this);
    }

    /**
     * Inverts a generic 4x4 matrix and stores the result in this matrix. This returns <code>null</code> if the
     * specified matrix is not invertible.
     *
     * @param matrix the matrix who's inverse is computed.
     *
     * @return a reference to this Matrix, or <code>null</code> if the matrix is not invertible.
     *
     * @throws IllegalArgumentException if the matrix is <code>null</code>.
     */
    public Matrix invert(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Taken from "Mathematics for 3D Game Programming and Computer Graphics, Second Edition", chapter 2,
        // algorithm 2.12, page 45.

        // Step A. Construct an augmented matrix M composed of a the specified matrix and the identity matrix placed
        // side-by-side to form a single 4x8 matrix. The final inverse is stored in the rightmost 4x4 portion of this
        // matrix. Rather than creating a single data structure, we store each 4x4 portion in a separate 16-value array
        // in row-major order. The first matrix is a local copy, and the second matrix is this matrix.
        double[] copy = new double[16];
        System.arraycopy(matrix.m, 0, copy, 0, 16);
        this.setIdentity();

        // Temporary 4-component buffer used to swap rows.
        double[] tmpRow = new double[4];

        // Step B and G. Perform steps C-F columns 0 to 4, where j indicates the current column. Note that we use a
        // zero-based index instead of the 1-based count used in the original text. Additionally, note that value at
        // row i column j Mij = copy[4 * i + j].
        for (int j = 0; j < 4; j++)
        {
            // Step C. Find the row i such that Mij has the largest absolute value, where i >= j. We skip row j because
            // there's no need to test the first value against itself.
            int i = j;
            double max = Math.abs(copy[4 * j + j]);
            for (int k = j + 1; k < 4; k++)
            {
                double d = Math.abs(copy[4 * k + j]);
                if (d > max)
                {
                    i = k;
                    max = d;
                }
            }

            // (Step C, continued). If the row i with the largest absolute value Mij is 0, then the matrix is not
            // invertible. We return null to indicate that the matrix is not invertible.
            if (max == 0)
                return null;

            // Step D. Swap rows i and j if they are not the same row. Since we store each part of the augmented
            // matrix separately, we must swap rows in both copy and this matrix. Note that the values of row i start
            // at index 4 * i.
            if (i != j)
            {
                System.arraycopy(copy, 4 * i, tmpRow, 0, 4);
                System.arraycopy(copy, 4 * j, copy, 4 * i, 4);
                System.arraycopy(tmpRow, 0, copy, 4 * j, 4);

                System.arraycopy(this.m, 4 * i, tmpRow, 0, 4);
                System.arraycopy(this.m, 4 * j, this.m, 4 * i, 4);
                System.arraycopy(tmpRow, 0, this.m, 4 * j, 4);
            }

            // Step E. Multiply row j by 1/Mjj. Since we store each part of the augmented matrix separately, we must
            // multiply the row in both copy and this matrix. Note that the values of row j start at index 4 * j.
            double mjj = 1.0 / copy[4 * j + j];
            for (int k = 0; k < 4; k++)
            {
                copy[4 * j + k] *= mjj;
                this.m[4 * j + k] *= mjj;
            }

            // Step F. Add -Mrj * Mjk to each entry Mrk in row r (except row j). Since we store each part of the
            // augmented matrix separately, we must multiply the row in both copy and this matrix. Note that values of
            // row r start at index 4 * r.
            for (int r = 0; r < 4; r++)
            {
                if (r == j)
                    continue;

                double mrj = copy[4 * r + j];
                for (int k = 0; k < 4; k++)
                {
                    copy[4 * r + k] -= mrj * copy[4 * j + k];
                    this.m[4 * r + k] -= mrj * this.m[4 * j + k];
                }
            }
        }

        return this;
    }

    /**
     * Inverts this matrix and stores the result in this matrix. This is assumed to represent an orthonormal transform
     * matrix. This matrix's upper-3x3 is transposed, then its fourth column is transformed by the transposed upper-3x3
     * and negated.
     *
     * @return a reference to this Matrix.
     */
    public Matrix invertTransformMatrix()
    {
        // Transform the translation vector of this matrix by the transpose of its upper 3x3 portion, and store the
        // negative of this vector in this matrix's translation component. We must perform this step first, as it
        // assumes that this matrix's upper 3x3 portion is in its original state.
        double tx = -(this.m[0] * this.m[3]) - (this.m[4] * this.m[7]) - (this.m[8] * this.m[11]);
        double ty = -(this.m[1] * this.m[3]) - (this.m[5] * this.m[7]) - (this.m[9] * this.m[11]);
        double tz = -(this.m[2] * this.m[3]) - (this.m[6] * this.m[7]) - (this.m[10] * this.m[11]);
        this.m[3] = tx;
        this.m[7] = ty;
        this.m[11] = tz;

        // Compute the transpose of this matrix's upper 3x3 portion, and store the result in this matrix's upper 3x3
        // portion.
        // Swap m[1] and m[4].
        double tmp = this.m[1];
        this.m[1] = this.m[4];
        this.m[4] = tmp;
        // Swap m[2] and m[8].
        tmp = this.m[2];
        this.m[2] = this.m[8];
        this.m[8] = tmp;
        // Swap m[6] and m[9].
        tmp = this.m[6];
        this.m[6] = this.m[9];
        this.m[9] = tmp;

        return this;
    }

    /**
     * Inverts the specified matrix and stores the result in this matrix. The specified matrix is assumed to represent
     * an orthonormal transform matrix. This matrix's upper 3x3 is transposed, then its fourth column is transformed by
     * the transposed upper 3x3 and negated.
     * <p/>
     * The result of this method is undefined if this matrix is passed in as the matrix to invert. In order to invert a
     * transform matrix in place, call {@link #invertTransformMatrix()}.
     *
     * @param matrix the matrix who's inverse is computed. This matrix is assumed to represent an orthonormal transform
     *               matrix.
     *
     * @return a reference to this Matrix.
     *
     * @throws IllegalArgumentException if the matrix is <code>null</code>.
     */
    public Matrix invertTransformMatrix(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute the transpose of the specified matrix's upper 3x3 portion, and store the result in this matrix's
        // upper 3x3 portion.
        this.m[0] = matrix.m[0];
        this.m[1] = matrix.m[4];
        this.m[2] = matrix.m[8];
        this.m[4] = matrix.m[1];
        this.m[5] = matrix.m[5];
        this.m[6] = matrix.m[9];
        this.m[8] = matrix.m[2];
        this.m[9] = matrix.m[6];
        this.m[10] = matrix.m[10];

        // Transform the translation vector of the specified matrix by the transpose of its upper 3x3 portion, and
        // store the negative of this vector in this matrix's translation component.
        this.m[3] = -(matrix.m[0] * matrix.m[3]) - (matrix.m[4] * matrix.m[7]) - (matrix.m[8] * matrix.m[11]);
        this.m[7] = -(matrix.m[1] * matrix.m[3]) - (matrix.m[5] * matrix.m[7]) - (matrix.m[9] * matrix.m[11]);
        this.m[11] = -(matrix.m[2] * matrix.m[3]) - (matrix.m[6] * matrix.m[7]) - (matrix.m[10] * matrix.m[11]);

        // Copy the specified matrix's bottom row into this matrix's bottom row. Since we're assuming the matrix
        // represents an orthonormal transform matrix, the bottom row should always be (0, 0, 0, 1).
        this.m[12] = matrix.m[12];
        this.m[13] = matrix.m[13];
        this.m[14] = matrix.m[14];
        this.m[15] = matrix.m[15];

        return this;
    }

    public Matrix transpose()
    {
        // Swap m12 and m21.
        double tmp = this.m[1];
        this.m[1] = this.m[4];
        this.m[4] = tmp;

        // Swap m13 and m31.
        tmp = this.m[2];
        this.m[2] = this.m[8];
        this.m[8] = tmp;

        // Swap m14 and m41.
        tmp = this.m[3];
        this.m[3] = this.m[12];
        this.m[12] = tmp;

        // Swap m23 and m32.
        tmp = this.m[6];
        this.m[6] = this.m[9];
        this.m[9] = tmp;

        // Swap m24 and m42.
        tmp = this.m[7];
        this.m[7] = this.m[13];
        this.m[13] = tmp;

        // Swap m34 and m43.
        tmp = this.m[11];
        this.m[11] = this.m[14];
        this.m[14] = tmp;

        return this;
    }

    public Matrix transpose(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Row 1
        this.m[0] = matrix.m[0];
        this.m[1] = matrix.m[4];
        this.m[2] = matrix.m[8];
        this.m[3] = matrix.m[12];
        // Row 1
        this.m[4] = matrix.m[1];
        this.m[5] = matrix.m[5];
        this.m[6] = matrix.m[9];
        this.m[7] = matrix.m[13];
        // Row 1
        this.m[8] = matrix.m[2];
        this.m[9] = matrix.m[6];
        this.m[10] = matrix.m[10];
        this.m[11] = matrix.m[14];
        // Row 1
        this.m[12] = matrix.m[3];
        this.m[13] = matrix.m[7];
        this.m[14] = matrix.m[11];
        this.m[15] = matrix.m[15];

        return this;
    }

    public Matrix multiply(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.copy().multiplyAndSet(matrix);
    }

    public Matrix multiply(
        double m11, double m12, double m13, double m14,
        double m21, double m22, double m23, double m24,
        double m31, double m32, double m33, double m34,
        double m41, double m42, double m43, double m44)
    {
        return this.copy().multiplyAndSet(
            m11, m12, m13, m14, // Row 1
            m21, m22, m23, m24, // Row 2
            m31, m32, m33, m34, // Row 3
            m41, m42, m43, m44); // Row 4
    }

    public Matrix multiplyAndSet(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Row 1
        double a = this.m[0];
        double b = this.m[1];
        double c = this.m[2];
        double d = this.m[3];
        this.m[0] = (a * matrix.m[0]) + (b * matrix.m[4]) + (c * matrix.m[8]) + (d * matrix.m[12]);
        this.m[1] = (a * matrix.m[1]) + (b * matrix.m[5]) + (c * matrix.m[9]) + (d * matrix.m[13]);
        this.m[2] = (a * matrix.m[2]) + (b * matrix.m[6]) + (c * matrix.m[10]) + (d * matrix.m[14]);
        this.m[3] = (a * matrix.m[3]) + (b * matrix.m[7]) + (c * matrix.m[11]) + (d * matrix.m[15]);

        // Row 2
        a = this.m[4];
        b = this.m[5];
        c = this.m[6];
        d = this.m[7];
        this.m[4] = (a * matrix.m[0]) + (b * matrix.m[4]) + (c * matrix.m[8]) + (d * matrix.m[12]);
        this.m[5] = (a * matrix.m[1]) + (b * matrix.m[5]) + (c * matrix.m[9]) + (d * matrix.m[13]);
        this.m[6] = (a * matrix.m[2]) + (b * matrix.m[6]) + (c * matrix.m[10]) + (d * matrix.m[14]);
        this.m[7] = (a * matrix.m[3]) + (b * matrix.m[7]) + (c * matrix.m[11]) + (d * matrix.m[15]);

        // Row 3
        a = this.m[8];
        b = this.m[9];
        c = this.m[10];
        d = this.m[11];
        this.m[8] = (a * matrix.m[0]) + (b * matrix.m[4]) + (c * matrix.m[8]) + (d * matrix.m[12]);
        this.m[9] = (a * matrix.m[1]) + (b * matrix.m[5]) + (c * matrix.m[9]) + (d * matrix.m[13]);
        this.m[10] = (a * matrix.m[2]) + (b * matrix.m[6]) + (c * matrix.m[10]) + (d * matrix.m[14]);
        this.m[11] = (a * matrix.m[3]) + (b * matrix.m[7]) + (c * matrix.m[11]) + (d * matrix.m[15]);

        // Row 4
        a = this.m[12];
        b = this.m[13];
        c = this.m[14];
        d = this.m[15];
        this.m[12] = (a * matrix.m[0]) + (b * matrix.m[4]) + (c * matrix.m[8]) + (d * matrix.m[12]);
        this.m[13] = (a * matrix.m[1]) + (b * matrix.m[5]) + (c * matrix.m[9]) + (d * matrix.m[13]);
        this.m[14] = (a * matrix.m[2]) + (b * matrix.m[6]) + (c * matrix.m[10]) + (d * matrix.m[14]);
        this.m[15] = (a * matrix.m[3]) + (b * matrix.m[7]) + (c * matrix.m[11]) + (d * matrix.m[15]);

        return this;
    }

    public Matrix multiplyAndSet(
        double m11, double m12, double m13, double m14,
        double m21, double m22, double m23, double m24,
        double m31, double m32, double m33, double m34,
        double m41, double m42, double m43, double m44)
    {
        // Row 1
        double a = this.m[0];
        double b = this.m[1];
        double c = this.m[2];
        double d = this.m[3];
        this.m[0] = (a * m11) + (b * m21) + (c * m31) + (d * m41);
        this.m[1] = (a * m12) + (b * m22) + (c * m32) + (d * m42);
        this.m[2] = (a * m13) + (b * m23) + (c * m33) + (d * m43);
        this.m[3] = (a * m14) + (b * m24) + (c * m34) + (d * m44);

        // Row 2
        a = this.m[4];
        b = this.m[5];
        c = this.m[6];
        d = this.m[7];
        this.m[4] = (a * m11) + (b * m21) + (c * m31) + (d * m41);
        this.m[5] = (a * m12) + (b * m22) + (c * m32) + (d * m42);
        this.m[6] = (a * m13) + (b * m23) + (c * m33) + (d * m43);
        this.m[7] = (a * m14) + (b * m24) + (c * m34) + (d * m44);

        // Row 3
        a = this.m[8];
        b = this.m[9];
        c = this.m[10];
        d = this.m[11];
        this.m[8] = (a * m11) + (b * m21) + (c * m31) + (d * m41);
        this.m[9] = (a * m12) + (b * m22) + (c * m32) + (d * m42);
        this.m[10] = (a * m13) + (b * m23) + (c * m33) + (d * m43);
        this.m[11] = (a * m14) + (b * m24) + (c * m34) + (d * m44);

        // Row 4
        a = this.m[12];
        b = this.m[13];
        c = this.m[14];
        d = this.m[15];
        this.m[12] = (a * m11) + (b * m21) + (c * m31) + (d * m41);
        this.m[13] = (a * m12) + (b * m22) + (c * m32) + (d * m42);
        this.m[14] = (a * m13) + (b * m23) + (c * m33) + (d * m43);
        this.m[15] = (a * m14) + (b * m24) + (c * m34) + (d * m44);

        return this;
    }

    public Matrix multiplyAndSet(Matrix lhs, Matrix rhs)
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

        // Row 1
        this.m[0] = (lhs.m[0] * rhs.m[0]) + (lhs.m[1] * rhs.m[4]) + (lhs.m[2] * rhs.m[8]) + (lhs.m[3] * rhs.m[12]);
        this.m[1] = (lhs.m[0] * rhs.m[1]) + (lhs.m[1] * rhs.m[5]) + (lhs.m[2] * rhs.m[9]) + (lhs.m[3] * rhs.m[13]);
        this.m[2] = (lhs.m[0] * rhs.m[2]) + (lhs.m[1] * rhs.m[6]) + (lhs.m[2] * rhs.m[10]) + (lhs.m[3] * rhs.m[14]);
        this.m[3] = (lhs.m[0] * rhs.m[3]) + (lhs.m[1] * rhs.m[7]) + (lhs.m[2] * rhs.m[11]) + (lhs.m[3] * rhs.m[15]);
        // Row 2
        this.m[4] = (lhs.m[4] * rhs.m[0]) + (lhs.m[5] * rhs.m[4]) + (lhs.m[6] * rhs.m[8]) + (lhs.m[7] * rhs.m[12]);
        this.m[5] = (lhs.m[4] * rhs.m[1]) + (lhs.m[5] * rhs.m[5]) + (lhs.m[6] * rhs.m[9]) + (lhs.m[7] * rhs.m[13]);
        this.m[6] = (lhs.m[4] * rhs.m[2]) + (lhs.m[5] * rhs.m[6]) + (lhs.m[6] * rhs.m[10]) + (lhs.m[7] * rhs.m[14]);
        this.m[7] = (lhs.m[4] * rhs.m[3]) + (lhs.m[5] * rhs.m[7]) + (lhs.m[6] * rhs.m[11]) + (lhs.m[7] * rhs.m[15]);
        // Row 3
        this.m[8] = (lhs.m[8] * rhs.m[0]) + (lhs.m[9] * rhs.m[4]) + (lhs.m[10] * rhs.m[8]) + (lhs.m[11] * rhs.m[12]);
        this.m[9] = (lhs.m[8] * rhs.m[1]) + (lhs.m[9] * rhs.m[5]) + (lhs.m[10] * rhs.m[9]) + (lhs.m[11] * rhs.m[13]);
        this.m[10] = (lhs.m[8] * rhs.m[2]) + (lhs.m[9] * rhs.m[6]) + (lhs.m[10] * rhs.m[10]) + (lhs.m[11] * rhs.m[14]);
        this.m[11] = (lhs.m[8] * rhs.m[3]) + (lhs.m[9] * rhs.m[7]) + (lhs.m[10] * rhs.m[11]) + (lhs.m[11] * rhs.m[15]);
        // Row 4
        this.m[12] = (lhs.m[12] * rhs.m[0]) + (lhs.m[13] * rhs.m[4]) + (lhs.m[14] * rhs.m[8]) + (lhs.m[15] * rhs.m[12]);
        this.m[13] = (lhs.m[12] * rhs.m[1]) + (lhs.m[13] * rhs.m[5]) + (lhs.m[14] * rhs.m[9]) + (lhs.m[15] * rhs.m[13]);
        this.m[14] = (lhs.m[12] * rhs.m[2]) + (lhs.m[13] * rhs.m[6]) + (lhs.m[14] * rhs.m[10]) + (lhs.m[15]
            * rhs.m[14]);
        this.m[15] = (lhs.m[12] * rhs.m[3]) + (lhs.m[13] * rhs.m[7]) + (lhs.m[14] * rhs.m[11]) + (lhs.m[15]
            * rhs.m[15]);

        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        Matrix that = (Matrix) o;
        return Arrays.equals(this.m, that.m);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(this.m);
    }

    @Override
    public String toString()
    {
        return Arrays.toString(this.m);
    }
}
