package org.droidplanner.services.android.impl.communication.model;

import android.os.Bundle;

import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.model.ICommandListener;

public class DataLink {

    public interface DataLinkProvider<T> {

        void sendMessage(T message, ICommandListener listener);

        boolean isConnected();

        void openConnection();

        void closeConnection();

        Bundle getConnectionExtras();

    }

    public interface DataLinkListener<T> {

        void notifyReceivedData(T packet);

        void onConnectionStatus(LinkConnectionStatus connectionStatus);
    }
}
