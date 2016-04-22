package org.droidplanner.android.utils.tracker;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.GimbalApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

import javax.microedition.khronos.opengles.GL10;

import timber.log.Timber;

//import com.o3dr.android.client.apis.ControlApi;


class TrackerDroneLoop
{
    private GimbalApi gimbalApi;
    private Drone drone  = null;
    //private final ControlApi controlApi;
    private VehicleApi vehicleApi;
    private boolean arduPilotSmoothing = false; // DK: ControlActions.ACTION_LOOK_AT_TARGET:


    double currVehicleAbsoluteYaw = 0;
    protected float yawOffset = 0;
    float offsetChange = 0.0f;
    long lastResetTime = 0;

    float MOVE_RIGHT = +3.0f;
    float MOVE_LEFT  = -3.0f;
    final long VEHICLE_CONTROL_UPDATE_FREQ = 40;
    final long VEHICLE_CONTROL_UPDATE_SPEED_IN_MILLIS = (long)(1000.0/(double)VEHICLE_CONTROL_UPDATE_FREQ);



    TrackerDroneLoop(Drone drone) {
        this.drone  = drone;
        this.gimbalApi = GimbalApi.getApi(drone);
        //this.controlApi = ControlApi.getApi(drone);
        this.vehicleApi = VehicleApi.getApi(drone);
    }

    private final GimbalApi.GimbalOrientationListener gimbalOrientationListener = new GimbalApi.GimbalOrientationListener() {
        @Override
        public void onGimbalOrientationUpdate(GimbalApi.GimbalOrientation gimbalOrientation) {
        }

        @Override
        public void onGimbalOrientationCommandError(int i) {
        }
    };

    void setGuidedMode() {
        if (!arduPilotSmoothing) {
            gimbalApi.startGimbalControl(gimbalOrientationListener);

            vehicleApi.setVehicleMode(VehicleMode.COPTER_GUIDED);
        }
    }

    void setLoiterMode() {
        if (!arduPilotSmoothing) {
            gimbalApi.stopGimbalControl(gimbalOrientationListener);

            vehicleApi.setVehicleMode(VehicleMode.COPTER_LOITER);
        }
    }

    protected boolean isTimeToRestartYawOffset() {
        // 0.0125 2^-10
        //  0.0000001
        //if (offsetChange < 1.0f/(1000.0f*100.0f*1.0f)) offsetChange = 0;
        //(offsetChange < 0.0000001);
        return  (System.currentTimeMillis() - lastResetTime) > VEHICLE_CONTROL_UPDATE_SPEED_IN_MILLIS;
    }

    protected void checkYawOffsetState() {
        if (isTimeToRestartYawOffset()) {
            Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
            currVehicleAbsoluteYaw = attitude.getYaw();

            yawOffset = 0;
            offsetChange = 0.0f;

            lastResetTime = System.currentTimeMillis();
        }
    }

    protected void updateYawChange() {
        offsetChange *= 0.5f;
    }

