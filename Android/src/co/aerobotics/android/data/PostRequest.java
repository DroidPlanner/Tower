package co.aerobotics.android.data;

import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by michaelwootton on 8/1/17.
 */

public class PostRequest {
    private boolean responseReceived = false;
    private JSONObject responseData = null;
    private boolean serverError = false;
    private String getRequestResponseData = "";
    private String errorMessage = null;
    private OnPostReturnedListener onPostReturnedListener;


    public PostRequest() {
    }

    public interface OnPostReturnedListener {
        void onSuccessfulResponse();
        void onErrorResponse();
    }

    public void setOnPostReturnedListener(PostRequest.OnPostReturnedListener listener){
        onPostReturnedListener = listener;
    }


    public void login(String jsonStr, String url) {
        resetFlags();
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

    private void resetFlags() {
        responseReceived = false;
        serverError = false;
    }


    public void post(String params, String url, final String token) {
        resetFlags();
        try {
            final JSONObject jsonBody = new JSONObject(params);
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
                    errorMessage = error.toString();
                    Log.i("Response", error.toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=UTF-8");
                    headers.put("Authorization", token);
                    return headers;
                }
            };

            postRequest.setRetryPolicy(new DefaultRetryPolicy(
                    60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleyRequest.getInstance().getRequestQueue().add(postRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void get(String url, final String token) {
        responseReceived = false;
        serverError = false;
        Log.i("Response", "GET URL: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        getRequestResponseData = response;
                        Log.d("Response", response);
                        responseReceived = true;
                    }
            },
                new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                responseReceived = true;
                serverError = true;
                Log.i("Response", "GET ERROR:" + error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Authorization", token);
                return headers;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyRequest.getInstance().getRequestQueue().add(stringRequest);
    }

    public void postJSONObject(JSONObject jsonBody, String url, final String token){
        responseReceived = false;
        serverError = false;
        final JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Post.Response", response.toString());
                responseData = response;
                responseReceived = true;
                onPostReturnedListener.onSuccessfulResponse();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Post.Response", error.toString());
                responseReceived = true;
                serverError = true;
                onPostReturnedListener.onErrorResponse();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Authorization", token);
                return headers;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyRequest.getInstance().getRequestQueue().add(postRequest);
    }

    public void request(int requestMethod, JSONObject jsonBody, String url, final String token) {
        responseReceived = false;
        serverError = false;
        final JsonObjectRequest postRequest = new JsonObjectRequest(requestMethod, url, jsonBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Post.Response", response.toString());
                responseData = response;
                responseReceived = true;

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Post.Response", error.toString());
                responseReceived = true;
                serverError = true;
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Authorization", token);
                return headers;
            }
        };
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
    public String getReturnDataString() {
        return getRequestResponseData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }



}
