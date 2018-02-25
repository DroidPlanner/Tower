package com.o3dr.android.client.utils.connection;

import java.nio.ByteBuffer;

/**
 * Provides updates about the connection.
 */
public interface IpConnectionListener {

    void onIpConnected();

    void onIpDisconnected();

    void onPacketReceived(ByteBuffer packetBuffer);
}
