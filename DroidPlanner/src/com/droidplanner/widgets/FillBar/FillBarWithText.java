package com.droidplanner.widgets.FillBar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.droidplanner.R;

public class FillBarWithText extends LinearLayout {

	public FillBarWithText(Context context, AttributeSet attrs) {
		super(context, attrs);

        setOrientation(VERTICAL);
        
        inflate(context,R.layout.subview_fillbar_with_text, this);
	}

}
