package org.droidplanner.android.fragments.account.editor.tool;

import android.view.View;
import android.widget.Toast;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.spatial.BaseSpatialItem;

/**
 * Created by Jeon-Sunghwan on 5/10/21.
 */
class CoordinateToolsImpl extends EditorToolsImpl implements View.OnClickListener {

    static final MissionItemType[] MARKER_ITEMS_TYPE = {
            MissionItemType.WAYPOINT
            //, MissionItemType.SPLINE_WAYPOINT,
            //MissionItemType.CIRCLE,
            //  MissionItemType.LAND,
            //  MissionItemType.REGION_OF_INTEREST,
            // MissionItemType.STRUCTURE_SCANNER
    };


    private final static String EXTRA_SELECTED_MARKER_MISSION_ITEM_TYPE = "extra_selected_marker_mission_item_type";

    private MissionItemType selectedType = MARKER_ITEMS_TYPE[0];//선택한 타입의 디폴트 값을 웨이포인트

    CoordinateToolsImpl(EditorToolsFragment fragment) {
        super(fragment);
    }


    @Override
    public EditorToolsFragment.EditorTools getEditorTools() {
        return EditorToolsFragment.EditorTools.COORDINATE;
    }


    @Override
    public void setup() {
        EditorToolsFragment.EditorToolListener listener = editorToolsFragment.listener;
        if (listener != null) {
            listener.enableGestureDetection(false);
        }
        Toast.makeText(editorToolsFragment.getContext(), "Give me a Destination Coordinates",
                Toast.LENGTH_SHORT).show();
        if (missionProxy != null)//미션이 있으면
            missionProxy.selection.clearSelection();
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(editorToolsFragment.getContext(), "click",
                Toast.LENGTH_SHORT).show();

        // If an mission item is selected, unselect it.
        missionProxy.selection.clearSelection(); // real?

        double latValue = Double.parseDouble(editorToolsFragment.lat.getText().toString());
        double lotValue = Double.parseDouble(editorToolsFragment.lon.getText().toString());
        LatLong text_coordinate = new LatLong(latValue, lotValue);
        BaseSpatialItem spatialItem = (BaseSpatialItem) selectedType.getNewItem();
        missionProxy.addSpatialWaypoint(spatialItem, text_coordinate);
    }


}
