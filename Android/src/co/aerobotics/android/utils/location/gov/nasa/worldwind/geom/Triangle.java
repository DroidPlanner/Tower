/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.geom;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

/**
 * Provides operations on triangles.
 *
 * @author Eric Dalgliesh 30/11/2006
 * @version $Id$
 */
public class Triangle
{
    protected static final double EPSILON = 0.0000001; // used in intersects method

    protected final Vec4 a;
    protected final Vec4 b;
    protected final Vec4 c;

    /** Constructs an empty triangle who's three vertices are set to (0, 0, 0). */
    public Triangle()
    {
        this.a = new Vec4();
        this.b = new Vec4();
        this.c = new Vec4();
    }

    /**
     * Constructs a triangle from three counter-clockwise ordered vertices. The front face of the triangle is determined
     * by the right-hand rule.
     *
     * @param a the first vertex.
     * @param b the second vertex.
     * @param c the third vertex.
     *
     * @throws IllegalArgumentException if any vertex is <code>null</code>.
     */
    public Triangle(Vec4 a, Vec4 b, Vec4 c)
    {
        if (a == null)
        {
            String msg = Logging.getMessage("nullValue.FirstVertexIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (b == null)
        {
            String msg = Logging.getMessage("nullValue.SecondVertexIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (c == null)
        {
            String msg = Logging.getMessage("nullValue.ThirdVertexIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.a = a;
        this.b = b;
        this.c = c;
    }

    public Triangle copy()
    {
        return new Triangle(this.a.copy(), this.b.copy(), this.c.copy());
    }

    public Triangle set(Triangle triangle)
    {
        if (triangle == null)
        {
            String msg = Logging.getMessage("nullValue.TriangleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.a.set(triangle.a);
        this.b.set(triangle.b);
        this.c.set(triangle.c);

        return this;
    }

    public Triangle set(Vec4 a, Vec4 b, Vec4 c)
    {
        if (a == null)
        {
            String msg = Logging.getMessage("nullValue.FirstVertexIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (b == null)
        {
            String msg = Logging.getMessage("nullValue.SecondVertexIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (c == null)
        {
            String msg = Logging.getMessage("nullValue.ThirdVertexIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.a.set(a);
        this.b.set(b);
        this.c.set(c);

        return this;
    }

    public Triangle set(double vax, double vay, double vaz, double vbx, double vby, double vbz, double vcx, double vcy,
        double vcz)
    {
        this.a.set(vax, vay, vaz);
        this.b.set(vbx, vby, vbz);
        this.c.set(vcx, vcy, vcz);

        return this;
    }

    /**
     * Returns the first vertex.
     *
     * @return the first vertex.
     */
    public Vec4 getA()
    {
        return this.a;
    }

    /**
     * Returns the second vertex.
     *
     * @return the second vertex.
     */
    public Vec4 getB()
    {
        return this.b;
    }

    /**
     * Returns the third vertex.
     *
     * @return the third vertex.
     */
    public Vec4 getC()
    {
        return this.c;
    }

    /**
     * Indicates whether a specified point is on the triangle.
     *
     * @param p the point to test. If null, the method returns false.
     *
     * @return true if the point is on the triangle, otherwise false.
     */
    public boolean contains(Vec4 p)
    {
        if (p == null)
            return false;

        // Compute vectors
        Vec4 v0 = this.c.subtract3(this.a);
        Vec4 v1 = this.b.subtract3(this.a);
        Vec4 v2 = p.subtract3(this.a);

        // Compute dot products
        double dot00 = v0.dotSelf3();
        double dot01 = v0.dot3(v1);
        double dot02 = v0.dot3(v2);
        double dot11 = v1.dotSelf3();
        double dot12 = v1.dot3(v2);

        // Compute barycentric coordinates
        double det = (dot00 * dot11 - dot01 * dot01);

        double detInv = 1 / det;
        double u = (dot11 * dot02 - dot01 * dot12) * detInv;
        double v = (dot00 * dot12 - dot01 * dot02) * detInv;

        // Check if point is contained in triangle (including edges and vertices)
        return (u >= 0d) && (v >= 0d) && (u + v <= 1d);

        // Check if point is contained inside triangle (NOT including edges or vertices)
        //return (u > 0d) && (v > 0d) && (u + v < 1d);
    }

    /**
     * Determine the intersection of the triangle with a specified line.
     *
     * @param line   the line to test.
     * @param result contains the point of intersection after this method returns, if the line intersects this
     *               triangle.
     *
     * @return <code>true</code> if the line intersects this triangle, and <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException if the line or the result is <code>null</code>.
     */
    public boolean intersect(Line line, Vec4 result)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return intersect(line, this.a, this.b, this.c, result);
    }

    /**
     * Determines the intersection of a specified line with a specified triangle. The triangle is specified by three
     * points ordered counterclockwise. The triangle's front face is determined by the right-hand rule.
     *
     * @param line   the line to test.
     * @param a      the first vertex of the triangle.
     * @param b      the second vertex of the triangle.
     * @param c      the third vertex of the triangle.
     * @param result contains the point of intersection after this method returns, if the line intersects this
     *               triangle.
     *
     * @return <code>true</code> if the line intersects this triangle, and <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException if the line, any of the triangle vertices, or the result is <code>null</code>.
     */
    public static boolean intersect(Line line, Vec4 a, Vec4 b, Vec4 c, Vec4 result)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (a == null)
        {
            String msg = Logging.getMessage("nullValue.FirstVertexIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (b == null)
        {
            String msg = Logging.getMessage("nullValue.SecondVertexIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (c == null)
        {
            String msg = Logging.getMessage("nullValue.ThirdVertexIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return intersect(line, a.x, a.y, a.z, b.x, b.y, b.z, c.x, c.y, c.z, result);
    }

    /**
     * Determines the intersection of a specified line with a triangle specified by individual coordinates.
     *
     * @param line   the line to test.
     * @param vax    the X coordinate of the first vertex of the triangle.
     * @param vay    the Y coordinate of the first vertex of the triangle.
     * @param vaz    the Z coordinate of the first vertex of the triangle.
     * @param vbx    the X coordinate of the second vertex of the triangle.
     * @param vby    the Y coordinate of the second vertex of the triangle.
     * @param vbz    the Z coordinate of the second vertex of the triangle.
     * @param vcx    the X coordinate of the third vertex of the triangle.
     * @param vcy    the Y coordinate of the third vertex of the triangle.
     * @param vcz    the Z coordinate of the third vertex of the triangle.
     * @param result contains the point of intersection after this method returns, if the line intersects this
     *               triangle.
     *
     * @return <code>true</code> if the line intersects this triangle, and <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException if the line or the result is <code>null</code>.
     */
    public static boolean intersect(Line line,
        double vax, double vay, double vaz, double vbx, double vby, double vbz, double vcx, double vcy, double vcz,
        Vec4 result)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // taken from Moller and Trumbore
        // http://www.cs.virginia.edu/~gfx/Courses/2003/ImageSynthesis/papers/Acceleration/
        // Fast%20MinimumStorage%20RayTriangle%20Intersection.pdf

        Vec4 origin = line.getOrigin();
        Vec4 dir = line.getDirection();

        // find vectors for two edges sharing Point a: vb - va and vc - va
        double edge1x = vbx - vax;
        double edge1y = vby - vay;
        double edge1z = vbz - vaz;

        double edge2x = vcx - vax;
        double edge2y = vcy - vay;
        double edge2z = vcz - vaz;

        // Start calculating determinant. Compute cross product of line direction and edge2.
        double pvecx = (dir.y * edge2z) - (dir.z * edge2y);
        double pvecy = (dir.z * edge2x) - (dir.x * edge2z);
        double pvecz = (dir.x * edge2y) - (dir.y * edge2x);

        // Get determinant.
        double det = edge1x * pvecx + edge1y * pvecy + edge1z * pvecz; // edge1 dot pvec

        if (det > -EPSILON && det < EPSILON) // If det is near zero, then ray lies on plane of triangle
            return false;

        double detInv = 1d / det;

        // Distance from vertA to ray origin: origin - va
        double tvecx = origin.x - vax;
        double tvecy = origin.y - vay;
        double tvecz = origin.z - vaz;

        // Calculate u parameter and test bounds: 1/det * tvec dot pvec
        double u = detInv * (tvecx * pvecx + tvecy * pvecy + tvecz * pvecz);
        if (u < 0 || u > 1)
            return false;

        // Prepare to test v parameter: tvec cross edge1
        double qvecx = (tvecy * edge1z) - (tvecz * edge1y);
        double qvecy = (tvecz * edge1x) - (tvecx * edge1z);
        double qvecz = (tvecx * edge1y) - (tvecy * edge1x);

        // Calculate v parameter and test bounds: 1/det * dir dot qvec
        double v = detInv * (dir.x * qvecx + dir.y * qvecy + dir.z * qvecz);
        if (v < 0 || u + v > 1)
            return false;

        // Calculate the point of intersection on the line: t = 1/det * edge2 dot qvec;
        double t = detInv * (edge2x * qvecx + edge2y * qvecy + edge2z * qvecz);
        if (t < 0)
            return false;

        line.getPointAt(t, result);

        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.a).append(", ");
        sb.append(this.b).append(", ");
        sb.append(this.c);

        return sb.toString();
    }
}
