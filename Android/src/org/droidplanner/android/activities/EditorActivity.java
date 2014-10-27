package org.droidplanner.android.activities;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.dialogs.EditInputDialog;
import org.droidplanner.android.dialogs.SelectionDialog;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.YesNoWithPrefsDialog;
import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.dialogs.openfile.OpenMissionDialog;
import org.droidplanner.android.fragments.EditorListFragment;
import org.droidplanner.android.fragments.EditorMapFragment;
import org.droidplanner.android.fragments.helpers.GestureMapFragment;
import org.droidplanner.android.fragments.helpers.GestureMapFragment.OnPathFinishedListener;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.MissionSelection;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.proxy.mission.item.adapters.AdapterMissionItems;
import org.droidplanner.android.proxy.mission.item.fragments.MissionDetailFragment;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.file.FileStream;
import org.droidplanner.android.utils.file.IO.MissionReader;
import org.droidplanner.android.utils.file.IO.MissionWriter;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.widgets.actionProviders.EditorToolsActionProvider;
import org.droidplanner.android.widgets.actionProviders.EditorToolsActionProvider.EditorTools;
import org.droidplanner.android.widgets.actionProviders.EditorToolsActionProvider.OnEditorToolSelected;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.helpers.units.Speed;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.util.Pair;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.Messages.ardupilotmega.msg_mission_item;
import com.google.android.gms.analytics.HitBuilders;

/**
 * This implements the map editor activity. The map editor activity allows the
 * user to create and/or modify autonomous missions for the drone.
 */
