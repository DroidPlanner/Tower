package org.droidplanner.widgets.RcStick;

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
		paintOutline = new Paint();
		paintOutline.setAntiAlias(true);
		paintOutline.setStyle(Style.STROKE);
		paintOutline.setStrokeWidth(3);
		paintOutline.setColor(Color.parseColor("#E0E0E0"));

		paintFill = new Paint(paintOutline);
		paintFill.setStyle(Style.FILL);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w - 1;
		height = h - 1;
		borders = new RectF(1, 1, width, height);
		stickRadius = (int) (STICK_SIZE*Math.min(width, height)/2);
		setPosition(0, 0);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawRoundRect(borders, stickRadius, stickRadius, paintOutline);

		canvas.drawCircle(xPos, yPos, stickRadius, paintFill);
	}

	public void setPosition(float x, float y) {
		xPos = (int) ((width - 2 * stickRadius) * ((1 + x) / 2f))
				+ stickRadius;
		yPos = (int) ((height - 2 * stickRadius) * ((1 - y) / 2f))
				+ stickRadius;
		invalidate();
	}

}
