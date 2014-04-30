package org.droidplanner.android.widgets.viewPager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/*
 * Simple extension to {@link ViewPager} that allows the definition of
 * the region where swiping is enabled
 * 
 * Based on the work of MartinHochstrasser on:
 * https://github.com/MartinHochstrasser/StickyViewPager/
 */
public class MapViewPager extends ViewPager {

	private int swipeRegionWidth = 40;
	private Context context;

	public MapViewPager(Context context) {
		super(context);
		this.context = context;
	}

	public MapViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (isInsideSwipeRegion(x, dx)) {
			return super.canScroll(v, checkV, dx, x, y);
		}
		return true;
	}

	/**
	 * Determines if the movement is inside the
	 * 
	 * @return true if is inside the region
	 */
	protected boolean isInsideSwipeRegion(float x, float dx) {
		if (dx > 0) {
			return x < swipeRegionWidth;
		} else {
			return x > (getWidth() - swipeRegionWidth);
		}
	}

	/**
	 * Sets the width of the swipeable region
	 * 
	 * @param width
	 */
	public void setSwipeMarginWidth(final int width) {
		swipeRegionWidth = (int) (width * context.getResources()
				.getDisplayMetrics().density);
	}

}
