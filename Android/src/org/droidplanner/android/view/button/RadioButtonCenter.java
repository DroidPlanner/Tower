package org.droidplanner.android.view.button;

import org.droidplanner.android.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.RadioButton;

/**
 * This class implements a radio button with custom gravity. The api's version
 * is always left-aligned
 * (https://stackoverflow.com/questions/4407553/android-radiobutton
 * -button-drawable -gravity/4407803#4407803).
 */
public class RadioButtonCenter extends RadioButton {

	/**
	 * This is the radio button drawable.
	 */
	private Drawable mButtonDrawable;

	public RadioButtonCenter(Context context) {
		super(context);
	}

	public RadioButtonCenter(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RadioButtonCenter(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		final TypedArray attributes = context.obtainStyledAttributes(attrs,
				R.styleable.RadioButtonCenter, defStyle, 0);

		try {
			mButtonDrawable = attributes.getDrawable(R.styleable.RadioButtonCenter_android_button);
		} finally {
			attributes.recycle();
		}
		setButtonDrawable(android.R.color.transparent);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mButtonDrawable != null) {
			mButtonDrawable.setState(getDrawableState());
			final int verticalGravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
			final int height = mButtonDrawable.getIntrinsicHeight();

			int y = 0;
			switch (verticalGravity) {
			case Gravity.BOTTOM:
				y = getHeight() - height;
				break;

			case Gravity.CENTER_VERTICAL:
				y = (getHeight() - height) / 2;
				break;
			}

			int buttonWidth = mButtonDrawable.getIntrinsicWidth();
			int buttonLeft = (getWidth() - buttonWidth) / 2;
			mButtonDrawable.setBounds(buttonLeft, y, buttonLeft + buttonWidth, y + height);
			mButtonDrawable.draw(canvas);
		}
	}
}
