package org.droidplanner.android.proxy.mission.item;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.android.R;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.fragments.MissionDetailFragment;
import org.droidplanner.android.proxy.mission.item.markers.MissionItemMarkerInfo;
import org.droidplanner.android.utils.unit.UnitManager;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.command.Takeoff;
import com.o3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Circle;
import com.o3dr.services.android.lib.drone.mission.item.spatial.SplineWaypoint;
import com.o3dr.services.android.lib.util.MathUtils;

/**
 * This class is responsible for providing logic to access and interpret the
 * {@link com.o3dr.services.android.lib.drone.mission.item.MissionItem} class on the Android layer,
 * as well as providing methods for rendering it on the Android UI.
 */
public class MissionItemProxy {

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
	private final List<MarkerInfo> mMarkerInfos;

	public MissionItemProxy(MissionProxy mission, MissionItem missionItem) {
		mMission = mission;
		mMissionItem = missionItem;
		mMarkerInfos = MissionItemMarkerInfo.newInstance(this);

        if(mMissionItem instanceof Survey){
            mMission.getDrone().buildSurvey((Survey) mMissionItem);
        }
        else if(mMissionItem instanceof StructureScanner){
            mMission.getDrone().buildStructureScanner((StructureScanner) mMissionItem);
        }
	}

	/**
	 * Provides access to the owning mission render instance.
	 * 
	 * @return
	 */
	public MissionProxy getMissionProxy() {
		return mMission;
	}

	/**
	 * Provides access to the mission item instance.
	 * 
	 * @return {@link com.o3dr.services.android.lib.drone.mission.item.MissionItem} object
	 */
	public MissionItem getMissionItem() {
		return mMissionItem;
	}

	public MissionDetailFragment getDetailFragment() {
		return MissionDetailFragment.newInstance(mMissionItem.getType());
	}

	public List<MarkerInfo> getMarkerInfos() {
		return mMarkerInfos;
	}

	/**
	 * @param previousPoint
	 *            Previous point on the path, null if there wasn't a previous
	 *            point
	 * @return the set of points/coords making up this mission item.
	 */
	public List<LatLong> getPath(LatLong previousPoint) {
		List<LatLong> pathPoints = new ArrayList<LatLong>();
		switch (mMissionItem.getType()) {
		case LAND:
		case WAYPOINT:
		case SPLINE_WAYPOINT:
			pathPoints.add(((MissionItem.SpatialItem) mMissionItem).getCoordinate());
			break;

		case CIRCLE:
			for (int i = 0; i <= 360; i += 10) {
				Circle circle = (Circle) mMissionItem;
				double startHeading = 0;
				if (previousPoint != null) {
					startHeading = MathUtils.getHeadingFromCoordinates(circle.getCoordinate(),
                            previousPoint);
				}
				pathPoints.add(MathUtils.newCoordFromBearingAndDistance(circle.getCoordinate(),
						startHeading + i, circle.getRadius()));
			}
			break;

		case SURVEY:
            List<LatLong> gridPoints = ((Survey)mMissionItem).getGridPoints();
			if (gridPoints != null && !gridPoints.isEmpty()) {
				pathPoints.addAll(gridPoints);
			}
			break;

		case STRUCTURE_SCANNER:
			StructureScanner survey = (StructureScanner)mMissionItem;
			pathPoints.addAll(survey.getPath());
			break;

		default:
			break;
		}

		return pathPoints;
	}

	public View getListViewItemView(Context context, ViewGroup parent) {
		final LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.fragment_editor_list_item, parent, false);

		TextView nameView = (TextView) view.findViewById(R.id.rowNameView);
		TextView altitudeView = (TextView) view.findViewById(R.id.rowAltitudeView);

		nameView.setText(String.format("%3d", mMission.getOrder(this)));

		final int leftDrawable = mMissionItem instanceof SplineWaypoint ? R.drawable.ic_mission_spline_wp
				: R.drawable.ic_mission_wp;
		altitudeView.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, 0, 0, 0);

		if (mMissionItem instanceof MissionItem.SpatialItem) {
			MissionItem.SpatialItem waypoint = (MissionItem.SpatialItem) mMissionItem;
            double altitude = waypoint.getCoordinate().getAltitude();
			altitudeView.setText(UnitManager.getUnitProvider().distanceToString(altitude));
            
            if(altitude < 0)
                altitudeView.setTextColor(Color.YELLOW);
            else
                altitudeView.setTextColor(Color.WHITE);

			try {
				double diff = mMission.getAltitudeDiffFromPreviousItem(this);
				if (diff > 0) {
					altitudeView.setTextColor(Color.RED);
				} else if (diff < 0) {
					altitudeView.setTextColor(Color.BLUE);
				}
			} catch (Exception e) {
				// Do nothing when last item doesn't have an altitude
			}
		} else if (mMissionItem instanceof Survey) {
            double altitude = ((Survey) mMissionItem).getSurveyDetail().getAltitude();
            String formattedAltitude = UnitManager.getUnitProvider().distanceToString(altitude);
			altitudeView.setText(formattedAltitude);

            if(altitude < 0)
                altitudeView.setTextColor(Color.YELLOW);
            else
                altitudeView.setTextColor(Color.WHITE);

		} else if (mMissionItem instanceof Takeoff) {
            double altitude = ((Takeoff) mMissionItem).getTakeoffAltitude();
			altitudeView.setText(UnitManager.getUnitProvider().distanceToString(altitude));

            if(altitude < 0)
                altitudeView.setTextColor(Color.YELLOW);
            else
                altitudeView.setTextColor(Color.WHITE);
		} else {
			altitudeView.setText("");
		}

		/*
		 * if (waypoint.getCmd().showOnMap()) {
		 * altitudeView.setText(String.format(Locale.ENGLISH, "%3.0fm",
		 * waypoint.getHeight())); } else { altitudeView.setText("-"); }
		 */
		// TODO fix the numbering
		// nameView.setText(String.format("%3d", waypoint.getOrder()));

		/*
		 * typeView.setText(waypoint.getCmd().getName());
		 * descView.setText(setupDescription(waypoint));
		 * 
		 * double distanceFromPrevPoint = waypoint.getDistanceFromPrevPoint();
		 * if(distanceFromPrevPoint != waypoint.UNKNOWN_DISTANCE) {
		 * distanceView.setText(String.format(Locale.ENGLISH, "%4.0fm",
		 * distanceFromPrevPoint)); } else { distanceView.setText("-"); }
		 */
		return view;
	}
}
