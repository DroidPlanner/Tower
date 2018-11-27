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
import co.aerobotics.android.utils.location.gov.nasa.worldwind.util.*;

/**
 * @author ccrick
 * @version $Id$
 */
public class BasicView extends WWObjectImpl implements View
{
    // TODO: add documentation to all public methods
    // TODO: currently, allowed tilt values (both lookAt and eye) are all positive.
    //          Does this conform to expectations (KML? positive CW? range?)
    // TODO: add setLookFromLookAt(globe, eyePos, lookAtPos) method;

    // TODO: make configurable
    protected static final double MINIMUM_NEAR_DISTANCE = 2;
    protected static final double MINIMUM_FAR_DISTANCE = 100;

    // View representation
    protected Matrix modelview = Matrix.fromIdentity();
    protected Matrix modelviewInv = Matrix.fromIdentity();
    protected Matrix modelviewTranspose = Matrix.fromIdentity();
    protected Matrix projection = Matrix.fromIdentity();
    protected Matrix modelviewProjection = Matrix.fromIdentity();
    protected Rect viewport = new Rect();

    protected Frustum frustum = new Frustum();
    protected Frustum frustumInModelCoords = new Frustum();
    /** The field of view in degrees. */
    protected Angle fieldOfView = Angle.fromDegrees(45);
    protected double nearClipDistance = MINIMUM_NEAR_DISTANCE;
    protected double farClipDistance = MINIMUM_FAR_DISTANCE;

    protected Vec4 unitX = new Vec4(1, 0, 0);
    protected Vec4 unitY = new Vec4(0, 1, 0);
    protected Vec4 unitZ = new Vec4(0, 0, 1);
    protected Vec4 unitW = new Vec4(0, 0, 0);

    public BasicView()
    {

    }

    /** {@inheritDoc} */
    public Matrix getModelviewMatrix()
    {
        return this.modelview;
    }

    /** {@inheritDoc} */
    public Matrix getProjectionMatrix()
    {
        return this.projection;
    }

    /** {@inheritDoc} */
    public Matrix getModelviewProjectionMatrix()
    {
        return this.modelviewProjection;
    }

    /** {@inheritDoc} */
    public Rect getViewport()
    {
        return this.viewport;
    }

    /** {@inheritDoc} */
    public Frustum getFrustum()
    {
        return this.frustum;
    }

    /** {@inheritDoc} */
    public Frustum getFrustumInModelCoordinates()
    {
        return this.frustumInModelCoords;
    }

    /** {@inheritDoc} */
    public Angle getFieldOfView()
    {
        return this.fieldOfView;
    }

