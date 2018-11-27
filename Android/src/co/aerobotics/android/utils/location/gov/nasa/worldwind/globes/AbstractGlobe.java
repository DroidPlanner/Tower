/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.globes;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.AVKey;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.render.DrawContext;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.terrain.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

import java.beans.PropertyChangeEvent;
import java.util.List;

/**
 * @author dcollins
 * @version $Id$
 */
public abstract class AbstractGlobe extends WWObjectImpl implements Globe
{
    protected ElevationModel elevationModel;
    protected Tessellator tessellator;
    protected Sector fullSphere = Sector.fromFullSphere();

    protected AbstractGlobe(ElevationModel elevationModel)
    {
        this.elevationModel = elevationModel;
        this.tessellator = this.createTessellator();
    }

    protected AbstractGlobe(ElevationModel elevationModel, Tessellator tessellator)
    {
        this.elevationModel = elevationModel;
        this.tessellator = tessellator;
    }

    /**
     * Overridden to forward messages to this globe's tessellator as well as its message listeners.
     *
     * @param event the message to forward.
     */
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);

        if (this.tessellator != null && this.elevationModel == event.getSource())
            this.tessellator.propertyChange(event);
    }

    /** {@inheritDoc} */
    public ElevationModel getElevationModel()
    {
        return this.elevationModel;
    }

    /** {@inheritDoc} */
    public void setElevationModel(ElevationModel elevationModel)
    {
        if (this.elevationModel != null)
            this.elevationModel.removePropertyChangeListener(this);
        if (elevationModel != null)
            elevationModel.addPropertyChangeListener(this);

        this.elevationModel = elevationModel;
    }

    /** {@inheritDoc} */
    public Tessellator getTessellator()
    {
        return this.tessellator;
    }

    /** {@inheritDoc} */
    public void setTessellator(Tessellator tessellator)
    {
        this.tessellator = tessellator;
    }

    /** {@inheritDoc} */
    public SectorGeometryList tessellate(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.tessellator == null)
        {
            Logging.warning(Logging.getMessage("Globe.TessellatorUnavailable"));
            return null;
        }

        return this.tessellator.tessellate(dc);
    }

    protected Tessellator createTessellator()
    {
        String configFile = Configuration.getStringValue(AVKey.TESSELLATOR_CONFIG_FILE);
        return (Tessellator) BasicFactory.create(AVKey.TESSELLATOR_FACTORY, configFile);
    }

    /** {@inheritDoc} */
    public double getBestResolution(Sector sector)
    {
        // Return Double.MAX_VALUE as the best attainable resolution if the elevation model is null. In this case we
        // must return a value indicating that the globe's elevation model has no best attainable resolution.
        return (this.elevationModel != null) ? this.elevationModel.getBestResolution(sector) : Double.MAX_VALUE;
    }

    /** {@inheritDoc} */
    public double getMinElevation()
    {
        // Return 0 as this globe's min elevation if the elevation model is null or does not cover the entire globe. If
        // the elevation model does not cover the globe, we must return 0 to ensure that areas without elevations are
        // treated correctly.
        return (this.elevationModel != null && this.elevationModel.intersects(this.fullSphere) == 0)
            ? this.elevationModel.getMinElevation() : 0;
    }

    /** {@inheritDoc} */
    public double getMaxElevation()
    {
        // Return 0 as this globe's max elevation if the elevation model is null or does not cover the entire globe. If
        // the elevation model does not cover the globe, we must return 0 to ensure that areas without elevations are
        // treated correctly.
        return (this.elevationModel != null && this.elevationModel.intersects(this.fullSphere) == 0)
            ? this.elevationModel.getMaxElevation() : 0;
    }

    /** {@inheritDoc} */
    public double[] getMinAndMaxElevations(Sector sector)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Return 0 as this globe's min and max elevation if the elevation model is null or does not intersect the
        // specified sector. If the elevation model does intersect the sector, we must return 0 to ensure that this
        // sector is consistently reported as having elevation 0.
        return (this.elevationModel != null && this.elevationModel.intersects(sector) != -1)
            ? this.elevationModel.getExtremeElevations(sector) : new double[] {0, 0};
    }

    /** {@inheritDoc} */
    public double getElevation(LatLon location)
    {
        if (location == null)
        {
            String msg = Logging.getMessage("nullValue.LocationIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Return 0 as this globe's elevation at the specified location if the elevation model is null or does not
        // contain the specified location. If the elevation model does contain the location, we must return 0 to ensure
        // that this location is consistently reported as having elevation 0.
        return (this.elevationModel != null && this.elevationModel.contains(location.latitude, location.longitude))
            ? this.elevationModel.getElevation(location.latitude, location.longitude) : 0;
    }

    /** {@inheritDoc} */
    public double getElevation(Angle latitude, Angle longitude)
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

        // Return 0 as this globe's elevation at the specified location if the elevation model is null or does not
        // contain the specified location. If the elevation model does contain the location, we must return 0 to ensure
        // that this location is consistently reported as having elevation 0.
        return (this.elevationModel != null && this.elevationModel.contains(latitude, longitude))
            ? this.elevationModel.getElevation(latitude, longitude) : 0;
    }

    /** {@inheritDoc} */
    public double getElevations(Sector sector, List<? extends LatLon> locations, double targetResolution,
        double[] buffer)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (locations == null)
        {
            String msg = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (targetResolution <= 0)
        {
            String msg = Logging.getMessage("generic.ResolutionIsInvalid", targetResolution);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (buffer == null)
        {
            String msg = Logging.getMessage("nullValue.ElevationsBufferIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (buffer.length < locations.size())
        {
            String msg = Logging.getMessage("generic.ElevationsBufferInvalidLength", buffer.length);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Return 0 as this globe's elevation at the specified location if the elevation model is null or does not
        // intersect the specified sector. If the elevation model does intersect the sector, we must return 0 to ensure
        // that this sector is consistently reported as having elevation 0.
        return (this.elevationModel != null && this.elevationModel.intersects(sector) != -1)
            ? this.elevationModel.getElevations(sector, locations, targetResolution, buffer) : 0;
    }

    /** {@inheritDoc} */
    public double getElevations(Sector sector, int numLat, int numLon, double targetResolution, double[] buffer)
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

        if (targetResolution <= 0)
        {
            String msg = Logging.getMessage("generic.ResolutionIsInvalid", targetResolution);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (buffer == null)
        {
            String msg = Logging.getMessage("nullValue.ElevationsBufferIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (buffer.length < numLat * numLon)
        {
            String msg = Logging.getMessage("generic.ElevationsBufferInvalidLength", buffer.length);
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Return 0 as this globe's elevation at the specified location if the elevation model is null or does not
        // intersect the specified sector. If the elevation model does intersect the sector, we must return 0 to ensure
        // that this sector is consistently reported as having elevation 0.
        return (this.elevationModel != null && this.elevationModel.intersects(sector) != -1)
            ? this.elevationModel.getElevations(sector, numLat, numLon, targetResolution, buffer) : 0;
    }
}
