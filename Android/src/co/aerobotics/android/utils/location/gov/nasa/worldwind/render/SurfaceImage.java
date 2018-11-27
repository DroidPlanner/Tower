/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.render;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.AVKey;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.cache.GpuResourceCache;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id$
 */
public class SurfaceImage implements Renderable, SurfaceTile
{
    protected String imagePath;
    protected Sector sector;
    protected boolean textureCreationFailed;

    public SurfaceImage(String imagePath, Sector sector)
    {
        if (WWUtil.isEmpty(imagePath))
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.imagePath = imagePath;
        this.sector = sector;
    }

    public String getImagePath()
    {
        return this.imagePath;
    }

    public Sector getSector()
    {
        return this.sector;
    }

    public boolean bind(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.textureCreationFailed)
            return false; // Message logged in loadGpuTexture.

        GpuTexture texture = this.getGpuTexture(dc);
        if (texture != null)
            texture.bind();

        return texture != null;
    }

    public void applyInternalTransform(DrawContext dc, Matrix matrix)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.textureCreationFailed)
            return; // Message logged in loadGpuTexture.

        GpuTexture texture = this.getGpuTexture(dc);
        if (texture != null)
            texture.applyInternalTransform(dc, matrix);
    }

    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.textureCreationFailed)
            return; // Message logged in loadGpuTexture.

        dc.getSurfaceTileRenderer().renderTile(dc, this);
    }

    protected GpuTexture getGpuTexture(DrawContext dc)
    {
        if (this.textureCreationFailed)
            return null;

        GpuResourceCache cache = dc.getGpuResourceCache();
        GpuTexture texture = cache.getTexture(this.imagePath);

        if (texture == null)
        {
            // TODO: load the texture on a non-rendering thread.
            texture = this.loadGpuTexture(dc);
            if (texture != null) // Don't add the texture to the cache if texture creation failed.
                cache.put(this.imagePath, texture);
        }

        return texture;
    }

    protected GpuTexture loadGpuTexture(DrawContext dc)
    {
        GpuTexture texture = null;

        GpuTextureData textureData = BasicGpuTextureFactory.createTextureData(AVKey.GPU_TEXTURE_FACTORY,
            this.imagePath, null);
        if (textureData != null)
        {
            texture = BasicGpuTextureFactory.createTexture(AVKey.GPU_TEXTURE_FACTORY, dc, textureData, null);
        }

        this.textureCreationFailed = (texture == null);
        return texture;
    }
}
