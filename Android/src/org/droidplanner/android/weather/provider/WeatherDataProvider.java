package org.droidplanner.android.weather.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.droidplanner.android.weather.item.SolarRadiation;
import org.droidplanner.android.weather.item.Wind;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

public class WeatherDataProvider implements IWeatherDataProvider {

	private static AsyncHttpClient client = new AsyncHttpClient();

	private AsyncListener listener;

	@SuppressWarnings("unused")
	private WeatherDataProvider() {

	}

	public WeatherDataProvider(AsyncListener listener) {
		this.listener = listener;
	}

	// Wind speed url
	private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";

	// Solar radiation url
	private static final String SOLAR_RADIATION_URL = "http://www.swpc.noaa.gov/ftpdir/lists/geomag/AK.txt";

	void get(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		client.get(url, params, responseHandler);
	}

	@Override
	public void getWind(Coord2D location) {
		RequestParams params = new RequestParams();
		params.put("lat", location.getLat());
		params.put("lon", location.getLng());
		get(WEATHER_URL, params, windHttpHandler);

	}

	private JsonHttpResponseHandler windHttpHandler = new JsonHttpResponseHandler() {

		public void onSuccess(int statusCode, Header[] headers,
				JSONObject response) {
			try {
				JSONObject windJson = response.getJSONObject("wind");
				double speed = windJson.getDouble("speed");

				Wind wind = new Wind(speed);
				listener.onWeatherFetchSuccess(wind);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		};

		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
			//listener.onWeatherFetchFail("Http error: " + statusCode + ", "
					//+ throwable.getMessage());
		};
	};

	private TextHttpResponseHandler solarRadiationHttpHandler = new TextHttpResponseHandler() {

		@Override
		public void onSuccess(int statusCode, Header[] headers, String response) {
			boolean isTodayReached = false;
			Scanner scanner = new Scanner(response);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains("Planetary")) {
					if (!isTodayReached) {
						isTodayReached = true;
						continue;
					}

					List<Integer> kIndexList = new ArrayList<Integer>();
					Pattern p = Pattern.compile("-?\\d+");
					Matcher m = p.matcher(line);
					while (m.find()) {
						kIndexList.add(Integer.parseInt(m.group()));
					}

					for (int i = kIndexList.size() - 1; i > 0; i--) {
						int kIndex = kIndexList.get(i);
						if (kIndex != -1) {
							listener.onWeatherFetchSuccess(new SolarRadiation(kIndex));
							scanner.close();
							return;
						}
					}
				}

			}
			scanner.close();
		}

		@Override
		public void onFailure(int statusCode, Header[] headers,
				String responseString, Throwable throwable) {
			//listener.onWeatherFetchFail("Http error: " + statusCode + ", "
				//	+ throwable.getMessage());

		}
	};

	@Override
	public void getSolarRadiation() {
		get(SOLAR_RADIATION_URL, null, solarRadiationHttpHandler);

	}

}
