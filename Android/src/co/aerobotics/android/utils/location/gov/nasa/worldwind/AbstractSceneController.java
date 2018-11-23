/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind;

import android.graphics.*;
import android.opengl.GLES20;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.AVKey;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.cache.GpuResourceCache;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.layers.Layer;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.pick.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.render.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.terrain.SectorGeometryList;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * @author dcollins
 * @version $Id$
 */
public abstract class AbstractSceneController extends WWObjectImpl implements SceneController
{
    protected abstract void doDrawFrame(DrawContext dc);

    protected Model model;
    protected View view;
    protected double verticalExaggeration;
    protected DrawContext dc;
    protected GpuResourceCache gpuResourceCache;
    protected boolean deepPick;
    protected Point pickPoint;
    protected PickedObjectList objectsAtPickPoint;
    protected Collection<PerformanceStatistic> perFrameStatistics = new ArrayList<PerformanceStatistic>();

    protected AbstractSceneController()
    {
        this.setVerticalExaggeration(Configuration.getDoubleValue(AVKey.VERTICAL_EXAGGERATION));
        this.dc = this.createDrawContext();
    }

    protected DrawContext createDrawContext()
    {
        return new DrawContextImpl();
    }

    /** {@inheritDoc} */
    public Model getModel()
    {
        return this.model;
    }

    /** {@inheritDoc} */
    public void setModel(Model model)
    {
        if (this.model != null)
            this.model.removePropertyChangeListener(this);
        if (model != null)
            model.addPropertyChangeListener(this);

        Model oldModel = this.model;
        this.model = model;
        this.firePropertyChange(AVKey.MODEL, oldModel, model);
    }

    /** {@inheritDoc} */
    public View getView()
    {
        return this.view;
    }

    /** {@inheritDoc} */
    public void setView(View view)
    {
        if (this.view != null)
            this.view.removePropertyChangeListener(this);
        if (view != null)
            view.addPropertyChangeListener(this);

        View oldView = this.view;
        this.view = view;

        this.firePropertyChange(AVKey.VIEW, oldView, view);
    }

    /** {@inheritDoc} */
    public double getVerticalExaggeration()
    {
        return this.verticalExaggeration;
    }

    /** {@inheritDoc} */
    public void setVerticalExaggeration(double verticalExaggeration)
    {
        Double oldVE = this.verticalExaggeration;
        this.verticalExaggeration = verticalExaggeration;
        this.firePropertyChange(AVKey.VERTICAL_EXAGGERATION, oldVE, verticalExaggeration);
    }

    /** {@inheritDoc} */
    public GpuResourceCache getGpuResourceCache()
    {
        return this.gpuResourceCache;
    }

    /** {@inheritDoc} */
    public void setGpuResourceCache(GpuResourceCache cache)
    {
        this.gpuResourceCache = cache;
    }

    /** {@inheritDoc} */
    public SectorGeometryList getSurfaceGeometry()
    {
        return this.dc.getSurfaceGeometry();
    }

    /** {@inheritDoc} */
    public boolean isDeepPickEnabled()
    {
        return this.deepPick;
    }

    /** {@inheritDoc} */
    public void setDeepPickEnabled(boolean tf)
    {
        this.deepPick = tf;
    }

    /** {@inheritDoc} */
    public Point getPickPoint()
    {
        return this.pickPoint;
    }

    /** {@inheritDoc} */
    public void setPickPoint(Point pickPoint)
    {
        this.pickPoint = pickPoint;
    }

    /** {@inheritDoc} */
    public PickedObjectList getObjectsAtPickPoint()
    {
        return this.objectsAtPickPoint;
    }

    /** {@inheritDoc} */
    public Collection<PerformanceStatistic> getPerFrameStatistics()
    {
        return this.perFrameStatistics;
    }

    /** {@inheritDoc} */
    public void drawFrame(int viewportWidth, int viewportHeight)
    {
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

        this.perFrameStatistics.clear();
        long beginTime = System.currentTimeMillis();

        // Prepare the drawing context for a new frame then cause this scene controller to draw its content. There is no
        // need to explicitly swap the front and back buffers here, as the owner WorldWindow does this for us. In the
        // case of WorldWindowGLSurfaceView, the GLSurfaceView automatically swaps the front and back buffers for us.
        this.initializeDrawContext(this.dc, viewportWidth, viewportHeight);
        this.doDrawFrame(this.dc);

        long endTime = System.currentTimeMillis();
        this.dc.addPerFrameStatistic(PerformanceStatistic.FRAME_TIME, "Frame Time (ms)", endTime - beginTime);
        this.addPerFrameStatistics(this.dc);
    }

    protected void initializeDrawContext(DrawContext dc, int viewportWidth, int viewportHeight)
    {
        long timeStamp = System.currentTimeMillis();

        dc.initialize(viewportWidth, viewportHeight);
        dc.setModel(this.model);
        dc.setView(this.view);
        dc.setVerticalExaggeration(this.verticalExaggeration);
        dc.setGpuResourceCache(this.gpuResourceCache);
        dc.setPickPoint(this.pickPoint);
        dc.setFrameTimeStamp(timeStamp);
        dc.setPerFrameStatistics(this.perFrameStatistics);
    }

