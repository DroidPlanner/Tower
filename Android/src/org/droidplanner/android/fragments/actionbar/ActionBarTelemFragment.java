package org.droidplanner.android.fragments.actionbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Signal;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.returnToMe.ReturnToMeState;
import com.o3dr.services.android.lib.util.MathUtils;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.fragments.SettingsFragment;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.view.spinners.ModeAdapter;
import org.droidplanner.android.view.spinners.SpinnerSelfSelect;

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
        eventFilter.addAction(AttributeEvent.SIGNAL_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_VEHICLE_MODE);
        eventFilter.addAction(AttributeEvent.TYPE_UPDATED);
        eventFilter.addAction(AttributeEvent.ALTITUDE_UPDATED);

        eventFilter.addAction(SettingsFragment.ACTION_PREF_HDOP_UPDATE);
        eventFilter.addAction(SettingsFragment.ACTION_PREF_UNIT_SYSTEM_UPDATE);

        eventFilter.addAction(AttributeEvent.RETURN_TO_ME_STATE_UPDATE);
        eventFilter.addAction(AttributeEvent.HOME_UPDATED);
    }

    private final Runnable resetHomeTelemContainerBg = new Runnable() {
        @Override
        public void run() {
            if(homeTelemContainer != null)
                homeTelemContainer.setBackgroundDrawable(null);
        }
    };

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
                    updateAllTelem();
                    break;

                case AttributeEvent.RETURN_TO_ME_STATE_UPDATE:
                    final @ReturnToMeState.ReturnToMeStates int state = intent.getIntExtra(AttributeEventExtra.EXTRA_RETURN_TO_ME_STATE, ReturnToMeState.STATE_IDLE);
                    if(state == ReturnToMeState.STATE_UPDATING_HOME){
                        //Change the home telem background for 1 second
                        if(homeTelemContainer != null){
                            handler.removeCallbacks(resetHomeTelemContainerBg);
                            homeTelemContainer.setBackgroundColor(Color.YELLOW);
                            handler.postDelayed(resetHomeTelemContainerBg, 1000l);
                        }
                    }
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

                case SettingsFragment.ACTION_PREF_HDOP_UPDATE:
                    updateGpsTelem();
                    break;

                case SettingsFragment.ACTION_PREF_UNIT_SYSTEM_UPDATE:
                    updateHomeTelem();
                    break;

                case AttributeEvent.ALTITUDE_UPDATED:
                    updateAltitudeTelem();
                    break;

                default:
                    break;
            }
        }


    };

    private final Handler handler = new Handler();

    private DroidPlannerPrefs appPrefs;

    private View homeTelemContainer;
    private TextView homeTelem;
    private TextView altitudeTelem;

    private TextView gpsTelem;
    private PopupWindow gpsPopup;

    private TextView batteryTelem;
    private PopupWindow batteryPopup;

    private TextView signalTelem;
    private PopupWindow signalPopup;

    private ImageView flightModeIcon;
    private SpinnerSelfSelect flightModeTelem;
    private int lastDroneType = -1;
    private ModeAdapter modeAdapter;

    private String emptyString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_action_bar_telem, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyString = getString(R.string.empty_content);

        final Context context = getActivity().getApplicationContext();
        final LayoutInflater inflater = LayoutInflater.from(context);

        final int popupWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        final int popupHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        final Drawable popupBg = getResources().getDrawable(android.R.color.transparent);

        homeTelemContainer = view.findViewById(R.id.bar_home_container);
        homeTelem = (TextView) view.findViewById(R.id.bar_home);
        altitudeTelem = (TextView) view.findViewById(R.id.bar_altitude);

        gpsTelem = (TextView) view.findViewById(R.id.bar_gps);
        final View gpsPopupView = inflater.inflate(R.layout.popup_info_gps, (ViewGroup) view, false);
        gpsPopup = new PopupWindow(gpsPopupView,popupWidth, popupHeight, true);
        gpsPopup.setBackgroundDrawable(popupBg);
        gpsTelem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsPopup.showAsDropDown(gpsTelem);
            }
        });

        batteryTelem = (TextView) view.findViewById(R.id.bar_battery);
        final View batteryPopupView = inflater.inflate(R.layout.popup_info_power, (ViewGroup) view, false);
        batteryPopup = new PopupWindow(batteryPopupView, popupWidth, popupHeight, true);
        batteryPopup.setBackgroundDrawable(popupBg);
        batteryTelem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batteryPopup.showAsDropDown(batteryTelem);
            }
        });

        signalTelem = (TextView) view.findViewById(R.id.bar_signal);
        final View signalPopupView = inflater.inflate(R.layout.popup_info_signal, (ViewGroup) view, false);
        signalPopup = new PopupWindow(signalPopupView, popupWidth, popupHeight, true);
        signalPopup.setBackgroundDrawable(popupBg);
        signalTelem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signalPopup.showAsDropDown(signalTelem);
            }
        });

        flightModeIcon = (ImageView) view.findViewById(R.id.bar_flight_mode_icon);
        flightModeTelem = (SpinnerSelfSelect) view.findViewById(R.id.bar_flight_mode);
        modeAdapter = new ModeAdapter(context, R.layout.spinner_drop_down_flight_mode);

        appPrefs = new DroidPlannerPrefs(context);
    }

    private void showTelemBar(){
        final View view = getView();
        if(view != null)
            view.setVisibility(View.VISIBLE);
    }

    private void hideTelemBar(){
        final View view = getView();
        if(view != null)
            view.setVisibility(View.GONE);
    }

    @Override
    public void onStart(){
        hideTelemBar();
        super.onStart();
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

    private void updateAllTelem() {
        updateFlightModeTelem();
        updateSignalTelem();
        updateGpsTelem();
        updateHomeTelem();
        updateBatteryTelem();
        updateAltitudeTelem();
    }

    private void updateFlightModeTelem() {
        final Drone drone = getDrone();

        final boolean isDroneConnected = drone.isConnected();
        final int droneType;
        if (isDroneConnected) {
            flightModeIcon.setImageResource(R.drawable.ic_navigation_light_blue_a400_18dp);
            Type type = drone.getAttribute(AttributeType.TYPE);
            droneType = type.getDroneType();
        } else {
            flightModeIcon.setImageResource(R.drawable.ic_navigation_grey_700_18dp);
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
            signalTelem.setText(emptyString);
            signalTelem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_signal_cellular_null_grey_700_18dp,
                    0, 0, 0);

            rssiView.setText("RSSI: " + emptyString);
            remRssiView.setText("RemRSSI: " + emptyString);
            noiseView.setText("Noise: " + emptyString);
            remNoiseView.setText("RemNoise: " + emptyString);
            fadeView.setText("Fade: "  + emptyString);
            remFadeView.setText("RemFade: " + emptyString);
        }
        else{
            final int signalStrength = (int) droneSignal.getSignalStrength();
            final int signalIcon;
            if (signalStrength >= 100)
                signalIcon = R.drawable.ic_signal_cellular_4_bar_grey_700_18dp;
            else if (signalStrength >= 75)
                signalIcon = R.drawable.ic_signal_cellular_3_bar_grey_700_18dp;
            else if (signalStrength >= 50)
                signalIcon = R.drawable.ic_signal_cellular_2_bar_grey_700_18dp;
            else if (signalStrength >= 25)
                signalIcon = R.drawable.ic_signal_cellular_1_bar_grey_700_18dp;
            else
                signalIcon = R.drawable.ic_signal_cellular_0_bar_grey_700_18dp;

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
        final boolean displayHdop = appPrefs.shouldGpsHdopBeDisplayed();

        final View popupView = gpsPopup.getContentView();
        TextView satNoView = (TextView) popupView.findViewById(R.id.bar_gps_satno);
        TextView hdopStatusView = (TextView) popupView.findViewById(R.id.bar_gps_hdop_status);
        hdopStatusView.setVisibility(displayHdop ? View.GONE : View.VISIBLE);

        final String update;
        final int gpsIcon;
        if (!drone.isConnected()) {
            update = (displayHdop ? "hdop: " : "") + emptyString;
            gpsIcon = R.drawable.ic_gps_off_grey_700_18dp;
            satNoView.setText("S: " + emptyString);
            hdopStatusView.setText("hdop: " + emptyString);
        } else {
            Gps droneGps = drone.getAttribute(AttributeType.GPS);
            final String fixStatus = droneGps.getFixStatus();

            if (displayHdop) {
                update = String.format(Locale.ENGLISH, "hdop: %.1f", droneGps.getGpsEph());
            } else {
                update = String.format(Locale.ENGLISH, "%s", fixStatus);
            }

            switch(fixStatus){
                case Gps.LOCK_3D:
                case Gps.LOCK_3D_DGPS:
                case Gps.LOCK_3D_RTK:
                    gpsIcon = R.drawable.ic_gps_fixed_black_24dp;
                    break;

                case Gps.LOCK_2D:
                case Gps.NO_FIX:
                default:
                    gpsIcon = R.drawable.ic_gps_not_fixed_grey_700_18dp;
                    break;
            }

            satNoView.setText(String.format(Locale.ENGLISH, "S: %d", droneGps.getSatellitesCount()));
            if (appPrefs.shouldGpsHdopBeDisplayed()) {
                hdopStatusView.setText(String.format(Locale.ENGLISH, "%s", fixStatus));
            } else {
                hdopStatusView.setText(String.format(Locale.ENGLISH, "hdop: %.1f", droneGps.getGpsEph()));
            }
        }

        gpsTelem.setText(update);
        gpsTelem.setCompoundDrawablesWithIntrinsicBounds(gpsIcon, 0, 0, 0);
        gpsPopup.update();
    }

    private void updateHomeTelem() {
        final Drone drone = getDrone();

        String update = getString(R.string.empty_content);
        int drawableResId = R.drawable.ic_home_grey_700_18dp;
        if (drone.isConnected()) {
            final Gps droneGps = drone.getAttribute(AttributeType.GPS);
            final Home droneHome = drone.getAttribute(AttributeType.HOME);
            if (droneGps.isValid() && droneHome.isValid()) {
                LengthUnit distanceToHome = getLengthUnitProvider().boxBaseValueToTarget
                        (MathUtils.getDistance2D(droneHome.getCoordinate(), droneGps.getPosition()));
                update = String.format("%s", distanceToHome);

                final ReturnToMeState returnToMe = drone.getAttribute(AttributeType.RETURN_TO_ME_STATE);
                if(returnToMe.getState() == ReturnToMeState.STATE_UPDATING_HOME){
                    //Change the home telemetry icon
                    drawableResId = R.drawable.ic_person_grey_700_18dp;
                }
            }
        }

        homeTelem.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0);
        homeTelem.setText(update);
    }

    private void updateBatteryTelem() {
        final Drone drone = getDrone();

        final View batteryPopupView = batteryPopup.getContentView();
        final TextView dischargeView = (TextView) batteryPopupView.findViewById(R.id.bar_power_discharge);
        final TextView currentView = (TextView) batteryPopupView.findViewById(R.id.bar_power_current);
        final TextView voltageView = (TextView) batteryPopupView.findViewById(R.id.bar_power_voltage);

        String update;
        Battery droneBattery;
        final int batteryIcon;
        if (!drone.isConnected() || ((droneBattery = drone.getAttribute(AttributeType.BATTERY)) == null)) {
            update = emptyString;
            dischargeView.setText("D: " + emptyString);
            currentView.setText("C: " + emptyString);
            voltageView.setText("V: " + emptyString);
            batteryIcon = R.drawable.ic_battery_circle_0_36dp;
        } else {
            Double discharge = droneBattery.getBatteryDischarge();
            String dischargeText;
            if (discharge == null) {
                dischargeText = "D: " + emptyString;
            } else {
                dischargeText = "D: " + electricChargeToString(discharge);
            }

            dischargeView.setText(dischargeText);
            voltageView.setText(String.format(Locale.ENGLISH, "V: %2.1f V", droneBattery.getBatteryVoltage()));
            currentView.setText(String.format("C: %2.1f A", droneBattery.getBatteryCurrent()));

            final double battRemain = droneBattery.getBatteryRemain();
            update = String.format(Locale.ENGLISH, "%2.0f%%", battRemain);

            if(battRemain >= 100){
                batteryIcon = R.drawable.ic_battery_circle_8_36dp;
            }
            else if(battRemain >= 87.5){
                batteryIcon = R.drawable.ic_battery_circle_7_36dp;
            }
            else if(battRemain >= 75){
                batteryIcon = R.drawable.ic_battery_circle_6_36dp;
            }
            else if(battRemain >= 62.5){
                batteryIcon = R.drawable.ic_battery_circle_5_36dp;
            }
            else if(battRemain >= 50){
                batteryIcon = R.drawable.ic_battery_circle_4_36dp;
            }
            else if(battRemain >= 37.5){
                batteryIcon = R.drawable.ic_battery_circle_3_36dp;
            }
            else if(battRemain >= 25){
                batteryIcon = R.drawable.ic_battery_circle_2_36dp;
            }
            else if(battRemain >= 12.5){
                batteryIcon = R.drawable.ic_battery_circle_1_36dp;
            }
            else{
                batteryIcon = R.drawable.ic_battery_circle_0_36dp;
            }
        }

        batteryPopup.update();
        batteryTelem.setText(update);
        batteryTelem.setCompoundDrawablesWithIntrinsicBounds(batteryIcon, 0, 0, 0);
    }

    private String electricChargeToString(double chargeInmAh) {
        double absCharge = Math.abs(chargeInmAh);
        if(absCharge >= 1000){
            return String.format(Locale.US, "%2.1f Ah", chargeInmAh / 1000);
        }
        else{
            return String.format(Locale.ENGLISH, "%2.0f mAh", chargeInmAh);
        }
    }

    private void updateAltitudeTelem() {
        final Drone drone = getDrone();
        final Altitude altitude = drone.getAttribute(AttributeType.ALTITUDE);
        if (altitude != null) {
            double alt = altitude.getAltitude();
            LengthUnit altUnit = getLengthUnitProvider().boxBaseValueToTarget(alt);

            this.altitudeTelem.setText(altUnit.toString());
        }
    }
}
