package org.droidplanner.core.gcs.location;

import android.location.Location;

public interface LocationReceiver {
	public void onLocationChanged(Location location);
}