public class EditorActivity extends DrawerNavigationUI implements OnPathFinishedListener,
        OnEditorToolSelected, MissionDetailFragment.OnMissionDetailListener, OnEditorInteraction,
		Callback, MissionSelection.OnSelectionUpdateListener, OnClickListener, OnLongClickListener, AdapterView.OnItemSelectedListener {

	/**
	 * Used to retrieve the item detail window when the activity is destroyed,
	 * and recreated.
	 */
	private static final String ITEM_DETAIL_TAG = "Item Detail Window";
    private static final String EXTRA_SPINNER_SELECTION_PER_TOOL ="extra_spinner_selection_per_tool";
    private static final String EXTRA_EDITOR_TOOL = "extra_editor_tool";

    /**
	 * Used to provide access and interact with the
	 * {@link org.droidplanner.core.mission.Mission} object on the Android
	 * layer.
	 */
	private MissionProxy missionProxy;

    /**
     * Menu action provider hosting the editor tools.
     */
    private EditorToolsActionProvider editorToolbar;

    /**
     * Stores the default or last used editor tool.
     */
    private EditorToolsActionProvider.EditorTools savedEditorTool;

	/*
	 * View widgets.
	 */
	private EditorMapFragment planningMapFragment;
	private GestureMapFragment gestureMapFragment;
	private MissionDetailFragment itemDetailFragment;
	private FragmentManager fragmentManager;
	private EditorListFragment missionListFragment;
	private TextView infoView;

    private Spinner waypointTypeSpinner;
    private Bundle spinnerSelectionPerTool;

    private boolean mMultiEditEnabled;

	/**
	 * This view hosts the mission item detail fragment. On phone, or device
	 * with limited screen estate, it's removed from the layout, and the item
	 * detail ends up displayed as a dialog.
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
		missionListFragment = (EditorListFragment) fragmentManager
				.findFragmentById(R.id.missionFragment1);

		infoView = (TextView) findViewById(R.id.editorInfoWindow);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button);
        resetMapBearing.setVisibility(View.GONE);

        final ImageButton zoomToFit = (ImageButton) findViewById(R.id.zoom_to_fit_button);
        zoomToFit.setVisibility(View.VISIBLE);
        zoomToFit.setOnClickListener(this);
		final ImageButton mGoToMyLocation = (ImageButton) findViewById(R.id.my_location_button);
		mGoToMyLocation.setOnClickListener(this);
		mGoToMyLocation.setOnLongClickListener(this);
		final ImageButton mGoToDroneLocation = (ImageButton) findViewById(R.id.drone_location_button);
		mGoToDroneLocation.setOnClickListener(this);
		mGoToDroneLocation.setOnLongClickListener(this);

        String editorToolName = EditorToolsActionProvider.EditorTools.MARKER.name();

        if(savedInstanceState != null){
            editorToolName = savedInstanceState.getString(EXTRA_EDITOR_TOOL, editorToolName);
            this.spinnerSelectionPerTool = savedInstanceState.getBundle
                    (EXTRA_SPINNER_SELECTION_PER_TOOL);
        }

        if(this.spinnerSelectionPerTool == null)
            this.spinnerSelectionPerTool = new Bundle(EditorTools.values().length);

        this.savedEditorTool = EditorToolsActionProvider.EditorTools.valueOf(editorToolName);

        waypointTypeSpinner = (Spinner) findViewById(R.id.waypoint_type_spinner);
        waypointTypeSpinner.setOnItemSelectedListener(this);

		// Retrieve the item detail fragment using its tag
		itemDetailFragment = (MissionDetailFragment) fragmentManager
                .findFragmentByTag(ITEM_DETAIL_TAG);

		/*
		 * On phone, this view will be null causing the item detail to be shown
		 * as a dialog.
		 */
		mContainerItemDetail = findViewById(R.id.containerItemDetail);

		gestureMapFragment.setOnPathFinishedListener(this);
	}

    @Override
    public void onApiConnected(DroidPlannerApi api){
        super.onApiConnected(api);

        if(planningMapFragment != null)
            planningMapFragment.onApiConnected(api);

        if(missionListFragment != null)
            missionListFragment.onApiConnected(api);

        missionProxy = dpApi.getMissionProxy();
        if(missionProxy != null)
           missionProxy.selection.addSelectionUpdateListener(this);
    }

    @Override
    public void onApiDisconnected(){
        super.onApiDisconnected();

        if(planningMapFragment != null)
            planningMapFragment.onApiDisconnected();

        if(missionProxy != null)
            missionProxy.selection.removeSelectionUpdateListener(this);
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.zoom_to_fit_button:
			if(planningMapFragment != null){
                planningMapFragment.zoomToFit();
            }
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
        if(editorToolbar != null)
		    editorToolbar.setToolAndUpdateView(getTool());

		setupTool(getTool());
	}

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBundle(EXTRA_SPINNER_SELECTION_PER_TOOL, this.spinnerSelectionPerTool);

        if(editorToolbar != null)
            outState.putString(EXTRA_EDITOR_TOOL, editorToolbar.getTool().name());
    }

    @Override
    protected int getNavigationDrawerEntryId() {
        return R.id.navigation_editor;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        //Reset the previous editor toolbar
        if(editorToolbar != null){
            editorToolbar.initialize(null, null, EditorToolsActionProvider.EditorTools.NONE);
        }

		getMenuInflater().inflate(R.menu.menu_mission, menu);

        final MenuItem editorToolbarItem = menu.findItem(R.id.menu_editor_toolbar);
        if(editorToolbarItem != null)
            editorToolbar = (EditorToolsActionProvider) editorToolbarItem.getActionProvider();

        editorToolbar.initialize(this, missionProxy, savedEditorTool);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(editorToolbar != null)
            editorToolbar.setTool(EditorTools.NONE);

		switch (item.getItemId()) {
		case R.id.menu_send_mission:
            if(dpApi != null) {
                final MissionProxy missionProxy = dpApi.getMissionProxy();
                if (dpApi.getMission().getItems().isEmpty()
                        || dpApi.getMission().hasTakeoffAndLandOrRTL()) {
                    missionProxy.sendMissionToAPM();
                } else {
                    YesNoWithPrefsDialog dialog = YesNoWithPrefsDialog.newInstance(
                            getApplicationContext(), "Mission Upload",
                            "Do you want to append a Takeoff and RTL to your mission?", "Ok",
                            "Skip", new YesNoDialog.Listener() {

                                @Override
                                public void onYes() {
                                    missionProxy.addTakeOffAndRTL();
                                    missionProxy.sendMissionToAPM();
                                }

                                @Override
                                public void onNo() {
                                    missionProxy.sendMissionToAPM();
                                }
                            }, getString(R.string.pref_auto_insert_mission_takeoff_rtl_land_key));

                    if (dialog != null) {
                        dialog.show(getSupportFragmentManager(), "Mission Upload check.");
                    }
                }
            }
			return true;

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
		OpenFileDialog missionDialog = new OpenMissionDialog() {
			@Override
			public void waypointFileLoaded(MissionReader reader) {
                if(dpApi != null) {
                        dpApi.getMission().onMissionLoaded(reader.getMsgMissionItems());
                }

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
                        if(dpApi != null) {
                                final List<msg_mission_item> missionItems = dpApi.getMission()
                                        .getMsgMissionItems();
                                if (MissionWriter.write(missionItems, input.toString())) {
                                    Toast.makeText(context, R.string.file_saved_success, Toast.LENGTH_SHORT).show();

                                    final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                                            .setCategory(GAUtils.Category.MISSION_PLANNING)
                                            .setAction("Mission saved to file")
                                            .setLabel("Mission items count")
                                            .setValue(missionItems.size());
                                    GAUtils.sendEvent(eventBuilder);

                                    return;
                                }
                        }

                        Toast.makeText(context, R.string.file_saved_error, Toast.LENGTH_SHORT).show();
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
            if(missionProxy != null) {
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

        if(missionProxy == null) return;

		// If an mission item is selected, unselect it.
		missionProxy.selection.clearSelection();

		switch (getTool()) {
		case MARKER:
                final MissionItemType selectionType = (MissionItemType) this.waypointTypeSpinner
                        .getSelectedItem();
            if(selectionType != null){
                missionProxy.addMissionItem(selectionType, point);
                if(selectionType == MissionItemType.LAND)
                    editorToolbar.setTool(EditorTools.NONE);
            }
			break;

		default:
			break;
		}
	}

    @Override
    public void onPathFinished(List<Coord2D> path) {
        List<Coord2D> points = planningMapFragment.projectPathIntoMap(path);
        switch (getTool()) {
            case DRAW:
                if(points.size() > 2) {
                    final MissionItemType selectionType = (MissionItemType) this.waypointTypeSpinner
                            .getSelectedItem();
                    if (selectionType != null) {
                        missionProxy.addMissionItems(selectionType, points);
                    }

                    editorToolbar.setTool(EditorTools.NONE);
                }
                else {
                    editorToolbar.setTool(EditorTools.DRAW);
                }
                break;

            default:
                editorToolbar.setTool(EditorTools.NONE);
                break;
        }

    }

	public EditorTools getTool() {
        if(editorToolbar != null)
            return editorToolbar.getTool();

        return this.savedEditorTool;
	}

	@Override
	public void editorToolChanged(EditorTools tools) {
		if(missionProxy != null) missionProxy.selection.clearSelection();
		setupTool(tools);
	}

	private void setupTool(EditorTools tool) {
		planningMapFragment.skipMarkerClickEvents(false);
		gestureMapFragment.disableGestureDetection();

		this.savedEditorTool = tool;

		final MissionItemType[] availableMissionItemTypes = tool.getSupportedMissionItemType();
		final AdapterMissionItems spinnerAdapter = new AdapterMissionItems(this,
				R.layout.editor_spinner_entry, availableMissionItemTypes);
		int spinnerSelection = spinnerSelectionPerTool.getInt(tool.name(),
				tool.getDefaultMissionItemTypeIndex());
		if (spinnerSelection > availableMissionItemTypes.length)
			spinnerSelection = tool.getDefaultMissionItemTypeIndex();

		this.waypointTypeSpinner.setAdapter(spinnerAdapter);
		this.waypointTypeSpinner.setSelection(spinnerSelection);

		switch (tool) {
		case DRAW:
			gestureMapFragment.enableGestureDetection();
			break;

		case MARKER:
			planningMapFragment.skipMarkerClickEvents(true);
			break;

		case COMMAND:
            //Show a dialog displaying the available mission commands.
            final CharSequence[] selections = {
                    MissionItemType.TAKEOFF.getLabel(),
                    MissionItemType.CHANGE_SPEED.getLabel(),
                    MissionItemType.RTL.getLabel()
            };
            SelectionDialog selectionDialog = SelectionDialog.newInstance("Select mission " +
                    "command", selections, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final Context context = getApplicationContext();

                    String label = selections[which].toString();
                    final MissionItemType selectedType = MissionItemType.fromLabel(label);

                    List<MissionItemProxy> missionProxies = missionProxy.getItems();
                    boolean allGood = missionProxies.isEmpty();
                    if(!allGood){
                        MissionItemType lastMissionItemType = missionProxies
                                .get(missionProxies.size() -1).getMissionItem().getType() ;

                        switch(selectedType){
                            case TAKEOFF:
                                //Takeoff should be preceded by Land or RTL
                                allGood = lastMissionItemType == MissionItemType.LAND ||
                                        lastMissionItemType == MissionItemType.RTL;
                                if(!allGood){
                                    Toast.makeText(context, "Must be preceded by Land or RTL",
                                            Toast.LENGTH_SHORT).show();
                                }
                                break;

                            case CHANGE_SPEED:
                                //Can be preceded by any type of waypoint.
                                allGood = true;
                                break;

                            case RTL:
                                //Can be preceded by any type except itself.
                                allGood = lastMissionItemType != selectedType;
                                if(!allGood){
                                    Toast.makeText(context, "Cannot be preceded by RTL",
                                            Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }

                    if(allGood) {
                        missionProxy.addMissionCmd(selectedType);
                        Toast.makeText(context, label + " command added.", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });

            if(selectionDialog != null){
                selectionDialog.show(fragmentManager, "Mission command selection dialog");
            }

            editorToolbar.setTool(EditorTools.NONE);
            break;

		default:
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
	public void onDetailDialogDismissed(List<MissionItemProxy> itemList) {
        if(missionProxy != null) missionProxy.selection.removeItemsFromSelection(itemList);
	}

	@Override
	public void onWaypointTypeChanged(MissionItemType newType, List<Pair<MissionItemProxy,
            MissionItemProxy>> oldNewItemsList) {
		if(missionProxy.replaceAll(oldNewItemsList) > 0 && editorToolbar != null
                && (newType == MissionItemType.LAND || newType == MissionItemType.RTL)){
         editorToolbar.setTool(EditorTools.NONE);
        }
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

            if(missionProxy != null) {
                final List<MissionItemProxy> selectedProxies = missionProxy.selection.getSelected();
                if (selectedProxies.size() >= 1) {
                    showItemDetail(selectMissionDetailType(selectedProxies));
                    enableMultiEdit(true);
                    return true;
                }
            }

			Toast.makeText(getApplicationContext(), R.string.editor_multi_edit_no_waypoint_error,
                    Toast.LENGTH_LONG).show();
			return true;

		case R.id.menu_action_delete:
            if(missionProxy != null)
			    missionProxy.removeSelection(missionProxy.selection);
			mode.finish();
            planningMapFragment.zoomToFit();
			return true;

		case R.id.menu_action_reverse:
            if(missionProxy != null)
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
            else if (referenceType != proxyType || MissionDetailFragment
                    .typeWithNoMuliEditSupport.contains(referenceType)) {
                    //Return a generic mission detail.
                    return new MissionDetailFragment();
                }
        }

        return MissionDetailFragment.newInstance(referenceType);
    }

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.action_mode_editor, menu);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode arg0) {
		missionListFragment.updateChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        if(missionProxy != null)
            missionProxy.selection.clearSelection();

        contextualActionBar = null;
        enableMultiEdit(false);
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

        if(missionProxy == null) return false;

		if (contextualActionBar != null) {
			if (missionProxy.selection.selectionContains(item)) {
				missionProxy.selection.clearSelection();
			} else {
				missionProxy.selection.setSelectionTo(missionProxy.getItems());
			}
		} else {
			editorToolbar.setTool(EditorTools.NONE);
			missionListFragment.updateChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
			contextualActionBar = startActionMode(this);
			missionProxy.selection.setSelectionTo(item);
		}
		return true;
	}

	@Override
	public void onItemClick(MissionItemProxy item, boolean zoomToFit) {
        enableMultiEdit(false);

        if(missionProxy == null) return;

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
					editorToolbar.setTool(EditorTools.NONE);
					missionProxy.selection.setSelectionTo(item);
				}
			}

			break;

		case TRASH:
			missionProxy.removeItem(item);
			missionProxy.selection.clearSelection();

			if (missionProxy.getItems().size() <= 0) {
				editorToolbar.setTool(EditorTools.NONE);
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
	public void onSelectionUpdate(List<MissionItemProxy> selected) {
		final boolean isEmpty = selected.isEmpty();

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
                        if(missionProxy != null) {
                            missionProxy.clear();
                        }
                    }

                    @Override
                    public void onNo() {}
                });

        if(ynd != null) {
            ynd.show(getSupportFragmentManager(), "clearMission");
        }
	}

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(this.savedEditorTool != null)
            this.spinnerSelectionPerTool.putInt(this.savedEditorTool.name(), position);

        MissionItemType selectedType = (MissionItemType) this.waypointTypeSpinner.getSelectedItem();
        if(selectedType == MissionItemType.SURVEY){
            Toast.makeText(this, R.string.draw_the_survey_region, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        if(this.savedEditorTool != null) {
            this.spinnerSelectionPerTool.putInt(this.savedEditorTool.name(),
                    this.savedEditorTool.getDefaultMissionItemTypeIndex());
        }
    }
}
