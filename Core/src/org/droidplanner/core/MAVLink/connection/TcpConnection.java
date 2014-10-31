package org.droidplanner.core.MAVLink.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Provides support for mavlink connection via TCP.
 */
public abstract class TcpConnection extends MavLinkConnection {

	private static final int CONNECTION_TIMEOUT = 20 * 1000; // 20 secs in ms

	private Socket socket;
	private BufferedOutputStream mavOut;
	private BufferedInputStream mavIn;

	private String serverIP;
	private int serverPort;

	@Override
	public final void openConnection() throws IOException {
		getTCPStream();
	}

	@Override
	public final int readDataBlock(byte[] buffer) throws IOException {
		return mavIn.read(buffer);
	}

	@Override
	public final void sendBuffer(byte[] buffer) throws IOException {
		if (mavOut != null) {
			mavOut.write(buffer);
			mavOut.flush();
		}
	}

	@Override
	public final void loadPreferences() {
		serverIP = loadServerIP();
		serverPort = loadServerPort();
	}

	protected abstract int loadServerPort();

	protected abstract String loadServerIP();

	@Override
	public final void closeConnection() throws IOException {
		if (socket != null)
			socket.close();
	}

	private void getTCPStream() throws IOException {
		InetAddress serverAddr = InetAddress.getByName(serverIP);
		socket = new Socket();
		socket.connect(new InetSocketAddress(serverAddr, serverPort), CONNECTION_TIMEOUT);
		mavOut = new BufferedOutputStream((socket.getOutputStream()));
		mavIn = new BufferedInputStream(socket.getInputStream());
	}

	@Override
	public final int getConnectionType() {
		return MavLinkConnectionTypes.MAVLINK_CONNECTION_TCP;
	}
}
