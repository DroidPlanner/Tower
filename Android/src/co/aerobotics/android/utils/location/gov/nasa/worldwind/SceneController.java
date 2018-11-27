/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind;

import android.graphics.Point;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.cache.GpuResourceCache;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.pick.PickedObjectList;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.terrain.SectorGeometryList;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.PerformanceStatistic;

import java.util.Collection;

/**
 * @author dcollins
 * @version $Id$
 */
public interface SceneController extends WWObject
{
    /**
     * Returns the current model.
     *
     * @return the current model.
     */
    Model getModel();

    /**
     * Specifies the scene controller's model. This method fires an {@link gov.nasa.worldwind.avlist.AVKey#MODEL}
     * property change event.
     *
     * @param model the scene controller's model.
     */
    void setModel(Model model);

    /**
     * Returns the current view.
     *
     * @return the current view.
     */
    View getView();

    /**
     * Sets the current view. This method fires an {@link gov.nasa.worldwind.avlist.AVKey#VIEW} property change event.
     *
     * @param view the view.
     */
    void setView(View view);

    /**
     * Indicates the current vertical exaggeration.
     *
     * @return the current vertical exaggeration.
     */
    double getVerticalExaggeration();

    /**
     * Specifies the exaggeration to apply to elevation values of terrain and other displayed items.
     *
     * @param verticalExaggeration the vertical exaggeration to apply.
     */
    void setVerticalExaggeration(double verticalExaggeration);

    /**
     * Returns this scene controller's GPU Resource cache.
     *
     * @return this scene controller's GPU Resource cache.
     */
    GpuResourceCache getGpuResourceCache();

    /**
     * Specifies the GPU Resource cache to use.
     *
     * @param cache the texture cache.
     */
    void setGpuResourceCache(GpuResourceCache cache);

    /**
     * Returns the surface geometry used to draw the most recent frame. The geometry spans only the area most recently
     * visible.
     *
     * @return the surface geometry used to draw the most recent frame. May be null.
     */
    SectorGeometryList getSurfaceGeometry();

    /**
     * Indicates whether all items under the cursor are identified during picking and within {@link
     * gov.nasa.worldwind.event.SelectEvent}s.
     *
     * @return true if all items under the cursor are identified during picking, otherwise false.
     */
    boolean isDeepPickEnabled();

    /**
     * Specifies whether all items under the cursor are identified during picking and within {@link
     * gov.nasa.worldwind.event.SelectEvent}s.
     *
     * @param tf true to identify all items under the cursor during picking, otherwise false.
     */
    void setDeepPickEnabled(boolean tf);

    /**
     * Returns the current pick point.
     *
     * @return the current pick point, or null if no pick point is current.
     */
    Point getPickPoint();

    /**
     * Specifies the current pick point.
     *
     * @param pickPoint the current pick point, or null.
     */
    void setPickPoint(Point pickPoint);

    /**
     * Returns the World Wind objects at the current pick point. The list of objects at the pick point is determined
     * during each call to drawFrame. This method returns the list of objects determined from the most recent call to
     * drawFrame.
     *
     * @return the list of currently picked objects, or null if no objects are currently picked.
     */
    PickedObjectList getObjectsAtPickPoint();

    /**
     * Returns the performance statistics computed during the most recent frame.
     *
     * @return the most recent frame's performance statistics.
     */
    Collection<PerformanceStatistic> getPerFrameStatistics();

    /**
     * Cause the window to regenerate the frame, including pick resolution.
     *
     * @param viewportWidth  the width of the current viewport this scene controller is associated with, in pixels. Must
     *                       not be less than zero.
     * @param viewportHeight the height of the current viewport this scene controller is associated with, in pixels.
     *                       Must not be less than zero.
     *
     * @throws IllegalArgumentException if either viewportWidth or viewportHeight are less than zero.
     */
    void drawFrame(int viewportWidth, int viewportHeight);
}
