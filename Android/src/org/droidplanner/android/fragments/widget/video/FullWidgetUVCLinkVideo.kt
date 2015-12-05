package org.droidplanner.android.fragments.widget



import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.property.State
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

        textureView?.setOnClickListener {
            when (aspectRatio) {
                ASPECT_RATIO_4_3 -> {
                    aspectRatio = ASPECT_RATIO_16_9
                    Toast.makeText(context, "Aspect Ratio 16:9", Toast.LENGTH_SHORT).show()
                }
                ASPECT_RATIO_16_9 -> {
                    aspectRatio = ASPECT_RATIO_21_9
                    Toast.makeText(context, "Aspect Ratio 21:9", Toast.LENGTH_SHORT).show()
                }
                ASPECT_RATIO_21_9 -> {
                    aspectRatio = ASPECT_RATIO_1_1
                    Toast.makeText(context, "Aspect Ratio 1:1", Toast.LENGTH_SHORT).show()
                }
                ASPECT_RATIO_1_1 -> {
                    aspectRatio = ASPECT_RATIO_4_3
                    Toast.makeText(context, "Aspect Ratio 4:3", Toast.LENGTH_SHORT).show()
                }
            }
            adjustAspectRatio(textureView as TextureView)
        }

        takePhotoButton?.visibility = View.GONE
        recordVideo?.visibility = View.GONE
        widgetButtonBar?.visibility = View.INVISIBLE


    }

}