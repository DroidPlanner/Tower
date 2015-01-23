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
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Fredia Huya-Kouadio on 1/22/15.
 */
public class DroneshareAccountFragment extends Fragment {

    private static final String TAG = DroneshareAccountFragment.class.getSimpleName();

    private DroidPlannerPrefs dpPrefs;

    private RecyclerView recyclerView;
    private UserDataAdapter userDataAdapter;

    private AccountLoginListener loginListener;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(!(activity instanceof AccountLoginListener)){
            throw new IllegalStateException("Parent must implement " + AccountLoginListener.class.getName());
        }

        loginListener = (AccountLoginListener) activity;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        loginListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_droneshare_account, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        final Context context = getActivity().getApplicationContext();

        dpPrefs = new DroidPlannerPrefs(context);
        final String username = dpPrefs.getDroneshareLogin();
        final String password = dpPrefs.getDronesharePassword();

        final TextView usernameView = (TextView) view.findViewById(R.id.dshare_username);
        usernameView.setText(username);

        recyclerView = (RecyclerView) view.findViewById(R.id.user_vehicles_list);

        //Use this setting to improve performance if you know that changes in content do not change the layout side
        // of the RecyclerView
        recyclerView.setHasFixedSize(true);

        //Use a grid layout manager
        final int colCount = getResources().getInteger(R.integer.vehiclesColCount);
        final RecyclerView.LayoutManager gridLayoutMgr = new GridLayoutManager(context, colCount);
        recyclerView.setLayoutManager(gridLayoutMgr);

        new AsyncTask<Void, Void, JSONObject>(){

            private ProgressDialog progressDialog;

            @Override
            protected void onPreExecute(){
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setIndeterminate(true);
                progressDialog.setTitle("Loading user data...");
                progressDialog.show();
            }

            @Override
            protected void onCancelled(){
                progressDialog.dismiss();
            }

            @Override
            protected JSONObject doInBackground(Void... params) {
                JSONObject userData = null;
                try {
                    userData = RESTClient.getUserData(username, password, dpPrefs.getDroneshareApiKey());
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                return userData;
            }

            @Override
            protected void onPostExecute(JSONObject result){
                progressDialog.dismiss();
                userDataAdapter = new UserDataAdapter(result);
                recyclerView.setAdapter(userDataAdapter);
            }
        }.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_droneshare_account, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.menu_dshare_logout:
                dpPrefs.setDronesharePassword("");
                loginListener.onSuccessfulLogout();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

}
