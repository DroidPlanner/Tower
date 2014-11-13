package org.droidplanner.android.widgets.actionProviders;

import org.droidplanner.R;
import org.droidplanner.android.api.Drone;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.BatteryInfo;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.FlightModesInfo;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.FlightTimeInfo;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.GpsInfo;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.HomeInfo;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.PhoneExtraInfo;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.SignalInfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;

import com.ox3dr.services.android.lib.drone.event.Event;

/**
 * This implements the info bar displayed on the action bar after connection
 * with the drone.
 * <p/>
 * <b>Note:</b> The parent activity must add instantiations of this class to the
 * list of DroneEvent listeners.
 */
public class InfoBarActionProvider extends ActionProvider {

    private static final String TAG = InfoBarActionProvider.class.getSimpleName();

    private final static IntentFilter eventFilter = new IntentFilter();
    static {
        eventFilter.addAction(Event.EVENT_BATTERY);
        eventFilter.addAction(Event.EVENT_CONNECTED);
        eventFilter.addAction(Event.EVENT_DISCONNECTED);
        eventFilter.addAction(Event.EVENT_GPS);
        eventFilter.addAction(Event.EVENT_GPS_COUNT);
        eventFilter.addAction(Event.EVENT_GPS_FIX);
        eventFilter.addAction(Event.EVENT_HOME);
        eventFilter.addAction(Event.EVENT_RADIO);
        eventFilter.addAction(Event.EVENT_STATE);
        eventFilter.addAction(Event.EVENT_VEHICLE_MODE);
        eventFilter.addAction(Event.EVENT_TYPE_UPDATED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            boolean updateExtra = true;

            if(Event.EVENT_BATTERY.equals(action)){
                if (mBatteryInfo != null)
                    mBatteryInfo.updateItemView(mContext, mDrone);
            }
            else if(Event.EVENT_CONNECTED.equals(action)){
                updateInfoBar();
                updateExtra = false;
            }
            else if(Event.EVENT_DISCONNECTED.equals(action)){
                setDrone(null);
                updateInfoBar();
                updateExtra = false;
            }
            else if(Event.EVENT_GPS.equals(action) || Event.EVENT_HOME.equals(action)){
                if (mHomeInfo != null)
                    mHomeInfo.updateItemView(mContext, mDrone);
            }
            else if(Event.EVENT_GPS_COUNT.equals(action) || Event.EVENT_GPS_FIX.equals(action)){
                if (mGpsInfo != null)
                    mGpsInfo.updateItemView(mContext, mDrone);
            }
            else if(Event.EVENT_RADIO.equals(action)){
                if (mSignalInfo != null)
                    mSignalInfo.updateItemView(mContext, mDrone);
            }
            else if(Event.EVENT_STATE.equals(action)){
                if (mFlightTimeInfo != null)
                    mFlightTimeInfo.updateItemView(mContext, mDrone);
            }
            else if(Event.EVENT_VEHICLE_MODE.equals(action) || Event.EVENT_TYPE_UPDATED.equals(action)){
                if (mFlightModesInfo != null)
                    mFlightModesInfo.updateItemView(mContext, mDrone);
            }
            else{
                updateExtra = false;
            }

            if (mPhoneExtraInfo != null && updateExtra) {
                mPhoneExtraInfo.updateItemView(mContext, mDrone);
            }
        }
    };

	/**
	 * Application context.
	 */
	private final Context mContext;

	/**
	 * Handle to the drone api.
	 */
	private Drone mDrone;

	/**
	 * Action provider's view.
	 */
	private View mView;

	/*
	 * Info bar items
	 */
	private HomeInfo mHomeInfo;
	private GpsInfo mGpsInfo;
	private BatteryInfo mBatteryInfo;
	private FlightTimeInfo mFlightTimeInfo;
	private SignalInfo mSignalInfo;
	private FlightModesInfo mFlightModesInfo;
	private PhoneExtraInfo mPhoneExtraInfo;

	public InfoBarActionProvider(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public View onCreateActionView() {
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		mView = inflater.inflate(R.layout.action_provider_info_bar, null);

		setupActionView();
		updateInfoBar();

		return mView;
	}

	/**
	 * This is used to update the current drone state.
	 * 
	 * @param drone
	 */
	public void setDrone(Drone drone) {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(mContext);
        if(drone == null) {
            if(mDrone != null) {
                lbm.unregisterReceiver(eventReceiver);
                updateInfoBar();
            }
        }
        else {
            updateInfoBar();
            lbm.registerReceiver(eventReceiver, eventFilter);
        }

        mDrone = drone;
	}

	/**
	 * Go through the list of action bar item, and initialize their view.
	 */
	private void setupActionView() {
		mHomeInfo = new HomeInfo(mContext, mView, mDrone);
		mGpsInfo = new GpsInfo(mContext, mView, mDrone);
		mBatteryInfo = new BatteryInfo(mContext, mView, mDrone);
		mFlightTimeInfo = new FlightTimeInfo(mContext, mView, mDrone);
		mSignalInfo = new SignalInfo(mContext, mView, mDrone);
		mFlightModesInfo = new FlightModesInfo(mContext, mView, mDrone);
		mPhoneExtraInfo = new PhoneExtraInfo(mContext, mView, mDrone);
	}

	/**
	 * This updates the info bar with the current drone state.
	 */
	private void updateInfoBar() {
		if (mHomeInfo != null)
			mHomeInfo.updateItemView(mContext, mDrone);

		if (mGpsInfo != null)
			mGpsInfo.updateItemView(mContext, mDrone);

		if (mBatteryInfo != null)
			mBatteryInfo.updateItemView(mContext, mDrone);

		if (mFlightTimeInfo != null)
			mFlightTimeInfo.updateItemView(mContext, mDrone);

		if (mSignalInfo != null)
			mSignalInfo.updateItemView(mContext, mDrone);

		if (mFlightModesInfo != null)
			mFlightModesInfo.updateItemView(mContext, mDrone);

		if (mPhoneExtraInfo != null)
			mPhoneExtraInfo.updateItemView(mContext, mDrone);
	}
}
