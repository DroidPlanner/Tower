/*
 * android-spinnerwheel
 * https://github.com/ai212983/android-spinnerwheel
 *
 * based on
 *
 * Android Wheel Control.
 * https://code.google.com/p/android-wheel/
 *
 * Copyright 2011 Yuri Kanivets
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.droidplanner.android.view.spinnerWheel;

import android.content.Context;
import android.view.MotionEvent;

public class WheelHorizontalScroller extends WheelScroller {

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 * @param listener
	 *            the scrolling listener
	 */
	public WheelHorizontalScroller(Context context, ScrollingListener listener) {
		super(context, listener);
	}

	@Override
	protected int getCurrentScrollerPosition() {
		return scroller.getCurrX();
	}

	@Override
	protected int getFinalScrollerPosition() {
		return scroller.getFinalX();
	}

	@Override
	protected float getMotionDistance(MotionEvent event, float previousX, float previousY) {
		float distanceX = event.getX() - previousX;
		float distanceY = event.getY() - previousY;
		if (Math.abs(distanceX) > Math.abs(distanceY)) {
			return distanceX;
		}

		return 0;
	}

	@Override
	protected void scrollerStartScroll(int distance, int time) {
		scroller.startScroll(0, 0, distance, 0, time);
	}

	@Override
	protected void scrollerFling(int position, int velocityX, int velocityY) {
		final int maxPosition = 0x7FFFFFFF;
		final int minPosition = -maxPosition;
		scroller.fling(position, 0, -velocityX, 0, minPosition, maxPosition, 0, 0);
	}
}
