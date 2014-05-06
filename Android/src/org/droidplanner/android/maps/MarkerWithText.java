package org.droidplanner.android.maps;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;

import org.droidplanner.R;

public class MarkerWithText {

	private static final int RECT_PADDING = 6;

	public static Bitmap getMarkerWithText(int color, String text,
			Context context) {
		return drawTextToBitmap(context, R.drawable.ic_marker_white, color,
				text);
	}

	/**
	 * Copied from:
	 * http://stackoverflow.com/questions/18335642/how-to-draw-text-
	 * in-default-marker-of-google-map-v2?lq=1
	 */
	private static Bitmap drawTextToBitmap(Context gContext, int gResId,
			int color, String gText) {
		Resources resources = gContext.getResources();
		float scale = resources.getDisplayMetrics().density;
		Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);

		android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
		if (bitmapConfig == null) {
			bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
		}
		bitmap = bitmap.copy(bitmapConfig, true);

		// copy bitmap to canvas, replace white with colour
		Paint paint = new Paint();
		paint.setColorFilter(new LightingColorFilter(0x000000, color));
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(bitmap, 0, 0, paint);

		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLACK);
		paint.setTextSize((int) (15 * scale));
		paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

		Rect bounds = new Rect();
		paint.getTextBounds(gText, 0, gText.length(), bounds);
		int x = (bitmap.getWidth() - bounds.width()) / 2;
		int y = (bitmap.getHeight() + bounds.height()) * 5 / 12; // At 5/12 from
																	// the top
																	// so it
																	// stays on
																	// the
																	// center

		canvas.drawText(gText, x, y, paint);

		return bitmap;
	}

	public static Bitmap getMarkerWithTextAndDetail(int gResId, String text,
			String detail, Resources res) {
		return drawTextAndDetailToBitmap(res, gResId, text, detail);
	}

	/**
	 * Based on:
	 * http://stackoverflow.com/questions/18335642/how-to-draw-text-in-
	 * default-marker-of-google-map-v2?lq=1
	 */
	private static Bitmap drawTextAndDetailToBitmap(Resources resources,
			int gResId, String gText, String gDetail) {
		float scale = resources.getDisplayMetrics().density;
		Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);

		android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
		if (bitmapConfig == null) {
			bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
		}
		bitmap = bitmap.copy(bitmapConfig, true);

		// copy bitmap to canvas, replace white with colour
		Paint paint = new Paint();
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(bitmap, 0, 0, paint);

		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.WHITE);
		paint.setTextSize((int) (13 * scale));
		paint.setFakeBoldText(true);
		paint.setShadowLayer(1f, 0f, 1f, Color.BLACK);

		Rect bounds = new Rect();
		paint.getTextBounds(gText, 0, gText.length(), bounds);
		bounds.offsetTo(-6, bounds.height() / 2);

		// paint and bounds for details
		Paint dpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dpaint.setColor(Color.WHITE);
		dpaint.setTextSize((int) (10 * scale));
		paint.setFakeBoldText(true);
		dpaint.setShadowLayer(1f, 0f, 1f, Color.BLACK);

		Rect dbounds;

		if (gDetail != null) {
			dbounds = new Rect();
			dpaint.getTextBounds(gDetail, 0, gDetail.length(), dbounds);
			dbounds.offsetTo(0, bounds.bottom + 2);
		} else {
			dbounds = new Rect(0, 0, 0, 0);
		}

		// include text and detail bounds
		Rect brect = new Rect(bounds);

		brect.union(dbounds);

		// position and inflate w/ padding
		int x = (bitmap.getWidth() - brect.width()) / 2;
		int y = bounds.top + (bitmap.getHeight() - brect.height()) / 2;
		brect.offsetTo(x, y - (bounds.height()));
		brect.set(brect.left - RECT_PADDING, brect.top - RECT_PADDING,
				brect.right + RECT_PADDING, brect.bottom + RECT_PADDING);

		// draw text
		x = (bitmap.getWidth() - bounds.width()) / 2;
		y = bounds.top
				+ (bitmap.getHeight() - (bounds.height() + dbounds.height()))
				/ 2;
		canvas.drawText(gText, x, y, paint);

		if (gDetail != null) {
			// draw detail
			x = (bitmap.getWidth() - dbounds.width()) / 2;
			y = y + bounds.height() + 2;
			canvas.drawText(gDetail, x, y, dpaint);
		}

		return bitmap;
	}
}
