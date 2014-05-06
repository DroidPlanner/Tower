package org.droidplanner.android.proxy.mission.item;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.droidplanner.R;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.fragments.MissionDetailFragment;
import org.droidplanner.android.proxy.mission.item.markers.MissionItemMarkerInfo;
import org.droidplanner.android.utils.DroneHelper;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.survey.Survey;
import org.droidplanner.core.mission.survey.grid.Grid;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for providing logic to access and interpret the
 * {@link org.droidplanner.core.mission.MissionItem} class on the Android layer,
 * as well as providing methods for rendering it on the Android UI.
 */
public class MissionItemProxy implements Comparable<MissionItemProxy> {

    /**
     * This is the mission item object this class is built around.
     */
    private final MissionItem mMissionItem;

    /**
     * This is the mission render to which this item belongs.
     */
    private final MissionProxy mMission;

    /**
     * This is the marker source for this mission item render.
     */
    private final MissionItemMarkerInfo mMarkerInfo;

    public MissionItemProxy(MissionProxy mission, MissionItem missionItem){
        mMission = mission;
        mMissionItem = missionItem;
        mMarkerInfo = MissionItemMarkerInfo.newInstance(this);
    }

    /**
     * Provides access to the owning mission render instance.
     * @return
     */
    public MissionProxy getMissionProxy(){
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

    public MissionItemMarkerInfo getMarkerInfo(){
        return mMarkerInfo;
    }

    /**
     * @return the set of points/coords making up this mission item.
     */
    public List<Coord2D> getPath() {
        List<Coord2D> pathPoints = new ArrayList<Coord2D>();
        switch (mMissionItem.getType()) {
            case LAND:
            case LOITER:
            case LOITER_INF:
            case LOITERT:
            case LOITERN:
            case TAKEOFF:
            case WAYPOINT:
                pathPoints.add(((SpatialCoordItem) mMissionItem).getCoordinate());
                break;
            
            case SURVEY:
                Grid grid = ((Survey) mMissionItem).grid;
            	if (grid != null) {				
            		pathPoints.addAll(grid.gridPoints);
            	}
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
    public int compareTo(MissionItemProxy another){
        return mMissionItem.compareTo(another.mMissionItem);
    }
}
