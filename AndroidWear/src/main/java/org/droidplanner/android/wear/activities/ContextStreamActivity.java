package org.droidplanner.android.wear.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

import org.droidplanner.R;
import org.droidplanner.android.lib.parcelables.ParcelableBattery;
import org.droidplanner.android.lib.parcelables.ParcelableGPS;
import org.droidplanner.android.lib.parcelables.ParcelableRadio;
import org.droidplanner.android.lib.utils.ParcelableUtils;
import org.droidplanner.android.lib.utils.WearUtils;
import org.droidplanner.android.wear.services.DroidPlannerWearService;

/**
 * Provides custom views for the context stream notification.
 */
public class ContextStreamActivity extends Activity {

    private static final IntentFilter sIntentFilter = new IntentFilter(DroidPlannerWearService
            .ACTION_RECEIVED_DATA);

    /**
     * Used to store, and retrieve the data bundle
     */
    private static final String EXTRA_DATA_BUNDLE = "extra_data_bundle";

    private final BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DroidPlannerWearService.ACTION_RECEIVED_DATA.equals(action)) {
                //update the ui.
                mDataBundle = intent.getBundleExtra(DroidPlannerWearService.EXTRA_RECEIVED_DATA);
                refreshUI();
            }
        }
    };

    private Bundle mDataBundle;
    private TextView mHomeInfo;
    private TextView mGpsInfo;
    private TextView mAirTimeInfo;
    private TextView mBatteryInfo;
    private TextView mRadioInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_context_stream);

        mHomeInfo = (TextView) findViewById(R.id.home_info);
        mGpsInfo = (TextView) findViewById(R.id.gps_info);
        mAirTimeInfo = (TextView) findViewById(R.id.air_time_info);
        mBatteryInfo = (TextView) findViewById(R.id.battery_info);
        mRadioInfo = (TextView) findViewById(R.id.radio_info);

        if (savedInstanceState != null) {
            mDataBundle = savedInstanceState.getBundle(EXTRA_DATA_BUNDLE);
        }

        refreshUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mDataBundle != null) {
            outState.putBundle(EXTRA_DATA_BUNDLE, mDataBundle);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        final Context context = getApplicationContext();

        LocalBroadcastManager.getInstance(context).registerReceiver(mDataReceiver, sIntentFilter);

        //Request data update from the service.
        startService(new Intent(context, DroidPlannerWearService.class).setAction
                (DroidPlannerWearService.ACTION_RECEIVED_DATA));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver
                (mDataReceiver);
    }

    private void refreshUI() {
        if (mDataBundle == null) {
            mHomeInfo.setText(R.string.empty_content);
            mGpsInfo.setText(R.string.empty_content);
            mAirTimeInfo.setText(R.string.empty_content);
            mBatteryInfo.setText(R.string.empty_content);
            mRadioInfo.setText(R.string.empty_content);
        }
        else {
            //Update the textviews within the view.
            String homeInfo = mDataBundle.getString(WearUtils.KEY_DRONE_HOME);
            if (homeInfo == null) {
                homeInfo = "0.0 m";
            }
            mHomeInfo.setText(homeInfo);

            final byte[] gpsByteArray = mDataBundle.getByteArray(WearUtils.KEY_DRONE_GPS);
            if (gpsByteArray != null) {
                final ParcelableGPS gpsInfo = ParcelableUtils.unmarshall(gpsByteArray,
                        ParcelableGPS.CREATOR);
                if (gpsInfo != null) {
                    final String gpsSummary = String.format("%s [ %d, %.1f ]",
                            gpsInfo.getFixType(), gpsInfo.getSatCount(), gpsInfo.getGpsEPH());
                    mGpsInfo.setText(gpsSummary);
                }
            }

            final long flightTime = mDataBundle.getLong(WearUtils.KEY_DRONE_FLIGHT_TIME);
            final long minutes = flightTime / 60;
            final long seconds = flightTime % 60;
            mAirTimeInfo.setText(String.format("%02d:%02d", minutes, seconds));

            final byte[] batteryByteArray = mDataBundle.getByteArray(WearUtils
                    .KEY_DRONE_BATTERY);
            if (batteryByteArray != null) {
                final ParcelableBattery batteryInfo = ParcelableUtils.unmarshall
                        (batteryByteArray, ParcelableBattery.CREATOR);
                if (batteryInfo != null) {
                    final String batterySummary = String.format("%2.0f%% [ %2.1fv, %2.1fA ]",
                            batteryInfo.getBattRemain(), batteryInfo.getBattVolt(),
                            batteryInfo.getBattCurrent());
                    mBatteryInfo.setText(batterySummary);
                }
            }

            final byte[] radioByteArray = mDataBundle.getByteArray(WearUtils.KEY_DRONE_SIGNAL);
            if (radioByteArray != null) {
                final ParcelableRadio radioInfo = ParcelableUtils.unmarshall(radioByteArray,
                        ParcelableRadio.CREATOR);
                if (radioInfo != null) {
                    mRadioInfo.setText(String.format("%d%%", radioInfo.getSignalStrength()));
                }
            }
        }
    }
}
