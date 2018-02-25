package com.o3dr.android.client.utils.connection;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.droidplanner.services.android.impl.utils.NetworkUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by Fredia Huya-Kouadio on 2/18/15.
 */
public class UdpConnection extends AbstractIpConnection {

    private static final String TAG = UdpConnection.class.getSimpleName();

    private final int serverPort;
    private final int readTimeout;

    private DatagramSocket socket;
    private DatagramPacket sendPacket;
    private DatagramPacket receivePacket;

    private int hostPort;
    private InetAddress hostAddress;

    public UdpConnection(Handler handler, int serverPort, int readBufferSize) {
        this(handler, serverPort, readBufferSize, false, 0);
    }

    public UdpConnection(Handler handler, int serverPort, int readBufferSize, boolean polling, int readTimeout){
        super(handler, readBufferSize, polling);
        this.serverPort = serverPort;
        if(polling) {
            this.readTimeout = readTimeout > 0 ? readTimeout : 33;//millisecond
        }
        else {
            this.readTimeout = CONNECTION_TIMEOUT;
        }
    }

    public UdpConnection(Handler handler, String address, int hostPort, int serverPort) throws UnknownHostException {
        super(handler, false, true);
        this.serverPort = serverPort;
        this.hostPort = hostPort;
        hostAddress = InetAddress.getByName(address);
        readTimeout = CONNECTION_TIMEOUT;
    }

    @Override
    protected void open(Bundle extras) throws IOException {
        Log.d(TAG, "Opening udp connection.");

        socket = (serverPort == -1) ?new DatagramSocket() : new DatagramSocket(serverPort);
        socket.setBroadcast(true);
        socket.setReuseAddress(true);
        socket.setSoTimeout(readTimeout);
        NetworkUtils.bindSocketToNetwork(extras, socket);
    }

    @Override
    protected int read(ByteBuffer buffer) throws IOException {
        if (receivePacket == null)
            receivePacket = new DatagramPacket(buffer.array(), buffer.capacity());

        socket.receive(receivePacket);
        hostAddress = receivePacket.getAddress();
        hostPort = receivePacket.getPort();
        return receivePacket.getLength();
    }

    @Override
    protected void send(PacketData data) throws IOException {
        if (hostAddress != null) {
            if (sendPacket == null) {
                sendPacket = new DatagramPacket(data.data, data.dataLength, hostAddress, hostPort);
            } else {
                sendPacket.setData(data.data, 0, data.dataLength);
                sendPacket.setAddress(hostAddress);
                sendPacket.setPort(hostPort);
            }

            socket.send(sendPacket);
        } else {
            Log.w(TAG, "Still awaiting connection from remote host.");
        }
    }

    @Override
    protected void close() throws IOException {
        Log.d(TAG, "Closing udp connection.");
        if (socket != null)
            socket.close();
    }
}
