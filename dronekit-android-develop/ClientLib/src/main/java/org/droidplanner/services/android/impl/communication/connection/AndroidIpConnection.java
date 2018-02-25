package org.droidplanner.services.android.impl.communication.connection;

import android.content.Context;
import android.os.Bundle;

import org.droidplanner.services.android.impl.utils.connection.WifiConnectionHandler;

import java.io.IOException;

/**
 * Created by fredia on 3/28/16.
 */
public abstract class AndroidIpConnection extends AndroidMavLinkConnection {

    private final WifiConnectionHandler wifiHandler;

    protected AndroidIpConnection(Context context, WifiConnectionHandler wifiHandler){
        super(context);
        this.wifiHandler = wifiHandler;
    }

    @Override
    protected final void openConnection(Bundle connectionExtras) throws IOException {
        if(this.wifiHandler != null) {
            this.wifiHandler.start();
        }
        onOpenConnection(connectionExtras);
    }

    protected abstract void onOpenConnection(Bundle extras) throws IOException;

    @Override
    protected final void closeConnection() throws IOException {
        onCloseConnection();
        if(this.wifiHandler != null) {
            this.wifiHandler.stop();
        }
    }

    protected abstract void onCloseConnection() throws IOException;

}
