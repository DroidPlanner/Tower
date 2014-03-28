package org.droidplanner.android.communication.connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.SharedPreferences;

public class TcpConnection extends MAVLinkConnection {
	private Socket socket;
	private BufferedOutputStream mavOut;
	private BufferedInputStream mavIn;

	private String serverIP;
	private int serverPort;

	public TcpConnection(Context context) {
		super(context);
	}

	@Override
	protected void openConnection() throws UnknownHostException, IOException {
		getTCPStream();
	}

	@Override
	protected void readDataBlock() throws IOException {
		iavailable = mavIn.read(readData);
	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException {
		if (mavOut != null) {
			mavOut.write(buffer);
			mavOut.flush();
		}
	}

	@Override
	protected void closeConnection() throws IOException {
		socket.close();
	}

	@Override
	protected void getPreferences(SharedPreferences prefs) {
		serverIP = prefs.getString("pref_server_ip", "");
		serverPort = Integer.parseInt(prefs.getString("pref_server_port", "0"));

	}

	private void getTCPStream() throws UnknownHostException, IOException {
		InetAddress serverAddr = InetAddress.getByName(serverIP);
		socket = new Socket(serverAddr, serverPort);
		mavOut = new BufferedOutputStream((socket.getOutputStream()));
		mavIn = new BufferedInputStream(socket.getInputStream());
	}

}
