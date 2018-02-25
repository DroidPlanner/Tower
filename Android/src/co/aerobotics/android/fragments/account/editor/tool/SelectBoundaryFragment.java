package co.aerobotics.android.fragments.account.editor.tool;

import android.app.AlertDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;


import co.aerobotics.android.R;

import java.util.ArrayList;

/**
 * Created by aerobotics on 2017/05/08.
 */

public class SelectBoundaryFragment extends DialogFragment {
    private ArrayList<String> mSelectedItems;
    public static final String LOGTAG = "SelectBoundaryFragment";

    public interface OnItemSelectedListener {
        void onItemSelected(SelectBoundaryFragment fragment, ArrayList<String> selectedItems);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle mArgs = getArguments();
        ArrayList<String> stringArrayList = mArgs.getStringArrayList("key");
        final CharSequence[] prefsCharSequence = stringArrayList.toArray(new CharSequence[stringArrayList.size()]);
        mSelectedItems = new ArrayList<String>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_boundary)
                .setMultiChoiceItems(prefsCharSequence, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            mSelectedItems.add(prefsCharSequence[which].toString());
                        } else if (mSelectedItems.contains(which)) {
                            // Else, if the item is already in the array, remove it
                            mSelectedItems.remove(Integer.valueOf(which));
                        }

                    }
                })
                // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog

                        Fragment fragment = getTargetFragment();
                        //if (fragment instanceof OnItemSelectedListener) {
                        Log.d(LOGTAG, "OK button clicked");
                        OnItemSelectedListener listener = (OnItemSelectedListener)fragment;
                        listener.onItemSelected(SelectBoundaryFragment.this, mSelectedItems);

                        //}
                        dialog.cancel();

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    public ArrayList<String> getItems(){
        return mSelectedItems;
    }
}
