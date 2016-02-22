package org.droidplanner.android.locationrelay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.FollowApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

public class LocationRelay {
    static final String TAG = LocationRelay.class.getSimpleName();
    private static LocationRelay sInstance = null;
    private static final boolean V = true;

    private static final double DEFAULT_MIN_ALTITUDE = 10.0;
    private static final long DRONELOC_SEND_INTERVAL = 1000;

    public static final String EVT_BASE = "org.droidplanner.android.locationrelay";
    public static final String EVT_DRONE_LOCATION_UPDATED = EVT_BASE + ".DRONE_LOCATION_UPDATED";
    public static final String EVT_TARGET_LOCATION_UPDATED = EVT_BASE + ".TARGET_LOCATION_UPDATED";

    // Internal events
    public static final String EVT_INTERNAL_TARGET_LOCATION = EVT_BASE + ".INTERNAL_TARGET_LOCATION";
    public static final String EVT_FOLLOW_STOPPED = EVT_BASE + ".FOLLOW_STOPPED";

    public static final String
        EXTRA_LAT = "lat"
    ,   EXTRA_LNG = "lng"
    ,   EXTRA_ALTITUDE = "altitude"
    ,   EXTRA_HEADING = "heading"
    ,   EXTRA_TIME = "time"
    ,   EXTRA_ACCURACY = "accuracy"
    ,   EXTRA_SPEED = "speed"
    ,   EXTRA_LOCATION = "location"
    ;

    static Intent makeFollowApiIntent(Intent src) {
        Intent intent = new Intent(FollowApi.EVT_EXTERNAL_LOCATION)
                .putExtra(FollowApi.EXTRA_LAT, src.getDoubleExtra(EXTRA_LAT, 0))
                .putExtra(FollowApi.EXTRA_LNG, src.getDoubleExtra(EXTRA_LNG, 0))
                .putExtra(FollowApi.EXTRA_ALTITUDE, src.getDoubleExtra(EXTRA_ALTITUDE, 0))
                .putExtra(FollowApi.EXTRA_HEADING, src.getFloatExtra(EXTRA_HEADING, 0))
                .putExtra(FollowApi.EXTRA_SPEED, src.getFloatExtra(EXTRA_SPEED, 0))
                .putExtra(FollowApi.EXTRA_ACCURACY, src.getFloatExtra(EXTRA_ACCURACY, 10))
                .putExtra(FollowApi.EXTRA_TIME, src.getLongExtra(EXTRA_TIME, System.currentTimeMillis()))
                ;

        return intent;
    }

    public static void init(Context context) {
        if(sInstance == null) {
            sInstance = new LocationRelay(context);
            sInstance.setUp();
        }
    }

    public static void shutdown() {
        if(sInstance != null) {
            sInstance.tearDown();
            sInstance = null;
        }
    }

    public static LocationRelay get() {
        return sInstance;
    }

    private final BroadcastReceiver mGlobalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final Drone drone = getDrone();
            if(V) Log.v(TAG, "intent.action=" + action + " mUseExternalLocations=" + mUseExternalLocations);

            if(drone == null) {
                Log.v(TAG, "No drone");
                return;
            }

