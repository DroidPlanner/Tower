package co.aerobotics.android.fragments.account.editor.tool;

import com.o3dr.services.android.lib.coordinate.LatLong;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aerobotics on 2017/05/09.
 */

public class CreateBoundaryToolsImpl extends EditorToolsImpl {
    private Map<String, List<LatLong>> nameToPolygon = new HashMap<String, List<LatLong>>();
    private ArrayList<String> polygonNames;
    private ArrayList<String> selectedItems = new ArrayList<>();
    private List<LatLong> points;

    CreateBoundaryToolsImpl(EditorToolsFragment fragment, ArrayList<String> selectedItems){
        super(fragment);
        this.selectedItems = selectedItems;
    }

    @Override
    public void setup(){}

    @Override
    public EditorToolsFragment.EditorTools getEditorTools() {
        return null;
    }

    public void createSurvey(){
        List<LatLong> poly = nameToPolygon.get(selectedItems.get(0));
        missionProxy.addSurveyPolygon(poly, false);
    }

    private JSONObject readJsonFile(String fileName){
        JSONObject jsonObj;
        try {
            FileInputStream fis = editorToolsFragment.getActivity().openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            String jsonString = sb.toString();
            try {
                jsonObj = new JSONObject(jsonString);
            }
            catch (JSONException jsonException){
                return null;
            }

            return jsonObj;
        } catch (FileNotFoundException fileNotFound) {
            return null;
        } catch (IOException ioException) {
            return null;
        }
    }

    public void getPolygons(){
        points = new ArrayList<LatLong>();
        polygonNames = new ArrayList<String>();
        JSONArray arr = null;
        JSONObject response = readJsonFile("orchards.json");
        try {
            arr = response.getJSONArray("orchards");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject jsonObject = arr.getJSONObject(i);
                String name = jsonObject.getString("name");
                polygonNames.add(name);
                String polygon = jsonObject.getString("polygon");
                String[] latLongPairs = polygon.split(" ");
                points = (convertToLatLongList(latLongPairs));
                nameToPolygon.put(name, points);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<LatLong> convertToLatLongList(String[] points){
        List<LatLong> path = new ArrayList<LatLong>();
        for (int i=0; i<points.length; i++ ){
            String[] point = points[i].split(",");
            path.add(new LatLong(Double.parseDouble(point[1]), Double.parseDouble(point[0])));
        }
        return path;
    }
}
