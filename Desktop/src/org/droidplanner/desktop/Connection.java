package org.droidplanner.desktop;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import com.MAVLink.Parser;

public class Connection {
	public int localPort = 14550;
	public InetAddress hostAdd = null;
	public int hostPort;
	public byte[] receiveData = new byte[1024];
	public byte[] sendBuffer = new byte[1024];
	public Parser parser = new Parser();
	public DatagramSocket socket;
	public int length;

	public Connection(int port) {
		localPort = port;
	}

	public byte[] readDataBlock() throws IOException {
		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);
		socket.receive(receivePacket);
		length = receivePacket.getLength();
		hostAdd = receivePacket.getAddress();
		hostPort = receivePacket.getPort();
		return receivePacket.getData();
	}
	
	public void getUdpStream() throws SocketException {
		socket = new DatagramSocket(localPort);
		socket.setBroadcast(true);
		socket.setReuseAddress(true);
	}

	public void sendBuffer(byte[] buffer) {
		try {
			if (hostAdd != null) { // Need to have received at least
									// one
									// packet
				DatagramPacket udpPacket = new DatagramPacket(buffer,
						buffer.length, hostAdd, hostPort);
				socket.send(udpPacket);
				System.out.println("sending: "
						+ Arrays.toString(udpPacket.getData()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}