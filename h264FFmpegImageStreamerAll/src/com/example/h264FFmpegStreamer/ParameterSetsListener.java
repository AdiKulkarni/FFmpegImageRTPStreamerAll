package com.example.h264FFmpegStreamer;

import android.util.Log;

public class ParameterSetsListener {

	ParameterSetsListener(){
	}
	public void avcParametersSetsEstablished(byte[] x, byte[] y){
		Log.i("AvcEncoder", "SPS: " + x.toString() + "PPS: "+y.toString());
	}
}
