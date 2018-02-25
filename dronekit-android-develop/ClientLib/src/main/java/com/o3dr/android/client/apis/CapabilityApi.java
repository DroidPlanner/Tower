package com.o3dr.android.client.apis;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.action.CapabilityActions.*;

/**
 * Allows to query the capabilities offered by the vehicle.
 * Created by Fredia Huya-Kouadio on 7/5/15.
 */
public class CapabilityApi extends Api {

    /**
     * Feature support check error. The drone is disconnected.
     */
    public static final int FEATURE_ERROR_DRONE_DISCONNECTED = -1;

    /**
     * Feature support check result. Indicate the feature is supported.
     */
    public static final int FEATURE_SUPPORTED = 0;

    /**
     * Feature support check result. Indicate the feature is not supported.
     */
    public static final int FEATURE_UNSUPPORTED = 1;

    private static final ConcurrentHashMap<Drone, CapabilityApi> capabilityApiCache = new ConcurrentHashMap<>();
    private static final Builder<CapabilityApi> apiBuilder = new Builder<CapabilityApi>() {
        @Override
        public CapabilityApi build(Drone drone) {
            return new CapabilityApi(drone);
        }
    };

    /**
     * Retrieves a capability api instance.
     * @param drone target vehicle.
     * @return a CapabilityApi instance.
     */
    public static CapabilityApi getApi(final Drone drone){
        return getApi(drone, capabilityApiCache, apiBuilder);
    }

    private final Drone drone;

    private CapabilityApi(Drone drone){
        this.drone = drone;
    }

    /**
     * Determine whether the given feature is supported.
     * @param featureId Id of the feature to check.
     * @param resultListener Callback to receive feature support status.
     */
    public void checkFeatureSupport(final String featureId, final FeatureSupportListener resultListener){
        if(TextUtils.isEmpty(featureId) || resultListener == null)
            return;

        if(!drone.isConnected()){
            drone.post(new Runnable() {
                @Override
                public void run() {
                    resultListener.onFeatureSupportResult(featureId, FEATURE_ERROR_DRONE_DISCONNECTED, null);
                }
            });
            return;
        }

        switch(featureId){
            case FeatureIds.IMU_CALIBRATION:
                    drone.post(new Runnable() {
                        @Override
                        public void run() {
                            resultListener.onFeatureSupportResult(featureId, FEATURE_SUPPORTED, null);
                        }
                    });
                break;

            case FeatureIds.SOLO_VIDEO_STREAMING:
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    drone.post(new Runnable() {
                        @Override
                        public void run() {
                            resultListener.onFeatureSupportResult(featureId, FEATURE_UNSUPPORTED, null);
                        }
                    });
                    break;
                }
            //********FALL THROUGH ***********//
            case FeatureIds.COMPASS_CALIBRATION:
            case FeatureIds.KILL_SWITCH:
                final Bundle params = new Bundle();
                params.putString(EXTRA_FEATURE_ID, featureId);
                drone.performAsyncActionOnDroneThread(new Action(ACTION_CHECK_FEATURE_SUPPORT, params),
                        new AbstractCommandListener() {
                    @Override
                    public void onSuccess() {
                        resultListener.onFeatureSupportResult(featureId, FEATURE_SUPPORTED, null);
                    }

                    @Override
                    public void onError(int executionError) {
                        resultListener.onFeatureSupportResult(featureId, FEATURE_UNSUPPORTED, null);
                    }

                    @Override
                    public void onTimeout() {
                        resultListener.onFeatureSupportResult(featureId, FEATURE_UNSUPPORTED, null);
                    }
                });
                break;

            default:
                    drone.post(new Runnable() {
                        @Override
                        public void run() {
                            resultListener.onFeatureSupportResult(featureId, FEATURE_UNSUPPORTED, null);
                        }
                    });
                break;
        }
    }

    /**
     * Defines the set of feature ids.
     */
    public static final class FeatureIds {

        /**
         * Id for the video feature.
         */
        public static final String SOLO_VIDEO_STREAMING = "feature_solo_video_streaming";

        /**
         * Id for the compass calibration feature.
         */
        public static final String COMPASS_CALIBRATION = "feature_compass_calibration";

        /**
         * Id for the imu calibration feature.
         */
        public static final String IMU_CALIBRATION = "feature_imu_calibration";

        /**
         * Id for the kill switch feature.
         */
        public static final String KILL_SWITCH = "feature_kill_switch";

        //Private to prevent instantiation.
        private FeatureIds(){}
    }

    public interface FeatureSupportListener {

        /**
         * Callback for the result from checking feature support.
         * @param featureId Id of the feature for which we're checking support.
         * @param result Result of the feature support check.
         * @param resultInfo Additional info about the level of support for the feature. Can be null.
         */
        void onFeatureSupportResult(String featureId, int result, Bundle resultInfo);
    }

}
