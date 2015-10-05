/*
 * Copyright (C) 2014 I.C.N.H GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.droidplanner.android.utils;

/*
 * Modified version of GitHub user mohlendo's ReorderRecyclerView gist
 * original at https://gist.github.com/mohlendo/68b7e2f89d0b1b354abe
 *
 * Copyright (C) 2014 I.C.N.H GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * A {@link android.support.v7.widget.RecyclerView} that provides reordering with drag&amp;drop.
 * The Adapter has to be of type {@link ReorderRecyclerView.ReorderAdapter}.
 * Furthermore you have to provide stable ids {@link android.support.v7.widget.RecyclerView.Adapter#setHasStableIds(boolean)}}
 */
public class ReorderRecyclerView extends RecyclerView {
    private static final String TAG = ReorderRecyclerView.class.getSimpleName();

    private static final int INVALID_POINTER_ID = -1;
    private static final int LINE_THICKNESS = 15;
    private static final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 100;
    private static final int INVALID_ID = -1;

    private int activePointerId = INVALID_POINTER_ID;
    private int downX;
    private int downY;
    private int totalOffsetY, totalOffsetX;

    private static final int SCALE_FACTOR = 20;

    private BitmapDrawable hoverCell;
    private Rect hoverCellOriginalBounds;
    private Rect hoverCellCurrentBounds;

    private boolean cellIsMobile = false;
    private long mobileItemId = INVALID_ID;

    private int smoothScrollAmountAtEdge;
    private boolean usWaitingForScrollFinish;

    // stop the swaps from happening while another is still animating
    boolean allowSwaps = true;

    public ReorderRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public ReorderRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReorderRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        smoothScrollAmountAtEdge = (int) (SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);