    /*
     computeYaw:

        computes the new absolute vehicle (gimbal) yaw required to center
        the tracked objects on the display

         GOPRO HERO3+ intrensics

         Wide FOV - 14mm
         Medium FOV - 21mm
         Narrow FOV - 28mm

         16x9    V-FOV  H-FOV  Focal Length
         ------+-------+------+-----------
         Wide    69.5   118.2  14mm
         Medium  55     94.4   21mm
         Narrow  37.2   64.4   28mm

     */
    public float computeYaw(int test, int w, int h, int ox, int oy) {
        updateYawChange();

        // resest abs yaw, yaw offset, and offset decay if needed
        checkYawOffsetState();

        //yaw value is between -180 and 180. Convert so the value is between 0 to 360
        if (currVehicleAbsoluteYaw < 0) {
            currVehicleAbsoluteYaw += 360;
        }

        int INACTIVE_CENTER_AREA_HORIZ = w/400;

        // make right offsetChange happe
        if (ox > w/2 && offsetChange == 0.0f) {
            offsetChange = 2*MOVE_RIGHT;
            Timber.d("testYaw3 MOVE_RIGHT ox=%d offsetChange=%f", ox, offsetChange);
        }
        else if (ox > INACTIVE_CENTER_AREA_HORIZ && offsetChange == 0.0f) {
            offsetChange = MOVE_RIGHT;
            Timber.d("testYaw3 MOVE_RIGHT ox=%d offsetChange=%f", ox, offsetChange);
        }
        else if (ox < -w/2 && offsetChange == 0.0f) {
            offsetChange = 2*MOVE_LEFT;
            Timber.d("testYaw3 MOVE_LEFT ox=%d offsetChange=%f", ox, offsetChange);
        }
        else if (ox < -INACTIVE_CENTER_AREA_HORIZ && offsetChange == 0.0f) {
            offsetChange = MOVE_LEFT;
            Timber.d("testYaw3 MOVE_LEFT ox=%d offsetChange=%f", ox, offsetChange);
        }

        yawOffset += offsetChange;

        if (yawOffset > 360)
            yawOffset -= 360.0f;
        if (yawOffset < 0)
            yawOffset += 360.0f;

        float rotateTo = (float) currVehicleAbsoluteYaw + yawOffset;
        rotateTo = (rotateTo + 360) % 360;

        long newTime = System.currentTimeMillis();

        Timber.d("computeYaw C yawOffset=%f",  yawOffset);

        return rotateTo;
    }

    public float computePitch(int w, int h, int ox, int oy) {
        GimbalApi.GimbalOrientation orientation = gimbalApi.getGimbalOrientation();

        float pitchTo = orientation.getPitch();

        float pitchOffset = 0;

        if (oy > h/50) {
            pitchOffset = -2;
        }
        else if (oy < -(h/50)) {
            pitchOffset = +2;
        }

        pitchTo += pitchOffset;

        if (pitchTo > 0)
            pitchTo = 0;
        if (pitchTo < -90 )
            pitchTo = -90;

        Timber.d("computePitch C pitchOffset=%f (offsetY=%d)", pitchOffset, oy);

        return pitchTo;
    }

    float Deg2Radians(float deg) {
        return (float)Math.PI*deg/180.0f;
    }

    double DegreesToRadians(double deg) {
        return (double)Deg2Radians((float)deg);
    }

    double RadiansToDegrees(double rads) {
        return 180.0*rads/Math.PI;
    }

    private double EARTH_RADIUS = 6371010.0;
    private double LATLON_TO_M = 111319.5;

    /*
    //Calculate a Location from a start location, azimuth (in degrees), and distance
//this only handles the 2D component (no altitude)
    public func newLocationFromAzimuthAndDistance(loc: Location, azimuth: Double, distance: Double) -> Location {
    var result = loc

    let rLat = DegreesToRadians(loc.latitude)
    let rLong = DegreesToRadians(loc.longitude)
    let dist = Double(distance) / EARTH_RADIUS
    let az = Double( DegreesToRadians(azimuth) )

    let lat = asin( sin(rLat) * cos(dist) + cos(rLat) * sin(dist) * cos(az) )

    result.latitude = RadiansToDegrees(lat)
    result.longitude = RadiansToDegrees(rLong + atan2( sin(az) * sin(dist) * cos(rLat), cos(dist) - sin(rLat) * sin(lat) ))

    return result
}
*/


    LatLongAlt newLocationFromAzimuthAndDistance(LatLongAlt loc, float azimuth, float distance) {
        LatLongAlt result = new LatLongAlt(0,0,0);

        double rLat = DegreesToRadians(loc.getLatitude());
        double rLong = DegreesToRadians(loc.getLongitude());
        double dist = (double)(distance) / EARTH_RADIUS;
        double az = ( DegreesToRadians(azimuth) );

        double lat = Math.asin(Math.sin(rLat) * Math.cos(dist) + Math.cos(rLat) * Math.sin(dist) * Math.cos(az));

        result.setLatitude(RadiansToDegrees(lat));

        double longitude = RadiansToDegrees(rLong + Math.atan2(Math.sin(az) * Math.sin(dist) * Math.cos(rLat),
                Math.cos(dist) - Math.sin(rLat) * Math.sin(lat)));

        result.setLongitude(longitude);

        return result;
    }

