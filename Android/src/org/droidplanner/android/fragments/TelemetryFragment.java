package org.droidplanner.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.CapabilityApi;
import com.o3dr.android.client.apis.solo.SoloCameraApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.WidgetActivity;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.fragments.widget.telem.TelemetryWidget;
import org.droidplanner.android.utils.unit.providers.speed.SpeedUnitProvider;
import org.droidplanner.android.widgets.AttitudeIndicator;

import timber.log.Timber;

public class TelemetryFragment extends ApiListenerFragment {

    private static final String TAG = TelemetryFragment.class.getSimpleName();

    private final static IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.ATTITUDE_UPDATED);
        eventFilter.addAction(AttributeEvent.SPEED_UPDATED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case AttributeEvent.ATTITUDE_UPDATED:
                    onOrientationUpdate();
                    break;

                case AttributeEvent.SPEED_UPDATED:
                    onSpeedUpdate();
                    break;
            }
        }
    };

    private AttitudeIndicator attitudeIndicator;
    private TextView roll;
    private TextView yaw;
    private TextView pitch;

    private TextView horizontalSpeed;
    private TextView verticalSpeed;

    private boolean headingModeFPV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_telemetry, container, false);

        final ViewGroup widgetsContainer = (ViewGroup) view.findViewById(R.id.telem_widgets_container);

        attitudeIndicator = (AttitudeIndicator) view.findViewById(R.id.aiView);

        roll = (TextView) view.findViewById(R.id.rollValueText);
        yaw = (TextView) view.findViewById(R.id.yawValueText);
        pitch = (TextView) view.findViewById(R.id.pitchValueText);

        horizontalSpeed = (TextView) view.findViewById(R.id.horizontal_speed_telem);
        verticalSpeed = (TextView) view.findViewById(R.id.vertical_speed_telem);

        generateTelemetryWidgets(inflater, widgetsContainer);

        return view;
    }

    private void generateTelemetryWidgets(LayoutInflater inflater, ViewGroup container){
        final FragmentManager fm = getChildFragmentManager();
        final TelemetryWidget[] telemWidgets = TelemetryWidget.values();

        for(TelemetryWidget telemWidget: telemWidgets){
            final @IdRes int holderId = telemWidget.getIdRes();

            //Inflate the widget container
            final View widgetView = inflater.inflate(R.layout.container_telemetry_widget, container, false);

            final View contentHolder = widgetView.findViewById(R.id.widget_container);
            contentHolder.setId(holderId);

            //Add the widget holder to the container
            container.addView(widgetView);

            //Add the widget fragment to the widget holder
            final Fragment widgetFragment = telemWidget.getMinimizedFragment();
            fm.beginTransaction().add(holderId, widgetFragment).commit();

            if(telemWidget.canMaximize()){
                final View maximizeView = widgetView.findViewById(R.id.widget_maximize_button);
                maximizeView.setVisibility(View.VISIBLE);

                maximizeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getContext(), WidgetActivity.class)
                                .putExtra(WidgetActivity.EXTRA_WIDGET_ID, WidgetActivity.WIDGET_SOLOLINK_VIDEO));
                    }
                });
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        headingModeFPV = prefs.getBoolean("pref_heading_mode", false);
    }

    @Override
    public void onApiConnected() {
        updateAllTelem();
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    private void updateAllTelem() {
        onOrientationUpdate();
        onSpeedUpdate();
    }

    private void onOrientationUpdate() {
        final Drone drone = getDrone();

        final Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
        if (attitude == null)
            return;

        float r = (float) attitude.getRoll();
        float p = (float) attitude.getPitch();
        float y = (float) attitude.getYaw();

        if (!headingModeFPV & y < 0) {
            y = 360 + y;
        }

        attitudeIndicator.setAttitude(r, p, y);

        roll.setText(String.format("%3.0f\u00B0", r));
        pitch.setText(String.format("%3.0f\u00B0", p));
        yaw.setText(String.format("%3.0f\u00B0", y));

    }

    private void onSpeedUpdate() {
        final Drone drone = getDrone();
        final Speed speed = drone.getAttribute(AttributeType.SPEED);

        final double groundSpeedValue = speed != null ? speed.getGroundSpeed() : 0;
        final double verticalSpeedValue = speed != null ? speed.getVerticalSpeed() : 0;

        final SpeedUnitProvider speedUnitProvider = getSpeedUnitProvider();

        horizontalSpeed.setText(getString(R.string.horizontal_speed_telem, speedUnitProvider.boxBaseValueToTarget(groundSpeedValue).toString()));
        verticalSpeed.setText(getString(R.string.vertical_speed_telem, speedUnitProvider.boxBaseValueToTarget(verticalSpeedValue).toString()));
    }

}
