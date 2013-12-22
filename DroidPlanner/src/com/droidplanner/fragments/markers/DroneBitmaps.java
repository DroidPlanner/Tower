package com.droidplanner.fragments.markers;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.MAVLink.Messages.enums.MAV_TYPE;
import com.droidplanner.R.drawable;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class DroneBitmaps {

	public static final int DRONE_MIN_ROTATION = 5;
	private BitmapDescriptor[] droneBitmaps;
	private Resources resources;

	public DroneBitmaps(Resources resources, int type) {
		this.resources = resources;
		buildBitmaps(type);
	}

	public BitmapDescriptor getIcon(double yaw) {
		int index = (int) (yaw / DRONE_MIN_ROTATION);
		return droneBitmaps[index];
	}

	private void buildBitmaps(int type) {
		int count = 360 / DRONE_MIN_ROTATION;
		droneBitmaps = new BitmapDescriptor[count];
		for (int i = 0; i < count; i++) {
			droneBitmaps[i] = generateIcon(i * DRONE_MIN_ROTATION, type);
		}
	}

	private BitmapDescriptor generateIcon(float heading, int type) {
		Bitmap planeBitmap = getBitmap(type);
		Matrix matrix = new Matrix();
		matrix.postRotate(heading);
		return BitmapDescriptorFactory.fromBitmap(Bitmap.createBitmap(
				planeBitmap, 0, 0, planeBitmap.getWidth(),
				planeBitmap.getHeight(), matrix, true));
	}

	private Bitmap getBitmap(int type) {
		switch (type) {
		case MAV_TYPE.MAV_TYPE_GROUND_ROVER:
			return BitmapFactory.decodeResource(resources, drawable.rover);
		case MAV_TYPE.MAV_TYPE_TRICOPTER:
		case MAV_TYPE.MAV_TYPE_QUADROTOR:
		case MAV_TYPE.MAV_TYPE_HEXAROTOR:
		case MAV_TYPE.MAV_TYPE_OCTOROTOR:
		case MAV_TYPE.MAV_TYPE_HELICOPTER:
			return BitmapFactory.decodeResource(resources, drawable.quad);
		case MAV_TYPE.MAV_TYPE_FIXED_WING:
		default:
			return BitmapFactory.decodeResource(resources, drawable.plane);
		}
	}
}
