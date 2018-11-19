package co.aerobotics.android.proxy.mission;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.CircularArray;
import android.util.Pair;
import android.widget.Toast;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.data.AeroviewPolygons;
import co.aerobotics.android.data.BoundaryDetail;
import co.aerobotics.android.data.QuickHullLatLng;
import co.aerobotics.android.data.SQLiteDatabaseHandler;
import co.aerobotics.android.graphic.map.PolygonData;
import co.aerobotics.android.maps.DPMap;
import co.aerobotics.android.proxy.mission.item.MissionItemProxy;
import co.aerobotics.android.utils.Utils;
import co.aerobotics.android.utils.analytics.GAUtils;
import co.aerobotics.android.utils.prefs.DroidPlannerPrefs;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.maps.model.LatLng;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem.SpatialItem;
import com.o3dr.services.android.lib.drone.mission.item.command.ChangeSpeed;
import com.o3dr.services.android.lib.drone.mission.item.command.ReturnToLaunch;
import com.o3dr.services.android.lib.drone.mission.item.command.Takeoff;
import com.o3dr.services.android.lib.drone.mission.item.complex.SplineSurvey;
import com.o3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.complex.SurveyDetail;
import com.o3dr.services.android.lib.drone.mission.item.spatial.BaseSpatialItem;
import com.o3dr.services.android.lib.drone.mission.item.spatial.RegionOfInterest;
import com.o3dr.services.android.lib.drone.mission.item.spatial.SplineWaypoint;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.util.MathUtils;

import co.aerobotics.android.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is used as a wrapper to {@link com.o3dr.services.android.lib.drone.mission.Mission}
 * object on the Android side.
 */
public class MissionProxy implements DPMap.PathSource {

    public static final String ACTION_MISSION_PROXY_UPDATE = Utils.PACKAGE_NAME + ".ACTION_MISSION_PROXY_UPDATE";
    public static final String MISSION_CLEARED = Utils.PACKAGE_NAME + ".ACTION_MISSION_CLEARED";
    private static final int UNDO_BUFFER_SIZE = 30;