    public LatLongAlt roiFromAttitude(float yaw, float pitch) {
        /*
        var currentROI: Location {
            let pitch = SoloApp.sharedInstance.gimbalModel.gimbalPitch
            let pos: Location = location
            var roi: Location = pos

        In these special cases, return a point straight ahead of the copter
        1. no gimbal pitch
        2. Copter altitude is below 0.0 (can't intersect with the ground plane in this case)

            if pitch == 0.0 || pos.altitude < 0.0 {
                //pick a point at a default distance in that direction
                roi = newLocationFromAzimuthAndDistance(pos, azimuth: heading, distance: ROI_DEFAULT_DISTANCE)
            }
            //find intersection with ground plane
            else {
                //based on altitude and gimbal pitch, we intersect the ground at this distance
                let inversePitch = 90 - pitch
                let dist = pos.altitude * tan(DegreesToRadians(inversePitch))
                roi = newLocationFromAzimuthAndDistance(pos, azimuth: heading, distance: dist)
                roi.altitude = 0
            }

            return roi
        }
       */
        Altitude altitude = drone.getAttribute(AttributeType.ALTITUDE);

        LatLong location = null;
        Gps droneGps = drone.getAttribute(AttributeType.GPS);
        if (droneGps.isValid()) {
            location = droneGps.getPosition();
        }

        float inversePitch = 90 - pitch;
        float dist  = (float)altitude.getAltitude() * (float)Math.tan(Deg2Radians(inversePitch));

        LatLongAlt loc = new LatLongAlt(location.getLatitude(),
                location.getLongitude(),
                altitude.getAltitude());

        LatLongAlt roi = newLocationFromAzimuthAndDistance(loc, yaw, dist);
        roi.setAltitude(0);

        return roi;
    }

    public void updateDroneCameraROI(LatLongAlt loc)
    {
        ControlApi.getApi(drone).lookAt(loc, true, null);
    }

    public void updateDroneCamera(int w, int h, int cx, int cy) {
        float rotateTo = computeYaw(0, w, h, cx, cy);
        float pitchTo = computePitch(w, h, cx, cy);

        if (arduPilotSmoothing) {
            LatLongAlt loc = roiFromAttitude(rotateTo, pitchTo);
            updateDroneCameraROI(loc);
        } else {
            GimbalApi.GimbalOrientation orientation = gimbalApi.getGimbalOrientation();
            gimbalApi.updateGimbalOrientation(
                    pitchTo,
                    orientation.getRoll(),
                    rotateTo,
                    gimbalOrientationListener);
        }
    }
}


/**
 * Created by Aaron Licata on 2/26/2016.
 */
public class TrackerManager {
    private Tracker tracker = null;
    private FboRenderer fbo = new FboRenderer();
    private TrackerDrawer trackerDrawer = new TrackerDrawer();
    private TrackerDroneLoop trackerDroneController = null;

    int[] sbbx = new int[4];

    public TrackerManager() {
    }

    public static final int getOptimalWidth() {
        return Tracker.OPTIMAL_WIDTH;
    }

    public static final int getOptimalHeight() {
        return Tracker.OPTIMAL_HEIGHT;
    }

    public void onSurfaceCreated(int mDisplayWidth, int mDisplayHeight) {
        fbo.initFBO(Tracker.OPTIMAL_WIDTH, Tracker.OPTIMAL_HEIGHT);

        trackerDrawer.TrackerDrawerInit(mDisplayWidth,
                mDisplayHeight,
                fbo.getWidth(),
                fbo.getHeight());

        tracker = new Tracker();
    }

