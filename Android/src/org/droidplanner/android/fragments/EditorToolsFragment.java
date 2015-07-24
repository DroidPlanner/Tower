package org.droidplanner.android.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.spatial.BaseSpatialItem;

import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.SupportYesNoDialog;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.MissionSelection;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.proxy.mission.item.adapters.AdapterMissionItems;
import org.droidplanner.android.widgets.button.RadioButtonCenter;

import java.util.List;


/**
 * This fragment implements and displays the 'tools' used in the editor window
 * to switch between different type of waypoints creation.
 */
public class EditorToolsFragment extends ApiListenerFragment implements OnClickListener {

    /**
     * Used as key to retrieve the last selected tool from the bundle passed on
     * fragment creation.
     */
    private static final String STATE_SELECTED_TOOL = "selected_tool";

    public enum EditorTools {
        MARKER, DRAW, TRASH, SELECTOR, NONE;
    }

    public interface EditorToolListener {
        public void editorToolChanged(EditorTools tools);

        public void enableGestureDetection(boolean enable);

        public void skipMarkerClickEvents(boolean skip);

        public void zoomToFitSelected();
    }

    private static final IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.MISSION_RECEIVED);
        eventFilter.addAction(AttributeEvent.STATE_CONNECTED);
        eventFilter.addAction(AttributeEvent.STATE_DISCONNECTED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.MISSION_RECEIVED:
                    setTool(tool, false);
                    break;

                case AttributeEvent.STATE_CONNECTED:
                case AttributeEvent.STATE_DISCONNECTED:
                    updateDroneConnectedLogo();
                    break;
            }
        }
    };

    /**
     * The marker tool should be set by default.
     */
    private static final EditorTools DEFAULT_TOOL = EditorTools.MARKER;

    private final EditorToolsImpl[] editorToolsImpls = new EditorToolsImpl[EditorTools.values().length];

    {
        editorToolsImpls[EditorTools.MARKER.ordinal()] = new MarkerToolsImpl(this);
        editorToolsImpls[EditorTools.DRAW.ordinal()] = new DrawToolsImpl(this);
        editorToolsImpls[EditorTools.TRASH.ordinal()] = new TrashToolsImpl(this);
        editorToolsImpls[EditorTools.SELECTOR.ordinal()] = new SelectorToolsImpl(this);
        editorToolsImpls[EditorTools.NONE.ordinal()] = new NoneToolsImpl(this);
    }

    private ImageView droneConnectedLogo;

    private EditorToolListener listener;
    private RadioGroup mEditorRadioGroup;
    private EditorTools tool = DEFAULT_TOOL;
    private MissionProxy mMissionProxy;

    //Sub action views
    private View editorSubTools;
    private Spinner drawItemsSpinner;
    private Spinner markerItemsSpinner;

    private View clearSubOptions;
    private TextView clearMission;
    private TextView clearSelected;

    private TextView selectAll;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editor_tools, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            // Retrieve the tool that was last selected before the fragment was destroyed.
            final String toolName = savedInstanceState.getString(STATE_SELECTED_TOOL, tool.name());
            tool = EditorTools.valueOf(toolName);

            for (EditorToolsImpl toolImpl : editorToolsImpls)
                toolImpl.onRestoreInstanceState(savedInstanceState);
        }

        final Context context = getContext();

        droneConnectedLogo = (ImageView) view.findViewById(R.id.drone_connected_icon);

        mEditorRadioGroup = (RadioGroup) view.findViewById(R.id.editor_tools_layout);
        editorSubTools = view.findViewById(R.id.editor_sub_tools);

        final DrawToolsImpl drawToolImpl = (DrawToolsImpl) editorToolsImpls[EditorTools.DRAW.ordinal()];
        final RadioButtonCenter buttonDraw = (RadioButtonCenter) view.findViewById(R.id.editor_tools_draw);
        final AdapterMissionItems drawItemsAdapter = new AdapterMissionItems(context,
                R.layout.spinner_drop_down_mission_item, DrawToolsImpl.DRAW_ITEMS_TYPE);
        drawItemsSpinner = (Spinner) view.findViewById(R.id.draw_items_spinner);
        drawItemsSpinner.setAdapter(drawItemsAdapter);
        drawItemsSpinner.setSelection(drawItemsAdapter.getPosition(drawToolImpl.getSelected()));
        drawItemsSpinner.setOnItemSelectedListener(drawToolImpl);

        final MarkerToolsImpl markerToolImpl = (MarkerToolsImpl) editorToolsImpls[EditorTools.MARKER.ordinal()];
        final RadioButtonCenter buttonMarker = (RadioButtonCenter) view.findViewById(R.id.editor_tools_marker);
        final AdapterMissionItems markerItemsAdapter = new AdapterMissionItems(context,
                R.layout.spinner_drop_down_mission_item, MarkerToolsImpl.MARKER_ITEMS_TYPE);
        markerItemsSpinner = (Spinner) view.findViewById(R.id.marker_items_spinner);
        markerItemsSpinner.setAdapter(markerItemsAdapter);
        markerItemsSpinner.setSelection(markerItemsAdapter.getPosition(markerToolImpl.getSelected()));
        markerItemsSpinner.setOnItemSelectedListener(markerToolImpl);

        final RadioButtonCenter buttonTrash = (RadioButtonCenter) view.findViewById(R.id.editor_tools_trash);
        final TrashToolsImpl trashToolImpl = (TrashToolsImpl) editorToolsImpls[EditorTools.TRASH.ordinal()];

        clearSubOptions = view.findViewById(R.id.clear_sub_options);

        clearMission = (TextView) view.findViewById(R.id.clear_mission_button);
        clearMission.setOnClickListener(trashToolImpl);

        clearSelected = (TextView) view.findViewById(R.id.clear_selected_button);
        clearSelected.setOnClickListener(trashToolImpl);

        final RadioButtonCenter buttonSelector = (RadioButtonCenter) view.findViewById(R.id.editor_tools_selector);
        final SelectorToolsImpl selectorToolImpl = (SelectorToolsImpl) editorToolsImpls[EditorTools.SELECTOR.ordinal()];
        selectAll = (TextView) view.findViewById(R.id.select_all_button);
        selectAll.setOnClickListener(selectorToolImpl);

        for (View vv : new View[]{buttonDraw, buttonMarker, buttonTrash, buttonSelector}) {
            vv.setOnClickListener(this);
        }
    }

    private void updateDroneConnectedLogo() {
        if(droneConnectedLogo == null)
            return;

        final Drone drone = getDrone();
        if(drone == null || !drone.isConnected()){
            droneConnectedLogo.setImageResource(R.drawable.ic_navigation_grey_700_18dp);
        }
        else{
            droneConnectedLogo.setImageResource(R.drawable.ic_navigation_green_600_18dp);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof EditorToolListener)) {
            throw new IllegalStateException("Parent activity must be an instance of " + EditorToolListener.class
                    .getName());
        }

        listener = (EditorToolListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onApiConnected() {
        updateDroneConnectedLogo();

        mMissionProxy = getMissionProxy();
        setToolAndUpdateView(tool);
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);

        final ImageButton buttonUndo = (ImageButton) getView().findViewById(R.id.editor_tools_undo);
        buttonUndo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setTool(EditorTools.NONE);

                if (mMissionProxy.canUndoMission())
                    mMissionProxy.undoMission();
                else {
                    Toast.makeText(getContext(), "No operation left to undo.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (mMissionProxy != null) {
            for (EditorToolsImpl toolImpl : editorToolsImpls)
                toolImpl.setMissionProxy(mMissionProxy);
        }
    }

    @Override
    public void onApiDisconnected() {
        updateDroneConnectedLogo();
        getBroadcastManager().unregisterReceiver(eventReceiver);
        mMissionProxy = null;

        for (EditorToolsImpl toolImpl : editorToolsImpls)
            toolImpl.setMissionProxy(null);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Store the currently selected tool
        savedInstanceState.putString(STATE_SELECTED_TOOL, tool.name());

        for (EditorToolsImpl toolImpl : editorToolsImpls) {
            toolImpl.onSaveInstanceState(savedInstanceState);
        }
    }

    public EditorToolsImpl getToolImpl() {
        return editorToolsImpls[tool.ordinal()];
    }

    @Override
    public void onClick(View v) {
        EditorTools newTool = getToolForView(v.getId());
        if(this.tool == newTool)
            newTool = EditorTools.NONE;

        setTool(newTool);
    }

    private void hideSubTools() {
        if(editorSubTools != null)
            editorSubTools.setVisibility(View.GONE);

        if (selectAll != null)
            selectAll.setVisibility(View.GONE);

        if (clearSubOptions != null)
            clearSubOptions.setVisibility(View.GONE);

        if (markerItemsSpinner != null)
            markerItemsSpinner.setVisibility(View.GONE);

        if (drawItemsSpinner != null)
            drawItemsSpinner.setVisibility(View.GONE);
    }

    public EditorTools getTool() {
        return tool;
    }

    /**
     * Updates the selected tool.
     *
     * @param tool selected tool.
     */
    public void setTool(EditorTools tool) {
        setTool(tool, true);
    }

    /**
     * Updates the selected tool, and optionally notify listeners.
     *
     * @param tool            selected tool.
     * @param notifyListeners true to notify listeners, false otherwise.
     */
    private void setTool(EditorTools tool, boolean notifyListeners) {
        if (mMissionProxy != null && mMissionProxy.getItems().size() > 0
                && tool != EditorTools.TRASH
                && tool != EditorTools.SELECTOR
                && tool != EditorTools.NONE) {

            MissionItemProxy lastMissionItem = mMissionProxy.getItems().get(mMissionProxy.getItems().size() - 1);
            switch (lastMissionItem.getMissionItem().getType()) {
                case LAND:
                case RETURN_TO_LAUNCH:
                    tool = EditorTools.NONE;
                    mEditorRadioGroup.clearCheck();
                    Toast.makeText(getActivity(), getString(R.string.editor_err_land_rtl_added),
                            Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }

        this.tool = tool;
        if (tool == EditorTools.NONE) {
            mEditorRadioGroup.clearCheck();
        }

        updateSubToolsVisibility();

        if (listener != null && notifyListeners) {
            listener.editorToolChanged(this.tool);
        }
    }

    private void updateSubToolsVisibility(){
        hideSubTools();
        switch (tool) {
            case SELECTOR:
                editorSubTools.setVisibility(View.VISIBLE);
                selectAll.setVisibility(View.VISIBLE);
                break;

            case TRASH:
                editorSubTools.setVisibility(View.VISIBLE);
                clearSubOptions.setVisibility(View.VISIBLE);
                break;

            case DRAW:
                editorSubTools.setVisibility(View.VISIBLE);
                drawItemsSpinner.setVisibility(View.VISIBLE);
                break;

            case MARKER:
                editorSubTools.setVisibility(View.VISIBLE);
                markerItemsSpinner.setVisibility(View.VISIBLE);
                break;

            default:
                hideSubTools();
                break;
        }
    }

    /**
     * Updates the selected tool, and the view to match.
     *
     * @param tool selected tool.
     */
    public void setToolAndUpdateView(EditorTools tool) {
        setTool(tool, false);
        mEditorRadioGroup.check(getViewForTool(tool));
    }

    /**
     * Retrieves the tool matching the selected view.
     *
     * @param viewId id of the selected view.
     * @return matching tool
     */
    private EditorTools getToolForView(int viewId) {
        switch (viewId) {
            case R.id.editor_tools_marker:
                return EditorTools.MARKER;

            case R.id.editor_tools_draw:
                return EditorTools.DRAW;

            case R.id.editor_tools_trash:
                return EditorTools.TRASH;

            case R.id.editor_tools_selector:
                return EditorTools.SELECTOR;

            default:
                return EditorTools.NONE;
        }
    }

    /**
     * Retrieves the view matching the selected tool.
     *
     * @param tool selected tool
     * @return matching view id.
     */
    private int getViewForTool(EditorTools tool) {
        switch (tool) {
            case MARKER:
                return R.id.editor_tools_marker;

            case DRAW:
                return R.id.editor_tools_draw;

            case TRASH:
                return R.id.editor_tools_trash;

            case SELECTOR:
                return R.id.editor_tools_selector;

            case NONE:
            default:
                // Passing -1 to the radio group clear the selected radio button.
                return -1;
        }
    }

    public static abstract class EditorToolsImpl implements MissionSelection.OnSelectionUpdateListener {

        protected MissionProxy missionProxy;
        protected final EditorToolsFragment editorToolsFragment;

        EditorToolsImpl(EditorToolsFragment fragment) {
            this.editorToolsFragment = fragment;
        }

        void setMissionProxy(MissionProxy missionProxy) {
            this.missionProxy = missionProxy;
        }

        void onSaveInstanceState(Bundle outState) {
        }

        void onRestoreInstanceState(Bundle savedState) {
        }

        public void onMapClick(LatLong point) {
            if (missionProxy == null) return;

            // If an mission item is selected, unselect it.
            missionProxy.selection.clearSelection();
        }

        public void onListItemClick(MissionItemProxy item) {
            if (missionProxy == null)
                return;

            if (missionProxy.selection.selectionContains(item)) {
                missionProxy.selection.clearSelection();
            } else {
                editorToolsFragment.setTool(EditorTools.NONE);
                missionProxy.selection.setSelectionTo(item);
            }
        }

        public void onPathFinished(List<LatLong> path) {
        }

        @Override
        public void onSelectionUpdate(List<MissionItemProxy> selected){

        }

        public abstract EditorTools getEditorTools();

        public abstract void setup();

    }

    private static class MarkerToolsImpl extends EditorToolsImpl implements AdapterView.OnItemSelectedListener {

        private static final MissionItemType[] MARKER_ITEMS_TYPE = {
                MissionItemType.WAYPOINT,
                MissionItemType.SPLINE_WAYPOINT,
                MissionItemType.CIRCLE,
                MissionItemType.LAND,
                MissionItemType.REGION_OF_INTEREST,
                MissionItemType.STRUCTURE_SCANNER
        };

        private final static String EXTRA_SELECTED_MARKER_MISSION_ITEM_TYPE = "extra_selected_marker_mission_item_type";

        private MissionItemType selectedType = MARKER_ITEMS_TYPE[0];

        MarkerToolsImpl(EditorToolsFragment fragment) {
            super(fragment);
        }

        void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            if (selectedType != null)
                outState.putString(EXTRA_SELECTED_MARKER_MISSION_ITEM_TYPE, selectedType.name());
        }

        void onRestoreInstanceState(Bundle savedState) {
            super.onRestoreInstanceState(savedState);
            final String selectedTypeName = savedState.getString(EXTRA_SELECTED_MARKER_MISSION_ITEM_TYPE,
                    MARKER_ITEMS_TYPE[0].name());
            selectedType = MissionItemType.valueOf(selectedTypeName);
        }

        @Override
        public void onMapClick(LatLong point) {
            if (missionProxy == null) return;

            // If an mission item is selected, unselect it.
            missionProxy.selection.clearSelection();

            if (selectedType == null)
                return;

            BaseSpatialItem spatialItem = (BaseSpatialItem) selectedType.getNewItem();
            missionProxy.addSpatialWaypoint(spatialItem, point);
        }

        @Override
        public EditorTools getEditorTools() {
            return EditorTools.MARKER;
        }

        MissionItemType getSelected() {
            return selectedType;
        }

        @Override
        public void setup() {
            EditorToolListener listener = editorToolsFragment.listener;
            if (listener != null) {
                listener.enableGestureDetection(false);
                listener.skipMarkerClickEvents(true);
            }

            if (missionProxy != null)
                missionProxy.selection.clearSelection();
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedType = (MissionItemType) parent.getItemAtPosition(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            selectedType = MARKER_ITEMS_TYPE[0];
        }
    }

    private static class DrawToolsImpl extends EditorToolsImpl implements AdapterView.OnItemSelectedListener {

        private static final MissionItemType[] DRAW_ITEMS_TYPE = {
                MissionItemType.WAYPOINT,
                MissionItemType.SPLINE_WAYPOINT,
                MissionItemType.SURVEY,
                MissionItemType.SPLINE_SURVEY
        };

        private final static String EXTRA_SELECTED_DRAW_MISSION_ITEM_TYPE = "extra_selected_draww_mission_item_type";

        private MissionItemType selectedType = DRAW_ITEMS_TYPE[0];

        DrawToolsImpl(EditorToolsFragment fragment) {
            super(fragment);
        }

        void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            if (selectedType != null)
                outState.putString(EXTRA_SELECTED_DRAW_MISSION_ITEM_TYPE, selectedType.name());
        }

        void onRestoreInstanceState(Bundle savedState) {
            super.onRestoreInstanceState(savedState);
            final String selectedTypeName = savedState.getString(EXTRA_SELECTED_DRAW_MISSION_ITEM_TYPE,
                    DRAW_ITEMS_TYPE[0].name());
            selectedType = MissionItemType.valueOf(selectedTypeName);
        }

        @Override
        public EditorTools getEditorTools() {
            return EditorTools.DRAW;
        }

        @Override
        public void setup() {
            EditorToolListener listener = editorToolsFragment.listener;
            if (listener != null) {
                listener.enableGestureDetection(true);
                listener.skipMarkerClickEvents(false);
            }

            if (missionProxy != null)
                missionProxy.selection.clearSelection();

            if (selectedType == MissionItemType.SURVEY) {
                Toast.makeText(editorToolsFragment.getContext(), R.string.draw_the_survey_region, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onPathFinished(List<LatLong> points) {
            if (missionProxy != null) {
                switch (selectedType) {
                    case WAYPOINT:
                    default:
                        missionProxy.addWaypoints(points);
                        break;

                    case SPLINE_WAYPOINT:
                        missionProxy.addSplineWaypoints(points);
                        break;

                    case SURVEY:
                        if (points.size() > 2) {
                            missionProxy.addSurveyPolygon(points, false);
                        } else {
                            editorToolsFragment.setTool(EditorTools.DRAW);
                            return;
                        }
                        break;

                    case SPLINE_SURVEY:
                        if (points.size() > 2) {
                            missionProxy.addSurveyPolygon(points, true);
                        } else {
                            editorToolsFragment.setTool(EditorTools.DRAW);
                            return;
                        }
                        break;
                }
            }
            editorToolsFragment.setTool(EditorTools.NONE);
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            selectedType = (MissionItemType) parent.getItemAtPosition(position);
            if (selectedType == MissionItemType.SURVEY || selectedType == MissionItemType.SPLINE_SURVEY) {
                Toast.makeText(editorToolsFragment.getContext(), R.string.draw_the_survey_region, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            selectedType = DRAW_ITEMS_TYPE[0];
        }

        MissionItemType getSelected() {
            return selectedType;
        }
    }

    private static class NoneToolsImpl extends EditorToolsImpl {

        NoneToolsImpl(EditorToolsFragment fragment) {
            super(fragment);
        }

        @Override
        public EditorTools getEditorTools() {
            return EditorTools.NONE;
        }

        @Override
        public void setup() {
            EditorToolListener listener = editorToolsFragment.listener;
            if (listener != null) {
                listener.enableGestureDetection(false);
                listener.skipMarkerClickEvents(false);
            }
        }
    }

    private static class TrashToolsImpl extends EditorToolsImpl implements OnClickListener {

        TrashToolsImpl(EditorToolsFragment fragment) {
            super(fragment);
        }

        @Override
        public void onListItemClick(MissionItemProxy item) {
            if (missionProxy == null)
                return;

            missionProxy.removeItem(item);
            missionProxy.selection.clearSelection();

            if (missionProxy.getItems().size() <= 0) {
                editorToolsFragment.setTool(EditorTools.NONE);
            }
        }

        @Override
        public void onSelectionUpdate(List<MissionItemProxy> selected){
            super.onSelectionUpdate(selected);
            editorToolsFragment.clearSelected.setEnabled(!selected.isEmpty());
        }

        @Override
        public EditorTools getEditorTools() {
            return EditorTools.TRASH;
        }

        @Override
        public void setup() {
            EditorToolListener listener = editorToolsFragment.listener;
            if (listener != null) {
                listener.enableGestureDetection(false);
                listener.skipMarkerClickEvents(false);
            }

            if (missionProxy != null) {
                List<MissionItemProxy> selected = missionProxy.selection.getSelected();
                editorToolsFragment.clearSelected.setEnabled(!selected.isEmpty());

                final List<MissionItemProxy> missionItems = missionProxy.getItems();
                editorToolsFragment.clearMission.setEnabled(!missionItems.isEmpty());
            }
        }

        private void doClearMissionConfirmation() {
            if(missionProxy == null || missionProxy.getItems().isEmpty())
                return;

            final Context context = editorToolsFragment.getContext();
            SupportYesNoDialog ynd = SupportYesNoDialog.newInstance(context, context.getString(R.string
                            .dlg_clear_mission_title),
                    context.getString(R.string.dlg_clear_mission_confirm), new SupportYesNoDialog.Listener() {
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
                ynd.show(editorToolsFragment.getChildFragmentManager(), "clearMission");
            }
        }

        private void deleteSelectedItems() {
            final Context context = editorToolsFragment.getContext();
            SupportYesNoDialog ynd = SupportYesNoDialog.newInstance(context,
                    context.getString(R.string.delete_selected_waypoints_title),
                    context.getString(R.string.delete_selected_waypoints_confirm), new SupportYesNoDialog.Listener() {
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
                            if (missionProxy != null)
                                missionProxy.selection.clearSelection();
                        }
                    });

            if (ynd != null) {
                ynd.show(editorToolsFragment.getChildFragmentManager(), "clearSelectedWaypoints");
            }
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.clear_mission_button:
                    doClearMissionConfirmation();
                    break;

                case R.id.clear_selected_button:
                    deleteSelectedItems();
                    break;
            }
        }
    }

    private static class SelectorToolsImpl extends EditorToolsImpl implements OnClickListener {

        SelectorToolsImpl(EditorToolsFragment fragment) {
            super(fragment);
        }

        @Override
        public void onListItemClick(MissionItemProxy item) {
            if (missionProxy == null)
                return;

            if (missionProxy.selection.selectionContains(item)) {
                missionProxy.selection.removeItemFromSelection(item);
            } else {
                missionProxy.selection.addToSelection(item);
            }
        }

        private void selectAll() {
            if (missionProxy == null)
                return;

            missionProxy.selection.setSelectionTo(missionProxy.getItems());
            EditorToolListener listener = editorToolsFragment.listener;
            if (listener != null)
                listener.zoomToFitSelected();
        }

        @Override
        public EditorTools getEditorTools() {
            return EditorTools.SELECTOR;
        }

        @Override
        public void setup() {
            EditorToolListener listener = editorToolsFragment.listener;
            if (listener != null) {
                listener.enableGestureDetection(false);
                listener.skipMarkerClickEvents(false);
            }

            Toast.makeText(editorToolsFragment.getContext(), "Click on mission items to select them.",
                    Toast.LENGTH_SHORT).show();

            if (missionProxy != null) {
                missionProxy.selection.clearSelection();
                final List<MissionItemProxy> missionItems = missionProxy.getItems();
                editorToolsFragment.selectAll.setEnabled(!missionItems.isEmpty());
            }
        }

        @Override
        public void onClick(View v) {
            selectAll();
        }
    }
}