    /** {@inheritDoc} */
    public void setFieldOfView(Angle fieldOfView)
    {
        if (fieldOfView == null)
        {
            String msg = Logging.getMessage("nullValue.FieldOfViewIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        this.fieldOfView = fieldOfView;
    }

    /** {@inheritDoc} */
    public double getNearClipDistance()
    {
        return this.nearClipDistance;
    }

    /** {@inheritDoc} */
    public double getFarClipDistance()
    {
        return this.farClipDistance;
    }

    /** {@inheritDoc} */
    public Vec4 getEyePoint()
    {
        return new Vec4().transformBy4(this.modelviewInv);
    }

    /** {@inheritDoc} */
    public Position getEyePosition(Globe globe)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return computeEyePositionFromModelview(globe);
    }

    /** {@inheritDoc} */
    public boolean project(Vec4 modelPoint, Vec4 result)
    {
        if (modelPoint == null)
        {
            String msg = Logging.getMessage("nullValue.ModelPointIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return WWMath.project(modelPoint.x, modelPoint.y, modelPoint.z, this.modelviewProjection, this.viewport,
            result);
    }

    /** {@inheritDoc} */
    public boolean unProject(Vec4 screenPoint, Vec4 result)
    {
        if (screenPoint == null)
        {
            String msg = Logging.getMessage("nullValue.ScreenPointIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return WWMath.unProject(screenPoint.z, screenPoint.y, screenPoint.z, this.modelviewProjection, this.viewport,
            result);
    }

    /** {@inheritDoc} */
    public boolean computeRayFromScreenPoint(Point point, Line result)
    {
        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ResultIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        double yInGLCoords = this.viewport.y + (this.viewport.height - point.y);
        return WWMath.computeRayFromScreenPoint(point.x, yInGLCoords, this.modelview, this.projection, this.viewport,
            result);
    }

    /** {@inheritDoc} */
    public boolean computePositionFromScreenPoint(Point point, Globe globe, Position result)
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

        Line line = new Line();

        //noinspection SimplifiableIfStatement
        if (!this.computeRayFromScreenPoint(point, line))
            return false;

        return globe.getIntersectionPosition(line, result);
    }

    /** {@inheritDoc} */
    public void apply(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (dc.getGlobe() == null)
        {
            String msg = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute the current viewport rectangle.
        this.viewport.set(0, 0, dc.getViewportWidth(), dc.getViewportHeight());

        // Compute the current clip plane distances. This must be done before computing the projection matrix or the
        // frustum. Both of these properties depend on the near and far clip distances.
        this.nearClipDistance = this.computeNearClipDistance(dc);
        this.farClipDistance = this.computeFarClipDistance(dc);

        // Compute the current projection matrix.
        this.projection.setPerspective(this.fieldOfView, dc.getViewportWidth(), dc.getViewportHeight(),
            this.nearClipDistance, this.farClipDistance);
        this.modelviewProjection.multiplyAndSet(this.projection, this.modelview);

        // Compute the current frustum.
        this.frustum.setPerspective(this.fieldOfView, dc.getViewportWidth(), dc.getViewportHeight(),
            this.nearClipDistance, this.farClipDistance);
        this.frustumInModelCoords.transformBy(this.frustum, this.modelviewTranspose);
    }

    /**
     * Gets the geographic position of the current lookAt point on the globe.
     *
     * @param globe the current globe
     *
     * @return the position of the LookAt
     *
     * @throws IllegalArgumentException if <code>globe</code> is null.
     */
    public Position getLookAtPosition(Globe globe)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 front = new Vec4(-this.modelview.m[8], -this.modelview.m[9], -this.modelview.m[10]);
        Vec4 eyePoint = getEyePoint();
        Line viewRay = new Line(eyePoint, front);

        // Compute the forward vector's intersection with the specified globe, and return the intersection position.
        // Return null if the forward vector does not intersect the globe.
        Position result = new Position();
        return globe.getIntersectionPosition(viewRay, result) ? result : null;
    }

    public double getZoom()
    {
        return this.modelview.m[11];
    }

    /**
     * Computes the distance between the view "eye" and the current lookAt point on the globe.
     *
     * @param globe the current globe
     *
     * @return the distance from the view "eye" to the current lookAt
     *
     * @throws IllegalArgumentException if <code>globe</code> is null.
     */
    public double getLookAtDistance(Globe globe)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 eye = getEyePoint();
        Position lookAt = getLookAtPosition(globe);
        if (lookAt == null)
            return -1;      // lookAt ray does not intersect globe

        return eye.distanceTo3(globe.computePointFromPosition(lookAt));
    }

    /**
     * Gets the current heading of the view "eye," as measured by clockwise rotation from North.
     *
     * @param globe the current globe
     *
     * @return the heading of the eye
     *
     * @throws IllegalArgumentException if <code>globe</code> is null.
     */
    public Angle getEyeHeading(Globe globe)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // TODO: what to do at the poles?

        Vec4 front = new Vec4(-this.modelview.m[8], -this.modelview.m[9], -this.modelview.m[10]);

        // get north pointing vector at eye position (not lookAt)
        Vec4 northTangent = globe.computeNorthPointingTangentAtLocation(getEyePosition(globe));
        Vec4 right = front.cross3(northTangent);
        Vec4 north = right.cross3(front);

        // take angle between up vector and north
        Vec4 up = new Vec4(this.modelview.m[4], this.modelview.m[5], this.modelview.m[6]);
        Angle delta = up.angleBetween3(north);

        // determine if delta is positive or negative
        if (up.cross3(north).dot3(front) > 0)
            delta.multiplyAndSet(-1);

        return delta.multiplyAndSet(-1);    // negate for positive CW heading rotations
    }

