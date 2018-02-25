package co.aerobotics.android.graphic.map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.o3dr.services.android.lib.coordinate.LatLong;

import co.aerobotics.android.R;
import co.aerobotics.android.maps.MarkerInfo;

public class GraphicHome extends MarkerInfo {

	private double homeLocationLat = 181, homeLocationLon = 181;
	public GraphicHome() {
	}

	public void setHomePosition(double latitude, double longitude){
		homeLocationLat = latitude;
		homeLocationLon = longitude;
	}
	@Override
	public float getAnchorU() {
		return 0.5f;
	}

	public boolean isValid() {
        //Home droneHome = drone.getAttribute(AttributeType.HOME);
		return checkGpsCoordination(homeLocationLat, homeLocationLon);
	}
    private static boolean checkGpsCoordination(double latitude, double longitude) {
        if (!Double.isNaN(latitude) && !Double.isNaN(longitude)){
            return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
        }
        else{
            return false;
        }

    }

	@Override
	public float getAnchorV() {
		return 0.5f;
	}

	@Override
	public Bitmap getIcon(Resources res) {
		return BitmapFactory.decodeResource(res, R.drawable.ic_wp_home);
	}

	@Override
	public LatLong getPosition() {
        //Home droneHome = drone.getAttribute(AttributeType.HOME);
        //if(droneHome == null) return null;
		LatLong droneHome = new LatLong(homeLocationLat, homeLocationLon);
		return droneHome;
	}

	/*public void setPosition(LatLong position){
		//Move the home location
		final Home currentHome = drone.getAttribute(AttributeType.HOME);
		final LatLongAlt homeCoord = currentHome.getCoordinate();
		final double homeAlt = homeCoord == null ? 0 : homeCoord.getAltitude();

		final LatLongAlt newHome = new LatLongAlt(position, homeAlt);
		VehicleApi.getApi(drone).setVehicleHome(newHome, new AbstractCommandListener() {
			@Override
			public void onSuccess() {
				Timber.i("Updated home location to %s", newHome);
				Toast.makeText(context, "Updated home location.", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(int i) {
				Timber.e("Unable to update home location: %d", i);
			}

			@Override
			public void onTimeout() {
				Timber.w("Home location update timed out.");
			}
		});
	}*/

	/*@Override
	public String getSnippet() {
        Home droneHome = drone.getAttribute(AttributeType.HOME);
		LatLongAlt coordinate = droneHome == null ? null : droneHome.getCoordinate();
		return "Home " + (coordinate == null ? "N/A" : coordinate.getAltitude());
	}*/

	@Override
	public String getTitle() {
		return "Home";
	}

	@Override
	public boolean isVisible() {
		return isValid();
	}

	@Override
	public boolean isDraggable() {
		return true;
	}
}
