package com.droidplanner.fragments;

import android.app.Fragment;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.widgets.HUD.HUD;

public class HudFragment extends Fragment {

	private HUD hudWidget;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.hud_fragment, container, false);
		hudWidget = (HUD) view.findViewById(R.id.hudWidget);
		hudWidget.setDrone(((SuperActivity) getActivity()).app.drone);
		hudWidget.onDroneUpdate();
		hudWidget.getHolder().setFormat(PixelFormat.TRANSPARENT);
		hudWidget.setZOrderMediaOverlay(true);

		SurfaceView videoSurface = (SurfaceView) view.findViewById(R.id.videoSurface);
		videoSurface.getHolder().addCallback(new Callback() {
			
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				MediaPlayer mp = new MediaPlayer();
				mp.setSurface(holder.getSurface());
				try {
					mp.setDataSource("http://www.pocketjourney.com/downloads/pj/video/famous.3gp");
					mp.prepare();
					mp.start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		return view;
	}

}
