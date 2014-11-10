package org.droidplanner.android.proxy.mission;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.droidplanner.android.api.DroneApi;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.utils.analytics.GAUtils;

import com.google.android.gms.analytics.HitBuilders;
import com.ox3dr.services.android.lib.coordinate.LatLong;
import com.ox3dr.services.android.lib.coordinate.LatLongAlt;
import com.ox3dr.services.android.lib.drone.mission.Mission;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItem;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItem.SpatialItem;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItemType;
import com.ox3dr.services.android.lib.drone.mission.item.command.ReturnToLaunch;
import com.ox3dr.services.android.lib.drone.mission.item.command.Takeoff;
import com.ox3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.SplineWaypoint;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.Waypoint;
import com.ox3dr.services.android.lib.util.MathUtils;

/**
 * This class is used as a wrapper to {@link com.ox3dr.services.android.lib.drone.mission.Mission}
 * object on the Android side.
 */
public class MissionProxy implements DPMap.PathSource {

    private static final String CLAZZ_NAME = MissionProxy.class.getName();

    public static final String ACTION_MISSION_PROXY_UPDATE = CLAZZ_NAME + "" +
            ".ACTION_MISSION_PROXY_UPDATE";

    private static final double DEFAULT_ALTITUDE = 20; //meters

	/**
	 * Stores all the mission item renders for this mission render.
	 */
	private final List<MissionItemProxy> mMissionItems = new ArrayList<MissionItemProxy>();

    private final LocalBroadcastManager lbm;

	public MissionSelection selection = new MissionSelection();

	public MissionProxy(Context context) {
        lbm = LocalBroadcastManager.getInstance(context);
	}

    public void notifyMissionUpdate(){
        lbm.sendBroadcast(new Intent(ACTION_MISSION_PROXY_UPDATE));
    }

	public List<MissionItemProxy> getItems() {
		return mMissionItems;
	}

	/**
	 * @return the map markers corresponding to this mission's command set.
	 */
	public List<MarkerInfo> getMarkersInfos() {
		List<MarkerInfo> markerInfos = new ArrayList<MarkerInfo>();

		for (MissionItemProxy itemProxy : mMissionItems) {
			List<MarkerInfo> itemMarkerInfos = itemProxy.getMarkerInfos();
			if (itemMarkerInfos != null && !itemMarkerInfos.isEmpty()) {
				markerInfos.addAll(itemMarkerInfos);
			}
		}
		return markerInfos;
	}

	/**
	 * Update the state for this object based on the state of the Mission
	 * object.
	 */
	public void load(Mission mission) {
        if(mission == null)
            return;

		selection.mSelectedItems.clear();
		mMissionItems.clear();

		for (MissionItem item : mission.getMissionItems()) {
			mMissionItems.add(new MissionItemProxy(this, item));
		}

		selection.notifySelectionUpdate();
        notifyMissionUpdate();
	}

	/**
	 * Checks if this mission render contains the passed argument.
	 * 
	 * @param item
	 *            mission item render object
	 * @return true if this mission render contains the passed argument
	 */
	public boolean contains(MissionItemProxy item) {
		return mMissionItems.contains(item);
	}

	/**
	 * Removes a waypoint mission item from the set of mission items commands.
	 * 
	 * @param item
	 *            item to remove
	 */
	public void removeItem(MissionItemProxy item) {
		mMissionItems.remove(item);
		selection.mSelectedItems.remove(item);

        selection.notifySelectionUpdate();
        notifyMissionUpdate();
	}

	/**
	 * Removes a set of mission items from the mission' set.
	 * 
	 * @param items
	 *            list of items to remove
	 */
	public void removeItemList(List<MissionItemProxy> items) {
		mMissionItems.removeAll(items);
		selection.mSelectedItems.removeAll(items);

		selection.notifySelectionUpdate();
        notifyMissionUpdate();
	}

	/**
	 * Adds a survey mission item to the set.
	 * 
	 * @param points
	 *            2D points making up the survey
	 */
	public void addSurveyPolygon(DroneApi droneApi, List<LatLong> points) {
		Survey survey = new Survey();
        survey.setPolygonPoints(points);
		mMissionItems.add(new MissionItemProxy(this, survey));
        droneApi.updateSurveyMissionItem(survey);
	}

	/**
	 * Add a set of waypoints generated around the passed 2D points.
	 *
	 * @param points list of points used to generate the mission waypoints
	 */
	public void addWaypoints(List<LatLong> points) {
		final double alt = getLastAltitude();
		final List<MissionItem> missionItemsToAdd = new ArrayList<MissionItem>(points.size());
		for (LatLong point : points) {
			Waypoint waypoint = new Waypoint();
            waypoint.setCoordinate(new LatLongAlt(point.getLatitude(), point.getLongitude(),
                    (float) alt));
			missionItemsToAdd.add(waypoint);
		}

		addMissionItems(missionItemsToAdd);
	}

