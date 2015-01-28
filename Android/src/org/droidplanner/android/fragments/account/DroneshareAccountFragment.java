package org.droidplanner.android.fragments.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import org.droidplanner.android.widgets.adapterViews.UserDataAdapter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Fredia Huya-Kouadio on 1/22/15.
 */
public class DroneshareAccountFragment extends Fragment {

    private static final String TAG = DroneshareAccountFragment.class.getSimpleName();

    private final static String EXTRA_USER_DATA = "extra_user_data";

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

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.user_vehicles_list);

        //Use this setting to improve performance if you know that changes in content do not change the layout side
        // of the RecyclerView
        recyclerView.setHasFixedSize(true);

        //Use a grid layout manager
        final int colCount = getResources().getInteger(R.integer.vehiclesColCount);
        final RecyclerView.LayoutManager gridLayoutMgr = new GridLayoutManager(context, colCount);
        recyclerView.setLayoutManager(gridLayoutMgr);

        userDataAdapter = new UserDataAdapter();

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

        new LoadUserData(userData == null).execute(username, password);
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

        private final ProgressDialog progressDialog;
        private final boolean forceUpdate;

        LoadUserData(boolean forceUpdate) {
            this.forceUpdate = forceUpdate;
            if (forceUpdate) {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setIndeterminate(true);
                progressDialog.setTitle("Loading user data...");
            } else {
                progressDialog = null;
            }
        }

        @Override
        protected void onPreExecute() {
            if (progressDialog != null)
                progressDialog.show();
        }

        @Override
        protected void onCancelled() {
            if (progressDialog != null)
                progressDialog.dismiss();
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
            if (progressDialog != null)
                progressDialog.dismiss();

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
