package co.aerobotics.android.data;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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


    public FarmDataHandler(Context context, JSONObject user) {
        this.user = user;
        sqLiteDatabaseHandler = new SQLiteDatabaseHandler(context);
    }

    public void parseUsersFarms() throws JSONException {
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

        farms.removeAll(invalidFarms);
        checkForDeletedFarms();
        addFarmsToDb(farms);
    }

    private List<Integer> getSharedFarmIds(JSONObject user) {
        List<Integer> sharedFarmIds = new ArrayList<>();
        JSONArray shareStatuses = null;
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
                Integer client_id = farm.getInt("client_id");
                sqLiteDatabaseHandler.createFarmName(name, farmId, client_id);
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
            farm.put("name", farmName);
            farm.put("id", farmId);
            farm.put("client_id", clientId);
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

    public Integer getActiveClientId() {
        return activeClientId;
    }

    public Integer getUserId() {
        return userId;
    }



}
