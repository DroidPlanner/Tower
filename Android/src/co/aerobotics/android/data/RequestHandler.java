package co.aerobotics.android.data;

import android.os.AsyncTask;

import com.android.volley.Request;

import org.json.JSONObject;


/**
 * Created by michaelwootton on 6/6/18.
 */

public class RequestHandler extends AsyncTask<Void, Void, Boolean> {

    private String url;
    private String token;
    private JSONObject params = null;
    private onRequestReturnedListener listener;
    private PostRequest postRequest = new PostRequest();
    private int requestMethod = -1;

    public interface onRequestReturnedListener {
        void onSuccess(JSONObject jsonObject);
        void onError(String error);
    }

    public RequestHandler() {
        this.listener = null;
    }

    public void post(String url, String token, JSONObject params) {
        this.url = url;
        this.token = token;
        this.params = params;
        this.requestMethod = Request.Method.POST;
    }

    public void get(String url, String token) {
        this.url = url;
        this.token = token;
        this.requestMethod = Request.Method.GET;
    }



    public void setOnRequestReturnedListener(onRequestReturnedListener onRequestReturnedListener) {
        this.listener = onRequestReturnedListener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        makeRequest();
        waitForRequestToReturnData();
        if (!isServerError()) {
            handleReturnData();
        } else {
            setError();
        }
        return null;
    }

    private void makeRequest() {
        postRequest.request(requestMethod, params, url, token);
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

    private boolean isServerError() {
        return postRequest.isServerError();
    }

    private void handleReturnData() {
        if (listener != null) {
            listener.onSuccess(postRequest.getResponseData());
        }
    }

    private void setError() {
        listener.onError(postRequest.getErrorMessage());
    }
}
