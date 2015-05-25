package org.droidplanner.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Speed;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.utils.unit.providers.speed.SpeedUnitProvider;
import org.droidplanner.android.widgets.AttitudeIndicator;

public class TelemetryFragment extends ApiListenerFragment {

    private final static IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.ATTITUDE_UPDATED);
        eventFilter.addAction(AttributeEvent.SPEED_UPDATED);
        eventFilter.addAction(AttributeEvent.ALTITUDE_UPDATED);
        eventFilter.addAction(AttributeEvent.STATE_UPDATED);
    }

    /**
     * This is the period for the flight time update.
     */
    protected final static long FLIGHT_TIMER_PERIOD = 1000l; // 1 second


    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final Drone drone = getDrone();

            switch (action) {
                case AttributeEvent.ATTITUDE_UPDATED:
                    final Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
                    onOrientationUpdate(attitude);
                    break;

                case AttributeEvent.SPEED_UPDATED:
                    final Speed droneSpeed = drone.getAttribute(AttributeType.SPEED);
                    onSpeedUpdate(droneSpeed);
                    break;

                case AttributeEvent.ALTITUDE_UPDATED:
                    final Altitude droneAltitude = drone.getAttribute(AttributeType.ALTITUDE);
                    onAltitudeUpdate(droneAltitude);
                    break;

                case AttributeEvent.STATE_UPDATED:
                    updateFlightTimer();
                    break;
            }
        }
    };

    /**
     * Runnable used to update the drone flight time.
     */
    protected Runnable mFlightTimeUpdater = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(this);
            final Drone drone = getDrone();
            if (drone == null || !drone.isConnected())
                return;

            if (flightTimer != null) {
                long timeInSeconds = drone.getFlightTime();
                long minutes = timeInSeconds / 60;
                long seconds = timeInSeconds % 60;

                flightTimer.setText(String.format("%02d:%02d", minutes, seconds));
            }

            mHandler.postDelayed(this, FLIGHT_TIMER_PERIOD);
        }
    };

    /**
     * This handler is used to update the flight time value.
     */
    protected final Handler mHandler = new Handler();


    private AttitudeIndicator attitudeIndicator;
    private TextView roll;
    private TextView yaw;
    private TextView pitch;
    private TextView groundSpeed;
    private TextView airSpeed;
    private TextView climbRate;
    private TextView altitude;
    private TextView flightTimer;
    private boolean headingModeFPV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_telemetry, container, false);
        attitudeIndicator = (AttitudeIndicator) view.findViewById(R.id.aiView);

        roll = (TextView) view.findViewById(R.id.rollValueText);
        yaw = (TextView) view.findViewById(R.id.yawValueText);
        pitch = (TextView) view.findViewById(R.id.pitchValueText);

        groundSpeed = (TextView) view.findViewById(R.id.groundSpeedValue);
        airSpeed = (TextView) view.findViewById(R.id.airSpeedValue);
        climbRate = (TextView) view.findViewById(R.id.climbRateValue);
        altitude = (TextView) view.findViewById(R.id.altitudeValue);

        flightTimer = (TextView) view.findViewById(R.id.flight_timer);

        final Resources res = getResources();
        final int popupWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        final int popupHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        final Drawable popupBg = res.getDrawable(android.R.color.transparent);

        final View resetTimerView = inflater.inflate(R.layout.popup_info_flight_time, container, false);
        final PopupWindow resetTimerPopup = new PopupWindow(resetTimerView, popupWidth, popupHeight, true);
        resetTimerPopup.setBackgroundDrawable(popupBg);

        final TextView resetTimer = (TextView) resetTimerView.findViewById(R.id.bar_flight_time_reset_timer);
        resetTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Drone drone = getDrone();
                if(drone != null && drone.isConnected())
                    drone.resetFlightTimer();

                updateFlightTimer();
                resetTimerPopup.dismiss();
            }
        });

        final ImageButton resetFlightTimerButton = (ImageButton) view.findViewById(R.id.reset_flight_timer_button);
        resetFlightTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimerPopup.showAsDropDown(resetFlightTimerButton);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        headingModeFPV = prefs.getBoolean("pref_heading_mode", false);
    }

    private void updateFlightTimer(){
        final Drone drone = getDrone();
        mHandler.removeCallbacks(mFlightTimeUpdater);
        if (drone != null && drone.isConnected()) {
            mFlightTimeUpdater.run();
        } else {
            flightTimer.setText("00:00");
        }
    }

    @Override
    public void onApiConnected() {
        updateFlightTimer();
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    public void onOrientationUpdate(Attitude droneAttitude) {
        if (droneAttitude == null)
            return;

        float r = (float) droneAttitude.getRoll();
        float p = (float) droneAttitude.getPitch();
        float y = (float) droneAttitude.getYaw();

        if (!headingModeFPV & y < 0) {
            y = 360 + y;
        }

        attitudeIndicator.setAttitude(r, p, y);

        roll.setText(String.format("%3.0f\u00B0", r));
        pitch.setText(String.format("%3.0f\u00B0", p));
        yaw.setText(String.format("%3.0f\u00B0", y));

    }

    private void onSpeedUpdate(Speed speed) {
        if (speed != null) {
            final SpeedUnitProvider speedUnitProvider = getSpeedUnitProvider();

            airSpeed.setText(speedUnitProvider.boxBaseValueToTarget(speed.getAirSpeed()).toString());
            groundSpeed.setText(speedUnitProvider.boxBaseValueToTarget(speed.getGroundSpeed()).toString());
            climbRate.setText(speedUnitProvider.boxBaseValueToTarget(speed.getVerticalSpeed()).toString());
        }
    }

    private void onAltitudeUpdate(Altitude altitude) {
        if (altitude != null) {
            double alt = altitude.getAltitude();
            LengthUnit altUnit = getLengthUnitProvider().boxBaseValueToTarget(alt);

            this.altitude.setText(altUnit.toString());
        }
    }
}
