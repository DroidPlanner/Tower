package org.droidplanner.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

import com.o3dr.android.client.utils.FileUtils;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.dialogs.SupportEditInputDialog;
import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.fragments.EditorListFragment;
import org.droidplanner.android.fragments.EditorMapFragment;
import org.droidplanner.android.fragments.account.editor.tool.EditorToolsFragment;
import org.droidplanner.android.fragments.account.editor.tool.EditorToolsFragment.EditorTools;
import org.droidplanner.android.fragments.account.editor.tool.EditorToolsImpl;
import org.droidplanner.android.fragments.helpers.GestureMapFragment;
import org.droidplanner.android.fragments.helpers.GestureMapFragment.OnPathFinishedListener;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.MissionSelection;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.proxy.mission.item.fragments.MissionDetailFragment;
import org.droidplanner.android.utils.file.DirectoryPath;
import org.droidplanner.android.utils.file.FileList;
import org.droidplanner.android.utils.file.FileStream;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * This implements the map editor activity. The map editor activity allows the
 * user to create and/or modify autonomous missions for the drone.
 */
public class EditorActivity extends DrawerNavigationUI implements OnPathFinishedListener,
        EditorToolsFragment.EditorToolListener, MissionDetailFragment.OnMissionDetailListener,
        OnEditorInteraction, MissionSelection.OnSelectionUpdateListener, OnClickListener,
        OnLongClickListener, SupportEditInputDialog.Listener {

    /**
     * Used to retrieve the item detail window when the activity is destroyed,
     * and recreated.
     */
    private static final String ITEM_DETAIL_TAG = "Item Detail Window";

    private static final String EXTRA_OPENED_MISSION_FILENAME = "extra_opened_mission_filename";

    private static final IntentFilter eventFilter = new IntentFilter();
    private static final String MISSION_FILENAME_DIALOG_TAG = "Mission filename";

    static {
        eventFilter.addAction(MissionProxy.ACTION_MISSION_PROXY_UPDATE);
        eventFilter.addAction(AttributeEvent.MISSION_RECEIVED);
        eventFilter.addAction(AttributeEvent.PARAMETERS_REFRESH_COMPLETED);
        eventFilter.addAction(DroidPlannerPrefs.PREF_VEHICLE_DEFAULT_SPEED);
        eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
        eventFilter.addAction(AttributeEvent.HEARTBEAT_RESTORED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case MissionProxy.ACTION_MISSION_PROXY_UPDATE:
                    if (mAppPrefs.isZoomToFitEnable()) gestureMapFragment.getMapFragment().zoomToFit();
                    // FALL THROUGH
                case AttributeEvent.PARAMETERS_REFRESH_COMPLETED:
                case DroidPlannerPrefs.PREF_VEHICLE_DEFAULT_SPEED:
                case AttributeEvent.STATE_CONNECTED:
                case AttributeEvent.HEARTBEAT_RESTORED:
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

    private TextView infoView;

    /**
     * If the mission was loaded from a file, the filename is stored here.
     */
    private File openedMissionFile;

    private FloatingActionButton itemDetailToggle;
    private EditorListFragment editorListFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentManager = getSupportFragmentManager();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        gestureMapFragment = ((GestureMapFragment) fragmentManager.findFragmentById(R.id.editor_map_fragment));
        if (gestureMapFragment == null) {
            gestureMapFragment = new GestureMapFragment();
            fragmentManager.beginTransaction().add(R.id.editor_map_fragment, gestureMapFragment).commit();
        }

        editorToolsFragment = (EditorToolsFragment) fragmentManager.findFragmentById(R.id.mission_tools_fragment);

        infoView = (TextView) findViewById(R.id.editorInfoWindow);

        final FloatingActionButton zoomToFit = (FloatingActionButton) findViewById(R.id.zoom_to_fit_button);
        zoomToFit.setVisibility(View.VISIBLE);
        zoomToFit.setOnClickListener(this);

        final FloatingActionButton mGoToMyLocation = (FloatingActionButton) findViewById(R.id.my_location_button);
        mGoToMyLocation.setOnClickListener(this);
        mGoToMyLocation.setOnLongClickListener(this);

        final FloatingActionButton mGoToDroneLocation = (FloatingActionButton) findViewById(R.id.drone_location_button);
        mGoToDroneLocation.setOnClickListener(this);
        mGoToDroneLocation.setOnLongClickListener(this);

        itemDetailToggle = (FloatingActionButton) findViewById(R.id.toggle_action_drawer);
        itemDetailToggle.setOnClickListener(this);

        if (savedInstanceState != null) {
            String openedMissionFilename = savedInstanceState.getString(EXTRA_OPENED_MISSION_FILENAME);
            if(!TextUtils.isEmpty(openedMissionFilename)) {
                openedMissionFile = new File(openedMissionFilename);
            }
        }

        // Retrieve the item detail fragment using its tag
        itemDetailFragment = (MissionDetailFragment) fragmentManager.findFragmentByTag(ITEM_DETAIL_TAG);

        gestureMapFragment.setOnPathFinishedListener(this);
        openActionDrawer();
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){
        if(intent == null || missionProxy == null)
            return;

        String action = intent.getAction();
        if(TextUtils.isEmpty(action))
            return;

        switch (action) {
            case Intent.ACTION_VIEW:
                Uri loadUri = intent.getData();
                if (loadUri != null) {
                    openMissionFile(loadUri);
                }
                break;
        }
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
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        missionProxy = dpApp.getMissionProxy();
        if (missionProxy != null) {
            missionProxy.selection.addSelectionUpdateListener(this);
            itemDetailToggle.setVisibility(missionProxy.selection.getSelected().isEmpty() ? View.GONE : View.VISIBLE);
        }

        handleIntent(getIntent());

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
        setupTool();
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_container;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(openedMissionFile != null) {
            outState.putString(EXTRA_OPENED_MISSION_FILENAME, openedMissionFile.getAbsolutePath());
        }
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
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
        OpenFileDialog missionDialog = new OpenFileDialog() {
            @Override
            public void onFileSelected(String filepath) {
                File missionFile = new File(filepath);
                openedMissionFile = missionFile;
                openMissionFile(Uri.fromFile(missionFile));
            }
        };
        missionDialog.openDialog(this, DirectoryPath.getWaypointsPath(), FileList.getWaypointFileList());
    }

    private void openMissionFile(Uri missionUri){
        if(missionProxy != null) {
            missionProxy.readMissionFromFile(missionUri);
        }
    }

    @Override
    public void onOk(String dialogTag, CharSequence input) {
        switch (dialogTag) {
            case MISSION_FILENAME_DIALOG_TAG:
                File saveFile = openedMissionFile == null
                        ? new File(DirectoryPath.getWaypointsPath(), input.toString() + FileList.WAYPOINT_FILENAME_EXT)
                        : new File(openedMissionFile.getParent(), input.toString() + FileList.WAYPOINT_FILENAME_EXT);
                missionProxy.writeMissionToFile(Uri.fromFile(saveFile));
                break;
        }
    }

    @Override
    public void onCancel(String dialogTag) {
    }

    private void saveMissionFile() {
        final String defaultFilename = openedMissionFile == null
                ? getWaypointFilename("waypoints")
                : FileUtils.getFilenameWithoutExtension(openedMissionFile);

        final SupportEditInputDialog dialog = SupportEditInputDialog.newInstance(MISSION_FILENAME_DIALOG_TAG,
                getString(R.string.label_enter_filename), defaultFilename, true);

        dialog.show(getSupportFragmentManager(), MISSION_FILENAME_DIALOG_TAG);
    }

    private static String getWaypointFilename(String prefix){
        return prefix + "-" + FileStream.getTimeStamp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        gestureMapFragment.getMapFragment().saveCameraPosition();
    }

    private void updateMissionLength() {
        if (missionProxy != null) {

            Pair<Double, Double> distanceAndTime = missionProxy.getMissionFlightTime();
            LengthUnit convertedMissionLength = unitSystem.getLengthUnitProvider()
                .boxBaseValueToTarget(distanceAndTime.first);

            double time = distanceAndTime.second;
            String infoString = getString(R.string.editor_info_window_distance,
                convertedMissionLength.toString()) +
                ", " +
                getString(R.string.editor_info_window_flight_time, time == Double.POSITIVE_INFINITY
                    ? time
                    : String.format(Locale.US, "%1$02d:%2$02d", ((int) time / 60), ((int) time % 60)));

            infoView.setText(infoString);

            // Remove detail window if item is removed
            if (missionProxy.selection.getSelected().isEmpty() && itemDetailFragment != null) {
                removeItemDetail();
            }
        }
    }

    @Override
    public void onMapClick(LatLong point) {
        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onMapClick(point);
    }

    public EditorTools getTool() {
        return editorToolsFragment.getTool();
    }

    public EditorToolsImpl getToolImpl() {
        return editorToolsFragment.getToolImpl();
    }

    @Override
    public void editorToolChanged(EditorTools tools) {
        setupTool();
    }

    @Override
    public void enableGestureDetection(boolean enable) {
        if (gestureMapFragment == null)
            return;

        if (enable)
            gestureMapFragment.enableGestureDetection();
        else
            gestureMapFragment.disableGestureDetection();
    }

    private void setupTool() {
        final EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.setup();
        editorListFragment.enableDeleteMode(toolImpl.getEditorTools() == EditorTools.TRASH);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateLocationButtonsMargin(itemDetailFragment != null);
    }

    @Override
    protected void addToolbarFragment(){
        final int toolbarId = getToolbarId();
        editorListFragment = (EditorListFragment) fragmentManager.findFragmentById(toolbarId);
        if (editorListFragment == null) {
            editorListFragment = new EditorListFragment();
            fragmentManager.beginTransaction().add(toolbarId, editorListFragment).commit();
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
        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onPathFinished(points);
    }

    @Override
    public void onDetailDialogDismissed(List<MissionItemProxy> itemList) {
        if (missionProxy != null) missionProxy.selection.removeItemsFromSelection(itemList);
    }

    @Override
    public void onWaypointTypeChanged(MissionItemType newType, List<Pair<MissionItemProxy,
            List<MissionItemProxy>>> oldNewItemsList) {
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
            } else if (referenceType != proxyType
                    || MissionDetailFragment.typeWithNoMultiEditSupport.contains(referenceType)) {
                //Return a generic mission detail.
                return new MissionDetailFragment();
            }
        }

        return MissionDetailFragment.newInstance(referenceType);
    }

    @Override
    public void onItemClick(MissionItemProxy item, boolean zoomToFit) {
        if (missionProxy == null) return;

        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onListItemClick(item);

        if (zoomToFit) {
            zoomToFitSelected();
        }
    }

    @Override
    public void zoomToFitSelected() {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        List<MissionItemProxy> selected = missionProxy.selection.getSelected();
        if (selected.isEmpty()) {
            planningMapFragment.zoomToFit();
        } else {
            planningMapFragment.zoomToFit(MissionProxy.getVisibleCoords(selected));
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
        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onSelectionUpdate(selected);

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
    }

}
