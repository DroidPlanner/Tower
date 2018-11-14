package co.aerobotics.android.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.aerobotics.android.R;

/**
 * Created by michaelwootton on 6/15/18.
 */

public class FarmDataHandler {
    private static final int DRONE_DEMO_ACCOUNT_ID = 247;

    private Integer activeClientId;
    private Integer userId;
    private JSONObject user;
    private List<Integer> allClientsIds = new ArrayList<>();
    private List<Integer> farmIdsFromServer = new ArrayList<>();
    private SQLiteDatabaseHandler sqLiteDatabaseHandler;
    private List<BoundaryDetail> serviceProviderBoundaries = new ArrayList<>();
    private SharedPreferences sharedPref;
    private Context context;

    public FarmDataHandler(Context context, JSONObject user) {
        this.context = context;
        this.user = user;
        sqLiteDatabaseHandler = new SQLiteDatabaseHandler(context);
        sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY),Context.MODE_PRIVATE);
    }

    public void parseUsersFarms() throws JSONException {
        allClientsIds.clear();
        JSONArray clients = user.getJSONArray("clients");
        List<JSONObject> farms = new ArrayList<>();
        userId = user.getInt("id");
        for (int i = 0; i < clients.length(); i++) {
            JSONObject client = clients.getJSONObject(i);
            int clientUserId = client.getInt("user_id");
            int clientId = client.getInt("id");
            if (userId == clientUserId) {
                activeClientId = clientId;
            }
            // if not drone demo account
            if (clientId != DRONE_DEMO_ACCOUNT_ID) {
                //Add id to list of all client ids
                allClientsIds.add(clientId);
                // Get farms for that client
                JSONArray farmsArray = client.getJSONArray("farms");
                for (int j = 0; j < farmsArray.length(); j++) {
                    JSONObject farmObject = farmsArray.getJSONObject(j);
                    JSONObject farm = extractFarmData(farmObject, clientId);
                    farms.add(farm);
                    farmIdsFromServer.add(farmObject.getInt("id"));
                }
            }
        }

        List<Integer> sharedFarmIds = getSharedFarmIds(user);
        List<JSONObject> invalidFarms = new ArrayList<>();
        for (JSONObject farm: farms) {
            if (farm.getInt("client_id") != activeClientId) {
                Integer farmId = farm.getInt("id");
                if (!sharedFarmIds.contains(farmId)) {
                    invalidFarms.add(farm);
                }
            }
        }
        List<JSONObject> serviceProviderFarms = getDroneServiceFarmsAndOrchards(user);
        if (serviceProviderFarms.size() > 0) {

            farms.addAll(serviceProviderFarms);
        }
        farms.removeAll(invalidFarms);
        checkForDeletedFarms();
        addFarmsToDb(farms);
        addServiceProviderBoundariesToLocalDatabase();
        setServiceProviderFarmIds();
        setAllClientsIds();
    }

    private List<JSONObject> getDroneServiceFarmsAndOrchards(JSONObject user) {
        try {
            JSONObject serviceProvider = user.getJSONObject("service_provider");
            if (serviceProvider != null) {
                JSONArray droneServices = serviceProvider.getJSONArray("drone_services");
                List<JSONObject> serviceProviderFarms = new ArrayList<>();
                for (int i = 0; i < droneServices.length(); i++) {
                    JSONObject droneService = droneServices.getJSONObject(i);
                    JSONArray orchards = droneService.getJSONArray("orchards");
                    Integer farmId = droneService.getInt("farm_id");
                    int clientId = droneService.getInt("client_id");
                    String farmName = droneService.getString("farm_name");
                    for (int j = 0; j < orchards.length(); j++) {
                        JSONObject orchard = orchards.getJSONObject(j);
                        String name = orchard.getString("name");
                        String polygon = orchard.getString("polygon");
                        String id = orchard.getString("id");
                        int cropTypeId = orchard.getInt("crop_type_id");
                        serviceProviderBoundaries.add(new BoundaryDetail(name, id, polygon, clientId, cropTypeId, farmId));
                    }
                    JSONObject farm = new JSONObject();
                    farm.put("name", farmName);
                    farm.put("id", farmId);
                    farm.put("client_id", clientId);
                    farm.put("crop_family_ids", new JSONArray());
                    serviceProviderFarms.add(farm);
                    allClientsIds.add(clientId);
                }
                return serviceProviderFarms;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    private void setServiceProviderFarmIds() {
        SharedPreferences.Editor editor = sharedPref.edit();
        List<Integer> serviceProviderFarmIds = new ArrayList<>();
        for (int i = 0; i < serviceProviderBoundaries.size(); i++) {
            serviceProviderFarmIds.add(serviceProviderBoundaries.get(i).getFarmId());
        }
        editor.putString(context.getResources().getString(R.string.service_provider_farms), new Gson().toJson(serviceProviderFarmIds)).apply();
    }

    private List<Integer> getSharedFarmIds(JSONObject user) {
        List<Integer> sharedFarmIds = new ArrayList<>();
        JSONArray shareStatuses;
        try {
            shareStatuses = user.getJSONArray("share_statuses");
            if (shareStatuses.length() > 0) {
                for (int m = 0; m < shareStatuses.length(); m++) {
                    JSONObject shareStatus = shareStatuses.getJSONObject(m);
                    JSONArray farmIdsArray = shareStatus.getJSONArray("farms");
                    if (farmIdsArray.length() > 0) {
                        for (int j = 0; j < farmIdsArray.length(); j++) {
                            sharedFarmIds.add(farmIdsArray.getInt(j));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sharedFarmIds;
    }
    private void checkForDeletedFarms() {
        String allClientIdsString = convertListToString(allClientsIds);
        List<JSONObject> farmJsonList = sqLiteDatabaseHandler.getFarmNamesAndIdList(allClientIdsString);
        List<Integer> localFarmIds = new ArrayList<>();
        for (int k = 0; k < farmJsonList.size(); k++) {
            JSONObject farm = farmJsonList.get(k);
            try {
                Integer farmId = farm.getInt("farm_id");
                localFarmIds.add(farmId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (Integer farmId: localFarmIds) {
            if (!farmIdsFromServer.contains(farmId)) {
                sqLiteDatabaseHandler.deleteFarm(farmId);
                sqLiteDatabaseHandler.deleteAllBoundariesThatBelongToFarm(farmId);
            }
        }
    }
    private void addFarmsToDb(List<JSONObject> farms) {
        for (JSONObject farm: farms) {
            try {
                String name = farm.getString("name");
                Integer farmId = farm.getInt("id");
                Integer clientId = farm.getInt("client_id");
                JSONArray cropFamilyIds = farm.getJSONArray("crop_family_ids");
                String cropFamilyIdsString = cropFamilyIds.toString().replaceAll("\\[", "").replaceAll("]","");
                sqLiteDatabaseHandler.createFarmName(name, farmId, clientId, cropFamilyIdsString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private JSONObject extractFarmData(JSONObject farmObject, Integer clientId) {
        JSONObject farm = new JSONObject();
        try {
            String farmName = farmObject.getString("name");
            Integer farmId = farmObject.getInt("id");
            JSONArray cropFamilyIds = farmObject.getJSONArray("crop_family_ids");
            farm.put("name", farmName);
            farm.put("id", farmId);
            farm.put("client_id", clientId);
            farm.put("crop_family_ids", cropFamilyIds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return farm;
    }

    private String convertListToString(List<Integer> intList) {
        return intList.toString().replaceAll("\\[", "").replaceAll("]","");
    }

    public List<Integer> getAllClientsIds() {
        return allClientsIds;
    }

    private void setAllClientsIds() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.all_client_ids), new Gson().toJson(allClientsIds)).apply();
    }
    public Integer getActiveClientId() {
        return activeClientId;
    }

    public Integer getUserId() {
        return userId;
    }

    private void addServiceProviderBoundariesToLocalDatabase() {
        if(!serviceProviderBoundaries.isEmpty()) {
            sqLiteDatabaseHandler.addBoundaryDetailList(serviceProviderBoundaries);
        }
    }


}
