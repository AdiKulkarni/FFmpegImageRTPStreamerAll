package com.example.h264FFmpegStreamer;

import java.io.IOException;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.example.h264codecstreamer.R;

public class MainStreamerActivity extends Activity implements
		SurfaceHolder.Callback {

	Camera camera;
	SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean previewing = false;
	long currTime, oldTime = 0;

	public int mCount = 0;
	AvcEncoder avcEncode = new AvcEncoder();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button buttonStartCameraPreview = (Button) findViewById(R.id.startcamerapreview);
		Button buttonStopCameraPreview = (Button) findViewById(R.id.stopcamerapreview);

		getWindow().setFormat(PixelFormat.UNKNOWN);
		surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		buttonStartCameraPreview
				.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						if (!previewing) {
							camera = Camera.open();
							if (camera != null) {
								try {
									camera.setPreviewDisplay(surfaceHolder);
									Parameters parameters = camera
											.getParameters();
									parameters
											.setPreviewFormat(ImageFormat.YV12);
									final int previewFormat = parameters
											.getPreviewFormat();
									parameters.setPreviewFpsRange(4000, 30000);
									parameters.setPreviewSize(640, 480);
									parameters.setPreviewFrameRate(15);
									parameters
											.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
									camera.setParameters(parameters);
									avcEncode.setFFmpegEncoder();
									camera.setPreviewCallback(new Camera.PreviewCallback() {
										@Override
										public void onPreviewFrame(
												byte[] bytes, Camera camera) {

											avcEncode.encodeWithFFmpeg(
													swapYV12toI420(bytes, 640,
															480), mCount++);
										}
									});

									camera.startPreview();
									previewing = true;

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				});

		buttonStopCameraPreview
				.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (camera != null && previewing) {
							camera.release();
							camera.stopPreview();
							camera = null;
							avcEncode.close();
							previewing = false;
							finish();
						}
					}
				});

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	public byte[] swapYV12toI420(byte[] yv12bytes, int width, int height) {
		byte[] i420bytes = new byte[yv12bytes.length];
		for (int i = 0; i < width * height; i++)
			i420bytes[i] = yv12bytes[i];
		for (int i = width * height; i < width * height
				+ (width / 2 * height / 2); i++)
			i420bytes[i] = yv12bytes[i + (width / 2 * height / 2)];
		for (int i = width * height + (width / 2 * height / 2); i < width
				* height + 2 * (width / 2 * height / 2); i++)
			i420bytes[i] = yv12bytes[i - (width / 2 * height / 2)];
		return i420bytes;
	}

	public static byte[] YV12toYUV420Planar(byte[] input, int width, int height) {
		/*
		 * COLOR_FormatYUV420Planar is I420 which is like YV12, but with U and V
		 * reversed. So we just have to reverse U and V.
		 */
		byte[] output = new byte[input.length];
		;
		final int frameSize = width * height;
		final int qFrameSize = frameSize / 4;

		System.arraycopy(input, 0, output, 0, frameSize); // Y
		System.arraycopy(input, frameSize, output, frameSize + qFrameSize,
				qFrameSize); // Cr (V)
		System.arraycopy(input, frameSize + qFrameSize, output, frameSize,
				qFrameSize); // Cb (U)

		return output;
	}
}