        // detector for the long press in order to start the dragging
        final GestureDetector longPressGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent event) {
                Log.d(TAG, "Longpress detected");
                downX = (int) event.getX();
                downY = (int) event.getY();
                activePointerId = event.getPointerId(0);

                totalOffsetY = 0;
                totalOffsetX = 0;
                View selectedView = findChildViewUnder(downX, downY);
                if (selectedView == null) {
                    return;
                }
                mobileItemId = getChildItemId(selectedView);
                hoverCell = getAndAddHoverView(selectedView);
                selectedView.setVisibility(INVISIBLE);
                cellIsMobile = true;
            }
        });

        //
        final OnItemTouchListener itemTouchListener = new OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
                if (longPressGestureDetector.onTouchEvent(event)) {
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        return cellIsMobile;
                    default:
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent event) {
                handleMotionEvent(event);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        };
        addOnItemTouchListener(itemTouchListener);
    }

    private void handleMotionEvent(MotionEvent event) {
        //Log.d(TAG, String.format("handleMotionEvent %s", event));

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:

                if (activePointerId == INVALID_POINTER_ID) {
                    break;
                }

                int pointerIndex = event.findPointerIndex(activePointerId);

                int deltaY = (int) event.getY(pointerIndex)- downY;
                int deltaX = (int) event.getX(pointerIndex)- downX;

                if (cellIsMobile) {
                    hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left + deltaX + totalOffsetX,
                            hoverCellOriginalBounds.top + deltaY + totalOffsetY);
                    if(hoverCell != null)
                        hoverCell.setBounds(hoverCellCurrentBounds);
                    invalidate();

                    handleCellSwitch();

                    handleMobileCellScroll();
                }
                break;
            case MotionEvent.ACTION_UP:
                touchEventsEnded();
                break;
            case MotionEvent.ACTION_CANCEL:
                touchEventsCancelled();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                /* If a multitouch event took place and the original touch dictating
                 * the movement of the hover cell has ended, then the dragging event
                 * ends and the hover cell is animated to its corresponding position
                 * in the listview. */
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                        MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    touchEventsEnded();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Creates the hover cell with the appropriate bitmap and of appropriate
     * size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
     * single time an invalidate call is made.
     */
    private BitmapDrawable getAndAddHoverView(View v) {
        int w = v.getWidth() + SCALE_FACTOR;
        int h = v.getHeight() + SCALE_FACTOR;
        int top = v.getTop();
        int left = v.getLeft();

        Bitmap b = getBitmapFromView(v);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, w, h, false);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), scaledBitmap);

        hoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
        hoverCellCurrentBounds = new Rect(hoverCellOriginalBounds);

        drawable.setBounds(hoverCellCurrentBounds);

        return drawable;
    }

    /**
     * Draws a black border over the screenshot of the view passed in.
     */
    private Bitmap getBitmapWithBorder(View v) {
        Bitmap bitmap = getBitmapFromView(v);
        Canvas can = new Canvas(bitmap);

        can.drawBitmap(bitmap, 0, 0, null);

        return bitmap;
    }

    /**
     * Returns a bitmap showing a screenshot of the view passed in
     */
    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }


    /**
     * dispatchDraw gets invoked when all the child views are about to be drawn.
     * By overriding this method, the hover cell (BitmapDrawable) can be drawn
     * over the recyclerviews's items whenever the recyclerviews is redrawn.
     */
    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        if (hoverCell != null) {
            hoverCell.draw(canvas);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (!(adapter instanceof ReorderAdapter) && !adapter.hasStableIds()) {
            throw new IllegalStateException("ReorderRecyclerView only works with ReorderAdapter and must have stable ids!");
        }
        super.setAdapter(adapter);
    }

    /**
     * This method determines whether the hover cell has been shifted far enough
     * to invoke a cell swap. If so, then the respective cell swap candidate is
     * determined and the data set is changed. Upon posting a notification of the
     * data set change, a layout is invoked to place the cells in the right place.
     *
     * A valid swap will be when both views are different and the hover view has
     * crossed the middle of the childUnderView
     *
     * The hover view will be 40 pixels wider and 40 pixels higher so we needs to account for this
     */
    private void handleCellSwitch() {
        ViewHolder mobileViewHolder = findViewHolderForItemId(mobileItemId);
        View mobileView = mobileViewHolder != null ? mobileViewHolder.itemView : null;
        if (mobileView != null) {
            View childViewUnder = null;
            int originalItem  = getChildPosition(mobileView);
            int leftRightBound = hoverCellCurrentBounds.left + (SCALE_FACTOR / 2);
            int topBottomBound = hoverCellCurrentBounds.bottom - (SCALE_FACTOR / 2);
            childViewUnder = findChildViewUnder(leftRightBound, topBottomBound);

            // check if bottom left corner overlaps middle of child
            if (!isSameView(childViewUnder, mobileView)) {
                int childCenterX = getCenterX(childViewUnder);
                int childCenterY = getCenterY(childViewUnder);
                if (childCenterX > leftRightBound && childCenterY < topBottomBound) {
                    swapElements(originalItem, getChildPosition(childViewUnder));
                    return;
                }
            }

            // check if top left corner overlaps middle of child
            topBottomBound = hoverCellCurrentBounds.top + (SCALE_FACTOR / 2);
            childViewUnder = findChildViewUnder(leftRightBound, topBottomBound);
            if (!isSameView(childViewUnder, mobileView)) {
                int childCenterX = getCenterX(childViewUnder);
                int childCenterY = getCenterY(childViewUnder);
                if (childCenterX > leftRightBound && childCenterY > topBottomBound) {
                    swapElements(originalItem, getChildPosition(childViewUnder));
                    return;
                }
            }

            leftRightBound = hoverCellCurrentBounds.right - (SCALE_FACTOR / 2);
            childViewUnder = findChildViewUnder(leftRightBound, topBottomBound);
            // check if top right overlaps middle of child
            if (!isSameView(childViewUnder, mobileView)) {
                int childCenterX = getCenterX(childViewUnder);
                int childCenterY = getCenterY(childViewUnder);
                if (childCenterX < leftRightBound && childCenterY > topBottomBound) {
                    swapElements(originalItem, getChildPosition(childViewUnder));
                }
            }

            topBottomBound = hoverCellCurrentBounds.bottom - (SCALE_FACTOR / 2);
            childViewUnder = findChildViewUnder(leftRightBound, topBottomBound);
            // chick if bottom right overlaps middle of child
            if (!isSameView(childViewUnder, mobileView)) {
                int childCenterX = getCenterX(childViewUnder);
                int childCenterY = getCenterY(childViewUnder);
                if (childCenterX < leftRightBound && childCenterY < topBottomBound) {
                    swapElements(originalItem, getChildPosition(childViewUnder));
                    return;
                }
            }


        }
    }

    private int getCenterX(View view) {
        return (int) (view.getX() + view.getWidth() / 2);
    }

    private int getCenterY(View view) {
        return (int) (view.getY() + view.getHeight() / 2);
    }

    private boolean isSameView(View view1, View view2) {
        if (view1 == null || view2 == null) {
            return true;
        }
        return getChildPosition(view1) == getChildPosition(view2);
    }

    /**
     * Swaps the the elements with the given indices.
     *
     * @param fromIndex the from-element index
     * @param toIndex   the to-element index
     */
    private void swapElements(int fromIndex, int toIndex) {
        // stop elements from swapping back and forth really fast in while swap is taking place
        if (allowSwaps) {
            allowSwaps = false;

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    allowSwaps = true;
                }
            }, getItemAnimator().getMoveDuration());
            Log.i(TAG, String.format("Swapping %d with %d", fromIndex, toIndex));
            ReorderAdapter adapter = (ReorderAdapter) getAdapter();
            adapter.swapElements(fromIndex, toIndex);
            adapter.notifyItemMoved(fromIndex, toIndex);
        }

    }

    /**
     * Resets all the appropriate fields to a default state while also animating
     * the hover cell back to its correct location.
     */
    private void touchEventsEnded() {
        ViewHolder viewHolderForItemId = findViewHolderForItemId(mobileItemId);
        if (viewHolderForItemId == null) {
            return;
        }
        final View mobileView = viewHolderForItemId.itemView;
        if (cellIsMobile || usWaitingForScrollFinish) {
            cellIsMobile = false;
            usWaitingForScrollFinish = false;
            activePointerId = INVALID_POINTER_ID;

            // If the autoscroller has not completed scrolling, we need to wait for it to
            // finish in order to determine the final location of where the hover cell
            // should be animated to.
            if (getScrollState() != SCROLL_STATE_IDLE) {
                usWaitingForScrollFinish = true;
                return;
            }

            hoverCellCurrentBounds.offsetTo(mobileView.getLeft(), mobileView.getTop());

            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(hoverCell, "bounds",
                    sBoundEvaluator, hoverCellCurrentBounds);
            hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });
            hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mobileItemId = INVALID_ID;
                    mobileView.setVisibility(VISIBLE);
                    hoverCell = null;
                    setEnabled(true);
                    invalidate();
                }
            });
            hoverViewAnimator.start();
        } else {
            touchEventsCancelled();
        }

    }

    /**
     * This TypeEvaluator is used to animate the BitmapDrawable back to its
     * final location when the user lifts his finger by modifying the
     * BitmapDrawable's bounds.
     */
    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction),
                    interpolate(startValue.top, endValue.top, fraction),
                    interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int) (start + fraction * (end - start));
        }
    };

    /**
     * Resets all the appropriate fields to a default state.
     */
    private void touchEventsCancelled() {
        ViewHolder viewHolderForItemId = findViewHolderForItemId(mobileItemId);
        if (viewHolderForItemId == null) {
            return;
        }
        View mobileView = viewHolderForItemId.itemView;
        if (cellIsMobile) {
            mobileItemId = INVALID_ID;
            mobileView.setVisibility(VISIBLE);
            hoverCell = null;
            invalidate();
        }
        cellIsMobile = false;
        activePointerId = INVALID_POINTER_ID;
    }

    /**
     * Determines whether this recyclerview is in a scrolling state invoked
     * by the fact that the hover cell is out of the bounds of the recyclerview;
     */
    private void handleMobileCellScroll() {
        handleMobileCellScroll(hoverCellCurrentBounds);
    }

    /**
     * This method is in charge of determining if the hover cell is above/below or
     * left/right the bounds of the recyclerview. If so, the recyclerview does an appropriate
     * upward or downward smooth scroll so as to reveal new items.
     */
    public boolean handleMobileCellScroll(Rect r) {
        if (getLayoutManager().canScrollVertically()) {
            int offset = computeVerticalScrollOffset();
            int height = getHeight();
            int extent = computeVerticalScrollExtent();
            int range = computeVerticalScrollRange();
            int hoverViewTop = r.top;
            int hoverHeight = r.height();

            if (hoverViewTop <= 0 && offset > 0) {
                Log.d(TAG, String.format("scrolling vertically by %d", -smoothScrollAmountAtEdge));
                scrollBy(0, -smoothScrollAmountAtEdge);
                return true;
            }

            if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
                Log.d(TAG, String.format("scrolling vertically by %d", smoothScrollAmountAtEdge));
                scrollBy(0, smoothScrollAmountAtEdge);
                return true;
            }
        }

        if (getLayoutManager().canScrollHorizontally()) {
            Log.d(TAG, "CAN SCROLL HORIZONTALLY");
            int offset = computeHorizontalScrollOffset();
            int width = getWidth();
            int extent = computeHorizontalScrollExtent();
            int range = computeHorizontalScrollRange();
            int hoverViewLeft = r.left;
            int hoverWidth = r.width();

            if (hoverViewLeft <= 0 && offset > 0) {
                Log.d(TAG, String.format("scrolling horizontally by %d", -smoothScrollAmountAtEdge));
                scrollBy(-smoothScrollAmountAtEdge, 0);
                return true;
            }

            if (hoverViewLeft + hoverWidth >= width && (offset + extent) < range) {
                Log.d(TAG, String.format("scrolling horizontally by %d", smoothScrollAmountAtEdge));
                scrollBy(smoothScrollAmountAtEdge, 0);
                return true;
            }
        }

        return false;
    }

    /**
     * Special adapter that provides reorder functionality.
     * Implementations have to provide stable ids {@link #hasStableIds()}
     */
    public static abstract class ReorderAdapter<VH extends android.support.v7.widget.RecyclerView.ViewHolder> extends Adapter<VH> {
        /**
         * Swap the positions of the elements with the given indices.
         * You don't have to notify the change.
         * This will be handled by the recyclerview.
         * Example:
         * <pre>
         * {@code
         * Object temp = cheeseList.get(fromIndex);
         * dataList.set(fromIndex, cheeseList.get(toIndex));
         * dataList.set(toIndex, temp);
         * }
         * </pre>
         *
         * @param fromIndex the index
         * @param toIndex   the index
         */
        public abstract void swapElements(int fromIndex, int toIndex);
    }
}