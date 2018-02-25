package com.o3dr.services.android.lib.drone.companion.solo.action;

/**
 * Created by Fredia Huya-Kouadio on 7/10/15.
 */
public class SoloActions {

    //Private to prevent instantiation
    private SoloActions() {
    }

    private static final String PACKAGE_NAME = "com.o3dr.services.android.lib.drone.companion.solo.action";

    public static final String ACTION_SEND_MESSAGE = PACKAGE_NAME + ".SEND_MESSAGE";

    /**
     * TLV message object to send to the sololink companion computer.
     *
     * @see {@link com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket}
     */
    public static final String EXTRA_MESSAGE_DATA = "extra_message_data";

}
