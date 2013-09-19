package com.droidplanner.fragments.markers.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.droidplanner.R;

public class MarkerWithText {

	public static Bitmap getMarkerWithText(String text, Context context) {
		return drawTextToBitmap(context, R.drawable.ic_menu_places, text);
	}

	/**
	 * Copied from:
	 * http://stackoverflow.com/questions/18335642/how-to-draw-text-in-default-marker-of-google-map-v2?lq=1
	 */
	private static Bitmap drawTextToBitmap(Context gContext, int gResId,
			String gText) {
		Resources resources = gContext.getResources();
		float scale = resources.getDisplayMetrics().density;
		Bitmap bitmap = BitmapFactory.decodeResource(resources, gResId);

		android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
		if (bitmapConfig == null) {
			bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
		}
		bitmap = bitmap.copy(bitmapConfig, true);

		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLACK);
		paint.setTextSize((int) (15 * scale));
		paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

		Rect bounds = new Rect();
		paint.getTextBounds(gText, 0, gText.length(), bounds);
		int x = (bitmap.getWidth() - bounds.width()) / 2;
		int y = (bitmap.getHeight() + bounds.height()) / 2;

		canvas.drawText(gText, x * scale, y * scale, paint);

		return bitmap;
	}
}
