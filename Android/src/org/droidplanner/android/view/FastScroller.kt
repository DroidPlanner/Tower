package org.droidplanner.android.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import org.droidplanner.android.R

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class FastScroller(context: Context, attrs: AttributeSet, defStyleAttr: Int) : LinearLayout(context, attrs, defStyleAttr){

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private companion object {
        const val HANDLE_ANIMATION_DURATION = 100L
        const val SCALE_X = "scaleX"
        const val SCALE_Y = "scaleY"
        const val ALPHA = "alpha"

        private const val HANDLE_HIDE_DELAY = 1000L
        private const val TRACK_SNAP_RANGE = 5
    }

    private val bubble : View
    private val handle : View
    private val scrollListener = ScrollListener()
    private val handleHider = HandleHider()

    private var heightRef : Int = 0
    private var widthRef : Int = 0
    private var currentAnimator: AnimatorSet? = null
    private var recyclerView: RecyclerView? = null

    init {
        clipChildren = false
        val inflater = LayoutInflater.from(context)
        inflater.inflate(if(orientation == HORIZONTAL) R.layout.horizontal_fastscroller else R.layout.vertical_fastscroller, this)

        bubble = findViewById(R.id.fastscroller_bubble);
        handle = findViewById(R.id.fastscroller_handle);
    }

    fun setRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        recyclerView.setOnScrollListener(scrollListener)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int){
        super.onSizeChanged(w, h, oldw, oldh)
        widthRef = w
        heightRef = h
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                setPosition(event.x, event.y)
                currentAnimator?.cancel()
                handler.removeCallbacks(handleHider)
                if(handle.visibility == INVISIBLE)
                    showHandle()

                setRecyclerViewPosition(event.x, event.y)
                return true
            }
            MotionEvent.ACTION_UP -> {
                getHandler().postDelayed(handleHider, HANDLE_HIDE_DELAY)
                return true
            }
            else -> return super.onTouchEvent(event)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true
    }

    private fun setRecyclerViewPosition(x: Float, y: Float){
        if(recyclerView != null){
            val itemCount = recyclerView!!.adapter.itemCount
            if(orientation == HORIZONTAL) {
                val proportion = if (bubble.x == 0f) 0f
                else if (bubble.x + bubble.width >= widthRef - TRACK_SNAP_RANGE) 1f
                else x / widthRef

                val targetPos = getValueInRange(0, itemCount - 1, (proportion * itemCount).toInt())
                recyclerView?.scrollToPosition(targetPos)
            }
            else {
                val proportion: Float
                if (bubble.y == 0f) {
                    proportion = 0f
                } else if (bubble.y + bubble.height >= heightRef - TRACK_SNAP_RANGE) {
                    proportion = 1f
                } else {
                    proportion = y / heightRef.toFloat()
                }
                val targetPos = getValueInRange(0, itemCount - 1, (proportion * itemCount.toFloat()).toInt())
                recyclerView?.scrollToPosition(targetPos)
            }
        }
    }

    private fun setPosition(x: Float, y: Float){
        if(orientation == HORIZONTAL) {
            val position = x / widthRef
            val bubbleWidth = bubble.width
            bubble.setX(getValueInRange(0, widthRef - bubbleWidth, ((widthRef - bubbleWidth) * position).toInt()).toFloat())
            val handleWidth = handle.width
            handle.setX(getValueInRange(0, widthRef - handleWidth, ((widthRef - handleWidth) * position).toInt()).toFloat())
        }
        else {
            val position = y / heightRef
            val bubbleHeight = bubble.height
            bubble.y = getValueInRange(0, heightRef - bubbleHeight, ((heightRef - bubbleHeight) * position).toInt()).toFloat()
            val handleHeight = handle.height
            handle.y = getValueInRange(0, heightRef - handleHeight, ((heightRef - handleHeight) * position).toInt()).toFloat()
        }
    }

    private fun getValueInRange(min: Int, max: Int, value: Int): Int {
        val minimum = Math.max(min, value)
        return Math.min(minimum, max)
    }

    private fun showHandle(){
        val animatorSet = AnimatorSet()
        handle.setPivotX(handle.width.toFloat())
        handle.setPivotY(handle.height.toFloat())
        handle.visibility = VISIBLE

        val growerX = ObjectAnimator.ofFloat(handle, SCALE_X, 0f, 1f).setDuration(HANDLE_ANIMATION_DURATION)
        val growerY = ObjectAnimator.ofFloat(handle, SCALE_Y, 0f, 1f).setDuration(HANDLE_ANIMATION_DURATION)
        val alpha = ObjectAnimator.ofFloat(handle, ALPHA, 0f, 1f).setDuration(HANDLE_ANIMATION_DURATION)
        animatorSet.playTogether(growerX, growerY, alpha)
        animatorSet.start()
    }

    private fun hideHandle(){
        currentAnimator = AnimatorSet()
        handle.setPivotX(handle.width.toFloat())
        handle.setPivotY(handle.height.toFloat())
        val shrinkerX = ObjectAnimator.ofFloat(handle, SCALE_X, 1f, 0f).setDuration(HANDLE_ANIMATION_DURATION)
        val shrinkerY = ObjectAnimator.ofFloat(handle, SCALE_Y, 1f, 0f).setDuration(HANDLE_ANIMATION_DURATION)
        val alpha = ObjectAnimator.ofFloat(handle, ALPHA, 1f, 0f).setDuration(HANDLE_ANIMATION_DURATION)
        currentAnimator?.playTogether(shrinkerX, shrinkerY, alpha)
        currentAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                handle.visibility = INVISIBLE
                currentAnimator = null
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                handle.visibility = INVISIBLE
                currentAnimator = null
            }
        })
        currentAnimator?.start()
    }

    private inner class ScrollListener : RecyclerView.OnScrollListener(){
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {

            val firstVisibleView = recyclerView!!.getChildAt(0)
            val firstVisiblePosition = recyclerView!!.getChildPosition(firstVisibleView)
            val visibleRange = recyclerView!!.childCount
            val lastVisiblePosition = firstVisiblePosition + visibleRange
            val itemCount = recyclerView!!.adapter.itemCount
            val position = if (firstVisiblePosition == 0) 0
            else if (lastVisiblePosition == itemCount - 1) itemCount - 1
            else firstVisiblePosition

            val proportion = position.toFloat() / itemCount.toFloat()
            setPosition((widthRef * proportion), heightRef * proportion)
        }
    }

    private inner class HandleHider : Runnable {
        override fun run() {
            hideHandle()
        }

    }
}