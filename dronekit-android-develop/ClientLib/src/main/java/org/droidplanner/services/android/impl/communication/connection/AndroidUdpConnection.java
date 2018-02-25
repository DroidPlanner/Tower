package org.droidplanner.services.android.impl.communication.connection;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;

import org.droidplanner.services.android.impl.core.MAVLink.connection.UdpConnection;
import org.droidplanner.services.android.impl.core.model.Logger;
import org.droidplanner.services.android.impl.utils.connection.WifiConnectionHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AndroidUdpConnection extends AndroidIpConnection {

    private static final String TAG = AndroidUdpConnection.class.getSimpleName();

    private final HashSet<PingTask> pingTasks = new HashSet<>();

    private final UdpConnection mConnectionImpl;
    private final int serverPort;

    private ScheduledExecutorService pingRunner;

    public AndroidUdpConnection(Context context, int udpServerPort, WifiConnectionHandler wifiHandler) {
        super(context, wifiHandler);
        this.serverPort = udpServerPort;

        mConnectionImpl = new UdpConnection(context) {
            @Override
            protected int loadServerPort() {
                return serverPort;
            }

            @Override
            protected Logger initLogger() {
                return AndroidUdpConnection.this.initLogger();
            }

            @Override
            protected void onConnectionOpened(Bundle extras) {
                AndroidUdpConnection.this.onConnectionOpened(extras);
            }

            @Override
            protected void onConnectionStatus(LinkConnectionStatus connectionStatus) {
                AndroidUdpConnection.this.onConnectionStatus(connectionStatus);
            }
        };
    }

    public AndroidUdpConnection(Context context, int udpServerPort) {
        this(context, udpServerPort, null);
    }

    public void addPingTarget(final InetAddress address, final int port, final long period, final byte[] payload) {
        if (address == null || payload == null || period <= 0)
            return;

        final PingTask pingTask = new PingTask(address, port, period, payload);

        pingTasks.add(pingTask);

        if (getConnectionStatus() == AndroidMavLinkConnection.MAVLINK_CONNECTED && pingRunner != null && !pingRunner.isShutdown())
            pingRunner.scheduleWithFixedDelay(pingTask, 0, period, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onCloseConnection() throws IOException {
        Log.d(TAG, "Closing udp connection.");
        if (pingRunner != null) {
            Log.d(TAG, "Shutting down pinging tasks.");
            pingRunner.shutdownNow();
            pingRunner = null;
        }

        mConnectionImpl.closeConnection();
    }

    @Override
    protected void loadPreferences() {
        mConnectionImpl.loadPreferences();
    }

    @Override
    protected void onOpenConnection(Bundle extras) throws IOException {
        Log.d(TAG, "Opening udp connection");
        mConnectionImpl.openConnection(extras);

        if (pingRunner == null || pingRunner.isShutdown())
            pingRunner = Executors.newSingleThreadScheduledExecutor();

        for (PingTask pingTask : pingTasks)
            pingRunner.scheduleWithFixedDelay(pingTask, 0, pingTask.period, TimeUnit.MILLISECONDS);
    }

    @Override
    protected int readDataBlock(byte[] buffer) throws IOException {
        return mConnectionImpl.readDataBlock(buffer);
    }

    @Override
    protected void sendBuffer(byte[] buffer) throws IOException {
        mConnectionImpl.sendBuffer(buffer);
    }

    @Override
    public int getConnectionType() {
        return mConnectionImpl.getConnectionType();
    }

    private class PingTask implements Runnable {

        private final InetAddress address;
        private final int port;
        private final long period;
        private final byte[] payload;

        private PingTask(InetAddress address, int port, long period, byte[] payload) {
            this.address = address;
            this.port = port;
            this.period = period;
            this.payload = payload;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;

            if (!(other instanceof PingTask))
                return false;

            PingTask that = (PingTask) other;
            return this.address.equals(that.address) && this.port == that.port && this.period == that.period;
        }

        @Override
        public int hashCode(){
            return toString().hashCode();
        }

        @Override
        public void run() {
            try {
                mConnectionImpl.sendBuffer(address, port, payload);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred while sending ping message.", e);
            }
        }

        @Override
        public String toString(){
            return "[" + address.toString() + "; " + port + "; " + period + "]";
        }
    }
}
