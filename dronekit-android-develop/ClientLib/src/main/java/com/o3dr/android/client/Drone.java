package com.o3dr.android.client;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.LinkListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.calibration.magnetometer.MagnetometerCalibrationStatus;
import com.o3dr.services.android.lib.drone.companion.solo.SoloAttributes;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.GuidedState;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.gcs.link.LinkEvent;
import com.o3dr.services.android.lib.gcs.link.LinkEventExtra;
import com.o3dr.services.android.lib.gcs.returnToMe.ReturnToMeState;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.IDroneApi;
import com.o3dr.services.android.lib.model.IObserver;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by fhuya on 11/4/14.
 */
public class Drone {
    private static final String CLAZZ_NAME = Drone.class.getName();
    private static final String TAG = Drone.class.getSimpleName();

    public interface OnAttributeRetrievedCallback<T extends Parcelable> {
        void onRetrievalSucceed(T attribute);

        void onRetrievalFailed();
    }

    public static class AttributeRetrievedListener<T extends Parcelable> implements OnAttributeRetrievedCallback<T> {

        @Override
        public void onRetrievalSucceed(T attribute) {
        }

        @Override
        public void onRetrievalFailed() {
        }
    }

    public interface OnMissionItemsBuiltCallback<T extends MissionItem> {
        void onMissionItemsBuilt(MissionItem.ComplexItem<T>[] complexItems);
    }

    public static final int COLLISION_SECONDS_BEFORE_COLLISION = 2;
    public static final double COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND = -3.0;
    public static final double COLLISION_SAFE_ALTITUDE_METERS = 1.0;

    public static final String ACTION_GROUND_COLLISION_IMMINENT = CLAZZ_NAME + ".ACTION_GROUND_COLLISION_IMMINENT";
    public static final String EXTRA_IS_GROUND_COLLISION_IMMINENT = "extra_is_ground_collision_imminent";

