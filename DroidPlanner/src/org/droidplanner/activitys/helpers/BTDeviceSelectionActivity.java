package org.droidplanner.activitys.helpers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import org.droidplanner.R;
import org.droidplanner.fragments.helpers.BTDeviceListFragment;
import org.droidplanner.glass.fragments.BTDeviceCardsFragment;
import org.droidplanner.glass.utils.GlassUtils;

/**
 * This class is used to handle the selection of bluetooth devices on connection request.
 *
 * @author Fredia Huya-Kouadio
 * @since 1.2.0
 */
public class BTDeviceSelectionActivity extends SuperActivity {

    /**
     * This tag is used for logging.
     *
     * @since 1.2.0
     */
    private static final String TAG = BTDeviceSelectionActivity.class.getName();

    /**
     * Request code used in onActivityResult to check for bluetooth activation result.
     *
     * @since 1.2.0
     */
    public static final int REQUEST_ENABLE_BT = 111;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Update the theme if we're on a glass device
        final boolean isGlassDevice = GlassUtils.isGlassDevice();
        if (isGlassDevice) {
            setTheme(android.R.style.Theme_DeviceDefault);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_device_selection);

        final FragmentManager fm = getSupportFragmentManager();
        Fragment deviceSelection = fm.findFragmentById(R.id.bt_device_selection_layout);
        if (deviceSelection == null) {
            deviceSelection = isGlassDevice ? new BTDeviceCardsFragment() : new
                    BTDeviceListFragment();
            fm.beginTransaction().add(R.id.bt_device_selection_layout, deviceSelection).commit();
        }
    }

    @Override
    public CharSequence[][] getHelpItems() {
        return new CharSequence[0][];
    }
}