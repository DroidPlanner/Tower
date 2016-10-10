package org.droidplanner.android.maps

import android.graphics.Color
import com.o3dr.services.android.lib.coordinate.LatLong

/**
 * Abstract representation of a polyline for a map implementing the DPMap interface
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
abstract class PolylineInfo {

    var proxyPolyline: ProxyPolyline? = null

    fun removeProxy(){
        proxyPolyline?.remove()
        proxyPolyline = null
    }

    fun isOnMap() = proxyPolyline != null

    fun updatePolyline(){
        proxyPolyline?.apply {
            setPoints(getPoints())
            color(getColor())
            width(getWidth())
            zIndex(getZIndex())
            clickable(isClickable())
            geodesic(isGeodesic())
            visible(isVisible())
        }
    }

    abstract fun getPoints(): List<LatLong>

    open fun getZIndex() = 1F
    open fun getColor() = Color.BLACK
    open fun getWidth() = 6F
    open fun isClickable() = false
    open fun isGeodesic() = false
    open fun isVisible() = true

    /**
     * Proxy interface to the actual map polyline implementation
     */
    interface ProxyPolyline {

        /**
         * Sets the vertices for the polyline
         */
        fun setPoints(points: List<LatLong>)

        /**
         * Specifies whether it's clickable
         */
        fun clickable(clickable: Boolean)

        /**
         * Sets the color for the polyline
         */
        fun color(color: Int)

        /**
         * Specifies whether to draw each segment as a geodesic
         */
        fun geodesic(geodesic: Boolean)

        /**
         * Specifies the visibility
         */
        fun visible(visible: Boolean)

        /**
         * Sets the width of the polyline in screen pixels
         */
        fun width(width: Float)

        /**
         * Specifies the polyline's zIndex, the order in which it will be drawn
         */
        fun zIndex(zIndex: Float)

        /**
         * Remove polyline from the map
         */
        fun remove()
    }
}