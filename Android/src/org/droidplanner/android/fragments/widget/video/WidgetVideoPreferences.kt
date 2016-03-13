package org.droidplanner.android.fragments.widget.video

import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.support.annotation.IntDef
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import org.droidplanner.android.R
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs
import timber.log.Timber

/**
 * Created by Fredia Huya-Kouadio on 10/18/15.
 */
class WidgetVideoPreferences : DialogFragment() {

    companion object {
        const val SOLO_VIDEO_TYPE = 0
        const val CUSTOM_VIDEO_TYPE = 1
    }

    @IntDef(SOLO_VIDEO_TYPE.toLong(), CUSTOM_VIDEO_TYPE.toLong())
    @Retention(AnnotationRetention.SOURCE)
    annotation class VideoType

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_widget_video_preferences, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = activity.applicationContext
        val appPrefs = DroidPlannerPrefs.getInstance(context)

        val udpPortView = view.findViewById(R.id.custom_video_provider_udp_port) as EditText?

        fun hideSoftInput() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isActive(udpPortView)) {
                imm.hideSoftInputFromWindow(udpPortView?.getWindowToken(), 0)
            }
        }

        val currentUdpPort = appPrefs.customVideoUdpPort
        val currentEntry = if(currentUdpPort == -1) "" else "$currentUdpPort"
        udpPortView?.setText(currentEntry)

        udpPortView?.setOnEditorActionListener { textView, actionId, keyEvent ->
            when(actionId){
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_NULL -> {
                    hideSoftInput()

                    val entry = textView.text
                    try {
                        if (!TextUtils.isEmpty(entry)) {
                            val udpPort = entry.toString().toInt();
                            appPrefs.customVideoUdpPort = udpPort
                        }
                    }catch(e: NumberFormatException){
                        Timber.e(e, "Invalid udp port value: %s", entry)
                        Toast.makeText(context, "Invalid udp port!", Toast.LENGTH_LONG).show()

                        val currentUdpPort = appPrefs.customVideoUdpPort
                        val currentEntry = if(currentUdpPort == -1) "" else "$currentUdpPort"
                        udpPortView.setText(currentEntry)
                    }
                }
            }
            true
        }

        val radioGroup = view.findViewById(R.id.video_widget_pref) as RadioGroup?
        radioGroup?.setOnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                R.id.solo_video_stream_check -> {
                    udpPortView?.isEnabled = false
                    appPrefs.videoWidgetType = SOLO_VIDEO_TYPE
                }

                R.id.custom_video_stream_check -> {
                    udpPortView?.isEnabled = true
                    appPrefs.videoWidgetType = CUSTOM_VIDEO_TYPE
                }
            }
        }

        val currentVideoType = appPrefs.videoWidgetType
        when(currentVideoType){
            SOLO_VIDEO_TYPE -> radioGroup?.check(R.id.solo_video_stream_check)
            CUSTOM_VIDEO_TYPE -> radioGroup?.check(R.id.custom_video_stream_check)
        }
    }
}