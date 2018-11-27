/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.render;

import android.opengl.GLES20;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.cache.GpuResourceCache;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.terrain.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * @author dcollins
 * @version $Id$
 */
public class SurfaceTileRenderer
{
    protected static final String VERTEX_SHADER_PATH = "shaders/SurfaceTileRenderer.vert";
    protected static final String FRAGMENT_SHADER_PATH = "shaders/SurfaceTileRenderer.frag";

    protected final Object programKey = new Object();
    protected boolean programCreationFailed;
    protected List<SurfaceTile> intersectingTiles = new ArrayList<SurfaceTile>();
    protected List<SectorGeometry> intersectingGeometry = new ArrayList<SectorGeometry>();
    /**
     * Matrix defining the tile coordinate transform matrix. Maps normalized surface texture coordinates to normalized
     * tile coordinates.
     */
    protected Matrix tileCoordMatrix = Matrix.fromIdentity();
    /**
     * Matrix defining the texture coordinate transform matrix. Maps normalized surface texture coordinates to tile
     * texture coordinates.
     */
    protected Matrix texCoordMatrix = Matrix.fromIdentity();

    public SurfaceTileRenderer()
    {
    }

    public void renderTile(DrawContext dc, SurfaceTile tile)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.error(msg);
            throw new IllegalStateException(msg);
        }

        if (tile == null)
        {
            String msg = Logging.getMessage("nullValue.TileIsNull");
            Logging.error(msg);
            throw new IllegalStateException(msg);
        }

        SectorGeometryList sgList = dc.getSurfaceGeometry();
        if (sgList == null)
        {
            Logging.warning(Logging.getMessage("generic.NoSurfaceGeometry"));
            return;
        }

        GpuProgram program = this.getGpuProgram(dc.getGpuResourceCache());
        if (program == null)
            return; // Message already logged in getGpuProgram.

        this.beginRendering(dc, program);
        sgList.beginRendering(dc);
        try
        {
            if (tile.bind(dc))
            {
                this.intersectingGeometry.clear();
                this.assembleIntersectingGeometry(tile, sgList);

                for (SectorGeometry sg : this.intersectingGeometry)
                {
                    sg.beginRendering(dc);
                    try
                    {
                        this.applyTileState(dc, sg, tile);
                        sg.render(dc);
                    }
                    finally
                    {
                        sg.endRendering(dc);
                    }
                }
            }
        }
        finally
        {
            sgList.endRendering(dc);
            this.endRendering(dc);
        }
    }

    public void renderTiles(DrawContext dc, Iterable<? extends SurfaceTile> tiles)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.error(msg);
            throw new IllegalStateException(msg);
        }

        if (tiles == null)
        {
            String msg = Logging.getMessage("nullValue.TileListIsNull");
            Logging.error(msg);
            throw new IllegalStateException(msg);
        }

        SectorGeometryList sgList = dc.getSurfaceGeometry();
        if (sgList == null)
        {
            Logging.warning(Logging.getMessage("generic.NoSurfaceGeometry"));
            return;
        }

        GpuProgram program = this.getGpuProgram(dc.getGpuResourceCache());
        if (program == null)
            return; // Exception logged in loadGpuProgram.

        this.beginRendering(dc, program);
        sgList.beginRendering(dc);
        try
        {
            for (SectorGeometry sg : sgList)
            {
                this.intersectingTiles.clear();
                this.assembleIntersectingTiles(sg, tiles);
                if (this.intersectingTiles.isEmpty())
                    continue; // Nothing to draw if the tiles don't intersect this surface geometry.

                sg.beginRendering(dc);
                try
                {
                    for (SurfaceTile tile : this.intersectingTiles)
                    {
                        if (tile.bind(dc))
                        {
                            this.applyTileState(dc, sg, tile);
                            sg.render(dc);
                        }
                    }
                }
                finally
                {
                    sg.endRendering(dc);
                }
            }
        }
        finally
        {
            sgList.endRendering(dc);
            this.endRendering(dc);
        }
    }

    protected void beginRendering(DrawContext dc, GpuProgram program)
    {
        // Bind this SurfaceTileRenderer's Gpu program as the current program.
        program.bind();
        dc.setCurrentProgram(program);
        // Specify that the tile textures are bound to texture unit GL_TEXTURE0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        program.loadUniformSampler("tileTexture", 0);
    }

    protected void endRendering(DrawContext dc)
    {
        dc.setCurrentProgram(null);
        // Restore the current program to 0 and the active texture unit to GL_TEXTURE0.
        GLES20.glUseProgram(0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Clear the list of intersecting tiles to avoid keeping references to caller specified tiles.
        this.intersectingTiles.clear();
        this.intersectingGeometry.clear();
    }

    protected void assembleIntersectingTiles(SectorGeometry sg, Iterable<? extends SurfaceTile> tiles)
    {
        Sector sgSector = sg.getSector();

        for (SurfaceTile tile : tiles)
        {
            if (tile != null && tile.getSector().intersectsInterior(sgSector))
                this.intersectingTiles.add(tile);
        }
    }

    protected void assembleIntersectingGeometry(SurfaceTile tile, Iterable<? extends SectorGeometry> sgList)
    {
        Sector tileSector = tile.getSector();

        for (SectorGeometry sg : sgList)
        {
            if (sg != null && sg.getSector().intersects(tileSector))
                this.intersectingGeometry.add(sg);
        }
    }

    protected void applyTileState(DrawContext dc, SectorGeometry sg, SurfaceTile tile)
    {
        GpuProgram program = dc.getCurrentProgram();
        if (program == null)
            return; // Message logged in loadGpuProgram.

        this.computeTileCoordMatrix(sg, tile, this.tileCoordMatrix);
        program.loadUniformMatrix("tileCoordMatrix", this.tileCoordMatrix);

        this.texCoordMatrix.setIdentity();
        tile.applyInternalTransform(dc, this.texCoordMatrix);
        this.texCoordMatrix.multiplyAndSet(this.tileCoordMatrix);
        program.loadUniformMatrix("texCoordMatrix", this.texCoordMatrix);
    }

    protected void computeTileCoordMatrix(SectorGeometry sg, SurfaceTile tile, Matrix result)
    {
        Sector sgSector = sg.getSector();
        double sgDeltaLon = sgSector.getDeltaLonRadians();
        double sgDeltaLat = sgSector.getDeltaLatRadians();

        Sector tileSector = tile.getSector();
        double tileDeltaLon = tileSector.getDeltaLonRadians();
        double tileDeltaLat = tileSector.getDeltaLatRadians();

        double sScale = tileDeltaLon > 0 ? sgDeltaLon / tileDeltaLon : 1;
        double tScale = tileDeltaLon > 0 ? sgDeltaLat / tileDeltaLat : 1;
        double sTrans = -(tileSector.minLongitude.radians - sgSector.minLongitude.radians) / sgDeltaLon;
        double tTrans = -(tileSector.minLatitude.radians - sgSector.minLatitude.radians) / sgDeltaLat;

        result.set(
            sScale, 0, 0, sScale * sTrans,
            0, tScale, 0, tScale * tTrans,
            0, 0, 1, 0,
            0, 0, 0, 1);
    }

    protected GpuProgram getGpuProgram(GpuResourceCache cache)
    {
        if (this.programCreationFailed)
            return null;

        GpuProgram program = cache.getProgram(this.programKey);

        if (program == null)
        {
            try
            {
                GpuProgram.GpuProgramSource source = GpuProgram.readProgramSource(VERTEX_SHADER_PATH,
                    FRAGMENT_SHADER_PATH);
                program = new GpuProgram(source);
                cache.put(this.programKey, program);
            }
            catch (Exception e)
            {
                String msg = Logging.getMessage("GL.ExceptionLoadingProgram", VERTEX_SHADER_PATH, FRAGMENT_SHADER_PATH);
                Logging.error(msg);
                this.programCreationFailed = true;
            }
        }

        return program;
    }
}
