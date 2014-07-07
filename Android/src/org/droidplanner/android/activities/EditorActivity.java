package org.droidplanner.android.activities;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.proxy.mission.MissionSelection;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.android.fragments.EditorListFragment;
import org.droidplanner.android.fragments.EditorMapFragment;
import org.droidplanner.android.fragments.EditorToolsFragment;
import org.droidplanner.android.fragments.EditorToolsFragment.EditorTools;
import org.droidplanner.android.fragments.EditorToolsFragment.OnEditorToolSelected;
import org.droidplanner.android.fragments.helpers.GestureMapFragment;
import org.droidplanner.android.fragments.helpers.GestureMapFragment.OnPathFinishedListener;
import org.droidplanner.android.proxy.mission.item.fragments.MissionDetailFragment;

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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This implements the map editor activity. The map editor activity allows the user to create
 * and/or modify autonomous missions for the drone.
 */
public class EditorActivity extends SuperUI implements OnPathFinishedListener,
		OnEditorToolSelected, MissionDetailFragment.OnMissionDetailListener,
		OnEditorInteraction, Callback, MissionSelection.OnSelectionUpdateListener {

    /**
     * Used to retrieve the item detail window when the activity is destroyed, and recreated.
     */
    private static final String ITEM_DETAIL_TAG = "Item Detail Window";

    /**
     * Used to provide access and interact with the {@link org.droidplanner.core.mission.Mission}
     * object on the Android layer.
     */
    private MissionProxy missionProxy;

    /*
    View widgets.
     */
	private EditorMapFragment planningMapFragment;
	private GestureMapFragment gestureMapFragment;
	private EditorToolsFragment editorToolsFragment;
	private MissionDetailFragment itemDetailFragment;
	private FragmentManager fragmentManager;
	private EditorListFragment missionListFragment;
	private TextView infoView;

    private View mSplineToggleContainer;
    private boolean mIsSplineEnabled;

    private ImageButton mGoToMyLocation;
    private ImageButton mGoToDroneLocation;

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

        mSplineToggleContainer = findViewById(R.id.editorSplineToggleContainer);
        mSplineToggleContainer.setVisibility(View.VISIBLE);

        mGoToMyLocation = (ImageButton)findViewById(R.id.my_location_button);
        mGoToMyLocation.setOnClickListener(new View.OnClickListener(){
           @Override
            public void onClick(View v){
               planningMapFragment.goToMyLocation();
           }
        });
        mGoToMyLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                planningMapFragment.setAutoPanMode(AutoPanMode.USER);
                return true;
            }
        });

        mGoToDroneLocation = (ImageButton) findViewById(R.id.drone_location_button);
        mGoToDroneLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                planningMapFragment.goToDroneLocation();
            }
        });
        mGoToDroneLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                planningMapFragment.setAutoPanMode(AutoPanMode.DRONE);
                return true;
            }
        });

        final RadioButton normalToggle = (RadioButton) findViewById(R.id.normalWpToggle);
        normalToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsSplineEnabled = !normalToggle.isChecked();
            }
        });

        final RadioButton splineToggle = (RadioButton) findViewById(R.id.splineWpToggle);
        splineToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsSplineEnabled = splineToggle.isChecked();
            }
        });

        //Retrieve the item detail fragment using its tag
        itemDetailFragment = (MissionDetailFragment) fragmentManager.findFragmentByTag
                (ITEM_DETAIL_TAG);

        /*
         * On phone, this view will be null causing the item detail to be shown as a dialog.
         */
        mContainerItemDetail = findViewById(R.id.containerItemDetail);

		missionProxy = ((DroidPlannerApp)getApplication()).missionProxy;
		gestureMapFragment.setOnPathFinishedListener(this);
	}

    @Override
    public void onResume(){
        super.onResume();
        setupTool(getTool());
    }

    @Override
    public void onStart(){
        super.onStart();
        missionProxy.selection.addSelectionUpdateListener(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        missionProxy.selection.removeSelectionUpdateListener(this);
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
				if (!missionProxy.contains(itemDetailFragment.getItem())) {
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
	public void onMapClick(Coord2D point) {
        //If an mission item is selected, unselect it.
        missionProxy.selection.clearSelection();

		switch (getTool()) {
		case MARKER:
            if(mIsSplineEnabled){
                missionProxy.addSplineWaypoint(point);
            }
            else {
                missionProxy.addWaypoint(point);
            }
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
		missionProxy.selection.clearSelection();
		setupTool(tools);
	}

    private void setupTool(EditorTools tool){
        switch (tool) {
            case DRAW:
                enableSplineToggle(true);
                gestureMapFragment.enableGestureDetection();
                break;

            case POLY:
                enableSplineToggle(false);
                Toast.makeText(this,R.string.draw_the_survey_region, Toast.LENGTH_SHORT).show();
                gestureMapFragment.enableGestureDetection();
                break;

            case MARKER:
                //Enable the spline selection toggle
                enableSplineToggle(true);
                gestureMapFragment.disableGestureDetection();
                break;

            case TRASH:
            case NONE:
                enableSplineToggle(false);
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

    private void enableSplineToggle(boolean isEnabled){
        if(mSplineToggleContainer != null){
            mSplineToggleContainer.setVisibility(isEnabled ? View.VISIBLE : View.INVISIBLE);
        }
    }

	private void showItemDetail(MissionItemProxy item) {
		if (itemDetailFragment == null) {
			addItemDetail(item);
		} else {
			switchItemDetail(item);
		}
	}

    private void addItemDetail(MissionItemProxy item) {
        itemDetailFragment = item.getDetailFragment();
        if(itemDetailFragment == null)
            return;

        if (mContainerItemDetail == null) {
            itemDetailFragment.show(fragmentManager, ITEM_DETAIL_TAG);
        }
        else {
            fragmentManager.beginTransaction().replace(R.id.containerItemDetail,
                    itemDetailFragment, ITEM_DETAIL_TAG).commit();
        }
    }

	public void switchItemDetail(MissionItemProxy item) {
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
		List<Coord2D> points = planningMapFragment.projectPathIntoMap(path);
		switch (getTool()) {
		case DRAW:
            if(mIsSplineEnabled){
                missionProxy.addSplineWaypoints(points);
            }
            else {
                missionProxy.addWaypoints(points);
            }
			break;

		case POLY:
			if (path.size()>2) {
				missionProxy.addSurveyPolygon(points);
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
    public void onDetailDialogDismissed(MissionItemProxy item) {
        missionProxy.selection.removeItemFromSelection(item);
    }

	@Override
	public void onWaypointTypeChanged(MissionItemProxy newItem, MissionItemProxy oldItem) {
		missionProxy.replace(oldItem, newItem);
	}

	private static final int MENU_DELETE = 1;
	private static final int MENU_REVERSE = 2;

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch(item.getItemId()){
		case MENU_DELETE:
			missionProxy.removeSelection(missionProxy.selection);
			mode.finish();
			return true;

		case MENU_REVERSE:
			missionProxy.reverse();
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
		missionProxy.selection.clearSelection();
		contextualActionBar = null;
		editorToolsFragment.getView().setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
		return false;
	}

	@Override
	public boolean onItemLongClick(MissionItemProxy item) {
		if (contextualActionBar != null) {
			if (missionProxy.selection.selectionContains(item)) {
				missionProxy.selection.clearSelection();
			} else {
				missionProxy.selection.setSelectionTo(missionProxy.getItems());
			}
		} else {
			editorToolsFragment.setTool(EditorTools.NONE);
			missionListFragment.updateChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			contextualActionBar = startActionMode(this);
			missionProxy.selection.setSelectionTo(item);
		}
		return true;
	}

	@Override
	public void onItemClick(MissionItemProxy item) {
		switch (getTool()) {
		default:
			if (contextualActionBar != null) {
				if (missionProxy.selection.selectionContains(item)) {
					missionProxy.selection.removeItemFromSelection(item);
				} else {
					missionProxy.selection.addToSelection(item);
				}
			} else {
				if (missionProxy.selection.selectionContains(item)) {
					missionProxy.selection.clearSelection();
				} else {
					editorToolsFragment.setTool(EditorTools.NONE);
					missionProxy.selection.setSelectionTo(item);
				}
			}
			break;

		case TRASH:
			missionProxy.removeItem(item);
			missionProxy.selection.clearSelection();
			if (missionProxy.getItems().size() <= 0) {
				editorToolsFragment.setTool(EditorTools.NONE);
			}
			break;
		}
	}

	@Override
	public void onListVisibilityChanged() {
	}

    @Override
    public void onSelectionUpdate(List<MissionItemProxy> selected) {
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
                        missionProxy.clear();
                        missionProxy.addTakeoff();
                    }

                    @Override
                    public void onNo() {
                    }
                }
        );

		ynd.show(getSupportFragmentManager(), "clearMission");
	}
}
