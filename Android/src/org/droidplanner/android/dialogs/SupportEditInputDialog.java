package org.droidplanner.android.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import org.droidplanner.android.R;

/**
 * Created by Fredia Huya-Kouadio on 6/17/15.
 */
public class SupportEditInputDialog extends DialogFragment {

    protected final static String EXTRA_DIALOG_TAG = "extra_dialog_tag";
    protected final static String EXTRA_TITLE = "title";
    protected static final String EXTRA_HINT = "hint";
    protected static final String EXTRA_HINT_IS_VALID_ENTRY = "extra_hint_is_valid_entry";

    public interface Listener {
        void onOk(String dialogTag, final CharSequence input);

        void onCancel(String dialogTag);
    }

    public static SupportEditInputDialog newInstance(String dialogTag, String title, String hint, boolean hintIsValidEntry){
        if (dialogTag == null)
            throw new IllegalArgumentException("The dialog tag must not be null!");

        SupportEditInputDialog dialog = new SupportEditInputDialog();

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_DIALOG_TAG, dialogTag);
        bundle.putString(EXTRA_TITLE, title);
        bundle.putBoolean(EXTRA_HINT_IS_VALID_ENTRY, hintIsValidEntry);

        if(hint == null){
            hint = "";
        }
        bundle.putString(EXTRA_HINT, hint);

        dialog.setArguments(bundle);

        return dialog;
    }

    protected Listener mListener;
    private EditText mEditText;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Object parent = getParentFragment();
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

    protected AlertDialog.Builder buildDialog(Bundle savedInstanceState){
        final Bundle arguments = getArguments();

        final String dialogTag = arguments.getString(EXTRA_DIALOG_TAG);
        final boolean hintIsValidEntry = arguments.getBoolean(EXTRA_HINT_IS_VALID_ENTRY);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(arguments.getString(EXTRA_TITLE))
                .setView(generateContentView(savedInstanceState))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CharSequence input = mEditText.getText();
                        if (TextUtils.isEmpty(input) && hintIsValidEntry) {
                            input = mEditText.getHint();
                        }

                        String value = null;
                        if (input != null)
                            value = input.toString().trim();

                        if (mListener != null)
                            mListener.onOk(dialogTag, value);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null)
                            mListener.onCancel(dialogTag);
                    }
                });

        return builder;
    }

    protected View generateContentView(Bundle savedInstanceState){
        final View contentView = getActivity().getLayoutInflater().inflate(R.layout
                .dialog_edit_input_content, null);

        if(contentView == null)
            return contentView;

        mEditText = (EditText) contentView.findViewById(R.id.dialog_edit_text_content);
        mEditText.setHint(getArguments().getString(EXTRA_HINT));

        return contentView;
    }
}