    /**
     * Gets the heading rotation around the current lookAt point on the globe, as measured in clockwise rotation from
     * North.
     *
     * @param globe the current globe
     *
     * @return the heading rotation around the LookAt
     *
     * @throws IllegalArgumentException if <code>globe</code> is null.
     */
    public Angle getLookAtHeading(Globe globe)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // TODO: what to do when look at at the poles?
        Vec4 right = new Vec4(this.modelview.m[0], this.modelview.m[1], this.modelview.m[2]);

        Position lookAtPosition = getLookAtPosition(globe);
        if (lookAtPosition == null)
            return null;

        // get north pointing vector at lookAt position (not eye position)
        Vec4 northTangent = globe.computeNorthPointingTangentAtLocation(lookAtPosition);
        Vec4 normal = globe.computeSurfaceNormalAtLocation(lookAtPosition.latitude, lookAtPosition.longitude);
        Vec4 forward = normal.cross3(right).normalize3();

        Angle delta = northTangent.angleBetween3(forward);
        // determine if newDelta is positive or negative
        if (forward.cross3(northTangent).dot3(normal) <= 0)
            delta.multiplyAndSet(-1);

        return delta.multiplyAndSet(-1);   // negate for positive CW heading rotations
    }

    /**
     * Gets the tilt of the view "eye," as found by rotating the eye itself.  The tilt value may be between zero and 90
     * degrees, with zero indicating that the eye looks directly down at the globe.
     *
     * @param globe the current globe
     *
     * @return the tilt of the eye
     *
     * @throws IllegalArgumentException if <code>globe</code> is null.
     */
    public Angle getEyeTilt(Globe globe)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Position eyePosition = getEyePosition(globe);
        Vec4 back = new Vec4(this.modelview.m[8], this.modelview.m[9], this.modelview.m[10]);
        Vec4 normal = new Vec4();

        globe.computeSurfaceNormalAtLocation(eyePosition.latitude, eyePosition.longitude, normal);
        return normal.angleBetween3(back);
    }

    /**
     * Gets the current tilt around the lookAt point.  The tilt value may be between zero and 90 degrees, with zero
     * indicating that the eye looks directly down at the lookAt, and 90 meaning that the eye looks toward the horizon
     * at the lookAt point.
     *
     * @param globe the current globe
     *
     * @return tilt around the lookAt point
     *
     * @throws IllegalArgumentException if <code>globe</code> is null.
     */
    public Angle getLookAtTilt(Globe globe)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Position lookAt = getLookAtPosition(globe);
        if (lookAt == null)
            return null;   // ray did not intersect the globe, so cannot pivot around LookAt

        Vec4 back = new Vec4(this.modelview.m[8], this.modelview.m[9], this.modelview.m[10]);

        Vec4 normal = new Vec4();
        globe.computeSurfaceNormalAtLocation(lookAt.latitude, lookAt.longitude, normal);

        Angle delta = normal.angleBetween3(back);

        // determine if delta is positive or negative
        Vec4 right = new Vec4(this.modelview.m[0], this.modelview.m[1], this.modelview.m[2]);
        if (normal.cross3(back).dot3(right) < 0)
            delta.multiplyAndSet(-1);

        return delta;
    }

    /**
     * Sets the geographic position of the view "eye."
     *
     * @param eyePos  the eye position.
     * @param heading the heading of the eye, positive clockwise from North
     * @param tilt    the tilt of the eye (0 to 90 degrees) from looking straight down at the globe
     * @param globe   the current globe
     *
     * @throws IllegalArgumentException If <code>eyePosition</code>, <code>heading</code>, <code>tilt</tilt> or
     *                                  <code>globe</code> is null.
     */
    public void setEyePosition(Position eyePos, Angle heading, Angle tilt, Globe globe)
    {
        if (eyePos == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (heading == null || tilt == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle zero = Angle.fromDegrees(0);
        Vec4 eyePoint = globe.computePointFromPosition(eyePos);

        setView(globe, eyePoint, tilt, zero, heading);
    }

    /**
     * Sets the geographic position of the view "eye."  The orientation of the eye (heading and tilt) will be retrieved
     * from the current modelview matrix.
     *
     * @param eyePos the eye position.
     * @param globe  the current globe
     *
     * @throws IllegalArgumentException If <code>eyePosition</code> or <code>globe</code> is null.
     */
    public void setEyePosition(Position eyePos, Globe globe)
    {
        if (eyePos == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle tilt = getEyeTilt(globe);
        Angle heading = getEyeHeading(globe);

        setEyePosition(eyePos, heading, tilt, globe);
    }

    /**
     * Sets the tilt of the view "eye."  The tilt value may be between zero and 90 degrees, with zero indicating that
     * the eye looks directly down at the globe.
     *
     * @param tilt  the eye tilt.
     * @param globe the current globe
     *
     * @throws IllegalArgumentException If <code>tilt</code> or <code>globe</code> is null.
     */
    public void setEyeTilt(Angle tilt, Globe globe)
    {
        if (tilt == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        else if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (tilt.degrees > 90)
            tilt.setDegrees(90);
        else if (tilt.degrees < 0)
            tilt.setDegrees(0);

        Angle zero = Angle.fromDegrees(0);
        Vec4 eyePoint = getEyePoint();
        Angle heading = getEyeHeading(globe);

        setView(globe, eyePoint, tilt, zero, heading);
    }

    /**
     * Sets the tilt around the lookAt point.  The tilt value may be between zero and 90 degrees, with zero indicating
     * that the eye looks directly down at the ookAt, and 90 meaning that the eye looks towards the horizon at the
     * lookAt point.
     *
     * @param newTilt the tilt around the lookAt point.
     * @param globe   the current globe
     *
     * @throws IllegalArgumentException If <code>newTilt</code> or <code>globe</code> is null.
     */
    public void setLookAtTilt(Angle newTilt, Globe globe)
    {
        if (newTilt == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        else if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (newTilt.degrees > 89)         // view gets stuck if LookAt tilt reaches 90 degrees
            newTilt.setDegrees(89);       // because the eye ray intersection misses the globe
        else if (newTilt.degrees < 0)
            newTilt.setDegrees(0);

        Angle currentTilt = getLookAtTilt(globe);
        if (currentTilt == null)
            return;

        Angle delta = newTilt.subtract(currentTilt).multiply(-1);

        Position lookAtPosition = getLookAtPosition(globe);
        if (lookAtPosition == null)
            return;

        Vec4 lookAtPoint = globe.computePointFromPosition(lookAtPosition);
        Vec4 eyePoint = getEyePoint();
        double dist = eyePoint.distanceTo3(lookAtPoint);

        Matrix M = this.modelview.copy();
        M = Matrix.fromTranslation(0, 0, dist).multiply(M);
        M = Matrix.fromRotationX(delta).multiply(M);
        M = Matrix.fromTranslation(0, 0, -dist).multiply(M);

        setView(M);
    }

    /**
     * Sets the heading of the view "eye."  The heading values run positive clockwise from North, and indicate the view
     * rotation about the eye itself.
     *
     * @param heading the eye heading.
     * @param globe   the current globe
     *
     * @throws IllegalArgumentException If <code>heading</code> or <code>globe</code> is null.
     */
    public void setEyeHeading(Angle heading, Globe globe)
    {
        if (heading == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        else if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle zero = Angle.fromDegrees(0);
        Vec4 eyePoint = getEyePoint();
        Angle tilt = getEyeTilt(globe);

        setView(globe, eyePoint, tilt, zero, heading);
    }

    /**
     * Sets the heading rotation about the lookAt point.  Heading values run positive clockwise from North, and indicate
     * the view rotation about the lookAt.
     *
     * @param newHeading the heading rotation about the lookAt.
     * @param globe      the current globe
     *
     * @throws IllegalArgumentException If <code>newHeading</code> or <code>globe</code> is null.
     */
    public void setLookAtHeading(Angle newHeading, Globe globe)
    {
        // TODO: fix or deprecate this

        if (newHeading == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        else if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle heading = getLookAtHeading(globe);
        Angle delta = newHeading.subtract(heading).multiply(-1);  // negate for positive CW rotations

        Position lookAtPosition = getLookAtPosition(globe);
        if (lookAtPosition == null)
            return;

        Vec4 normal = new Vec4();
        globe.computeSurfaceNormalAtLocation(lookAtPosition.latitude, lookAtPosition.longitude, normal);

        Matrix M = this.modelview.copy();
        M = M.multiply(Matrix.fromAxisAngle(delta, normal));

        setView(M);
    }

    /**
     * Sets the zoom of the view "eye."  Values indicate the number of meters from the closest point to the center of
     * the globe along the current view trajectory.
     *
     * @param zoom the zoom distance of the eye.
     */
    public void setZoom(double zoom)
    {
        Matrix newView = Matrix.fromIdentity();
        newView.set(
            this.modelview.m[0], this.modelview.m[1], this.modelview.m[2], this.modelview.m[3],
            this.modelview.m[4], this.modelview.m[5], this.modelview.m[6], this.modelview.m[7],
            this.modelview.m[8], this.modelview.m[9], this.modelview.m[10], zoom,
            this.modelview.m[12], this.modelview.m[13], this.modelview.m[14], this.modelview.m[15]);
        setView(newView);
    }

    /**
     * Sets the view by setting a lookAt point, plus heading and tilt rotations about the lookAt, and a range value
     * corresponding to the fixed distance between the lookAt and the eye. This method corresponds to OrbitView in
     * WorldWind, or LookAt in KML. All rotations are positive clockwise.
     *
     * @param lookAtPosition the desired lookAt position
     * @param lookAtHeading  the heading rotation about the lookAt
     * @param lookAtTilt     the tilt rotation about the lookAt (zero to 90 degrees)
     * @param range          the distance from the lookAt of the eye, to be held fixed
     * @param globe          the current globe
     *
     * @throws IllegalArgumentException If <code>lookAtPosition</code>, <code>lookAtHeading</code>,
     *                                  <code>lookAtTilt</code> or <code>globe</code> is null.
     */
    public void setLookAtPosition(Position lookAtPosition, Angle lookAtHeading, Angle lookAtTilt, double range,
        Globe globe)
    {
        if (lookAtPosition == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (lookAtHeading == null || lookAtTilt == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Matrix M;
        // Zoom.
        M = Matrix.fromTranslation(0, 0, -range);
        // Tilt. Positive clockwise from looking straight down at the globe.
        M = M.multiply(Matrix.fromRotationX(lookAtTilt.multiply(-1.0)));
        // Heading. Also positive clockwise from North.
        M = M.multiply(Matrix.fromRotationZ(lookAtHeading.multiply(-1.0)));
        // translate and orient
        M = M.multiply(globe.computeViewOrientationAtPosition(lookAtPosition.latitude,
            lookAtPosition.longitude, lookAtPosition.elevation));

        setView(M);
    }

    protected Position computeEyePositionFromModelview(Globe globe)
    {
        if (globe != null)
        {
            Vec4 eyePoint = new Vec4().transformBy4(this.modelviewInv);
            return globe.computePositionFromPoint(eyePoint);
        }

        return new Position(); // (0,0,0)
    }

    protected double computeNearClipDistance(DrawContext dc)
    {
        Position eyePos = this.getEyePosition(dc.getGlobe());
        double tanHalfFov = this.fieldOfView.tanHalfAngle();
        double d = eyePos.elevation / (2 * Math.sqrt(2 * tanHalfFov * tanHalfFov + 1));

        if (d < MINIMUM_FAR_DISTANCE)
            d = MINIMUM_FAR_DISTANCE;

        return d;
    }

    protected double computeFarClipDistance(DrawContext dc)
    {
        Position eyePos = this.getEyePosition(dc.getGlobe());
        double d = WWMath.computeHorizonDistance(dc.getGlobe(), eyePos.elevation);

        if (d < MINIMUM_FAR_DISTANCE)
            d = MINIMUM_FAR_DISTANCE;

        return d;
    }

    /***********************************
     /
     / compute Matrix transforms
     /
     /***********************************/

    /**
     * Computes the modelview transform to use during rendering to set the View to the appropriate location and
     * orientation.  Tilt, roll, and heading values indicate rotations about the "eye" itself.
     *
     * @param globe    the current globe
     * @param eyePoint the current eyePoint
     * @param tilt     the current eye tilt
     * @param roll     the current eye roll
     * @param heading  the current eye heading
     *
     * @return the modelview transform for this view
     *
     * @throws IllegalArgumentException if globe is null
     */
    public Matrix computeViewMatrix(Globe globe, Vec4 eyePoint, Angle tilt, Angle roll, Angle heading)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Position eyePosition = globe.computePositionFromPoint(eyePoint);

        return computeViewMatrix(globe, eyePosition, tilt, roll, heading);
    }

    /**
     * Computes the modelview transform to use during rendering to set the View to the appropriate location and
     * orientation.  Tilt, roll, and heading values indicate rotations about the "eye" itself.
     *
     * @param globe   the current globe
     * @param eyePos  the current eye position
     * @param tilt    the current eye tilt
     * @param roll    the current eye roll
     * @param heading the current eye heading
     *
     * @return the modelview transform for this view
     *
     * @throws IllegalArgumentException if globe is null
     */
    public Matrix computeViewMatrix(Globe globe, Position eyePos, Angle tilt, Angle roll, Angle heading)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Matrix matrix = Matrix.fromIdentity();

        // first apply the user-specified heading/tilt/roll:
        // order corresponds to KML rotations (YXZ, positive clockwise)
        // But keep the rotations CCW because this is the View

        // tilt
        if (tilt != null)
            matrix = matrix.multiply(Matrix.fromRotationX(tilt.multiply(-1)));
        // roll
        if (roll != null)
            matrix = matrix.multiply(Matrix.fromRotationY(roll));
        // heading - negate for positive CW rotations
        if (heading != null)
            matrix = matrix.multiply(Matrix.fromRotationZ(heading.multiply(-1)));
        //matrix = matrix.multiply(Matrix.fromRotationZ(this.heading));

        // translate and orient
        matrix = matrix.multiply(
            globe.computeViewOrientationAtPosition(eyePos.latitude, eyePos.longitude, eyePos.elevation));

        return matrix;
    }

    protected void setView(Globe globe, Vec4 eyePoint, Angle tilt, Angle roll, Angle heading)
    {
        setView(computeViewMatrix(globe, eyePoint, tilt, roll, heading));
    }

    protected void setView(Matrix matrix)
    {
        this.modelview = matrix;
        this.modelviewInv.invertTransformMatrix(matrix);
        this.modelviewTranspose.transpose(matrix);
    }
}
