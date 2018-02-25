package org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.sololink;

import org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.AbstractLinkManager;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSetting;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

/**
 * Created by Fredia Huya-Kouadio on 7/10/15.
 */
public interface SoloLinkListener extends AbstractLinkManager.LinkListener {

    void onTlvPacketReceived(TLVPacket packet);

    void onPresetButtonLoaded(int buttonType, SoloButtonSetting buttonSettings);
}
