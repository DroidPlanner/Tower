package org.droidplanner.android.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by fredia on 3/10/16.
 */
public class OkDialog extends DialogFragment {

    private final static boolean DEFAULT_DISMISS_DIALOG_WITHOUT_CLICKING = true;

    protected final static String EXTRA_TITLE = "title";
    protected final static String EXTRA_MESSAGE = "message";
    protected final static String EXTRA_BUTTON_LABEL = "button_label";
    protected final static String EXTRA_DISMISS_DIALOG_WITHOUT_CLICKING = "dismiss_dialog_without_clicking";
    protected final static String EXTRA_SHOW_CANCEL = "show_cancel";

    public interface Listener {
        void onOk();

        void onCancel();

        void onDismiss();
    }

    public static OkDialog newInstance(Context context, String title, String msg) {
        return newInstance(context, title, msg, null);
    }

    public static OkDialog newInstance(Context context, String title, String msg, Listener listener) {
        return newInstance(context, title, msg, listener, false);
    }

    public static OkDialog newInstance(Context context, String title, String msg, Listener listener, boolean showCancel) {
        return newInstance(title, msg, DEFAULT_DISMISS_DIALOG_WITHOUT_CLICKING, context.getString(android.R.string.ok), listener, showCancel);
    }

    public static OkDialog newInstance(String title, String msg, String buttonLabel) {
        return newInstance(title, msg, DEFAULT_DISMISS_DIALOG_WITHOUT_CLICKING, buttonLabel, null, false);
    }


    public static OkDialog newInstance(String title, String msg, boolean dismissDialogWithoutClicking,
                                       String buttonLabel) {
        return newInstance(title, msg, dismissDialogWithoutClicking, buttonLabel, null, false);
    }

    public static OkDialog newInstance(String title, String msg, boolean dismissDialogWithoutClicking,
                                       String buttonLabel, Listener listener, boolean showCancel) {
        OkDialog fragment = new OkDialog();
        Bundle b = new Bundle();
        b.putString(EXTRA_TITLE, title);
        b.putString(EXTRA_MESSAGE, msg);
        b.putString(EXTRA_BUTTON_LABEL, buttonLabel);
        b.putBoolean(EXTRA_DISMISS_DIALOG_WITHOUT_CLICKING, dismissDialogWithoutClicking);
        b.putBoolean(EXTRA_SHOW_CANCEL, showCancel);

        fragment.setArguments(b);
        fragment.listener = listener;
        return fragment;
    }

    private boolean dismissDialogWithoutClicking;
    private Listener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();

        final String title = args.getString(EXTRA_TITLE, "");
        final String message = args.getString(EXTRA_MESSAGE, "");
        final String buttonLabel = args.getString(EXTRA_BUTTON_LABEL, getString(android.R.string.ok));
        dismissDialogWithoutClicking = args.getBoolean(EXTRA_DISMISS_DIALOG_WITHOUT_CLICKING, DEFAULT_DISMISS_DIALOG_WITHOUT_CLICKING);

        boolean showCancel = args.getBoolean(EXTRA_SHOW_CANCEL);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setMessage(message)
            ;
        if (showCancel) {
            builder
                .setPositiveButton(buttonLabel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (listener != null) {
                                listener.onOk();
                            }
                            dismiss();
                        }
                    }
                )
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(listener != null){
                            listener.onCancel();
                        }
                        dismiss();
                    }
                });
        } else {
            builder.setNeutralButton(buttonLabel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (listener != null) {
                            listener.onOk();
                        }
                        dismiss();
                    }
                }
            );
        }

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if(dialog != null) {
            dialog.setCanceledOnTouchOutside(dismissDialogWithoutClicking);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (listener != null) {
                        listener.onCancel();
                    }
                }
            });

            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (listener != null) {
                        listener.onDismiss();
                    }
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Dialog dialog = getDialog();
        if(dialog != null) {
            dialog.setOnDismissListener(null);
        }

        dismissAllowingStateLoss();
    }

    public void removeListener() {
        listener = null;
    }
}