    private final IBinder.DeathRecipient binderDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            notifyDroneServiceInterrupted("Lost access to the drone api.");
        }
    };

    private final ConcurrentLinkedQueue<DroneListener> droneListeners = new ConcurrentLinkedQueue<>();

    private Handler handler;
    private ControlTower serviceMgr;
    private DroneObserver droneObserver;

    private final AtomicReference<IDroneApi> droneApiRef = new AtomicReference<>(null);
    private ConnectionParameter connectionParameter;
    private LinkListener linkListener;
    private ExecutorService asyncScheduler;

    // flightTimer
    // ----------------
    private long startTime = 0;
    private long elapsedFlightTime = 0;

    private final Context context;
    private final ClassLoader contextClassLoader;

    /**
     * Creates a Drone instance.
     *
     * @param context Application context
     */
    public Drone(Context context) {
        this.context = context;
        this.contextClassLoader = context.getClassLoader();
    }

    void init(ControlTower controlTower, Handler handler) {
        this.handler = handler;
        this.serviceMgr = controlTower;
        this.droneObserver = new DroneObserver(this);
    }

    Context getContext() {
        return this.context;
    }

    synchronized void start() {
        if (!serviceMgr.isTowerConnected()) {
            throw new IllegalStateException("Service manager must be connected.");
        }

        IDroneApi droneApi = droneApiRef.get();
        if (isStarted(droneApi)) {
            return;
        }

        try {
            droneApi = serviceMgr.registerDroneApi();
            droneApi.asBinder().linkToDeath(binderDeathRecipient, 0);
        } catch (RemoteException e) {
            throw new IllegalStateException("Unable to retrieve a valid drone handle.");
        }

        if (asyncScheduler == null || asyncScheduler.isShutdown()) {
            asyncScheduler = Executors.newFixedThreadPool(1);
        }

        addAttributesObserver(droneApi, this.droneObserver);
        resetFlightTimer();

        droneApiRef.set(droneApi);
    }

    synchronized void destroy() {
        IDroneApi droneApi = droneApiRef.get();

        removeAttributesObserver(droneApi, this.droneObserver);

        try {
            if (isStarted(droneApi)) {
                droneApi.asBinder().unlinkToDeath(binderDeathRecipient, 0);
                serviceMgr.releaseDroneApi(droneApi);
            }
        } catch (RemoteException | NoSuchElementException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (asyncScheduler != null) {
            asyncScheduler.shutdownNow();
            asyncScheduler = null;
        }

        droneApiRef.set(null);
    }

    private void checkForGroundCollision() {
        Speed speed = getAttribute(AttributeType.SPEED);
        Altitude altitude = getAttribute(AttributeType.ALTITUDE);
        if (speed == null || altitude == null) {
            return;
        }

        double verticalSpeed = speed.getVerticalSpeed();
        double altitudeValue = altitude.getAltitude();

        boolean isCollisionImminent = altitudeValue
            + (verticalSpeed * COLLISION_SECONDS_BEFORE_COLLISION) < 0
            && verticalSpeed < COLLISION_DANGEROUS_SPEED_METERS_PER_SECOND
            && altitudeValue > COLLISION_SAFE_ALTITUDE_METERS;

        Bundle extrasBundle = new Bundle(1);
        extrasBundle.putBoolean(EXTRA_IS_GROUND_COLLISION_IMMINENT, isCollisionImminent);
        notifyAttributeUpdated(ACTION_GROUND_COLLISION_IMMINENT, extrasBundle);
    }

    private void handleRemoteException(RemoteException e) {
        final IDroneApi droneApi = droneApiRef.get();
        if (droneApi != null && !droneApi.asBinder().pingBinder()) {
            final String errorMsg = e.getMessage();
            Log.e(TAG, errorMsg, e);
            notifyDroneServiceInterrupted(errorMsg);
        }
    }

    public double getSpeedParameter() {
        Parameters params = getAttribute(AttributeType.PARAMETERS);
        if (params != null) {
            Parameter speedParam = params.getParameter("WPNAV_SPEED");
            if (speedParam != null) {
                return speedParam.getValue();
            }
        }

        return 0;
    }

    /**
     * Causes the Runnable to be added to the message queue.
     *
     * @param action Runnabl that will be executed.
     */
    public void post(Runnable action) {
        if (handler == null || action == null) {
            return;
        }

        handler.post(action);
    }

    /**
     * Reset the vehicle flight timer.
     */
    public void resetFlightTimer() {
        elapsedFlightTime = 0;
        startTime = SystemClock.elapsedRealtime();
    }

    private void stopTimer() {
        // lets calc the final elapsed timer
        elapsedFlightTime += SystemClock.elapsedRealtime() - startTime;
        startTime = SystemClock.elapsedRealtime();
    }

    /**
     * @return Vehicle flight time in seconds.
     */
    public long getFlightTime() {
        State droneState = getAttribute(AttributeType.STATE);
        if (droneState != null && droneState.isFlying()) {
            // calc delta time since last checked
            elapsedFlightTime += SystemClock.elapsedRealtime() - startTime;
            startTime = SystemClock.elapsedRealtime();
        }
        return elapsedFlightTime / 1000;
    }

    public <T extends Parcelable> T getAttribute(String type) {
        final IDroneApi droneApi = droneApiRef.get();
        if (!isStarted(droneApi) || type == null) {
            return this.getAttributeDefaultValue(type);
        }

        T attribute = null;
        Bundle carrier = null;
        try {
            carrier = droneApi.getAttribute(type);
        } catch (RemoteException e) {
            handleRemoteException(e);
        }

        if (carrier != null) {
            try {
                carrier.setClassLoader(contextClassLoader);
                attribute = carrier.getParcelable(type);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return attribute == null ? this.<T>getAttributeDefaultValue(type) : attribute;
    }

    public <T extends Parcelable> void getAttributeAsync(final String attributeType,
                                                         final OnAttributeRetrievedCallback<T> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback must be non-null.");
        }

        final IDroneApi droneApi = droneApiRef.get();
        if (!isStarted(droneApi)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onRetrievalFailed();
                }
            });
            return;
        }

        asyncScheduler.execute(new Runnable() {
            @Override
            public void run() {
                final T attribute = getAttribute(attributeType);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (attribute == null) {
                            callback.onRetrievalFailed();
                        } else {
                            callback.onRetrievalSucceed(attribute);
                        }
                    }
                });
            }
        });
    }

    private <T extends Parcelable> T getAttributeDefaultValue(String attributeType) {
        if (attributeType == null) {
            return null;
        }

        switch (attributeType) {
            case AttributeType.ALTITUDE:
                return (T) new Altitude();

            case AttributeType.GPS:
                return (T) new Gps();

            case AttributeType.STATE:
                return (T) new State();

            case AttributeType.PARAMETERS:
                return (T) new Parameters();

            case AttributeType.SPEED:
                return (T) new Speed();

            case AttributeType.ATTITUDE:
                return (T) new Attitude();

            case AttributeType.HOME:
                return (T) new Home();

            case AttributeType.BATTERY:
                return (T) new Battery();

            case AttributeType.MISSION:
                return (T) new Mission();

            case AttributeType.SIGNAL:
                return (T) new Signal();

            case AttributeType.GUIDED_STATE:
                return (T) new GuidedState();

            case AttributeType.TYPE:
                return (T) new Type();

            case AttributeType.FOLLOW_STATE:
                return (T) new FollowState();

            case AttributeType.MAGNETOMETER_CALIBRATION_STATUS:
                return (T) new MagnetometerCalibrationStatus();

            case AttributeType.RETURN_TO_ME_STATE:
                return (T) new ReturnToMeState();

            case AttributeType.CAMERA:
            case SoloAttributes.SOLO_STATE:
            case SoloAttributes.SOLO_GOPRO_STATE:
            case SoloAttributes.SOLO_GOPRO_STATE_V2:
            default:
                return null;
        }
    }

    /**
     * Connect to a vehicle using a specified {@link ConnectionParameter}.
     *
     * @param connParams Specified parameters to determine how to connect the vehicle.
     */
    public void connect(final ConnectionParameter connParams) {
        connect(connParams, null);
    }

    /**
     * Connect to a vehicle using a specified {@link ConnectionParameter} and a {@link LinkListener}
     * callback.
     *
     * @param connParams Specified parameters to determine how to connect the vehicle.
     * @param linkListener A callback that will update the caller on the state of the link connection.
     */
    public void connect(ConnectionParameter connParams, LinkListener linkListener) {
        VehicleApi.getApi(this).connect(connParams);
        this.connectionParameter = connParams;
        this.linkListener = linkListener;
    }

    /**
     * Disconnect from the vehicle.
     */
    public void disconnect() {
        VehicleApi.getApi(this).disconnect();
        this.connectionParameter = null;
        this.linkListener = null;
    }

    private static AbstractCommandListener wrapListener(final Handler handler, final AbstractCommandListener listener) {
        AbstractCommandListener wrapperListener = listener;
        if (handler != null && listener != null) {
            wrapperListener = new AbstractCommandListener() {
                @Override
                public void onSuccess() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSuccess();
                        }
                    });
                }

                @Override
                public void onError(final int executionError) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onError(executionError);
                        }
                    });
                }

                @Override
                public void onTimeout() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onTimeout();
                        }
                    });
                }
            };
        }

        return wrapperListener;
    }

    public boolean performAction(Action action) {
        return performActionOnDroneThread(action, null);
    }

    public boolean performActionOnDroneThread(Action action, AbstractCommandListener listener) {
        return performActionOnHandler(action, this.handler, listener);
    }

    public boolean performActionOnHandler(Action action, final Handler handler, final AbstractCommandListener listener) {
        final IDroneApi droneApi = droneApiRef.get();
        if (isStarted(droneApi)) {
            try {
                droneApi.executeAction(action, wrapListener(handler, listener));
                return true;
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        return false;
    }

    public boolean performAsyncAction(Action action) {
        return performAsyncActionOnDroneThread(action, null);
    }

    public boolean performAsyncActionOnDroneThread(Action action, AbstractCommandListener listener) {
        return performAsyncActionOnHandler(action, this.handler, listener);
    }

    public boolean performAsyncActionOnHandler(Action action, Handler handler, AbstractCommandListener listener) {
        final IDroneApi droneApi = droneApiRef.get();
        if (isStarted(droneApi)) {
            try {
                droneApi.executeAsyncAction(action, wrapListener(handler, listener));
                return true;
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }

        return false;
    }

    private boolean isStarted(IDroneApi droneApi) {
        return droneApi != null && droneApi.asBinder().pingBinder();
    }

    public boolean isStarted() {
        return isStarted(droneApiRef.get());
    }

    public boolean isConnected() {
        final IDroneApi droneApi = droneApiRef.get();
        State droneState = getAttribute(AttributeType.STATE);
        return isStarted(droneApi) && droneState.isConnected();
    }

    public ConnectionParameter getConnectionParameter() {
        return this.connectionParameter;
    }

    public <T extends MissionItem> void buildMissionItemsAsync(final MissionItem.ComplexItem<T>[] missionItems,
                                                               final OnMissionItemsBuiltCallback<T> callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback must be non-null.");
        }

        if (missionItems == null || missionItems.length == 0) {
            return;
        }

        asyncScheduler.execute(new Runnable() {
            @Override
            public void run() {
                for (MissionItem.ComplexItem<T> missionItem : missionItems)
                    MissionApi.getApi(Drone.this).buildMissionItem(missionItem);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onMissionItemsBuilt(missionItems);
                    }
                });
            }
        });
    }

    public void registerDroneListener(DroneListener listener) {
        if (listener == null) {
            return;
        }

        if (!droneListeners.contains(listener)) {
            droneListeners.add(listener);
        }
    }

    private void addAttributesObserver(IDroneApi droneApi, IObserver observer) {
        if (isStarted(droneApi)) {
            try {
                droneApi.addAttributesObserver(observer);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void addMavlinkObserver(MavlinkObserver observer) {
        final IDroneApi droneApi = droneApiRef.get();
        if (isStarted(droneApi)) {
            try {
                droneApi.addMavlinkObserver(observer);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void removeMavlinkObserver(MavlinkObserver observer) {
        final IDroneApi droneApi = droneApiRef.get();
        if (isStarted(droneApi)) {
            try {
                droneApi.removeMavlinkObserver(observer);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public void unregisterDroneListener(DroneListener listener) {
        if (listener == null) {
            return;
        }

        droneListeners.remove(listener);
    }

    private void removeAttributesObserver(IDroneApi droneApi, IObserver observer) {
        if (isStarted(droneApi)) {
            try {
                droneApi.removeAttributesObserver(observer);
            } catch (RemoteException e) {
                handleRemoteException(e);
            }
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public ExecutorService getAsyncScheduler(){
        return asyncScheduler;
    }

    void notifyAttributeUpdated(final String attributeEvent, final Bundle extras) {
        //Update the bundle classloader
        if (extras != null) {
            extras.setClassLoader(contextClassLoader);
        }

        switch (attributeEvent) {
            case AttributeEvent.STATE_UPDATED:
                getAttributeAsync(AttributeType.STATE, new OnAttributeRetrievedCallback<State>() {
                    @Override
                    public void onRetrievalSucceed(State state) {
                        if (state.isFlying()) {
                            resetFlightTimer();
                        } else {
                            stopTimer();
                        }
                    }

                    @Override
                    public void onRetrievalFailed() {
                        stopTimer();
                    }
                });
                break;

            case AttributeEvent.SPEED_UPDATED:
                checkForGroundCollision();
                break;

            case LinkEvent.LINK_STATE_UPDATED:
                sendLinkEventToListener(extras);
                return;
        }

        sendDroneEventToListeners(attributeEvent, extras);
    }

    private void sendDroneEventToListeners(final String attributeEvent, final Bundle extras) {
        if (droneListeners.isEmpty()) {
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DroneListener listener : droneListeners) {
                    try {
                        listener.onDroneEvent(attributeEvent, extras);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        });
    }

    private void sendLinkEventToListener(Bundle extras) {
        if (linkListener == null) {
            return;
        }

        if (extras != null) {
            final LinkConnectionStatus status = extras.getParcelable(LinkEventExtra.EXTRA_CONNECTION_STATUS);
            if (status != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        linkListener.onLinkStateUpdated(status);
                    }
                });
            }
        }
    }

    void notifyDroneServiceInterrupted(final String errorMsg) {
        if (droneListeners.isEmpty()) {
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                for (DroneListener listener : droneListeners)
                    listener.onDroneServiceInterrupted(errorMsg);
            }
        });
    }
}
