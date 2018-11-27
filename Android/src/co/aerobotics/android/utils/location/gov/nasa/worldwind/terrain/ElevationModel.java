/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.terrain;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.*;

import java.util.List;

/**
 * <p/>
 * Provides the elevations to a {@link gov.nasa.worldwind.globes.Globe} or other object holding elevations.
 * <p/>
 * An <code>ElevationModel</code> often approximates elevations at multiple levels of spatial resolution. For any given
 * viewing position, the model determines an appropriate target resolution. That target resolution may not be
 * immediately achievable, however, because the corresponding elevation data might not be locally available and must be
 * retrieved from a remote location. When this is the case, the elevations returned for a sector represent the
 * resolution achievable with the data currently available. That resolution may not be the same as the target
 * resolution. The achieved resolution is made available in the interface.
 * <p/>
 *
 * @author dcollins
 * @version $Id$
 */
public interface ElevationModel extends WWObject, Restorable, Disposable
{
    /**
     * Returns the elevation model's name.
     *
     * @return the elevation model's name.
     *
     * @see #setName(String)
     */
    String getName();

    /**
     * Set the elevation model's name. The name is a convenience attribute typically used to identify the elevation
     * model in user interfaces. By default, an elevation model has no name.
     *
     * @param name the name to give the elevation model.
     */
    void setName(String name);

    /**
     * Indicates whether the elevation model is allowed to retrieve data from the network. Some elevation models have no
     * need to retrieve data from the network. This flag is meaningless for such elevation models.
     *
     * @return <code>true</code> if the elevation model is enabled to retrieve network data, else <code>false</code>.
     */
    boolean isNetworkRetrievalEnabled();

    /**
     * Controls whether the elevation model is allowed to retrieve data from the network. Some elevation models have no
     * need for data from the network. This flag may be set but is meaningless for such elevation models.
     *
     * @param networkRetrievalEnabled <code>true</code> if network retrieval is allowed, else <code>false</code>.
     */
    void setNetworkRetrievalEnabled(boolean networkRetrievalEnabled);

    /**
     * Returns the current expiry time.
     *
     * @return the current expiry time.
     *
     * @see #setExpiryTime(long)
     */
    long getExpiryTime();

    /**
     * Specifies the time of the elevation model's most recent dataset update. If greater than zero, the model ignores
     * and eliminates any previously cached data older than the time specified, and requests new information from the
     * data source. If zero, the model uses any expiry times intrinsic to the model, typically initialized at model
     * construction. The default expiry time is 0, thereby enabling a model's intrinsic expiration criteria.
     *
     * @param expiryTime the expiry time of any cached data, expressed as a number of milliseconds beyond the epoch.
     *
     * @see System#currentTimeMillis() for a description of milliseconds beyond the epoch.
     */
    void setExpiryTime(long expiryTime);

    /**
     * Specifies the value used to identify missing data in an elevation model. Locations with this elevation value are
     * assigned the missing-data replacement value, specified by {@link #setMissingDataReplacement(double)}.
     * <p/>
     * The missing-data value is often specified by the metadata of the data set, in which case the elevation model
     * automatically defines that value to be the missing-data signal. When the missing-data signal is not specified in
     * the metadata, the application may specify it via this method.
     *
     * @param flag the missing-data signal value. The default is -{@link Double#MAX_VALUE}.
     *
     * @see #setMissingDataReplacement(double)
     * @see #getMissingDataSignal
     */
    void setMissingDataSignal(double flag);

    /**
     * Returns the current missing-data signal.
     *
     * @return the missing-data signal.
     *
     * @see #getMissingDataReplacement()
     */
    double getMissingDataSignal();

    /**
     * Indicates whether the elevation model covers a specified sector either partially or fully.
     *
     * @param sector the sector in question.
     *
     * @return 0 if the elevation model fully contains the sector, 1 if the elevation model intersects the sector but
     *         does not fully contain it, or -1 if the sector does not intersect the elevation model.
     */
    int intersects(Sector sector);

    /**
     * Indicates whether a specified location is within the elevation model's domain.
     *
     * @param latitude  the latitude of the location in question.
     * @param longitude the longitude of the location in question.
     *
     * @return true if the location is within the elevation model's domain, otherwise false.
     */
    boolean contains(Angle latitude, Angle longitude);

    /**
     * Returns the maximum elevation contained in the elevation model. When the elevation model is associated with a
     * globe, this value is the elevation of the highest point on the globe.
     *
     * @return The maximum elevation of the elevation model.
     */
    double getMaxElevation();

