package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.gcs.follow.FollowLocationSource;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_DISABLE_FOLLOW_ME;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_ENABLE_FOLLOW_ME;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_NEW_EXTERNAL_LOCATION;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.ACTION_UPDATE_FOLLOW_PARAMS;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.EXTRA_FOLLOW_TYPE;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.EXTRA_LOCATION;
import static com.o3dr.services.android.lib.gcs.action.FollowMeActions.EXTRA_LOCATION_SOURCE;

/**
 * Provides access to the Follow me api.
 * Created by Fredia Huya-Kouadio on 1/19/15.
 */
public class FollowApi extends Api {

    private static final ConcurrentHashMap<Drone, FollowApi> followApiCache = new ConcurrentHashMap<>();
    private static final Builder<FollowApi> apiBuilder = new Builder<FollowApi>() {
        @Override
        public FollowApi build(Drone drone) {
            return new FollowApi(drone);
        }
    };

    /**
     * Retrieves a FollowApi instance.
     *
     * @param drone target vehicle
     * @return a FollowApi instance.
     */
    public static FollowApi getApi(final Drone drone) {
        return getApi(drone, followApiCache, apiBuilder);
    }

    private final Drone drone;

    private FollowApi(Drone drone) {
        this.drone = drone;
    }

    /**
     * Enables follow-me if disabled.
     *
     * @param type follow-me mode to use.
     * @param source The location source to use
     */
    public void enableFollowMe(FollowType type, FollowLocationSource source) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_FOLLOW_TYPE, type);
        params.putParcelable(EXTRA_LOCATION_SOURCE, source);
        drone.performAsyncAction(new Action(ACTION_ENABLE_FOLLOW_ME, params));
    }

    public void enableFollowMe(FollowType type) {
        enableFollowMe(type, FollowLocationSource.INTERNAL);
    }

    /**
     * Updates the parameters for the currently enabled follow me mode.
     *
     * @param params Set of parameters for the current follow me mode.
     */
    public void updateFollowParams(Bundle params) {
        drone.performAsyncAction(new Action(ACTION_UPDATE_FOLLOW_PARAMS, params));
    }

    /**
     * Disables follow me is enabled.
     */
    public void disableFollowMe() {
        drone.performAsyncAction(new Action(ACTION_DISABLE_FOLLOW_ME));
    }

    /**
     * A new FollowLocation for the drone to follow.
     */
    public void updateLocation(android.location.Location loc) {
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_LOCATION, loc);
        drone.performAsyncAction(new Action(ACTION_NEW_EXTERNAL_LOCATION, params));
    }
}
