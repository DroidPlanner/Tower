/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.globes;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.terrain.ElevationModel;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id$
 */
public class EllipsoidalGlobe extends AbstractGlobe
{
    protected final double equatorialRadius;
    protected final double polarRadius;
    protected final double es;
    protected Vec4 center;

    public EllipsoidalGlobe(double equatorialRadius, double polarRadius, double es, ElevationModel elevationModel)
    {
        super(elevationModel);

        this.equatorialRadius = equatorialRadius;
        this.polarRadius = polarRadius;
        this.es = es;
        this.center = new Vec4(0, 0, 0);
        this.setElevationModel(elevationModel);
    }

    public EllipsoidalGlobe(double equatorialRadius, double polarRadius, double es, ElevationModel elevationModel,
        Vec4 center)
    {
        super(elevationModel);

        this.equatorialRadius = equatorialRadius;
        this.polarRadius = polarRadius;
        this.es = es;
        this.center = center;
        this.setElevationModel(elevationModel);
    }

    /** {@inheritDoc} */
    public double getRadius()
    {
        return this.equatorialRadius;
    }

    /** {@inheritDoc} */
    public Intersection[] intersect(Line line)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.intersect(line, this.equatorialRadius, this.polarRadius);
    }

    /** {@inheritDoc} */
    public boolean getIntersectionPosition(Line line, Position result)
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

        Intersection[] intersection = this.intersect(line);
        if (intersection == null || intersection.length == 0)
            return false;

        this.computePositionFromPoint(intersection[0].getIntersectionPoint(), result);
        return true;
    }

    protected Intersection[] intersect(Line line, double equRadius, double polRadius)
    {
        if (line == null)
            return null;

        // Taken from Lengyel, 2Ed., Section 5.2.3, page 148.

        double m = equRadius / polRadius; // "ratio of the x semi-axis length to the y semi-axis length"
        double n = 1d;                    // "ratio of the x semi-axis length to the z semi-axis length"
        double m2 = m * m;
        double n2 = n * n;
        double r2 = equRadius * equRadius; // nominal radius squared //equRadius * polRadius;

        double vx = line.getDirection().x;
        double vy = line.getDirection().y;
        double vz = line.getDirection().z;
        double sx = line.getOrigin().x;
        double sy = line.getOrigin().y;
        double sz = line.getOrigin().z;

        double a = vx * vx + m2 * vy * vy + n2 * vz * vz;
        double b = 2d * (sx * vx + m2 * sy * vy + n2 * sz * vz);
        double c = sx * sx + m2 * sy * sy + n2 * sz * sz - r2;

        double discriminant = discriminant(a, b, c);
        if (discriminant < 0)
            return null;

        double discriminantRoot = Math.sqrt(discriminant);
        if (discriminant == 0)
        {
            Vec4 p = new Vec4();
            line.getPointAt((-b - discriminantRoot) / (2 * a), p);
            return new Intersection[] {new Intersection(p, true)};
        }
        else // (discriminant > 0)
        {
            Vec4 near = new Vec4();
            Vec4 far = new Vec4();
            line.getPointAt((-b - discriminantRoot) / (2 * a), near);
            line.getPointAt((-b + discriminantRoot) / (2 * a), far);
            if (c >= 0) // Line originates outside the Globe.
                return new Intersection[] {new Intersection(near, false), new Intersection(far, false)};
            else // Line originates inside the Globe.
                return new Intersection[] {new Intersection(far, false)};
        }
    }

    protected static double discriminant(double a, double b, double c)
    {
        return b * b - 4 * a * c;
    }

    /** {@inheritDoc} */
    public Vec4 computePointFromPosition(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.computePointFromPosition(position.latitude, position.longitude, position.elevation);
    }

    /** {@inheritDoc} */
    public Vec4 computePointFromPosition(LatLon location, double metersElevation)
    {
        if (location == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.computePointFromPosition(location.latitude, location.longitude, metersElevation);
    }

    /** {@inheritDoc} */
    public Vec4 computePointFromPosition(Angle latitude, Angle longitude, double metersElevation)
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

        Vec4 result = new Vec4();
        this.geodeticToCartesian(latitude.radians, longitude.radians, metersElevation, result);
        return result;
    }

    /** {@inheritDoc} */
    public void computePointFromPosition(Position position, Vec4 result)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.computePointFromPosition(position.latitude, position.longitude, position.elevation, result);
    }

    /** {@inheritDoc} */
    public void computePointFromPosition(LatLon location, double metersElevation, Vec4 result)
    {
        if (location == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.computePointFromPosition(location.latitude, location.longitude, metersElevation, result);
    }

    /** {@inheritDoc} */
    public void computePointFromPosition(Angle latitude, Angle longitude, double metersElevation, Vec4 result)
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

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.geodeticToCartesian(latitude.radians, longitude.radians, metersElevation, result);
    }

    /** {@inheritDoc} */
    public void computePointsFromPositions(Sector sector, int numLat, int numLon, double[] metersElevation,
        Vec4[] result)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (numLat <= 0)
        {
            String msg = Logging.getMessage("generic.HeightIsInvalid", numLat);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (numLon <= 0)
        {
            String msg = Logging.getMessage("generic.WidthIsInvalid", numLon);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (metersElevation == null)
        {
            String msg = Logging.getMessage("nullValue.ElevationsBufferIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (metersElevation.length < numLat * numLon)
        {
            String msg = Logging.getMessage("generic.ElevationsBufferInvalidLength", metersElevation.length);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result.length < numLat * numLon)
        {
            String msg = Logging.getMessage("generic.ResultArrayInvalidLength", result.length);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.geodeticToCartesian(sector, numLat, numLon, metersElevation, result);
    }

    /** {@inheritDoc} */
    public Position computePositionFromPoint(Vec4 point)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Position result = new Position();
        this.cartesianToGeodetic(point, result);
        return result;
    }

    /** {@inheritDoc} */
    public void computePositionFromPoint(Vec4 point, Position result)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.cartesianToGeodetic(point, result);
    }

    /**
     * Maps a position to world Cartesian coordinates. The Y axis points to the north pole. The Z axis points to the
     * intersection of the prime meridian and the equator, in the equatorial plane. The X axis completes a right-handed
     * coordinate system, and is 90 degrees east of the Z axis and also in the equatorial plane. Sea level is at z =
     * zero.
     *
     * @param latitudeRadians  the latitudeRadians of the position, in radians.
     * @param longitudeRadians the longitudeRadians of the position, in radians.
     * @param metersElevation  the number of meters above or below mean sea level.
     * @param result           contains the Cartesian point corresponding to the input position after this method
     *                         returns.
     */
    protected void geodeticToCartesian(double latitudeRadians, double longitudeRadians, double metersElevation,
        Vec4 result)
    {
        double cosLat = Math.cos(latitudeRadians);
        double sinLat = Math.sin(latitudeRadians);
        double cosLon = Math.cos(longitudeRadians);
        double sinLon = Math.sin(longitudeRadians);

        double rpm = // getRadius (in meters) of vertical in prime meridian
            this.equatorialRadius / Math.sqrt(1.0 - this.es * sinLat * sinLat);

        result.x = (rpm + metersElevation) * cosLat * sinLon;
        result.y = (rpm * (1.0 - this.es) + metersElevation) * sinLat;
        result.z = (rpm + metersElevation) * cosLat * cosLon;
    }

    /**
     * Maps a geographic grid of positions to world Cartesian coordinates. The grid is evenly spaced locations in
     * latitude and longitude defined the sector, numLat, and numLon. Each grid position's elevation in meters is
     * defined by the metersElevation array. The result array is populated with the world Cartesian coordinate at each
     * grid position. Both the metersElevation and result array are organized by assigning index zero to the sector's
     * lower left corner and proceeding in row major order. Both the metersElevation and result array s must have length
     * of at least numLat * numLon.
     * <p/>
     * The Y axis points to the north pole. The Z axis points to the intersection of the prime meridian and the equator,
     * in the equatorial plane. The X axis completes a right-handed coordinate system, and is 90 degrees east of the Z
     * axis and also in the equatorial plane. Sea level is at z = zero.
     *
     * @param sector          the sector in question.
     * @param numLat          the grid height in number of latitude positions.
     * @param numLon          the grid width in number of longitude positions.
     * @param metersElevation an array containing the elevation for each position. The array must be pre-allocated and
     *                        contain at least numLat * numLon elements.
     * @param result          contains the Cartesian points corresponding to each grid position after this method
     *                        returns. The array must be pre-allocated and contain at least numLat * numLon non-null
     *                        elements.
     */
    protected void geodeticToCartesian(Sector sector, int numLat, int numLon, double[] metersElevation, Vec4[] result)
    {
        double minLat = sector.minLatitude.radians;
        double maxLat = sector.maxLatitude.radians;
        double minLon = sector.minLongitude.radians;
        double maxLon = sector.maxLongitude.radians;
        double deltaLat = sector.getDeltaLatRadians() / (numLat > 1 ? numLat - 1 : 1);
        double deltaLon = sector.getDeltaLonRadians() / (numLon > 1 ? numLon - 1 : 1);

        double lat = minLat;
        double lon = minLon;
        int index = 0;

        for (int j = 0; j < numLat; j++)
        {
            // Explicitly set the first and last row to minLat and maxLat, respectively, rather than using the
            // accumulated lat value. We do this to ensure that the Cartesian points of adjacent sectors are a
            // perfect match.
            if (j == 0)
                lat = minLat;
            else if (j == numLat - 1)
                lat = maxLat;
            else
                lat += deltaLat;

            // Latitude is constant for each row, therefore values depending only on latitude can be computed once per
            // row.
            double cosLat = Math.cos(lat);
            double sinLat = Math.sin(lat);
            double rpm = // getRadius (in meters) of vertical in prime meridian
                this.equatorialRadius / Math.sqrt(1.0 - this.es * sinLat * sinLat);

            for (int i = 0; i < numLon; i++)
            {
                // Explicitly set the first and last column to minLon and maxLon, respectively, rather than using the
                // accumulated lon value. We do this to ensure that the Cartesian points of adjacent sectors are a
                // perfect match.
                if (i == 0)
                    lon = minLon;
                else if (i == numLon - 1)
                    lon = maxLon;
                else
                    lon += deltaLon;

                double cosLon = Math.cos(lon);
                double sinLon = Math.sin(lon);

                result[index].x = (rpm + metersElevation[index]) * cosLat * sinLon;
                result[index].y = (rpm * (1.0 - this.es) + metersElevation[index]) * sinLat;
                result[index].z = (rpm + metersElevation[index]) * cosLat * cosLon;
                index++;
            }
        }
    }

    @SuppressWarnings( {"SuspiciousNameCombination"})
    protected void cartesianToGeodetic(Vec4 cart, Position result)
    {
        // Contributed by Nathan Kronenfeld. Integrated 1/24/2011. Brings this calculation in line with Vermeille's
        // most recent update.

        // According to
        // H. Vermeille,
        // "An analytical method to transform geocentric into geodetic coordinates"
        // http://www.springerlink.com/content/3t6837t27t351227/fulltext.pdf
        // Journal of Geodesy, accepted 10/2010, not yet published
        double X = cart.z;
        double Y = cart.x;
        double Z = cart.y;
        double XXpYY = X * X + Y * Y;
        double sqrtXXpYY = Math.sqrt(XXpYY);

        double a = this.equatorialRadius;
        double ra2 = 1 / (a * a);
        double e2 = this.es;
        double e4 = e2 * e2;

        // Step 1
        double p = XXpYY * ra2;
        double q = Z * Z * (1 - e2) * ra2;
        double r = (p + q - e4) / 6;

        double h;
        double phi;

        double evoluteBorderTest = 8 * r * r * r + e4 * p * q;
        if (evoluteBorderTest > 0 || q != 0)
        {
            double u;

            if (evoluteBorderTest > 0)
            {
                // Step 2: general case
                double rad1 = Math.sqrt(evoluteBorderTest);
                double rad2 = Math.sqrt(e4 * p * q);

                // 10*e2 is my arbitrary decision of what Vermeille means by "near... the cusps of the evolute".
                if (evoluteBorderTest > 10 * e2)
                {
                    double rad3 = Math.cbrt((rad1 + rad2) * (rad1 + rad2));
                    u = r + 0.5 * rad3 + 2 * r * r / rad3;
                }
                else
                {
                    u = r + 0.5 * Math.cbrt((rad1 + rad2) * (rad1 + rad2)) + 0.5 * Math.cbrt(
                        (rad1 - rad2) * (rad1 - rad2));
                }
            }
            else
            {
                // Step 3: near evolute
                double rad1 = Math.sqrt(-evoluteBorderTest);
                double rad2 = Math.sqrt(-8 * r * r * r);
                double rad3 = Math.sqrt(e4 * p * q);
                double atan = 2 * Math.atan2(rad3, rad1 + rad2) / 3;

                u = -4 * r * Math.sin(atan) * Math.cos(Math.PI / 6 + atan);
            }

            double v = Math.sqrt(u * u + e4 * q);
            double w = e2 * (u + v - q) / (2 * v);
            double k = (u + v) / (Math.sqrt(w * w + u + v) + w);
            double D = k * sqrtXXpYY / (k + e2);
            double sqrtDDpZZ = Math.sqrt(D * D + Z * Z);

            h = (k + e2 - 1) * sqrtDDpZZ / k;
            phi = 2 * Math.atan2(Z, sqrtDDpZZ + D);
        }
        else
        {
            // Step 4: singular disk
            double rad1 = Math.sqrt(1 - e2);
            double rad2 = Math.sqrt(e2 - p);
            double e = Math.sqrt(e2);

            h = -a * rad1 * rad2 / e;
            phi = rad2 / (e * rad2 + rad1 * Math.sqrt(p));
        }

        // Compute lambda
        double lambda;
        double s2 = Math.sqrt(2);
        if ((s2 - 1) * Y < sqrtXXpYY + X)
        {
            // case 1 - -135deg < lambda < 135deg
            lambda = 2 * Math.atan2(Y, sqrtXXpYY + X);
        }
        else if (sqrtXXpYY + Y < (s2 + 1) * X)
        {
            // case 2 - -225deg < lambda < 45deg
            lambda = -Math.PI * 0.5 + 2 * Math.atan2(X, sqrtXXpYY - Y);
        }
        else
        {
            // if (sqrtXXpYY-Y<(s2=1)*X) {  // is the test, if needed, but it's not
            // case 3: - -45deg < lambda < 225deg
            lambda = Math.PI * 0.5 - 2 * Math.atan2(X, sqrtXXpYY + Y);
        }

        result.setRadians(phi, lambda, h);
    }

    /** {@inheritDoc} */
    public Vec4 computeSurfaceNormalAtLocation(LatLon location)
    {
        if (location == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 result = new Vec4();
        this.computeSurfaceNormalAtLocation(location.latitude, location.longitude, result);
        return result;
    }

    /** {@inheritDoc} */
    public Vec4 computeSurfaceNormalAtLocation(Angle latitude, Angle longitude)
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

        Vec4 result = new Vec4();
        this.computeSurfaceNormalAtLocation(latitude, longitude, result);
        return result;
    }

    /** {@inheritDoc} */
    public void computeSurfaceNormalAtLocation(LatLon location, Vec4 result)
    {
        if (location == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.computeSurfaceNormalAtLocation(location.latitude, location.longitude, result);
    }

    /** {@inheritDoc} */
    public void computeSurfaceNormalAtLocation(Angle latitude, Angle longitude, Vec4 result)
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

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        double cosLat = latitude.cos();
        double cosLon = longitude.cos();
        double sinLat = latitude.sin();
        double sinLon = longitude.sin();

        double eqSquared = this.equatorialRadius * this.equatorialRadius;
        double polSquared = this.polarRadius * this.polarRadius;

        result.x = cosLat * sinLon / eqSquared;
        result.y = (1 - this.es) * sinLat / polSquared;
        result.z = cosLat * cosLon / eqSquared;
        result.normalize3AndSet();
    }

    public Vec4 computeNorthPointingTangentAtLocation(LatLon location)
    {
        if (location == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 result = new Vec4();
        this.computeNorthPointingTangentAtLocation(location.latitude, location.longitude, result);
        return result;
    }

    public Vec4 computeNorthPointingTangentAtLocation(Angle latitude, Angle longitude)
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

        Vec4 result = new Vec4();
        this.computeNorthPointingTangentAtLocation(latitude, longitude, result);
        return result;
    }

    /** {@inheritDoc} */
    public void computeNorthPointingTangentAtLocation(LatLon location, Vec4 result)
    {
        if (location == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.computeNorthPointingTangentAtLocation(location.latitude, location.longitude, result);
    }

    /** {@inheritDoc} */
    public void computeNorthPointingTangentAtLocation(Angle latitude, Angle longitude, Vec4 result)
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

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        double cosLat = latitude.cos();
        double sinLat = latitude.sin();
        double cosLon = longitude.cos();
        double sinLon = longitude.sin();

        // The north-pointing tangent is derived by rotating the vector (0, 1, 0) about the Y-axis by longitude degrees,
        // then rotating it about the X-axis by -latitude degrees. The latitude angle must be inverted because latitude
        // is a clockwise rotation about the X-axis, and standard rotation matrices assume counter-clockwise rotation.
        // The combined rotation can be represented by a combining two rotation matrices Rlat, and Rlon, then
        // transforming the vector (0, 1, 0) by the combined transform:
        //
        // NorthTangent = (Rlon * Rlat) * (0, 1, 0)
        //
        // This computation can be simplified and encoded inline by making two observations:
        // - The vector's X and Z coordinates are always 0, and its Y coordinate is always 1.
        // - Inverting the latitude rotation angle is equivalent to inverting sinLat. We know this by the trigonimetric
        //   identities cos(-x) = cos(x), and sin(-x) = -sin(x).

        result.x = -sinLat * sinLon;
        result.y = cosLat;
        result.z = -sinLat * cosLon;
        result.normalize3AndSet();
    }

    public Matrix computeViewOrientationAtPosition(Angle latitude, Angle longitude, double metersElevation)
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

        // Rotate the coordinate system to match the latitude.
        // Latitude is treated clockwise as rotation about the X-axis.
        // We don't flip the latitude value for a clockwise rotation
        // because we are computing the View.
        Matrix transform = Matrix.fromRotationX(latitude);
        // Rotate the coordinate system to match the longitude.
        // Longitude is treated as counter-clockwise rotation about the Y-axis.
        // Negate because we are dealing with the View
        transform = transform.multiply(Matrix.fromRotationY(longitude.multiply(-1)));
        Vec4 point = new Vec4();
        this.geodeticToCartesian(latitude.radians, longitude.radians, metersElevation, point);
        // Transform to the cartesian coordinates of (latitude, longitude, metersElevation).
        // Negate because we are computing the View
        point.multiply3AndSet(-1);
        transform = transform.multiply(Matrix.fromTranslation(point));

        return transform;
    }
}
