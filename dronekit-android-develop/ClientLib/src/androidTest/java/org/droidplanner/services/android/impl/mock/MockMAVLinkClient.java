package org.droidplanner.services.android.impl.mock;

import android.content.Context;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.model.ICommandListener;

import org.droidplanner.services.android.impl.communication.model.DataLink;
import org.droidplanner.services.android.impl.communication.service.MAVLinkClient;


/**
 * Created by Fredia Huya-Kouadio on 12/17/15.
 */
public class MockMAVLinkClient extends MAVLinkClient {

    private MAVLinkPacket data;

    public MockMAVLinkClient(Context context, DataLink.DataLinkListener listener, ConnectionParameter connParams) {
        super(context, listener, connParams, null);
    }

    public MAVLinkPacket getData() {
        return data;
    }

    @Override
    protected void sendMavMessage(MAVLinkMessage message, int sysId, int compId, ICommandListener listener){
        data = message.pack();
    }
}
