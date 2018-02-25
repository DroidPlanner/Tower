package co.aerobotics.android.data;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import co.aerobotics.android.DroidPlannerApp;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.GPSSignalLevel;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

/**
 * Created by aerobotics on 2017/05/31.
 */

public class DJIFlightControllerState {
    public static final String FLAG_CONTROLLER_STATE = "dji_sdk_state_change";
    private FlightControllerState state;
    private static DJIFlightControllerState instance = null;
    private Context context;
    private Handler mHandler;
    private FlightController mFlightController = null;
    public int[] firmwareVersionArray;
    private FlightAssistant mFlightAssistant = null;


    private DJIFlightControllerState() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized DJIFlightControllerState getInstance(){
        if (instance == null){
            instance = new DJIFlightControllerState();
        }
        return instance;
    }

    public void unregisterCallback(){
        FlightController mFlightController = null;
        BaseProduct product = DroidPlannerApp.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }
        if (mFlightController != null) {
            mFlightController.setStateCallback(null);
        }
    }
    public void initFlightController(final Context context) {
        this.context = context;
        BaseProduct product = DroidPlannerApp.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }
        if (mFlightController != null) {
            runFirmwareRunnable();
            mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(@NonNull FlightControllerState djiFlightControllerCurrentState) {
                    setFlightControllerState(djiFlightControllerCurrentState);
                    Log.i(FLAG_CONTROLLER_STATE, "state callback: " + Thread.currentThread().getName());
                    notifyStatusChange();
                }
            });
        }
        if (mFlightController != null) {
            mFlightAssistant = mFlightController.getFlightAssistant();
        }
    }

    private void runFirmwareRunnable(){
        mHandler.removeCallbacks(firmwareRunnable);
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(firmwareRunnable, 2000);
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 100);
    }

    private Runnable firmwareRunnable = new Runnable() {
        @Override
        public void run() {
            mFlightController.getFirmwareVersion(new CommonCallbacks.CompletionCallbackWith<String>() {
                @Override
                public void onSuccess(String s) {
                    //Toast.makeText(context, s, Toast.LENGTH_LONG).show();
                    getFirmwareVersion(s);
                }

                @Override
                public void onFailure(DJIError djiError) {

                }
            });
        }
    };

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONTROLLER_STATE);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    };

    private synchronized void getFirmwareVersion(String s){
        String[] strArray = s.split("\\.");
        int [] intArray = new int[strArray.length];
        for (int i =0; i < strArray.length; i++){
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        firmwareVersionArray  = intArray;
    }

    public synchronized FlightControllerState getFlightControllerState(){
        return this.state;
    }

    private synchronized void setFlightControllerState(FlightControllerState state){
        this.state = state;
    }

    public synchronized double getDroneLongitude(){
        return state.getAircraftLocation().getLongitude();
    }

    public synchronized double getDroneLatitude(){
        return state.getAircraftLocation().getLatitude();
    }

    public synchronized double getDroneAltitude(){
        return state.getAircraftLocation().getAltitude();
    }

    public synchronized double getDroneYaw(){
        return state.getAttitude().yaw;
    }

    public synchronized double getHomeLatitude(){
        return state.getHomeLocation().getLatitude();
    }

    public synchronized double getHomeLongitude(){
        return state.getHomeLocation().getLongitude();
    }

    public synchronized GPSSignalLevel getGpsSignalLevel(){
        return state.getGPSSignalLevel();
    }

    public synchronized int getSatelliteCount(){
        return state.getSatelliteCount();
    }

    public synchronized String getFlightModeString(){
        return state.getFlightModeString();
    }

    public synchronized boolean isHomePosSet(){
        return state.isHomeLocationSet();
    }

    public synchronized FlightAssistant getFlightAssistant() {
        return mFlightAssistant;
    }

}
