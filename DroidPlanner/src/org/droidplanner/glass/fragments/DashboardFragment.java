package org.droidplanner.glass.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import org.droidplanner.R;

/**
 * Glass dashboard fragment.
 * This is displayed upon launching the app, and allows the user to navigate between the
 * different app sections.
 */
public class DashboardFragment extends Fragment {

    private OnDashboardListener mDashListener;

    /**
     * Used to convey to the parent which section was selected.
     */
    public interface OnDashboardListener {

        /**
         * Triggered when a section is selected.
         * @param sectionInfo selected section info
         */
        public void onSectionSelected(SectionInfo sectionInfo);

        /**
         * Returns the name of the sections provided by the parent.
         * @return array of section names.
         */
        public SectionInfo[] getSectionsNames();
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(!(activity instanceof OnDashboardListener)){
            throw new IllegalStateException("Parent must be an instance of " +
                    OnDashboardListener.class.getName());
        }

        mDashListener = (OnDashboardListener) activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mDashListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        final View view = inflater.inflate(R.layout.fragment_glass_dashboard, container, false);

        CardScrollView dashView = (CardScrollView) view.findViewById(R.id.glass_dashboard);
        dashView.setAdapter(new SectionCardAdapter((Activity) mDashListener,
                mDashListener.getSectionsNames()));
        dashView.setHorizontalScrollBarEnabled(true);
        dashView.activate();
        dashView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Perform click sound
                view.playSoundEffect(SoundEffectConstants.CLICK);
                mDashListener.onSectionSelected((SectionInfo)parent.getItemAtPosition(position));
            }
        });
        return view;
    }

    public static class SectionInfo {
        private final int mSectionNameRes;
        private final int mSectionLogoRes;
        private final int mSectionDescRes;

        public SectionInfo(int sectionNameRes, int sectionLogoRes, int sectionDescRes){
            mSectionNameRes = sectionNameRes;
            mSectionLogoRes = sectionLogoRes;
            mSectionDescRes = sectionDescRes;
        }

        public int getSectionNameRes() {
            return mSectionNameRes;
        }

        public int getSectionLogoRes() {
            return mSectionLogoRes;
        }

        public int getSectionDescRes() {
            return mSectionDescRes;
        }

        @Override
        public boolean equals(Object o){
            if(o == this)
                return true;

            if(!(o instanceof SectionInfo))
                return false;

            SectionInfo that = (SectionInfo) o;
            return this.mSectionNameRes == that.mSectionNameRes && this.mSectionLogoRes == that
                    .mSectionLogoRes && this.mSectionDescRes == that.mSectionDescRes;
        }

        @Override
        public int hashCode(){
            return mSectionNameRes;
        }
    }

    private static class SectionCardAdapter extends CardScrollAdapter {

        private final Context mContext;
        private final SectionInfo[] mSectionInfos;

        public SectionCardAdapter(Context context, SectionInfo[] sectionInfos){
            super();
            mContext = context;
            mSectionInfos = sectionInfos;
        }

        @Override
        public int getCount() {
            return mSectionInfos.length;
        }

        @Override
        public Object getItem(int i) {
            return mSectionInfos[i];
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final Card card = new Card(mContext);
            final SectionInfo sectionInfo = (SectionInfo)getItem(i);
            card.setText(sectionInfo.getSectionNameRes())
                    .setFootnote(sectionInfo.getSectionDescRes())
                    .addImage(sectionInfo.getSectionLogoRes())
                    .setImageLayout(Card.ImageLayout.FULL);

            return card.toView();
        }

        @Override
        public int findIdPosition(Object o) {
            return -1;
        }

        @Override
        public int findItemPosition(Object o) {
            int defaultId = -1;
            if(!(o instanceof SectionInfo))
                return defaultId;

            SectionInfo sectionInfo = (SectionInfo) o;
            for(int i = 0; i < getCount(); i++){
                if(sectionInfo.equals(getItem(i)))
                    return i;
            }

            return defaultId;
        }
    }
}
