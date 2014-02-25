package org.droidplanner.activities;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.activities.helpers.OnEditorInteraction;
import org.droidplanner.activities.helpers.SuperUI;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.fragments.EditorListFragment;
import org.droidplanner.fragments.EditorMapFragment;
import org.droidplanner.fragments.EditorToolsFragment;
import org.droidplanner.fragments.EditorToolsFragment.EditorTools;
import org.droidplanner.fragments.EditorToolsFragment.OnEditorToolSelected;
import org.droidplanner.fragments.helpers.GestureMapFragment;
import org.droidplanner.fragments.helpers.GestureMapFragment.OnPathFinishedListener;
import org.droidplanner.fragments.helpers.MapProjection;
import org.droidplanner.fragments.mission.MissionDetailFragment;
import org.droidplanner.fragments.mission.MissionDetailFragment.OnWayPointTypeChangeListener;

import android.app.ActionBar;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class EditorActivity extends SuperUI implements OnPathFinishedListener,
		OnEditorToolSelected, OnWayPointTypeChangeListener,
		OnEditorInteraction, Callback {

	private EditorMapFragment planningMapFragment;
	private GestureMapFragment gestureMapFragment;
	private Mission mission;
	private EditorToolsFragment editorToolsFragment;
	private MissionDetailFragment itemDetailFragment;
	private FragmentManager fragmentManager;
	private EditorListFragment missionListFragment;
	private TextView infoView;

    private View mContainerItemDetail;

	private ActionMode contextualActionBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		fragmentManager = getSupportFragmentManager();

		planningMapFragment = ((EditorMapFragment) fragmentManager
				.findFragmentById(R.id.mapFragment));
		gestureMapFragment = ((GestureMapFragment) fragmentManager
				.findFragmentById(R.id.gestureMapFragment));
		editorToolsFragment = (EditorToolsFragment) fragmentManager
				.findFragmentById(R.id.editorToolsFragment);
		missionListFragment = (EditorListFragment) fragmentManager
				.findFragmentById(R.id.missionFragment1);
		infoView = (TextView) findViewById(R.id.editorInfoWindow);

        /*
         * On phone, this view will be null causing the item detail to be shown as a dialog.
         */
        mContainerItemDetail = findViewById(R.id.containerItemDetail);

		mission = drone.mission;
		gestureMapFragment.setOnPathFinishedListener(this);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		updateMapPadding();
	}

	private void updateMapPadding() {
		int topPadding = infoView.getBottom();
		int rightPadding = 0,bottomPadding = 0;
		if (mission.getItems().size()>0) {
			rightPadding = editorToolsFragment.getView().getRight();
			bottomPadding = missionListFragment.getView().getHeight();
		}
		planningMapFragment.mMap.setPadding(rightPadding, topPadding, 0, bottomPadding);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		planningMapFragment.saveCameraPosition();
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		super.onDroneEvent(event, drone);
		switch (event) {
		case MISSION_UPDATE:
			// Remove detail window if item is removed
			if (itemDetailFragment != null) {
				if (!drone.mission.hasItem(itemDetailFragment.getItem())) {
					removeItemDetail();
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			planningMapFragment.saveCameraPosition();
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onMapClick(LatLng point) {
        //If an mission item is selected, unselect it.
        mission.clearSelection();
        removeItemDetail();
        notifySelectionChanged();

		switch (getTool()) {
		case MARKER:
			mission.addWaypoint(point);
			break;
		case DRAW:
			break;
		case POLY:
			break;
		case TRASH:
			break;
		case NONE:
			break;
		}
	}

	public EditorTools getTool() {
		return editorToolsFragment.getTool();
	}

	@Override
	public void editorToolChanged(EditorTools tools) {
		removeItemDetail();
		mission.clearSelection();
		notifySelectionChanged();

		switch (tools) {
		case DRAW:
		case POLY:
			gestureMapFragment.enableGestureDetection();
			break;
		case MARKER:
		case TRASH:
		case NONE:
			gestureMapFragment.disableGestureDetection();
			break;
		}
	}

	private void showItemDetail(MissionItem item) {
		if (itemDetailFragment == null) {
			addItemDetail(item);
		} else {
			switchItemDetail(item);
		}
	}

	private void addItemDetail(MissionItem item) {
		itemDetailFragment = item.getDetailFragment();

        if (mContainerItemDetail == null) {
            itemDetailFragment.show(fragmentManager, "Item detail dialog");
        } else {
            fragmentManager.beginTransaction().add(R.id.containerItemDetail,
                    itemDetailFragment).commit();
        }
    }

    public MissionDetailFragment getItemDetailFragment(){
        return itemDetailFragment;
    }

	public void switchItemDetail(MissionItem item) {
        removeItemDetail();
		addItemDetail(item);
	}

	private void removeItemDetail() {
		if (itemDetailFragment != null) {
            if (mContainerItemDetail == null) {
                itemDetailFragment.dismiss();
            } else {
                fragmentManager.beginTransaction().remove(itemDetailFragment).commit();
            }
            itemDetailFragment = null;
		}
	}

	@Override
	public void onPathFinished(List<Point> path) {
		List<LatLng> points = MapProjection.projectPathIntoMap(path,
				planningMapFragment.mMap);
		switch (getTool()) {
		case DRAW:
			drone.mission.addWaypoints(points);
			break;
		case POLY:
			drone.mission.addSurveyPolygon(points);
			break;
		default:
			break;
		}
		editorToolsFragment.setTool(EditorTools.NONE);
	}

	@Override
	public void onWaypointTypeChanged(MissionItem newItem, MissionItem oldItem) {
		mission.replace(oldItem, newItem);
		showItemDetail(newItem);
	}

	private static final int MENU_DELETE = 1;
	private static final int MENU_REVERSE = 2;

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch(item.getItemId()){
		case MENU_DELETE:
			mission.removeWaypoints(mission.getSelected());
			notifySelectionChanged();
			mode.finish();
			return true;
		case MENU_REVERSE:
			mission.reverse();
			notifySelectionChanged();
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean onCreateActionMode(ActionMode arg0, Menu menu) {
		menu.add(0, MENU_DELETE, 0, "Delete");
		menu.add(0, MENU_REVERSE, 0, "Reverse");
		editorToolsFragment.getView().setVisibility(View.INVISIBLE);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode arg0) {
		missionListFragment.updateChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mission.clearSelection();
		notifySelectionChanged();
		contextualActionBar = null;
		editorToolsFragment.getView().setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
		return false;
	}

	@Override
	public boolean onItemLongClick(MissionItem item) {
		if (contextualActionBar != null) {
			if (mission.selectionContains(item)) {
				mission.clearSelection();
			} else {
				mission.clearSelection();
				mission.addToSelection(mission.getItems());
			}
			notifySelectionChanged();
		} else {
			removeItemDetail();
			missionListFragment.updateChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			contextualActionBar = startActionMode(this);
			editorToolsFragment.setTool(EditorTools.NONE);
			mission.clearSelection();
			mission.addToSelection(item);
			notifySelectionChanged();
		}
		return true;
	}

	@Override
	public void onItemClick(MissionItem item) {
		switch (editorToolsFragment.getTool()) {
		default:
			if (contextualActionBar != null) {
				if (mission.selectionContains(item)) {
					mission.removeItemFromSelection(item);
				} else {
					mission.addToSelection(item);
				}
			} else {
				if (mission.selectionContains(item)) {
					mission.clearSelection();
					removeItemDetail();
				} else {
					mission.setSelectionTo(item);
					showItemDetail(item);
				}
			}
			break;
		case TRASH:
			mission.removeWaypoint(item);
			mission.clearSelection();
			if (mission.getItems().size() <= 0) {
				editorToolsFragment.setTool(EditorTools.NONE);
			}
			break;
		}
		notifySelectionChanged();
	}

	private void notifySelectionChanged() {
        List<MissionItem> selectedItems = mission.getSelected();
        missionListFragment.updateMissionItemSelection(selectedItems);

		if (selectedItems.size() == 0) {
			missionListFragment.setArrowsVisibility(false);
		} else {
			missionListFragment.setArrowsVisibility(true);
		}
		planningMapFragment.update();
	}

	@Override
	public void onListVisibilityChanged() {
		updateMapPadding();
	}

}
