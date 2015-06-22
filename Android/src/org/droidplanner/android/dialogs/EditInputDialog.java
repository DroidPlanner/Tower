package org.droidplanner.android.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import org.droidplanner.android.R;

/**
 * Created by fhuya on 10/14/14.
 */
public class EditInputDialog extends YesNoDialog {

    protected static final String EXTRA_HINT = "hint";

    public interface Listener {
        void onOk(final CharSequence input);

        void onCancel();
    }

    public static EditInputDialog newInstance(String title, String hint, boolean hintIsValidEntry, Listener listener){
        EditInputDialog dialog = new EditInputDialog();

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TITLE, title);

        if(hint == null){
            hint = "";
        }
        bundle.putString(EXTRA_HINT, hint);

        dialog.setArguments(bundle);
        dialog.mListener = listener;
        dialog.hintIsValidEntry = hintIsValidEntry;

        return dialog;
    }

    protected Listener mListener;
    protected boolean hintIsValidEntry;
    private EditText mEditText;

    @Override
    protected AlertDialog.Builder buildDialog(Bundle savedInstanceState){
        final Bundle arguments = getArguments();

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
                        if(input != null)
                            value = input.toString().trim();

                        mListener.onOk(value);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onCancel();
                    }
                });

        return builder;
    }

    @Override
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
