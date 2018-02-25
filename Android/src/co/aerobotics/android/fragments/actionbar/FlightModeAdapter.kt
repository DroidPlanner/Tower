package co.aerobotics.android.fragments.actionbar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import com.google.android.gms.analytics.HitBuilders
import com.o3dr.android.client.Drone
import com.o3dr.android.client.apis.VehicleApi
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.property.State
import com.o3dr.services.android.lib.drone.property.Type
import com.o3dr.services.android.lib.drone.property.VehicleMode

/**
 * Created by Fredia Huya-Kouadio on 9/25/15.
 */
class FlightModeAdapter(context: Context, val drone: Drone) : co.aerobotics.android.fragments.actionbar.SelectionListAdapter<VehicleMode>(context) {

    private var selectedMode: VehicleMode
    private val flightModes : List<VehicleMode>

    init {
        val state: State = drone.getAttribute(AttributeType.STATE)
        selectedMode = state.vehicleMode

        val type: Type = drone.getAttribute(AttributeType.TYPE)
        flightModes = VehicleMode.getVehicleModePerDroneType(type.droneType)
    }

    override fun getCount() = flightModes.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View{
        val vehicleMode = flightModes[position]

        val containerView = convertView ?: LayoutInflater.from(parent.context).inflate(co.aerobotics.android.R.layout.item_selection, parent, false)

        val holder = (containerView.tag as ViewHolder?) ?: ViewHolder(containerView.findViewById(co.aerobotics.android.R.id.item_selectable_option) as TextView,
                containerView.findViewById(co.aerobotics.android.R.id.item_selectable_check) as RadioButton)

        val clickListener = object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (drone.isConnected) {
                    selectedMode = vehicleMode

                    holder.checkView.isChecked = true

                    VehicleApi.getApi(drone).setVehicleMode(vehicleMode)

                    //Record the attempt to change flight modes
                    val eventBuilder = HitBuilders.EventBuilder().setCategory(co.aerobotics.android.utils.analytics.GAUtils.Category.FLIGHT).setAction("Flight mode changed").setLabel(vehicleMode.label)
                    co.aerobotics.android.utils.analytics.GAUtils.sendEvent(eventBuilder)

                    listener?.onSelection()
                }
            }
        }

        holder.checkView.isChecked = vehicleMode === selectedMode
        holder.checkView.setOnClickListener(clickListener)

        holder.labelView.text = vehicleMode.label
        holder.labelView.setOnClickListener(clickListener)

        containerView.setOnClickListener(clickListener)

        containerView.tag = holder
        return containerView
    }

    override fun getSelection() = flightModes.indexOf(selectedMode)

    class ViewHolder(val labelView: TextView, val checkView: RadioButton)
}