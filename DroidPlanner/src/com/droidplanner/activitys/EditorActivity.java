package com.droidplanner.activitys;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperUI;
import com.droidplanner.drone.variables.mission.Mission;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.drone.variables.mission.waypoints.SpatialCoordItem;
import com.droidplanner.fragments.EditorToolsFragment;
import com.droidplanner.fragments.EditorToolsFragment.EditorTools;
import com.droidplanner.fragments.EditorToolsFragment.OnEditorToolSelected;
import com.droidplanner.fragments.PlanningMapFragment;
import com.droidplanner.fragments.helpers.GestureMapFragment;
import com.droidplanner.fragments.helpers.GestureMapFragment.OnPathFinishedListner;
import com.droidplanner.fragments.helpers.MapProjection;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.fragments.mission.MissionDetailFragment;
import com.droidplanner.fragments.mission.MissionDetailFragment.OnWayPointTypeChangeListener;
import com.droidplanner.polygon.PolygonPoint;
import com.droidplanner.widgets.adapterViews.NavigationHubAdapter;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class EditorActivity extends SuperUI implements
		OnMapInteractionListener, OnPathFinishedListner, OnEditorToolSelected,
		OnWayPointTypeChangeListener {

    /**
     * Activity title.
     * Used to update the action bar when the navigation drawer opens/closes.
     * @since 1.2.0
     */
    public static final int LABEL_RESOURCE = R.string.screen_editor;

    /**
     * Activity logo.
     * Used by the navigation drawer.
     * @since 1.2.0
     */
    public static final int LOGO_RESOURCE = R.drawable.ic_edit;

    /**
     * This textview displays the status of the mission(s) that's being edited.
     * @since 1.2.0
     */
    private TextView mMissionsStatusView;

	private PlanningMapFragment planningMapFragment;
	private GestureMapFragment gestureMapFragment;
	private Mission mission;
	private EditorToolsFragment editorToolsFragment;
	private MissionDetailFragment itemDetailFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);

        mMissionsStatusView = (TextView) findViewById(R.id.editor_missions_status);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		final FragmentManager fragmentManager = getFragmentManager();

		planningMapFragment = ((PlanningMapFragment) fragmentManager
				.findFragmentById(R.id.mapFragment));
		gestureMapFragment = ((GestureMapFragment) fragmentManager
				.findFragmentById(R.id.gestureMapFragment));
		editorToolsFragment = (EditorToolsFragment) fragmentManager
				.findFragmentById(R.id.editorToolsFragment);

		mission = drone.mission;
		gestureMapFragment.setOnPathFinishedListner(this);
		mission.onMissionUpdate();

        setupNavDrawer();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mission.removeOnMissionUpdateListner(planningMapFragment);
	}

    @Override
    public void onStart(){
        super.onStart();

        //Update the map top padding to account for the mission(s) status view
        if(mMissionsStatusView != null && mMapFragment != null){
            int topPadding = mMissionsStatusView.getLayoutParams().height;
            mMapFragment.setTopPadding(topPadding);
        }
    }

    @Override
    public void onStop(){
        super.onStop();

        //Reset the map top padding.
        if(mMissionsStatusView != null && mMapFragment != null){
            mMapFragment.setTopPadding(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_editor_load:
                return true;

            case R.id.menu_editor_receive:
                return true;

            case R.id.menu_editor_save:
                return true;

            case R.id.menu_editor_send:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public boolean onMarkerClick(MissionItem wp) {
		showItemDetail(wp);
		return true;
	}

	@Override
	public void onAddPoint(LatLng point) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMoveHome(LatLng coord) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMoveWaypoint(SpatialCoordItem waypoint, LatLng latLng) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMovingWaypoint(SpatialCoordItem source, LatLng latLng) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMovePolygonPoint(PolygonPoint source, LatLng newCoord) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapClick(LatLng point) {
		switch (editorToolsFragment.getTool()) {
		case MARKER:
			mission.addWaypoint(point, mission.getDefaultAlt());
			break;
		case DRAW:
			break;
		case POLY:
			break;
		case TRASH:
			break;
		}
	}

	@Override
	public void editorToolChanged(EditorTools tools) {
		removeItemDetail(); // TODO remove this, used for debbuging
		switch (tools) {
		case DRAW:
		case POLY:
			gestureMapFragment.enableGestureDetection();
			break;
		case MARKER:
		case TRASH:
			gestureMapFragment.disableGestureDetection();
			break;
		}
	}

    @Override
    protected int getLabelResource(){
        return LABEL_RESOURCE;
    }

    @Override
    protected NavigationHubAdapter.HubItem getNavigationHubItem(){
        return NavigationHubAdapter.HubItem.EDITOR;
    }

    private void showItemDetail(MissionItem item) {
        removeItemDetail();

        itemDetailFragment = item.getDetailFragment();
        if (itemDetailFragment != null)
            itemDetailFragment.show(getFragmentManager(), "Item Dialog");
    }

    private void removeItemDetail() {
        if (itemDetailFragment != null)
            itemDetailFragment.dismiss();
    }

	@Override
	public void onPathFinished(List<Point> path) {
		List<LatLng> points = MapProjection.projectPathIntoMap(path,
				planningMapFragment.mMap);
		switch (editorToolsFragment.getTool()) {
		case DRAW:
			drone.mission.addWaypointsWithDefaultAltitude(points);
			break;
		case POLY:
			drone.mission.addSurveyPolygon(points);
			break;
		default:			
			break;
		}
		editorToolsFragment.setTool(EditorTools.MARKER);
	}

	@Override
	public void onWaypointTypeChanged(MissionItem newItem, MissionItem oldItem) {
		mission.replace(oldItem, newItem);
		showItemDetail(newItem);
	}

}