            switch(action) {
                case EVT_TARGET_LOCATION_UPDATED: {
                    if(isDroneFollowing() && mUseExternalLocations) {
                        // Bounce this location to the Follow API
                        Intent bounce = makeFollowApiIntent(intent);
                        if(bounce != null) {
                            mContext.sendBroadcast(bounce);
                        }

                        // Broadcast this event internally, in case the map wants to draw
                        // the target moving around. (It does.)
                        Location location = toAndroidLocation(intent);

                        if(location != null) {
                            sendInternalTargetLocationUpdate(location);
                        }
                    }

                    break;
                }
            }
        }
    };

    private final BroadcastReceiver mLocalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final Drone drone = getDrone();

            if(drone == null) {
                Log.v(TAG, "No drone");
                return;
            }

            switch(action) {
                case AttributeEvent.GPS_POSITION: {
                    if(mBroadcastLocationUpdates) {
                        final Gps gps = drone.getAttribute(AttributeType.GPS);
                        if (gps != null && gps.isValid()) {
                            final LatLong droneLocation = gps.getPosition();
                            broadcastDroneLocation(droneLocation);
                        }
                    }
                    else {
                        if(V) Log.v(TAG, "Not broadcasting drone location updates");
                    }

                    break;
                }

                case AttributeEvent.ALTITUDE_UPDATED: {
                    mCurrentAltitude = drone.getAttribute(AttributeType.ALTITUDE);
                    if(V) Log.v(TAG, "mCurrentAltitude=" + mCurrentAltitude.getAltitude());
                    break;
                }
            }
        }
    };

    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(DroidPlannerPrefs.PREF_BROADCAST_DRONE_LOCATION.equals(key)) {
                broadcastLocationUpdates(sharedPreferences.getBoolean(key, false));
            }
            else if(DroidPlannerPrefs.PREF_USE_RELAYED_LOCATIONS.equals(key)) {
                useExternalLocations(sharedPreferences.getBoolean(key, false));
            }
        }
    };

    private final Context mContext;
    private Altitude mCurrentAltitude;
    private boolean mBroadcastLocationUpdates;
    private boolean mUseExternalLocations;
    private boolean mIsDroneFollowing;
    private long mLastLocationSendTime = 0;
    private FollowType mSelectedFollowType = FollowType.LEASH;

    private LocationRelay(Context context) {
        super();
        mContext = context;

        DroidPlannerPrefs prefs = DroidPlannerApp.get().getAppPreferences();
        mUseExternalLocations = prefs.isUsingRelayedLocations();
        mBroadcastLocationUpdates = prefs.isBroadcastingDroneLocationUpdates();

        Log.v(TAG, "useExternal=" + mUseExternalLocations + " broadcast=" + mBroadcastLocationUpdates);
    }

    public Drone getDrone() {
        return ((DroidPlannerApp)mContext).getDrone();
    }

    public FollowType getSelectedFollowType() { return mSelectedFollowType; }
    public void setSelectedFollowType(FollowType type) { mSelectedFollowType = type; }

    public boolean isFollowing(Drone drone) {
        if(drone != null) {
            final FollowState followState = drone.getAttribute(AttributeType.FOLLOW_STATE);
            return followState.isEnabled();
        }
        else {
            return false;
        }
    }

    public boolean isDroneFollowing() {
        return mIsDroneFollowing;
    }

    public void setDroneFollowing(boolean follow) {

        if(mIsDroneFollowing) {
            if(!follow) {
                sendLocalBroadcast(new Intent(EVT_FOLLOW_STOPPED));
            }
        }

        mIsDroneFollowing = follow;
    }

    public boolean isUsingExternalLocations() {
        return mUseExternalLocations;
    }

    public void useExternalLocations(boolean use) {
        Log.v(TAG, "useExternalLocations(): use=" + use);

        FollowApi api = FollowApi.getApi(getDrone());

        if(mUseExternalLocations != use) {
            if(api != null) {
                Log.v(TAG, "Telling FollowApi use locations: " + use);
                api.useExternalLocations(use);
            }

            if(mUseExternalLocations) {
                // turn it off
            }
            else {
                // turn it on
            }

            mUseExternalLocations = use;
        }
    }

    public boolean isBroadcastingLocationUpdates() {
        return mBroadcastLocationUpdates;
    }

    public void broadcastLocationUpdates(boolean bcast) {
        Log.v(TAG, "broadcastLocationUpdates(): bcast=" + bcast);

        if(mBroadcastLocationUpdates != bcast) {
            mBroadcastLocationUpdates = bcast;
        }
    }

    public void broadcastDroneLocation(LatLong loc) {
        if(loc != null) {
            final long now = SystemClock.elapsedRealtime();

            // Throttle the output of drone locations. They arrive in firehose-like
            // fashion, WAY faster than Android GPS.
            if((now - mLastLocationSendTime) > DRONELOC_SEND_INTERVAL) {
                Intent intent = new Intent(EVT_DRONE_LOCATION_UPDATED)
                        .putExtra(EXTRA_LAT, loc.getLatitude())
                        .putExtra(EXTRA_LNG, loc.getLongitude())
                        .putExtra(EXTRA_TIME, System.currentTimeMillis())
                        ;

                if(mCurrentAltitude != null) {
                    intent.putExtra(EXTRA_ALTITUDE, Math.max(mCurrentAltitude.getAltitude(), DEFAULT_MIN_ALTITUDE));
                }
                else {
                    intent.putExtra(EXTRA_ALTITUDE, DEFAULT_MIN_ALTITUDE);
                }

                mContext.sendBroadcast(intent);

                mLastLocationSendTime = now;
            }
        }
        else {
            Log.w(TAG, "broadcastDroneLocation(): null location");
        }
    }

    public static LatLong toLatLong(Intent intent) {
        LatLong ll = new LatLong(intent.getDoubleExtra(EXTRA_LAT, 0), intent.getDoubleExtra(EXTRA_LNG, 0));
        return ll;
    }

    public static Location toAndroidLocation(Intent intent) {
        Location loc = new Location("EXPLICIT");
        loc.setLatitude(intent.getDoubleExtra(EXTRA_LAT, 0));
        loc.setLongitude(intent.getDoubleExtra(EXTRA_LNG, 0));
        loc.setAltitude(intent.getDoubleExtra(EXTRA_ALTITUDE, 0));
        loc.setAccuracy(intent.getIntExtra(EXTRA_ACCURACY, 100));
        loc.setBearing(intent.getFloatExtra(EXTRA_HEADING, 0));
        loc.setSpeed(intent.getFloatExtra(EXTRA_SPEED, 0));
        loc.setTime(intent.getLongExtra(EXTRA_TIME, 0));
        return loc;
    }

    private void setUp() {
        registerForEvents();

        final DroidPlannerPrefs dpPrefs = ((DroidPlannerApp)mContext).getAppPreferences();
        mBroadcastLocationUpdates = dpPrefs.isBroadcastingDroneLocationUpdates();
        mUseExternalLocations = dpPrefs.isUsingRelayedLocations();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
    }

    private void tearDown() {
        unregisterForEvents();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.unregisterOnSharedPreferenceChangeListener(mPrefChangeListener);
    }

    private void registerForEvents() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(EVT_TARGET_LOCATION_UPDATED);
        mContext.registerReceiver(mGlobalReceiver, filter);

        // Local events
        filter = new IntentFilter();
        filter.addAction(AttributeEvent.GPS_POSITION);
        filter.addAction(AttributeEvent.ALTITUDE_UPDATED);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mLocalReceiver, filter);
    }

    private void unregisterForEvents() {
        try {
            mContext.unregisterReceiver(mGlobalReceiver);
        } catch(Throwable ex) { /* ok */ }

        try {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mLocalReceiver);
        } catch(Throwable ex) { /* ok */ }
    }

    void sendInternalTargetLocationUpdate(Location location) {
        Intent intent = new Intent(EVT_INTERNAL_TARGET_LOCATION)
                .putExtra(EXTRA_LOCATION, location);
        sendLocalBroadcast(intent);
    }

    void sendLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
