package com.o3dr.android.client.apis;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_BUILD_COMPLEX_MISSION_ITEM;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_CHANGE_MISSION_SPEED;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_GENERATE_DRONIE;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_GOTO_WAYPOINT;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_LOAD_MISSION;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_LOAD_WAYPOINTS;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_SAVE_MISSION;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_SET_MISSION;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.ACTION_START_MISSION;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.EXTRA_FORCE_ARM;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.EXTRA_FORCE_MODE_CHANGE;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.EXTRA_LOAD_MISSION_URI;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.EXTRA_MISSION;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.EXTRA_MISSION_ITEM_INDEX;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.EXTRA_MISSION_SPEED;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.EXTRA_PUSH_TO_DRONE;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.EXTRA_SAVE_MISSION_URI;
import static com.o3dr.services.android.lib.drone.mission.action.MissionActions.EXTRA_SET_LOADED_MISSION;

/**
 * Provides access to missions specific functionality.
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class MissionApi extends Api {

    private static final ConcurrentHashMap<Drone, MissionApi> missionApiCache = new ConcurrentHashMap<>();
    private static final Builder<MissionApi> apiBuilder = new Builder<MissionApi>() {
        @Override
        public MissionApi build(Drone drone) {
            return new MissionApi(drone);
        }
    };

    /**
     * Retrieves a MissionApi instance.
     * @param drone Target vehicle
     * @return a MissionApi instance.
     */
    public static MissionApi getApi(final Drone drone) {
        return getApi(drone, missionApiCache, apiBuilder);
    }

    private final Drone drone;

    private MissionApi(Drone drone){
        this.drone = drone;
    }

    /**
     * Generate action to create a dronie mission, and upload it to the connected drone.
     */
    public void generateDronie() {
        drone.performAsyncAction(new Action(ACTION_GENERATE_DRONIE));
    }

    /**
     * Generate action to update the mission property for the drone model in memory.
     *
     * @param mission     mission to upload to the drone.
     * @param pushToDrone if true, upload the mission to the connected device.
     */
    public void setMission(Mission mission, boolean pushToDrone) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_MISSION, mission);
        params.putBoolean(EXTRA_PUSH_TO_DRONE, pushToDrone);
        drone.performAsyncAction(new Action(ACTION_SET_MISSION, params));
    }

    /**
     * Starts the mission. The vehicle will only accept this command if armed and in Auto mode.
     * note: This command is only supported by APM:Copter V3.3 and newer.
     *
     * @param forceModeChange Change to Auto mode if not in Auto.
     * @param forceArm Arm the vehicle if it is disarmed.
     * @param listener
     */
    public void startMission(boolean forceModeChange, boolean forceArm, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putBoolean(EXTRA_FORCE_MODE_CHANGE, forceModeChange);
        params.putBoolean(EXTRA_FORCE_ARM, forceArm);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_START_MISSION, params), listener);
    }

    /**
     * Jump to the desired command in the mission list. Repeat this action only the specified number of times
     * @param waypoint command to jump to
     * @param listener
     */
    public void gotoWaypoint(int waypoint, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putInt(EXTRA_MISSION_ITEM_INDEX, waypoint);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_GOTO_WAYPOINT, params), listener);
    }

    /**
     * Load waypoints from the target vehicle.
     */
    public void loadWaypoints() {
        drone.performAsyncAction(new Action(ACTION_LOAD_WAYPOINTS));
    }

    /**
     * Loads the mission from the given source uri
     * @param sourceUri
     * @param loadingCallback Invoked when the loading operation completes.
     * @since 3.0.0
     */
    @Nullable
    public void loadMission(Uri sourceUri, LoadingCallback<Mission> loadingCallback) {
        loadAndSetMission(sourceUri, false, loadingCallback);
    }

    /**
     * Loads and sets the mission retrieved from the source uri.
     * @param sourceUri
     * @param loadingCallback Invoked when the loading operation completes.
     * @since 3.0.0
     */
    public void loadAndSetMission(Uri sourceUri, LoadingCallback<Mission> loadingCallback){
        loadAndSetMission(sourceUri, true, loadingCallback);
    }

    private void loadAndSetMission(final Uri sourceUri, final boolean setMission, final LoadingCallback<Mission> loadingCallback){
        if(sourceUri == null){
            throw new NullPointerException("Mission source uri must be non null.");
        }
        if(!setMission && loadingCallback == null){
            // No point to load the mission if no one is listening for it.
            return;
        }

        drone.getAsyncScheduler().execute(new Runnable() {
            @Override
            public void run() {
                postLoadingStart(loadingCallback);

                Bundle params = new Bundle();
                params.putParcelable(EXTRA_LOAD_MISSION_URI, sourceUri);
                params.putBoolean(EXTRA_SET_LOADED_MISSION, setMission);

                Action loadAction = new Action(ACTION_LOAD_MISSION, params);
                boolean result = drone.performAction(loadAction);
                if (loadingCallback != null) {
                    if (result) {
                        final Mission loadedMission = loadAction.getData().getParcelable(EXTRA_MISSION);
                        if (loadedMission == null) {
                            postLoadingFailed(loadingCallback);
                        }
                        else {
                            postLoadingComplete(loadedMission, loadingCallback);
                        }
                    } else {
                        postLoadingFailed(loadingCallback);
                    }
                }
            }
        });
    }

    private void postLoadingStart(final LoadingCallback<?> callback) {
        if(callback != null){
            drone.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    callback.onLoadingStart();
                }
            });
        }
    }

    private void postLoadingFailed(final LoadingCallback<?> callback){
        if(callback != null){
            drone.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    callback.onLoadingFailed();
                }
            });
        }
    }

    private <T> void postLoadingComplete(final T loaded, final LoadingCallback<T> callback) {
        if(callback != null){
            drone.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    callback.onLoadingComplete(loaded);
                }
            });
        }
    }

    /**
     * Saves a mission to the given save uri.
     * @param mission Mission to save
     * @param saveUri Destination uri for the mission
     * @param listener
     *
     * @since 3.0.0
     */
    public void saveMission(Mission mission, Uri saveUri, AbstractCommandListener listener){
        if(mission == null){
            throw new NullPointerException("Mission must be non null.");
        }
        if(saveUri == null){
            throw new NullPointerException("Mission destination uri must be non null.");
        }
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_MISSION, mission);
        params.putParcelable(EXTRA_SAVE_MISSION_URI, saveUri);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SAVE_MISSION, params), listener);
    }

    /**
     * Build and return complex mission item.
     * @param itemBundle bundle containing the complex mission item to update.
     */
    private Action buildComplexMissionItem(Bundle itemBundle) {
        Action payload = new Action(ACTION_BUILD_COMPLEX_MISSION_ITEM, itemBundle);
        boolean result = drone.performAction(payload);
        if(result)
            return payload;
        else
            return null;
    }

    /**
     * Builds and validates a complex mission item against the target vehicle.
     * @param complexItem Mission item to build.
     * @return an updated mission item.
     */
    public <T extends MissionItem> T buildMissionItem(MissionItem.ComplexItem<T> complexItem){
        T missionItem = (T) complexItem;
        Bundle payload = missionItem.getType().storeMissionItem(missionItem);
        if (payload == null)
            return null;

        Action result = buildComplexMissionItem(payload);
        if(result != null){
            T updatedItem = MissionItemType.restoreMissionItemFromBundle(result.getData());
            complexItem.copy(updatedItem);
            return (T) complexItem;
        }
        else
            return null;
    }

    /**
     * Stops the vehicle at the current location. The vehicle will remain in Auto mode
     * @param listener
     *
     * @since 2.8.0
     */
    public void pauseMission(AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putFloat(EXTRA_MISSION_SPEED, 0);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_CHANGE_MISSION_SPEED, params), listener);
    }

    /**
     * Sets the mission to a specified speed
     * @param speed Speed to set mission in m/s
     * @param listener
     *
     * @since 2.8.0
     */
    public void setMissionSpeed(float speed, AbstractCommandListener listener) {
        Bundle params = new Bundle();
        params.putFloat(EXTRA_MISSION_SPEED, speed);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_CHANGE_MISSION_SPEED, params), listener);
    }

    public interface LoadingCallback<T> {
        void onLoadingStart();
        void onLoadingComplete(T loaded);
        void onLoadingFailed();
    }
}