    private double getLastAltitude(){
        if(!mMissionItems.isEmpty()) {
            MissionItem lastItem = mMissionItems.get(mMissionItems.size() - 1).getMissionItem();
            if (lastItem instanceof MissionItem.SpatialItem)
                return ((MissionItem.SpatialItem) lastItem).getCoordinate().getAltitude();
        }

        return DEFAULT_ALTITUDE;
    }

	/**
	 * Add a set of spline waypoints generated around the passed 2D points.
	 * 
	 * @param points
	 *            list of points used as location for the spline waypoints
	 */
	public void addSplineWaypoints(List<LatLong> points) {
		final double alt = getLastAltitude();
		final List<MissionItem> missionItemsToAdd = new ArrayList<MissionItem>(points.size());
		for (LatLong point : points) {
			SplineWaypoint splineWaypoint = new SplineWaypoint();
            splineWaypoint.setCoordinate(new LatLongAlt(point.getLatitude(), point.getLongitude(),
                    (float) alt));
			missionItemsToAdd.add(splineWaypoint);
		}

		addMissionItems(missionItemsToAdd);
	}

	private void addMissionItems(List<MissionItem> missionItems) {
		for (MissionItem missionItem : missionItems) {
			mMissionItems.add(new MissionItemProxy(this, missionItem));
		}

        notifyMissionUpdate();
	}

	/**
	 * Add a waypoint generated around the passed 2D point. TODO: replace
	 * Coord2D with library's classes such as android.graphics.Point
	 * 
	 * @param point
	 *            point used to generate the mission waypoint
	 */
	public void addWaypoint(LatLong point) {
		final double alt = getLastAltitude();
		final Waypoint waypoint = new Waypoint();
        waypoint.setCoordinate(new LatLongAlt(point.getLatitude(), point.getLongitude(),
                (float) alt));
		addMissionItem(waypoint);
	}

	/**
	 * Add a spline waypoint generated around the passed 2D point.
	 * 
	 * @param point
	 *            point used as location for the spline waypoint.
	 */
	public void addSplineWaypoint(LatLong point) {
		final double alt = getLastAltitude();
		final SplineWaypoint splineWaypoint = new SplineWaypoint();
        splineWaypoint.setCoordinate(new LatLongAlt(point.getLatitude(), point.getLongitude(),
                (float) alt));
		addMissionItem(splineWaypoint);
	}

	private void addMissionItem(MissionItem missionItem) {
		mMissionItems.add(new MissionItemProxy(this, missionItem));
        notifyMissionUpdate();
	}

    private void addMissionItem(int index, MissionItem missionItem){
        mMissionItems.add(index, new MissionItemProxy(this, missionItem));
        notifyMissionUpdate();
    }

	public void addTakeoff() {
		Takeoff takeoff = new Takeoff();
        takeoff.setTakeoffAltitude(10);
        addMissionItem(takeoff);
	}
    public boolean hasTakeoffAndLandOrRTL() {
        if (mMissionItems.size() >= 2) {
            if (isFirstItemTakeoff() && isLastItemLandOrRTL()) {
                return true;
            }
        }
        return false;
    }

    public boolean isFirstItemTakeoff(){
        return !mMissionItems.isEmpty() && mMissionItems.get(0).getMissionItem().getType() ==
                MissionItemType.TAKEOFF;
    }

    public boolean isLastItemLandOrRTL(){
        final int itemsCount = mMissionItems.size();
        if(itemsCount == 0) return false;

        final MissionItemType itemType = mMissionItems.get(itemsCount -1).getMissionItem()
                .getType();
        return itemType == MissionItemType.RETURN_TO_LAUNCH || itemType == MissionItemType.LAND;
    }

    public void addTakeOffAndRTL(){
        if(!isFirstItemTakeoff()){
            double defaultAlt = Takeoff.DEFAULT_TAKEOFF_ALTITUDE;
            if(!mMissionItems.isEmpty()){
                MissionItem firstItem = mMissionItems.get(0).getMissionItem();
                if(firstItem instanceof MissionItem.SpatialItem)
                    defaultAlt = ((MissionItem.SpatialItem)firstItem).getCoordinate().getAltitude();
            }

            final Takeoff takeOff = new Takeoff();
            takeOff.setTakeoffAltitude(defaultAlt);
            addMissionItem(0, takeOff);
        }

        if(!isLastItemLandOrRTL()){
            final ReturnToLaunch rtl = new ReturnToLaunch();
            addMissionItem(rtl);
        }
    }

	/**
	 * Returns the order for the given argument in the mission set.
	 * 
	 * @param item
	 * @return order of the given argument
	 */
	public int getOrder(MissionItemProxy item) {
        return mMissionItems.indexOf(item) + 1;
	}