    protected void initializeFrame(DrawContext dc)
    {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA); // Blend in pre-multiplied alpha mode.
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        // We do not specify glCullFace, because the default cull face state GL_BACK is appropriate for our needs.
    }

    protected void finalizeFrame(DrawContext dc)
    {
        // Restore the default GL state values we modified in initializeFrame.
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);
        GLES20.glDepthFunc(GLES20.GL_LESS);
        GLES20.glClearColor(0f, 0f, 0f, 0f);
    }

    protected void clearFrame(DrawContext dc)
    {
        int c = dc.getClearColor();
        GLES20.glClearColor(Color.red(c) / 255f, Color.green(c) / 255f, Color.blue(c) / 255f, Color.alpha(c) / 255f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    protected void applyView(DrawContext dc)
    {
        if (this.view != null)
            this.view.apply(dc);
    }

    protected void createTerrain(DrawContext dc)
    {
        long beginTime = System.currentTimeMillis();
        SectorGeometryList surfaceGeometry = null;

        try
        {
            if (dc.getGlobe() != null)
                surfaceGeometry = dc.getGlobe().tessellate(dc);

            // If there's no surface geometry, just log a warning and keep going. Some layers may have meaning without
            // surface geometry.
            if (surfaceGeometry == null)
                Logging.warning(Logging.getMessage("generic.NoSurfaceGeometry"));
        }
        catch (Exception e)
        {
            Logging.error(Logging.getMessage("SceneController.ExceptionCreatingSurfaceGeometry"), e);
        }

        dc.setSurfaceGeometry(surfaceGeometry);
        dc.setVisibleSector(surfaceGeometry != null ? surfaceGeometry.getSector() : null);

        long endTime = System.currentTimeMillis();
        dc.addPerFrameStatistic(PerformanceStatistic.TERRAIN_FRAME_TIME, "Frame Time (ms): Terrain Creation",
            endTime - beginTime);
        dc.addPerFrameStatistic(PerformanceStatistic.TERRAIN_TILE_COUNT, "Tile Count: Terrain",
            dc.getSurfaceGeometry() != null ? dc.getSurfaceGeometry().size() : 0);
    }

    protected void draw(DrawContext dc)
    {
        this.drawLayers(dc);
    }

    protected void drawLayers(DrawContext dc)
    {
        if (dc.getLayers() == null)
            return;

        for (Layer layer : dc.getLayers())
        {
            try
            {
                if (layer != null)
                {
                    dc.setCurrentLayer(layer);
                    layer.render(dc);
                }
            }
            catch (Exception e)
            {
                String msg = Logging.getMessage("SceneController.ExceptionRenderingLayer",
                    (layer != null ? layer.getName() : Logging.getMessage("term.Unknown")));
                Logging.error(msg, e);
                // Don't abort; continue on to the next layer.
            }
        }

        dc.setCurrentLayer(null);
    }

    protected void pick(DrawContext dc)
    {
        this.beginPicking(dc);
        try
        {
            this.doPick(dc);
        }
        finally
        {
            this.endPicking(dc);
        }
    }

    /**
     * Configures the draw context and GL state for picking. This ensures that pick colors are drawn into the
     * framebuffer as specified, and are not modified by any GL state. This makes the following GL state changes: <ul>
     * <li>Disable blending</li> <li>Disable dithering</li> </ul>
     *
     * @param dc the draw context to configure.
     */
    protected void beginPicking(DrawContext dc)
    {
        dc.setPickingMode(true);
        GLES20.glDisable(GLES20.GL_BLEND); // Blending is disabled by default, but is enabled in initializeFrame.
        GLES20.glDisable(GLES20.GL_DITHER); // Dithering is enabled by default.
    }

    /**
     * Restores the draw context and GL state modified in beginPicking. This makes the following GL state changes: <ul>
     * <li>Enable blending</li> <li>Enable dithering</li> </ul>
     *
     * @param dc the draw context on which to restore state.
     */
    protected void endPicking(DrawContext dc)
    {
        dc.setPickingMode(false);
        GLES20.glEnable(GLES20.GL_BLEND); // Blending is disabled by default, but is enabled in initializeFrame.
        GLES20.glEnable(GLES20.GL_DITHER); // Dithering is enabled by default.
    }

    protected void doPick(DrawContext dc)
    {
        this.doPickTerrain(dc);
        this.doPickNonTerrain(dc);
        this.resolveTopPick(dc);
        this.objectsAtPickPoint = new PickedObjectList(dc.getObjectsAtPickPoint());

        if (this.isDeepPickEnabled() && this.objectsAtPickPoint.hasNonTerrainObjects())
            this.doDeepPick(dc);
    }

    protected void doPickTerrain(DrawContext dc)
    {
        if (dc.getSurfaceGeometry() == null || dc.getPickPoint() == null)
            return;

        long beginTime = System.currentTimeMillis();

        dc.getSurfaceGeometry().pick(dc, dc.getPickPoint());

        long endTime = System.currentTimeMillis();
        dc.addPerFrameStatistic(PerformanceStatistic.TERRAIN_PICK_TIME, "Pick Time (ms): Terrain", endTime - beginTime);
    }

    protected void doPickNonTerrain(DrawContext dc)
    {
        if (dc.getPickPoint() == null) // Don't do the pick if there's no current pick point.
            return;

        this.pickLayers(dc);
    }

    protected void pickLayers(DrawContext dc)
    {
        if (dc.getLayers() == null)
            return;

        for (Layer layer : dc.getLayers())
        {
            try
            {
                if (layer != null)
                {
                    dc.setCurrentLayer(layer);
                    layer.pick(dc, dc.getPickPoint());
                }
            }
            catch (Exception e)
            {
                String msg = Logging.getMessage("SceneController.ExceptionPickingLayer",
                    (layer != null ? layer.getName() : Logging.getMessage("term.Unknown")));
                Logging.error(msg, e);
                // Don't abort; continue on to the next layer.
            }
        }

        dc.setCurrentLayer(null);
    }

    protected void resolveTopPick(DrawContext dc)
    {
        // Make a last reading to find out which is the top (resultant) color.
        PickedObjectList pickedObjects = dc.getObjectsAtPickPoint();
        if (pickedObjects != null && pickedObjects.size() == 1)
        {
            pickedObjects.get(0).setOnTop();
        }
        else if (pickedObjects != null && pickedObjects.size() > 1)
        {
            int colorCode = dc.getPickColor(dc.getPickPoint());
            if (colorCode != 0)
            {
                // Find the picked object in the list and set the "onTop" flag.
                for (PickedObject po : pickedObjects)
                {
                    if (po != null && po.getColorCode() == colorCode)
                    {
                        po.setOnTop();
                        break;
                    }
                }
            }
        }
    }

    protected void doDeepPick(DrawContext dc)
    {
        this.beginDeepPicking(dc);
        try
        {
            this.doPickNonTerrain(dc);
        }
        finally
        {
            this.endDeepPicking(dc);
        }

        PickedObjectList currentPickedObjects = this.objectsAtPickPoint;
        this.objectsAtPickPoint = this.mergePickedObjectLists(currentPickedObjects, dc.getObjectsAtPickPoint());
    }

    /**
     * Configures the draw context and GL state for deep picking. This ensures that an object can be picked regardless
     * its depth relative to other objects. This makes the following GL state changes: <ul> <li>Disable depth test</li>
     * </ul>
     *
     * @param dc the draw context to configure.
     */
    protected void beginDeepPicking(DrawContext dc)
    {
        dc.setDeepPickingEnabled(true);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST); // Depth test is disabled by default, but enabled in initializeFrame.
    }

    /**
     * Restores the draw context and the GL state modified in beginDeepPicking. This makes the following GL state
     * changes: <ul> <li>Enable depth test</li> </ul>
     *
     * @param dc the draw context on which to restore state.
     */
    protected void endDeepPicking(DrawContext dc)
    {
        dc.setDeepPickingEnabled(false);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST); // Depth test is disabled by default, but enabled in initializeFrame.
    }

    protected PickedObjectList mergePickedObjectLists(PickedObjectList listA, PickedObjectList listB)
    {
        if (listA == null || listB == null || !listA.hasNonTerrainObjects() || !listB.hasNonTerrainObjects())
            return listA;

        for (PickedObject pb : listB)
        {
            if (pb.isTerrain())
                continue;

            boolean common = false; // cannot modify listA within its iterator, so use a flag to indicate commonality
            for (PickedObject pa : listA)
            {
                if (pa.isTerrain())
                    continue;

                if (pa.getObject() == pb.getObject())
                {
                    common = true;
                    break;
                }
            }

            if (!common)
                listA.add(pb);
        }

        return listA;
    }

    protected void addPerFrameStatistics(DrawContext dc)
    {
        if (this.gpuResourceCache != null)
        {
            dc.addPerFrameStatistic(PerformanceStatistic.GPU_RESOURCE_CACHE, "Cache Size (Kb): GPU Resources",
                this.gpuResourceCache.getUsedCapacity() / 1000);
        }

        this.perFrameStatistics.addAll(WorldWind.getMemoryCacheSet().getPerformanceStatistics());

        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        dc.addPerFrameStatistic(PerformanceStatistic.JVM_HEAP, "JVM total memory (Kb)", totalMemory / 1000);
        dc.addPerFrameStatistic(PerformanceStatistic.JVM_HEAP_USED, "JVM used memory (Kb)",
            (totalMemory - freeMemory) / 1000);
    }
}
