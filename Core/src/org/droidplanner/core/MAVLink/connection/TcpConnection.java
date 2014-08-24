package org.droidplanner.core.MAVLink.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Provides support for mavlink connection via TCP.
 */
public abstract class TcpConnection extends MavLinkConnection {

    private Socket socket;
    private BufferedOutputStream mavOut;
    private BufferedInputStream mavIn;

    private String serverIP;
    private int serverPort;

    @Override
    protected final void openConnection() throws IOException {
        getTCPStream();
    }

    @Override
    protected final int readDataBlock(byte[] buffer) throws IOException {
        return mavIn.read(buffer);
    }

    @Override
    protected final void sendBuffer(byte[] buffer) throws IOException {
        if (mavOut != null) {
            mavOut.write(buffer);
            mavOut.flush();
        }
    }

    @Override
    protected final void loadPreferences(){
        serverIP = loadServerIP();
        serverPort = loadServerPort();
    }

    protected abstract int loadServerPort();

    protected abstract String loadServerIP();

    @Override
    protected final void closeConnection() throws IOException {
        socket.close();
    }

    private void getTCPStream() throws IOException {
        InetAddress serverAddr = InetAddress.getByName(serverIP);
        socket = new Socket(serverAddr, serverPort);
        mavOut = new BufferedOutputStream((socket.getOutputStream()));
        mavIn = new BufferedInputStream(socket.getInputStream());
    }

    @Override
    public final int getConnectionType(){
        return MavLinkConnectionTypes.MAVLINK_CONNECTION_TCP;
    }
}
