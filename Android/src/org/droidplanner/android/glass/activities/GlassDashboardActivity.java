package org.droidplanner.android.glass.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import org.droidplanner.R;
import org.droidplanner.android.utils.Constants;
import org.droidplanner.android.utils.Utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Glass dashboard activity.
 * This is displayed upon launching the app, and allows the user to navigate between the
 * different app sections.
 */
public class GlassDashboardActivity extends GlassUI {

    //TODO: update description resource for the section info.
    private final Map<SectionInfo, Runnable> mSectionInfos = new LinkedHashMap<SectionInfo,
            Runnable>();

    {
        mSectionInfos.put(new SectionInfo(R.string.flight_data, R.drawable.ic_action_plane_white,
                R.string.empty_string), new Runnable() {
            @Override
            public void run() {
                launchFlightData();
            }
        });


        mSectionInfos.put(new SectionInfo(R.string.mission_editor, R.drawable.ic_edit,
                R.string.empty_string), new Runnable() {
            @Override
            public void run() {
                launchMissionEditor();
            }
        });

        //TODO: enable settings, and mission editor once feature complete.
        /*
        mSectionInfos.put(new SectionInfo(R.string.settings, R.drawable.ic_action_settings_white,
                R.string.empty_string), new Runnable() {
            @Override
            public void run() {
                launchSettings();
            }
        });
        */
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glass_dashboard);

        CardScrollView dashView = (CardScrollView) findViewById(R.id.glass_dashboard);
        dashView.setAdapter(new SectionCardAdapter(getApplicationContext(),
                mSectionInfos.keySet().toArray(new SectionInfo[mSectionInfos.size()])));
        dashView.setHorizontalScrollBarEnabled(true);
        dashView.activate();
        dashView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Perform click sound
                view.playSoundEffect(SoundEffectConstants.CLICK);
                SectionInfo sectionInfo = (SectionInfo) parent.getItemAtPosition(position);

                Runnable sectionCb = mSectionInfos.get(sectionInfo);
                if (sectionCb != null) { sectionCb.run(); }
            }
        });

        updateConnectionPrefs();
    }

    private void updateConnectionPrefs() {
        mPrefs.prefs.edit().putString(Constants.PREF_CONNECTION_TYPE,
                Utils.ConnectionType.BLUETOOTH.name()).apply();
    }

    private void launchFlightData() {
        startActivity(new Intent(getApplicationContext(), GlassHudActivity.class));
    }

    private void launchMissionEditor() {
        startActivity(new Intent(getApplicationContext(), GlassEditorActivity.class));
    }

    private void launchSettings() {
        startActivity(new Intent(getApplicationContext(), GlassSettingsActivity.class));
    }

    public static class SectionInfo {
        private final int mSectionNameRes;
        private final int mSectionLogoRes;
        private final int mSectionDescRes;

        public SectionInfo(int sectionNameRes, int sectionLogoRes, int sectionDescRes) {
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
        public boolean equals(Object o) {
            if (o == this) { return true; }

            if (!(o instanceof SectionInfo)) { return false; }

            final SectionInfo that = (SectionInfo) o;
            return this.mSectionNameRes == that.mSectionNameRes && this.mSectionLogoRes == that
                    .mSectionLogoRes && this.mSectionDescRes == that.mSectionDescRes;
        }

        @Override
        public int hashCode() {
            return mSectionNameRes;
        }
    }

    private static class SectionCardAdapter extends CardScrollAdapter {

        private final Context mContext;
        private final SectionInfo[] mSectionInfos;

        public SectionCardAdapter(Context context, SectionInfo[] sectionInfos) {
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
            final SectionInfo sectionInfo = (SectionInfo) getItem(i);
            card.setText(sectionInfo.getSectionNameRes())
                    .setFootnote(sectionInfo.getSectionDescRes())
                    .addImage(sectionInfo.getSectionLogoRes())
                    .setImageLayout(Card.ImageLayout.FULL);

            return card.getView();
        }

        @Override
        public int getPosition(Object o) {
            int defaultId = -1;
            if (!(o instanceof SectionInfo)) { return defaultId; }

            SectionInfo sectionInfo = (SectionInfo) o;
            for (int i = 0; i < getCount(); i++) {
                if (sectionInfo.equals(getItem(i))) { return i; }
            }

            return defaultId;
        }
    }
}
