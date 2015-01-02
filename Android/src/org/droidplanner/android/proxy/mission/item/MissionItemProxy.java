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

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
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

    private final Drone.OnMissionItemsBuiltCallback missionItemBuiltListener = new Drone.OnMissionItemsBuiltCallback() {
        @Override
        public void onMissionItemsBuilt(MissionItem.ComplexItem[] complexItems) {
        }
    };


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
            mMission.getDrone().buildMissionItemsAsync(missionItemBuiltListener, (Survey) mMissionItem);
        }
        else if(mMissionItem instanceof StructureScanner){
            mMission.getDrone().buildMissionItemsAsync(missionItemBuiltListener, (StructureScanner) mMissionItem);
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

    public MissionProxy getMission(){return mMission;}

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
}
