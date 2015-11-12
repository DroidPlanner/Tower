package org.droidplanner.android.fragments.widget


import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.view.*
import com.serenegiant.usb.USBMonitor
import org.droidplanner.android.R
import org.droidplanner.android.fragments.widget.video.BaseUVCVideoWidget
import kotlin.properties.Delegates


public class MiniWidgetUVCLinkVideo : BaseUVCVideoWidget() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_mini_widget_uvc_video, container, false)
    }

    override fun onAttach(device: UsbDevice?) {
        super.onAttach(device);
    }

    override fun onConnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock, createNew: Boolean) {
        super.onConnect(device, ctrlBlock, createNew)
    }

    override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock) {
        super.onDisconnect(device, ctrlBlock)
    }

    override fun onDettach(device: UsbDevice?) {
        super.onDettach(device)
    }

    override fun onCancel() {
        super.onCancel()
    }

}