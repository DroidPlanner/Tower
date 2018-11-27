/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.render;

import android.graphics.Point;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.cache.GpuResourceCache;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.Sector;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.globes.Globe;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.layers.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.pick.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.terrain.SectorGeometryList;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.PerformanceStatistic;

import java.util.Collection;

/**
 * @author dcollins
 * @version $Id$
 */
public interface DrawContext extends WWObject
{
    /**
     * Initializes this <code>DrawContext</code>. This method should be called at the beginning of each frame to prepare
     * the <code>DrawContext</code> for the coming render pass.
     */
    void initialize(int viewportWidth, int viewportHeight);

    int getViewportWidth();

    int getViewportHeight();

    /**
     * Returns the WorldWindow's background color.
     *
     * @return the WorldWindow's background color.
     */
    int getClearColor();

    /**
     * Retrieves the current <code>Model</code>, which may be null.
     *
     * @return the current <code>Model</code>, which may be null
     */
    Model getModel();

    /**
     * Assign a new <code>Model</code>. Some layers cannot function properly with a null <code>Model</code>. It is
     * recommended that the <code>Model</code> is never set to null during a normal render pass.
     *
     * @param model the new <code>Model</code>
     */
    void setModel(Model model);

    /**
     * Retrieves the current <code>View</code>, which may be null.
     *
     * @return the current <code>View</code>, which may be null
     */
    View getView();

    /**
     * Assigns a new <code>View</code>. Some layers cannot function properly with a null <code>View</code>. It is
     * recommended that the <code>View</code> is never set to null during a normal render pass.
     *
     * @param view the enw <code>View</code>
     */
    void setView(View view);

    /**
     * Retrieves the current <code>Globe</code>, which may be null.
     *
     * @return the current <code>Globe</code>, which may be null
     */
    Globe getGlobe();

    /**
     * Retrieves a list containing all the current layers. No guarantee is made about the order of the layers.
     *
     * @return a <code>LayerList</code> containing all the current layers
     */
    LayerList getLayers();

    /**
     * Retrieves the current vertical exaggeration. Vertical exaggeration affects the appearance of areas with varied
     * elevation. A vertical exaggeration of zero creates a surface which exactly fits the shape of the underlying
     * <code>Globe</code>. A vertical exaggeration of 3 will create mountains and valleys which are three times as
     * high/deep as they really are.
     *
     * @return the current vertical exaggeration
     */
    double getVerticalExaggeration();

    /**
     * Sets the vertical exaggeration. Vertical exaggeration affects the appearance of areas with varied elevation. A
     * vertical exaggeration of zero creates a surface which exactly fits the shape of the underlying
     * <code>Globe</code>. A vertical exaggeration of 3 will create mountains and valleys which are three times as
     * high/deep as they really are.
     *
     * @param verticalExaggeration the new vertical exaggeration.
     */
    void setVerticalExaggeration(double verticalExaggeration);

    /**
     * Retrieves a <code>Sector</code> which is at least as large as the current visible sector. The value returned is
     * the value passed to <code>SetVisibleSector</code>. This method may return null.
     *
     * @return a <code>Sector</code> at least the size of the current visible sector, null if unavailable
     */
    Sector getVisibleSector();

    /**
     * Sets the visible <code>Sector</code>. The new visible sector must completely encompass the Sector which is
     * visible on the display.
     *
     * @param sector the new visible <code>Sector</code>
     */
    void setVisibleSector(Sector sector);

    /**
     * Returns the GPU resource cache used by this draw context.
     *
     * @return the GPU resource cache used by this draw context.
     */
    GpuResourceCache getGpuResourceCache();

    /**
     * Specifies the GPU resource cache for this draw context.
     *
     * @param gpuResourceCache the GPU resource cache for this draw context.
     */
    void setGpuResourceCache(GpuResourceCache gpuResourceCache);

