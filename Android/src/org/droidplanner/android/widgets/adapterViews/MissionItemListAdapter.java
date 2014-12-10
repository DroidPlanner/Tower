package org.droidplanner.android.widgets.adapterViews;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.Takeoff;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.spatial.SplineWaypoint;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.utils.unit.UnitManager;

/**
 * Created by fhuya on 12/9/14.
 */
public class MissionItemListAdapter extends RecyclerView.Adapter<MissionItemListAdapter
        .ViewHolder> {

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

    private final MissionProxy missionProxy;
    private final OnEditorInteraction editorListener;

    public MissionItemListAdapter(MissionProxy missionProxy, OnEditorInteraction editorListener) {
        this.missionProxy = missionProxy;
        this.editorListener = editorListener;
    }

    @Override
    public int getItemCount() {
        return missionProxy.getItems().size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .fragment_editor_list_item, parent, false);

        final TextView nameView = (TextView) view.findViewById(R.id.rowNameView);
        final TextView altitudeView = (TextView) view.findViewById(R.id.rowAltitudeView);

        return new ViewHolder(view, nameView, altitudeView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final MissionItemProxy proxy = missionProxy.getItems().get(position);

        final View container = viewHolder.viewContainer;
        container.setActivated(missionProxy.selection.selectionContains(proxy));
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorListener.onItemClick(proxy, true);
            }
        });
        container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return editorListener.onItemLongClick(proxy);
            }
        });

        final TextView nameView = viewHolder.nameView;
        final TextView altitudeView = viewHolder.altitudeView;

        final MissionProxy missionProxy = proxy.getMission();
        final MissionItem missionItem = proxy.getMissionItem();

        nameView.setText(String.format("%3d", missionProxy.getOrder(proxy)));

        final int leftDrawable = missionItem instanceof SplineWaypoint
                ? R.drawable.ic_mission_spline_wp
                : R.drawable.ic_mission_wp;
        altitudeView.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, 0, 0, 0);

        if (missionItem instanceof MissionItem.SpatialItem) {
            MissionItem.SpatialItem waypoint = (MissionItem.SpatialItem) missionItem;
            double altitude = waypoint.getCoordinate().getAltitude();
            altitudeView.setText(UnitManager.getUnitProvider().distanceToString(altitude));

            if (altitude < 0)
                altitudeView.setTextColor(Color.YELLOW);
            else
                altitudeView.setTextColor(Color.WHITE);

            try {
                double diff = missionProxy.getAltitudeDiffFromPreviousItem(proxy);
                if (diff > 0) {
                    altitudeView.setTextColor(Color.RED);
                } else if (diff < 0) {
                    altitudeView.setTextColor(Color.BLUE);
                }
            } catch (Exception e) {
                // Do nothing when last item doesn't have an altitude
            }
        } else if (missionItem instanceof Survey) {
            double altitude = ((Survey) missionItem).getSurveyDetail().getAltitude();
            String formattedAltitude = UnitManager.getUnitProvider().distanceToString(altitude);
            altitudeView.setText(formattedAltitude);

            if (altitude < 0)
                altitudeView.setTextColor(Color.YELLOW);
            else
                altitudeView.setTextColor(Color.WHITE);

        } else if (missionItem instanceof Takeoff) {
            double altitude = ((Takeoff) missionItem).getTakeoffAltitude();
            altitudeView.setText(UnitManager.getUnitProvider().distanceToString(altitude));

            if (altitude < 0)
                altitudeView.setTextColor(Color.YELLOW);
            else
                altitudeView.setTextColor(Color.WHITE);
        } else {
            altitudeView.setText("");
        }
    }
}
