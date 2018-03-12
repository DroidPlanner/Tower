package co.aerobotics.android.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;

import co.aerobotics.android.activities.interfaces.APIContract;
import co.aerobotics.android.graphic.map.PolygonData;

import com.goebl.simplify.PointExtractor;
import com.goebl.simplify.Simplify;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by michaelwootton on 8/2/17.
 */

public class AeroviewPolygons {

    private Context context;
    private Map<String, String> mapNameToID = new HashMap<>();
    private RetrieveClientDataTask mAuthTask = null;
    public static final String ACTION_POLYGON_UPDATE = "update_polygon";
    public static final String ACTION_ERROR_MSG = "server_error";
    public static final String SYNC_COMPLETE = "sync_successful";
    private Handler handler = new Handler(Looper.getMainLooper());
    private SQLiteDatabaseHandler sqLiteDatabaseHandler;
    private OnSyncFinishedListener onSyncFinishedListener;


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

    public AeroviewPolygons(Context context) {
        this.context = context;
        sqLiteDatabaseHandler = new SQLiteDatabaseHandler(context);

    }

    /**
     * Extract client boundary info from json received from AeroView and then add new boundaries to the
     * local database
     */
    public void addNewBoundariesFromAeroView(){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        String jsonString = sharedPref.getString(context.getResources().getString(R.string.json_data), "");

        try {
            JSONObject json = new JSONObject(jsonString);

            JSONArray arr = json.getJSONArray("boundary_data");
            List<BoundaryDetail> boundariesList = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject polyDict = arr.getJSONObject(i);
                String name = polyDict.getString("name");
                String polygon = polyDict.getString("polygon");
                String id = polyDict.getString("id");
                int clientId = sharedPref.getInt(context.getResources().getString(R.string.client_id), -1);
                if (!polygon.equals("")) {
                    boundariesList.add(new BoundaryDetail(name,id, polygon, clientId , true));
                }
            }
            if(!boundariesList.isEmpty()){
                addBoundaryDetailsToDB(boundariesList);
            }
        }  catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addCropTypesToDB(){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        String jsonString = sharedPref.getString(context.getResources().getString(R.string.json_data), "");
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray array = json.getJSONArray("crop_types");
            for (int i = 0; i < array.length(); i++){
                JSONObject dict = array.getJSONObject(i);
                sqLiteDatabaseHandler.createCropType(dict.getString("name"), dict.getInt("id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addFarmNamesToDB(){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        String jsonString = sharedPref.getString(context.getResources().getString(R.string.json_data), "");
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray array = json.getJSONArray("farms");
            for (int i = 0; i < array.length(); i++){
                JSONObject dict = array.getJSONObject(i);
                sqLiteDatabaseHandler.createFarmName(dict.getString("name"), dict.getInt("id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateFarmNamesInDB(){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        String jsonString = sharedPref.getString(context.getResources().getString(R.string.json_data), "");
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray array = json.getJSONArray("farms");
            for (int i = 0; i < array.length(); i++){
                JSONObject dict = array.getJSONObject(i);
                sqLiteDatabaseHandler.updateFarmNameId(dict.getString("name"), dict.getInt("id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateCropTypesInDB(){

        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        String jsonString = sharedPref.getString(context.getResources().getString(R.string.json_data), "");
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray array = json.getJSONArray("crop_types");
            for (int i = 0; i < array.length(); i++){
                JSONObject dict = array.getJSONObject(i);
                sqLiteDatabaseHandler.updateCropTypes(dict.getString("name"), dict.getInt("id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds new boundary details to the local database
     */
    public void addBoundaryDetailsToDB(List<BoundaryDetail> boundaryDetailList){
        SQLiteDatabaseHandler dbHandler = new SQLiteDatabaseHandler(context);
        dbHandler.addBoundaryDetailList(boundaryDetailList);
    }

    public void addPolygonsToMap(){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        List<BoundaryDetail> boundaryDetails = sqLiteDatabaseHandler.getAllBoundaryDetail(sharedPref.getInt(context.getResources().getString(R.string.client_id), -1));
        DroidPlannerApp.getInstance().polygonMap.clear();
        for (BoundaryDetail boundaryDetail : boundaryDetails) {
            if (boundaryDetail.isDisplay()) {
                PolygonData polygonData = new PolygonData(boundaryDetail.getName(), convertStringToLatLngList(boundaryDetail.getPoints()), false, boundaryDetail.getBoundaryId());
                DroidPlannerApp.getInstance().polygonMap.put(boundaryDetail.getBoundaryId(), polygonData);
            }
        }
        Intent intent = new Intent(ACTION_POLYGON_UPDATE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private List<LatLng> convertStringToLatLngList(String polygon){
        String[] latLongPairs = polygon.split(" ");
        LatLng [] points = (convertToLatLongList(latLongPairs));
        Simplify<LatLng> simplify = new Simplify<LatLng>(new LatLng[0], latLngPointExtractor);
        LatLng[] simplified = simplify.simplify(points, 80f, false);
        return new ArrayList<>(Arrays.asList(simplified));
    }


    public void executeAeroViewSync(){
        List<String> requests = sqLiteDatabaseHandler.getAllRequests();
        PostOfflineDataTask mPostTask = new PostOfflineDataTask(requests);
        mPostTask.execute((Void) null);
    }

    /**
     * Converts String array of coordinates to list of Google LatLng type
     * @param points String array
     * @return List<LatLng>
     */

    private LatLng[] convertToLatLongList(String[] points){

        List<LatLng> path = new ArrayList<LatLng>();
        LatLng [] pointsList;
        for (int i=0; i<points.length; i++ ){
            String[] point = points[i].split(",");
            path.add(new LatLng(Double.parseDouble(point[1]), Double.parseDouble(point[0])));
        }
        pointsList = path.toArray(new LatLng[path.size()]);
        return pointsList;
    }

    public void executeClientDataTask(){
        if (mAuthTask != null){
            return;
        }
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        String username = sharedPref.getString(context.getResources().getString(R.string.username), "");
        String password = sharedPref.getString(context.getResources().getString(R.string.password), "");
        mAuthTask = new RetrieveClientDataTask(username, password);
        mAuthTask.execute((Void) null);
    }

    public interface OnSyncFinishedListener{
        void onSyncFinished();
    }

    public void setOnSyncFinishedListener(OnSyncFinishedListener listener){
        onSyncFinishedListener = listener;
    }
    /**
     * Async Task to request client data from AeroView server.
     */
    public class RetrieveClientDataTask extends AsyncTask<Void, Void, Boolean> implements APIContract{

        String mUsername;
        String mPassword;

        RetrieveClientDataTask(String mUsername, String mPassword){
            this.mPassword = mPassword;
            this.mUsername = mUsername;
        }

        /**
         * On successful get from server the data is stored in shared prefs.
         * @param params
         * @return boolean
         */
        @Override
        protected Boolean doInBackground(Void... params) {

            String jsonStr = String.format("{\"email\":\"%s\",\"password\":\"%s\", \"app\":\"flight\"}",mUsername,mPassword);
            Log.d("JsonStr", jsonStr);

            PostRequest postRequest = new PostRequest();
            postRequest.post(jsonStr, APIContract.USER_DETAILS_URL);

            do {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!postRequest.isServerResponseReceived());

            if (postRequest.isServerError()){
                //TODO handle error message better
                return false;
            }
            try {
                int rc = postRequest.getResponseData().getInt("rc");
                if (rc == 0){
                    SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(context.getResources().getString(R.string.username), mUsername);
                    editor.putString(context.getResources().getString(R.string.password), mPassword);
                    editor.putString(context.getResources().getString(R.string.json_data), postRequest.getResponseData().toString());
                    editor.apply();
                    return true;
                } else{
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){

                addNewBoundariesFromAeroView();
                //addPolygonsToMap();
                addCropTypesToDB();
                addFarmNamesToDB();
                updateFarmNamesInDB();
                updateCropTypesInDB();
                if(onSyncFinishedListener!=null) {
                    onSyncFinishedListener.onSyncFinished();
                }
            } else{
                Intent intent = new Intent(ACTION_ERROR_MSG);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                addPolygonsToMap();
            }
        }
    }

    /**
     * Send the offline post requests that were saved in the database
     */

    private class PostOfflineDataTask extends AsyncTask<Void, Void, Boolean> implements APIContract{

        private List<String> requests;

        private PostOfflineDataTask(List<String> requests){
            this.requests = requests;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(!requests.isEmpty()) {
                for (String request : requests) {
                    try {
                        JSONObject jsonObject = new JSONObject(request);
                        PostRequest postRequest = new PostRequest();
                        postRequest.postJSONObject(jsonObject, APIContract.ADD_AEROVIEW_BOUNDARY);

                        do {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } while (!postRequest.isServerResponseReceived());

                        if (!postRequest.isServerError()) {
                            String tempId = postRequest.getResponseData().getString("tempId");
                            if(tempId!=null) {
                                JSONObject boundaryAdded = postRequest.getResponseData().getJSONObject("boundaryAdded");
                                String aeroviewId = String.valueOf(boundaryAdded.get("id"));
                                sqLiteDatabaseHandler.removeRequest(tempId);
                                sqLiteDatabaseHandler.updateBoundaryId(tempId, aeroviewId);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success){
            if(success){
                executeClientDataTask();
            }
        }

    }
}
