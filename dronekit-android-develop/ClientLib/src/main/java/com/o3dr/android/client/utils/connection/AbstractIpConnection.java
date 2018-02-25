package com.o3dr.android.client.utils.connection;

import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.o3dr.services.android.lib.model.ICommandListener;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base class for ip connection (tcp, udp).
 */
public abstract class AbstractIpConnection {

    private static final String TAG = AbstractIpConnection.class.getSimpleName();

    public static final int CONNECTION_TIMEOUT = 15 * 1000; //5 seconds

    /*
    Connection state
     */
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    /**
     * Size of the buffer used to read messages from the connection.
     */
    private static final int DEFAULT_READ_BUFFER_SIZE = 4096;

    private IpConnectionListener ipConnectionListener;

    /**
     * Queue the set of packets to send.
     * A thread will be blocking on it until there's element(s) available to send.
     */
    private final LinkedBlockingQueue<PacketData> packetsToSend = new LinkedBlockingQueue<>();

    private final AtomicInteger connectionStatus = new AtomicInteger(STATE_DISCONNECTED);
    private final AtomicReference<Bundle> extrasHolder = new AtomicReference<>();

    private final boolean isSendingDisabled;
    private final boolean isReadingDisabled;

    private final ByteBuffer readBuffer;

    private final Runnable managerTask = new Runnable() {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);

            Thread sendingThread = null;

            try {
                try {
                    open(extrasHolder.get());
                    connectionStatus.set(STATE_CONNECTED);
                    if(ipConnectionListener != null)
                        ipConnectionListener.onIpConnected();
                } catch (IOException e) {
                    Log.e(TAG, "Unable to open ip connection.", e);
                    return;
                }

                if(!isSendingDisabled) {
                    //Launch the packet dispatching thread
                    sendingThread = new Thread(sendingTask, "IP Connection-Sending Thread");
                    sendingThread.start();
                }

                if(!isReadingDisabled) {
                    try {
                        while (connectionStatus.get() == STATE_CONNECTED) {
                            readBuffer.clear();
                            try {
                                int packetSize = read(readBuffer);
                                if (packetSize > 0) {
                                    readBuffer.limit(packetSize);

                                    if (ipConnectionListener != null) {
                                        readBuffer.rewind();
                                        ipConnectionListener.onPacketReceived(readBuffer);
                                    }
                                }
                            }catch(InterruptedIOException e){
                                if(!isPolling)
                                    throw e;
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error occurred while reading from the connection.", e);
                    }
                }
                else if(sendingThread != null){
                    try {
                        sendingThread.join();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Error while waiting for sending thread to complete.", e);
                    }
                }
            }
            finally{
                if(sendingThread != null && sendingThread.isAlive())
                    sendingThread.interrupt();

                disconnect();
                Log.i(TAG, "Exiting connection manager thread.");
            }
        }
    };

    /**
     * Blocks until there's packet(s) to send, then dispatch them.
     */
    private final Runnable sendingTask = new Runnable() {
        @Override
        public void run() {
            try{
                while(connectionStatus.get() == STATE_CONNECTED){
                    final PacketData packetData = packetsToSend.take();
                    final ICommandListener listener = packetData.listener;

                    try {
                        send(packetData);
                        postSendSuccess(listener);
                    } catch (IOException e) {
                        Log.e(TAG, "Error occurred while sending packet.", e);
                        postSendTimeout(listener);
                    }
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Dispatching thread was interrupted.", e);
            }
            finally{
                disconnect();
                Log.i(TAG, "Exiting packet dispatcher thread.");
            }
        }

        private void postSendSuccess(final ICommandListener listener){
            if(handler == null || listener == null)
                return;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onSuccess();
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            });
        }

        private void postSendTimeout(final ICommandListener listener){
            if(handler == null || listener == null)
                return;

            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listener.onTimeout();
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            });
        }
    };

    private final boolean isPolling;
    private final Handler handler;

    private Thread managerThread;

    public AbstractIpConnection(Handler handler){
        this(handler, false, false);
    }

    public AbstractIpConnection(Handler handler, int readBufferSize, boolean isPolling){
        this(handler, readBufferSize, false, false, isPolling);
    }

    public AbstractIpConnection(Handler handler, boolean disableSending, boolean disableReading){
        this(handler, DEFAULT_READ_BUFFER_SIZE, disableSending, disableReading, false);
    }

    public AbstractIpConnection(Handler handler, int readBufferSize, boolean disableSending, boolean disableReading, boolean isPolling){
        this.handler = handler;
        this.readBuffer = ByteBuffer.allocate(readBufferSize);
        isReadingDisabled = disableReading;
        isSendingDisabled = disableSending;
        this.isPolling = isPolling;
    }

    protected abstract void open(Bundle extras) throws IOException;

    protected abstract int read(ByteBuffer buffer) throws IOException;

    protected abstract void send(PacketData data) throws IOException;

    protected abstract void close() throws IOException;

    /**
     * Establish an ip connection. If successful, ConnectionListener#onIpConnected() is called.
     * @param extras
     */
    public void connect(Bundle extras){
        if(connectionStatus.compareAndSet(STATE_DISCONNECTED, STATE_CONNECTING)){
            Log.i(TAG, "Starting manager thread.");
            extrasHolder.set(extras);
            managerThread = new Thread(managerTask, "IP Connection-Manager Thread");
            managerThread.setPriority(Thread.MAX_PRIORITY);
            managerThread.start();
        }
    }

    public Bundle getConnectionExtras(){
        return extrasHolder.get();
    }

    /**
     * Disconnect an existing ip connection. If successful, ConnectionListener#onIpDisconnected() is called.
     */
    public void disconnect(){
        if(connectionStatus.get() == STATE_DISCONNECTED || managerThread == null)
            return;

        connectionStatus.set(STATE_DISCONNECTED);
        if(managerThread != null && managerThread.isAlive() && !managerThread.isInterrupted()){
            managerThread.interrupt();
        }

        try {
            close();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred while closing ip connection.", e);
        }

        if(ipConnectionListener != null)
            ipConnectionListener.onIpDisconnected();
    }

    public void setIpConnectionListener(IpConnectionListener ipConnectionListener) {
        this.ipConnectionListener = ipConnectionListener;
    }

    public void sendPacket(byte[] packet, int packetSize, ICommandListener listener){
        if(packet == null || packetSize <= 0)
            return;

        packetsToSend.offer(new PacketData(packetSize, packet, listener));
    }

    public int getConnectionStatus(){
        return connectionStatus.get();
    }

    protected static final class PacketData {
        public final int dataLength;
        public final byte[] data;
        public final ICommandListener listener;

        public PacketData(int dataLength, byte[] data, ICommandListener listener) {
            this.dataLength = dataLength;
            this.data = data;
            this.listener = listener;
        }
    }
}
