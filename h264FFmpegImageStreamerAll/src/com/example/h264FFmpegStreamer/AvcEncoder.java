package com.example.h264FFmpegStreamer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import com.android.myffmpegx264lib.imageEncoder;

public class AvcEncoder {

	public imageEncoder encoder;

	private static final String TAG = "com.example.h264ImageStreamer";

	// networking variables
	private int DATAGRAM_PORT = 4003;
	private static final int MAX_UDP_DATAGRAM_LEN = 1400;
	byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];
	byte[] sendData = new byte[MAX_UDP_DATAGRAM_LEN];
	DatagramPacket dp;
	DatagramSocket ds;
	InetAddress clientIp;
	int clientPort;
	int qCount = 0;

	private LinkedBlockingDeque<byte[]> udpQueue = new LinkedBlockingDeque<byte[]>();

	public AvcEncoder() {

		Log.i("AvcEncoder", "outputStream initialized");
		Thread udpThread = new Thread() {

			@Override
			public void run() {
				try {
					ds = new DatagramSocket(DATAGRAM_PORT);
					dp = new DatagramPacket(lMsg, lMsg.length);
					ds.receive(dp);
					clientPort = dp.getPort();
					clientIp = dp.getAddress();
					ds.connect(dp.getAddress(), dp.getPort());
					Log.i("AvcEncoder", " Connected to: " + clientIp + ":"
							+ clientPort);

					while (true) {
						
						byte[] outData = udpQueue.pollFirst(1000,
								TimeUnit.MILLISECONDS);
						if (outData == null)
							continue;
						
						byte[] buff = new byte[1400];
						int c = 0, rCounter = 0;

						for (int i = 0; i < outData.length; i++) {

							buff[c] = outData[i];
							c++;

							if (i == outData.length - 1) {
								try {
									DatagramPacket packet = new DatagramPacket(
											buff, outData.length - rCounter
													* 1400, clientIp,
											clientPort);

									ds.send(packet);
									// Log.i("AvcEncoder", outData.length -
									// mCounter
									// * 1400 + " bytes written");
								} catch (Exception e) {
									Log.i("AvcEncoder", "Missed packet");
								}
							}

							else if (i % 1399 == 0 && i != 0) {
								rCounter++;
								DatagramPacket packet = new DatagramPacket(
										buff, buff.length, clientIp, clientPort);
								buff = new byte[1400];
								c = 0;
								try {
									ds.send(packet);
								} catch (Exception e) {
									// Log.i("AvcEncoder", "Missed packet");

								}
							} else {
							}
						}
					}

				} catch (IOException e) { // TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		udpThread.start();
	}

	public void close() {
		try {

			encoder.closeImageEncoder();
			encoder.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// called from Camera.setPreviewCallbackWithBuffer(...) in other class

	public void setFFmpegEncoder() {
		encoder = new imageEncoder();
	}

	public void encodeWithFFmpeg(byte[] data, int counter) {

		byte[] outBytes = new byte[1000000];
		int[] outImageSize = new int[1];
		encoder.encodeImage(data, data.length, counter, outBytes, outImageSize);
		byte[] outData = new byte[outImageSize[0]];
		for (int i = 0; i < outImageSize[0]; i++)
			outData[i] = outBytes[i];
		udpQueue.add(outData);
	}
}
