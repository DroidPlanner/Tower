package com.droidplanner.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public abstract class TcpConnection extends MAVLinkConnection {
	private Socket socket;
	private BufferedOutputStream mavOut;
	private BufferedInputStream mavIn;

	private String serverIP;
	private int serverPort;

	public TcpConnection(Context context) {
		super(context);

	}

	@Override
	public void run() {
		super.run();

		try {
			getTCPStream();
			while (true) {
				iavailable = mavIn.read(readData);
				handleData();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendBuffer(byte[] buffer) {
		if (mavOut != null) {
			try {
				mavOut.write(buffer);
				mavOut.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void getTCPStream() throws UnknownHostException, IOException {
		Log.d("TCP Client", "TCP connection started at: " + serverIP + ":"
				+ serverPort);
		InetAddress serverAddr = InetAddress.getByName(serverIP);
		socket = new Socket(serverAddr, serverPort);
		mavOut = new BufferedOutputStream((socket.getOutputStream()));
		// receive the message which the server sends back
		mavIn = new BufferedInputStream(socket.getInputStream());
	}

	public void getPreferences(SharedPreferences prefs) {
		serverIP = prefs.getString("pref_server_ip", "");
		serverPort = Integer.parseInt(prefs.getString("pref_server_port", "0"));

	}

}
