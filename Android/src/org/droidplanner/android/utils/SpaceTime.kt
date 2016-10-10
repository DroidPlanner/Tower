package org.droidplanner.android.utils

import android.os.Parcel
import android.os.Parcelable
import com.o3dr.services.android.lib.coordinate.LatLongAlt

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class SpaceTime(latitude: Double, longitude: Double, altitude: Double, var timeInMs: Long) :
        LatLongAlt(latitude, longitude, altitude) {

    constructor(space: LatLongAlt, timeInMs: Long): this(space.latitude, space.longitude, space.altitude, timeInMs)

    constructor(spaceTime: SpaceTime): this(spaceTime.latitude, spaceTime.longitude, spaceTime.altitude, spaceTime.timeInMs)

    fun set(reference: SpaceTime) {
        super.set(reference)
        timeInMs = reference.timeInMs
    }

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other !is SpaceTime) return false
        if (!super.equals(other)) return false

        if (timeInMs != other.timeInMs) return false

        return true
    }

    override fun hashCode(): Int{
        var result = super.hashCode()
        result = 31 * result + timeInMs.hashCode()
        return result
    }

    override fun toString(): String{
        val superToString = super.toString()
        return "SpaceTime{$superToString, time=$timeInMs}"
    }

    companion object {
        @JvmStatic val CREATOR = object : Parcelable.Creator<SpaceTime> {
            override fun createFromParcel(source: Parcel): SpaceTime {
                return source.readSerializable() as SpaceTime
            }

            override fun newArray(size: Int): Array<out SpaceTime?> {
                return arrayOfNulls(size)
            }

        }
    }

}