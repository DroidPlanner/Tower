package co.aerobotics.android.data;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by michaelwootton on 8/1/17.
 */

public class PostRequest {
    private boolean responseReceived = false;
    private JSONObject responseData = null;
    private boolean serverError = false;

    public PostRequest() {
    }

    public void post(String jsonStr, String url) {
        responseReceived = false;
        serverError = false;
        try {
            final JSONObject jsonBody = new JSONObject(jsonStr);
            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    responseData = response;
                    Log.d("Response", response.toString());
                    responseReceived = true;
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    responseReceived = true;
                    serverError = true;
                    Log.d("Response", error.toString());
                }
            });
            postRequest.setRetryPolicy(new DefaultRetryPolicy(
                    60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleyRequest.getInstance().getRequestQueue().add(postRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void postJSONObject(JSONObject jsonBody, String url){
        responseReceived = false;
        serverError = false;
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                responseData = response;
                Log.d("Post.Response", response.toString());
                responseReceived = true;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseReceived = true;
                serverError = true;
                Log.d("Post.Response", error.toString());
            }
        });
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyRequest.getInstance().getRequestQueue().add(postRequest);
    }

    public boolean isServerError() {
        return serverError;
    }

    public boolean isServerResponseReceived(){
        return responseReceived;
    }
    public JSONObject getResponseData(){
        return responseData;
    }

}
