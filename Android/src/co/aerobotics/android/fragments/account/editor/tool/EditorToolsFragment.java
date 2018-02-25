package co.aerobotics.android.fragments.account.editor.tool;

import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.data.QuickHullLatLng;
import co.aerobotics.android.fragments.helpers.ApiListenerFragment;
import co.aerobotics.android.data.AeroviewPolygons;
import co.aerobotics.android.graphic.map.PolygonData;
import co.aerobotics.android.maps.GoogleMapFragment;
import co.aerobotics.android.proxy.mission.item.MissionItemProxy;
import co.aerobotics.android.view.button.RadioButtonCenter;
import com.google.android.gms.maps.model.LatLng;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;

import co.aerobotics.android.R;
import co.aerobotics.android.dialogs.SupportYesNoDialog;
import co.aerobotics.android.proxy.mission.MissionProxy;
import co.aerobotics.android.proxy.mission.item.adapters.AdapterMissionItems;

import java.util.ArrayList;
import java.util.List;


/**
 * This fragment implements and displays the 'tools' used in the editor window
 * to switch between different type of waypoints creation.
 */
public class EditorToolsFragment extends ApiListenerFragment implements OnClickListener, SupportYesNoDialog.Listener, SelectBoundaryFragment.OnItemSelectedListener {

    /**
     * Used as key to retrieve the last selected tool from the bundle passed on
     * fragment creation.
     */
    private static final String STATE_SELECTED_TOOL = "selected_tool";

    public enum EditorTools {
        MARKER, DRAW, TRASH, IMPORT, SELECTOR, NONE,
    }

    public interface EditorToolListener {
        void editorToolChanged(EditorTools tools);

        void enableGestureDetection(boolean enable);

        void zoomToFitSelected();
    }

    private static final IntentFilter eventFilter = new IntentFilter();

