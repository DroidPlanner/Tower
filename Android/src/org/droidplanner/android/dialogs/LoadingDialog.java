package org.droidplanner.android.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import org.droidplanner.android.R;

/**
 * Created by fhuya on 5/10/2016.
 */
public class LoadingDialog extends DialogFragment {

    protected final static String EXTRA_TITLE = "title";

    public interface Listener {
        void onStarted();

        void onCancel();

        void onDismiss();
    }

    public static LoadingDialog newInstance(String title, Listener listener){
        LoadingDialog dialog = new LoadingDialog();
        Bundle b = new Bundle();
        b.putString(EXTRA_TITLE, title);

        dialog.setArguments(b);
        dialog.listener = listener;
        return dialog;
    }

    private Listener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Bundle args = getArguments();

        final String title = args.getString(EXTRA_TITLE, "");
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(R.layout.fragment_loading_dialog)
                .create();
    }

    @Override
    public void onStart(){
        super.onStart();
        Dialog dialog = getDialog();
        if(dialog != null){
            dialog.setCanceledOnTouchOutside(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if(listener != null){
                        listener.onCancel();
                    }
                }
            });

            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if(listener != null){
                        listener.onDismiss();
                    }
                }
            });
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(listener != null){
            listener.onStarted();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        Dialog dialog = getDialog();
        if(dialog != null){
            dialog.setOnDismissListener(null);
        }

        dismissAllowingStateLoss();
    }
}