    /**
     * Returns the minimum elevation contained in the elevation model. When associated with a globe, this value is the
     * elevation of the lowest point on the globe. It may be negative, indicating a value below mean surface level. (Sea
     * level in the case of Earth.)
     *
     * @return The minimum elevation of the model.
     */
    double getMinElevation();

    /**
     * Returns the minimum and maximum elevations at a specified location.
     *
     * @param latitude  the latitude of the location in question.
     * @param longitude the longitude of the location in question.
     *
     * @return A two-element <code>double</code> array indicating, respectively, the minimum and maximum elevations at
     *         the specified location. These values are the global minimum and maximum if the local minimum and maximum
     *         values are currently unknown.
     */
    double[] getExtremeElevations(Angle latitude, Angle longitude);

    /**
     * Returns the minimum and maximum elevations within a specified sector of the elevation model.
     *
     * @param sector the sector in question.
     *
     * @return A two-element <code>double</code> array indicating, respectively, the sector's minimum and maximum
     *         elevations. These elements are the global minimum and maximum if the local minimum and maximum values are
     *         currently unknown.
     */
    double[] getExtremeElevations(Sector sector);

    /**
     * Indicates the best resolution attainable for a specified sector.
     *
     * @param sector the sector in question. If null, the elevation model's best overall resolution is returned. This is
     *               the best attainable at <em>some</em> locations but not necessarily at all locations.
     *
     * @return the best resolution attainable for the specified sector, in radians, or {@link Double#MAX_VALUE} if the
     *         sector does not intersect the elevation model.
     */
    double getBestResolution(Sector sector);

    /**
     * Returns the detail hint associated with the specified sector. If the elevation model does not have any detail
     * hint for the sector, this method returns zero.
     *
     * @param sector the sector in question.
     *
     * @return The detail hint corresponding to the specified sector.
     *
     * @throws IllegalArgumentException if <code>sector</code> is null.
     */
    double getDetailHint(Sector sector);

    /**
     * Returns the elevation at a specified location. If the elevation at the specified location is the elevation
     * model's missing data signal, or if the location specified is outside the elevation model's coverage area, the
     * elevation model's missing data replacement value is returned.
     * <p/>
     * The elevation returned from this method is the best available in memory. If no elevation is in memory, the
     * elevation model's minimum extreme elevation at the location is returned. Local disk caches are not consulted.
     *
     * @param latitude  the latitude of the location in question.
     * @param longitude the longitude of the location in question.
     *
     * @return The elevation corresponding to the specified location, or the elevation model's missing-data replacement
     *         value if there is no elevation for the given location.
     *
     * @see #setMissingDataSignal(double)
     * @see #getUnmappedElevation(gov.nasa.worldwind.geom.Angle, gov.nasa.worldwind.geom.Angle)
     */
    double getElevation(Angle latitude, Angle longitude);

    /**
     * Returns the elevation at a specified location, but without replacing missing data with the elevation model's
     * missing data replacement value. When a missing data signal is found, the signal value is returned, not the
     * replacement value.
     *
     * @param latitude  the latitude of the location for which to return the elevation.
     * @param longitude the longitude of the location for which to return the elevation.
     *
     * @return the elevation at the specified location, or the elevation model's missing data signal. If no data is
     *         currently in memory for the location, and the location is within the elevation model's coverage area, the
     *         elevation model's minimum elevation at that location is returned.
     */
    double getUnmappedElevation(Angle latitude, Angle longitude);

