package org.droidplanner.android.fragments.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * This is a utility class to quickly create dialog fragment for use.
 */
public class GenericDialogFragment extends DialogFragment {

    public static GenericDialogFragment newInstance(String title, String msg,
                                                    String positiveButtonText, Runnable positiveCb,
                                                    String negativeButtonText,
                                                    Runnable negativeCb) {
        GenericDialogFragment dialog = new GenericDialogFragment();
        dialog.mTitle = title;
        dialog.mMsg = msg;
        dialog.mPositiveButtonText = positiveButtonText;
        dialog.mPositiveCallback = positiveCb;
        dialog.mNegativeButtonText = negativeButtonText;
        dialog.mNegativeCallback = negativeCb;
        return dialog;
    }

    private String mTitle;
    private String mMsg;
    private String mPositiveButtonText;
    private String mNegativeButtonText;

    private Runnable mPositiveCallback;
    private Runnable mNegativeCallback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        if(mTitle != null) {
            builder.setTitle(mTitle);
        }

        if(mMsg != null) {
            builder.setMessage(mMsg);
        }

        if(mPositiveButtonText != null) {
            builder.setPositiveButton(mPositiveButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mPositiveCallback != null) {
                        mPositiveCallback.run();
                    }
                }
            });
        }

        if(mNegativeButtonText != null){
            builder.setNegativeButton(mNegativeButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(mNegativeCallback != null){
                        mNegativeCallback.run();
                    }
                }
            });
        }

        return builder.create();
    }
}
