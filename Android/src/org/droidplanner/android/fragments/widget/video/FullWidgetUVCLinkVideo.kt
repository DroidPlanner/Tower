package org.droidplanner.android.fragments.widget



import android.os.Bundle
import android.view.*
import android.widget.Toast
import org.droidplanner.android.R
import org.droidplanner.android.fragments.widget.video.BaseUVCVideoWidget



public class FullWidgetUVCLinkVideo : BaseUVCVideoWidget() {

    private val widgetButtonBar by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.widget_button_bar)
    }

    private val takePhotoButton by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.uvc_take_picture_button)
    }

    private val recordVideo by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.uvc_record_video_button)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_widget_uvc_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        takePhotoButton?.setOnClickListener {
            Toast.makeText(context, "NOT implemented YET", Toast.LENGTH_SHORT).show()
        }

        recordVideo?.setOnClickListener {
            Toast.makeText(context, "NOT implemented YET", Toast.LENGTH_SHORT).show()
        }

        widgetButtonBar?.visibility = View.INVISIBLE

    }

}