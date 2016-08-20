package org.droidplanner.android.maps.providers.baidu_map;

import android.content.res.Resources;
import android.preference.DialogPreference;
import android.util.Log;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.droidplanner.android.R;

public class BaiduMapOfflineMapPreference extends DialogPreference implements MKOfflineMapListener {
    private MKOfflineMap mOffline = null;
    private View mBindDialogView = null;
    private TextView mCidView = null;
    private TextView mStateView =null;
    private EditText mCityNameView = null;
    private LocalMapAdapter mLAdapter = null;
    private LinearLayout mCityListLayout = null;
    private LinearLayout mLocalMapLayout = null;
    private List<MKOLUpdateElement> mLocalMapList = null; // downloaded offline map list
    private static final int DATA_SIZE_ONE_KB = 1024 * 1024;
    private static final int DATA_SIZE_UNIT_KB = 1024;
    private static final double DATA_SIZE_UNIT_MB = 1024 * 1024.0;

    public BaiduMapOfflineMapPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.fragment_baidumap_offline_map_preference);
    }

    private void initView(View view) {

        mCidView = (TextView) view.findViewById(org.droidplanner.android.R.id.cityid);
        mCityNameView = (EditText) view.findViewById(org.droidplanner.android.R.id.city);
        mStateView = (TextView) view.findViewById(org.droidplanner.android.R.id.state);

        ListView hotCityList = (ListView) view.findViewById(org.droidplanner.android.R.id.hotcitylist);
        ArrayList<String> hotCities = new ArrayList<String>();
        // get hot city list
        ArrayList<MKOLSearchRecord> hotCityRecords = mOffline.getHotCityList();
        if (hotCityRecords != null) {
            for (MKOLSearchRecord r : hotCityRecords) {
                hotCities.add(r.cityName + "(" + r.cityID + ")" + "   --" + this.formatDataSize(r.size));
            }
        }
        ListAdapter hAdapter = (ListAdapter) new ArrayAdapter<String>(this.getContext(),
                android.R.layout.simple_list_item_1, hotCities);
        hotCityList.setAdapter(hAdapter);

        ListView allCityList = (ListView) view.findViewById(R.id.allcitylist);
        // get all offline city map
        ArrayList<String> allCities = new ArrayList<String>();
        ArrayList<MKOLSearchRecord> offlineCityrecords = mOffline.getOfflineCityList();
        if (offlineCityrecords != null) {
            for (MKOLSearchRecord r : offlineCityrecords) {
                allCities.add(r.cityName + "(" + r.cityID + ")" + "   --" + this.formatDataSize(r.size));
            }
        }
        ListAdapter aAdapter = (ListAdapter) new ArrayAdapter<String>(this.getContext(),
                android.R.layout.simple_list_item_1, allCities);
        allCityList.setAdapter(aAdapter);

        mCityListLayout = (LinearLayout) view.findViewById(R.id.citylist_layout);
        mLocalMapLayout = (LinearLayout) view.findViewById(R.id.localmap_layout);
        mLocalMapLayout.setVisibility(View.GONE);
        mCityListLayout.setVisibility(View.VISIBLE);

        // get downloaded offline map info
        mLocalMapList = mOffline.getAllUpdateInfo();
        if (mLocalMapList == null) {
            mLocalMapList = Collections.emptyList();
        }

        ListView localMapListView = (ListView) view.findViewById(R.id.localmaplist);
        mLAdapter = new LocalMapAdapter();
        localMapListView.setAdapter(mLAdapter);

        Button button = (Button) view.findViewById(R.id.search);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                search(v);
            }
        });

        button = (Button) view.findViewById(R.id.start);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                start(v);
            }
        });

        button = (Button) view.findViewById(R.id.stop);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stop(v);
            }
        });

        button = (Button) view.findViewById(R.id.del);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                remove(v);
            }
        });

        button = (Button) view.findViewById(R.id.clButton);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCityListButton(v);
            }
        });

        button = (Button) view.findViewById(R.id.localButton);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickLocalMapListButton(v);
            }
        });
    }

    /**
     * switch to city list
     *
     * @param view
     */
    public void clickCityListButton(View view) {
        if (mBindDialogView != null) {
            mLocalMapLayout.setVisibility(View.GONE);
            mCityListLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * switch to download manager list
     *
     * @param view
     */
    public void clickLocalMapListButton(View view) {
        if (mBindDialogView != null) {
            mLocalMapLayout.setVisibility(View.VISIBLE);
            mCityListLayout.setVisibility(View.GONE);
        }
    }

    /**
     * search offline city
     *
     * @param view
     */
    public void search(View view) {
        ArrayList<MKOLSearchRecord> records = mOffline.searchCity(mCityNameView
                .getText().toString());
        if (records == null) {
            return;
        }
        mCidView.setText(String.valueOf(records.get(0).cityID));
    }

    /**
     * Start to download
     *
     * @param view
     */
    public void start(View view) {
        int cityid = Integer.parseInt(mCidView.getText().toString());
        mOffline.start(cityid);
        clickLocalMapListButton(view);
        final Context context = getContext();
        Resources res = context.getResources();
        String text = String.format(res.getString(R.string.pref_baidu_map_offline_download_start_message), cityid);
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        updateView();
    }

    /**
     * Pause download
     *
     * @param view
     */
    public void stop(View view) {
        int cityid = Integer.parseInt(mCidView.getText().toString());
        mOffline.pause(cityid);
        final Context context = getContext();
        Resources res = context.getResources();
        String text = String.format(res.getString(R.string.pref_baidu_map_offline_download_stop_message), cityid);
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        updateView();
    }

    /**
     * Remove offline map
     *
     * @param view
     */
    public void remove(View view) {
        int cityid = Integer.parseInt(mCidView.getText().toString());
        mOffline.remove(cityid);
        final Context context = getContext();
        Resources res = context.getResources();
        String text = String.format(res.getString(R.string.pref_baidu_map_offline_download_remove_message), cityid);
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        updateView();
    }

    /**
     * Display offline map update status
     */
    public void updateView() {
        mLocalMapList = mOffline.getAllUpdateInfo();
        if (mLocalMapList == null) {
            mLocalMapList = Collections.emptyList();;
        }
        mLAdapter.notifyDataSetChanged();
    }

    public String formatDataSize(int size) {
        String ret = "";
        if (size < (DATA_SIZE_ONE_KB)) {
            ret = String.format("%dK", size / DATA_SIZE_UNIT_KB);
        } else {
            ret = String.format("%.1fM", size / (DATA_SIZE_UNIT_MB));
        }
        return ret;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // destroy offline map module when exit
        mOffline.destroy();
        super.onDialogClosed(positiveResult);
    }


    @Override
    public void onBindDialogView(View view){
        // Initialize Baidu Map library
        com.baidu.mapapi.SDKInitializer.initialize(this.getContext().getApplicationContext());
        mBindDialogView = view;
        mOffline = new MKOfflineMap();
        mOffline.init(this);

        initView(view);
        super.onBindDialogView(view);
    }

    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                MKOLUpdateElement update = mOffline.getUpdateInfo(state);
                // display download progress
                if (update != null) {
                    mStateView.setText(String.format("%s : %d%%", update.cityName, update.ratio));
                    updateView();
                }
            }
            break;
            case MKOfflineMap.TYPE_NEW_OFFLINE:
                // Prompt offline map update is available
                Log.d("Baidu offline map", String.format("add offlinemap num:%d", state));
                break;
            case MKOfflineMap.TYPE_VER_UPDATE:
                // Not used yet
                break;
            default:
                break;
        }
    }

    /**
     *  list adapter for managing offline map
     */
    public class LocalMapAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mLocalMapList.size();
        }

        @Override
        public Object getItem(int index) {
            return mLocalMapList.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }

        @Override
        public View getView(int index, View view, ViewGroup arg2) {
            MKOLUpdateElement e = (MKOLUpdateElement) getItem(index);
            if (view == null) {
                view = View.inflate(BaiduMapOfflineMapPreference.this.getContext(), R.layout.list_baidumap_offline_localmap_item, null);
            }
            initViewItem(view, e);
            return view;
        }

        void initViewItem(View view, final MKOLUpdateElement e) {
            Button display = (Button) view.findViewById(R.id.display);
            Button remove = (Button) view.findViewById(R.id.remove);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView update = (TextView) view.findViewById(R.id.update);
            TextView ratio = (TextView) view.findViewById(R.id.ratio);
            ratio.setText(e.ratio + "%");
            title.setText(e.cityName);
            if (e.update) {
                update.setText(R.string.pref_baidu_map_offline_preference_download_available);
            } else {
                update.setText(R.string.pref_baidu_map_offline_preference_download_latest);
            }
            if (e.ratio != 100) {
                display.setEnabled(false);
            } else {
                display.setEnabled(true);
            }
            remove.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    mOffline.remove(e.cityID);
                    updateView();
                }
            });
            display.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Not used yet.
                }
            });
        }

    }
}

