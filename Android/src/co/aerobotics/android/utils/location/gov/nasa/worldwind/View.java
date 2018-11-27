/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind;

import android.graphics.Point;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.globes.Globe;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.render.DrawContext;

/**
 * @author dcollins
 * @version $Id$
 */
public interface View extends WWObject
{
    /**
     * Returns the modelview matrix. The modelview matrix transforms model coordinates to eye coordinates. This matrix
     * is computed during the most recent call to <code>apply</code>, using the model space translation and orientation
     * specific to the implementation.
     *
     * @return the current modelview matrix.
     */
    Matrix getModelviewMatrix();

    /**
     * Gets the projection matrix. The projection matrix transforms eye coordinates to clip coordinates. This matrix is
     * computed during the most recent call to <code>apply</code>, using the projection parameters specific to the
     * implementation. The method {@link #getFrustum} returns the geometry corresponding to this matrix.
     *
     * @return the current projection matrix.
     */
    Matrix getProjectionMatrix();

    /**
     * Returns the combined modelview-projection matrix. This matrix is computed during the most recent call to
     * <code>apply</code>, by multiplying the modelview and projection matrices. The returned matrix is equivalent to
     * <code>Matrix.fromIdentity().multiplyAndSet(view.getProjectionMatrix(), view.getModelviewMatrix()</code>, but is
     * provided by View as a convenience to avoid repeatedly computing this combined matrix.
     *
     * @return the current model-view matrix.
     */

    Matrix getModelviewProjectionMatrix();

    /**
     * Returns the rectangle of to the screen coordinate viewport (x, y, width, height) that this View corresponds to.
     * The viewport rectangle transforms clip coordinates to screen coordinates. This rectange is computed during the
     * most recent call to <code>apply</code> based on the current draw context's viewport.
     *
     * @return the viewport rectangle.
     */
    Rect getViewport();

    /**
     * Returns the viewing <code>Frustum</code> in eye coordinates. The <code>Frustum</code> is the portion of viewable
     * space defined by three sets of parallel 'clipping' planes. This value is computed during the most recent call to
     * <code>apply</code>.
     *
     * @return viewing Frustum in eye coordinates.
     */
    Frustum getFrustum();

    /**
     * Returns the viewing <code>Frustum</code> in model coordinates. Model coordinate frustums are useful for
     * performing visibility tests against world geometry. This frustum has the same shape as the frustum returned in
     * <code>getFrustum</code>, but it has been transformed into model coordinates. This value is computed during the
     * most recent call to <code>apply</code>.
     *
     * @return viewing Frustum in model coordinates.
     */
    Frustum getFrustumInModelCoordinates();

    /**
     * Returns the horizontal field-of-view angle (the angle of visibility), or null if the implementation does not
     * support a field-of-view.
     *
     * @return Angle of the horizontal field-of-view, or null if none exists.
     */
    Angle getFieldOfView();

    /**
     * Sets the horizontal field-of-view angle (the angle of visibility) to the specified <code>fieldOfView</code>. This
     * may be ignored if the implementation that do not support a field-of-view.
     *
     * @param fieldOfView the horizontal field-of-view angle.
     *
     * @throws IllegalArgumentException If the implementation supports field-of-view, and <code>fieldOfView</code> is
     *                                  null.
     */
    void setFieldOfView(Angle fieldOfView);

    /**
     * Returns the near clipping plane distance, in eye coordinates.  Implementations of the <code>View</code> interface
     * are not required to have a method for setting the near and far distance.
     *
     * @return near clipping plane distance, in eye coordinates.
     */
    double getNearClipDistance();

    /**
     * Returns the far clipping plane distance, in eye coordinates. Implementations of the <code>View</code> interface
     * are not required to have a method for setting the near and far distance.
     *
     * @return far clipping plane distance, in eye coordinates.
     */
    double getFarClipDistance();

    /**
     * Gets the current eye point in world coordinates.
     *
     * @return the current eye point
     */
    Vec4 getEyePoint();

    /**
     * Returns the current geographic coordinates of this view's eye position, corresponding to this view's most recent
     * state.
     *
     * @param globe the globe for which to compute the current eye position.
     *
     * @return the position of the eye corresponding to the most recent state of this view
     */
    Position getEyePosition(Globe globe);

    /**
     * Transforms the model coordinate point to screen coordinates, using this view's transform state computed the most
     * recent call to <code>apply</code>. The result's x and y values represent the point's screen coordinates relative
     * to the lower left corner. The result's z value represents the point's depth as a value in the range [0, 1].
     *
     * @param modelPoint the model coordinate point to project.
     * @param result     contains the point transformed to screen coordinates after this method returns. This value is
     *                   not modified if this returns <code>false</code>.
     *
     * @return <code>true</code> if the model point is successfully transformed, and <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException if either the model point or the result are <code>null</code>.
     */
    boolean project(Vec4 modelPoint, Vec4 result);

    /**
     * Transforms the screen coordinate point to model coordinates, using this view's transform state computed the most
     * recent call to <code>apply</code>. The screen point's x and y values represent its coordinates relative to the
     * lower left corner. The screen point's z value represents its depth as a value in the range [0, 1].
     *
     * @param screenPoint the screen coordinate point to project.
     * @param result      contains the point transformed to model coordinates after this method returns. This value is
     *                    not modified if this returns <code>false</code>.
     *
     * @return <code>true</code> if the screen point is successfully transformed, and <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException if either the screen point or the result are <code>null</code>.
     */
    boolean unProject(Vec4 screenPoint, Vec4 result);

    /**
     * Computes a line in model coordinates that originates from the eye point and passes through the screen point.
     *
     * @param point  the screen point to compute a ray for.
     * @param result contains the line in model coordinates after this method returns. This value is not modified if
     *               this returns <code>false</code>.
     *
     * @return <code>true</code> if a ray is successfully computed, and <code>false</code> otherwise.
     */
    boolean computeRayFromScreenPoint(Point point, Line result);

    /**
     * Computes the intersection of the specified globe with the line that originates from the eye point and passes
     * through the screen point. Only the globe's ellipsoid is considered; terrain elevations are not incorporated. This
     * view's transform state computed the most recent call to <code>apply</code> is incorporated into the computed
     * position.
     *
     * @param point  the screen point for which to compute the geographic position.
     * @param globe  the globe to d
     * @param result contains the screen point's geographic position after this method returns. This value is not
     *               modified if this returns <code>false</code>.
     *
     * @return <code>true</code> if the screen point corresponds to a position on the globe's ellipsoid, and
     *         <code>false</code> otherwise.
     */
    boolean computePositionFromScreenPoint(Point point, Globe globe, Position result);

    void apply(DrawContext dc);
}
