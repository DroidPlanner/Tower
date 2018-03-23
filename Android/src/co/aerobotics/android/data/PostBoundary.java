package co.aerobotics.android.data;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    private String email;
    private String password;
    private JSONArray farmArray;
    private JSONArray cropTypeArray;
    private Context context;
    private String tempId;

    public PostBoundary(Context context, Survey survey, String boundaryName, Integer farmID, Integer cropTypeID, Integer clientID, String email, String password, JSONArray farmArray, JSONArray cropTypeArray, String tempId) {
        this.survey = survey;
        this.boundaryName = boundaryName;
        this.farmID = farmID;
        this.cropTypeID = cropTypeID;
        this.clientID = clientID;
        this.email = email;
        this.password = password;
        this.farmArray = farmArray;
        this.cropTypeArray = cropTypeArray;
        this.context = context;
        this.tempId = tempId;
    }

    public void post(BoundaryDetail boundaryDetail){
        //PostRequest request = new PostRequest();
        //request.postJSONObject(generateJson(), APIContract.ADD_AEROVIEW_BOUNDARY);
        AddBoundaryTask mTask = new AddBoundaryTask(boundaryDetail);
        mTask.execute((Void) null);
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

    public JSONObject generateJson(){
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();

        try {
            jsonObject.put("email", email);
            jsonObject.put("password", password);
            jsonObject.put("clientId", clientID);
            jsonObject.put("newFarms", farmArray);
            jsonObject.put("newCropTypes", cropTypeArray);
            jsonObject.put("farmId", farmID);
            jsonObject.put("cropTypeId", cropTypeID);
            jsonObject.put("name", boundaryName);
            jsonObject.put("polygon", getPolygonCoords());
            jsonObject.put("hectares", survey.getPolygonArea());
            jsonObject.put("tempId", tempId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("Post.Response", jsonObject.toString());
        return jsonObject;

    }

    private class AddBoundaryTask extends AsyncTask<Void, Void, Boolean> implements APIContract {

        private JSONObject jsonObject;
        private BoundaryDetail boundaryDetail;

        public AddBoundaryTask(BoundaryDetail boundaryDetail){

            this.boundaryDetail = boundaryDetail;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            PostRequest postRequest = new PostRequest();
            postRequest.postJSONObject(generateJson(), APIContract.ADD_AEROVIEW_BOUNDARY);

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
                    jsonObject = postRequest.getResponseData().getJSONObject("boundaryAdded");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                return true;
            }
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){

                try {
                    JSONObject json = jsonObject;
                    String name = json.getString("name");
                    String polygon = json.getString("polygon");
                    String id = json.getString("id");
                    //List<BoundaryDetail> boundariesList = new ArrayList<>();
                    if (!polygon.equals("")) {
                        boundaryDetail.setName(name);
                        boundaryDetail.setBoundaryId(id);
                        boundaryDetail.setPoints(polygon);
                        //BoundaryDetail boundaryDetail = new BoundaryDetail(name,id, polygon);
                        //boundariesList.add(new BoundaryDetail(name,id, polygon));
                        //aeroviewPolygons.addBoundaryDetailsToDB(boundariesList);
                        SQLiteDatabaseHandler db_handler = new SQLiteDatabaseHandler(context);
                        db_handler.addBoundaryDetail(boundaryDetail);
                        AeroviewPolygons aeroviewPolygons = new AeroviewPolygons(context);
                        aeroviewPolygons.executeAeroViewSync();
                    }

                }  catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

    }

}
