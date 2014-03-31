package org.droidplanner.android.mission.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.droidplanner.R;
import org.droidplanner.android.fragments.markers.helpers.MarkerWithText;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.android.mission.item.fragments.MissionDetailFragment;
import org.droidplanner.android.mission.item.fragments.MissionWaypointFragment;
import org.droidplanner.android.graphic.map.MarkerManager.MarkerSource;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;

import java.io.Serializable;

/**
 * This class is responsible for providing logic to access and interpret the
 * {@link org.droidplanner.core.mission.MissionItem} class on the Android layer,
 * as well as providing methods for rendering it on the Android UI.
 */
public class MissionItemRender implements Comparable<MissionItemRender>, Serializable,
        MarkerSource {

    /**
     * This is the mission item object this class is built around.
     */
    private final MissionItem mMissionItem;

    /**
     * This mission render to which this item belongs.
     */
    private final MissionRender mMission;

    public MissionItemRender(MissionRender mission, MissionItem missionItem){
        mMission = mission;
        mMissionItem = missionItem;
    }

    /**
     * Provides access to the mission item instance.
     * @return {@link org.droidplanner.core.mission.MissionItem} object
     */
    public MissionItem getMissionItem(){
        return mMissionItem;
    }

    public MissionDetailFragment getDetailFragment() {
        final Bundle fragmentArgs = new Bundle();
        fragmentArgs.putSerializable(MissionDetailFragment.EXTRA_MISSION_ITEM_RENDER, this);

        MissionDetailFragment fragment = new MissionWaypointFragment();
        fragment.setArguments(fragmentArgs);

        return fragment;
    }

    public View getListViewItemView(Context context, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.fragment_editor_list_item, parent, false);

        TextView nameView = (TextView) view.findViewById(R.id.rowNameView);
        TextView altitudeView = (TextView) view.findViewById(R.id.rowAltitudeView);
                /*
		TextView typeView = (TextView) view.findViewById(R.id.rowTypeView);
		TextView descView = (TextView) view.findViewById(R.id.rowDescView);
		TextView distanceView = (TextView) view.findViewById(R.id.rowDistanceView);
*/

        nameView.setText(String.format("%3d", mMissionItem.getMission().getOrder(mMissionItem)));

        if (mMissionItem instanceof SpatialCoordItem) {
            SpatialCoordItem waypoint = (SpatialCoordItem) mMissionItem;
            altitudeView.setText(String.format("%3.0fm", waypoint.getCoordinate().getAltitude()
                    .valueInMeters()));

            try {
                Length diff = waypoint.getMission().getAltitudeDiffFromPreviousItem(waypoint);
                if (diff.valueInMeters() > 0) {
                    altitudeView.setTextColor(Color.RED);
                } else if (diff.valueInMeters() < 0) {
                    altitudeView.setTextColor(Color.BLUE);
                }
            } catch (Exception e) {
                // Do nothing when last item doesn't have an altitude
            }
        } else {
            altitudeView.setText("");
        }

		/*
		if (waypoint.getCmd().showOnMap()) {
			altitudeView.setText(String.format(Locale.ENGLISH, "%3.0fm", waypoint.getHeight()));
		} else {
			altitudeView.setText("-");
		}
*/
        //TODO fix the numbering
        //nameView.setText(String.format("%3d", waypoint.getOrder()));


	/*	typeView.setText(waypoint.getCmd().getName());
		descView.setText(setupDescription(waypoint));

		double distanceFromPrevPoint = waypoint.getDistanceFromPrevPoint();
		if(distanceFromPrevPoint != waypoint.UNKNOWN_DISTANCE) {
			distanceView.setText(String.format(Locale.ENGLISH, "%4.0fm", distanceFromPrevPoint));
		}
		else {
			distanceView.setText("-");
		}
		*/
        return view;
    }

    @Override
    public int compareTo(MissionItemRender another){
        return mMissionItem.compareTo(another.mMissionItem);
    }

    /*****
     * TODO: Abstract this class, and create specialize instances for the different types for
     * mission items.
     * Keep a factory in this class to retrieve the correct sub instances.
     */
    @Override
    public MarkerOptions build(Context context) {
        return new MarkerOptions()
                .position(DroneHelper.CoordToLatLang(((SpatialCoordItem)mMissionItem).getCoordinate
                        ()))
                .draggable(true).anchor(0.5f, 0.5f).icon(getIcon(context));
    }

    @Override
    public void update(Marker marker, Context context) {
        marker.setPosition(DroneHelper.CoordToLatLang(((SpatialCoordItem)mMissionItem).getCoordinate()));
        marker.setIcon(getIcon(context));
    }

    protected BitmapDescriptor getIcon(Context context) {
        int drawable;
        if (mMission.selectionContains(MissionItemRender.this)) {
            drawable = R.drawable.ic_wp_map_selected;
        } else {
            drawable = R.drawable.ic_wp_map;
        }
        Bitmap marker = MarkerWithText.getMarkerWithTextAndDetail(drawable,
                Integer.toString(mMission.getOrder(MissionItemRender.this)),
                getIconDetail(),
                context);
        return BitmapDescriptorFactory.fromBitmap(marker);
    }

    private String getIconDetail() {
        try {
            if (mMission.getAltitudeDiffFromPreviousItem(MissionItemRender.this)
                    .valueInMeters() ==
                    0) {
                return null;
            } else {
                return null; // altitude.toString();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
