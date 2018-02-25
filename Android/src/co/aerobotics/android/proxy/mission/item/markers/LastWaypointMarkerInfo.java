package co.aerobotics.android.proxy.mission.item.markers;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import co.aerobotics.android.R;
import co.aerobotics.android.maps.MarkerInfo;
import com.o3dr.services.android.lib.coordinate.LatLong;

/**
 * Created by root on 2017/07/04.
 */

public class LastWaypointMarkerInfo extends MarkerInfo {

    private LatLong mPoint;

    public LastWaypointMarkerInfo(LatLong mPoint){
        this.mPoint = mPoint;
    }

    @Override
    public Bitmap getIcon(Resources res) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(res, R.drawable.checkered_flag_cropped);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 50, 50, false);
        return resizedBitmap;
    }

    @Override
    public float getRotation(){
        return 0;
    }

    @Override
    public float getAnchorU() {
        return 0.2f;
    }

    @Override
    public float getAnchorV() {
        return 1;
    }

    @Override
    public LatLong getPosition() {
        return mPoint;
    }

    public void setPosition(LatLong point){
        mPoint = point;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public boolean isFlat() {
        return false;
    }
}
