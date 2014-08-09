package org.droidplanner.android.gcs.location;

import android.location.Location;

public interface LocationReceiver {
	public void onLocationChanged(Location location);
}
