package org.droidplanner.android.activities;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.mission.item.MissionItemRender;
import org.droidplanner.android.mission.MissionRender;
import org.droidplanner.android.mission.MissionSelection;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.android.fragments.EditorListFragment;
import org.droidplanner.android.fragments.EditorMapFragment;
import org.droidplanner.android.fragments.EditorToolsFragment;
import org.droidplanner.android.fragments.EditorToolsFragment.EditorTools;
import org.droidplanner.android.fragments.EditorToolsFragment.OnEditorToolSelected;
import org.droidplanner.android.fragments.helpers.GestureMapFragment;
import org.droidplanner.android.fragments.helpers.GestureMapFragment.OnPathFinishedListener;
import org.droidplanner.android.fragments.helpers.MapProjection;
import org.droidplanner.android.mission.item.fragments.MissionDetailFragment;
import org.droidplanner.android.mission.item.fragments.MissionDetailFragment.OnWayPointTypeChangeListener;
import org.droidplanner.android.graphic.DroneHelper;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.android.dialogs.YesNoDialog;

import android.app.ActionBar;
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
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * This implements the map editor activity. The map editor activity allows the user to create
 * and/or modify autonomous missions for the drone.
 */
public class EditorActivity extends SuperUI implements OnPathFinishedListener,
		OnEditorToolSelected, OnWayPointTypeChangeListener,
		OnEditorInteraction, Callback, MissionSelection.OnSelectionUpdateListener {

    /**
     * Used to provide access and interact with the {@link org.droidplanner.core.mission.Mission}
     * object on the Android layer.
     */
    private MissionRender missionRender;

	private EditorMapFragment planningMapFragment;
	private GestureMapFragment gestureMapFragment;
	private EditorToolsFragment editorToolsFragment;
	private MissionDetailFragment itemDetailFragment;
	private FragmentManager fragmentManager;
	private EditorListFragment missionListFragment;
	private TextView infoView;

    /**
     * This view hosts the mission item detail fragment.
     * On phone, or device with limited screen estate, it's removed from the layout,
     * and the item detail ends up displayed as a dialog.
     */
    private View mContainerItemDetail;

	private ActionMode contextualActionBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);

		ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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

		missionRender = ((DroidPlannerApp)getApplication()).missionRender;
		gestureMapFragment.setOnPathFinishedListener(this);
	}

    @Override
    public void onStart(){
        super.onStart();
        missionRender.selection.addSelectionUpdateListener(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        missionRender.selection.removeSelectionUpdateListener(this);
    }

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		updateMapPadding();
	}

	private void updateMapPadding() {
		int topPadding = infoView.getBottom();
		int rightPadding = 0,bottomPadding = 0;

		if (missionRender.getItems().size()>0) {
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
				if (!missionRender.contains(itemDetailFragment.getItem())) {
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
        missionRender.selection.clearSelection();

		switch (getTool()) {
		case MARKER:
			missionRender.addWaypoint(DroneHelper.LatLngToCoord(point));
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
		missionRender.selection.clearSelection();

		switch (tools) {
		case DRAW:
		case POLY:
			Toast.makeText(this,R.string.draw_the_survey_region, Toast.LENGTH_SHORT).show();
			gestureMapFragment.enableGestureDetection();
			break;
		case MARKER:
		case TRASH:
		case NONE:
			gestureMapFragment.disableGestureDetection();
			break;
		}
	}

	@Override
	public void editorToolLongClicked(EditorTools tools) {
		switch (tools) {
		case TRASH: {
			// Clear the mission?
			doClearMissionConfirmation();
			break;
		}

		default: {
			break;
		}
		}
	}

	private void showItemDetail(MissionItemRender item) {
		if (itemDetailFragment == null) {
			addItemDetail(item);
		} else {
			switchItemDetail(item);
		}
	}

    private void addItemDetail(MissionItemRender item) {
        itemDetailFragment = item.getDetailFragment();
        if(itemDetailFragment == null)
            return;

        if (mContainerItemDetail == null) {
            itemDetailFragment.show(fragmentManager, "Item detail dialog");
        } else {
            fragmentManager.beginTransaction().replace(R.id.containerItemDetail,
                    itemDetailFragment).commit();
        }
    }

	public void switchItemDetail(MissionItemRender item) {
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
	public void onPathFinished(List<Coord2D> path) {
		List<Coord2D> points = MapProjection.projectPathIntoMap(path, planningMapFragment.mMap);
		switch (getTool()) {
		case DRAW:
			missionRender.addWaypoints(points);
			break;

		case POLY:
			if (path.size()>2) {
				missionRender.addSurveyPolygon(points);
			}else{
				editorToolsFragment.setTool(EditorTools.POLY);
				return;
			}
			break;

            default:
			break;
		}
		editorToolsFragment.setTool(EditorTools.NONE);
	}

	@Override
	public void onWaypointTypeChanged(MissionItemRender newItem, MissionItemRender oldItem) {
		missionRender.replace(oldItem, newItem);
	}

	private static final int MENU_DELETE = 1;
	private static final int MENU_REVERSE = 2;

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch(item.getItemId()){
		case MENU_DELETE:
			missionRender.removeSelection(missionRender.selection);
			mode.finish();
			return true;

		case MENU_REVERSE:
			missionRender.reverse();
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
		missionRender.selection.clearSelection();
		contextualActionBar = null;
		editorToolsFragment.getView().setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
		return false;
	}

	@Override
	public boolean onItemLongClick(MissionItemRender item) {
		if (contextualActionBar != null) {
			if (missionRender.selection.selectionContains(item)) {
				missionRender.selection.clearSelection();
			} else {
				missionRender.selection.setSelectionTo(missionRender.getItems());
			}
		} else {
			editorToolsFragment.setTool(EditorTools.NONE);
			missionListFragment.updateChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			contextualActionBar = startActionMode(this);
			missionRender.selection.setSelectionTo(item);
		}
		return true;
	}

	@Override
	public void onItemClick(MissionItemRender item) {
		switch (editorToolsFragment.getTool()) {
		default:
			if (contextualActionBar != null) {
				if (missionRender.selection.selectionContains(item)) {
					missionRender.selection.removeItemFromSelection(item);
				} else {
					missionRender.selection.addToSelection(item);
				}
			} else {
				if (missionRender.selection.selectionContains(item)) {
					missionRender.selection.clearSelection();
				} else {
					editorToolsFragment.setTool(EditorTools.NONE);
					missionRender.selection.setSelectionTo(item);
				}
			}
			break;

		case TRASH:
			missionRender.removeItem(item);
			missionRender.selection.clearSelection();
			if (missionRender.getItems().size() <= 0) {
				editorToolsFragment.setTool(EditorTools.NONE);
			}
			break;
		}
	}

	@Override
	public void onListVisibilityChanged() {
		updateMapPadding();
	}

    @Override
    public void onSelectionUpdate(List<MissionItemRender> selected) {
        final int selectedCount = selected.size();
        
        missionListFragment.setArrowsVisibility(selectedCount > 0);
        
        if(selectedCount != 1){
            removeItemDetail();
        }
        else{
            if(contextualActionBar != null)
                removeItemDetail();
            else{
                showItemDetail(selected.get(0));
            }
        }

        planningMapFragment.update();
    }

	private void doClearMissionConfirmation() {
		YesNoDialog ynd = YesNoDialog.newInstance(
				getString(R.string.dlg_clear_mission_title),
				getString(R.string.dlg_clear_mission_confirm),
				new YesNoDialog.Listener() {
					@Override
					public void onYes() {
						missionRender.clear();
						missionRender.addTakeoff();
					}

					@Override
					public void onNo() {
					}
				});

		ynd.show(getSupportFragmentManager(), "clearMission");
	}
}
