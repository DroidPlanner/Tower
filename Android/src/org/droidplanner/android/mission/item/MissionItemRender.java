package org.droidplanner.android.mission.item;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.android.mission.MissionRender;
import org.droidplanner.android.mission.item.fragments.MissionDetailFragment;
import org.droidplanner.android.mission.item.markers.MissionItemGenericMarkerSource;
import org.droidplanner.android.mission.item.markers.MissionItemMarkerSource;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.commands.Takeoff;
import org.droidplanner.core.mission.survey.Survey;
import org.droidplanner.core.mission.survey.grid.Grid;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

/**
 * This class is responsible for providing logic to access and interpret the
 * {@link org.droidplanner.core.mission.MissionItem} class on the Android layer,
 * as well as providing methods for rendering it on the Android UI.
 */
public class MissionItemRender implements Comparable<MissionItemRender> {

    /**
     * This is the mission item object this class is built around.
     */
    private final MissionItem mMissionItem;

    /**
     * This is the mission render to which this item belongs.
     */
    private final MissionRender mMission;

    /**
     * This is the marker source for this mission item render.
     */
    private final MissionItemGenericMarkerSource mMarkerSource;

    public MissionItemRender(MissionRender mission, MissionItem missionItem){
        mMission = mission;
        mMissionItem = missionItem;
        mMarkerSource = MissionItemMarkerSource.newInstance(this);
    }

    /**
     * Provides access to the owning mission render instance.
     * @return
     */
    public MissionRender getMissionRender(){
        return mMission;
    }

    /**
     * Provides access to the mission item instance.
     * @return {@link org.droidplanner.core.mission.MissionItem} object
     */
    public MissionItem getMissionItem(){
        return mMissionItem;
    }

    public MissionDetailFragment getDetailFragment() {
        return MissionDetailFragment.newInstance(mMissionItem.getType());
    }

    public MissionItemGenericMarkerSource getMarkerSource(){
        return mMarkerSource;
    }

    /**
     * @return the set of points/coords making up this mission item.
     */
    public List<LatLng> getPath() {
        List<LatLng> pathPoints = new ArrayList<LatLng>();
        Grid grid;
		switch (mMissionItem.getType()) {
            case LAND:
            case LOITER:
            case LOITER_INF:
            case LOITERT:
            case LOITERN:            
            case WAYPOINT:
                pathPoints.add(DroneHelper.CoordToLatLang(((SpatialCoordItem)
                        mMissionItem).getCoordinate()));
                break;
            case SURVEY:
            	grid = ((Survey) mMissionItem).grid;
            	if (grid != null) {				
            		pathPoints.addAll(DroneHelper.CoordToLatLang(grid.gridPoints));
            	}
            case TAKEOFF:
            default:
                break;
        }

        return pathPoints;
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
        } else if (mMissionItem instanceof Survey) {
			altitudeView.setText(((Survey)mMissionItem).surveyData.getAltitude().toString());

        } else if (mMissionItem instanceof Takeoff) {
			altitudeView.setText(((Takeoff)mMissionItem).getFinishedAlt().toString());
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
}
