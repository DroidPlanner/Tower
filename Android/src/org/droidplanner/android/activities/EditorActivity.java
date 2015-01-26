package org.droidplanner.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.dialogs.EditInputDialog;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.dialogs.openfile.OpenMissionDialog;
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
import org.droidplanner.android.utils.prefs.AutoPanMode;

import java.util.List;

/**
 * This implements the map editor activity. The map editor activity allows the
 * user to create and/or modify autonomous missions for the drone.
 */
public class EditorActivity extends DrawerNavigationUI implements OnPathFinishedListener,
        OnEditorToolSelected, MissionDetailFragment.OnMissionDetailListener, OnEditorInteraction, MissionSelection.OnSelectionUpdateListener, OnClickListener, OnLongClickListener {

    /**
     * Used to retrieve the item detail window when the activity is destroyed,
     * and recreated.
     */
    private static final String ITEM_DETAIL_TAG = "Item Detail Window";

    private static final String EXTRA_IS_SPLINE_ENABLED = "extra_is_spline_enabled";
    private static final String EXTRA_OPENED_MISSION_FILENAME = "extra_opened_mission_filename";

    private static final IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(MissionProxy.ACTION_MISSION_PROXY_UPDATE);
        eventFilter.addAction(AttributeEvent.MISSION_RECEIVED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case MissionProxy.ACTION_MISSION_PROXY_UPDATE:
                    updateMissionLength();
                    break;

                case AttributeEvent.MISSION_RECEIVED:
                    final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
                    if (planningMapFragment != null) {
                        planningMapFragment.zoomToFit();
                    }
                    break;
            }
        }
    };

    /**
     * Used to provide access and interact with the
     * {@link org.droidplanner.android.proxy.mission.MissionProxy} object on the Android
     * layer.
     */
    private MissionProxy missionProxy;

    /*
     * View widgets.
     */
    private GestureMapFragment gestureMapFragment;
    private EditorToolsFragment editorToolsFragment;
    private MissionDetailFragment itemDetailFragment;
    private FragmentManager fragmentManager;

    private View mSplineToggleContainer;
    private boolean mIsSplineEnabled;

    private TextView infoView;

    /**
     * If the mission was loaded from a file, the filename is stored here.
     */
    private String openedMissionFilename;

    private RadioButton normalToggle;
    private RadioButton splineToggle;
    private View mLocationButtonsContainer;
    private ImageButton itemDetailToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        fragmentManager = getSupportFragmentManager();

        gestureMapFragment = ((GestureMapFragment) fragmentManager.findFragmentById(R.id.gestureMapFragment));
        editorToolsFragment = (EditorToolsFragment) fragmentManager.findFragmentById(R.id.editor_tools_fragment);

        mSplineToggleContainer = findViewById(R.id.editorSplineToggleContainer);
        mSplineToggleContainer.setVisibility(View.VISIBLE);

        infoView = (TextView) findViewById(R.id.editorInfoWindow);

        mLocationButtonsContainer = findViewById(R.id.location_button_container);
        final ImageButton zoomToFit = (ImageButton) findViewById(R.id.zoom_to_fit_button);
        zoomToFit.setVisibility(View.VISIBLE);
        zoomToFit.setOnClickListener(this);

        final ImageButton mGoToMyLocation = (ImageButton) findViewById(R.id.my_location_button);
        mGoToMyLocation.setOnClickListener(this);
        mGoToMyLocation.setOnLongClickListener(this);

        final ImageButton mGoToDroneLocation = (ImageButton) findViewById(R.id.drone_location_button);
        mGoToDroneLocation.setOnClickListener(this);
        mGoToDroneLocation.setOnLongClickListener(this);

        itemDetailToggle = (ImageButton) findViewById(R.id.toggle_action_drawer);
        itemDetailToggle.setOnClickListener(this);

        normalToggle = (RadioButton) findViewById(R.id.normalWpToggle);
        normalToggle.setOnClickListener(this);
        splineToggle = (RadioButton) findViewById(R.id.splineWpToggle);
        splineToggle.setOnClickListener(this);

        if (savedInstanceState != null) {
            mIsSplineEnabled = savedInstanceState.getBoolean(EXTRA_IS_SPLINE_ENABLED);
            openedMissionFilename = savedInstanceState.getString(EXTRA_OPENED_MISSION_FILENAME);
        }

        // Retrieve the item detail fragment using its tag
        itemDetailFragment = (MissionDetailFragment) fragmentManager.findFragmentByTag(ITEM_DETAIL_TAG);

        gestureMapFragment.setOnPathFinishedListener(this);
        openActionDrawer();
    }

    @Override
    protected float getActionDrawerTopMargin() {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
    }

    /**
     * Account for the various ui elements and update the map padding so that it
     * remains 'visible'.
     */
    private void updateLocationButtonsMargin(boolean isOpened) {
        final View actionDrawer = getActionDrawer();
        if (actionDrawer == null)
            return;

        itemDetailToggle.setActivated(isOpened);

        // Update the right margin for the my location button
        final ViewGroup.MarginLayoutParams marginLp = (ViewGroup.MarginLayoutParams) mLocationButtonsContainer
                .getLayoutParams();
        final int rightMargin = isOpened ? marginLp.leftMargin + actionDrawer.getWidth() : marginLp.leftMargin;
        marginLp.setMargins(marginLp.leftMargin, marginLp.topMargin, rightMargin, marginLp.bottomMargin);
        mLocationButtonsContainer.requestLayout();
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        missionProxy = dpApp.getMissionProxy();
        if (missionProxy != null) {
            missionProxy.selection.addSelectionUpdateListener(this);
            itemDetailToggle.setVisibility(missionProxy.selection.getSelected().isEmpty() ? View.GONE : View.VISIBLE);
        }

        updateMissionLength();
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();

        if (missionProxy != null)
            missionProxy.selection.removeSelectionUpdateListener(this);

        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public void onClick(View v) {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();

        switch (v.getId()) {
            case R.id.toggle_action_drawer:
                if (missionProxy == null)
                    return;

                if (itemDetailFragment == null) {
                    List<MissionItemProxy> selected = missionProxy.selection.getSelected();
                    showItemDetail(selectMissionDetailType(selected));
                } else {
                    removeItemDetail();
                }
                break;

            case R.id.zoom_to_fit_button:
                if (planningMapFragment != null) {
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
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();

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
    protected int getToolbarId() {
        return R.id.actionbar_container;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(EXTRA_IS_SPLINE_ENABLED, mIsSplineEnabled);
        outState.putString(EXTRA_OPENED_MISSION_FILENAME, openedMissionFilename);
    }

    @Override
    protected int getNavigationDrawerEntryId() {
        return R.id.navigation_editor;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_mission, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_open_mission:
                openMissionFile();
                return true;

            case R.id.menu_save_mission:
                saveMissionFile();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openMissionFile() {
        OpenFileDialog missionDialog = new OpenMissionDialog() {
            @Override
            public void waypointFileLoaded(MissionReader reader) {
                openedMissionFilename = getSelectedFilename();
                missionProxy.readMissionFromFile(reader);
                gestureMapFragment.getMapFragment().zoomToFit();
            }
        };
        missionDialog.openDialog(this);
    }

    private void saveMissionFile() {
        final Context context = getApplicationContext();
        final String defaultFilename = TextUtils.isEmpty(openedMissionFilename)
                ? FileStream.getWaypointFilename("waypoints")
                : openedMissionFilename;

        final EditInputDialog dialog = EditInputDialog.newInstance(context, getString(R.string.label_enter_filename),
                defaultFilename, new EditInputDialog.Listener() {
                    @Override
                    public void onOk(CharSequence input) {
                        if (missionProxy.writeMissionToFile(input.toString())) {
                            Toast.makeText(context, R.string.file_saved_success, Toast.LENGTH_SHORT)
                                    .show();

                            final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                                    .setCategory(GAUtils.Category.MISSION_PLANNING)
                                    .setAction("Mission saved to file")
                                    .setLabel("Mission items count");
                            GAUtils.sendEvent(eventBuilder);

                            return;
                        }

                        Toast.makeText(context, R.string.file_saved_error, Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onCancel() {
                    }
                });

        dialog.show(getSupportFragmentManager(), "Mission filename");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        gestureMapFragment.getMapFragment().saveCameraPosition();
    }

    private void updateMissionLength() {
        if (missionProxy != null) {

            double missionLength = missionProxy.getMissionLength();
            LengthUnit convertedMissionLength = unitSystem.getLengthUnitProvider().boxBaseValueToTarget(missionLength);
            double speedParameter = dpApp.getDrone().getSpeedParameter();
            String infoString = getString(R.string.editor_info_window_distance, convertedMissionLength.toString());
            if (speedParameter > 0) {
                int time = (int) (missionLength / speedParameter);
                infoString += ", " + getString(R.string.editor_info_window_flight_time, time / 60, time % 60);
            }
            infoView.setText(infoString);

            // Remove detail window if item is removed
            if (missionProxy.selection.getSelected().isEmpty() && itemDetailFragment != null) {
                removeItemDetail();
            }
        }
    }

    @Override
    public void onMapClick(LatLong point) {
        if (missionProxy == null) return;

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

            default:
                break;
        }
    }

    public EditorTools getTool() {
        return editorToolsFragment.getTool();
    }

    @Override
    public void editorToolChanged(EditorTools tools) {
        setupTool(tools);
    }

    private void setupTool(EditorTools tool) {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        if (planningMapFragment != null)
            planningMapFragment.skipMarkerClickEvents(false);

        gestureMapFragment.disableGestureDetection();
        boolean clearSelection = true;

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
                if (planningMapFragment != null)
                    planningMapFragment.skipMarkerClickEvents(true);
                break;

            case SELECTOR:
                enableSplineToggle(false);
                Toast.makeText(getApplicationContext(), "Click on mission items to select them.",
                        Toast.LENGTH_SHORT).show();
                break;

            case TRASH:
                enableSplineToggle(false);
                if (missionProxy != null) {
                    clearSelection = false;

                    List<MissionItemProxy> selected = missionProxy.selection.getSelected();
                    if (!selected.isEmpty()) {
                        deleteSelectedItems();
                    }
                }
                break;

            case NONE:
                clearSelection = false;
                enableSplineToggle(false);
                break;
        }

        if (missionProxy != null && clearSelection) missionProxy.selection.clearSelection();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateLocationButtonsMargin(itemDetailFragment != null);
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

        editorToolsFragment.setToolAndUpdateView(EditorTools.NONE);
    }

    private void addItemDetail(MissionDetailFragment itemDetail) {
        itemDetailFragment = itemDetail;
        if (itemDetailFragment == null)
            return;

        fragmentManager.beginTransaction()
                .replace(getActionDrawerId(), itemDetailFragment, ITEM_DETAIL_TAG)
                .commit();
        updateLocationButtonsMargin(true);
    }

    public void switchItemDetail(MissionDetailFragment itemDetail) {
        removeItemDetail();
        addItemDetail(itemDetail);
    }

    private void removeItemDetail() {
        if (itemDetailFragment != null) {
            fragmentManager.beginTransaction().remove(itemDetailFragment).commit();
            itemDetailFragment = null;

            updateLocationButtonsMargin(false);
        }
    }

    @Override
    public void onPathFinished(List<LatLong> path) {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        List<LatLong> points = planningMapFragment.projectPathIntoMap(path);
        switch (getTool()) {
            case DRAW:
                if (missionProxy != null) {
                    if (mIsSplineEnabled) {
                        missionProxy.addSplineWaypoints(points);
                    } else {
                        missionProxy.addWaypoints(points);
                    }
                }
                break;

            case POLY:
                if (missionProxy != null && path.size() > 2) {
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
        if (missionProxy != null) missionProxy.selection.removeItemsFromSelection(itemList);
    }

    @Override
    public void onWaypointTypeChanged(MissionItemType newType, List<Pair<MissionItemProxy,
            MissionItemProxy>> oldNewItemsList) {
        missionProxy.replaceAll(oldNewItemsList);
    }

    private MissionDetailFragment selectMissionDetailType(List<MissionItemProxy> proxies) {
        if (proxies == null || proxies.isEmpty())
            return null;

        MissionItemType referenceType = null;
        for (MissionItemProxy proxy : proxies) {
            final MissionItemType proxyType = proxy.getMissionItem().getType();
            if (referenceType == null) {
                referenceType = proxyType;
            } else if (referenceType != proxyType || MissionDetailFragment
                    .typeWithNoMultiEditSupport.contains(referenceType)) {
                //Return a generic mission detail.
                return new MissionDetailFragment();
            }
        }

        return MissionDetailFragment.newInstance(referenceType);
    }

    @Override
    public void onItemClick(MissionItemProxy item, boolean zoomToFit) {
        if (missionProxy == null) return;

        switch (getTool()) {
            case SELECTOR:
                if (missionProxy.selection.selectionContains(item)) {
                    missionProxy.selection.removeItemFromSelection(item);
                } else {
                    missionProxy.selection.addToSelection(item);
                }
                break;

            case TRASH:
                missionProxy.removeItem(item);
                missionProxy.selection.clearSelection();

                if (missionProxy.getItems().size() <= 0) {
                    editorToolsFragment.setTool(EditorTools.NONE);
                }
                break;

            default:
                if (missionProxy.selection.selectionContains(item)) {
                    missionProxy.selection.clearSelection();
                } else {
                    editorToolsFragment.setTool(EditorTools.NONE);
                    missionProxy.selection.setSelectionTo(item);
                }
                break;
        }

        if (zoomToFit) {
            final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
            List<MissionItemProxy> selected = missionProxy.selection.getSelected();
            if (selected.isEmpty()) {
                planningMapFragment.zoomToFit();
            } else {
                planningMapFragment.zoomToFit(MissionProxy.getVisibleCoords(selected));
            }
        }
    }

    @Override
    public void onListVisibilityChanged() {
    }

    @Override
    protected boolean enableMissionMenus() {
        return true;
    }

    @Override
    public void onSelectionUpdate(List<MissionItemProxy> selected) {
        final boolean isEmpty = selected.isEmpty();

        if (isEmpty) {
            itemDetailToggle.setVisibility(View.GONE);
            removeItemDetail();
        } else {
            itemDetailToggle.setVisibility(View.VISIBLE);
            if (getTool() == EditorTools.SELECTOR)
                removeItemDetail();
            else {
                showItemDetail(selectMissionDetailType(selected));
            }
        }

        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        if (planningMapFragment != null)
            planningMapFragment.postUpdate();
    }

    private void deleteSelectedItems() {
        YesNoDialog ynd = YesNoDialog.newInstance(getApplicationContext(), getString(R.string.delete_selected_waypoints_title),
                getString(R.string.delete_selected_waypoints_confirm), new YesNoDialog.Listener() {
                    @Override
                    public void onYes() {
                        if (missionProxy != null) {
                            missionProxy.removeSelection(missionProxy.selection);
                            if (missionProxy.selection.getSelected().isEmpty())
                                editorToolsFragment.setTool(EditorTools.NONE);
                        }
                    }

                    @Override
                    public void onNo() {
                    }
                });

        if (ynd != null) {
            ynd.show(getSupportFragmentManager(), "clearSelectedWaypoints");
        }
    }

    private void doClearMissionConfirmation() {
        YesNoDialog ynd = YesNoDialog.newInstance(getApplicationContext(), getString(R.string.dlg_clear_mission_title),
                getString(R.string.dlg_clear_mission_confirm), new YesNoDialog.Listener() {
                    @Override
                    public void onYes() {
                        if (missionProxy != null) {
                            missionProxy.clear();
                            missionProxy.addTakeoff();
                        }
                    }

                    @Override
                    public void onNo() {
                    }
                });

        if (ynd != null) {
            ynd.show(getSupportFragmentManager(), "clearMission");
        }
    }

}
