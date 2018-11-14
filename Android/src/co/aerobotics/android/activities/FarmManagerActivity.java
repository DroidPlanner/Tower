package co.aerobotics.android.activities;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.activities.interfaces.APIContract;
import co.aerobotics.android.data.AeroviewPolygons;
import co.aerobotics.android.data.Farm;
import co.aerobotics.android.data.SQLiteDatabaseHandler;
import co.aerobotics.android.dialogs.AddNewFarmDialog;

public class FarmManagerActivity extends DrawerNavigationUI implements APIContract{

    private FragmentManager fragmentManager;
    private ArrayAdapter<Farm> listAdapter;
    private List<Integer> selectedFarmIds = new ArrayList<>();
    List<Farm> farms = new ArrayList<>();
    private SharedPreferences sharedPref;
    private ListView itemList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFragmentManager();
        setContentView(R.layout.activity_farm_manager);
        initializeSharedPrefs();
        getCurrentlySelectedFarmIds();
        getAllFarmsAccessibleToActiveClient();
        setupListView();
        setupEditTextViewAsSearchInputForListView();
        initializeButtonOnClickListener();
        initializeOnAddNewFarmButtonClickListener();
        shouldShowFarmPrompt();
    }


    private void initializeFragmentManager() {
        fragmentManager = getSupportFragmentManager();
    }

    private void initializeOnAddNewFarmButtonClickListener() {
        FloatingActionButton addNewFarmButton = (FloatingActionButton) findViewById(R.id.addNewFarm);
        addNewFarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddNewFarmDialog();
            }
        });
    }

    private void openAddNewFarmDialog() {
        final AddNewFarmDialog dialogFragment = new AddNewFarmDialog();
        dialogFragment.show(fragmentManager, null);
        fragmentManager.executePendingTransactions();
        dialogFragment.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                getAllFarmsAccessibleToActiveClient();
                sortFarmNamesAlphabetically();
                if (dialogFragment.getNewFarmId() != null) {
                    selectedFarmIds.add(dialogFragment.getNewFarmId());
                    setCurrentlySelectedFarmsAsChecked();
                    listAdapter.notifyDataSetChanged();
                }
                dialogFragment.dismissAllowingStateLoss();
            }
        });
    }

    private void initializeSharedPrefs() {
        sharedPref = this.getSharedPreferences(this.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY), Context.MODE_PRIVATE);
    }

    private void getCurrentlySelectedFarmIds() {
        String activeFarmsString = sharedPref.getString(this.getResources().getString(R.string.active_farms), "[]");
        Type type = new TypeToken<ArrayList<Integer>>() { }.getType();
        selectedFarmIds = new Gson().fromJson(activeFarmsString, type);
    }

    private void getAllFarmsAccessibleToActiveClient() {
        farms.clear();
        SQLiteDatabaseHandler sqLiteDatabaseHandler = new SQLiteDatabaseHandler(this.getApplicationContext());
        String allClientIds = sharedPref.getString(this.getResources().getString(R.string.all_client_ids), "")
                .replaceAll("\\[", "").replaceAll("]","");
        List<JSONObject> farmNameIdMap = sqLiteDatabaseHandler.getFarmNamesAndIdList(allClientIds);
        for (JSONObject farm: farmNameIdMap) {
            try {
                String farmName = farm.getString("name");
                Integer id = farm.getInt("farm_id");
                Farm farmObj = new Farm(farmName, id);
                farms.add(farmObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void setupListView() {
        initializeListAdapter();
        initializeListView();
        sortFarmNamesAlphabetically();
        populateListView();
        initializeListViewOnItemClickListener();
        setCurrentlySelectedFarmsAsChecked();
    }

    private void initializeListView() {
        itemList = (ListView) findViewById(R.id.farmListView);
    }

    private void initializeListAdapter() {
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, android.R.id.text1,  farms);
    }

    private void sortFarmNamesAlphabetically() {
        if (farms.size() > 0) {
            Collections.sort(farms, new Comparator<Farm>() {
                @Override
                public int compare(Farm farmA, Farm farmB) {
                    return farmA.getName().toLowerCase().compareTo(farmB.getName().toLowerCase());
                }
            });
        }
    }

    private void populateListView() {
        itemList.setAdapter(listAdapter);
    }

    private void initializeListViewOnItemClickListener() {
        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Farm farm = listAdapter.getItem(position);
                if (farm != null) {
                    if (selectedFarmIds.contains(farm.getId())) {
                        selectedFarmIds.remove(farm.getId());
                    } else {
                        selectedFarmIds.add(farm.getId());
                    }
                }
            }
        });
    }

    private void setCurrentlySelectedFarmsAsChecked() {
       int numFarms = listAdapter.getCount();
       for (int i = 0; i < numFarms; i++) {
           Farm farm = listAdapter.getItem(i);
           if (farm != null) {
               if (selectedFarmIds.contains(farm.getId())) {
                   itemList.setItemChecked(i, true);
               }
           }
       }
   }

    private void setupEditTextViewAsSearchInputForListView() {
        EditText filterText = (EditText) findViewById(R.id.searchFarmTextView);
        filterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                FarmManagerActivity.this.listAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void initializeButtonOnClickListener() {
        Button goFlyButton = (Button) findViewById(R.id.goFlyButton);
        goFlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeSelectedFarmsToSharedPrefs();
                addSelectedFarmBoundariesToMap();
                openEditorActivity();
            }
        });
    }

    private void writeSelectedFarmsToSharedPrefs() {
        if (selectedFarmIds != null) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.active_farms), new Gson().toJson(selectedFarmIds)).apply();
        }
    }

    private void addSelectedFarmBoundariesToMap() {
        if (DroidPlannerApp.getInstance().isNetworkAvailable()) {
            fetchFarmBoundariesFromServer();
        } else {
            addLocalBoundariesToMap();
        }

    }

    private void fetchFarmBoundariesFromServer() {
        AeroviewPolygons aeroviewPolygons = new AeroviewPolygons(FarmManagerActivity.this.getApplicationContext());
        aeroviewPolygons.executeGetFarmOrchardsTask();
    }

    private void addLocalBoundariesToMap() {
        AeroviewPolygons aeroviewPolygons = new AeroviewPolygons(FarmManagerActivity.this.getApplicationContext());
        aeroviewPolygons.addPolygonsToMap();
    }

    private String parseListObjectToString() {
        StringBuilder stringBuilder  = new StringBuilder();
        Iterator<Integer> iterator = selectedFarmIds.iterator();
        while(iterator.hasNext())
        {
            stringBuilder.append(iterator.next());
            if(iterator.hasNext()) {
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    private void openEditorActivity() {
        Intent intent = new Intent(FarmManagerActivity.this, EditorActivity.class);
        FarmManagerActivity.this.startActivity(intent);
        finish();
    }

    private void shouldShowFarmPrompt() {
        if (farms.size() == 0) {
            promptUserToAddNewFarm();
        }
    }

    private void promptUserToAddNewFarm() {
        final TapTargetSequence targetSequence = new TapTargetSequence(this).targets(
                TapTarget.forView(findViewById(R.id.addNewFarm), "Getting started", "Add a farm name.")
                        // All options below are optional
                        .outerCircleColor(R.color.primary_dark_blue)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.white)   // Specify a color for the target circle
                        .titleTextSize(24)                  // Specify the size (in sp) of the title text
                        .titleTextColor(R.color.white)      // Specify the color of the title text
                        .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                        .descriptionTextColor(R.color.white)  // Specify the color of the description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        .dimColor(R.color.primary_dark_blue) // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                        .targetRadius(28)                  // Specify the target radius (in dp)
                        .id(1)).listener(new TapTargetSequence.Listener() {
            @Override
            public void onSequenceFinish() {
                openAddNewFarmDialog();
            }

            @Override
            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

            }

            @Override
            public void onSequenceCanceled(TapTarget lastTarget) {

            }
        });

        targetSequence.start();
    }

    @Override
    protected int getToolbarId() {
        return R.id.farm_manager_actionbar_container;
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return R.id.navigation_farms;
    }
}
