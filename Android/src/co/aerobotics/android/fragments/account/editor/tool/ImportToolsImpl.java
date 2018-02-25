package co.aerobotics.android.fragments.account.editor.tool;


import android.view.View;
import android.widget.Toast;

import co.aerobotics.android.DroidPlannerApp;
import com.goebl.simplify.PointExtractor;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by aerobotics on 2017/05/05.
 */

class ImportToolsImpl extends EditorToolsImpl implements View.OnClickListener {

    public static final int DIALOG_FRAGMENT = 1;
    private ArrayList<String> polygonNames;
    private static final String TAG = "ImportToolsImpl";

    private static PointExtractor<LatLng> latLngPointExtractor = new PointExtractor<LatLng>() {
        @Override
        public double getX(LatLng point) {
            return point.latitude * 1000000;
        }

        @Override
        public double getY(LatLng point) {
            return point.longitude * 1000000;
        }
    };

    ImportToolsImpl(EditorToolsFragment fragment){
        super(fragment);
    }

    @Override
    public void setup(){
        EditorToolsFragment.EditorToolListener listener = editorToolsFragment.listener;

        if (listener != null) {
            listener.enableGestureDetection(false);
        }



        editorToolsFragment.importPolygon.setEnabled(true);
        editorToolsFragment.buildMission.setEnabled(true);
    }
    private void setResultToToast(final String string) {
        editorToolsFragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(editorToolsFragment.getActivity(), string, Toast.LENGTH_LONG).show();
            }
        });
    }
/*
    private void createSurvey(){
        List<String> selectedPolygons = DroidPlannerApp.getInstance().getSelectedPolygons();
        if (!DroidPlannerApp.getInstance().getSelectedPolygons().isEmpty()) {

            for (String name : selectedPolygons) {
                PolygonData polygonData = DroidPlannerApp.getInstance().polygonMap.get(name);

                polygonData.setSelected(false);
                DroidPlannerApp.getInstance().polygonMap.put(name, polygonData);

                List<LatLng> mapsPolygon = polygonData.getPoints();

                List<LatLong> dronekitPoly = new ArrayList<>();
                for (LatLng point : mapsPolygon) {
                    dronekitPoly.add(new LatLong(point.latitude, point.longitude));
                }

                missionProxy.addAeroViewSurveyPolygon(dronekitPoly, polygonData.getName());
            }
            DroidPlannerApp.getInstance().getSelectedPolygons().clear();
            LocalBroadcastManager.getInstance(editorToolsFragment.getActivity()).sendBroadcast(new Intent(AeroviewPolygons.ACTION_POLYGON_UPDATE));

        } else{
            setResultToToast("No Survey Boundaries Selected");
        }

    }

    private void createMergedConvexSurvey(){
        List<String> selectedPolygons = DroidPlannerApp.getInstance().getSelectedPolygons();
        if (!DroidPlannerApp.getInstance().getSelectedPolygons().isEmpty()) {
            List<LatLng> allPoints = new ArrayList<LatLng>();
            for (String name : selectedPolygons) {
                PolygonData polygonData = DroidPlannerApp.getInstance().polygonMap.get(name);
                List<LatLng> mapsPolygon = polygonData.getPoints();
                for (LatLng point : mapsPolygon) {
                    allPoints.add(point);
                }
                polygonData.setSelected(false);
                DroidPlannerApp.getInstance().polygonMap.put(name,polygonData);
            }

            List<LatLng> mergedPoints = new QuickHullLatLng().quickHull(allPoints);
            List<LatLong> dronekitPoly = new ArrayList<>();
            for (LatLng point : mergedPoints) {
                dronekitPoly.add(new LatLong(point.latitude, point.longitude));
            }
            missionProxy.addSurveyPolygon(dronekitPoly, false);
            DroidPlannerApp.getInstance().getSelectedPolygons().clear();
            LocalBroadcastManager.getInstance(editorToolsFragment.getActivity()).sendBroadcast(new Intent(AeroviewPolygons.ACTION_POLYGON_UPDATE));

        } else{
            setResultToToast("No Survey Boundaries Selected");
        }
    }

*/
    @Override
    public EditorToolsFragment.EditorTools getEditorTools() {
        return EditorToolsFragment.EditorTools.IMPORT;
    }



    @Override
    public void onClick(View v) {
        if (DroidPlannerApp.getInstance().getSelectedPolygons().size() > 1) {
            //createMergedConvexSurvey();
        } else{
            //createSurvey();
        }
        /*switch (v.getId()) {
            case R.id.import_polygon_button:
                new AeroviewPolygons(editorToolsFragment.getActivity()).executeClientDataTask();
                DroidPlannerApp.getInstance().selectedPolygons.clear();
                break;
            case R.id.build_mission_button:
                if (DroidPlannerApp.getInstance().getSelectedPolygons().size() > 1) {
                    createMergedConvexSurvey();
                } else{
                    createSurvey();
                }
                break;

        }*/
    }
}
