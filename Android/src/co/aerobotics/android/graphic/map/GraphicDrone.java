package co.aerobotics.android.graphic.map;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;

import co.aerobotics.android.maps.MarkerInfo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.o3dr.services.android.lib.coordinate.LatLong;

import dji.sdk.base.BaseProduct;


public class GraphicDrone extends MarkerInfo {
    private double droneLocationLat = 181, droneLocationLng  = 181, droneYaw = 0;


	public GraphicDrone() {

	}

	public synchronized void setGraphicDroneLat(double droneLocationLat){
        this.droneLocationLat = droneLocationLat;
    }

    public synchronized void setGraphicDroneLon(double droneLocationLng){
        this.droneLocationLng = droneLocationLng;
    }

    public void setYaw(double droneYaw){
        this.droneYaw = droneYaw;
    }

	@Override
	public float getAnchorU() {
		return 0.5f;
	}

	@Override
	public float getAnchorV() {
		return 0.5f;
	}

	@Override
	public LatLong getPosition() {
        LatLong droneGps = new LatLong(droneLocationLat, droneLocationLng);
        return isValid() ? droneGps : null;
	}

	@Override
	public Bitmap getIcon(Resources res) {
		BaseProduct product = DroidPlannerApp.getProductInstance();
		if (product != null && product.isConnected()) {
			return BitmapFactory.decodeResource(res, R.drawable.quad);
		}
		return BitmapFactory.decodeResource(res, R.drawable.quad_disconnect);

	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isFlat() {
		return true;
	}

	@Override
	public float getRotation() {
        //Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
		//Attitude attitude = drone.getAttribute(AttributeType.ATTITUDE);
		return (float) droneYaw;
	}

	public boolean isValid() {
        //Gps droneGps = drone.getAttribute(AttributeType.GPS);

        return checkGpsCoordination(droneLocationLat, droneLocationLng);
	}

	private static boolean checkGpsCoordination(double latitude, double longitude) {
        if (!Double.isNaN(latitude) && !Double.isNaN(longitude)){
            return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
        }
        else{
            return false;
        }

	}

}
