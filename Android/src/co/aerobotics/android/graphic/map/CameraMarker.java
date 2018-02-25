package co.aerobotics.android.graphic.map;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import co.aerobotics.android.R;

import co.aerobotics.android.maps.MarkerInfo;
import com.google.android.gms.maps.model.LatLng;
import com.o3dr.services.android.lib.coordinate.LatLong;

/**
 * Created by michaelwootton on 8/29/17.
 */

public class CameraMarker extends MarkerInfo {
    private LatLng position;
    private float rotation = 0f;

    public CameraMarker (LatLng position, float rotation){
        this.position = position;
        this.rotation = rotation;
    }

    @Override
    public Bitmap getIcon(Resources res) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_camera_circle_blue);
        //Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 40, 40, false);
        return imageBitmap;
    }

    @Override
    public LatLong getPosition() {
        return new LatLong(position.latitude, position.longitude);
    }

    @Override
    public boolean isDraggable(){
        return false;
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
    public float getRotation(){
        return rotation;
    }

}
