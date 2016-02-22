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

package org.droidplanner.android.view.spinnerWheel.adapters;

import android.content.Context;
import android.text.TextUtils;

/**
 * Numeric Wheel adapter.
 */
public class NumericWheelAdapter extends AbstractWheelTextAdapter<Integer> {

	/** The default min value */
	public static final int DEFAULT_MAX_VALUE = 9;

	/** The default max value */
	private static final int DEFAULT_MIN_VALUE = 0;

	// Values
	private int minValue;
	private int maxValue;

	// format
	private String format;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 */
	public NumericWheelAdapter(Context context) {
		this(context, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 * @param minValue
	 *            the spinnerwheel min value
	 * @param maxValue
	 *            the spinnerwheel max value
	 */
	public NumericWheelAdapter(Context context, int minValue, int maxValue) {
		this(context, minValue, maxValue, null);
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the current context
	 * @param minValue
	 *            the spinnerwheel min value
	 * @param maxValue
	 *            the spinnerwheel max value
	 * @param format
	 *            the format string
	 */
	public NumericWheelAdapter(Context context, int minValue, int maxValue, String format) {
		this(context, TEXT_VIEW_ITEM_RESOURCE, minValue, maxValue, format);
	}

	public NumericWheelAdapter(Context context, int itemResource, int minValue, int maxValue,
			String format) {
		super(context, itemResource);
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.format = format;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
		notifyDataInvalidatedEvent();
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
		notifyDataInvalidatedEvent();
	}

	@Override
	public CharSequence getItemText(int index) {
		if (index >= 0 && index < getItemsCount()) {
			int value = minValue + index;
			return format != null ? String.format(format, value) : Integer.toString(value);
		}
		return null;
	}

    @Override
	public Integer getItem(int index) {
		if (index >= 0 && index < getItemsCount()) {
			return minValue + index;
		}
		throw new IllegalArgumentException("Index is out of range.");
	}

    @Override
	public int getItemIndex(Integer value) {
		if (value < this.minValue || value > this.maxValue) {
			return -1;
		}

		return value - this.minValue;
	}

    @Override
    public Integer parseItemText(CharSequence itemText) {
        String text = itemText.toString();
        if(TextUtils.isEmpty(text))
            return 0;
        return Integer.parseInt(text);
    }

    @Override
	public int getItemsCount() {
		return maxValue - minValue + 1;
	}
}
