package org.droidplanner.android.maps.weather.provider;

import org.droidplanner.android.maps.weather.provider.items.WeatherItem;
import org.droidplanner.android.maps.weather.provider.items.Wind;
import org.droidplanner.core.helpers.coordinates.Coord2D;

public interface WeatherDataProvider {

	

	public interface AsyncListener {
		public void onResult(WeatherItem item);

		public void onError(String reason);
	}

	public void getWind(Coord2D location);

	

}
