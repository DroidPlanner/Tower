package org.droidplanner.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class YesNoDialogFragment extends DialogFragment {
    
    public interface Listener {
        void onOK();
        void onCancel();
    }
    
    public static YesNoDialogFragment newInstance(
            String title, String msg, String ok, String cancel, Listener listener) {
        YesNoDialogFragment f = new YesNoDialogFragment();
        Bundle b = new Bundle();
        b.putString("title", title);
        b.putString("message", msg);
        b.putString("okButton", ok);
        b.putString("cancelButton", cancel);
        f.setArguments(b);
        f.mListener = listener;
        return f;
    }
    
    Listener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(getArguments().getString("title"))
            .setMessage(getArguments().getString("message"))
            .setPositiveButton(getArguments().getString("okButton"), 
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(mListener != null) {
                        mListener.onOK();
                    }
                }
            })
            .setNegativeButton(getArguments().getString("cancelButton"), 
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(mListener != null) {
                        mListener.onCancel();
                    }
                }
            });
            
        return b.create();
    }
}