    private static final IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.MISSION_DRONIE_CREATED);
        eventFilter.addAction(AttributeEvent.MISSION_UPDATED);
        eventFilter.addAction(AttributeEvent.MISSION_RECEIVED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case AttributeEvent.MISSION_DRONIE_CREATED:
                case AttributeEvent.MISSION_UPDATED:
                case AttributeEvent.MISSION_RECEIVED:
                    Mission droneMission = drone.getAttribute(AttributeType.MISSION);
                    load(droneMission);
                    break;
            }
        }
    };

    private final Drone.OnMissionItemsBuiltCallback missionItemsBuiltListener = new Drone.OnMissionItemsBuiltCallback() {
        @Override
        public void onMissionItemsBuilt(MissionItem.ComplexItem[] complexItems) {
            notifyMissionUpdate(false);
        }
    };

    /**
     * Stores all the mission item renders for this mission render.
     */
    private final List<MissionItemProxy> missionItemProxies = new ArrayList<MissionItemProxy>();

    private final LocalBroadcastManager lbm;
    private final DroidPlannerPrefs dpPrefs;
    private final Context context;
    private final DroidPlannerApp dpApp;
    private final Drone drone;

    private final CircularArray<Mission> undoBuffer = new CircularArray<>(UNDO_BUFFER_SIZE);

    public Mission currentMission;
    public MissionSelection selection = new MissionSelection();

    public MissionProxy(DroidPlannerApp app, Drone drone) {
        this.dpApp = app;
        this.context = app.getApplicationContext();
        this.drone = drone;
        this.currentMission = generateMission(true);
        lbm = LocalBroadcastManager.getInstance(context);
        lbm.registerReceiver(eventReceiver, eventFilter);

        dpPrefs = DroidPlannerPrefs.getInstance(context);
    }

    public void notifyMissionUpdate() {
        notifyMissionUpdate(true);
    }

    public boolean canUndoMission() {
        return !undoBuffer.isEmpty();
    }

    public void undoMission() {
        if (!canUndoMission())
            throw new IllegalStateException("Invalid state for mission undoing.");

        Mission previousMission = undoBuffer.popLast();
        load(previousMission, false);
    }

    public void notifyMissionUpdate(boolean saveMission) {
        if (saveMission && currentMission != null) {
            //Store the current state of the mission.
            undoBuffer.addLast(currentMission);
        }

        currentMission = generateMission(true);
        lbm.sendBroadcast(new Intent(ACTION_MISSION_PROXY_UPDATE));
    }

    public List<MissionItemProxy> getItems() {
        return missionItemProxies;
    }

    private MissionItem[] getMissionItems() {
        List<MissionItem> missionItems = new ArrayList<MissionItem>(missionItemProxies.size());
        for (MissionItemProxy mip : missionItemProxies)
            missionItems.add(mip.getMissionItem());

        return missionItems.toArray(new MissionItem[missionItems.size()]);
    }

    public Drone getDrone() {
        return this.drone;
    }

    /**
     * Update the state for this object based on the state of the Mission
     * object.
     */
    public void load(Mission mission) {
        load(mission, true);
    }

    private void load(Mission mission, boolean isNew) {
        if (mission == null)
            return;

        if(!mission.equals(currentMission)) {
            if (isNew) {
                currentMission = null;
                clearUndoBuffer();
            }

            selection.mSelectedItems.clear();
            missionItemProxies.clear();

            for (MissionItem item : mission.getMissionItems()) {
                missionItemProxies.add(new MissionItemProxy(this, item));
            }

            selection.notifySelectionUpdate();

            notifyMissionUpdate(isNew);
        }
    }

    public void displayMission(boolean isNew){
        /*if (isNew) {
            currentMission = null;
            clearUndoBuffer();
        }*/

        //selection.mSelectedItems.clear();
        //missionItemProxies.clear();

        /*for (MissionItem item : currentMission.getMissionItems()) {
            missionItemProxies.add(new MissionItemProxy(this, item));
        }*/

        selection.notifySelectionUpdate();

        notifyMissionUpdate(isNew);
    }

    private void clearUndoBuffer(){
        while(!undoBuffer.isEmpty())
            undoBuffer.popLast();
    }

    /**
     * Checks if this mission render contains the passed argument.
     *
     * @param item mission item render object
     * @return true if this mission render contains the passed argument
     */
    public boolean contains(MissionItemProxy item) {
        return missionItemProxies.contains(item);
    }

    /**
     * Removes a waypoint mission item from the set of mission items commands.
     *
     * @param item item to remove
     */
    public void removeItem(MissionItemProxy item) {
        missionItemProxies.remove(item);
        selection.mSelectedItems.remove(item);

        selection.notifySelectionUpdate();
        notifyMissionUpdate();
    }

    /**
     * Adds a survey mission item to the set.
     *
     * @param points 2D points making up the survey
     */
    public void addSurveyPolygon(List<LatLong> points, boolean spline) {
        Survey survey;
        if(spline){
            survey = new SplineSurvey();
        }else {
            survey = new Survey();
        }
        survey.setPolygonPoints(points);

        // Load the last survey preferences.
        dpPrefs.loadSurveyPreferences(drone, survey);

        addMissionItem(survey);
    }

    /**
     * Adds a aeroview survey mission to the set.
     *
     * @param points 2d points making up the survey
     * @param id name of the aeroview boundary to be surveyed
     */

    public void addAeroViewSurveyPolygon(List<LatLong> points, String id){
        Survey survey = new Survey();
        survey.setPolygonPoints(points);
        survey.setID(id);
        //TODO: get survey detail from database

        SQLiteDatabaseHandler dbHandler = new SQLiteDatabaseHandler(context);
        BoundaryDetail boundaryDetail = dbHandler.getBoundaryDetail(id);

        survey.setPolygonPointAltitudes(boundaryDetail.getPointAltitudesAsList());

        SurveyDetail surveyDetail = new SurveyDetail();
        {
            surveyDetail.setAngle(boundaryDetail.getAngle());
            surveyDetail.setOverlap(boundaryDetail.getOverlap());
            surveyDetail.setSidelap(boundaryDetail.getSidelap());
            surveyDetail.setAltitude(boundaryDetail.getAltitude());
            surveyDetail.setSpeed(boundaryDetail.getSpeed());
            surveyDetail.setCameraDetail(boundaryDetail.getCameraDetailFromString());
            surveyDetail.setSaveable(false);
            surveyDetail.setSunny(true);
        }

        survey.setSurveyDetail(surveyDetail);
        addMissionItem(survey);
    }

    /**
     * Add a set of waypoints generated around the passed 2D points.
     *
     * @param points list of points used to generate the mission waypoints
     */
    public void addWaypoints(List<LatLong> points) {
        double alt = getLastAltitude();
        List<MissionItem> missionItemsToAdd = new ArrayList<MissionItem>(points.size());
        for (LatLong point : points) {
            Waypoint waypoint = new Waypoint();
            waypoint.setCoordinate(new LatLongAlt(point.getLatitude(), point.getLongitude(),
                    (float) alt));
            missionItemsToAdd.add(waypoint);
        }

        addMissionItems(missionItemsToAdd);
    }

    public double getLastAltitude() {
        if (!missionItemProxies.isEmpty()) {
            MissionItem lastItem = missionItemProxies.get(missionItemProxies.size() - 1).getMissionItem();
            if (lastItem instanceof MissionItem.SpatialItem
                    && !(lastItem instanceof RegionOfInterest)) {
                return ((MissionItem.SpatialItem) lastItem).getCoordinate().getAltitude();
            }
        }

        return dpPrefs.getDefaultAltitude();
    }

    /**
     * Add a set of spline waypoints generated around the passed 2D points.
     *
     * @param points list of points used as location for the spline waypoints
     */
    public void addSplineWaypoints(List<LatLong> points) {
        double alt = getLastAltitude();
        List<MissionItem> missionItemsToAdd = new ArrayList<MissionItem>(points.size());
        for (LatLong point : points) {
            SplineWaypoint splineWaypoint = new SplineWaypoint();
            splineWaypoint.setCoordinate(new LatLongAlt(point.getLatitude(), point.getLongitude(),
                    (float) alt));
            missionItemsToAdd.add(splineWaypoint);
        }

        addMissionItems(missionItemsToAdd);
    }

    public void addMissionItems(List<MissionItem> missionItems) {
        for (MissionItem missionItem : missionItems) {
            missionItemProxies.add(new MissionItemProxy(this, missionItem));
        }

        notifyMissionUpdate();
    }

    public void addSpatialWaypoint(BaseSpatialItem spatialItem, LatLong point) {
        double alt = getLastAltitude();
        spatialItem.setCoordinate(new LatLongAlt(point.getLatitude(), point.getLongitude(), alt));
        addMissionItem(spatialItem);
    }

    /**
     * Add a waypoint generated around the passed 2D point.
     *
     * @param point point used to generate the mission waypoint
     */
    public void addWaypoint(LatLong point) {
        double alt = getLastAltitude();
        Waypoint waypoint = new Waypoint();
        waypoint.setCoordinate(new LatLongAlt(point.getLatitude(), point.getLongitude(), alt));
        addMissionItem(waypoint);
    }

    /**
     * Add a spline waypoint generated around the passed 2D point.
     *
     * @param point point used as location for the spline waypoint.
     */
    public void addSplineWaypoint(LatLong point) {
        double alt = getLastAltitude();
        SplineWaypoint splineWaypoint = new SplineWaypoint();
        splineWaypoint.setCoordinate(new LatLongAlt(point.getLatitude(), point.getLongitude(), alt));
        addMissionItem(splineWaypoint);
    }

    private void addMissionItem(MissionItem missionItem) {
        missionItemProxies.add(new MissionItemProxy(this, missionItem));
        notifyMissionUpdate();
    }

    private void addMissionItem(int index, MissionItem missionItem) {
        missionItemProxies.add(index, new MissionItemProxy(this, missionItem));
        notifyMissionUpdate();
    }

    public void addTakeoff() {
        Takeoff takeoff = new Takeoff();
        takeoff.setTakeoffAltitude(dpPrefs.getDefaultAltitude());
        addMissionItem(takeoff);
    }

    public boolean hasTakeoffAndLandOrRTL() {
        if (missionItemProxies.size() >= 2) {
            if (isFirstItemTakeoff() && isLastItemLandOrRTL()) {
                return true;
            }
        }
        return false;
    }

    public boolean isFirstItemTakeoff() {
        return !missionItemProxies.isEmpty()
            && missionItemProxies.get(0).getMissionItem().getType() == MissionItemType.TAKEOFF;
    }

    public boolean isLastItemLandOrRTL() {
        int itemsCount = missionItemProxies.size();
        if (itemsCount == 0) return false;

        MissionItemType itemType = missionItemProxies.get(itemsCount - 1).getMissionItem()
                .getType();
        return itemType == MissionItemType.RETURN_TO_LAUNCH || itemType == MissionItemType.LAND;
    }

    public void addTakeOffAndRTL() {
        if (!isFirstItemTakeoff()) {
            double defaultAlt = dpPrefs.getDefaultAltitude();
            if (!missionItemProxies.isEmpty()) {
                MissionItem firstItem = missionItemProxies.get(0).getMissionItem();
                if (firstItem instanceof MissionItem.SpatialItem)
                    defaultAlt = ((MissionItem.SpatialItem) firstItem).getCoordinate().getAltitude();
                else if (firstItem instanceof Survey) {
                    SurveyDetail surveyDetail = ((Survey) firstItem).getSurveyDetail();
                    if (surveyDetail != null)
                        defaultAlt = surveyDetail.getAltitude();
                }
            }

            Takeoff takeOff = new Takeoff();
            takeOff.setTakeoffAltitude(defaultAlt);
            addMissionItem(0, takeOff);
        }

        if (!isLastItemLandOrRTL()) {
            ReturnToLaunch rtl = new ReturnToLaunch();
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
        return missionItemProxies.indexOf(item) + 1;
    }

    /**
     * @return The order of the first waypoint.
     */
    public int getFirstWaypoint(){
        if(missionItemProxies.isEmpty())
            return 0;

        return getOrder(missionItemProxies.get(0));
    }

    /**
     * @return The order for the last waypoint.
     */
    public int getLastWaypoint(){
        int lastIndex = missionItemProxies.size() -1;
        if(lastIndex < 0)
            return 0;

        return getOrder(missionItemProxies.get(lastIndex));
    }

    /**
     * Updates a mission item render
     *
     * @param oldItem mission item render to update
     * @param newItem new mission item render
     */
    public void replace(MissionItemProxy oldItem, MissionItemProxy newItem) {
        int index = missionItemProxies.indexOf(oldItem);
        if (index == -1)
            return;

        missionItemProxies.remove(index);
        missionItemProxies.add(index, newItem);

        if (selection.selectionContains(oldItem)) {
            selection.removeItemFromSelection(oldItem);
            selection.addToSelection(newItem);
        }

        notifyMissionUpdate();
    }

    public void replaceAll(List<Pair<MissionItemProxy, List<MissionItemProxy>>> oldNewList) {
        if (oldNewList == null) {
            return;
        }

        int pairSize = oldNewList.size();
        if (pairSize == 0) {
            return;
        }

        List<MissionItemProxy> selectionsToRemove = new ArrayList<>(pairSize);
        List<MissionItemProxy> itemsToSelect = new ArrayList<>(pairSize);

        for (int i = 0; i < pairSize; i++) {
            MissionItemProxy oldItem = oldNewList.get(i).first;
            int index = missionItemProxies.indexOf(oldItem);
            if (index == -1) {
                continue;
            }

            missionItemProxies.remove(index);

            List<MissionItemProxy> newItems = oldNewList.get(i).second;
            missionItemProxies.addAll(index, newItems);

            if (selection.selectionContains(oldItem)) {
                selectionsToRemove.add(oldItem);
                itemsToSelect.addAll(newItems);
            }
        }

        //Update the selection list.
        selection.removeItemsFromSelection(selectionsToRemove);
        selection.addToSelection(itemsToSelect);

        notifyMissionUpdate();
    }
    public void flipWaypoints(){
        if(!missionItemProxies.isEmpty()){
            int numPoints= missionItemProxies.size();
            for(int i = 0; i < numPoints; i++){
                swap(i,numPoints-i);
            }
        }
        notifyMissionUpdate();
    }
    public void swap(int fromIndex, int toIndex) {
        MissionItemProxy from = missionItemProxies.get(fromIndex);
        MissionItemProxy to = missionItemProxies.get(toIndex);

        missionItemProxies.set(toIndex, from);
        missionItemProxies.set(fromIndex, to);
        //notifyMissionUpdate();
    }

    public void clear() {
        selection.clearSelection();
        missionItemProxies.clear();
        Intent intent = new Intent(MISSION_CLEARED);
        lbm.sendBroadcast(intent);
        notifyMissionUpdate();
    }

    public double getAltitudeDiffFromPreviousItem(MissionItemProxy waypointRender) {
        int itemsCount = missionItemProxies.size();
        if (itemsCount < 2)
            return 0;

        MissionItem waypoint = waypointRender.getMissionItem();
        if (!(waypoint instanceof MissionItem.SpatialItem))
            return 0;

        int index = missionItemProxies.indexOf(waypointRender);
        if (index == -1 || index == 0)
            return 0;

        MissionItem previous = missionItemProxies.get(index - 1).getMissionItem();
        if (previous instanceof MissionItem.SpatialItem) {
            return ((MissionItem.SpatialItem) waypoint).getCoordinate().getAltitude()
                    - ((MissionItem.SpatialItem) previous).getCoordinate().getAltitude();
        }

        return 0;
    }

    public double getDistanceFromLastWaypoint(MissionItemProxy waypointRender) {
        if (missionItemProxies.size() < 2)
            return 0;

        MissionItem waypoint = waypointRender.getMissionItem();
        if (!(waypoint instanceof MissionItem.SpatialItem))
            return 0;

        int index = missionItemProxies.indexOf(waypointRender);
        if (index == -1 || index == 0)
            return 0;

        MissionItem previous = missionItemProxies.get(index - 1).getMissionItem();
        if (previous instanceof MissionItem.SpatialItem) {
            return MathUtils.getDistance3D(((MissionItem.SpatialItem) waypoint).getCoordinate(),
                    ((MissionItem.SpatialItem) previous).getCoordinate());
        }

        return 0;
    }

    @Override
    public List<LatLong> getPathPoints() {
        if (missionItemProxies.isEmpty()) {
            return Collections.emptyList();
        }

        // Partition the mission items into spline/non-spline buckets.
        List<Pair<Boolean, List<MissionItemProxy>>> bucketsList = new ArrayList<>();

        boolean isSpline = false;
        List<MissionItemProxy> currentBucket = new ArrayList<>();
        for (MissionItemProxy missionItemProxy : missionItemProxies) {

            MissionItem missionItem = missionItemProxy.getMissionItem();
            if (missionItem instanceof MissionItem.Command) {
                //Skip commands
                continue;
            }

            if (missionItem instanceof SplineWaypoint || missionItem instanceof SplineSurvey) {
                if (!isSpline) {
                    if (!currentBucket.isEmpty()) {
                        // Get the last item from the current bucket. It will become the first
                        // anchor point for the spline path.
                        MissionItemProxy lastItem = currentBucket.get(currentBucket.size() - 1);

                        // Store the previous item bucket.
                        bucketsList.add(new Pair<>(Boolean.FALSE, currentBucket));

                        // Create a new bucket for this category and update 'isSpline'
                        currentBucket = new ArrayList<>();
                        currentBucket.add(lastItem);
                    }

                    isSpline = true;
                }

                // Add the current element into the bucket
                currentBucket.add(missionItemProxy);
            } else {
                if (isSpline) {

                    // Add the current item to the spline bucket. It will act as the end anchor
                    // point for the spline path.
                    if (!currentBucket.isEmpty()) {
                        currentBucket.add(missionItemProxy);

                        // Store the previous item bucket.
                        bucketsList.add(new Pair<>(Boolean.TRUE, currentBucket));

                        currentBucket = new ArrayList<>();
                    }

                    isSpline = false;
                }

                // Add the current element into the bucket
                currentBucket.add(missionItemProxy);
            }
        }

        bucketsList.add(new Pair<>(isSpline, currentBucket));

        List<LatLong> pathPoints = new ArrayList<>();
        LatLong lastPoint = null;

        for (Pair<Boolean, List<MissionItemProxy>> bucketEntry : bucketsList) {

            List<MissionItemProxy> bucket = bucketEntry.second;
            if (bucketEntry.first) {
                List<LatLong> splinePoints = new ArrayList<>();
                int bucketSize = bucket.size();
                for(int i = 0; i < bucketSize; i++){
                    MissionItemProxy missionItemProxy = bucket.get(i);
                    MissionItemType missionItemType = missionItemProxy.getMissionItem().getType();
                    List<LatLong> missionItemPath = missionItemProxy.getPath(lastPoint);

                    switch(missionItemType){
                        case SURVEY:
                            if(!missionItemPath.isEmpty()) {
                                if (i == 0)
                                    splinePoints.add(missionItemPath.get(0));
                                else {
                                    splinePoints.add(missionItemPath.get(missionItemPath.size() - 1));
                                }
                            }
                            break;

                        default:
                            splinePoints.addAll(missionItemPath);
                            break;
                    }

                    if (!splinePoints.isEmpty()) {
                        lastPoint = splinePoints.get(splinePoints.size() - 1);
                    }
                }

                pathPoints.addAll(MathUtils.SplinePath.process(splinePoints));
            }
            else {
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
        missionItemProxies.removeAll(missionSelection.mSelectedItems);
        missionSelection.clearSelection();
        notifyMissionUpdate();
    }

    public void move(MissionItemProxy item, LatLong position) {
        MissionItem missionItem = item.getMissionItem();
        if (missionItem instanceof SpatialItem) {
            SpatialItem spatialItem = (SpatialItem) missionItem;
            spatialItem.setCoordinate(new LatLongAlt(position.getLatitude(),
                    position.getLongitude(), spatialItem.getCoordinate().getAltitude()));

            if (spatialItem instanceof StructureScanner) {
                this.drone.buildMissionItemsAsync(new StructureScanner[]{(StructureScanner) spatialItem},
                        missionItemsBuiltListener);
            }

            notifyMissionUpdate();
        }
    }

    public List<LatLong> getVisibleCoords() {
        return getVisibleCoords(missionItemProxies);
    }

    public void movePolygonPoint(Survey survey, int index, LatLong position) {
        survey.getPolygonPoints().get(index).set(position);
        this.drone.buildMissionItemsAsync(new Survey[]{survey}, missionItemsBuiltListener);
        notifyMissionUpdate();
    }

    public static List<LatLong> getVisibleCoords(List<MissionItemProxy> mipList) {
        List<LatLong> coords = new ArrayList<LatLong>();

        if (mipList == null || mipList.isEmpty()) {
            return coords;
        }

        for (MissionItemProxy itemProxy : mipList) {
            MissionItem item = itemProxy.getMissionItem();
            if (!(item instanceof SpatialItem))
                continue;

            LatLong coordinate = ((SpatialItem) item).getCoordinate();
            if (coordinate.getLatitude() == 0 || coordinate.getLongitude() == 0)
                continue;

            coords.add(coordinate);
        }

        return coords;
    }

    private Mission generateMission() {
        return generateMission(false);
    }

    private Mission generateMission(boolean isDeepCopy) {
        Mission mission = new Mission();

        if (!missionItemProxies.isEmpty()) {
            for (MissionItemProxy itemProxy : missionItemProxies) {
                MissionItem sourceItem = itemProxy.getMissionItem();
                MissionItem destItem = isDeepCopy ? sourceItem.clone() : sourceItem;
                mission.addMissionItem(destItem);
            }
        }

        return mission;
    }

    public void sendMissionToAPM(Drone drone) {
        MissionApi.getApi(drone).setMission(generateMission(), true);

        int missionItemsCount = missionItemProxies.size();

        String missionItemsList = "[";
        if (missionItemsCount > 0) {
            boolean isFirst = true;
            for (MissionItemProxy itemProxy : missionItemProxies) {
                if (isFirst)
                    isFirst = false;
                else
                    missionItemsList += ", ";

                missionItemsList += itemProxy.getMissionItem().getType().getLabel();
            }
        }

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

    public Pair<Double, Double> getMissionFlightTime() {
        if (missionItemProxies.isEmpty()) {
            return Pair.create(0.0, 0.0);
        }

        double currentSpeed = dpApp.getVehicleSpeed();
        double accumulatedDistance = 0;
        double accumulatedDelay = 0;
        LatLong lastPoint = null;
        List<Pair<Double, Double>> speedPerDistance = new LinkedList<>();

        for (MissionItemProxy proxy : missionItemProxies) {
            final MissionItem missionItem = proxy.getMissionItem();
            if (!(missionItem instanceof MissionItem.Command)) {
                // If the mission item has a spatial component, retrieve that component.
                List<LatLong> path = proxy.getPath(lastPoint);
                if (!path.isEmpty()) {
                    for (LatLong point : path) {
                        if (lastPoint != null) {
                            // Accumulate the distance between the last point and the current point.
                            accumulatedDistance += MathUtils.getDistance2D(lastPoint, point);
                        }
                        lastPoint = point;
                    }
                }
                if (missionItem instanceof  Waypoint){
                    accumulatedDelay += ((Waypoint) missionItem).getDelay();
                }else if (missionItem instanceof  SplineWaypoint){
                    accumulatedDelay += ((SplineWaypoint) missionItem).getDelay();
                }
            } else if (missionItem instanceof ChangeSpeed) {
                //  We're updating the vehicle speed, so let's store the distance accumulated at
                // the current speed.
                if (accumulatedDistance > 0) {
                    speedPerDistance.add(Pair.create(currentSpeed, accumulatedDistance));
                    accumulatedDistance = 0;
                }
                currentSpeed = ((ChangeSpeed) missionItem).getSpeed();
            }
        }

        if (accumulatedDistance > 0) {
            speedPerDistance.add(Pair.create(currentSpeed, accumulatedDistance));
        }

        if (speedPerDistance.isEmpty()) {
            return Pair.create(0.0, 0.0);
        } else {
            double totalFlightDistance = 0;
            double totalFlightTime = 0;
            for (Pair<Double, Double> entry : speedPerDistance) {
                double speed = entry.first;
                double distance = entry.second;

                totalFlightDistance += distance;
                if (speed <= 0) {
                    // No way the vehicle is completing its mission if the speed is less or equal to
                    // 0.
                    totalFlightTime += Double.POSITIVE_INFINITY;
                } else {
                    totalFlightTime += distance / speed;
                }
            }
            totalFlightTime += accumulatedDelay;
            return Pair.create(totalFlightDistance, totalFlightTime);
        }
    }

    public double getAccumulatedMissionDelay(){
        double accumulatedDelay = 0; //time in decimal seconds
        for (MissionItemProxy itemProxy : missionItemProxies) {
            MissionItem missionItem = itemProxy.getMissionItem();
            switch (missionItem.getType()) {
                case WAYPOINT:
                    accumulatedDelay += ((Waypoint) missionItem).getDelay();
                    break;
                case SPLINE_WAYPOINT:
                    accumulatedDelay += ((SplineWaypoint) missionItem).getDelay();
                    break;
                default:
                    break;
            }
        }
        return accumulatedDelay;
    }

    public void makeAndUploadDronie(Drone drone) {
        MissionApi.getApi(drone).generateDronie();
    }

    public List<List<LatLong>> getPolygonsPath() {
        ArrayList<List<LatLong>> polygonPaths = new ArrayList<List<LatLong>>();
        for (MissionItemProxy itemProxy : missionItemProxies) {
            MissionItem item = itemProxy.getMissionItem();
            if (item instanceof Survey) {
                polygonPaths.add(((Survey) item).getPolygonPoints());
            }
        }
        return polygonPaths;
    }

    public void writeMissionToFile(Uri saveUri){
        MissionApi.getApi(drone).saveMission(generateMission(), saveUri, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, R.string.file_saved_success, Toast.LENGTH_SHORT)
                    .show();

                final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                    .setCategory(GAUtils.Category.MISSION_PLANNING)
                    .setAction("Mission saved to file")
                    .setLabel("Mission items count");
                GAUtils.sendEvent(eventBuilder);
            }

            @Override
            public void onError(int executionError) {
                Toast.makeText(context, R.string.file_saved_error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTimeout() {
                Toast.makeText(context, R.string.file_saved_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void readMissionFromFile(final Uri fileUri){
        MissionApi.getApi(drone).loadAndSetMission(fileUri, new MissionApi.LoadingCallback<Mission>() {
            @Override
            public void onLoadingStart() {
                Toast.makeText(context, "Loading mission...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoadingComplete(Mission loaded) {
                load(loaded);
                Toast.makeText(context, "Mission loaded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoadingFailed() {
                Toast.makeText(context, "Mission loading failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createSurvey(){
        List<String> selectedPolygons = DroidPlannerApp.getInstance().getSelectedPolygons();
        if (!DroidPlannerApp.getInstance().getSelectedPolygons().isEmpty()) {

            for (String id : selectedPolygons) {
                PolygonData polygonData = DroidPlannerApp.getInstance().polygonMap.get(id);

                polygonData.setSelected(false);
                DroidPlannerApp.getInstance().polygonMap.put(id, polygonData);

                List<LatLng> mapsPolygon = polygonData.getPoints();

                List<LatLong> dronekitPoly = new ArrayList<>();
                for (LatLng point : mapsPolygon) {
                    dronekitPoly.add(new LatLong(point.latitude, point.longitude));
                }

                this.addAeroViewSurveyPolygon(dronekitPoly, id);
            }
            DroidPlannerApp.getInstance().getSelectedPolygons().clear();
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(AeroviewPolygons.ACTION_POLYGON_UPDATE));

        }
    }

    public void createMergedConvexSurvey(){
        List<String> selectedPolygons = DroidPlannerApp.getInstance().getSelectedPolygons();
        if (!DroidPlannerApp.getInstance().getSelectedPolygons().isEmpty()) {
            List<LatLng> allPoints = new ArrayList<LatLng>();
            for (String id: selectedPolygons) {
                PolygonData polygonData = DroidPlannerApp.getInstance().polygonMap.get(id);
                List<LatLng> mapsPolygon = polygonData.getPoints();
                for (LatLng point : mapsPolygon) {
                    allPoints.add(point);
                }
                polygonData.setSelected(false);
                DroidPlannerApp.getInstance().polygonMap.put(id,polygonData);
            }

            List<LatLng> mergedPoints = new QuickHullLatLng().quickHull(allPoints);
            List<LatLong> dronekitPoly = new ArrayList<>();
            for (LatLng point : mergedPoints) {
                dronekitPoly.add(new LatLong(point.latitude, point.longitude));
            }
            this.addSurveyPolygon(dronekitPoly, false);
            DroidPlannerApp.getInstance().getSelectedPolygons().clear();
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(AeroviewPolygons.ACTION_POLYGON_UPDATE));

        }
    }


}
