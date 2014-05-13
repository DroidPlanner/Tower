package org.droidplanner.android.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.ActionMode;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;


/**
 * This class provides the necessary workarounds to make the default preference fragment screen
 * work again on glass post XE16.
 */
public class GlassPreferenceFragment extends PreferenceFragment {

    @Override
    public void onStart() {
        super.onStart();

        final Activity parentActivity = getActivity();
        if (parentActivity != null) {
            updateWindowCallback(parentActivity.getWindow());
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        final Activity parentActivity = getActivity();
        if (parentActivity != null) {
            restoreWindowCallback(parentActivity.getWindow());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference instanceof PreferenceScreen) {
            //Update the preference dialog window callback. The new callback is able to detect
            // and handle the glass touchpad gestures.
            final Dialog prefDialog = ((PreferenceScreen) preference).getDialog();
            if (prefDialog != null) {
                updateWindowCallback(prefDialog.getWindow());
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    /**
     * Replace the current window callback with one supporting glass gesture events.
     *
     * @param window
     */
    public static void updateWindowCallback(Window window) {
        if (window == null) {
            return;
        }

        final Window.Callback originalCb = window.getCallback();
        if (!(originalCb instanceof GlassCallback)) {
            final GlassCallback glassCb = new GlassCallback(window.getContext(), originalCb);
            window.setCallback(glassCb);
        }
    }

    /**
     * Replace the current window callback with one supporting glass gesture events.
     * @param window
     * @param glassDetector GestureDetector to which the generic motion events should be directed.
     */
    public static void updateWindowCallback(Window window, GestureDetector glassDetector) {
        if (window == null) {
            return;
        }

        final Window.Callback originalCb = window.getCallback();
        if(!(originalCb instanceof GlassCallback)){
            final GlassCallback glassCb = new GlassCallback(originalCb, glassDetector);
            window.setCallback(glassCb);
        }
    }

    /**
     * Restore the original window callback for this window, if it was updated with a glass
     * window callback.
     *
     * @param window
     */
    public static void restoreWindowCallback(Window window) {
        if (window == null) {
            return;
        }

        final Window.Callback currentCb = window.getCallback();
        if (currentCb instanceof GlassCallback) {
            final Window.Callback originalCb = ((GlassCallback) currentCb).getOriginalCallback();
            window.setCallback(originalCb);
        }
    }

    /**
     * Window.Callback implementation able to detect, and handle glass touchpad gestures.
     */
    private static class GlassCallback implements Window.Callback {

        /**
         * Used to detect and handle glass touchpad events.
         */
        private final GestureDetector mGlassDetector;

        /**
         * This handles the motion events not supported by the glass window callback.
         */
        private final Window.Callback mOriginalCb;

        public GlassCallback(Context context, Window.Callback original) {
            mOriginalCb = original;

            mGlassDetector = new GestureDetector(context);
            mGlassDetector.setBaseListener(new GestureDetector.BaseListener() {
                @Override
                public boolean onGesture(Gesture gesture) {
                    switch (gesture) {
                        case TAP:
                            final KeyEvent enterKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN,
                                KeyEvent.KEYCODE_DPAD_CENTER);
                            enterKeyEvent.setSource(InputDevice.SOURCE_KEYBOARD);

                            dispatchKeyEvent(enterKeyEvent);
                            dispatchKeyEvent(KeyEvent.changeAction(enterKeyEvent,
                                    KeyEvent.ACTION_UP));
                            return true;

                        case SWIPE_LEFT:
                            final KeyEvent upKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN,
                                    KeyEvent.KEYCODE_DPAD_UP);
                            upKeyEvent.setSource(InputDevice.SOURCE_KEYBOARD);

                            dispatchKeyEvent(upKeyEvent);
                            dispatchKeyEvent(KeyEvent.changeAction(upKeyEvent, KeyEvent.ACTION_UP));
                            return true;

                        case SWIPE_RIGHT:
                            final KeyEvent downKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN,
                                    KeyEvent.KEYCODE_DPAD_UP);
                            downKeyEvent.setSource(InputDevice.SOURCE_KEYBOARD);
                            dispatchKeyEvent(downKeyEvent);
                            dispatchKeyEvent(KeyEvent.changeAction(downKeyEvent,
                                    KeyEvent.ACTION_UP));
                            return true;
                    }

                    return false;
                }
            });
        }

        public GlassCallback(Window.Callback original, GestureDetector glassDetector) {
            mOriginalCb = original;
            mGlassDetector = glassDetector;
        }

        /**
         * @return the Window.Callback instance this one replaced.
         */
        public Window.Callback getOriginalCallback() {
            return mOriginalCb;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            return mOriginalCb.dispatchKeyEvent(event);
        }

        @Override
        public boolean dispatchKeyShortcutEvent(KeyEvent event) {
            return mOriginalCb.dispatchKeyShortcutEvent(event);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            return mOriginalCb.dispatchTouchEvent(event);
        }

        @Override
        public boolean dispatchTrackballEvent(MotionEvent event) {
            return mOriginalCb.dispatchTrackballEvent(event);
        }

        @Override
        public boolean dispatchGenericMotionEvent(MotionEvent event) {
            return (mGlassDetector != null && mGlassDetector.onMotionEvent(event)) || mOriginalCb
                    .dispatchGenericMotionEvent(event);
        }

        @Override
        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
            return mOriginalCb.dispatchPopulateAccessibilityEvent(event);
        }

        @Override
        public View onCreatePanelView(int featureId) {
            return mOriginalCb.onCreatePanelView(featureId);
        }

        @Override
        public boolean onCreatePanelMenu(int featureId, Menu menu) {
            return mOriginalCb.onCreatePanelMenu(featureId, menu);
        }

        @Override
        public boolean onPreparePanel(int featureId, View view, Menu menu) {
            return mOriginalCb.onPreparePanel(featureId, view, menu);
        }

        @Override
        public boolean onMenuOpened(int featureId, Menu menu) {
            return mOriginalCb.onMenuOpened(featureId, menu);
        }

        @Override
        public boolean onMenuItemSelected(int featureId, MenuItem item) {
            return mOriginalCb.onMenuItemSelected(featureId, item);
        }

        @Override
        public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
            mOriginalCb.onWindowAttributesChanged(attrs);
        }

        @Override
        public void onContentChanged() {
            mOriginalCb.onContentChanged();
        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            mOriginalCb.onWindowFocusChanged(hasFocus);
        }

        @Override
        public void onAttachedToWindow() {
            mOriginalCb.onAttachedToWindow();
        }

        @Override
        public void onDetachedFromWindow() {
            mOriginalCb.onDetachedFromWindow();
        }

        @Override
        public void onPanelClosed(int featureId, Menu menu) {
            mOriginalCb.onPanelClosed(featureId, menu);
        }

        @Override
        public boolean onSearchRequested() {
            return mOriginalCb.onSearchRequested();
        }

        @Override
        public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
            return mOriginalCb.onWindowStartingActionMode(callback);
        }

        @Override
        public void onActionModeStarted(ActionMode mode) {
            mOriginalCb.onActionModeStarted(mode);
        }

        @Override
        public void onActionModeFinished(ActionMode mode) {
            mOriginalCb.onActionModeFinished(mode);
        }
    }
}
