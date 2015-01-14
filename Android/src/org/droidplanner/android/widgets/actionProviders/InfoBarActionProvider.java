package org.droidplanner.android.widgets.actionProviders;

import org.droidplanner.android.R;
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
import android.support.v4.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;

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
        eventFilter.addAction(AttributeEvent.BATTERY_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
        eventFilter.addAction(AttributeEvent.GPS_POSITION);
        eventFilter.addAction(AttributeEvent.GPS_COUNT);
        eventFilter.addAction(AttributeEvent.GPS_FIX);
        eventFilter.addAction(AttributeEvent.HOME_UPDATED);
        eventFilter.addAction(AttributeEvent.SIGNAL_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_VEHICLE_MODE);
        eventFilter.addAction(AttributeEvent.TYPE_UPDATED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            boolean updateExtra = true;

            switch (action) {
                case AttributeEvent.BATTERY_UPDATED:
                    if (mBatteryInfo != null)
                        mBatteryInfo.updateItemView(mContext, mDrone);
                    break;
                case AttributeEvent.STATE_CONNECTED:
                    updateInfoBar();
                    updateExtra = false;
                    break;
                case AttributeEvent.STATE_DISCONNECTED:
                    setDrone(null);
                    updateInfoBar();
                    updateExtra = false;
                    break;
                case AttributeEvent.GPS_POSITION:
                case AttributeEvent.HOME_UPDATED:
                    if (mHomeInfo != null)
                        mHomeInfo.updateItemView(mContext, mDrone);
                    break;
                case AttributeEvent.GPS_COUNT:
                case AttributeEvent.GPS_FIX:
                    if (mGpsInfo != null)
                        mGpsInfo.updateItemView(mContext, mDrone);
                    break;
                case AttributeEvent.SIGNAL_UPDATED:
                    if (mSignalInfo != null)
                        mSignalInfo.updateItemView(mContext, mDrone);
                    break;
                case AttributeEvent.STATE_UPDATED:
                    if (mFlightTimeInfo != null)
                        mFlightTimeInfo.updateItemView(mContext, mDrone);
                    break;
                case AttributeEvent.STATE_VEHICLE_MODE:
                case AttributeEvent.TYPE_UPDATED:
                    if (mFlightModesInfo != null)
                        mFlightModesInfo.updateItemView(mContext, mDrone);
                    break;
                default:
                    updateExtra = false;
                    break;
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
