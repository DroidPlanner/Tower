package org.droidplanner.android.widgets.spinnerWheel;

import java.util.LinkedList;
import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Wraps the horizontal spinner wheel, and its title within a view.
 */
public class CardWheelHorizontalView extends LinearLayout implements OnWheelChangedListener,
		OnWheelClickedListener, OnWheelScrollListener {

	public interface OnCardWheelChangedListener {
		void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue);
	}

	private final List<OnCardWheelChangedListener> mChangingListeners = new LinkedList<OnCardWheelChangedListener>();

	private View mVerticalDivider;
	private View mHorizontalDivider;

	private TextView mTitleView;
	private EditText mNumberInputText;
	private WheelHorizontalView mSpinnerWheel;

	public CardWheelHorizontalView(Context context) {
		this(context, null);
	}

	public CardWheelHorizontalView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CardWheelHorizontalView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context, attrs);
	}

	private void initialize(final Context context, AttributeSet attrs) {
		final TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.CardWheelHorizontalView, 0, 0);

		try {
			// Setup the container view.
			setBackgroundResource(R.drawable.bg_cell_white);

			// Setup the children views
			final LayoutInflater inflater = LayoutInflater.from(context);

			// Setup the divider view
			mVerticalDivider = inflater.inflate(R.layout.card_title_vertical_divider, this, false);
			mHorizontalDivider = inflater.inflate(R.layout.card_title_horizontal_divider, this,
					false);

			// Setup the title view
			mTitleView = (TextView) inflater.inflate(R.layout.card_wheel_horizontal_view_title,
					this, false);
			mTitleView.setText(a.getString(R.styleable.CardWheelHorizontalView_android_text));

			final int orientation = a.getInt(
					R.styleable.CardWheelHorizontalView_android_orientation, VERTICAL);
			if (orientation == HORIZONTAL) {
				setOrientation(HORIZONTAL);
			} else {
				setOrientation(VERTICAL);
			}

			updateTitleLayout();

			// Setup the spinnerwheel view
			final View spinnerWheelFrame = inflater.inflate(R.layout.card_wheel_horizontal_view,
					this, false);
			addView(spinnerWheelFrame);

			mSpinnerWheel = (WheelHorizontalView) spinnerWheelFrame
					.findViewById(R.id.horizontalSpinnerWheel);
			mSpinnerWheel.addChangingListener(this);
			mSpinnerWheel.addClickingListener(this);
			mSpinnerWheel.addScrollingListener(this);

			mNumberInputText = (EditText) spinnerWheelFrame.findViewById(R.id.numberInputText);
			mNumberInputText.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						mNumberInputText.selectAll();
					} else {
						hideSoftInput();
					}
				}
			});
			mNumberInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
						hideSoftInput();

						final CharSequence input = v.getText();
						if (input != null) {
							final int update = Integer.parseInt(input.toString());
							final int updateIndex = mSpinnerWheel.getViewAdapter().getItemIndex(
									update);
							if (updateIndex == -1) {
								Toast.makeText(context,
										"Entered value is outside of the allowed " + "range.",
										Toast.LENGTH_LONG).show();
							} else {
								setCurrentItemIndex(updateIndex, true);
							}
						}

						return true;
					}
					return false;
				}
			});
		} finally {
			a.recycle();
		}
	}

	/**
	 * Called after the orientation, and/or title is set in order to update the
	 * view layout.
	 */
	private void updateTitleLayout() {
		if (mTitleView == null || mVerticalDivider == null || mHorizontalDivider == null) {
			return;
		}

		final int childCount = getChildCount();
		if (mTitleView.length() > 0) {
			final View divider = getOrientation() == VERTICAL ? mVerticalDivider
					: mHorizontalDivider;

			if (childCount <= 1) {
				addView(mTitleView, 0);
				addView(divider, 1);
			} else {
				if (getChildAt(1) != divider) {
					removeViewAt(1);
					addView(divider, 1);
				}
			}
		} else if (childCount > 1) {
			removeViewAt(0);
			removeViewAt(1);
		}
	}

	@Override
	public void setOrientation(int orientation) {
		super.setOrientation(orientation);
		updateTitleLayout();
	}

	public void setViewAdapter(NumericWheelAdapter adapter) {
		mSpinnerWheel.setViewAdapter(adapter);
	}

	public void setCurrentValue(int value) {
		mSpinnerWheel.setCurrentItem(mSpinnerWheel.getViewAdapter().getItemIndex(value));
	}

	public int getCurrentValue() {
		return mSpinnerWheel.getViewAdapter().getItem(mSpinnerWheel.getCurrentItem());
	}

	private int getValue(int valueIndex) {
		return mSpinnerWheel.getViewAdapter().getItem(valueIndex);
	}

	private void setCurrentItemIndex(int index, boolean animated) {
		mSpinnerWheel.setCurrentItem(index, animated);
	}

	public void setText(CharSequence title) {
		mTitleView.setText(title);
		updateTitleLayout();
	}

	public void setText(int titleRes) {
		mTitleView.setText(titleRes);
		updateTitleLayout();
	}

	public CharSequence getText() {
		return mTitleView.getText();
	}

	public void addChangingListener(OnCardWheelChangedListener listener) {
		mChangingListeners.add(listener);
	}

	public void removeChangingListener(OnCardWheelChangedListener listener) {
		mChangingListeners.remove(listener);
	}

	@Override
	public void onChanged(AbstractWheel wheel, int oldIndex, int newIndex) {
		final int oldValue = getValue(oldIndex);
		final int newValue = getValue(newIndex);

		for (OnCardWheelChangedListener listener : mChangingListeners) {
			listener.onChanged(this, oldValue, newValue);
		}
	}

	@Override
	public void onItemClicked(AbstractWheel wheel, int itemIndex, boolean isCurrentItem) {
		if (isCurrentItem) {
			final String currentValue = String.valueOf(mSpinnerWheel.getViewAdapter().getItem(
					itemIndex));
			showSoftInput(currentValue);
		} else {
			hideSoftInput();
			setCurrentItemIndex(itemIndex, true);
		}
	}

	@Override
	public void onScrollingStarted(AbstractWheel wheel) {
		hideSoftInput();
	}

	@Override
	public void onScrollingFinished(AbstractWheel wheel) {
	}

	private void showSoftInput(String currentValue) {
		final Context context = getContext();
		final InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			mNumberInputText.setText(currentValue);
			mNumberInputText.setVisibility(VISIBLE);
			mNumberInputText.requestFocus();
			imm.showSoftInput(mNumberInputText, 0);
		}
	}

	private void hideSoftInput() {
		final Context context = getContext();
		final InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && imm.isActive(mNumberInputText)) {
			imm.hideSoftInputFromWindow(mNumberInputText.getWindowToken(), 0);
			mNumberInputText.setVisibility(INVISIBLE);
		}
	}

}
