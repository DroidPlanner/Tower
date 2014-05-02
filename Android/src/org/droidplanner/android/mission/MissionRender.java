package org.droidplanner.android.mission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.droidplanner.android.fragments.helpers.MapPath;
import org.droidplanner.android.graphic.map.MarkerManager.MarkerSource;
import org.droidplanner.android.mission.item.MissionItemRender;
import org.droidplanner.android.mission.item.markers.MissionItemGenericMarkerSource;
import org.droidplanner.android.mission.item.markers.MissionItemMarkerSource;
import org.droidplanner.android.mission.item.markers.MissionItemSurveyMarkerSource;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.commands.Takeoff;
import org.droidplanner.core.mission.survey.Survey;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;
import org.droidplanner.core.mission.waypoints.Waypoint;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * This class is used to render a {@link org.droidplanner.core.mission.Mission} object on the Android
 * side.
 */
public class MissionRender implements MapPath.PathSource {

    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final int DEFAULT_WIDTH = 4;

    private final Mission mMission;

    /**
     * This is the mission path outline on the map.
     */
    private HashMap<GoogleMap, Polyline> mMissionPaths = new HashMap<GoogleMap, Polyline>();

    /**
     * Stores all the mission item renders for this mission render.
     */
    private final List<MissionItemRender> mMissionItems = new ArrayList<MissionItemRender>();

    public MissionSelection selection = new MissionSelection();

	public MissionRender(Mission mission){
        mMission = mission;
        refresh();
    }

    /**
     * Provides access to the class' mission instance.
     * @return {@link org.droidplanner.core.mission.Mission} object
     */
    public Mission getMission(){
        return mMission;
    }

    public List<MissionItemRender> getItems(){
        return mMissionItems;
    }

    /**
     * @return the map markers corresponding to this mission's command set.
     */
    public List<MarkerSource> getMarkers(){
        List<MarkerSource> markers = new ArrayList<MarkerSource>();
        for(MissionItemRender itemRender: mMissionItems){
            MissionItemGenericMarkerSource markerSource = itemRender.getMarkerSource();
            if (markerSource != null){
            	if (markerSource instanceof MissionItemMarkerSource) {
            		markers.add((MissionItemMarkerSource)markerSource);					
				}else if(markerSource instanceof MissionItemSurveyMarkerSource) {
					markers.addAll(((MissionItemSurveyMarkerSource)markerSource).getMarkers());
				}
            }
        }
        return markers;
    }

    /**
     * Update the state for this object based on the state of the Mission object.
     */
    public void refresh(){
        selection.mSelectedItems.clear();
        mMissionItems.clear();

        for(MissionItem item: mMission.getItems()){
            mMissionItems.add(new MissionItemRender(this, item));
        }

        selection.notifySelectionUpdate();
    }

    /**
     * Checks if this mission render contains the passed argument.
     * @param item mission item render object
     * @return true if this mission render contains the passed argument
     */
    public boolean contains(MissionItemRender item){
        return mMissionItems.contains(item);
    }

    /**
     * Removes a mission item from the set of mission items commands.
     * @param item item to remove
     */
    public void removeItem(MissionItemRender item){
        mMissionItems.remove(item);
        selection.mSelectedItems.remove(item);
        mMission.removeWaypoint(item.getMissionItem());
        selection.notifySelectionUpdate();
    }

