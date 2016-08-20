package org.droidplanner.android.view.adapterViews;

import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.CameraTrigger;
import com.o3dr.services.android.lib.drone.mission.item.command.ChangeSpeed;
import com.o3dr.services.android.lib.drone.mission.item.command.EpmGripper;
import com.o3dr.services.android.lib.drone.mission.item.command.ReturnToLaunch;
import com.o3dr.services.android.lib.drone.mission.item.command.SetServo;
import com.o3dr.services.android.lib.drone.mission.item.command.Takeoff;
import com.o3dr.services.android.lib.drone.mission.item.command.YawCondition;
import com.o3dr.services.android.lib.drone.mission.item.complex.SplineSurvey;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Circle;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Land;
import com.o3dr.services.android.lib.drone.mission.item.spatial.RegionOfInterest;
import com.o3dr.services.android.lib.drone.mission.item.spatial.SplineWaypoint;

import org.beyene.sius.unit.composition.speed.SpeedUnit;
import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.utils.unit.UnitManager;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.utils.unit.providers.speed.SpeedUnitProvider;
import org.droidplanner.android.utils.unit.systems.UnitSystem;

import java.util.Collections;
import java.util.Locale;

/**
 * Created by fhuya on 12/9/14.
 */
public class MissionItemListAdapter extends RecyclerView.Adapter<MissionItemListAdapter.ViewHolder> {

    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {

        final View viewContainer;
        final TextView nameView;
        final TextView altitudeView;

        public ViewHolder(View container, TextView nameView, TextView altitudeView) {
            super(container);
            this.viewContainer = container;
            this.nameView = nameView;
            this.altitudeView = altitudeView;
        }

    }