    /**
     * Indicates the surface geometry that is visible this frame.
     *
     * @return the visible surface geometry.
     */
    SectorGeometryList getSurfaceGeometry();

    /**
     * Specifies the surface geometry that is visible this frame.
     *
     * @param surfaceGeometry the visible surface geometry.
     */
    void setSurfaceGeometry(SectorGeometryList surfaceGeometry);

    SurfaceTileRenderer getSurfaceTileRenderer();

    /**
     * Returns the current-layer. The field is informative only and enables layer contents to determine their containing
     * layer.
     *
     * @return the current layer, or null if no layer is current.
     */
    Layer getCurrentLayer();

    /**
     * Sets the current-layer field to the specified layer or null. The field is informative only and enables layer
     * contents to determine their containing layer.
     *
     * @param layer the current layer or null.
     */
    void setCurrentLayer(Layer layer);

    GpuProgram getCurrentProgram();

    void setCurrentProgram(GpuProgram program);

    /**
     * Returns the time stamp corresponding to the beginning of a pre-render, pick, render sequence. The stamp remains
     * constant across these three operations so that called objects may avoid recomputing the same values during each
     * of the calls in the sequence.
     *
     * @return the frame time stamp. See {@link System#currentTimeMillis()} for its numerical meaning.
     */
    long getFrameTimeStamp();

    /**
     * Specifies the time stamp corresponding to the beginning of a pre-render, pick, render sequence. The stamp must
     * remain constant across these three operations so that called objects may avoid recomputing the same values during
     * each of the calls in the sequence.
     *
     * @param timeStamp the frame time stamp. See {@link System#currentTimeMillis()} for its numerical meaning.
     */
    void setFrameTimeStamp(long timeStamp);

    /**
     * Indicates whether the drawing is occurring in picking picking mode. In picking mode, each unique object is drawn
     * in a unique RGB color by calling {@link #getUniquePickColor()} prior to rendering. Any OpenGL state that could
     * cause an object to draw a color other than the unique RGB pick color must be disabled. This includes
     * antialiasing, blending, and dithering.
     *
     * @return true if drawing should occur in picking mode, otherwise false.
     */
    boolean isPickingMode();

    /**
     * Specifies whether drawing should occur in picking mode. See {@link #isPickingMode()} for more information.
     *
     * @param tf true to specify that drawing should occur in picking mode, otherwise false.
     */
    void setPickingMode(boolean tf);

    /**
     * Indicates whether all items under the pick point are picked.
     *
     * @return true if all items under the pick point are picked, otherwise false .
     */
    boolean isDeepPickingEnabled();

    /**
     * Specifies whether all items under the pick point are picked.
     *
     * @param tf true to pick all objects under the pick point.
     */
    void setDeepPickingEnabled(boolean tf);

    /**
     * Returns a unique color to serve as a pick identifier during picking.
     *
     * @return a unique pick color.
     */
    int getUniquePickColor();

    int getPickColor(Point point);

    /**
     * Returns the current pick point.
     *
     * @return the current pick point, or null if no pick point is available.
     */
    Point getPickPoint();

    /**
     * Specifies the pick point.
     *
     * @param point the pick point, or null to indicate there is no pick point.
     */
    void setPickPoint(Point point);

    /**
     * Returns the World Wind objects at the current pick point. The list of objects is determined while drawing in
     * picking mode, and is cleared each time this draw context is initialized.
     *
     * @return the list of currently picked objects.
     */
    PickedObjectList getObjectsAtPickPoint();

    /**
     * Adds a single picked object to the current picked-object list.
     *
     * @param pickedObject the object to add.
     *
     * @throws IllegalArgumentException if the pickedObject is null.
     */
    void addPickedObject(PickedObject pickedObject);

    Collection<PerformanceStatistic> getPerFrameStatistics();

    void setPerFrameStatistics(Collection<PerformanceStatistic> perFrameStatistics);

    void addPerFrameStatistic(String key, String displayName, Object value);
}
