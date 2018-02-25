package co.aerobotics.android.data;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aerobotics on 2017/05/06.
 */

public class AuthRequest extends JsonObjectRequest {
    public AuthRequest(String clientId, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener){
        super(Request.Method.GET, String.format("%s%s/","https://aeroview.aerobotics.co.za/list/", clientId), null, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> params = new HashMap<String, String>();
        String creds = String.format("%s:%s","","");
        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
        params.put("Authorization", auth);
        return params;

    }
}
