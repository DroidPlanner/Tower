package org.droidplanner.android.proxy.mission;

import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.geoTools.spline.SplinePath;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.commands.Takeoff;
import org.droidplanner.core.mission.survey.Survey;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;
import org.droidplanner.core.mission.waypoints.SplineWaypoint;
import org.droidplanner.core.mission.waypoints.Waypoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is used to render a {@link org.droidplanner.core.mission.Mission} object on the
 * Android side.
 */
public class MissionProxy implements DPMap.PathSource {

    private final Mission mMission;

    /**
     * Stores all the mission item renders for this mission render.
     */
    private final List<MissionItemProxy> mMissionItems = new ArrayList<MissionItemProxy>();

    public MissionSelection selection = new MissionSelection();

    public MissionProxy(Mission mission) {
        mMission = mission;
        refresh();
    }

    /**
     * Provides access to the class' mission instance.
     * @return {@link org.droidplanner.core.mission.Mission} object
     */
    public Mission getMission() {
        return mMission;
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
     * Update the state for this object based on the state of the Mission object.
     */
    public void refresh(){
        selection.mSelectedItems.clear();
        mMissionItems.clear();

        for (MissionItem item : mMission.getItems()) {
            mMissionItems.add(new MissionItemProxy(this, item));
        }

        selection.notifySelectionUpdate();
    }

    /**
     * Checks if this mission render contains the passed argument.
     * @param item mission item render object
     * @return true if this mission render contains the passed argument
     */
    public boolean contains(MissionItemProxy item) {
        return mMissionItems.contains(item);
    }

    /**
     * Removes a waypoint mission item from the set of mission items commands.
     * @param item item to remove
     */
    public void removeItem(MissionItemProxy item) {
        mMissionItems.remove(item);
        selection.mSelectedItems.remove(item);
        mMission.removeWaypoint(item.getMissionItem());
        selection.notifySelectionUpdate();
    }

    /**
     * Removes a set of mission items from the mission' set.
     * @param items list of items to remove
     */
    public void removeItemList(List<MissionItemProxy> items){
    	
    	final List<MissionItem> toRemove = new ArrayList<MissionItem>(items.size());
    	for(MissionItemProxy item: items){
    		toRemove.add(item.getMissionItem());
    	}
    	
    	mMissionItems.removeAll(items);
    	selection.mSelectedItems.removeAll(items);
        mMission.removeWaypoints(toRemove);
        selection.notifySelectionUpdate();
    }

    /**
     * Adds a survey mission item to the set.
     * @param points 2D points making up the survey
     */
    public void addSurveyPolygon(List<Coord2D> points) {
        Survey survey = new Survey(mMission, points);
        mMissionItems.add(new MissionItemProxy(this, survey));
        mMission.addMissionItem(survey);
    }

    /**
     * Add a set of waypoints generated around the passed 2D points.
     * TODO: replace Coord2D with library's classes such as android.graphics.Point
     * @param points list of points used to generate the mission waypoints
     */
    public void addWaypoints(List<Coord2D> points) {
        final Altitude alt = mMission.getLastAltitude();
        final List<MissionItem> missionItemsToAdd = new ArrayList<MissionItem>(points.size());
        for (Coord2D point : points) {
            Waypoint waypoint = new Waypoint(mMission, new Coord3D(point, alt));
            missionItemsToAdd.add(waypoint);
        }

        addMissionItems(missionItemsToAdd);
    }

    /**
     * Add a set of spline waypoints generated around the passed 2D points.
     * @param points list of points used as location for the spline waypoints
     */
    public void addSplineWaypoints(List<Coord2D> points){
        final Altitude alt = mMission.getLastAltitude();
        final List<MissionItem> missionItemsToAdd = new ArrayList<MissionItem>(points.size());
        for (Coord2D point : points) {
            SplineWaypoint splineWaypoint = new SplineWaypoint(mMission, new Coord3D(point, alt));
            missionItemsToAdd.add(splineWaypoint);
        }

        addMissionItems(missionItemsToAdd);
    }

    private void addMissionItems(List<MissionItem> missionItems){
        for(MissionItem missionItem: missionItems){
            mMissionItems.add(new MissionItemProxy(this, missionItem));
        }
        mMission.addMissionItems(missionItems);
    }

    /**
     * Add a waypoint generated around the passed 2D point.
     * TODO: replace Coord2D with library's classes such as android.graphics.Point
     * @param point point used to generate the mission waypoint
     */
    public void addWaypoint(Coord2D point) {
        final Altitude alt = mMission.getLastAltitude();
        final Waypoint waypoint = new Waypoint(mMission, new Coord3D(point, alt));
        addMissionItem(waypoint);
    }

    /**
     * Add a spline waypoint generated around the passed 2D point.
     * @param point point used as location for the spline waypoint.
     */
    public void addSplineWaypoint(Coord2D point){
        final Altitude alt = mMission.getLastAltitude();
        final SplineWaypoint splineWaypoint = new SplineWaypoint(mMission, new Coord3D(point, alt));
        addMissionItem(splineWaypoint);
    }

    private void addMissionItem(MissionItem missionItem){
        mMissionItems.add(new MissionItemProxy(this, missionItem));
        mMission.addMissionItem(missionItem);
    }

    public void addTakeoff() {
		Takeoff takeoff = new Takeoff(mMission, new Altitude(10));
		mMissionItems.add(new MissionItemProxy(this, takeoff));
        mMission.addMissionItem(takeoff);
	}

	/**
     * Returns the order for the given argument in the mission set.
     * @param item
     * @return order of the given argument
     */
    public int getOrder(MissionItemProxy item) {
        return mMission.getOrder(item.getMissionItem());
    }

    /**
     * Updates a mission item render
     * @param oldItem mission item render to update
     * @param newItem new mission item render
     */
    public void replace(MissionItemProxy oldItem, MissionItemProxy newItem) {
        final int index = mMissionItems.indexOf(oldItem);
        if (index == -1)
            return;

        mMissionItems.remove(index);
        mMissionItems.add(index, newItem);

        //Update the mission object
        mMission.replace(oldItem.getMissionItem(), newItem.getMissionItem());

        if(selection.selectionContains(oldItem)){
            selection.removeItemFromSelection(oldItem);
            selection.addToSelection(newItem);
        }
    }

    /**
     * Reverse the order of the mission items renders.
     */
    public void reverse() {
        Collections.reverse(mMissionItems);
        mMission.reverse();
    }

    public void clear() {
    	removeItemList(mMissionItems);	
	}

	/**
     * Moves the selected objects up or down into the mission listing
     *
     * Think of it as pushing the selected objects, while you can only move a
     * single unselected object per turn.
     *
     * @param moveUp
     *            true to move up, but can be false to move down
     */
    public void moveSelection(boolean moveUp){
        if(selection.mSelectedItems.size() > 0 || selection.mSelectedItems.size() < mMissionItems.size()){
            Collections.sort(selection.mSelectedItems);
            if(moveUp){
                Collections.rotate(getSubListToRotateUp(), 1);
            }
            else{
                Collections.rotate(getSubListToRotateDown(), -1);
            }

            selection.notifySelectionUpdate();
            mMission.notifyMissionUpdate();
        }
    }

    private List<MissionItemProxy> getSubListToRotateUp() {
        final int from = mMissionItems.indexOf(selection.mSelectedItems.get(0));
        int to = from;
        do{
            if(mMissionItems.size() < to + 2)
                return mMissionItems.subList(0, 0);
        }while(selection.mSelectedItems.contains(mMissionItems.get(++to)));

        return mMissionItems.subList(from, to + 1); //includes one unselected item
    }

    private List<MissionItemProxy> getSubListToRotateDown() {
        final int from = mMissionItems.indexOf(selection.mSelectedItems.get(selection.mSelectedItems
                .size() -  1));
        int to = from;
        do{
            if(to < 1)
                return mMissionItems.subList(0, 0);
        } while(selection.mSelectedItems.contains(mMissionItems.get(--to)));

        return mMissionItems.subList(to, from + 1); // includes one unselected item.
    }

    public Length getAltitudeDiffFromPreviousItem(MissionItemProxy waypointRender) throws
            IllegalArgumentException {
        MissionItem waypoint = waypointRender.getMissionItem();
        if(!(waypoint instanceof SpatialCoordItem))
            throw new IllegalArgumentException("Invalid mission item type.");

        return mMission.getAltitudeDiffFromPreviousItem((SpatialCoordItem) waypoint);
    }

    public Length getDistanceFromLastWaypoint(MissionItemProxy waypointRender) throws
            IllegalArgumentException {
        MissionItem waypoint = waypointRender.getMissionItem();
        if(!(waypoint instanceof SpatialCoordItem))
            throw new IllegalArgumentException("Invalid mission item type.");

        return mMission.getDistanceFromLastWaypoint((SpatialCoordItem) waypoint);
    }

    @Override
    public List<Coord2D> getPathPoints() {
        if(mMissionItems.isEmpty()){
            return Collections.emptyList();
        }

        //Partition the mission items into spline/non-spline buckets.
        final List<Pair<Boolean, List<MissionItemProxy>>> bucketsList = new
                ArrayList<Pair<Boolean, List<MissionItemProxy>>>();

        boolean isSpline = false;
        List<MissionItemProxy> currentBucket = new ArrayList<MissionItemProxy>();
        for (MissionItemProxy missionItemProxy: mMissionItems) {

            if (missionItemProxy.getMissionItem() instanceof SplineWaypoint){
                if(!isSpline){
                    if(!currentBucket.isEmpty()) {
                        //Get the last item from the current bucket. It will become the first
                        // anchor point for the spline path.
                        final MissionItemProxy lastItem = currentBucket.get(currentBucket.size()
                                -1);

                        //Store the previous item bucket.
                        bucketsList.add(new Pair<Boolean, List<MissionItemProxy>>(Boolean.FALSE,
                                currentBucket));

                        //Create a new bucket for this category and update 'isSpline'
                        currentBucket = new ArrayList<MissionItemProxy>();
                        currentBucket.add(lastItem);
                    }

                    isSpline = true;
                }

                //Add the current element into the bucket
                currentBucket.add(missionItemProxy);
            }
            else{
                if(isSpline){

                    //Add the current item to the spline bucket. It will act as the end anchor
                    // point for the spline path.
                    if(!currentBucket.isEmpty()) {
                        currentBucket.add(missionItemProxy);

                        //Store the previous item bucket.
                        bucketsList.add(new Pair<Boolean, List<MissionItemProxy>>(Boolean.TRUE,
                                currentBucket));

                        currentBucket = new ArrayList<MissionItemProxy>();
                    }

                    isSpline = false;
                }

                //Add the current element into the bucket
                currentBucket.add(missionItemProxy);
            }
        }

        bucketsList.add(new Pair<Boolean, List<MissionItemProxy>>(isSpline, currentBucket));

        final List<Coord2D> pathPoints = new ArrayList<Coord2D>();
        Coord2D lastPoint = null;

        for(Pair<Boolean, List<MissionItemProxy>> bucketEntry : bucketsList){

            final List<MissionItemProxy> bucket = bucketEntry.second;
            if(bucketEntry.first){
                final List<Coord2D> splinePoints = new ArrayList<Coord2D>();
                for(MissionItemProxy missionItemProxy: bucket){
                    splinePoints.addAll(missionItemProxy.getPath(lastPoint));

                    if(!splinePoints.isEmpty()){
                        lastPoint = splinePoints.get(splinePoints.size() -1);
                    }
                }

                pathPoints.addAll(SplinePath.process(splinePoints));
            }
            else{
                for(MissionItemProxy missionItemProxy : bucket){
                    pathPoints.addAll(missionItemProxy.getPath(lastPoint));

                    if(!pathPoints.isEmpty()){
                        lastPoint = pathPoints.get(pathPoints.size() -1);
                    }
                }
            }
        }

        return pathPoints;
    }

	public void removeSelection(MissionSelection missionSelection) {
		removeItemList(missionSelection.mSelectedItems);
	}

	public void move(MissionItemProxy item, Coord2D position) {
			((SpatialCoordItem)item.getMissionItem()).setPosition(position);	
            mMission.notifyMissionUpdate();	
	}

}
