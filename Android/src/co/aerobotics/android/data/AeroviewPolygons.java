package co.aerobotics.android.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;

import co.aerobotics.android.activities.interfaces.APIContract;
import co.aerobotics.android.graphic.map.PolygonData;

import com.goebl.simplify.PointExtractor;
import com.goebl.simplify.Simplify;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by michaelwootton on 8/2/17.
 */

public class AeroviewPolygons implements APIContract{

    private Context context;
    public static final String ACTION_POLYGON_UPDATE = "update_polygon";
    public static final String ACTION_ERROR_MSG = "server_error";
    public static final String SYNC_COMPLETE = "sync_successful";
    public static final String ACTION_VIEW_FARM = "zoom_to_farm";
    private SQLiteDatabaseHandler sqLiteDatabaseHandler;
    private OnSyncFinishedListener onSyncFinishedListener;
    private static final int DRONE_DEMO_ACCOUNT_ID = 247;
    private boolean isGetCropTypesTaskExecuted = false;
    private boolean isGetFarmOrchardsTaskExecuted = false;
    private boolean isGetFarmsTaskExecuted = false;
    private SharedPreferences sharedPref;
    private ArrayList<String> farmPointStrings;

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
        sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);

    }

    private void addNewCropTypesToDB(String jsonString) {
        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++){
                JSONObject dict = array.getJSONObject(i);
                sqLiteDatabaseHandler.createCropType(dict.getString("name"), dict.getInt("id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateExistingCropTypesInDB(String jsonString) {
        try {
            JSONArray array = new JSONArray(jsonString);
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
    private void addBoundaryDetailsToDB(List<BoundaryDetail> boundaryDetailList){
        SQLiteDatabaseHandler dbHandler = new SQLiteDatabaseHandler(context);
        dbHandler.addBoundaryDetailList(boundaryDetailList);
    }

    public void addPolygonsToMap(){
        String activeFarmIds = getActiveFarmsString();
        List<BoundaryDetail> boundaryDetails = sqLiteDatabaseHandler.getBoundaryDetailsForFarmIds(activeFarmIds);
        DroidPlannerApp.getInstance().polygonMap.clear();
        farmPointStrings = new ArrayList<>();

        for (BoundaryDetail boundaryDetail : boundaryDetails) {
            PolygonData polygonData = new PolygonData(boundaryDetail.getName(), convertStringToLatLngList(boundaryDetail.getPoints()), false, boundaryDetail.getBoundaryId());
            DroidPlannerApp.getInstance().polygonMap.put(boundaryDetail.getBoundaryId(), polygonData);

            for(LatLng point: polygonData.getPoints())
                farmPointStrings.add(point.latitude + " " + point.longitude);
        }
        Intent intent = new Intent(ACTION_POLYGON_UPDATE);
        if(farmPointStrings!=null) intent.putStringArrayListExtra("farm_points", farmPointStrings);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private String getActiveFarmsString() {
        String activeFarmsString= context.getResources().getString(R.string.active_farms);
        String activeFarmsIdsFromSharedPrefs = sharedPref.getString(activeFarmsString, "[]");
        String activeFarmsIdsFormatted = activeFarmsIdsFromSharedPrefs.replaceAll("\\[", "").replaceAll("]","");
        return activeFarmsIdsFormatted;
    }

    public List<LatLng> convertStringToLatLngList(String polygon){
        String[] latLongPairs = polygon.split(" ");
        LatLng [] points = (convertToLatLongList(latLongPairs));
        Simplify<LatLng> simplify = new Simplify<LatLng>(new LatLng[0], latLngPointExtractor);
        LatLng[] simplified = simplify.simplify(points, 80f, false);
        return new ArrayList<>(Arrays.asList(simplified));
    }

    public void postOfflineFarms() {
        String token = sharedPref.getString(context.getResources().getString(R.string.user_auth_token), "");
        Integer clientId = sharedPref.getInt(context.getResources().getString(R.string.client_id), -1);
        JSONArray farmsToSync = sqLiteDatabaseHandler.getOfflineFarms(clientId);
        if (farmsToSync.length() > 0) {
            for (int i = 0; i < farmsToSync.length(); i++) {
                try {
                    OfflineFarmPostHandler offlineFarmPostHandler = new OfflineFarmPostHandler(farmsToSync.getJSONObject(i), token);
                    offlineFarmPostHandler.syncFarmWithServer();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class OfflineFarmPostHandler {

        private JSONObject farmDetails;
        private String token;

        OfflineFarmPostHandler(JSONObject farmDetails, String token) {
            this.farmDetails = farmDetails;
            this.token = token;
        }

        void syncFarmWithServer() {
            final PostRequest postRequest = new PostRequest();
            postRequest.setOnPostReturnedListener(new PostRequest.OnPostReturnedListener() {
                @Override
                public void onSuccessfulResponse() {
                    updateLocalDatabase(postRequest.getResponseData());
                }

                @Override
                public void onErrorResponse() {

                }
            });

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    postRequest.postJSONObject(farmDetails, APIContract.GATEWAY_FARMS, token);
                }
            });
        }

        private void updateLocalDatabase(JSONObject returnData) {
            try {
                Integer tempFarmId = farmDetails.getInt("temp_id");
                Integer primaryKey = farmDetails.getInt("primary_key_id");
                Integer farmId = returnData.getInt("id");
                sqLiteDatabaseHandler.updateFarmNameId(farmId, primaryKey);

                String activeFarms = sharedPref.getString(context.getResources().getString(R.string.active_farms), "[]");
                List<Integer> activeFarmIds = parseStringToListIntegerObject(activeFarms);
                if (activeFarmIds.contains(tempFarmId)) {
                    activeFarmIds.remove(tempFarmId);
                    activeFarmIds.add(farmId);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(context.getResources().getString(R.string.active_farms), new Gson().toJson(activeFarmIds)).apply();
                }

                JSONArray offlineBoundariesForFarm = sqLiteDatabaseHandler.getOfflineBoundariesForFarm(tempFarmId, farmId);
                PostOfflineBoundariesTask mPostTask = new PostOfflineBoundariesTask(offlineBoundariesForFarm, token);
                mPostTask.execute((Void) null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void executeOfflineBoundariesSync(){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        String token = sharedPref.getString(context.getResources().getString(R.string.user_auth_token), "");
        JSONArray requestParams = sqLiteDatabaseHandler.getOfflineBoundariesForSyncedFarms();
        PostOfflineBoundariesTask mPostTask = new PostOfflineBoundariesTask(requestParams, token);
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

    public void executeGetFarmOrchardsTask() {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        String token = sharedPref.getString(context.getResources().getString(R.string.user_auth_token), "");
        String activeFarms = sharedPref.getString(context.getResources().getString(R.string.active_farms), "[]");
        List<Integer> farmIds = parseStringToListIntegerObject(activeFarms);
        farmIds.removeAll(Collections.singleton(null));
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.active_farms), new Gson().toJson(farmIds)).apply();
        if (farmIds != null && farmIds.size() > 0) {
            List<Integer> tempFarmIds = new ArrayList<>();
            for(Integer farmId: farmIds) {
                if(farmId < 0) {
                    tempFarmIds.add(farmId);
                }
            }
            farmIds.removeAll(tempFarmIds);
            getFarmOrchardsTask getFarmOrchardsTask = new getFarmOrchardsTask(token, farmIds);
            getFarmOrchardsTask.execute((Void) null);
        } else {
            isGetFarmOrchardsTaskExecuted = true;
        }
    }


    private List<Integer> parseStringToListIntegerObject(String activeFarmsString) {
        Type type = new TypeToken<ArrayList<Integer>>() { }.getType();
        return new Gson().fromJson(activeFarmsString, type);
    }

    private class getFarmOrchardsTask extends AsyncTask<Void, Void, Boolean> implements APIContract {

        private String mToken;
        private List<Integer> farmIds;
        PostRequest postRequest = new PostRequest();
        private String orchards;
        List<BoundaryDetail> boundariesList = new ArrayList<>();

        private getFarmOrchardsTask(String mToken, List<Integer> farmIds) {
            this.mToken = mToken;
            this.farmIds = farmIds;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            isGetFarmOrchardsTaskExecuted = false;
            makeGetRequestForFarmOrchards();
            waitForRequestToReturnData();
            if (!isServerError()) {
                handleReturnData();
                return true;
            }
            return false;
        }

        private void makeGetRequestForFarmOrchards() {
            String farmIds = parseListObjectToString();
            postRequest.get(APIContract.GATEWAY_ORCHARDS + "?farm_id__in=" + farmIds, mToken);
        }

        @NonNull
        private String parseListObjectToString() {
            StringBuilder stringBuilder  = new StringBuilder();
            Iterator<Integer> iterator = farmIds.iterator();
            while(iterator.hasNext())
            {
                stringBuilder.append(iterator.next());
                if(iterator.hasNext()) {
                    stringBuilder.append(",");
                }
            }
            return stringBuilder.toString();
        }

        private void waitForRequestToReturnData() {
            do {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!postRequest.isServerResponseReceived());
        }

        @NonNull
        private Boolean isServerError() {
            return postRequest.isServerError();
        }

        private void handleReturnData() {
            getReturnData();
            parseReturnDataToBoundaryDetailList();
            addBoundariesToLocalDatabase();
        }

        private void getReturnData() {
            orchards = postRequest.getReturnDataString();
        }

        @Override
        protected void onPostExecute(final Boolean requestReturnedSuccessfully) {
            if(requestReturnedSuccessfully){
                setTaskCompleteFlag();
                handleAsyncRequestReturns();
                addPolygonsToMap();
            } else{
                displayErrorMessage();
            }
        }

        private void setTaskCompleteFlag() {
            isGetFarmOrchardsTaskExecuted = true;
        }

        private void parseReturnDataToBoundaryDetailList() {
            JSONArray boundariesArray = null;
            try {
                boundariesArray = new JSONArray(orchards);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (boundariesArray != null) {
                for (int i = 0; i < boundariesArray.length(); i++) {
                    JSONObject jsonObject;
                    try {
                        jsonObject = boundariesArray.getJSONObject(i);
                        String id = jsonObject.getString("id");
                        String name = jsonObject.getString("name");
                        String polygon = jsonObject.getString("polygon");
                        String altitudes = "";
                        //if (polygonPointsAltered(id, polygon) || polygonPointAltitudesEmpty(id, polygon)) {
                        List<LatLng> pointList = convertStringToLatLngList(polygon);
                        altitudes = fetchPointAltitudes(pointList);
                        //}
                        int farmId = jsonObject.getInt("farm_id");
                        int clientId = jsonObject.getInt("client_id");
                        int cropTypeId = jsonObject.getInt("crop_type_id");
                        if (!polygon.equals("") && clientId != DRONE_DEMO_ACCOUNT_ID) {
                            boundariesList.add(new BoundaryDetail(name, id, polygon, altitudes, clientId, cropTypeId, farmId));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void addBoundariesToLocalDatabase() {
            if(!boundariesList.isEmpty()) {
                addBoundaryDetailsToDB(boundariesList);
            }
        }

        private void displayErrorMessage() {
            Intent intent = new Intent(ACTION_ERROR_MSG);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    /*
     * Checks if the points of a boundary polygon have been altered
     */
    public boolean polygonPointsAltered(String id, String polygon) {
        BoundaryDetail bound = sqLiteDatabaseHandler.getBoundaryDetail(id);
        return !bound.getPoints().equals(polygon);
    }

    /*
     * takes in a list of LatLng coordinates and queries the Google Elevation API to return each point's altitude
     */
    public String fetchPointAltitudes(List<LatLng> polygon) throws IOException {
        String requestStringStart = "https://maps.googleapis.com/maps/api/elevation/json?locations=";
        String requestStringEnd = "&key=AIzaSyAilGCqDRAqrYxjWF3saUBkadntr-i8hKw";
        String pointString = "";
        int counter = 0;
        int step = 1; // every how many points should elevation data be fetched?
        int maxpoints = 100; // prevent a massive number of API calls (and URLS longer than 8192 chars) - sample and hold altitudes at intervals
        if (polygon.size() > maxpoints) {
            step = (int) Math.ceil((double) polygon.size() / (double) maxpoints);
        }

        for (LatLng point : polygon) {
            if (counter == 0) {
                pointString += point.latitude + "," + point.longitude;
            } else if (counter % step == 0) { // only make a call every *step* iterations
                pointString +=  "|" + point.latitude + "," + point.longitude;
            }
            counter++;
        }

        String requestString = requestStringStart + pointString + requestStringEnd;
        String outputString = "";

        try  {
            String returnData = getJSONFromUrl(requestString, 5000);
            JSONObject obj = new JSONObject(returnData);

            JSONArray arr = obj.getJSONArray("results");

            for (int i = 0; i < polygon.size(); i++) {
                String altitude = "";

                if (i/step <= arr.length()-1) {
                    altitude = arr.getJSONObject(i/step).getString("elevation");
                } else {
                    altitude = arr.getJSONObject(arr.length()-1).getString("elevation"); // If something's gone wrong, fill overflow with last value in JSON object
                }

                if (i == 0) {
                    outputString = altitude;
                } else {
                    outputString += "," + altitude;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return outputString;
    }

    public String getJSONFromUrl(String url, int timeout) {
        HttpURLConnection c = null;
        try {
            URL requestURL = new URL(url);
            c = (HttpsURLConnection) requestURL.openConnection();

            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            return sb.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
        return null;
    }

    /**
     * Send the offline post requests that were saved in the database
     */

    private class PostOfflineBoundariesTask extends AsyncTask<Void, Void, Boolean> implements APIContract{

        private JSONArray requests;
        private String mToken;

        private PostOfflineBoundariesTask(JSONArray requests, String mToken){
            this.requests = requests;
            this.mToken = mToken;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(requests.length() > 0) {
                for (int i = 0; i < requests.length(); i++) {
                    try {
                        final JSONObject jsonObject = requests.getJSONObject(i);
                        final PostRequest postRequest = new PostRequest();
                        postRequest.postJSONObject(jsonObject, APIContract.GATEWAY_ORCHARDS, mToken);
                        postRequest.setOnPostReturnedListener(new PostRequest.OnPostReturnedListener() {
                            @Override
                            public void onSuccessfulResponse() {
                                JSONObject returnData = postRequest.getResponseData();
                                try {
                                    String id = returnData.getString("id");
                                    String farmId = returnData.getString("farm_id");
                                    String tempId = jsonObject.getString("temp_id");
                                    sqLiteDatabaseHandler.removeRequest(tempId);
                                    sqLiteDatabaseHandler.updateOfflineBoundary(tempId, id, farmId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onErrorResponse() {

                            }
                        });

//                        do {
//                            try {
//                                Thread.sleep(10);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        } while (!postRequest.isServerResponseReceived());
//
//                        if (!postRequest.isServerError()) {
//                            String tempId = postRequest.getResponseData().getString("tempId");
//                            if(tempId!=null) {
//                                JSONObject boundaryAdded = postRequest.getResponseData().getJSONObject("boundaryAdded");
//                                String aeroviewId = String.valueOf(boundaryAdded.get("id"));
//                                sqLiteDatabaseHandler.removeRequest(tempId);
//                                sqLiteDatabaseHandler.updateBoundaryId(tempId, aeroviewId);
//                            }
//                        }

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
                // executeClientDataTask();
            }
        }

    }

    public void executeGetCropTypesTask () {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        String token = sharedPref.getString(context.getResources().getString(R.string.user_auth_token), "");
        GetCropTypesTask getCropTypesTask = new GetCropTypesTask(token);
        getCropTypesTask.execute((Void) null);
    }

    private class GetCropTypesTask extends AsyncTask<Void, Void, Boolean> implements APIContract {

        private String mToken;
        PostRequest cropTypesRequest = new PostRequest();
        String cropTypes;

        private GetCropTypesTask(String mToken) {
            this.mToken = mToken;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            makeGetRequestForCropTypes();
            waitForRequestToReturnData();
            if (!isServerError()) {
                handleReturnData();
                return true;
            }
            return false;
        }

        private void makeGetRequestForCropTypes() {
            cropTypesRequest.get(APIContract.GATEWAY_CROPTYPES, mToken);
        }

        private void waitForRequestToReturnData() {
            do {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!cropTypesRequest.isServerResponseReceived());
        }

        private boolean isServerError() {
            return cropTypesRequest.isServerError();
        }

        private void handleReturnData() {
            getReturnData();
            addNewCropTypesToDB(cropTypes);
            updateExistingCropTypesInDB(cropTypes);
        }

        private void getReturnData() {
            cropTypes = cropTypesRequest.getReturnDataString();
        }

        @Override
        protected void onPostExecute(final Boolean requestReturnedSuccessfully) {
            if(requestReturnedSuccessfully){
                setTaskCompleteFlag();
                handleAsyncRequestReturns();
            } else {
                displayErrorMessage();
            }
        }

        private void setTaskCompleteFlag() {
            isGetCropTypesTaskExecuted = true;
        }

        private void displayErrorMessage() {
            Intent intent = new Intent(ACTION_ERROR_MSG);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    public void executeGetFarmsTask() {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        String token = sharedPref.getString(context.getResources().getString(R.string.user_auth_token), "");
        Integer userId = sharedPref.getInt(context.getResources().getString(R.string.user_id), -1);
        getFarmsTask getFarmsTask = new getFarmsTask(token, userId);
        getFarmsTask.execute((Void) null);
    }

    private class getFarmsTask extends AsyncTask<Void, Void, Boolean> implements APIContract {

        private String token;
        private Integer userId;
        private PostRequest farmsRequest = new PostRequest();
        private String user;

        private getFarmsTask(String token, Integer userId) {
            this.token = token;
            this.userId = userId;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            makeGetRequestForFarms();
            waitForRequestToReturnData();
            return !isServerError();
        }

        private void makeGetRequestForFarms() {
            farmsRequest.get(APIContract.GATEWAY_USERS + userId.toString() + "/", token);
        }

        private void waitForRequestToReturnData() {
            do {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!farmsRequest.isServerResponseReceived());
        }

        private boolean isServerError() {
            return farmsRequest.isServerError();
        }

        private void getReturnData() {
            user = farmsRequest.getReturnDataString();
        }

        @Override
        protected void onPostExecute(final Boolean requestReturnedSuccessfully) {
            if(requestReturnedSuccessfully){
                handleReturnData();
                setTaskCompleteFlag();
                handleAsyncRequestReturns();
            } else{
                displayErrorMessage();
            }
        }

        private void handleReturnData() {
            getReturnData();
            try {
                JSONObject user = new JSONObject(this.user);
                FarmDataHandler farmDataHandler = new FarmDataHandler(context, user);
                farmDataHandler.parseUsersFarms();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void setTaskCompleteFlag() {
            isGetFarmsTaskExecuted = true;
        }

        private void displayErrorMessage() {
            Intent intent = new Intent(ACTION_ERROR_MSG);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    public void executeGetCropFamiliesTask () {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        String token = sharedPref.getString(context.getResources().getString(R.string.user_auth_token), "");
        GetCropFamilies getCropFamilies = new GetCropFamilies(token);
        getCropFamilies.execute((Void) null);
    }

    private class GetCropFamilies extends AsyncTask<Void,Void, Boolean> implements APIContract {

        private String mToken;
        PostRequest cropFamiliesRequest = new PostRequest();
        String cropFamilies;

        private GetCropFamilies(String mToken) {
            this.mToken = mToken;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            makeGetRequestForCropFamilies();
            waitForRequestToReturnData();
            if (!isServerError()) {
                handleReturnData();
                return true;
            }
            return false;
        }

        private void makeGetRequestForCropFamilies() {
            cropFamiliesRequest.get(APIContract.GATEWAY_CROPFAMILIES, mToken);
        }

        private void waitForRequestToReturnData() {
            do {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!cropFamiliesRequest.isServerResponseReceived());
        }

        private boolean isServerError() {
            return cropFamiliesRequest.isServerError();
        }

        private void handleReturnData() {
            getReturnData();
            addNewCropFamiliesToDB(cropFamilies);
        }

        private void addNewCropFamiliesToDB(String cropFamilies) {
            try {
                JSONArray array = new JSONArray(cropFamilies);
                for (int i = 0; i < array.length(); i++){
                    JSONObject dict = array.getJSONObject(i);
                    sqLiteDatabaseHandler.createCropFamily(dict.getString("name"), dict.getInt("id"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void getReturnData() {
            cropFamilies = cropFamiliesRequest.getReturnDataString();
        }

        @Override
        protected void onPostExecute(final Boolean requestReturnedSuccessfully) {
            if(requestReturnedSuccessfully){
                handleAsyncRequestReturns();
            } else{
                displayErrorMessage();
            }
        }

        private void displayErrorMessage() {
            Intent intent = new Intent(ACTION_ERROR_MSG);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    public interface OnSyncFinishedListener {
        void onSyncFinished();
    }

    public void setOnSyncFinishedListener(OnSyncFinishedListener listener){
        onSyncFinishedListener = listener;
    }

    private void handleAsyncRequestReturns() {
        if (isGetCropTypesTaskExecuted && isGetFarmOrchardsTaskExecuted && isGetFarmsTaskExecuted) {
            if(onSyncFinishedListener != null) {
                onSyncFinishedListener.onSyncFinished();
            }
        }
    }
}
