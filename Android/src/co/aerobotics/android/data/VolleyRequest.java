package co.aerobotics.android.data;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by aerobotics on 2017/05/06.
 */

public class VolleyRequest {
    //private static VolleyApplication mInstance = new VolleyApplication();
    private static VolleyRequest mInstance = null;
    private RequestQueue mRequestQueue;
    private Context mCtx;

    private VolleyRequest() {}

    private VolleyRequest(Context context){
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyRequest getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyRequest(context);
        }
        return mInstance;
    }

    public static synchronized VolleyRequest getInstance(){
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(JsonObjectRequest req) {
        getRequestQueue().add(req);
    }
}
