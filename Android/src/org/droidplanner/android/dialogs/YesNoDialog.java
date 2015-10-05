package org.droidplanner.android.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import org.droidplanner.android.R;

public class YesNoDialog extends DialogFragment {

    protected final static String EXTRA_DIALOG_TAG = "extra_dialog_tag";
    protected final static String EXTRA_TITLE = "title";
    protected final static String EXTRA_MESSAGE = "message";
    protected final static String EXTRA_POSITIVE_LABEL = "positive_label";
    protected final static String EXTRA_NEGATIVE_LABEL = "negative_label";

    public interface Listener {
        void onYes(String dialogTag);

        void onNo(String dialogTag);
    }

    public static YesNoDialog newInstance(Context context, String dialogTag, String title, String msg) {
        if (dialogTag == null)
            throw new IllegalArgumentException("The dialog tag must not be null!");

        YesNoDialog f = new YesNoDialog();

        Bundle b = new Bundle();
        b.putString(EXTRA_DIALOG_TAG, dialogTag);
        b.putString(EXTRA_TITLE, title);
        b.putString(EXTRA_MESSAGE, msg);
        b.putString(EXTRA_POSITIVE_LABEL, context.getString(android.R.string.yes));
        b.putString(EXTRA_NEGATIVE_LABEL, context.getString(android.R.string.no));

        f.setArguments(b);
        return f;
    }

    protected Listener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Object parent = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            parent = getParentFragment();

        if(parent == null)
            parent = activity;

        if (!(parent instanceof Listener)) {
            throw new IllegalStateException("Parent activity must implement " + Listener.class.getName());
        }

        mListener = (Listener) parent;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return buildDialog(savedInstanceState).create();
    }

    protected AlertDialog.Builder buildDialog(Bundle savedInstanceState) {
        final Bundle arguments = getArguments();

        final String dialogTag = arguments.getString(EXTRA_DIALOG_TAG);

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.arma)
                .setTitle(arguments.getString(EXTRA_TITLE))
                .setView(generateContentView(savedInstanceState))
                .setPositiveButton(arguments.getString(EXTRA_POSITIVE_LABEL), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null)
                            mListener.onYes(dialogTag);
                    }
                })
                .setNegativeButton(arguments.getString(EXTRA_NEGATIVE_LABEL), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null)
                            mListener.onNo(dialogTag);
                    }
                });

        return b;
    }

    protected View generateContentView(Bundle savedInstanceState) {
        final View contentView = getActivity().getLayoutInflater().inflate(R.layout.dialog_yes_no_content, null);

        if (contentView == null) {
            return contentView;
        }

        final TextView messageView = (TextView) contentView.findViewById(R.id.yes_no_message);
        messageView.setText(getArguments().getString(EXTRA_MESSAGE));

        return contentView;
    }
}
