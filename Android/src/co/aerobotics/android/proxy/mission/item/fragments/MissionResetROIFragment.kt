package co.aerobotics.android.proxy.mission.item.fragments

import com.o3dr.services.android.lib.drone.mission.MissionItemType

/**
 * Created by Fredia Huya-Kouadio on 10/20/15.
 */
class MissionResetROIFragment : co.aerobotics.android.proxy.mission.item.fragments.MissionDetailFragment() {

    override fun getResource() = co.aerobotics.android.R.layout.fragment_editor_detail_reset_roi

    override fun onApiConnected(){
        super.onApiConnected()
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.RESET_ROI))
    }
}