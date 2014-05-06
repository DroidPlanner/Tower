package org.droidplanner.android.communication.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public class UdpConnection extends MAVLinkConnection {

	private DatagramSocket socket;
	private int serverPort;

	private int hostPort;
	private InetAddress hostAdd = null;

	public UdpConnection(Context context) {
		super(context);
	}

	@Override
	protected void openConnection() throws UnknownHostException, IOException {
		getUdpStream();
	}

	@Override
	protected void readDataBlock() throws IOException {
		DatagramPacket packet = new DatagramPacket(readData, readData.length);
		socket.receive(packet);
		hostAdd = packet.getAddress();
		hostPort = packet.getPort();
		iavailable = packet.getLength();
	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException {
		new UdpSender().execute(buffer);
	}

	private class UdpSender extends AsyncTask<byte[], Integer, Integer> {

		@Override
		protected Integer doInBackground(byte[]... params) {
			try {
				byte[] buffer = params[0];
                if (hostAdd != null) {  // We can't send to our sister until they have connected to us
				    DatagramPacket packet = new DatagramPacket(buffer,
						buffer.length, hostAdd, hostPort);
				    socket.send(packet);
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	protected void closeConnection() throws IOException {
		socket.close();
	}

	@Override
	protected void getPreferences(SharedPreferences prefs) {
		serverPort = Integer.parseInt(prefs.getString("pref_udp_server_port",
				"14550"));
	}

	private void getUdpStream() throws UnknownHostException, IOException {
		socket = new DatagramSocket(serverPort);
		socket.setBroadcast(true);
		socket.setReuseAddress(true);
	}

}
