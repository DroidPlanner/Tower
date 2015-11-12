package org.droidplanner.android.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Spinner;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;

import org.droidplanner.android.R;

import java.util.ArrayList;
import java.util.List;



public class UVCDialog extends DialogFragment {

    private static final String TAG = UVCDialog.class.getSimpleName();


    protected USBMonitor mUSBMonitor;
    private Spinner mSpinner;
    private DeviceListAdapter mDeviceListAdapter;

    /**
     * Helper method
     * @param parent FragmentActivity
     * @return
     */
    public static UVCDialog showDialog(final Activity parent) {
        UVCDialog dialog = newInstance();
        try {
            dialog.show(parent.getFragmentManager(), TAG);
        } catch (final IllegalStateException e) {
            dialog = null;
        }
        return dialog;
    }

    /**
     * Helper method
     * @param parent FragmentActivity
     * @return
     */
    public static UVCDialog showDialog(final Activity parent, USBMonitor mUSBMonitor) {
        UVCDialog dialog = newInstance();
        dialog.mUSBMonitor = mUSBMonitor;
        try {
            dialog.show(parent.getFragmentManager(), TAG);
        } catch (final IllegalStateException e) {
            dialog = null;
        }
        return dialog;
    }

    public static UVCDialog newInstance() {
        final UVCDialog dialog = new UVCDialog();
        final Bundle args = new Bundle();
        dialog.setArguments(args);
        return dialog;
    }

    public UVCDialog() {

    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
            savedInstanceState = getArguments();
    }

    @Override
    public void onSaveInstanceState(final Bundle saveInstanceState) {
        final Bundle args = getArguments();
        if (args != null) saveInstanceState.putAll(args);
        super.onSaveInstanceState(saveInstanceState);
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(initView());
        builder.setTitle(R.string.uvc_device_select);
        builder.setPositiveButton(android.R.string.ok, mOnDialogClickListener);
        builder.setNegativeButton(android.R.string.cancel , mOnDialogClickListener);
        builder.setNeutralButton(R.string.uvc_device_refresh, null);
        final Dialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    /**
     * create view that this fragment shows
     * @return
     */
    private final View initView() {
        final View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_uvc_device, null);
        mSpinner = (Spinner)rootView.findViewById(R.id.spinner1);
        final View empty = rootView.findViewById(android.R.id.empty);
        mSpinner.setEmptyView(empty);
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        updateDevices();
        final Button button = (Button)getDialog().findViewById(android.R.id.button3);
        if (button != null) {
            button.setOnClickListener(mOnClickListener);
        }
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case android.R.id.button3:
                    updateDevices();
                    break;
            }
        }
    };

    private final DialogInterface.OnClickListener mOnDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(final DialogInterface dialog, final int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    final Object item = mSpinner.getSelectedItem();
                    if (item instanceof UsbDevice) {
                        mUSBMonitor.requestPermission((UsbDevice)item);
                    }
                    break;
            }
        }
    };

    public void updateDevices() {
        mUSBMonitor.dumpDevices();
        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(getActivity(), R.xml.uvc_device_filter);
        mDeviceListAdapter = new DeviceListAdapter(getActivity(), mUSBMonitor.getDeviceList(filter.get(0)));
        mSpinner.setAdapter(mDeviceListAdapter);
    }

    private static final class DeviceListAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private final List<UsbDevice> mList;

        public DeviceListAdapter(final Context context, final List<UsbDevice>list) {
            mInflater = LayoutInflater.from(context);
            mList = list != null ? list : new ArrayList<UsbDevice>();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public UsbDevice getItem(final int position) {
            if ((position >= 0) && (position < mList.size()))
                return mList.get(position);
            else
                return null;
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_uvc_device, parent, false);
            }
            if (convertView instanceof CheckedTextView) {
                final UsbDevice device = getItem(position);
                ((CheckedTextView)convertView).setText(
                        String.format("(%x:%x:%s)", device.getVendorId(), device.getProductId(), device.getDeviceName()));
            }
            return convertView;
        }
    }
}