	/**
	 * Updates a mission item render
	 * 
	 * @param oldItem
	 *            mission item render to update
	 * @param newItem
	 *            new mission item render
	 */
	public void replace(MissionItemProxy oldItem, MissionItemProxy newItem) {
		final int index = mMissionItems.indexOf(oldItem);
		if (index == -1)
			return;

		mMissionItems.remove(index);
		mMissionItems.add(index, newItem);

		if (selection.selectionContains(oldItem)) {
			selection.removeItemFromSelection(oldItem);
			selection.addToSelection(newItem);
		}

        notifyMissionUpdate();
	}

    public void replaceAll(List<Pair<MissionItemProxy, MissionItemProxy>> oldNewList){
        if(oldNewList == null){
            return;
        }

        final int pairSize = oldNewList.size();
        if(pairSize == 0){
            return;
        }

        final List<MissionItemProxy> selectionsToRemove = new ArrayList<MissionItemProxy>(pairSize);
        final List<MissionItemProxy> itemsToSelect = new ArrayList<MissionItemProxy>(pairSize);

        for(int i = 0; i < pairSize; i++){
            final MissionItemProxy oldItem = oldNewList.get(i).first;
            final int index = mMissionItems.indexOf(oldItem);
            if(index == -1){
                continue;
            }

            final MissionItemProxy newItem = oldNewList.get(i).second;
            mMissionItems.remove(index);
            mMissionItems.add(index, newItem);

            if(selection.selectionContains(oldItem)){
                selectionsToRemove.add(oldItem);
                itemsToSelect.add(newItem);
            }
        }

        //Update the selection list.
        selection.removeItemsFromSelection(selectionsToRemove);
        selection.addToSelection(itemsToSelect);

        notifyMissionUpdate();
    }

	/**
	 * Reverse the order of the mission items renders.
	 */
	public void reverse() {
		Collections.reverse(mMissionItems);
	}

	public void clear() {
        selection.clearSelection();
		removeItemList(mMissionItems);
	}

	public double getAltitudeDiffFromPreviousItem(MissionItemProxy waypointRender) {
        final int itemsCount = mMissionItems.size();
        if(itemsCount < 2)
            return 0;

		MissionItem waypoint = waypointRender.getMissionItem();
		if (!(waypoint instanceof MissionItem.SpatialItem))
			return 0;

        final int index = mMissionItems.indexOf(waypointRender);
        if(index == -1 || index == 0)
            return 0;

		MissionItem previous = mMissionItems.get(index - 1).getMissionItem();
		if (previous instanceof MissionItem.SpatialItem) {
			return ((MissionItem.SpatialItem)waypoint).getCoordinate().getAltitude()
					- ((MissionItem.SpatialItem) previous).getCoordinate().getAltitude();
		}

        return 0;
	}

	public double getDistanceFromLastWaypoint(MissionItemProxy waypointRender)
			throws IllegalArgumentException {
        if(mMissionItems.size() < 2)
            return 0;

		MissionItem waypoint = waypointRender.getMissionItem();
		if (!(waypoint instanceof MissionItem.SpatialItem))
			return 0;

        final int index = mMissionItems.indexOf(waypointRender);
        if(index == -1 || index == 0)
            return 0;

        MissionItem previous = mMissionItems.get(index - 1).getMissionItem();
        if(previous instanceof MissionItem.SpatialItem){
            return MathUtils.getDistance(((MissionItem.SpatialItem) waypoint).getCoordinate(),
                    ((MissionItem.SpatialItem) previous).getCoordinate());
        }

		return 0;
	}

