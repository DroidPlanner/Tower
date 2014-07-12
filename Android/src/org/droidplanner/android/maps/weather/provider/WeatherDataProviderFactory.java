package org.droidplanner.android.maps.weather.provider;

import org.droidplanner.android.maps.weather.provider.WeatherDataProvider.AsyncListener;

public class WeatherDataProviderFactory {
	public enum ProviderType {
		OPEN_WEATHER_MAP
	}
	
	private WeatherDataProviderFactory(){
		
	}

	public static WeatherDataProvider getInstance(ProviderType type, AsyncListener listener) {
		WeatherDataProvider provider = null;
		switch (type) {
		case OPEN_WEATHER_MAP:
			provider = new HttpWeatherDataProvider(listener);
			break;
		}

		return provider;
	}
}
