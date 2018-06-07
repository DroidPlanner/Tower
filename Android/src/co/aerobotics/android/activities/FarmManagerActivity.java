package co.aerobotics.android.activities;

import android.content.DialogInterface;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.activities.interfaces.APIContract;
import co.aerobotics.android.data.AeroviewPolygons;
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
        fragmentManager = getSupportFragmentManager();
        setContentView(R.layout.activity_farm_manager);
        initializeSharedPrefs();
        getCurrentlySelectedFarmIds();
        getAllFarmsAccessibleToActiveClient();
        setupListView();
        setupEditTextViewAsSearchInputForListView();
        initializeButtonOnClickListener();
        initializeOnAddNewFarmButtonClickListener();
    }

    private void initializeOnAddNewFarmButtonClickListener() {
        FloatingActionButton addNewFarmButton = (FloatingActionButton) findViewById(R.id.addNewFarm);
        addNewFarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialogFragment = new AddNewFarmDialog();
                dialogFragment.show(fragmentManager, null);
                fragmentManager.executePendingTransactions();
                dialogFragment.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        getAllFarmsAccessibleToActiveClient();
                        sortFarmNamesAlphabetically();
                        listAdapter.notifyDataSetChanged();
                    }
                });

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
        Map<String, Integer> farmNameIdMap = sqLiteDatabaseHandler.getFarmNamesAndIdJsonArray(allClientIds);
        for (Map.Entry<String, Integer> farm: farmNameIdMap.entrySet()) {
            Farm farmObj = new Farm(farm.getKey(), farm.getValue());
            farms.add(farmObj);
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
                    return farmA.getName().compareTo(farmB.getName());
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
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.active_farms), new Gson().toJson(selectedFarmIds)).apply();
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

    private class Farm {
        private String name;
        private Integer id;

        Farm(String name, Integer id){
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return this.name;
        }
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
