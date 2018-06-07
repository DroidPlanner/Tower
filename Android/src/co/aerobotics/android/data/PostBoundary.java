package co.aerobotics.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import co.aerobotics.android.R;
import co.aerobotics.android.activities.interfaces.APIContract;

/**
 * Created by michaelwootton on 9/18/17.
 */

public class PostBoundary implements APIContract{

    private final Survey survey;
    private String boundaryName;
    private Integer farmID;
    private Integer cropTypeID;
    private Integer clientID;
    private JSONArray farmArray;
    private Context context;

    public PostBoundary(Context context, Survey survey, String boundaryName, Integer farmID,
                        Integer cropTypeID, Integer clientID, String email, String password,
                        JSONArray farmArray, JSONArray cropTypeArray, String tempId) {
        this.survey = survey;
        this.boundaryName = boundaryName;
        this.farmID = farmID;
        this.cropTypeID = cropTypeID;
        this.clientID = clientID;
        this.farmArray = farmArray;
        this.context = context;
    }


    public void post(BoundaryDetail boundaryDetail){
        //PostRequest request = new PostRequest();
        //request.postJSONObject(getNewBoundaryParamsAsJson(), APIContract.GATEWAY_ORCHARDS);
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
        String token = sharedPref.getString(context.getResources().getString(R.string.user_auth_token), "");

        if (farmArray.length() == 0) {
            AddBoundaryTask mTask = new AddBoundaryTask(boundaryDetail, token);
            mTask.execute((Void) null);
        } else {
            String farmName = null;
            try {
                farmName = this.farmArray.get(0).toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (farmName != null) {
                AddNewFarmTask mAddNewFarmTask= new AddNewFarmTask(boundaryDetail, farmName, this.clientID, token);
                mAddNewFarmTask.execute((Void) null);
            }

        }

    }

    private String getPolygonCoords(){

        List<LatLong> points = survey.getPolygonPoints();
        StringBuilder pointListAsString = new StringBuilder();
        for (LatLong point : points){
            pointListAsString.append(String.format("%s,%s ", String.valueOf(point.getLongitude()),
                    String.valueOf(point.getLatitude())));
        }
        return pointListAsString.toString().trim();
    }

    public JSONObject getNewBoundaryParamsAsJson(){
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        try {
            jsonObject.put("client_id", clientID);
            jsonObject.put("farm_id", farmID);
            jsonObject.put("crop_type_id", cropTypeID);
            jsonObject.put("name", boundaryName);
            jsonObject.put("polygon", getPolygonCoords());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("Post.Response", jsonObject.toString());
        return jsonObject;

    }

    private class AddBoundaryTask extends AsyncTask<Void, Void, Boolean> implements APIContract {

        private JSONObject jsonObject;
        private BoundaryDetail boundaryDetail;
        private String mToken;

        public AddBoundaryTask(BoundaryDetail boundaryDetail, String mToken){
            this.boundaryDetail = boundaryDetail;
            this.mToken = mToken;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            PostRequest postRequest = new PostRequest();
            postRequest.postJSONObject(getNewBoundaryParamsAsJson(), APIContract.GATEWAY_ORCHARDS, mToken);
//            OkHttpRequest okHttpRequest = new OkHttpRequest();
//            String response = okHttpRequest.okHttpPost(getNewBoundaryParamsAsJson(), APIContract.GATEWAY_ORCHARDS, mToken);

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
            } else {

                try {
                    jsonObject = postRequest.getResponseData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){

                try {
                    SQLiteDatabaseHandler db_handler = new SQLiteDatabaseHandler(context);
                    JSONObject json = jsonObject;
                    String name = json.getString("name");
                    String polygon = json.getString("polygon");
                    String id = json.getString("id");
                    int farmId = json.getInt("farm_id");

                    if (!polygon.equals("")) {
                        boundaryDetail.setName(name);
                        boundaryDetail.setBoundaryId(id);
                        boundaryDetail.setPoints(polygon);
                        boundaryDetail.setFarmId(farmId);
                        db_handler.addBoundaryDetail(boundaryDetail);
                        //TODO: add boundaries to map
//                        AeroviewPolygons aeroviewPolygons = new AeroviewPolygons(context);
//                        aeroviewPolygons.executeAeroViewSync();
                    }

                }  catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class AddNewFarmTask extends AsyncTask<Void, Void, Boolean> implements APIContract {

        private JSONObject jsonObject;
        private String mToken;
        private BoundaryDetail boundaryDetail;
        private String farmName;
        private int clientId;

        public AddNewFarmTask(BoundaryDetail boundaryDetail, String farmName, int clientId, String mToken){
            this.boundaryDetail = boundaryDetail;
            this.farmName = farmName;
            this.clientId = clientId;
            this.mToken = mToken;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            JSONObject postParams = new JSONObject();
            try {
                postParams.put("name", this.farmName);
                postParams.put("client_id", this.clientId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            PostRequest postRequest = new PostRequest();
            postRequest.postJSONObject(postParams, APIContract.GATEWAY_FARMS, mToken);

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
            } else {

                try {
                    jsonObject = postRequest.getResponseData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){
                try {
                    SQLiteDatabaseHandler db_handler = new SQLiteDatabaseHandler(context);
                    JSONObject json = jsonObject;
                    String farmName = json.getString("name");
                    int clientId = json.getInt("client_id");
                    int farmId = json.getInt("id");
                    db_handler.createFarmName(farmName, farmId, clientId);
                    AddBoundaryTask mTask = new AddBoundaryTask(boundaryDetail, this.mToken);
                    mTask.execute((Void) null);
                }  catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
