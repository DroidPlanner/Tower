package org.droidplanner.android.fragments.account.editor.tool;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;

import org.droidplanner.android.R;

import java.util.List;

/**
 * Created by Fredia Huya-Kouadio on 8/25/15.
 */
class DrawToolsImpl extends EditorToolsImpl implements AdapterView.OnItemSelectedListener {

    static final MissionItemType[] DRAW_ITEMS_TYPE = {
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
    public EditorToolsFragment.EditorTools getEditorTools() {
        return EditorToolsFragment.EditorTools.DRAW;
    }

    @Override
    public void setup() {
        EditorToolsFragment.EditorToolListener listener = editorToolsFragment.listener;
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
                        editorToolsFragment.setTool(EditorToolsFragment.EditorTools.DRAW);
                        return;
                    }
                    break;

                case SPLINE_SURVEY:
                    if (points.size() > 2) {
                        missionProxy.addSurveyPolygon(points, true);
                    } else {
                        editorToolsFragment.setTool(EditorToolsFragment.EditorTools.DRAW);
                        return;
                    }
                    break;
            }
        }
        editorToolsFragment.setTool(EditorToolsFragment.EditorTools.NONE);
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
