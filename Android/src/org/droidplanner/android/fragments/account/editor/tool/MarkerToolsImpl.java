package org.droidplanner.android.fragments.account.editor.tool;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.spatial.BaseSpatialItem;

/**
 * Created by Fredia Huya-Kouadio on 8/25/15.
 */
class MarkerToolsImpl extends EditorToolsImpl implements AdapterView.OnItemSelectedListener {

    static final MissionItemType[] MARKER_ITEMS_TYPE = {
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
    public EditorToolsFragment.EditorTools getEditorTools() {
        return EditorToolsFragment.EditorTools.MARKER;
    }

    MissionItemType getSelected() {
        return selectedType;
    }

    @Override
    public void setup() {
        EditorToolsFragment.EditorToolListener listener = editorToolsFragment.listener;
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
