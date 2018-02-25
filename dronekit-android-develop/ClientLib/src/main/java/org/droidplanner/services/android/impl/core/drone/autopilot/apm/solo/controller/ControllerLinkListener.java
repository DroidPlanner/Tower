package org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.controller;

import org.droidplanner.services.android.impl.core.drone.autopilot.apm.solo.AbstractLinkManager;
import com.o3dr.services.android.lib.drone.companion.solo.button.ButtonPacket;
import com.o3dr.services.android.lib.drone.companion.solo.controller.SoloControllerUnits;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;

/**
 * Created by Fredia Huya-Kouadio on 7/10/15.
 */
public interface ControllerLinkListener extends AbstractLinkManager.LinkListener {

    void onTlvPacketReceived(TLVPacket packet);

    void onWifiInfoUpdated(String wifiName, String wifiPassword);

    void onButtonPacketReceived(ButtonPacket packet);

    void onTxPowerComplianceCountryUpdated(String compliantCountry);

    void onControllerModeUpdated();

    void onControllerUnitUpdated(@SoloControllerUnits.ControllerUnit String trimmedResponse);
}