    /**
     * Returns the elevations of a collection of locations. Replaces any elevation values corresponding to the missing
     * data signal with the elevation model's missing data replacement value. If a location within the elevation model's
     * coverage area cannot currently be determined, the elevation model's minimum extreme elevation for that location
     * is returned in the output buffer. If a location is outside the elevation model's coverage area, the output buffer
     * for that location is not modified; it retains the buffer's original value.
     *
     * @param sector           the sector in question.
     * @param latlons          the locations to return elevations for. If a location is null, the output buffer for that
     *                         location is not modified.
     * @param targetResolution the desired horizontal resolution, in radians, of the raster or other elevation sample
     *                         from which elevations are drawn. (To compute radians from a distance, divide the distance
     *                         by the radius of the globe, ensuring that both the distance and the radius are in the
     *                         same units.)
     * @param buffer           an array in which to place the returned elevations. The array must be pre-allocated and
     *                         contain at least as many elements as the list of locations.
     *
     * @return the resolution achieved, in radians, or {@link Double#MAX_VALUE} if individual elevations cannot be
     *         determined for all of the locations.
     *
     * @see #setMissingDataSignal(double)
     */
    @SuppressWarnings( {"JavadocReference"})
    double getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer);

    /**
     * Returns the elevations of a collection of locations. <em>Does not</em> replace any elevation values corresponding
     * to the missing data signal with the elevation model's missing data replacement value. If a location within the
     * elevation model's coverage area cannot currently be determined, the elevation model's minimum extreme elevation
     * for that location is returned in the output buffer. If a location is outside the elevation model's coverage area,
     * the output buffer for that location is not modified; it retains the buffer's original value.
     *
     * @param sector           the sector in question.
     * @param latlons          the locations to return elevations for. If a location is null, the output buffer for that
     *                         location is not modified.
     * @param targetResolution the desired horizontal resolution, in radians, of the raster or other elevation sample
     *                         from which elevations are drawn. (To compute radians from a distance, divide the distance
     *                         by the radius of the globe, ensuring that both the distance and the radius are in the
     *                         same units.)
     * @param buffer           an array in which to place the returned elevations. The array must be pre-allocated and
     *                         contain at least as many elements as the list of locations.
     *
     * @return the resolution achieved, in radians, or {@link Double#MAX_VALUE} if individual elevations cannot be
     *         determined for all of the locations.
     *
     * @see #setMissingDataSignal(double)
     */
    double getUnmappedElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution,
        double[] buffer);

    /**
     * Returns the elevation used for missing values in the elevation model.
     *
     * @return the value that indicates that no data is available at a location.
     *
     * @see #setMissingDataSignal(double)
     * @see #getMissingDataSignal
     */
    double getMissingDataReplacement();

    /**
     * Specifies the elevation used for missing values in the elevation model.
     *
     * @param missingDataValue the value that indicates that no data is available at a location.
     *
     * @see #setMissingDataSignal(double)
     */
    void setMissingDataReplacement(double missingDataValue);

    /**
     * Determines the elevations at specified locations within a specified {@link Sector}.
     *
     * @param sector    the sector containing the locations.
     * @param latlons   the locations for which to return elevations.
     * @param tileWidth the number of locations that comprise one row in the {@code latlons} argument.
     * @param buffer    a buffer in which to put the elevations. The buffer must have at least as many elements as the
     *                  number of specified locations.
     *
     * @throws Exception                if the method fails. Different elevation models may fail for different reasons.
     * @throws IllegalArgumentException if either the sector, list of locations or buffer is null, if the buffer size is
     *                                  not at least as large as the location list, or the tile width is greater than
     *                                  the locations list length or less than 1.
     */
    void composeElevations(Sector sector, List<? extends LatLon> latlons, int tileWidth, double[] buffer)
        throws Exception;

    /**
     * Returns the proportion of this elevation model's data that is local -- in the computer's data cache or installed
     * data filestore -- for a specified sector and target resolution.
     *
     * @param sector           the sector of interest.
     * @param targetResolution the desired horizontal resolution, in radians, of the raster or other elevation sample
     *                         from which elevations are drawn. (To compute radians from a distance, divide the distance
     *                         by the radius of the globe, ensuring that both the distance and the radius are in the
     *                         same units.) Specify null to use this elevation model's best resolution.
     *
     * @return the fraction of the data that is local. A value of 1.0 indicates that all the data is available.
     */
    double getLocalDataAvailability(Sector sector, Double targetResolution);

    /**
     * Returns the elevations corresponding to a grid of locations in a specified sector. The grid is evenly spaced
     * locations in latitude and longitude defined by numLat and numLon.  The buffer is populated with this model's
     * elevation value at each grid point, starting in the sector's lower left corner and proceeding in row major order.
     * The buffer must have length of at least numLat * numLon. If a location is outside the elevation model's coverage
     * area, the output buffer for that location is not modified; it retains the buffer's original value.
     *
     * @param sector           the sector in question.
     * @param numLat           the grid height in number of latitude locations.
     * @param numLon           the grid width in number of longitude locations.
     * @param targetResolution the desired horizontal resolution, in radians, of the raster or other elevation sample
     *                         from which elevations are drawn. (To compute radians from a distance, divide the distance
     *                         by the radius of the globe, ensuring that both the distance and the radius are in the
     *                         same units.)
     * @param buffer           an array in which to place the returned elevations. The array must be pre-allocated and
     *                         contain at least numLat * numLon elements.
     *
     * @return the resolution achieved, in radians, or {@link Double#MAX_VALUE} if individual elevations cannot be
     *         determined for all of the locations.
     *
     * @throws IllegalArgumentException if the sector is null, if either numLat or numLon are less than one, if
     *                                  targetResolution is less than or equal to zero, if the buffer is null, or if the
     *                                  buffer's length is less than numLat * numLon.
     */
    double getElevations(Sector sector, int numLat, int numLon, double targetResolution, double[] buffer);
}
