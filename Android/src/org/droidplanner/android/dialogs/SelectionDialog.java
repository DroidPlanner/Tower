package org.droidplanner.android.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by fhuya on 10/26/14.
 */
public class SelectionDialog extends DialogFragment {

    protected final static String EXTRA_TITLE = "title";
    protected final static String EXTRA_SELECTIONS = "selections";

    public static SelectionDialog newInstance(String title, CharSequence[] selections,
                                              DialogInterface.OnClickListener listener){
        SelectionDialog dialog = new SelectionDialog();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TITLE, title);
        bundle.putCharSequenceArray(EXTRA_SELECTIONS, selections);

        dialog.setArguments(bundle);
        dialog.selectionClickListener = listener;

        return dialog;
    }

    protected DialogInterface.OnClickListener selectionClickListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Bundle args = getArguments();

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setTitle(args.getString(EXTRA_TITLE))
                .setItems(args.getCharSequenceArray(EXTRA_SELECTIONS), selectionClickListener);

        return b.create();
    }
}
