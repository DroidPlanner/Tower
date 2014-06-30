package org.droidplanner.android.fragments;

import android.app.Dialog;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Used to implement various workarounds for the preference fragment.
 */
public class DpPreferenceFragment extends PreferenceFragment {

    /**
     * Allows the settings screen to perform the correct/expected behavior when the up arrow is
     * clicked.
     * @param prefScreen
     * @param pref
     * @return
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen prefScreen, Preference pref){
        if(pref instanceof PreferenceScreen){
            final Dialog dialog = ((PreferenceScreen) pref).getDialog();
            final View homeBtn = dialog.findViewById(android.R.id.home);
            if(homeBtn != null){
                View.OnClickListener dismissDialogClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                };

                // Prepare yourselves for some hacky programming
                ViewParent homeBtnContainer = homeBtn.getParent();

                // The home button is an ImageView inside a FrameLayout
                if (homeBtnContainer instanceof FrameLayout) {
                    ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();

                    if (containerParent instanceof LinearLayout) {
                        // This view also contains the title text, set the whole view as clickable
                        containerParent.setOnClickListener(dismissDialogClickListener);
                    } else {
                        // Just set it on the home button
                        ((FrameLayout) homeBtnContainer).setOnClickListener(dismissDialogClickListener);
                    }
                } else {
                    // The 'If all else fails' default case
                    homeBtn.setOnClickListener(dismissDialogClickListener);
                }
            }
            return true;
        }
        return false;
    }
}