    static {
        eventFilter.addAction(AttributeEvent.MISSION_RECEIVED);
        eventFilter.addAction(GoogleMapFragment.ACTION_POLYGON_CLICKED);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AttributeEvent.MISSION_RECEIVED:
                    setTool(EditorTools.NONE);
                    break;
                case GoogleMapFragment.ACTION_POLYGON_CLICKED:
                    setTool(EditorTools.IMPORT);
                    break;

            }
        }
    };

    /**
     * The marker tool should be set by default.
     */
    private static final EditorTools DEFAULT_TOOL = EditorTools.NONE;

    private final EditorToolsImpl[] editorToolsImpls = new EditorToolsImpl[EditorTools.values().length];

    {
        editorToolsImpls[EditorTools.MARKER.ordinal()] = new MarkerToolsImpl(this);
        editorToolsImpls[EditorTools.DRAW.ordinal()] = new DrawToolsImpl(this);
        editorToolsImpls[EditorTools.TRASH.ordinal()] = new TrashToolsImpl(this);
        editorToolsImpls[EditorTools.IMPORT.ordinal()] = new ImportToolsImpl(this);
        editorToolsImpls[EditorTools.SELECTOR.ordinal()] = new SelectorToolsImpl(this);
        editorToolsImpls[EditorTools.NONE.ordinal()] = new NoneToolsImpl(this);
    }

    EditorToolListener listener;
    private RadioGroup mEditorRadioGroup;
    private EditorTools tool = DEFAULT_TOOL;
    private MissionProxy mMissionProxy;

    //Sub action views
    private View editorSubTools;
    private Spinner drawItemsSpinner;
    private Spinner markerItemsSpinner;

    private View clearSubOptions;
    TextView clearMission;
    TextView clearSelected;

    TextView selectAll;

    private View polygonSubOptions;
    TextView importPolygon;
    TextView buildMission;

    FragmentManager ft;
    private ImportToolsImpl importTools;
    private MixpanelAPI mMixpanel;

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

        final AdapterMissionItems markerItemsAdapter = new AdapterMissionItems(context,
                R.layout.spinner_drop_down_mission_item, MarkerToolsImpl.MARKER_ITEMS_TYPE);
        markerItemsSpinner = (Spinner) view.findViewById(R.id.marker_items_spinner);
        markerItemsSpinner.setAdapter(markerItemsAdapter);
        markerItemsSpinner.setSelection(markerItemsAdapter.getPosition(markerToolImpl.getSelected()));
        markerItemsSpinner.setOnItemSelectedListener(markerToolImpl);
        markerItemsSpinner.setVisibility(View.GONE);

        final RadioButtonCenter buttonTrash = (RadioButtonCenter) view.findViewById(R.id.editor_tools_trash);
        final TrashToolsImpl trashToolImpl = (TrashToolsImpl) editorToolsImpls[EditorTools.TRASH.ordinal()];

        clearSubOptions = view.findViewById(R.id.clear_sub_options);

        clearMission = (TextView) view.findViewById(R.id.clear_mission_button);
        clearMission.setOnClickListener(trashToolImpl);

        clearSelected = (TextView) view.findViewById(R.id.clear_selected_button);
        clearSelected.setOnClickListener(trashToolImpl);



        final RadioButtonCenter buttonImport = (RadioButtonCenter) view.findViewById(R.id.editor_tools_import);
        final ImportToolsImpl importToolsImpl = (ImportToolsImpl) editorToolsImpls[EditorTools.IMPORT.ordinal()];


        ft  = getFragmentManager();
        mMixpanel = MixpanelAPI.getInstance(this.getActivity(), DroidPlannerApp.getInstance().getMixpanelToken());

        for (View vv : new View[]{buttonDraw, buttonTrash, buttonImport}) {
            vv.setOnClickListener(this);
        }
    }

    @Override
    public void onItemSelected(SelectBoundaryFragment fragment, ArrayList<String> selectedItems) {
        //List of selected boundary names
        if (selectedItems != null && selectedItems.size() > 0){
            //CreateBoundaryToolsImpl createBoundaryTools = new CreateBoundaryToolsImpl(this, selectedItems);
            //mMissionProxy = getMissionProxy();
            //importTools.createSurvey(selectedItems);
            listener.zoomToFitSelected();
        }
        else{
            Toast.makeText(getContext(), R.string.no_items_selected, Toast.LENGTH_SHORT).show();
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
        mMissionProxy = getMissionProxy();
        setToolAndUpdateView(tool);
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);


        if (mMissionProxy != null) {
            for (EditorToolsImpl toolImpl : editorToolsImpls)
                toolImpl.setMissionProxy(mMissionProxy);

        }
    }

    @Override
    public void onApiDisconnected() {
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
        if (this.tool == newTool)
            newTool = EditorTools.NONE;

        if(newTool == EditorTools.IMPORT){
            mMixpanel.track("FPA: TapGenerateSurvey");
            if (DroidPlannerApp.getInstance().getSelectedPolygons().size() > 1) {
                createMergedConvexSurvey();
            } else{
                createSurvey();
            }
            mEditorRadioGroup.clearCheck();
        } else {
            setTool(newTool);
        }
    }

    private void hideSubTools() {
        if (editorSubTools != null)
            editorSubTools.setVisibility(View.INVISIBLE);

        if (selectAll != null)
            selectAll.setVisibility(View.GONE);

        if (clearSubOptions != null)
            clearSubOptions.setVisibility(View.GONE);

        if (markerItemsSpinner != null)
            markerItemsSpinner.setVisibility(View.GONE);

        if (drawItemsSpinner != null)
            drawItemsSpinner.setVisibility(View.GONE);

        if (importPolygon != null)
            polygonSubOptions.setVisibility(View.GONE);
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
                && tool != EditorTools.NONE
                && tool != EditorTools.IMPORT
                ) {

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
        if (tool == EditorTools.NONE || tool == EditorTools.IMPORT) {
            mEditorRadioGroup.clearCheck();
        }

        updateSubToolsVisibility();

        if (listener != null && notifyListeners) {
            listener.editorToolChanged(this.tool);
        }
    }

    private void updateSubToolsVisibility() {
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
                //drawItemsSpinner.setVisibility(View.VISIBLE);
                break;

            case MARKER:
                editorSubTools.setVisibility(View.VISIBLE);
                markerItemsSpinner.setVisibility(View.GONE);
                break;
            case IMPORT:
                //editorSubTools.setVisibility(View.VISIBLE);
                //polygonSubOptions.setVisibility(View.VISIBLE);
                //importPolygon.setVisibility(View.VISIBLE);
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

            case R.id.editor_tools_import:
                return EditorTools.IMPORT;

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

            case IMPORT:
                return R.id.editor_tools_import;

            case NONE:
            default:
                // Passing -1 to the radio group clear the selected radio button.
                return -1;
        }
    }

    private void createSurvey(){
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

                getMissionProxy().addAeroViewSurveyPolygon(dronekitPoly, id);
            }
            DroidPlannerApp.getInstance().getSelectedPolygons().clear();
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(AeroviewPolygons.ACTION_POLYGON_UPDATE));

        } else{
            setResultToToast("First click on a AeroView survey boundary");
        }

    }

    private void createMergedConvexSurvey(){
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
            getMissionProxy().addSurveyPolygon(dronekitPoly, false);
            DroidPlannerApp.getInstance().getSelectedPolygons().clear();
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(AeroviewPolygons.ACTION_POLYGON_UPDATE));

        } else{
            setResultToToast("First click on a AeroView survey boundary");
        }
    }

    private void setResultToToast(final String string) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), string, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDialogYes(String dialogTag) {
        getToolImpl().onDialogYes(dialogTag);
    }

    @Override
    public void onDialogNo(String dialogTag) {
        getToolImpl().onDialogNo(dialogTag);
    }

}
