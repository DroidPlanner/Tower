package org.droidplanner.android.maps.weather.provider;

import org.apache.http.Header;
import org.droidplanner.android.maps.weather.provider.items.Wind;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class HttpWeatherDataProvider implements WeatherDataProvider {

	private static AsyncHttpClient client = new AsyncHttpClient();
	
	private AsyncListener listener;
	
	@SuppressWarnings("unused")
	private HttpWeatherDataProvider() {
		
	}
	
	HttpWeatherDataProvider (AsyncListener listener){
		this.listener = listener;
	}

	// openweathermap.org
	private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";

	void get(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		client.get(url, params, responseHandler);
	}

	@Override
	public void getWind(Coord2D location) {
		RequestParams params = new RequestParams();
		params.put("lat", location.getLat());
		params.put("lon", location.getLng());
		get(WEATHER_URL, params, httpHandler);

	}

	private JsonHttpResponseHandler httpHandler = new JsonHttpResponseHandler() {

		public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
			try {
				JSONObject windJson = response.getJSONObject("wind");
				double speed = windJson.getDouble("speed");
				double bearing = windJson.getDouble("deg");
				
				Wind wind = new Wind(speed,bearing);
				listener.onResult(wind);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		};
		
		public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
			listener.onError("Http error: " + statusCode + ", " + throwable.getMessage());
		};
	};

}
