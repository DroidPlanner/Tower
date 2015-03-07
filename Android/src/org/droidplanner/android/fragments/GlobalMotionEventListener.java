package org.droidplanner.android.fragments;

        import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
        import android.os.Binder;
        import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Toast;

import org.droidplanner.android.activities.helpers.ControllerEventCaptureView;
import org.droidplanner.android.activities.interfaces.PhysicalDeviceEvents;

public class GlobalMotionEventListener extends Service {

    private String TAG = this.getClass().getSimpleName();
    // window manager
    private WindowManager mWindowManager;

    private ControllerEventCaptureView eventsView;
    private Handler mHandler;

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        eventsView = new ControllerEventCaptureView(this);

        // fetch window manager object
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        // set layout parameter of window manager
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                5,
                5,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSPARENT);

        Log.i(TAG, "add View");

        mWindowManager.addView(eventsView, params);
        
        mHandler = new Handler();
    }


    @Override
    public void onDestroy() {
        if(mWindowManager != null) {
            if(eventsView != null) mWindowManager.removeView(eventsView);
        }
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public GlobalMotionEventListener getService() {
            return GlobalMotionEventListener.this;
        }
    }

    public ControllerEventCaptureView getMotionView() {
        return eventsView;
    }
}