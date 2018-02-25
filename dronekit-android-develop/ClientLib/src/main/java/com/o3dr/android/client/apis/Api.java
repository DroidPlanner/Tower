package com.o3dr.android.client.apis;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.model.AbstractCommandListener;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Common interface for the drone set of api classes.
 * Created by Fredia Huya-Kouadio on 7/5/15.
 */
public abstract class Api {

    protected interface Builder<T extends Api> {
        T build(Drone drone);
    }

    /**
     * Retrieves the api instance bound to the given Drone object.
     * @param drone Drone object
     * @param apiCache Used to retrieve the api instance if it exists, or store it if it doesn't exist.
     * @param apiBuilder Api instance generator.
     * @param <T> Specific api instance type.
     * @return The matching Api instance.
     */
    protected static <T extends Api> T getApi(Drone drone, ConcurrentHashMap<Drone, T> apiCache, Api.Builder<T> apiBuilder){
        if(drone == null || apiCache == null)
            return null;

        T apiInstance = apiCache.get(drone);
        if(apiInstance == null && apiBuilder != null){
            apiInstance = apiBuilder.build(drone);
            final T previousInstance = apiCache.putIfAbsent(drone, apiInstance);
            if(previousInstance != null)
                apiInstance = previousInstance;
        }

        return apiInstance;
    }

    protected static void postSuccessEvent(final AbstractCommandListener listener){
        if(listener != null){
            listener.onSuccess();
        }
    }

    protected static void postErrorEvent(int error, AbstractCommandListener listener){
        if(listener != null){
            listener.onError(error);
        }
    }

    protected static void postTimeoutEvent(AbstractCommandListener listener){
        if(listener != null){
            listener.onTimeout();
        }
    }
}
