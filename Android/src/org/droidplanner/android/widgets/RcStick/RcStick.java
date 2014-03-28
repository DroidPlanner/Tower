package org.droidplanner.android.widgets.RcStick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class RcStick extends View {

	private static final float STICK_SIZE = 0.3f;

	private Paint paintOutline;
	private Paint paintFill;
	private int height;
	private int width;
	private int xPos, yPos;
	private int stickRadius;
	private RectF borders;

	public RcStick(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	private void initialize() {
		paintFill = new Paint();
		paintFill.setAntiAlias(true);
		paintFill.setStyle(Style.FILL);
		paintFill.setColor(Color.parseColor("#333333"));

		paintOutline = new Paint(paintFill);
		paintOutline.setColor(Color.parseColor("#3CB4E5"));
		paintOutline.setStrokeWidth(6);
		paintOutline.setStyle(Style.STROKE);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w - 1;
		height = h - 1;
		borders = new RectF(1, 1, width, height);
		stickRadius = (int) (STICK_SIZE * Math.min(width, height) / 2);
		setPosition(0, 0);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawRoundRect(borders, stickRadius, stickRadius, paintFill);

		canvas.drawCircle(xPos, yPos, stickRadius, paintOutline);
	}

	public void setPosition(float x, float y) {
		xPos = (int) ((width - 2 * stickRadius) * ((1 + x) / 2f)) + stickRadius;
		yPos = (int) ((height - 2 * stickRadius) * ((1 - y) / 2f))
				+ stickRadius;
		invalidate();
	}

}