    private final View.OnLongClickListener vibrateOnLongPress = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            vibrator.vibrate(10L);
            return true;
        }
    };

    private final MissionProxy missionProxy;
    private final OnEditorInteraction editorListener;
    private final LengthUnitProvider lengthUnitProvider;
    private final SpeedUnitProvider speedUnitProvider;
    private final Vibrator vibrator;

    public MissionItemListAdapter(Context context, MissionProxy missionProxy, OnEditorInteraction editorListener) {
        this.missionProxy = missionProxy;
        this.editorListener = editorListener;

        final UnitSystem unitSystem = UnitManager.getUnitSystem(context);
        this.lengthUnitProvider = unitSystem.getLengthUnitProvider();
        this.speedUnitProvider = unitSystem.getSpeedUnitProvider();

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public int getItemCount() {
        return missionProxy.getItems().size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_editor_list_item, parent, false);

        final TextView nameView = (TextView) view.findViewById(R.id.rowNameView);
        final TextView altitudeView = (TextView) view.findViewById(R.id.rowAltitudeView);

        return new ViewHolder(view, nameView, altitudeView);
    }

    public void swap(int fromPosition, int toPosition) {
        Collections.swap(missionProxy.getItems(), fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void dismiss(int deletedPosition) {
        MissionItemProxy deletedItem = missionProxy.getItems().remove(deletedPosition);
        missionProxy.selection.getSelected().remove(deletedItem);
        notifyItemRemoved(deletedPosition);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final MissionItemProxy proxy = missionProxy.getItems().get(position);

        final View container = viewHolder.viewContainer;
        container.setActivated(missionProxy.selection.selectionContains(proxy));
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editorListener != null)
                editorListener.onItemClick(proxy, true);
            }
        });
        container.setOnLongClickListener(vibrateOnLongPress);

        final TextView nameView = viewHolder.nameView;
        final TextView altitudeView = viewHolder.altitudeView;

        final MissionProxy missionProxy = proxy.getMissionProxy();
        final MissionItem missionItem = proxy.getMissionItem();

        nameView.setText(String.format(Locale.US, "%3d", missionProxy.getOrder(proxy)));

        int leftDrawable;

        // Spatial item's icons
        if (missionItem instanceof MissionItem.SpatialItem) {
            if (missionItem instanceof SplineWaypoint) {
                leftDrawable = R.drawable.ic_mission_spline_wp;
            } else if (missionItem instanceof Circle) {
                leftDrawable = R.drawable.ic_mission_circle_wp;
            } else if (missionItem instanceof RegionOfInterest) {
                leftDrawable = R.drawable.ic_mission_roi_wp;
            } else if (missionItem instanceof Land) {
                leftDrawable = R.drawable.ic_mission_land_wp;
            } else {
                leftDrawable = R.drawable.ic_mission_wp;
            }
        // Command icons
        } else if (missionItem instanceof MissionItem.Command) {
            if (missionItem instanceof CameraTrigger) {
                leftDrawable = R.drawable.ic_mission_camera_trigger_wp;
            } else if (missionItem instanceof ChangeSpeed) {
                leftDrawable = R.drawable.ic_mission_change_speed_wp;
            } else if (missionItem instanceof EpmGripper) {
                leftDrawable = R.drawable.ic_mission_epm_gripper_wp;
            } else if (missionItem instanceof ReturnToLaunch) {
                leftDrawable = R.drawable.ic_mission_rtl_wp;
            } else if (missionItem instanceof SetServo) {
                leftDrawable = R.drawable.ic_mission_set_servo_wp;
            } else if (missionItem instanceof Takeoff) {
                leftDrawable = R.drawable.ic_mission_takeoff_wp;
            } else if (missionItem instanceof YawCondition) {
                leftDrawable = R.drawable.ic_mission_yaw_cond_wp;
            } else {
                leftDrawable = R.drawable.ic_mission_command_wp;
            }
        // Complex item's icons
        // TODO CameraDetail (inconvertible type) and StructureScanner (condition always false) WPs
        } else if (missionItem instanceof MissionItem.ComplexItem) {
            if (missionItem instanceof SplineSurvey) {
                leftDrawable = R.drawable.ic_mission_spline_survey_wp;
            } else if (missionItem instanceof Survey) {
                leftDrawable = R.drawable.ic_mission_survey_wp;
            } else {
                leftDrawable = R.drawable.ic_mission_command_wp;
            }
        // Fallback icon
        } else {
            leftDrawable = R.drawable.ic_mission_wp;
        }

        altitudeView.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, 0, 0, 0);

        if (missionItem instanceof MissionItem.SpatialItem) {
            MissionItem.SpatialItem waypoint = (MissionItem.SpatialItem) missionItem;
            double altitude = waypoint.getCoordinate().getAltitude();
            LengthUnit convertedAltitude = lengthUnitProvider.boxBaseValueToTarget(altitude);
            LengthUnit roundedConvertedAltitude = (LengthUnit) convertedAltitude.valueOf(Math.round(convertedAltitude.getValue()));
            altitudeView.setText(roundedConvertedAltitude.toString());

            if (altitude < 0)
                altitudeView.setTextColor(Color.YELLOW);
            else
                altitudeView.setTextColor(Color.WHITE);

        } else if (missionItem instanceof Survey) {
            double altitude = ((Survey) missionItem).getSurveyDetail().getAltitude();
            LengthUnit convertedAltitude = lengthUnitProvider.boxBaseValueToTarget(altitude);
            LengthUnit roundedConvertedAltitude = (LengthUnit) convertedAltitude.valueOf(Math.round(convertedAltitude.getValue()));
            altitudeView.setText(roundedConvertedAltitude.toString());

            if (altitude < 0)
                altitudeView.setTextColor(Color.YELLOW);
            else
                altitudeView.setTextColor(Color.WHITE);

        } else if (missionItem instanceof Takeoff) {
            double altitude = ((Takeoff) missionItem).getTakeoffAltitude();
            LengthUnit convertedAltitude = lengthUnitProvider.boxBaseValueToTarget(altitude);
            LengthUnit roundedConvertedAltitude = (LengthUnit) convertedAltitude.valueOf(Math.round(convertedAltitude.getValue()));
            altitudeView.setText(roundedConvertedAltitude.toString());

            if (altitude < 0)
                altitudeView.setTextColor(Color.YELLOW);
            else
                altitudeView.setTextColor(Color.WHITE);
        }else if(missionItem instanceof ChangeSpeed){
            final double speed = ((ChangeSpeed) missionItem).getSpeed();
            final SpeedUnit convertedSpeed = speedUnitProvider.boxBaseValueToTarget(speed);
            altitudeView.setText(convertedSpeed.toString());
        }
        else {
            altitudeView.setText("");
        }
    }
}
