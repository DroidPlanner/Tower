package org.droidplanner.android.widgets.actionProviders;

import org.droidplanner.R;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.BatteryInfo;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.FlightModesInfo;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.FlightTimeInfo;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.GpsInfo;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.HomeInfo;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.PhoneExtraInfo;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem.SignalInfo;

import android.content.Context;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;

/**
 * This implements the info bar displayed on the action bar after connection
 * with the drone.
 * <p/>
 * <b>Note:</b> The parent activity must add instantiations of this class to the
 * list of DroneEvent listeners.
 */
public class InfoBarActionProvider extends ActionProvider implements
		OnDroneListener {

	/**
	 * Application context.
	 */
	private final Context mContext;

	/**
	 * Current drone state.
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
		mDrone = drone;
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
		setDrone(drone);

		boolean updateExtra = true;
		switch (event) {
		case BATTERY:
			if (mBatteryInfo != null)
				mBatteryInfo.updateItemView(mContext, mDrone);
			break;

		case CONNECTED:
			updateInfoBar();
			updateExtra = false;
			break;

		case DISCONNECTED:
			setDrone(null);
			updateInfoBar();
			updateExtra = false;
			break;

		case GPS_FIX:
		case GPS_COUNT:
			if (mGpsInfo != null)
				mGpsInfo.updateItemView(mContext, mDrone);
			break;

		case HOME:
			if (mHomeInfo != null)
				mHomeInfo.updateItemView(mContext, mDrone);
			break;

		case RADIO:
			if (mSignalInfo != null)
				mSignalInfo.updateItemView(mContext, mDrone);
			break;

		case STATE:
			if (mFlightTimeInfo != null)
				mFlightTimeInfo.updateItemView(mContext, mDrone);
			break;

		case MODE:
		case TYPE:
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
