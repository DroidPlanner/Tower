package org.droidplanner.android.fragments.widget.weather;

import android.location.Location;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by fredia on 5/22/16.
 */
class WeatherFetcher extends AsyncTask<Void, Void, JSONObject> {
    private WeakReference<MiniWidgetWeatherInfo> weakReference;
    private Location location;

    WeatherFetcher(MiniWidgetWeatherInfo weakReference, Location location) {
        this.weakReference = new WeakReference<>(weakReference);
        this.location = location;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        MiniWidgetWeatherInfo fragment = weakReference.get();
        return fragment != null ? fragment.fetchWeatherInformationRequest(location) : null;
    }

    /**
     * After completing background task refresh the testViews
     */
    @Override
    protected void onPostExecute(JSONObject result) {
        MiniWidgetWeatherInfo fragment = weakReference.get();
        if (fragment != null) {
            //Let the UI thread update the views.
            fragment.onJSONRetrieved(result);

        }
    }
}
