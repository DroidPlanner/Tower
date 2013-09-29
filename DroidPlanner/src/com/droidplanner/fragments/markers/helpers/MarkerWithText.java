package com.droidplanner.fragments.markers.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;

import com.droidplanner.R;

public class MarkerWithText {

	private static final int RECT_PADDING = 6;

	public static Bitmap getMarkerWithText(String text, String detail, Context context) {
		return drawTextToBitmap(context, R.drawable.ic_menu_places, text, detail);
	}

	/**
	 * Copied from:
	 * http://stackoverflow.com/questions/18335642/how-to-draw-text-in-default-marker-of-google-map-v2?lq=1
	 */
	private static Bitmap drawTextToBitmap(Context gContext, int gResId,
			String gText, String gDetail) {
		Resources resources = gContext.getResources();
		float scale = resources.getDisplayMetrics().density;
		Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);

		android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
		if (bitmapConfig == null) {
			bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
		}
		bitmap = bitmap.copy(bitmapConfig, true);

		// paint and bounds for text
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLACK);
		paint.setTextSize((int) (15 * scale));
		paint.setFakeBoldText(true);
		paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

		Rect bounds = new Rect();
		paint.getTextBounds(gText, 0, gText.length(), bounds);
		bounds.offsetTo(0, bounds.height() / 2);

		// paint and bounds for details
		Paint dpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dpaint.setColor(Color.BLACK);
		dpaint.setTextSize((int) (14 * scale));
		paint.setFakeBoldText(true);
		dpaint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
		
		Rect dbounds = new Rect();
		dpaint.getTextBounds(gDetail, 0, gDetail.length(), dbounds);
		dbounds.offsetTo(0, bounds.bottom + 2);

		// paint and bounds for background
		Paint bpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bpaint.setColor(Color.WHITE);
		bpaint.setAlpha(160);
		bpaint.setShadowLayer(1f, 1f, 1f, Color.GRAY);
		bpaint.setStyle(Paint.Style.FILL);

		// include text and detail bounds
		Rect brect = new Rect(bounds);
		brect.union(dbounds);

		// position and inflate w/ padding
		int x = (bitmap.getWidth() - brect.width()) / 2;
		int y = bounds.top + (bitmap.getHeight() - brect.height()) / 2;
		brect.offsetTo(x, y - (bounds.height()));
		brect.set(brect.left - RECT_PADDING, brect.top - RECT_PADDING, brect.right + RECT_PADDING, brect.bottom + RECT_PADDING);

		// draw background w/ rounded corners
		RectF brectF = new RectF(brect);
		canvas.drawRoundRect(brectF, 12, 12, bpaint);

		// draw text
		x = (bitmap.getWidth() - bounds.width()) / 2;
		y = bounds.top + (bitmap.getHeight() - (bounds.height() + dbounds.height())) / 2;
		canvas.drawText(gText, x, y, paint);

		// draw detail
		x = (bitmap.getWidth() - dbounds.width()) / 2;
		y = y + bounds.height() + 2;
		canvas.drawText(gDetail, x, y, dpaint);

		return bitmap;
	}
}
