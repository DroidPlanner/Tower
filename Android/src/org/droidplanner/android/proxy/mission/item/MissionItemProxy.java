package org.droidplanner.android.proxy.mission.item;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.SplineSurvey;
import com.o3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Circle;
import com.o3dr.services.android.lib.util.MathUtils;

import org.droidplanner.android.proxy.mission.MissionProxy;

import java.util.ArrayList;
import java.util.List;

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

	public MissionItemProxy(MissionProxy mission, MissionItem missionItem) {

		mMission = mission;
		mMissionItem = missionItem;

        final Drone.OnMissionItemsBuiltCallback missionItemBuiltListener = new Drone.OnMissionItemsBuiltCallback() {
            @Override
            public void onMissionItemsBuilt(MissionItem.ComplexItem[] complexItems) {
                mMission.notifyMissionUpdate(false);
            }
        };

		if(mMissionItem instanceof SplineSurvey){
			mMission.getDrone().buildMissionItemsAsync(new SplineSurvey[]{(SplineSurvey) mMissionItem}, missionItemBuiltListener);
		}else if(mMissionItem instanceof Survey){
            mMission.getDrone().buildMissionItemsAsync(new Survey[]{(Survey) mMissionItem}, missionItemBuiltListener);
        }
        else if(mMissionItem instanceof StructureScanner){
            mMission.getDrone().buildMissionItemsAsync(new StructureScanner[]{(StructureScanner) mMissionItem}, missionItemBuiltListener);
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
                Circle circle = (Circle) mMissionItem;
                LatLongAlt circleCenter = circle.getCoordinate();
                double circleRadius = circle.getRadius();
                double startHeading = previousPoint == null ? 0
                    : MathUtils.getHeadingFromCoordinates(circleCenter, previousPoint);
                int circleTurnsAngle = 360 * circle.getTurns();
                for (int i = 0; i <= circleTurnsAngle; i += 10) {
                    pathPoints.add(MathUtils.newCoordFromBearingAndDistance(circleCenter,
                        startHeading + i, circleRadius));
                }
                break;

			case SPLINE_SURVEY:
			case SURVEY:
				List<LatLong> gridPoints = ((Survey) mMissionItem).getGridPoints();
				if (gridPoints != null && !gridPoints.isEmpty()) {
					pathPoints.addAll(gridPoints);
				}
				break;

			case STRUCTURE_SCANNER:
				StructureScanner survey = (StructureScanner) mMissionItem;
				pathPoints.addAll(survey.getPath());
				break;

			default:
				break;
		}

		return pathPoints;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MissionItemProxy)) {
			return false;
		}

		MissionItemProxy that = (MissionItemProxy) o;

		if (mMissionItem != null ? !mMissionItem.equals(that.mMissionItem) : that.mMissionItem != null) {
			return false;
		}
		if (mMission != null ? !mMission.equals(that.mMission) : that.mMission != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = mMissionItem != null ? mMissionItem.hashCode() : 0;
		result = 31 * result + (mMission != null ? mMission.hashCode() : 0);
		return result;
	}
}
