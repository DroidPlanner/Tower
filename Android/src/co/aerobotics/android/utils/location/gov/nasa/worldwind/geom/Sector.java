/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.geom;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.globes.Globe;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * @author dcollins
 * @version $Id$
 */
public class Sector implements Iterable<LatLon>
{
    public final Angle minLatitude;
    public final Angle maxLatitude;
    public final Angle minLongitude;
    public final Angle maxLongitude;

    public Sector()
    {
        this.minLatitude = new Angle();
        this.maxLatitude = new Angle();
        this.minLongitude = new Angle();
        this.maxLongitude = new Angle();
    }

    public Sector(Angle minLatitude, Angle maxLatitude, Angle minLongitude, Angle maxLongitude)
    {
        if (minLatitude == null)
        {
            String msg = Logging.getMessage("nullValue.MinLatitudeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (maxLatitude == null)
        {
            String msg = Logging.getMessage("nullValue.MaxLatitudeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (minLongitude == null)
        {
            String msg = Logging.getMessage("nullValue.MinLongitudeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (maxLongitude == null)
        {
            String msg = Logging.getMessage("nullValue.MaxLongitudeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
    }

    public static Sector fromDegrees(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude)
    {
        return new Sector(Angle.fromDegrees(minLatitude), Angle.fromDegrees(maxLatitude),
            Angle.fromDegrees(minLongitude), Angle.fromDegrees(maxLongitude));
    }

    public static Sector fromRadians(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude)
    {
        return new Sector(Angle.fromRadians(minLatitude), Angle.fromRadians(maxLatitude),
            Angle.fromRadians(minLongitude), Angle.fromRadians(maxLongitude));
    }

    public static Sector fromFullSphere()
    {
        return Sector.fromDegrees(-90, 90, -180, 180);
    }

    /**
     * Returns a {@link gov.nasa.worldwind.geom.Box} that bounds the specified sector on the surface of the specified
     * {@link Globe}. The returned box encloses the globe's surface terrain in the sector,
     * according to the specified vertical exaggeration and the globe's minimum and maximum elevations in the sector. If
     * the minimum and maximum elevation are equal, this assumes a maximum elevation of 10 + the minimum. If this fails
     * to compute a box enclosing the sector, this returns a unit box enclosing one of the boxes corners.
     *
     * @param globe                the globe the extent relates to.
     * @param verticalExaggeration the globe's vertical surface exaggeration.
     * @param sector               a sector on the globe's surface to compute a bounding box for.
     *
     * @return a box enclosing the globe's surface on the specified sector.
     *
     * @throws IllegalArgumentException if either the globe or sector is null.
     */
    public static Box computeBoundingBox(Globe globe, double verticalExaggeration, Sector sector)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        double[] minAndMaxElevations = globe.getMinAndMaxElevations(sector);
        return computeBoundingBox(globe, verticalExaggeration, sector,
            minAndMaxElevations[0], minAndMaxElevations[1]);
    }

    /**
     * Returns a {@link gov.nasa.worldwind.geom.Box} that bounds the specified sector on the surface of the specified
     * {@link Globe}. The returned box encloses the globe's surface terrain in the sector,
     * according to the specified vertical exaggeration, minimum elevation, and maximum elevation. If the minimum and
     * maximum elevation are equal, this assumes a maximum elevation of 10 + the minimum. If this fails to compute a box
     * enclosing the sector, this returns a unit box enclosing one of the boxes corners.
     *
     * @param globe                the globe the extent relates to.
     * @param verticalExaggeration the globe's vertical surface exaggeration.
     * @param sector               a sector on the globe's surface to compute a bounding box for.
     * @param minElevation         the globe's minimum elevation in the sector.
     * @param maxElevation         the globe's maximum elevation in the sector.
     *
     * @return a box enclosing the globe's surface on the specified sector.
     *
     * @throws IllegalArgumentException if either the globe or sector is null.
     */
    public static Box computeBoundingBox(Globe globe, double verticalExaggeration, Sector sector,
        double minElevation, double maxElevation)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute the exaggerated minimum and maximum heights.
        double minHeight = minElevation * verticalExaggeration;
        double maxHeight = maxElevation * verticalExaggeration;

        if (minHeight == maxHeight)
            maxHeight = minHeight + 10; // Ensure the top and bottom heights are not equal.

        List<Vec4> points = new ArrayList<Vec4>();
        for (LatLon ll : sector)
        {
            points.add(globe.computePointFromPosition(ll, minHeight));
            points.add(globe.computePointFromPosition(ll, maxHeight));
        }

        // A point at the centroid captures the maximum vertical dimension.
        LatLon centroid = sector.getCentroid();
        points.add(globe.computePointFromPosition(centroid, maxHeight));

        // If the sector spans the equator, then the curvature of all four edges need to be taken into account. The
        // extreme points along the top and bottom edges are located at their mid-points, and the extreme points along
        // the left and right edges are on the equator. Add points with the longitude of the sector's centroid but with
        // the sector's min and max latitude, and add points with the sector's min and max longitude but with latitude
        // at the equator. See WWJINT-225.
        if (sector.minLatitude.degrees < 0 && sector.maxLatitude.degrees > 0)
        {
            points.add(globe.computePointFromPosition(new LatLon(sector.minLatitude, centroid.longitude),
                maxHeight));
            points.add(globe.computePointFromPosition(new LatLon(sector.maxLatitude, centroid.longitude),
                maxHeight));
            points.add(globe.computePointFromPosition(new LatLon(Angle.fromDegrees(0), sector.minLongitude),
                maxHeight));
            points.add(globe.computePointFromPosition(new LatLon(Angle.fromDegrees(0), sector.maxLongitude),
                maxHeight));
        }
        // If the sector is located entirely in the southern hemisphere, then the curvature of its top edge needs to be
        // taken into account. The extreme point along the top edge is located at its mid-point. Add a point with the
        // longitude of the sector's centroid but with the sector's max latitude. See WWJINT-225.
        else if (sector.minLatitude.degrees < 0)
        {
            points.add(globe.computePointFromPosition(new LatLon(sector.maxLatitude, centroid.longitude),
                maxHeight));
        }
        // If the sector is located entirely in the northern hemisphere, then the curvature of its bottom edge needs to
        // be taken into account. The extreme point along the bottom edge is located at its mid-point. Add a point with
        // the longitude of the sector's centroid but with the sector's min latitude. See WWJINT-225.
        else
        {
            points.add(globe.computePointFromPosition(new LatLon(sector.minLatitude, centroid.longitude),
                maxHeight));
        }

        if (sector.getDeltaLonDegrees() > 180)
        {
            // Need to compute more points to ensure the box encompasses the full sector.
            Angle cLon = sector.getCentroid().longitude;
            Angle cLat = sector.getCentroid().latitude;

            // centroid latitude, longitude midway between min longitude and centroid longitude
            Angle lon = Angle.midAngle(sector.minLongitude, cLon);
            points.add(globe.computePointFromPosition(cLat, lon, maxHeight));

            // centroid latitude, longitude midway between centroid longitude and max longitude
            lon = Angle.midAngle(cLon, sector.maxLongitude);
            points.add(globe.computePointFromPosition(cLat, lon, maxHeight));

            // centroid latitude, longitude at min longitude and max longitude
            points.add(globe.computePointFromPosition(cLat, sector.minLongitude, maxHeight));
            points.add(globe.computePointFromPosition(cLat, sector.maxLongitude, maxHeight));
        }

        try
        {
            return Box.computeBoundingBox(points);
        }
        catch (Exception e)
        {
            return new Box(points.get(0)); // unit box around point
        }
    }

    public Sector copy()
    {
        return new Sector(this.minLatitude.copy(), this.maxLatitude.copy(), this.minLongitude.copy(),
            this.maxLongitude.copy());
    }

    public Sector set(Sector sector)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.minLatitude.set(sector.minLatitude);
        this.maxLatitude.set(sector.maxLatitude);
        this.minLongitude.set(sector.minLongitude);
        this.maxLongitude.set(sector.maxLongitude);

        return this;
    }

    public Sector set(Angle minLatitude, Angle maxLatitude, Angle minLongitude, Angle maxLongitude)
    {
        if (minLatitude == null)
        {
            String msg = Logging.getMessage("nullValue.MinLatitudeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (maxLatitude == null)
        {
            String msg = Logging.getMessage("nullValue.MaxLatitudeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (minLongitude == null)
        {
            String msg = Logging.getMessage("nullValue.MinLongitudeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (maxLongitude == null)
        {
            String msg = Logging.getMessage("nullValue.MaxLongitudeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.minLatitude.set(minLatitude);
        this.maxLatitude.set(maxLatitude);
        this.minLongitude.set(minLongitude);
        this.maxLongitude.set(maxLongitude);

        return this;
    }

    public Sector setDegrees(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude)
    {
        this.minLatitude.setDegrees(minLatitude);
        this.maxLatitude.setDegrees(maxLatitude);
        this.minLongitude.setDegrees(minLongitude);
        this.maxLongitude.setDegrees(maxLongitude);

        return this;
    }

    public Sector setRadians(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude)
    {
        this.minLatitude.setRadians(minLatitude);
        this.maxLatitude.setRadians(maxLatitude);
        this.minLongitude.setRadians(minLongitude);
        this.maxLongitude.setRadians(maxLongitude);

        return this;
    }

    public Angle getDeltaLat()
    {
        return Angle.fromDegrees(this.maxLatitude.degrees - this.minLatitude.degrees);
    }

    public Angle getDeltaLon()
    {
        return Angle.fromDegrees(this.maxLongitude.degrees - this.minLongitude.degrees);
    }

    public double getDeltaLatDegrees()
    {
        return this.maxLatitude.degrees - this.minLatitude.degrees;
    }

    public double getDeltaLonDegrees()
    {
        return this.maxLongitude.degrees - this.minLongitude.degrees;
    }

    public double getDeltaLatRadians()
    {
        return this.maxLatitude.radians - this.minLatitude.radians;
    }

    public double getDeltaLonRadians()
    {
        return this.maxLongitude.radians - this.minLongitude.radians;
    }

    public LatLon getCentroid()
    {
        return LatLon.fromDegrees(
            0.5 * (this.minLatitude.degrees + this.maxLatitude.degrees),
            0.5 * (this.minLongitude.degrees + this.maxLongitude.degrees));
    }

    /**
     * Computes the Cartesian coordinates of a Sector's center.
     *
     * @param result the Cartesian coordinates of the sector's center.
     *
     * @throws IllegalArgumentException if <code>globe</code> is null.
     */
    public void getCentroid(LatLon result)
    {
        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        result.setDegrees(
            0.5 * (this.minLatitude.degrees + this.maxLatitude.degrees),
            0.5 * (this.minLongitude.degrees + this.maxLongitude.degrees));
    }

    public boolean isEmpty()
    {
        return this.minLatitude.degrees == this.maxLatitude.degrees
            && this.minLongitude.degrees == this.maxLongitude.degrees;
    }

    /**
     * Determines whether a latitude/longitude position is within the sector. The sector's angles are assumed to be
     * normalized to +/- 90 degrees latitude and +/- 180 degrees longitude. The result of the operation is undefined if
     * they are not.
     *
     * @param location the position to test, with angles normalized to +/- &#960 latitude and +/- 2&#960 longitude.
     *
     * @return <code>true</code> if the position is within the sector, <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException if <code>latlon</code> is null.
     */
    public boolean contains(LatLon location)
    {
        if (location == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.containsDegrees(location.latitude.degrees, location.longitude.degrees);
    }

    public boolean contains(Angle latitude, Angle longitude)
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

        return this.containsDegrees(latitude.degrees, longitude.degrees);
    }

    public boolean containsDegrees(double latitude, double longitude)
    {
        return latitude >= this.minLatitude.degrees && latitude <= this.maxLatitude.degrees
            && longitude >= this.minLongitude.degrees && longitude <= this.maxLongitude.degrees;
    }

    public boolean containsRadians(double latitude, double longitude)
    {
        return latitude >= this.minLatitude.radians && latitude <= this.maxLatitude.radians
            && longitude >= this.minLongitude.radians && longitude <= this.maxLongitude.radians;
    }

    /**
     * Determines whether another sectror is fully contained within this one. The sector's angles are assumed to be
     * normalized to +/- 90 degrees latitude and +/- 180 degrees longitude. The result of the operation is undefined if
     * they are not.
     *
     * @param that the sector to test for containment.
     *
     * @return <code>true</code> if this sector fully contains the input sector, otherwise <code>false</code>.
     */
    public boolean contains(Sector that)
    {
        if (that == null)
            return false;

        // Assumes normalized angles -- [-180, 180], [-90, 90]
        if (that.minLongitude.degrees < this.minLongitude.degrees)
            return false;
        if (that.maxLongitude.degrees > this.maxLongitude.degrees)
            return false;
        if (that.minLatitude.degrees < this.minLatitude.degrees)
            return false;
        //noinspection RedundantIfStatement
        if (that.maxLatitude.degrees > this.maxLatitude.degrees)
            return false;

        return true;
    }

    /**
     * Determines whether this sector intersects another sector's range of latitude and longitude. The sector's angles
     * are assumed to be normalized to +/- 90 degrees latitude and +/- 180 degrees longitude. The result of the
     * operation is undefined if they are not.
     *
     * @param that the sector to test for intersection.
     *
     * @return <code>true</code> if the sectors intersect, otherwise <code>false</code>.
     */
    public boolean intersects(Sector that)
    {
        if (that == null)
            return false;

        // Assumes normalized angles -- [-180, 180], [-90, 90]
        if (that.maxLongitude.degrees < this.minLongitude.degrees)
            return false;
        if (that.minLongitude.degrees > this.maxLongitude.degrees)
            return false;
        if (that.maxLatitude.degrees < this.minLatitude.degrees)
            return false;
        //noinspection RedundantIfStatement
        if (that.minLatitude.degrees > this.maxLatitude.degrees)
            return false;

        return true;
    }

    /**
     * Determines whether the interiors of this sector and another sector intersect. The sector's angles are assumed to
     * be normalized to +/- 90 degrees latitude and +/- 180 degrees longitude. The result of the operation is undefined
     * if they are not.
     *
     * @param that the sector to test for intersection.
     *
     * @return <code>true</code> if the sectors' interiors intersect, otherwise <code>false</code>.
     *
     * @see #intersects(Sector)
     */
    public boolean intersectsInterior(Sector that)
    {
        if (that == null)
            return false;

        // Assumes normalized angles -- [-180, 180], [-90, 90]
        if (that.maxLongitude.degrees <= this.minLongitude.degrees)
            return false;
        if (that.minLongitude.degrees >= this.maxLongitude.degrees)
            return false;
        if (that.maxLatitude.degrees <= this.minLatitude.degrees)
            return false;
        //noinspection RedundantIfStatement
        if (that.minLatitude.degrees >= this.maxLatitude.degrees)
            return false;

        return true;
    }

    public Sector[] subdivide(int div)
    {
        double dLat = this.getDeltaLatDegrees() / div;
        double dLon = this.getDeltaLonDegrees() / div;

        Sector[] sectors = new Sector[div * div];
        int idx = 0;
        for (int row = 0; row < div; row++)
        {
            for (int col = 0; col < div; col++)
            {
                sectors[idx++] = Sector.fromDegrees(
                    this.minLatitude.degrees + dLat * row,
                    this.minLatitude.degrees + dLat * row + dLat,
                    this.minLongitude.degrees + dLon * col,
                    this.minLongitude.degrees + dLon * col + dLon);
            }
        }

        return sectors;
    }

    /**
     * Computes the Cartesian coordinates of a Sector's center.
     *
     * @param globe        The globe associated with the sector.
     * @param exaggeration The vertical exaggeration to apply.
     * @param result       contains the Cartesian coordinates of the sector's center after this method returns.
     *
     * @throws IllegalArgumentException if the globe is <code>null</code> or if the result is <code>null</code>.
     */
    public void computeCentroidPoint(Globe globe, double exaggeration, Vec4 result)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle lat = Angle.fromDegrees(0.5 * (this.minLatitude.radians + this.maxLatitude.radians));
        Angle lon = Angle.fromDegrees(0.5 * (this.minLongitude.radians + this.maxLongitude.radians));
        globe.computePointFromPosition(lat, lon, exaggeration * globe.getElevation(lat, lon), result);
    }

    /**
     * Computes the Cartesian coordinates of this Sector's corners.
     *
     * @param globe        The globe associated with the sector.
     * @param exaggeration The vertical exaggeration to apply.
     * @param result       an array containing the four Cartesian points corresponding to each corner of this Sector.
     *                     The array must be pre-allocated and contain at least 4 non-null elements.
     *
     * @throws IllegalArgumentException if the globe is <code>null</code>, if the result array is <code>null</code>, or
     *                                  if the result array length is less than 4.
     */
    public void computeCornerPoints(Globe globe, double exaggeration, Vec4[] result)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result.length < 4)
        {
            String msg = Logging.getMessage("generic.ResultArrayInvalidLength", result.length);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle minLat = this.minLatitude;
        Angle maxLat = this.maxLatitude;
        Angle minLon = this.minLongitude;
        Angle maxLon = this.maxLongitude;

        globe.computePointFromPosition(minLat, minLon, exaggeration * globe.getElevation(minLat, minLon), result[0]);
        globe.computePointFromPosition(minLat, maxLon, exaggeration * globe.getElevation(minLat, maxLon), result[1]);
        globe.computePointFromPosition(maxLat, maxLon, exaggeration * globe.getElevation(maxLat, maxLon), result[2]);
        globe.computePointFromPosition(maxLat, minLon, exaggeration * globe.getElevation(maxLat, minLon), result[3]);
    }

    /**
     * Sets this sector to the union of this sector and another. After this method returns this sector's minimum
     * latitude and longitude will be the minimum of the two sectors, and its maximum latitude and longitude will be the
     * maximum of the two sectors. The sectors are assumed to be normalized to +/- 90 degrees latitude and +/- 180
     * degrees longitude. The result of the operation is undefined if they are not.
     *
     * @param sector the sector to join with <code>this</code>.
     *
     * @return A reference to this sector.
     *
     * @throws IllegalArgumentException if the sector is null.
     */
    public Sector union(Sector sector)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (sector.minLatitude.degrees < this.minLatitude.degrees)
            this.minLatitude.set(sector.minLatitude);
        if (sector.maxLatitude.degrees > this.maxLatitude.degrees)
            this.maxLatitude.set(sector.maxLatitude);
        if (sector.minLongitude.degrees < this.minLongitude.degrees)
            this.minLongitude.set(sector.minLongitude);
        if (sector.maxLongitude.degrees > this.maxLongitude.degrees)
            this.maxLongitude.set(sector.maxLongitude);

        return this;
    }

    public Sector union(Sector lhs, Sector rhs)
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

        this.minLatitude.set(lhs.minLatitude.degrees < rhs.minLatitude.degrees ? lhs.minLatitude : rhs.minLatitude);
        this.maxLatitude.set(lhs.maxLatitude.degrees > rhs.maxLatitude.degrees ? lhs.maxLatitude : rhs.maxLatitude);
        this.minLongitude.set(
            lhs.minLongitude.degrees < rhs.minLongitude.degrees ? lhs.minLongitude : rhs.minLongitude);
        this.maxLongitude.set(
            lhs.maxLongitude.degrees > rhs.maxLongitude.degrees ? lhs.maxLongitude : rhs.maxLongitude);

        return this;
    }

    public Sector intersection(Sector sector)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle minLat, maxLat;
        minLat = (this.minLatitude.degrees > sector.minLatitude.degrees) ? this.minLatitude : sector.minLatitude;
        maxLat = (this.maxLatitude.degrees < sector.maxLatitude.degrees) ? this.maxLatitude : sector.maxLatitude;
        if (minLat.degrees > maxLat.degrees)
            maxLat = minLat;

        Angle minLon, maxLon;
        minLon = (this.minLongitude.degrees > sector.minLongitude.degrees) ? this.minLongitude : sector.minLongitude;
        maxLon = (this.maxLongitude.degrees < sector.maxLongitude.degrees) ? this.maxLongitude : sector.maxLongitude;
        if (minLon.degrees > maxLon.degrees)
            maxLon = minLon;

        this.minLatitude.set(minLat);
        this.maxLatitude.set(maxLat);
        this.minLongitude.set(minLon);
        this.maxLongitude.set(maxLon);

        return this;
    }

    public Sector intersection(Sector lhs, Sector rhs)
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

        Angle minLat, maxLat;
        minLat = (lhs.minLatitude.degrees > rhs.minLatitude.degrees) ? lhs.minLatitude : rhs.minLatitude;
        maxLat = (lhs.maxLatitude.degrees < rhs.maxLatitude.degrees) ? lhs.maxLatitude : rhs.maxLatitude;
        if (minLat.degrees > maxLat.degrees)
            maxLat = minLat;

        Angle minLon, maxLon;
        minLon = (lhs.minLongitude.degrees > rhs.minLongitude.degrees) ? lhs.minLongitude : rhs.minLongitude;
        maxLon = (lhs.maxLongitude.degrees < rhs.maxLongitude.degrees) ? lhs.maxLongitude : rhs.maxLongitude;
        if (minLon.degrees > maxLon.degrees)
            maxLon = minLon;

        this.minLatitude.set(minLat);
        this.maxLatitude.set(maxLat);
        this.minLongitude.set(minLon);
        this.maxLongitude.set(maxLon);

        return this;
    }

    /**
     * Creates an iterator over the four corners of the sector, starting with the southwest position and continuing
     * counter-clockwise.
     *
     * @return an iterator for the sector.
     */
    public Iterator<LatLon> iterator()
    {
        return new Iterator<LatLon>()
        {
            private int position = 0;

            public boolean hasNext()
            {
                return this.position < 4;
            }

            public LatLon next()
            {
                if (this.position > 3)
                    throw new NoSuchElementException();

                LatLon p;
                switch (this.position)
                {
                    case 0:
                        p = new LatLon(Sector.this.minLatitude, Sector.this.minLongitude);
                        break;
                    case 1:
                        p = new LatLon(Sector.this.minLatitude, Sector.this.maxLongitude);
                        break;
                    case 2:
                        p = new LatLon(Sector.this.maxLatitude, Sector.this.maxLongitude);
                        break;
                    default:
                        p = new LatLon(Sector.this.maxLatitude, Sector.this.minLongitude);
                        break;
                }
                ++this.position;

                return p;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns a list of the Lat/Lon coordinates of a Sector's corners.
     *
     * @return an array of the four corner locations, in the order SW, SE, NE, NW
     */
    public LatLon[] getCorners()
    {
        LatLon[] corners = new LatLon[4];

        corners[0] = new LatLon(this.minLatitude, this.minLongitude);
        corners[1] = new LatLon(this.minLatitude, this.maxLongitude);
        corners[2] = new LatLon(this.maxLatitude, this.maxLongitude);
        corners[3] = new LatLon(this.maxLatitude, this.minLongitude);

        return corners;
    }

    /**
     * Retrieve the size of this object in bytes. This implementation returns an exact value of the object's size.
     *
     * @return the size of this object in bytes
     */
    public long getSizeInBytes()
    {
        return 4 * minLatitude.getSizeInBytes();  // 4 angles
    }

    /**
     * Tests the equality of the sectors' angles. Sectors are equal if all of their corresponding angles are equal.
     *
     * @param o the sector to compareTo with <code>this</code>.
     *
     * @return <code>true</code> if the four corresponding angles of each sector are equal, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        Sector that = (Sector) o;
        return this.minLatitude.equals(that.minLatitude)
            && this.maxLatitude.equals(that.maxLatitude)
            && this.minLongitude.equals(that.minLongitude)
            && this.maxLongitude.equals(that.maxLongitude);
    }

    /**
     * Computes a hash code from the sector's four angles.
     *
     * @return a hash code incorporating the sector's four angles.
     */
    @Override
    public int hashCode()
    {
        int result;
        result = this.minLatitude.hashCode();
        result = 29 * result + this.maxLatitude.hashCode();
        result = 29 * result + this.minLongitude.hashCode();
        result = 29 * result + this.maxLongitude.hashCode();
        return result;
    }

    /**
     * Returns a string indicating the sector's angles.
     *
     * @return A string indicating the sector's angles.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("(");
        sb.append(this.minLatitude.toString());
        sb.append(", ");
        sb.append(this.minLongitude.toString());
        sb.append(")");
        sb.append(", ");
        sb.append("(");
        sb.append(this.maxLatitude.toString());
        sb.append(", ");
        sb.append(this.maxLongitude.toString());
        sb.append(")");
        sb.append(")");
        return sb.toString();
    }
}
