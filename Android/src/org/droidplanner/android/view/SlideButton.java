package org.droidplanner.android.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * Created by Fredia Huya-Kouadio on 4/6/15.
 */
public class SlideButton extends SeekBar {
    private Drawable thumb;

    public SlideButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        this.thumb = thumb;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!thumb.getBounds().contains((int) event.getX(), (int) event.getY())) {
                return false;
            }
        }

        return super.onTouchEvent(event);
    }

}

