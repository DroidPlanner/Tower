package org.droidplanner.android.view

import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class ScrollingLinearLayoutManager(context: Context, orientation: Int, reverseLayout: Boolean, val duration: Int) :
        LinearLayoutManager(context, orientation, reverseLayout){

    companion object {
        const val TARGET_SEEK_SCROLL_DISTANCE_PX = 10000
    }

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int){
        val firstVisibleChild = recyclerView.getChildAt(0)
        val itemSize = if(orientation == HORIZONTAL) firstVisibleChild.width else firstVisibleChild.height
        val currentPosition = recyclerView.getChildAdapterPosition(firstVisibleChild)
        var distanceInPixels = Math.abs((currentPosition - position) * itemSize)
        if(distanceInPixels == 0){
            distanceInPixels = Math.abs(if(orientation == HORIZONTAL) firstVisibleChild.x else firstVisibleChild.y).toInt()
        }

        val smoothScroller = SmoothScroller(recyclerView.context, distanceInPixels.toFloat(), duration)
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }

    private inner class SmoothScroller(context: Context, val distanceInPixels: Float, duration: Int) :
            LinearSmoothScroller(context) {

        private val duration: Float

        init {
            val millisecondsPerPx = calculateSpeedPerPixel(context.getResources().getDisplayMetrics())
            this.duration = if (distanceInPixels < TARGET_SEEK_SCROLL_DISTANCE_PX)
                (Math.abs(distanceInPixels) * millisecondsPerPx)
            else
                duration.toFloat()
        }

        override fun computeScrollVectorForPosition(targetPosition: Int): PointF {
            return this@ScrollingLinearLayoutManager.computeScrollVectorForPosition(targetPosition)
        }

        override fun calculateTimeForScrolling(dx: Int): Int {
            val proportion = dx.toFloat() / distanceInPixels
            return (duration * proportion).toInt()
        }
    }
}