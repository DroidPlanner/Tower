package co.aerobotics.android.fragments.widget

import android.app.DialogFragment
import android.app.FragmentManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

/**
 * Created by Fredia Huya-Kouadio on 10/18/15.
 */
class WidgetsListPrefFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, co.aerobotics.android.R.style.CustomDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(co.aerobotics.android.R.layout.fragment_widgets_list_pref, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val widgetsListPref = view.findViewById(co.aerobotics.android.R.id.widgets_list_pref) as ListView?
        widgetsListPref?.adapter = WidgetsAdapter(activity.applicationContext, fragmentManager)
    }

    class WidgetsAdapter(context: Context, val fm : FragmentManager) : ArrayAdapter<TowerWidgets>(context, 0, TowerWidgets.enabledWidgets()){

        val appPrefs = co.aerobotics.android.utils.prefs.DroidPlannerPrefs.getInstance(context)
        val lbm = LocalBroadcastManager.getInstance(context)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val towerWidget = getItem(position)
            val view = convertView ?: LayoutInflater.from(parent.context).inflate(co.aerobotics.android.R.layout.list_widgets_list_pref_item, parent, false)

            var viewHolder = view.tag as ViewHolder?
            if(viewHolder == null){
                viewHolder = ViewHolder(view.findViewById(co.aerobotics.android.R.id.widget_pref_icon) as ImageView?,
                        view.findViewById(co.aerobotics.android.R.id.widget_check) as CheckBox?,
                        view.findViewById(co.aerobotics.android.R.id.widget_pref_title) as TextView?,
                        view.findViewById(co.aerobotics.android.R.id.widget_pref_summary) as TextView?,
                        view.findViewById(co.aerobotics.android.R.id.widget_pref_info))
            }

            viewHolder.prefIcon?.visibility = if(towerWidget.hasPreferences()) View.VISIBLE else View.GONE
            viewHolder.prefIcon?.setOnClickListener { towerWidget.getPrefFragment()?.show(fm, "Widget pref dialog") }

            viewHolder.prefTitle?.setText(towerWidget.labelResId)
            viewHolder.prefSummary?.setText(towerWidget.descriptionResId)

            viewHolder.prefCheck?.setOnCheckedChangeListener(null)
            viewHolder.prefCheck?.isChecked = appPrefs.isWidgetVisible(towerWidget)
            viewHolder.prefCheck?.setOnCheckedChangeListener { compoundButton, b ->
                appPrefs.enableWidget(towerWidget, b)
                lbm.sendBroadcast(Intent(co.aerobotics.android.fragments.SettingsFragment.ACTION_WIDGET_PREFERENCE_UPDATED)
                        .putExtra(co.aerobotics.android.fragments.SettingsFragment.EXTRA_ADD_WIDGET, b)
                        .putExtra(co.aerobotics.android.fragments.SettingsFragment.EXTRA_WIDGET_PREF_KEY, towerWidget.prefKey))
            }

            viewHolder.prefInfo?.setOnClickListener { viewHolder?.prefCheck?.toggle() }

            view.tag = viewHolder

            return view
        }

        class ViewHolder(val prefIcon: ImageView?, val prefCheck: CheckBox?, val prefTitle : TextView?, val prefSummary: TextView?, val prefInfo: View?)
    }
}