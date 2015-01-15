package org.droidplanner.android.fragments.actionbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.util.MathUtils;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.utils.unit.UnitManager;
import org.droidplanner.android.widgets.spinners.ModeAdapter;
import org.droidplanner.android.widgets.spinners.SpinnerSelfSelect;

import java.util.List;
import java.util.Locale;

/**
 * Created by Fredia Huya-Kouadio on 1/14/15.
 */
public class ActionBarTelemFragment extends ApiListenerFragment {

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
        eventFilter.addAction(AttributeEvent.STATE_VEHICLE_MODE);
        eventFilter.addAction(AttributeEvent.TYPE_UPDATED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(getActivity() == null)
                return;

            switch (intent.getAction()) {
                case AttributeEvent.BATTERY_UPDATED:
                    updateBatteryTelem();
                    break;

                case AttributeEvent.STATE_CONNECTED:
                    showTelemBar();
                    updateAllTelem();
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                    hideTelemBar();
                    break;

                case AttributeEvent.GPS_POSITION:
                case AttributeEvent.HOME_UPDATED:
                    updateHomeTelem();
                    break;

                case AttributeEvent.GPS_COUNT:
                case AttributeEvent.GPS_FIX:
                    updateGpsTelem();
                    break;

                case AttributeEvent.SIGNAL_UPDATED:
                    updateSignalTelem();
                    break;

                case AttributeEvent.STATE_VEHICLE_MODE:
                case AttributeEvent.TYPE_UPDATED:
                    updateFlightModeTelem();
                    break;

                default:
                    break;
            }
        }


    };

    private DroidPlannerPrefs appPrefs;

    private TextView homeTelem;
    private TextView gpsTelem;

    private TextView batteryTelem;
    private PopupWindow batteryPopup;

    private TextView signalTelem;
    private PopupWindow signalPopup;

    private SpinnerSelfSelect flightModeTelem;
    private int lastDroneType = -1;
    private ModeAdapter modeAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_action_bar_telem, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Context context = getActivity().getApplicationContext();
        final LayoutInflater inflater = LayoutInflater.from(context);

        final int popupWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        final int popupHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        final Drawable popupBg = getResources().getDrawable(R.drawable.panel_white_bg);

        homeTelem = (TextView) view.findViewById(R.id.bar_home);
        gpsTelem = (TextView) view.findViewById(R.id.bar_gps);

        batteryTelem = (TextView) view.findViewById(R.id.bar_battery);
        final View batteryPopupView = inflater.inflate(R.layout.popup_info_power, null);
        batteryPopup = new PopupWindow(batteryPopupView, popupWidth, popupHeight, true);
        batteryPopup.setBackgroundDrawable(popupBg);
        batteryTelem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batteryPopup.showAsDropDown(batteryTelem);
            }
        });

        signalTelem = (TextView) view.findViewById(R.id.bar_signal);
        final View signalPopupView = inflater.inflate(R.layout.popup_info_signal, null);
        signalPopup = new PopupWindow(signalPopupView, popupWidth, popupHeight, true);
        signalPopup.setBackgroundDrawable(popupBg);
        signalTelem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signalPopup.showAsDropDown(signalTelem);
            }
        });

        flightModeTelem = (SpinnerSelfSelect) view.findViewById(R.id.bar_flight_mode);
        modeAdapter = new ModeAdapter(context, R.layout.spinner_drop_down_flight_mode);

        appPrefs = new DroidPlannerPrefs(context);
    }

    @Override
    public void onApiConnected() {
        final Drone drone = getDrone();
        if(drone.isConnected())
            showTelemBar();
        else
            hideTelemBar();

        flightModeTelem.setAdapter(modeAdapter);
        flightModeTelem.setOnSpinnerItemSelectedListener(new SpinnerSelfSelect.OnSpinnerItemSelectedListener() {
            @Override
            public void onSpinnerItemSelected(Spinner spinner, int position) {
                final Drone drone = getDrone();
                if (drone.isConnected()) {
                    final VehicleMode newMode = (VehicleMode) spinner.getItemAtPosition(position);
                    drone.changeVehicleMode(newMode);

                    //Record the attempt to change flight modes
                    final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                            .setCategory(GAUtils.Category.FLIGHT)
                            .setAction("Flight mode changed")
                            .setLabel(newMode.getLabel());
                    GAUtils.sendEvent(eventBuilder);
                }
            }
        });

        updateAllTelem();
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    private void showTelemBar() {
        final View view = getView();
        if(view != null)
            view.setVisibility(View.VISIBLE);
    }

    private void hideTelemBar(){
        final View view = getView();
        if(view != null)
            view.setVisibility(View.INVISIBLE);
    }

    private void updateAllTelem() {
        updateFlightModeTelem();
        updateSignalTelem();
        updateGpsTelem();
        updateHomeTelem();
        updateBatteryTelem();
    }

    private void updateFlightModeTelem() {
        final Drone drone = getDrone();

        final boolean isDroneConnected = drone.isConnected();
        final int droneType;
        if (isDroneConnected) {
            Type type = drone.getAttribute(AttributeType.TYPE);
            droneType = type.getDroneType();
        } else {
            droneType = -1;
        }

        if (droneType != lastDroneType) {
            final List<VehicleMode> flightModes = VehicleMode.getVehicleModePerDroneType(droneType);

            modeAdapter.clear();
            modeAdapter.addAll(flightModes);
            modeAdapter.notifyDataSetChanged();

            lastDroneType = droneType;
        }

        if (isDroneConnected) {
            final State droneState = drone.getAttribute(AttributeType.STATE);
            flightModeTelem.forcedSetSelection(modeAdapter.getPosition(droneState.getVehicleMode()));
        }
    }

    private void updateSignalTelem() {
        final Drone drone = getDrone();

        final View popupView = signalPopup.getContentView();
        TextView rssiView = (TextView) popupView.findViewById(R.id.bar_signal_rssi);
        TextView remRssiView = (TextView) popupView.findViewById(R.id.bar_signal_remrssi);
        TextView noiseView = (TextView) popupView.findViewById(R.id.bar_signal_noise);
        TextView remNoiseView = (TextView) popupView.findViewById(R.id.bar_signal_remnoise);
        TextView fadeView = (TextView) popupView.findViewById(R.id.bar_signal_fade);
        TextView remFadeView = (TextView) popupView.findViewById(R.id.bar_signal_remfade);

        final Signal droneSignal = drone.getAttribute(AttributeType.SIGNAL);
        if(!drone.isConnected() || !droneSignal.isValid()){
            final String defaultValue = getString(R.string.empty_content);

            signalTelem.setText(defaultValue);
            signalTelem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_signal_wifi_statusbar_null_black_24dp,
                    0, 0, 0);

            rssiView.setText(defaultValue);
            remRssiView.setText(defaultValue);
            noiseView.setText(defaultValue);
            remNoiseView.setText(defaultValue);
            fadeView.setText(defaultValue);
            remFadeView.setText(defaultValue);
        }
        else{
            final int signalStrength = MathUtils.getSignalStrength(droneSignal.getFadeMargin(),
                    droneSignal.getRemFadeMargin());
            final int signalIcon;
            if (signalStrength >= 100)
                signalIcon = R.drawable.ic_signal_wifi_4_bar_black_24dp;
            else if (signalStrength >= 75)
                signalIcon = R.drawable.ic_signal_wifi_3_bar_black_24dp;
            else if (signalStrength >= 50)
                signalIcon = R.drawable.ic_signal_wifi_2_bar_black_24dp;
            else if (signalStrength >= 25)
                signalIcon = R.drawable.ic_signal_wifi_1_bar_black_24dp;
            else
                signalIcon = R.drawable.ic_signal_wifi_0_bar_black_24dp;

            signalTelem.setText(String.format(Locale.ENGLISH, "%d%%", signalStrength));
            signalTelem.setCompoundDrawablesWithIntrinsicBounds(signalIcon, 0, 0, 0);

            rssiView.setText(String.format("RSSI %2.0f dB", droneSignal.getRssi()));
            remRssiView.setText(String.format("RemRSSI %2.0f dB", droneSignal.getRemrssi()));
            noiseView.setText(String.format("Noise %2.0f dB", droneSignal.getNoise()));
            remNoiseView.setText(String.format("RemNoise %2.0f dB", droneSignal.getRemnoise()));
            fadeView.setText(String.format("Fade %2.0f dB", droneSignal.getFadeMargin()));
            remFadeView.setText(String.format("RemFade %2.0f dB", droneSignal.getRemFadeMargin()));
        }

        signalPopup.update();
    }

    private void updateGpsTelem() {
        final Drone drone = getDrone();

        final String update;
        final int gpsIcon;
        if (!drone.isConnected()) {
            update = getString(R.string.empty_content);
            gpsIcon = R.drawable.ic_gps_off_black_24dp;
        } else {
            Gps droneGps = drone.getAttribute(AttributeType.GPS);
            final String fixStatus = droneGps.getFixStatus();

            if (appPrefs.shouldGpsHdopBeDisplayed()) {
                update = String.format(Locale.ENGLISH, "%.1f", droneGps.getGpsEph());
            } else {
                update = String.format(Locale.ENGLISH, "%s", fixStatus);
            }

            switch(fixStatus){
                case Gps.LOCK_3D:
                    gpsIcon = R.drawable.ic_gps_fixed_black_24dp;
                    break;

                case Gps.LOCK_2D:
                case Gps.NO_FIX:
                default:
                    gpsIcon = R.drawable.ic_gps_not_fixed_black_24dp;
                    break;
            }
        }

        gpsTelem.setText(update);
        gpsTelem.setCompoundDrawablesWithIntrinsicBounds(gpsIcon, 0, 0, 0);
    }

    private void updateHomeTelem() {
        final Drone drone = getDrone();

        String update = getString(R.string.empty_content);
        if (drone.isConnected()) {
            final Gps droneGps = drone.getAttribute(AttributeType.GPS);
            final Home droneHome = drone.getAttribute(AttributeType.HOME);
            if (droneGps.isValid() && droneHome.isValid()) {
                double distanceToHome = MathUtils.getDistance(droneHome.getCoordinate(), droneGps.getPosition());
                update = String.format("%s", UnitManager.getUnitProvider().distanceToString(distanceToHome));
            }
        }

        homeTelem.setText(update);
    }

    private void updateBatteryTelem() {
        final Drone drone = getDrone();

        final View batteryPopupView = batteryPopup.getContentView();
        final TextView currentView = (TextView) batteryPopupView.findViewById(R.id.bar_power_current);
        final TextView mAhView = (TextView) batteryPopupView.findViewById(R.id.bar_power_mAh);

        String update;
        Battery droneBattery;
        final int batteryIcon;
        if (!drone.isConnected() || ((droneBattery = drone.getAttribute(AttributeType.BATTERY)) == null)) {
            update = getString(R.string.empty_content);
            currentView.setText(R.string.empty_content);
            mAhView.setText(R.string.empty_content);
            batteryIcon = R.drawable.ic_battery_unknown_black_24dp;
        } else {
            Double discharge = droneBattery.getBatteryDischarge();
            String dischargeText;
            if (discharge == null) {
                dischargeText = getString(R.string.empty_content);
            } else {
                dischargeText = UnitManager.getUnitProvider().electricChargeToString(discharge);
            }

            mAhView.setText(String.format(Locale.ENGLISH, "Remaining %2.0f%%", droneBattery.getBatteryRemain()));
            currentView.setText(String.format("Current %2.1f A", droneBattery.getBatteryCurrent()));

            update = String.format(Locale.ENGLISH, "%2.1fv\n", droneBattery.getBatteryVoltage()).concat(dischargeText);
            batteryIcon = R.drawable.ic_battery_std_black_24dp;
        }

        batteryPopup.update();
        batteryTelem.setText(update);
        batteryTelem.setCompoundDrawablesWithIntrinsicBounds(batteryIcon, 0, 0, 0);
    }
}
