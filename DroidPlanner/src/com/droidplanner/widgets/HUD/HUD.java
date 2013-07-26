package com.droidplanner.widgets.HUD;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.HudUpdatedListner;

/**
 * Widget for a HUD Originally copied from http://code.google.com/p/copter-gcs/
 * Modified by Karsten Prange (realbuxtehuder): - Improved consistency across
 * different screen sizes by replacing all fixed and density scaled size and
 * position values by percentual scaled values - Added functionality to show
 * dummy data for debugging purposes - Some minor layout changes
 */

public class HUD extends SurfaceView implements SurfaceHolder.Callback,
		HudUpdatedListner {
	private HudThread renderer;
	int width;
	int height;
	public HudYaw hudYaw = new HudYaw();
	public HudInfo data = new HudInfo();
	public HurRoll hudRoll = new HurRoll();
	public HudPlane hudPlane = new HudPlane();
	private HudPitch hudPitch = new HudPitch();
	public HudScroller hudScroller = new HudScroller();
	private HudFailsafe hudFailsafe = new HudFailsafe();

	HudCommonPaints commonPaints = new HudCommonPaints();
	
	Drone drone;

	@Override
	protected void onDraw(Canvas canvas) {
		if (drone == null) {
			return;
		}

		// clear screen
		canvas.drawColor(Color.rgb(20, 20, 20));
		canvas.translate(width / 2, data.attHeightPx / 2 + hudYaw.yawHeightPx); // set
		// center of HUD excluding YAW area

		// from now on each drawing routine has to undo all applied
		// transformations, clippings, etc by itself
		// this will improve performance because not every routine applies that
		// stuff, so general save and restore
		// is not necessary
		hudPitch.drawPitch(this, canvas);
		hudRoll.drawRoll(this, canvas);
		hudYaw.drawYaw(this, canvas);
		hudPlane.drawPlane(this, canvas);
		hudScroller.drawRightScroller(this, canvas);
		hudScroller.drawLeftScroller(this, canvas);
		data.drawAttitudeInfoText(this, canvas);
		hudFailsafe.drawFailsafe(this, canvas);
	}

	public HUD(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		getHolder().addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		this.width = width;
		this.height = height;

		// do as much precalculation as possible here because it
		// takes some load off the onDraw() routine which is called much more
		// frequently

		hudPlane.setupPlane(this);
		commonPaints.setupCommonPaints(this);		
		hudYaw.setupYaw(this, this);
		data.setupAtt(this);
		hudRoll.setupRoll(this);
		hudPitch.setupPitch(this);
		hudFailsafe.setupFailsafe(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		renderer = new HudThread(getHolder(), this);
		if (!renderer.isRunning()) {
			renderer.setRunning(true);
			renderer.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		renderer.setRunning(false);
		while (retry) {
			try {
				renderer.join();
				renderer = null;
				retry = false;
			} catch (InterruptedException e) {
				// we will try it again and again...
			}
		}
	}

	public void setDrone(Drone drone) {
		this.drone = drone;
		this.drone.setHudListner(this);
	}

	@Override
	public void onDroneUpdate() {
		if (renderer != null)
			renderer.setDirty();
	}
}
