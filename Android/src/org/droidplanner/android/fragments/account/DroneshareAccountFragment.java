package org.droidplanner.android.fragments.account;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.geeksville.apiproxy.rest.RESTClient;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.interfaces.AccountLoginListener;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.view.NiceProgressView;
import org.droidplanner.android.view.adapterViews.UserDataAdapter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Fredia Huya-Kouadio on 1/22/15.
 */
public class DroneshareAccountFragment extends Fragment {

    private static final String TAG = DroneshareAccountFragment.class.getSimpleName();

    private final static String EXTRA_USER_DATA = "extra_user_data";

    public final static String DRONESHARE_URL = "http://www.droneshare.com/";

    private DroidPlannerPrefs dpPrefs;

    private JSONObject userData;
    private UserDataAdapter userDataAdapter;

    private AccountLoginListener loginListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof AccountLoginListener)) {
            throw new IllegalStateException("Parent must implement " + AccountLoginListener.class.getName());
        }

        loginListener = (AccountLoginListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        loginListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_droneshare_account, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Context context = getActivity().getApplicationContext();

        dpPrefs = new DroidPlannerPrefs(context);
        final String username = dpPrefs.getDroneshareLogin();
        final String password = dpPrefs.getDronesharePassword();

        final TextView usernameView = (TextView) view.findViewById(R.id.dshare_username);
        usernameView.setText(username);

        final String userProfileUrl = DRONESHARE_URL + "user/" + username;
        final TextView userUrlView = (TextView) view.findViewById(R.id.dshare_user_url);
        userUrlView.setText(userProfileUrl);

        final View userInfoBox = view.findViewById(R.id.user_info_container);
        userInfoBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open the user profile on droneshare.
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(userProfileUrl)));
            }
        });

        final NiceProgressView progressView = (NiceProgressView) view.findViewById(R.id.vehicle_loading_progress);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.user_vehicles_list);

        //Use this setting to improve performance if you know that changes in content do not change the layout side
        // of the RecyclerView
        recyclerView.setHasFixedSize(true);

        //Use a grid layout manager
        final int colCount = getResources().getInteger(R.integer.vehiclesColCount);
        final RecyclerView.LayoutManager gridLayoutMgr = new GridLayoutManager(context, colCount);
        recyclerView.setLayoutManager(gridLayoutMgr);

        userDataAdapter = new UserDataAdapter(context);

        if (savedInstanceState != null) {
            String userDataString = savedInstanceState.getString(EXTRA_USER_DATA);
            if (userDataString != null) {
                try {
                    userData = new JSONObject(userDataString);
                    userDataAdapter.updateUserData(userData);
                } catch (JSONException e) {
                    Log.e(TAG, "Unable to read saved user data.", e);
                }
            }
        }

        recyclerView.setAdapter(userDataAdapter);

        new LoadUserData(progressView, userData == null).execute(username, password);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (userData != null)
            outState.putString(EXTRA_USER_DATA, userData.toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_droneshare_account, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_dshare_logout:
                dpPrefs.setDronesharePassword("");
                loginListener.onLogout();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private class LoadUserData extends AsyncTask<String, Void, JSONObject> {

        private final boolean forceUpdate;
        private final NiceProgressView progressView;

        LoadUserData(NiceProgressView progressView, boolean forceUpdate) {
            this.progressView = progressView;
            this.forceUpdate = forceUpdate;
        }

        @Override
        protected void onPreExecute() {
            if(progressView != null && forceUpdate)
                progressView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onCancelled() {
            if(progressView != null && forceUpdate)
                progressView.setVisibility(View.GONE);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            final String username = params[0];
            final String password = params[1];

            JSONObject userData = null;
            try {
                userData = RESTClient.getUserData(username, password, dpPrefs.getDroneshareApiKey());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return userData;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if(progressView != null && forceUpdate)
                progressView.setVisibility(View.GONE);

            if(loginListener == null)
                return;

            if (result == null) {
                if (forceUpdate)
                    loginListener.onLogout();
            } else {
                userData = result;
                userDataAdapter.updateUserData(result);
            }
        }
    }

}
