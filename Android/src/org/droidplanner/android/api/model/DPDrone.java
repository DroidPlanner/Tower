package org.droidplanner.android.api.model;

import android.os.RemoteException;

import com.three_dr.services.android.lib.drone.property.Altitude;
import com.three_dr.services.android.lib.drone.property.Attitude;
import com.three_dr.services.android.lib.drone.property.Battery;
import com.three_dr.services.android.lib.drone.property.Gps;
import com.three_dr.services.android.lib.drone.property.Home;
import com.three_dr.services.android.lib.drone.property.Mission;
import com.three_dr.services.android.lib.drone.property.Parameters;
import com.three_dr.services.android.lib.drone.property.Speed;
import com.three_dr.services.android.lib.drone.property.State;

/**
 * Created by fhuya on 10/29/14.
 */
public class DPDrone {

    
    public Gps getGps()  {
        return null;
    }

    
    public State getState()  {
        return null;
    }

    
    public Parameters getParameters()  {
        return null;
    }

    
    public Speed getSpeed()  {
        return null;
    }

    
    public Attitude getAttitude()  {
        return null;
    }

    
    public Home getHome()  {
        return null;
    }

    
    public Battery getBattery()  {
        return null;
    }

    
    public Altitude getAltitude()  {
        return null;
    }

    
    public Mission getMission()  {
        return null;
    }

    public boolean isConnected(){
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void refreshParameters(){
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void writeParameters(Parameters parameters){
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void toggleConnectionState() {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
