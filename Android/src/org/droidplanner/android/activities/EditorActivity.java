package org.droidplanner.android.activities;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.dialogs.EditInputDialog;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.dialogs.openfile.OpenMissionDialog;
import org.droidplanner.android.fragments.EditorListFragment;
import org.droidplanner.android.fragments.EditorMapFragment;
import org.droidplanner.android.fragments.EditorToolsFragment;
import org.droidplanner.android.fragments.EditorToolsFragment.EditorTools;
import org.droidplanner.android.fragments.EditorToolsFragment.OnEditorToolSelected;
import org.droidplanner.android.fragments.helpers.GestureMapFragment;
import org.droidplanner.android.fragments.helpers.GestureMapFragment.OnPathFinishedListener;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.MissionSelection;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.proxy.mission.item.fragments.MissionDetailFragment;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.file.FileStream;
import org.droidplanner.android.utils.file.IO.MissionReader;
import org.droidplanner.android.utils.file.IO.MissionWriter;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.helpers.units.Speed;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.util.Pair;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.common.msg_mission_item;
import com.google.android.gms.analytics.HitBuilders;

/**
 * This implements the map editor activity. The map editor activity allows the
 * user to create and/or modify autonomous missions for the drone.
 */
public class EditorActivity extends DrawerNavigationUI implements OnPathFinishedListener,
		OnEditorToolSelected, MissionDetailFragment.OnMissionDetailListener, OnEditorInteraction,
		Callback, MissionSelection.OnSelectionUpdateListener, OnClickListener, OnLongClickListener {

	/**
	 * Used to retrieve the item detail window when the activity is destroyed,
	 * and recreated.
	 */
	private static final String ITEM_DETAIL_TAG = "Item Detail Window";
    private static final String EXTRA_IS_SPLINE_ENABLED = "extra_is_spline_enabled";

    /**
	 * Used to provide access and interact with the
	 * {@link org.droidplanner.core.mission.Mission} object on the Android
	 * layer.
	 */
	private MissionProxy missionProxy;

	/*
	 * View widgets.
	 */
	private EditorMapFragment planningMapFragment;
	private GestureMapFragment gestureMapFragment;
	private EditorToolsFragment editorToolsFragment;
	private MissionDetailFragment itemDetailFragment;
	private FragmentManager fragmentManager;
	private EditorListFragment missionListFragment;

	private View mSplineToggleContainer;
	private boolean mIsSplineEnabled;

	private TextView infoView;

    private boolean mMultiEditEnabled;

	/**
	 * This view hosts the mission item detail fragment. On phone, or device
	 * with limited screen estate, it's removed from the layout, and the item
	 * detail ends up displayed as a dialog.
	 */
	private View mContainerItemDetail;

	private ActionMode contextualActionBar;
	private RadioButton normalToggle;
	private RadioButton splineToggle;

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
				.findFragmentById(R.id.flightActionsFragment);
		missionListFragment = (EditorListFragment) fragmentManager
				.findFragmentById(R.id.missionFragment1);

		mSplineToggleContainer = findViewById(R.id.editorSplineToggleContainer);
		mSplineToggleContainer.setVisibility(View.VISIBLE);

		infoView = (TextView) findViewById(R.id.editorInfoWindow);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button);
        resetMapBearing.setOnClickListener(this);
        final ImageButton zoomToFit = (ImageButton) findViewById(R.id.zoom_to_fit_button);
        zoomToFit.setVisibility(View.VISIBLE);
        zoomToFit.setOnClickListener(this);
		final ImageButton mGoToMyLocation = (ImageButton) findViewById(R.id.my_location_button);
		mGoToMyLocation.setOnClickListener(this);
		mGoToMyLocation.setOnLongClickListener(this);
		final ImageButton mGoToDroneLocation = (ImageButton) findViewById(R.id.drone_location_button);
		mGoToDroneLocation.setOnClickListener(this);
		mGoToDroneLocation.setOnLongClickListener(this);
		normalToggle = (RadioButton) findViewById(R.id.normalWpToggle);
		normalToggle.setOnClickListener(this);
		splineToggle = (RadioButton) findViewById(R.id.splineWpToggle);
		splineToggle.setOnClickListener(this);

        if(savedInstanceState != null){
            mIsSplineEnabled = savedInstanceState.getBoolean(EXTRA_IS_SPLINE_ENABLED);
        }

		// Retrieve the item detail fragment using its tag
		itemDetailFragment = (MissionDetailFragment) fragmentManager
                .findFragmentByTag(ITEM_DETAIL_TAG);

		/*
		 * On phone, this view will be null causing the item detail to be shown
		 * as a dialog.
		 */
		mContainerItemDetail = findViewById(R.id.containerItemDetail);

        final DroidPlannerApp dpApp = ((DroidPlannerApp) getApplication());
		missionProxy = dpApp.getMissionProxy();
		gestureMapFragment.setOnPathFinishedListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.map_orientation_button:
			if(planningMapFragment != null) {
				planningMapFragment.updateMapBearing(0);
			}
			break;
		case R.id.zoom_to_fit_button:
			if(planningMapFragment != null){
                planningMapFragment.zoomToFit();
            }
			break;
		case R.id.splineWpToggle:
			mIsSplineEnabled = splineToggle.isChecked();
			break;
		case R.id.normalWpToggle:
			mIsSplineEnabled = !normalToggle.isChecked();
			break;
		case R.id.drone_location_button:
			planningMapFragment.goToDroneLocation();
			break;
		case R.id.my_location_button:
			planningMapFragment.goToMyLocation();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onLongClick(View view) {
		switch (view.getId()) {
		case R.id.drone_location_button:
			planningMapFragment.setAutoPanMode(AutoPanMode.DRONE);
			return true;
		case R.id.my_location_button:
			planningMapFragment.setAutoPanMode(AutoPanMode.USER);
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		editorToolsFragment.setToolAndUpdateView(getTool());
		setupTool(getTool());
	}

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_IS_SPLINE_ENABLED, mIsSplineEnabled);
    }

    @Override
    protected int getNavigationDrawerEntryId() {
        return R.id.navigation_editor;
    }

    @Override
	public void onStart() {
		super.onStart();
		missionProxy.selection.addSelectionUpdateListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		missionProxy.selection.removeSelectionUpdateListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.menu_mission, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_open_mission:
			openMissionFile();
			return true;

		case R.id.menu_save_mission:
			saveMissionFile();
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	private void openMissionFile() {
		OpenFileDialog missionDialog = new OpenMissionDialog(drone) {
			@Override
			public void waypointFileLoaded(MissionReader reader) {
				drone.getMission().onMissionLoaded(reader.getMsgMissionItems());
				planningMapFragment.zoomToFit();
			}
		};
		missionDialog.openDialog(this);
	}

	private void saveMissionFile() {
        final Context context = getApplicationContext();
        final EditInputDialog dialog = EditInputDialog.newInstance(context, getString(R.string.label_enter_filename),
                FileStream.getWaypointFilename("waypoints"), new EditInputDialog.Listener() {
                    @Override
                    public void onOk(CharSequence input) {
                        final List<msg_mission_item> missionItems = drone.getMission()
                                .getMsgMissionItems();
                        if (MissionWriter.write( missionItems, input.toString())) {
                            Toast.makeText(context, R.string.file_saved_success, Toast.LENGTH_SHORT).show();

                            final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                                    .setCategory(GAUtils.Category.MISSION_PLANNING)
                                    .setAction("Mission saved to file")
                                    .setLabel("Mission items count")
                                    .setValue(missionItems.size());
                            GAUtils.sendEvent(eventBuilder);
                        } else {
                            Toast.makeText(context, R.string.file_saved_error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancel() {}
                });

        dialog.show(getSupportFragmentManager(), "Mission filename");
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
			Length missionLength = missionProxy.getMissionLength();
			Speed speedParameter = drone.getSpeed().getSpeedParameter();
			String infoString = "Distance " + missionLength;
			if (speedParameter != null) {
				int time = (int) (missionLength.valueInMeters() / speedParameter
						.valueInMetersPerSecond());
				infoString = infoString
						+ String.format(", Flight time: %02d:%02d", time / 60, time % 60);
			}
			infoView.setText(infoString);

			// Remove detail window if item is removed
			if (missionProxy.selection.getSelected().isEmpty() && itemDetailFragment != null) {
				removeItemDetail();
			}
			break;

		case MISSION_RECEIVED:
			if (planningMapFragment != null) {
				planningMapFragment.zoomToFit();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onMapClick(Coord2D point) {
        enableMultiEdit(false);

		// If an mission item is selected, unselect it.
		missionProxy.selection.clearSelection();

		switch (getTool()) {
		case MARKER:
			if (mIsSplineEnabled) {
				missionProxy.addSplineWaypoint(point);
			} else {
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

	private void setupTool(EditorTools tool) {
		planningMapFragment.skipMarkerClickEvents(false);
		switch (tool) {
		case DRAW:
			enableSplineToggle(true);
			gestureMapFragment.enableGestureDetection();
			break;

		case POLY:
			enableSplineToggle(false);
			Toast.makeText(this, R.string.draw_the_survey_region, Toast.LENGTH_SHORT).show();
			gestureMapFragment.enableGestureDetection();
			break;

		case MARKER:
			// Enable the spline selection toggle
			enableSplineToggle(true);
			gestureMapFragment.disableGestureDetection();
			planningMapFragment.skipMarkerClickEvents(true);
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

	private void enableSplineToggle(boolean isEnabled) {
		if (mSplineToggleContainer != null) {
			mSplineToggleContainer.setVisibility(isEnabled ? View.VISIBLE : View.INVISIBLE);
		}
	}

	private void showItemDetail(MissionDetailFragment itemDetail) {
		if (itemDetailFragment == null) {
			addItemDetail(itemDetail);
		} else {
			switchItemDetail(itemDetail);
		}
	}

	private void addItemDetail(MissionDetailFragment itemDetail) {
		itemDetailFragment = itemDetail;
		if (itemDetailFragment == null)
			return;

		if (mContainerItemDetail == null) {
			itemDetailFragment.show(fragmentManager, ITEM_DETAIL_TAG);
		} else {
			fragmentManager.beginTransaction()
					.replace(R.id.containerItemDetail, itemDetailFragment, ITEM_DETAIL_TAG)
					.commit();
		}
	}

	public void switchItemDetail(MissionDetailFragment itemDetail) {
		removeItemDetail();
		addItemDetail(itemDetail);
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
			if (mIsSplineEnabled) {
				missionProxy.addSplineWaypoints(points);
			} else {
				missionProxy.addWaypoints(points);
			}
			break;

		case POLY:
			if (path.size() > 2) {
				missionProxy.addSurveyPolygon(points);
			} else {
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
	public void onDetailDialogDismissed(List<MissionItemProxy> itemList) {
        missionProxy.selection.removeItemsFromSelection(itemList);
	}

	@Override
	public void onWaypointTypeChanged(List<Pair<MissionItemProxy, MissionItemProxy>> oldNewItemsList) {
		missionProxy.replaceAll(oldNewItemsList);
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_action_multi_edit:
            if(mMultiEditEnabled){
                removeItemDetail();
                enableMultiEdit(false);
                return true;
            }

			final List<MissionItemProxy> selectedProxies = missionProxy.selection.getSelected();
            if(selectedProxies.size() >= 1){
                showItemDetail(selectMissionDetailType(selectedProxies));
                enableMultiEdit(true);
            }
            else {
                Toast.makeText(getApplicationContext(), "No Waypoint(s) selected.", Toast.LENGTH_LONG)
                        .show();
            }

            HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                    .setCategory(GAUtils.Category.EDITOR)
                    .setAction("Action mode button")
                    .setLabel("Multi edit");
            GAUtils.sendEvent(eventBuilder);

            return true;

		case R.id.menu_action_delete:
			missionProxy.removeSelection(missionProxy.selection);
			mode.finish();
            planningMapFragment.zoomToFit();
			return true;

		case R.id.menu_action_reverse:
			missionProxy.reverse();
			return true;

		default:
			return false;
		}
	}

    private MissionDetailFragment selectMissionDetailType(List<MissionItemProxy> proxies){
        if(proxies == null || proxies.isEmpty())
            return null;

        MissionItemType referenceType = null;
        for(MissionItemProxy proxy: proxies){
            final MissionItemType proxyType = proxy.getMissionItem().getType();
            if(referenceType == null){
                referenceType = proxyType;
            }
            else if(referenceType != proxyType){
                //Return a generic mission detail.
                return new MissionDetailFragment();
            }
        }

        return MissionDetailFragment.newInstance(referenceType);
    }

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.action_mode_editor, menu);
		editorToolsFragment.getView().setVisibility(View.INVISIBLE);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode arg0) {
		missionListFragment.updateChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		missionProxy.selection.clearSelection();

        contextualActionBar = null;
        enableMultiEdit(false);

		editorToolsFragment.getView().setVisibility(View.VISIBLE);
	}

    private void enableMultiEdit(boolean enable){
        mMultiEditEnabled = enable;

        if(contextualActionBar != null){
            final Menu menu = contextualActionBar.getMenu();
            final MenuItem multiEdit = menu.findItem(R.id.menu_action_multi_edit);
            multiEdit.setIcon(mMultiEditEnabled
                    ? R.drawable.ic_action_copy_blue
                    : R.drawable.ic_action_copy);
        }
    }

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onItemLongClick(MissionItemProxy item) {
        enableMultiEdit(false);
		if (contextualActionBar != null) {
			if (missionProxy.selection.selectionContains(item)) {
				missionProxy.selection.clearSelection();
			} else {
				missionProxy.selection.setSelectionTo(missionProxy.getItems());
			}
		} else {
			editorToolsFragment.setTool(EditorTools.NONE);
			missionListFragment.updateChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
			contextualActionBar = startActionMode(this);
			missionProxy.selection.setSelectionTo(item);
		}
		return true;
	}

	@Override
	public void onItemClick(MissionItemProxy item, boolean zoomToFit) {
        enableMultiEdit(false);
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

        if(zoomToFit) {
            List<MissionItemProxy> selected = missionProxy.selection.getSelected();
            if (selected.isEmpty()) {
                planningMapFragment.zoomToFit();
            }
            else{
                planningMapFragment.zoomToFit(MissionProxy.getVisibleCoords(selected));
            }
        }
	}

	@Override
	public void onListVisibilityChanged() {}

    @Override
    protected boolean enableMissionMenus(){
        return true;
    }

	@Override
	public void onSelectionUpdate(List<MissionItemProxy> selected) {
		final boolean isEmpty = selected.isEmpty();

		missionListFragment.setArrowsVisibility(!isEmpty);

		if (isEmpty) {
			removeItemDetail();
		} else {
			if (contextualActionBar != null && !mMultiEditEnabled)
				removeItemDetail();
			else {
				showItemDetail(selectMissionDetailType(selected));
			}
		}

		planningMapFragment.postUpdate();
	}

	private void doClearMissionConfirmation() {
		YesNoDialog ynd = YesNoDialog.newInstance(getApplicationContext(), getString(R.string
                        .dlg_clear_mission_title),
                getString(R.string.dlg_clear_mission_confirm), new YesNoDialog.Listener() {
                    @Override
                    public void onYes() {
                        missionProxy.clear();
                        missionProxy.addTakeoff();
                    }

                    @Override
                    public void onNo() {}
                });

        if(ynd != null) {
            ynd.show(getSupportFragmentManager(), "clearMission");
        }
	}

}
