package org.droidplanner.services.android.impl.mock;

import android.content.Context;
import android.os.Parcelable;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.HashMap;

/**
 * Mock implementation for the Drone object.
 * Created by Fredia Huya-Kouadio on 10/23/15.
 */
public abstract class MockDrone extends Drone {

    private final HashMap<String, DroneAttribute> mockAttributes = new HashMap<>();

    protected Action asyncAction;
    protected Action syncAction;

    /**
     * Creates a Drone instance.
     *
     * @param context Application context
     */
    public MockDrone(Context context) {
        super(context);
    }

    public Action getAsyncAction() {
        return asyncAction;
    }

    public Action getSyncAction() {
        return syncAction;
    }

    public void setAttribute(String attributeType, DroneAttribute attribute){
        mockAttributes.put(attributeType, attribute);
    }

    @Override
    public <T extends Parcelable> T getAttribute(String type) {
        return (T) mockAttributes.get(type);
    }


}