    /**
     * Removes a set of mission items from the mission' set.
     * @param items list of items to remove
     */
    public void removeItemList(List<MissionItemRender> items){
    	
    	final List<MissionItem> toRemove = new ArrayList<MissionItem>(items.size());
    	for(MissionItemRender item: items){
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
    public void addSurveyPolygon(List<Coord2D> points){
        Survey survey = new Survey(mMission, points);
        mMissionItems.add(new MissionItemRender(this, survey));
        mMission.addWaypoint(survey);
    }

    /**
     * Add a set of waypoints generated around the passed 2D points.
     * TODO: replace Coord2D with library's classes such as android.graphics.Point
     * @param points list of points used to generate the mission waypoints
     */
    public void addWaypoints(List<Coord2D> points){
        final Altitude alt = mMission.getLastAltitude();
        final List<MissionItem> missionItemsToAdd = new ArrayList<MissionItem>(points.size());
        for(Coord2D point: points){
            Waypoint waypoint = new Waypoint(mMission, new Coord3D(point, alt));
            mMissionItems.add(new MissionItemRender(this, waypoint));
            missionItemsToAdd.add(waypoint);
        }

        mMission.addWaypoints(missionItemsToAdd);
    }

    /**
     * Add a waypoint generated around the passed 2D point.
     * TODO: replace Coord2D with library's classes such as android.graphics.Point
     * @param point point used to generate the mission waypoint
     */
    public void addWaypoint(Coord2D point){
        final Altitude alt = mMission.getLastAltitude();
        final Waypoint waypoint = new Waypoint(mMission, new Coord3D(point, alt));
        mMissionItems.add(new MissionItemRender(this, waypoint));
        mMission.addWaypoint(waypoint);
    }

    public void addTakeoff() {
		Takeoff takeoff = new Takeoff(mMission, new Altitude(10));
		mMissionItems.add(new MissionItemRender(this, takeoff));
        mMission.addWaypoint(takeoff);		
	}

	/**
     * Returns the order for the given argument in the mission set.
     * @param item
     * @return order of the given argument
     */
    public int getOrder(MissionItemRender item){
        return mMission.getOrder(item.getMissionItem());
    }

    /**
     * Updates the mission outline on the map.
     * @param map google map for which the mission outline should be updated
     */
    public void updateMissionPath(GoogleMap map){
        Polyline missionPath = mMissionPaths.get(map);
        if(missionPath == null) {
            PolylineOptions pathOptions = new PolylineOptions();
            pathOptions.color(DEFAULT_COLOR).width(DEFAULT_WIDTH);
            missionPath = map.addPolyline(pathOptions);
        }

        missionPath.setPoints(getPathPoints());
        mMissionPaths.put(map, missionPath);
    }

    /**
     * Updates a mission item render
     * @param oldItem mission item render to update
     * @param newItem new mission item render
     */
    public void replace(MissionItemRender oldItem, MissionItemRender newItem){
        final int index = mMissionItems.indexOf(oldItem);
        if(index == -1)
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
    public void reverse(){
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

    private List<MissionItemRender> getSubListToRotateUp(){
        final int from = mMissionItems.indexOf(selection.mSelectedItems.get(0));
        int to = from;
        do{
            if(mMissionItems.size() < to + 2)
                return mMissionItems.subList(0, 0);
        }while(selection.mSelectedItems.contains(mMissionItems.get(++to)));

        return mMissionItems.subList(from, to+1); //includes one unselected item
    }

    private List<MissionItemRender> getSubListToRotateDown(){
        final int from = mMissionItems.indexOf(selection.mSelectedItems.get(selection.mSelectedItems.size() - 1));
        int to = from;
        do{
            if(to < 1)
                return mMissionItems.subList(0, 0);
        } while(selection.mSelectedItems.contains(mMissionItems.get(--to)));

        return mMissionItems.subList(to, from + 1); // includes one unselected item.
    }

    public Length getAltitudeDiffFromPreviousItem(MissionItemRender waypointRender) throws
            IllegalArgumentException {
        MissionItem waypoint = waypointRender.getMissionItem();
        if(!(waypoint instanceof SpatialCoordItem))
            throw new IllegalArgumentException("Invalid mission item type.");

        return mMission.getAltitudeDiffFromPreviousItem((SpatialCoordItem) waypoint);
    }

    public Length getDistanceFromLastWaypoint(MissionItemRender waypointRender) throws
            IllegalArgumentException {
        MissionItem waypoint = waypointRender.getMissionItem();
        if(!(waypoint instanceof SpatialCoordItem))
            throw new IllegalArgumentException("Invalid mission item type.");

        return mMission.getDistanceFromLastWaypoint((SpatialCoordItem) waypoint);
    }

    @Override
    public List<LatLng> getPathPoints() {
        List<LatLng> pathPoints = new ArrayList<LatLng>();
        for(MissionItemRender missionItem: mMissionItems){
            pathPoints.addAll(missionItem.getPath());
        }
        return pathPoints;
    }

	public void removeSelection(MissionSelection missionSelection) {
		removeItemList(missionSelection.mSelectedItems);
	}

	public void move(MissionItemRender item, Coord2D position) {
			((SpatialCoordItem)item.getMissionItem()).setPosition(position);	
            mMission.notifyMissionUpdate();	
	}

}
