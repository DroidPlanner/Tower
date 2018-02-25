package co.aerobotics.android.utils;

import co.aerobotics.android.DroidPlannerApp;

import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;

/**
 * Created by dji on 16/1/6.
 */
public class ModuleVerificationUtil {
    public static boolean isProductModuleAvailable() {
        return (null != DroidPlannerApp.getProductInstance());
    }

    public static boolean isAircraft() {
        return DroidPlannerApp.getProductInstance() instanceof Aircraft;
    }

    public static boolean isHandHeld() {
        return DroidPlannerApp.getProductInstance() instanceof HandHeld;
    }

    public static boolean isCameraModuleAvailable() {
        return isProductModuleAvailable() && (null != DroidPlannerApp.getProductInstance().getCamera());
    }

    public static boolean isPlaybackAvailable() {
        return isCameraModuleAvailable() && (null != DroidPlannerApp.getProductInstance()
                                                                         .getCamera()
                                                                         .getPlaybackManager());
    }

    public static boolean isMediaManagerAvailable() {
        return isCameraModuleAvailable() && (null != DroidPlannerApp.getProductInstance()
                                                                         .getCamera()
                                                                         .getMediaManager());
    }

    public static boolean isRemoteControllerAvailable() {
        return isProductModuleAvailable() && isAircraft() && (null != DroidPlannerApp.getAircraftInstance()
                                                                                          .getRemoteController());
    }

    public static boolean isFlightControllerAvailable() {
        return isProductModuleAvailable() && isAircraft() && (null != DroidPlannerApp.getAircraftInstance()
                                                                                          .getFlightController());
    }

    public static boolean isCompassAvailable() {
        return isFlightControllerAvailable() && isAircraft() && (null != DroidPlannerApp.getAircraftInstance()
                                                                                             .getFlightController()
                                                                                             .getCompass());
    }

    public static boolean isFlightLimitationAvailable() {
        return isFlightControllerAvailable() && isAircraft();
    }

    public static boolean isGimbalModuleAvailable() {
        return isProductModuleAvailable() && (null != DroidPlannerApp.getProductInstance().getGimbal());
    }

    public static boolean isAirlinkAvailable() {
        return isProductModuleAvailable() && (null != DroidPlannerApp.getProductInstance().getAirLink());
    }

    public static boolean isWiFiLinkAvailable() {
        return isAirlinkAvailable() && (null != DroidPlannerApp.getProductInstance().getAirLink().getWiFiLink());
    }

    public static boolean isLightbridgeLinkAvailable() {
        return isAirlinkAvailable() && (null != DroidPlannerApp.getProductInstance()
                                                                    .getAirLink()
                                                                    .getLightbridgeLink());
    }
}
