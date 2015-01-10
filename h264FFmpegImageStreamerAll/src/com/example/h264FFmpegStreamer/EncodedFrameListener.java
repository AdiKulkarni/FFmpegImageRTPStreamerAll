package com.example.h264FFmpegStreamer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.util.Log;

public class EncodedFrameListener {

	private int DATAGRAM_PORT = 4003;
	private static final int MAX_UDP_DATAGRAM_LEN = 1024;
	byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];
	byte[] sendData = new byte[MAX_UDP_DATAGRAM_LEN];
	DatagramPacket dp;
	DatagramSocket ds;

	public EncodedFrameListener() {
		Thread udpThread = new Thread() {

			@Override
			public void run() {
				try {
					ds = new DatagramSocket(DATAGRAM_PORT);
					dp = new DatagramPacket(lMsg, lMsg.length);
					ds.receive(dp);
					ds.connect(dp.getAddress(), dp.getPort());
					Log.i("AvcEncoder", " Connected to: " + dp.getAddress()
							+ ":" + dp.getPort());

				} catch (IOException e) { // TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		udpThread.start();
	}

	public void frameReceived(byte[] data, int pos, int length) {

		byte[] buff = new byte[1400];
		int c = 0, mCounter = 0;
		for (int i = 0; i < data.length; i++) {
			buff[c] = data[i];
			c++;
			if (i % 1399 == 0 && i != 0) {
				mCounter++;
				DatagramPacket packet = new DatagramPacket(buff, buff.length,
						dp.getAddress(), dp.getPort());
				buff = new byte[1400];
				c = 0;

				try {
					ds.send(packet);
				} catch (Exception e) {
					Log.i("AvcEncoder", "Missed packet");

				}
			}

			if (i == data.length - 1) {
				DatagramPacket packet = new DatagramPacket(buff, data.length
						- mCounter * 1400, dp.getAddress(), dp.getPort());
				try {
					ds.send(packet);
					Log.i("AvcEncoder", data.length - mCounter * 1400
							+ " bytes written");
				} catch (Exception e) {
					Log.i("AvcEncoder", "Missed packet");
				}
			}
		}
	}
	
	public void stop(){
		ds.close();
	}

}
