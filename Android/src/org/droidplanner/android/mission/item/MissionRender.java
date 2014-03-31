package org.droidplanner.android.mission.item;

import com.google.android.gms.maps.model.LatLng;

import org.droidplanner.android.fragments.helpers.MapPath;
import org.droidplanner.android.graphic.map.MarkerManager;
import org.droidplanner.android.graphic.map.MarkerManager.MarkerSource;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.survey.Survey;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;
import org.droidplanner.core.mission.waypoints.Waypoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is used to render a {@link org.droidplanner.core.mission.Mission} object on the Android
 * side.
 */
public class MissionRender implements MapPath.PathSource {

    private final Mission mMission;

    /**
     * Stores all the mission item renders for this mission render.
     */
    private final List<MissionItemRender> mMissionItems = new ArrayList<MissionItemRender>();

    /**
     * Stores the selected mission items renders.
     */
    private final List<MissionItemRender> mSelectedItems = new ArrayList<MissionItemRender>();

    /**
     * Stores the list of selection update listeners.
     */
    private final List<OnSelectionUpdateListener> mSelectionsListeners = new
            ArrayList<OnSelectionUpdateListener>();

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
            markers.add(itemRender);
        }
        return markers;
    }

    /**
     * Update the state for this object based on the state of the Mission object.
     */
    public void refresh(){
        mSelectedItems.clear();
        mMissionItems.clear();

        for(MissionItem item: mMission.getItems()){
            mMissionItems.add(new MissionItemRender(this, item));
        }

        notifySelectionUpdate();
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
     * Removes a waypoint mission item from the set of mission items commands.
     * @param item item to remove
     */
    public void removeWaypoint(MissionItemRender item){
        mMissionItems.remove(item);
        mSelectedItems.remove(item);
        mMission.removeWaypoint(item.getMissionItem());
        notifySelectionUpdate();
    }

    /**
     * Removes a set of mission items from the mission' set.
     * @param items list of items to remove
     */
    public void removeWaypoints(List<MissionItemRender> items){
        mMissionItems.removeAll(items);
        mSelectedItems.removeAll(items);

        final List<MissionItem> toRemove = new ArrayList<MissionItem>(items.size());
        for(MissionItemRender item: items){
            toRemove.add(item.getMissionItem());
        }

        mMission.removeWaypoints(toRemove);
        notifySelectionUpdate();
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

    /**
     * Returns the order for the given argument in the mission set.
     * @param item
     * @return order of the given argument
     */
    public int getOrder(MissionItemRender item){
        return mMission.getOrder(item.getMissionItem());
    }

    /**
     * Updates a mission item render
     * @param oldItem mission item render to update
     * @param newItem new mission item render
     */
    public void replace(MissionItemRender oldItem, MissionItemRender newItem){
        final int index = mMissionItems.indexOf(oldItem);
        if(selectionContains(oldItem)){
            removeItemFromSelection(oldItem);
            addToSelection(newItem);
        }

        mMissionItems.remove(index);
        mMissionItems.add(index, newItem);

        //Update the mission object
        mMission.replace(oldItem.getMissionItem(), newItem.getMissionItem());
    }

    /**
     * Reverse the order of the mission items renders.
     */
    public void reverse(){
        Collections.reverse(mMissionItems);
        mMission.reverse();
    }

    /**
     * Deselects all mission items renders
     */
    public void clearSelection() {
        mSelectedItems.clear();
        notifySelectionUpdate();
    }

    /**
     * Checks if the passed mission item render is selected.
     * @param item mission item render to check for selection
     * @return true if selected
     */
    public boolean selectionContains(MissionItemRender item) {
        return mSelectedItems.contains(item);
    }

    /**
     * Selects the given list of mission items renders
     * TODO: check if the given mission items renders belong to this mission render
     * @param items list of mission items renders to select.
     */
    public void addToSelection(List<MissionItemRender> items) {
        mSelectedItems.addAll(items);
        notifySelectionUpdate();
    }

    /**
     * Adds the given mission item render to the selected list.
     * TODO: check the mission item render belongs to this mission render
     * @param item mission item render to add to the selected list.
     */
    public void addToSelection(MissionItemRender item) {
        mSelectedItems.add(item);
        notifySelectionUpdate();
    }

    /**
     * Selects only the given mission item render.
     * TODO: check the mission item render belongs to this mission render
     * @param item mission item render to select.
     */
    public void setSelectionTo(MissionItemRender item) {
        mSelectedItems.clear();
        mSelectedItems.add(item);
        notifySelectionUpdate();
    }

    /**
     * Selects only the given mission items renders.
     * TODO: check the mission items renders belong to this mission render
     * @param items list of mission items renders to select.
     */
    public void setSelectionTo(List<MissionItemRender> items){
        mSelectedItems.clear();
        mSelectedItems.addAll(items);
        notifySelectionUpdate();
    }

    /**
     * Removes the given mission item render from the selected list.
     * TODO: check the argument belongs to this mission render
     * @param item mission item rendere to remove from the selected list
     */
    public void removeItemFromSelection(MissionItemRender item) {
        mSelectedItems.remove(item);
        notifySelectionUpdate();
    }

    /**
     * @return the list of selected mission items renders
     */
    public List<MissionItemRender> getSelected() {
        return mSelectedItems;
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
        if(mSelectedItems.size() > 0 || mSelectedItems.size() < mMissionItems.size()){
            Collections.sort(mSelectedItems);
            if(moveUp){
                Collections.rotate(getSubListToRotateUp(), 1);
            }
            else{
                Collections.rotate(getSubListToRotateDown(), -1);
            }

            notifySelectionUpdate();
            mMission.notifyMissionUpdate();
        }
    }

    private List<MissionItemRender> getSubListToRotateUp(){
        final int from = mMissionItems.indexOf(mSelectedItems.get(0));
        int to = from;
        do{
            if(mMissionItems.size() < to + 2)
                return mMissionItems.subList(0, 0);
        }while(mSelectedItems.contains(mMissionItems.get(++to)));

        return mMissionItems.subList(from, to+1); //includes one unselected item
    }

    private List<MissionItemRender> getSubListToRotateDown(){
        final int from = mMissionItems.indexOf(mSelectedItems.get(mSelectedItems.size() - 1));
        int to = from;
        do{
            if(to < 1)
                return mMissionItems.subList(0, 0);
        } while(mSelectedItems.contains(mMissionItems.get(--to)));

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

    /**
     * Adds given argument to the list of selection update listeners.
     * @param listener
     */
    public void addSelectionUpdateListener(OnSelectionUpdateListener listener){
        mSelectionsListeners.add(listener);
    }

    private void notifySelectionUpdate(){
        for(OnSelectionUpdateListener listener: mSelectionsListeners)
            listener.onSelectionUpdate(mSelectedItems);
    }

    /**
     * Removes given argument from the list of selection update listeners.
     * @param listener
     */
    public void removeSelectionUpdateListener(OnSelectionUpdateListener listener){
        mSelectionsListeners.remove(listener);
    }

    @Override
    public List<LatLng> getPathPoints() {
        return null;
    }

    /**
     * Classes interested in getting selection update should implement this interface.
     */
    public interface OnSelectionUpdateListener {
        public void onSelectionUpdate(List<MissionItemRender> selected);
    }

}
