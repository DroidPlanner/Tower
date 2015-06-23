package org.droidplanner.android.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import org.droidplanner.android.R;

/**
 * Created by Fredia Huya-Kouadio on 6/17/15.
 */
public class SupportYesNoDialog extends DialogFragment {

    protected final static String EXTRA_TITLE = "title";
    protected final static String EXTRA_MESSAGE = "message";
    protected final static String EXTRA_POSITIVE_LABEL = "positive_label";
    protected final static String EXTRA_NEGATIVE_LABEL = "negative_label";

    public interface Listener {
        void onYes();

        void onNo();
    }

    public static SupportYesNoDialog newInstance(Context context, String title, String msg,
                                          Listener listener) {
        SupportYesNoDialog f = new SupportYesNoDialog();
        Bundle b = new Bundle();
        b.putString(EXTRA_TITLE, title);
        b.putString(EXTRA_MESSAGE, msg);
        b.putString(EXTRA_POSITIVE_LABEL, context.getString(android.R.string.yes));
        b.putString(EXTRA_NEGATIVE_LABEL, context.getString(android.R.string.no));

        f.setArguments(b);
        f.mListener = listener;
        return f;
    }

    protected Listener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return buildDialog(savedInstanceState).create();
    }

    protected AlertDialog.Builder buildDialog(Bundle savedInstanceState){
        final Bundle arguments = getArguments();

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.arma)
                .setTitle(arguments.getString(EXTRA_TITLE))
                .setView(generateContentView(savedInstanceState))
                .setPositiveButton(arguments.getString(EXTRA_POSITIVE_LABEL), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onYes();
                    }
                })
                .setNegativeButton(arguments.getString(EXTRA_NEGATIVE_LABEL), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onNo();
                    }
                });

        return b;
    }

    protected View generateContentView(Bundle savedInstanceState){
        final View contentView = getActivity().getLayoutInflater().inflate(R.layout.dialog_yes_no_content, null);

        if(contentView == null){
            return contentView;
        }

        final TextView messageView = (TextView) contentView.findViewById(R.id.yes_no_message);
        messageView.setText(getArguments().getString(EXTRA_MESSAGE));

        return contentView;
    }
}
