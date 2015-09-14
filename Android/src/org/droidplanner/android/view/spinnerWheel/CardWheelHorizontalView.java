package org.droidplanner.android.view.spinnerWheel;

import java.util.LinkedList;
import java.util.List;

import org.beyene.sius.unit.Unit;
import org.droidplanner.android.R;
import org.droidplanner.android.view.spinnerWheel.adapters.AbstractWheelTextAdapter;

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
public class  CardWheelHorizontalView<T> extends LinearLayout implements OnWheelChangedListener,
		OnWheelClickedListener, OnWheelScrollListener {

    private final static String TAG = CardWheelHorizontalView.class.getSimpleName();

	public interface OnCardWheelScrollListener<T> {
        void onScrollingStarted(CardWheelHorizontalView cardWheel, T startValue);

        void onScrollingUpdate(CardWheelHorizontalView cardWheel, T oldValue, T newValue);

		void onScrollingEnded(CardWheelHorizontalView cardWheel, T startValue, T endValue);
	}

	private final List<OnCardWheelScrollListener<T>> mScrollingListeners = new LinkedList<>();

	private View mVerticalDivider;
	private View mHorizontalDivider;

	private TextView mTitleView;
	private EditText mNumberInputText;
	private WheelHorizontalView<T> mSpinnerWheel;

    private T scrollingStartValue;

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
			mHorizontalDivider = inflater.inflate(R.layout.card_title_horizontal_divider, this,	false);

			// Setup the title view
			mTitleView = (TextView) inflater.inflate(R.layout.card_wheel_horizontal_view_title,	this, false);
			mTitleView.setText(a.getString(R.styleable.CardWheelHorizontalView_android_text));

			final int orientation = a.getInt(R.styleable.CardWheelHorizontalView_android_orientation, VERTICAL);
			if (orientation == HORIZONTAL) {
				setOrientation(HORIZONTAL);
			} else {
				setOrientation(VERTICAL);
			}

			updateTitleLayout();

			// Setup the spinnerwheel view
			final View spinnerWheelFrame = inflater.inflate(R.layout.card_wheel_horizontal_view, this, false);
			addView(spinnerWheelFrame);

			mSpinnerWheel = (WheelHorizontalView) spinnerWheelFrame.findViewById(R.id.horizontalSpinnerWheel);
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
                            AbstractWheelTextAdapter<T> viewAdapter = mSpinnerWheel.getViewAdapter();
							final T update = viewAdapter.parseItemText(input);
							final int updateIndex = viewAdapter.getItemIndex(update);
							if (updateIndex == -1) {
								Toast.makeText(context,	"Entered value is outside of the allowed range.",
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
			final View divider = getOrientation() == VERTICAL ? mVerticalDivider : mHorizontalDivider;

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

	public void setViewAdapter(AbstractWheelTextAdapter<T> adapter) {
		mSpinnerWheel.setViewAdapter(adapter);
	}

	public void setCurrentValue(T value) {
		mSpinnerWheel.setCurrentItem(mSpinnerWheel.getViewAdapter().getItemIndex(value));
	}

	public T getCurrentValue() {
		return mSpinnerWheel.getViewAdapter().getItem(mSpinnerWheel.getCurrentItem());
	}

	private T getValue(int valueIndex) {
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

	public void addScrollListener(OnCardWheelScrollListener<T> listener) {
		mScrollingListeners.add(listener);
	}

	public void removeChangingListener(OnCardWheelScrollListener<T> listener) {
		mScrollingListeners.remove(listener);
	}

	@Override
	public void onChanged(AbstractWheel wheel, int oldIndex, int newIndex) {
		final T oldValue = getValue(oldIndex);
		final T newValue = getValue(newIndex);

		for (OnCardWheelScrollListener<T> listener : mScrollingListeners) {
			listener.onScrollingUpdate(this, oldValue, newValue);
		}
	}

	@Override
	public void onItemClicked(AbstractWheel wheel, int itemIndex, boolean isCurrentItem) {
		if (isCurrentItem) {
			final T currentValue = mSpinnerWheel.getViewAdapter().getItem(itemIndex);
			showSoftInput(currentValue);
		} else {
			hideSoftInput();
			setCurrentItemIndex(itemIndex, true);
		}
	}

	@Override
	public void onScrollingStarted(AbstractWheel wheel) {
		hideSoftInput();
        scrollingStartValue = getCurrentValue();
        for(OnCardWheelScrollListener<T> listener: mScrollingListeners){
            listener.onScrollingStarted(this, scrollingStartValue);
        }
	}


	@Override
	public void onScrollingFinished(AbstractWheel wheel) {
        final T endValue = getCurrentValue();
        for (OnCardWheelScrollListener<T> listener : mScrollingListeners) {
            listener.onScrollingEnded(this, scrollingStartValue, endValue);
        }
	}

	private void showSoftInput(T currentValue) {
		final Context context = getContext();
		final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
            if(currentValue instanceof Unit)
			    mNumberInputText.setText(String.valueOf(((Unit)currentValue).getValue()));
            else{
                mNumberInputText.setText(currentValue.toString());
            }
			mNumberInputText.setVisibility(VISIBLE);
			mNumberInputText.requestFocus();
			imm.showSoftInput(mNumberInputText, 0);
		}
	}

	private void hideSoftInput() {
		final Context context = getContext();
		final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && imm.isActive(mNumberInputText)) {
			imm.hideSoftInputFromWindow(mNumberInputText.getWindowToken(), 0);
			mNumberInputText.setVisibility(INVISIBLE);
		}
	}
}
