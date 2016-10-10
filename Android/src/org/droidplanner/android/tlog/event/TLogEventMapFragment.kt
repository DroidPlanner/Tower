package org.droidplanner.android.tlog.event

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.widget.Toast
import com.MAVLink.common.msg_global_position_int
import com.o3dr.android.client.utils.data.tlog.TLogParser
import com.o3dr.services.android.lib.coordinate.LatLong
import org.droidplanner.android.R
import org.droidplanner.android.droneshare.data.SessionContract
import org.droidplanner.android.fragments.DroneMap
import org.droidplanner.android.maps.MarkerInfo
import org.droidplanner.android.maps.PolylineInfo
import org.droidplanner.android.tlog.interfaces.TLogDataSubscriber
import org.droidplanner.android.tlog.viewers.TLogPositionViewer
import org.droidplanner.android.utils.prefs.AutoPanMode
import java.util.*

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogEventMapFragment : DroneMap(), TLogDataSubscriber, TLogEventListener {

    private val eventsPolylineInfo = TLogEventsPolylineInfo()
    private val selectedPositionMarkerInfo = GlobalPositionMarkerInfo()

    override fun isMissionDraggable() = false

    override fun setAutoPanMode(target: AutoPanMode?): Boolean {
        return when(target){
            AutoPanMode.DISABLED -> true
            else -> {
                Toast.makeText(activity, "Auto pan is not supported on this map.",
                        Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    override fun shouldUpdateMission(): Boolean {
        return false
    }

    override fun onApiConnected() {
        super.onApiConnected()
        eventsPolylineInfo.update(this)
        selectedPositionMarkerInfo.updateMarker(this)
    }

    override fun onTLogDataLoaded(events: List<TLogParser.Event>, hasMore: Boolean) {
        for(event in events){
            val coord = TLogPositionViewer.tlogEventToSpaceTime(event) ?: continue
            eventsPolylineInfo.addCoord(coord)
        }
        eventsPolylineInfo.update(this)
    }

    override fun onClearTLogData() {
        eventsPolylineInfo.clear()
        eventsPolylineInfo.update(this)

        selectedPositionMarkerInfo.selectedGlobalPosition = null
        selectedPositionMarkerInfo.updateMarker(this)
    }

    override fun onTLogEventSelected(event: TLogParser.Event?) {
        if(event == null){
            selectedPositionMarkerInfo.selectedGlobalPosition = null
            zoomToFit()
        }
        else{
            //Add a marker for the selected event
            val globalPositionInt = event.mavLinkMessage as msg_global_position_int
            selectedPositionMarkerInfo.selectedGlobalPosition = globalPositionInt
            mMapFragment.zoomToFit(listOf(LatLong(globalPositionInt.lat.toDouble()/ 1E7, globalPositionInt.lon.toDouble()/ 1E7)))
        }
        selectedPositionMarkerInfo.updateMarker(this)
    }

    internal fun zoomToFit(){
        eventsPolylineInfo.zoomToFit(this)
    }

    override fun onTLogSelected(tlogSession: SessionContract.SessionData) {
        eventsPolylineInfo.clear()
        eventsPolylineInfo.update(this)

        selectedPositionMarkerInfo.selectedGlobalPosition = null
        selectedPositionMarkerInfo.updateMarker(this)
    }

    private class TLogEventsPolylineInfo : PolylineInfo() {

        private val eventCoords = ArrayList<LatLong>()

        fun clear(refreshPolyline: Boolean = false) {
            eventCoords.clear()
            if(refreshPolyline) {
                updatePolyline()
            }
        }

        fun addCoord(coord: LatLong){
            eventCoords.add(coord)
        }

        fun update(mapHandle: TLogEventMapFragment) {
            if(isOnMap()) {
                updatePolyline()
            }
            else {
                if(eventCoords.isNotEmpty())
                    mapHandle.addPolyline(this)
            }
            zoomToFit(mapHandle)
        }

        fun zoomToFit(mapHandle: TLogEventMapFragment){
            mapHandle.mMapFragment.zoomToFit(eventCoords)
        }

        override fun getPoints(): List<LatLong> {
            return eventCoords
        }

        override fun getColor() = 0xfffd693f.toInt()

    }

    private class GlobalPositionMarkerInfo : MarkerInfo() {
        var selectedGlobalPosition : msg_global_position_int? = null

        override fun isVisible() = selectedGlobalPosition != null

        override fun getPosition() =
                if(selectedGlobalPosition == null) null
                else LatLong(selectedGlobalPosition!!.lat.toDouble() / 1E7, selectedGlobalPosition!!.lon.toDouble() / 1E7)

        override fun getRotation(): Float {
            val headingx100 = selectedGlobalPosition?.hdg ?: return 0f
            val heading = headingx100 / 100f
            if (heading < 0f || heading > 360f)
                return 0f
            return heading
        }

        override fun getIcon(res: Resources) = BitmapFactory.decodeResource(res, R.drawable.quad_disconnect)
    }
}