    // from display to framebuffer cs
    public int toFbX(float x) {
        return (int)(x*fbo.getWidth()/trackerDrawer.getDisplayWidth());
    }

    // from display to framebuffer cs
    public int toFbY(float y) {
        return (int)(y* fbo.getHeight()/trackerDrawer.getDisplayHeight());
    }

    public int toDisplayX(int x) {
        return (int)(x*trackerDrawer.getDisplayWidth()/fbo.getWidth());
    }

    public int toDisplayY(int y) {
        return (int)(y*trackerDrawer.getDisplayHeight()/fbo.getHeight());
    }

    public void setSelection(float x, float y, float dx, float dy,  boolean doInit) {
        tracker.requestInit(toFbX(x), toFbY(y), toFbX(dx), toFbY(dy));
        trackerDrawer.resetUiSelection((int) x, (int) y, (int) dx, (int) dy);
    }

    public void toggleCommands(float x, float y) {
        trackerDrawer.toggleCommands(x, y);

        if (trackerDrawer.getDebugOn()) {
            trackerDroneController.setGuidedMode();
            trackerDrawer.toggleTest = !trackerDrawer.toggleTest;
            Timber.d("togglTest=%d", trackerDrawer.toggleTest ? 1 : 0);
        }
        else {
            trackerDroneController.setLoiterMode();
        }
    }

    public void onDrawFrameOnScreen()
    {
        if (trackerDrawer.getDebugOn()) {

            trackerDrawer.TrackerDrawer_drawDebug(tracker.status,
                    tracker.bbx,
                    tracker.P0,
                    tracker.P1,
                    tracker.numPoints);
        }

        trackerDrawer.TrackerDrawer_draw(tracker.status, tracker.bbx);
    }

    public boolean isTrackerResultAvailable(int[] fbBbx) {
        return (fbBbx[0]>0 && fbBbx[1]>0 && fbBbx[1]>0 && fbBbx[1]>0);
    }


    public void onDrawFrameOffScreen(GL10 gl, int textureId) {
        FboRenderer.drawOffScreenFrame(gl, fbo, textureId);

        int[] fbBbx = new int[4];

        //  TESTS:
        // 1) disable tracker processing to test if it freezes the yaw update because in same rendering thread
        // 2) try large displacements (10+ degrees) to check that yaw never reaches target
        fbBbx = tracker.sendFrameBufferToTracker(fbo.getFBOBuffer(),
                fbo.getWidth(),
                fbo.getHeight(),
                trackerDrawer.getCmdStatus());

        // update when there is a new result
        // the old result will be remembered by the getPixelSpaceBbx method
        if (isTrackerResultAvailable(fbBbx)
                || fbBbx[0]==0
                )
        {
            sbbx[0] = toDisplayX(fbBbx[0]);
            sbbx[1] = toDisplayY(fbBbx[1]);
            sbbx[2] = toDisplayX(fbBbx[2]);
            sbbx[3] = toDisplayY(fbBbx[3]);

            int offsetX = (sbbx[0] + sbbx[2] / 2) - trackerDrawer.getDisplayWidth()/2;
            int offsetY = (sbbx[1] + sbbx[3] / 2) - trackerDrawer.getDisplayHeight()/2;

            int oxLimit = trackerDrawer.getDisplayWidth()/2-1;
            if (offsetX < -oxLimit)
                offsetX = 0;

            int oyLimit = trackerDrawer.getDisplayHeight()/2-1;
            if (offsetY < -oyLimit)
                offsetY = 0;

            if (trackerDrawer.getDebugOn()) {

                trackerDroneController.updateDroneCamera(
                                trackerDrawer.getDisplayWidth(),
                                trackerDrawer.getDisplayHeight(),
                                offsetX,
                                offsetY);

            }
        }
    }

    public void onSurfaceChanged(int width, int height) {
        trackerDrawer.TrackerDrawer_onSurfaceChanged(width, height);
    }

    public void setDrone(Drone drone) {
        trackerDroneController = new TrackerDroneLoop(drone);
    }
}