	@Override
	public List<LatLong> getPathPoints() {
		if (mMissionItems.isEmpty()) {
			return Collections.emptyList();
		}

		// Partition the mission items into spline/non-spline buckets.
		final List<Pair<Boolean, List<MissionItemProxy>>> bucketsList = new ArrayList<Pair<Boolean, List<MissionItemProxy>>>();

		boolean isSpline = false;
		List<MissionItemProxy> currentBucket = new ArrayList<MissionItemProxy>();
		for (MissionItemProxy missionItemProxy : mMissionItems) {

			if (missionItemProxy.getMissionItem() instanceof SplineWaypoint) {
				if (!isSpline) {
					if (!currentBucket.isEmpty()) {
						// Get the last item from the current bucket. It will
						// become the first
						// anchor point for the spline path.
						final MissionItemProxy lastItem = currentBucket
								.get(currentBucket.size() - 1);

						// Store the previous item bucket.
						bucketsList.add(new Pair<Boolean, List<MissionItemProxy>>(Boolean.FALSE,
								currentBucket));

						// Create a new bucket for this category and update
						// 'isSpline'
						currentBucket = new ArrayList<MissionItemProxy>();
						currentBucket.add(lastItem);
					}

					isSpline = true;
				}

				// Add the current element into the bucket
				currentBucket.add(missionItemProxy);
			} else {
				if (isSpline) {

					// Add the current item to the spline bucket. It will act as
					// the end anchor
					// point for the spline path.
					if (!currentBucket.isEmpty()) {
						currentBucket.add(missionItemProxy);

						// Store the previous item bucket.
						bucketsList.add(new Pair<Boolean, List<MissionItemProxy>>(Boolean.TRUE,
								currentBucket));

						currentBucket = new ArrayList<MissionItemProxy>();
					}

					isSpline = false;
				}

				// Add the current element into the bucket
				currentBucket.add(missionItemProxy);
			}
		}

		bucketsList.add(new Pair<Boolean, List<MissionItemProxy>>(isSpline, currentBucket));

		final List<LatLong> pathPoints = new ArrayList<LatLong>();
		LatLong lastPoint = null;

		for (Pair<Boolean, List<MissionItemProxy>> bucketEntry : bucketsList) {

			final List<MissionItemProxy> bucket = bucketEntry.second;
			if (bucketEntry.first) {
				final List<LatLong> splinePoints = new ArrayList<LatLong>();
				for (MissionItemProxy missionItemProxy : bucket) {
					splinePoints.addAll(missionItemProxy.getPath(lastPoint));

					if (!splinePoints.isEmpty()) {
						lastPoint = splinePoints.get(splinePoints.size() - 1);
					}
				}

				pathPoints.addAll(MathUtils.SplinePath.process(splinePoints));
			} else {
				for (MissionItemProxy missionItemProxy : bucket) {
					pathPoints.addAll(missionItemProxy.getPath(lastPoint));

					if (!pathPoints.isEmpty()) {
						lastPoint = pathPoints.get(pathPoints.size() - 1);
					}
				}
			}
		}

		return pathPoints;
	}

	public void removeSelection(MissionSelection missionSelection) {
		removeItemList(missionSelection.mSelectedItems);
	}

	public void move(MissionItemProxy item, LatLong position) {
        MissionItem missionItem = item.getMissionItem();
        if(missionItem instanceof SpatialItem){
            SpatialItem spatialItem = (SpatialItem) missionItem;
            spatialItem.setCoordinate(new LatLongAlt(position.getLatitude(),
                    position.getLongitude(), spatialItem.getCoordinate().getAltitude()));
            notifyMissionUpdate();
        }
	}

	public List<LatLong> getVisibleCoords() {
		return getVisibleCoords(mMissionItems);
	}

    public static List<LatLong> getVisibleCoords(List<MissionItemProxy> mipList){
        final List<LatLong> coords = new ArrayList<LatLong>();

        if(mipList == null || mipList.isEmpty()){
            return coords;
        }

        for (MissionItemProxy itemProxy : mipList) {
            MissionItem item = itemProxy.getMissionItem();
            if (!(item instanceof SpatialItem))
                continue;

            final LatLong coordinate = ((SpatialItem) item).getCoordinate();
            if (coordinate.getLatitude() == 0 || coordinate.getLongitude() == 0)
                continue;

            coords.add(coordinate);
        }

        return coords;
    }

    public void sendMissionToAPM(DroneApi droneApi){
        final Mission mission = new Mission();
        final int missionItemsCount = mMissionItems.size();

        String missionItemsList = "[";
        if(missionItemsCount > 0){
            boolean isFirst = true;
            for(MissionItemProxy itemProxy: mMissionItems){
                if(isFirst)
                    isFirst = false;
                else
                    missionItemsList += ", ";

                missionItemsList += itemProxy.getMissionItem().getType().getLabel();

                mission.addMissionItem(itemProxy.getMissionItem());
            }
        }

        droneApi.setMission(mission, true);

        missionItemsList += "]";

        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(GAUtils.Category.MISSION_PLANNING)
                .setAction("Mission sent to drone")
                .setLabel("Mission items: " + missionItemsList);
        GAUtils.sendEvent(eventBuilder);

        //Send an event for the created mission
        eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(GAUtils.Category.MISSION_PLANNING)
                .setAction("Mission sent to drone")
                .setLabel("Mission items count")
                .setValue(missionItemsCount);
        GAUtils.sendEvent(eventBuilder);
    }

	public double getMissionLength() {
		List<LatLong> points = getPathPoints();
        double length = 0;
		if (points.size()>1) {
			for (int i = 1; i < points.size(); i++) {
				length += MathUtils.getDistance(points.get(i-1), points.get(i));
			}
		}

        return length;
	}

    public void makeAndUploadDronie(DroneApi droneApi) {
        droneApi.generateDronie();
    }
}
