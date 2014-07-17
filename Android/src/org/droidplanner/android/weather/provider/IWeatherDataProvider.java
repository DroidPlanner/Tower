package org.droidplanner.android.weather.provider;


import org.droidplanner.android.weather.item.IWeatherItem;
import org.droidplanner.core.helpers.coordinates.Coord2D;

public interface IWeatherDataProvider {

	

	public interface AsyncListener {
		public void onResult(IWeatherItem item);

		public void onError(String reason);
	}

	public void getWind(Coord2D location);
	public void getSolarRadiation();

	

}
