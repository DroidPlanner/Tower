package co.aerobotics.android.fragments.widget.video



import android.os.Bundle
import android.view.*
import co.aerobotics.android.fragments.widget.video.BaseUVCVideoWidget


class MiniWidgetUVCLinkVideo : BaseUVCVideoWidget() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(co.aerobotics.android.R.layout.fragment_mini_widget_uvc_video, container, false)
        aspectRatio = ASPECT_RATIO_16_9
        adjustAspectRatio(textureView as TextureView)
    }

}