package co.aerobotics.android.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.aerobotics.android.mission.MissionDetails;

/**
 * Created by michaelwootton on 8/23/17.
 */

public class SQLiteDatabaseHandler extends SQLiteOpenHelper {

    private static final String LOG = "DatabaseHelper";
    private Context context;

    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_NAME = "BoundariesDB";
    private static final String TABLE_BOUNDARIES = "Boundaries";
    private static final String TABLE_REQUESTS = "Requests";
    private static final String KEY_ID = "id";

    private static final String KEY_REQUEST = "request";
    private static final String[] REQUEST_COLUMNS = {KEY_ID, KEY_REQUEST};

    private static final String KEY_NAME= "name";
    private static final String KEY_BOUNDARY_ID = "boundary_id";
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_POINTS = "polygon_points";
    private static final String KEY_ANGLE = "angle";
    private static final String KEY_OVERLAP = "overlap";
    private static final String KEY_SIDELAP = "sidelap";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_DISPLAY = "display";
    private static final String KEY_CAMERA = "camera";
    private static final String KEY_BOUNDARY_FARM_ID = "farm_id";
    private static final String KEY_BOUNDARY_CROPTYPE_ID = "crop_type_id";

    private static final String[] BOUNDARY_TABLE_COLUMNS = {KEY_NAME, KEY_BOUNDARY_ID, KEY_POINTS,
            KEY_ANGLE, KEY_OVERLAP, KEY_SIDELAP, KEY_ALTITUDE, KEY_SPEED, KEY_REQUEST, KEY_CLIENT_ID,
            KEY_DISPLAY, KEY_CAMERA, KEY_BOUNDARY_FARM_ID, KEY_BOUNDARY_CROPTYPE_ID};

    private static final String TABLE_CROPTYPES = "croptypes";
    private static final String KEY_CROPTYPE = "croptype";
    private static final String KEY_CROPTYPE_ID = "croptype_id";
    private static final String[] CROPTYPE_COLUMNS = {KEY_ID, KEY_CROPTYPE, KEY_CROPTYPE_ID};

    private static final String TABLE_FARMNAMES = "farmnames";
    private static final String KEY_FARMNAME = "farmname";
    private static final String KEY_FARMNAME_ID = "farmname_id";
    private static final String KEY_FARMNAME_CROP_FAMILY_ID = "farmname_crop_family_id";
    private static final String[] FARMNAME_COLUMNS = {KEY_ID, KEY_FARMNAME, KEY_FARMNAME_ID, KEY_CLIENT_ID, KEY_FARMNAME_CROP_FAMILY_ID};

    private static final String TABLE_MISSION_DETAILS = "mission_details";
    private static final String KEY_MISSION_WAYPOINTS = "mission_details_waypoints";
    private static final String KEY_MISSION_ALTITUDE = "mission_details_altitude";
    private static final String KEY_MISSION_IMAGE_DISTANCE = "mission_details_image_distance";
    private static final String KEY_MISSION_SPEED = "mission_details_speed";

    private static final String TABLE_CROP_FAMILIES = "crop_families";
    private static final String KEY_CROP_FAMILY_ID = "crop_family_id";
    private static final String KEY_CROP_FAMILY_NAME = "crop_family_name";

    private static final String CREATE_REQUEST_TABLE = "CREATE TABLE " + TABLE_REQUESTS + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            KEY_REQUEST + " TEXT NOT NULL)";

    private static final String CREATE_CROPTYPE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CROPTYPES + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            KEY_CROPTYPE + " TEXT NOT NULL, " + KEY_CROPTYPE_ID + " INTEGER, " + "UNIQUE(" + KEY_CROPTYPE + ")" +  " ON CONFLICT IGNORE)";

    private static final String CREATE_FARMNAME_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_FARMNAMES + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            KEY_FARMNAME + " TEXT NOT NULL, " + KEY_FARMNAME_ID + " INTEGER, " + KEY_CLIENT_ID + " INTEGER, " + KEY_FARMNAME_CROP_FAMILY_ID + " TEXT, " + "UNIQUE(" + KEY_FARMNAME_ID + ")" +  " ON CONFLICT IGNORE)";

    private static final String CREATE_BOUNDARY_TABLE = "CREATE TABLE IF NOT EXISTS Boundaries (" + KEY_ID +  " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "boundary_id TEXT, " + KEY_POINTS + " TEXT, " + "name TEXT, " + "angle INTEGER, " + "overlap INTEGER, " + "sidelap INTEGER, "
            + "altitude INTEGER, " + "speed INTEGER, request TEXT, client_id INTEGER, display INTEGER, camera TEXT, farm_id INTEGER," +
            " crop_type_id INTEGER, UNIQUE(" + KEY_BOUNDARY_ID + ")" + " ON CONFLICT IGNORE)";

    private static final String CREATE_MISSION_DETAILS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MISSION_DETAILS + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            KEY_MISSION_WAYPOINTS + " TEXT NOT NULL, " + KEY_MISSION_ALTITUDE + " REAL, " + KEY_MISSION_IMAGE_DISTANCE + " REAL, " + KEY_MISSION_SPEED + " REAL) ";

    private static String CREATE_CROP_FAMILIES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CROP_FAMILIES +  " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            KEY_CROP_FAMILY_ID + " INTEGER, " + KEY_CROP_FAMILY_NAME + " TEXT)";

    public SQLiteDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        Log.d(LOG, CREATE_BOUNDARY_TABLE);
        Log.d(LOG, CREATE_CROPTYPE_TABLE);
        Log.d(LOG, CREATE_FARMNAME_TABLE);

        db.execSQL(CREATE_BOUNDARY_TABLE);
        db.execSQL(CREATE_CROPTYPE_TABLE);
        db.execSQL(CREATE_FARMNAME_TABLE);
        db.execSQL(CREATE_MISSION_DETAILS_TABLE);
        db.execSQL(CREATE_CROP_FAMILIES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // you can implement here migration process
        if(oldVersion < 2) {
            upgradeVersion2(db);
        }

        if(oldVersion < 3){
            upgradeVersion3(db);
        }

        if(oldVersion < 4){
            upgradeVersion4(db);
        }

        if(oldVersion < 5){
            upgradeVersion5(db);
        }

        if(oldVersion < 6){
            upgradeVersion6(db);
        }

        if (oldVersion < 7) {
            upgradeVersion7(db);
        }

        if (oldVersion < 8) {
            upgradeVersion8(db);
        }

    }

    private void upgradeVersion2(SQLiteDatabase db){
        db.execSQL("ALTER TABLE " + TABLE_BOUNDARIES + " ADD COLUMN " + KEY_REQUEST + " TEXT");
        db.execSQL("ALTER TABLE " + TABLE_BOUNDARIES + " ADD COLUMN " + KEY_POINTS + " TEXT");
        db.execSQL(CREATE_CROPTYPE_TABLE);
        db.execSQL(CREATE_FARMNAME_TABLE);
    }

    private void upgradeVersion3(SQLiteDatabase db){
        db.execSQL("ALTER TABLE " + TABLE_BOUNDARIES + " ADD COLUMN " + KEY_CLIENT_ID + " INTEGER");
        db.execSQL("ALTER TABLE " + TABLE_BOUNDARIES + " ADD COLUMN " + KEY_DISPLAY + " INTEGER");
    }

    private void upgradeVersion4(SQLiteDatabase db){
        db.execSQL("ALTER TABLE " + TABLE_BOUNDARIES + " ADD COLUMN " + KEY_CAMERA + " TEXT");
    }

    private void upgradeVersion5(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FARMNAMES);
        db.execSQL(CREATE_FARMNAME_TABLE);
    }

    private void upgradeVersion6(SQLiteDatabase db) {
        db.execSQL(CREATE_MISSION_DETAILS_TABLE);
    }

    private void upgradeVersion7(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + TABLE_BOUNDARIES + " ADD COLUMN " + KEY_BOUNDARY_FARM_ID + " INTEGER");
    }

    private void upgradeVersion8(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FARMNAMES);
        db.execSQL(CREATE_FARMNAME_TABLE);
        db.execSQL("ALTER TABLE " + TABLE_BOUNDARIES + " ADD COLUMN " + KEY_BOUNDARY_CROPTYPE_ID + " INTEGER");
        // db.execSQL("ALTER TABLE " + TABLE_FARMNAMES + " ADD COLUMN " + KEY_FARMNAME_CROP_FAMILY_ID + " TEXT");
        db.execSQL(CREATE_CROP_FAMILIES_TABLE);
    }

    public List<MissionDetails> getAllMissionDetails(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<MissionDetails> missionDetailsList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_MISSION_DETAILS;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                MissionDetails missionDetails = new MissionDetails();
                missionDetails.setAltitude(cursor.getFloat(cursor.getColumnIndex(KEY_MISSION_ALTITUDE)));
                missionDetails.setImageDistance(cursor.getFloat(cursor.getColumnIndex(KEY_MISSION_IMAGE_DISTANCE)));
                missionDetails.setWaypoints(cursor.getString(cursor.getColumnIndex(KEY_MISSION_WAYPOINTS)));
                missionDetails.setSpeed(cursor.getFloat(cursor.getColumnIndex(KEY_MISSION_SPEED)));
                missionDetailsList.add(missionDetails);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return missionDetailsList;
    }

    public void addMissionDetails(List<MissionDetails> missionDetailsList) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (MissionDetails missionDetails : missionDetailsList) {
            ContentValues values = new ContentValues();
            values.put(KEY_MISSION_ALTITUDE, missionDetails.getAltitude());
            values.put(KEY_MISSION_IMAGE_DISTANCE, missionDetails.getImageDistance());
            values.put(KEY_MISSION_WAYPOINTS, missionDetails.getWaypoints());
            values.put(KEY_MISSION_SPEED, missionDetails.getSpeed());
            db.insert(TABLE_MISSION_DETAILS, null, values);
        }
        db.close();
    }

    public void deleteMissionDetails() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_MISSION_DETAILS);
        db.close();
    }

    public JSONArray getOfflineBoundariesForFarm(Integer tempFarmId, Integer farmId) {
        JSONArray params = new JSONArray();
        String query = "SELECT * FROM " + TABLE_BOUNDARIES + " WHERE " + KEY_BOUNDARY_ID + " LIKE '%_temp' AND " + KEY_BOUNDARY_FARM_ID + " = " + tempFarmId.toString();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("name", c.getString(c.getColumnIndex(KEY_NAME)));
                    jsonObject.put("polygon", c.getString(c.getColumnIndex(KEY_POINTS)));
                    jsonObject.put("client_id", c.getString(c.getColumnIndex(KEY_CLIENT_ID)));
                    jsonObject.put("farm_id", farmId);
                    jsonObject.put("crop_type_id", c.getString(c.getColumnIndex(KEY_BOUNDARY_CROPTYPE_ID)));
                    jsonObject.put("temp_id", c.getString(c.getColumnIndex(KEY_BOUNDARY_ID)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                params.put(jsonObject);
            } while (c.moveToNext());
            c.close();
        }
        db.close();
        return params;
    }
    public JSONArray getOfflineBoundariesForSyncedFarms() {
        JSONArray params = new JSONArray();
        String query = "SELECT * FROM " + TABLE_BOUNDARIES + " WHERE " + KEY_BOUNDARY_ID + " LIKE '%_temp' AND " + KEY_BOUNDARY_FARM_ID + " > 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("name", c.getString(c.getColumnIndex(KEY_NAME)));
                    jsonObject.put("polygon", c.getString(c.getColumnIndex(KEY_POINTS)));
                    jsonObject.put("client_id", c.getString(c.getColumnIndex(KEY_CLIENT_ID)));
                    jsonObject.put("farm_id", c.getString(c.getColumnIndex(KEY_BOUNDARY_FARM_ID)));
                    jsonObject.put("crop_type_id", c.getString(c.getColumnIndex(KEY_BOUNDARY_CROPTYPE_ID)));
                    jsonObject.put("temp_id", c.getString(c.getColumnIndex(KEY_BOUNDARY_ID)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                params.put(jsonObject);
            } while (c.moveToNext());
            c.close();
        }
        db.close();
        return params;
    }

    public List<JSONObject> getFarmNamesAndIdList(String clientIds) {
        // JSONArray farms = new JSONArray();
        // Map<String, Integer> farms = new HashMap<>();
        List<JSONObject> farms = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_FARMNAMES + " WHERE " + KEY_CLIENT_ID + " IN (" + clientIds + "); ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                JSONObject json = new JSONObject();
                try {
                    json.put("name", c.getString(c.getColumnIndex(KEY_FARMNAME)));
                    json.put("farm_id", c.getInt(c.getColumnIndex(KEY_FARMNAME_ID)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                farms.add(json);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return farms;
    }

    /**
     * Add a new farm name to the local database
     * @param farmname String farmName
     * @param id Int farmId or null if it is a new farm created locally
     * @return Long primary key id
     */

    public long createFarmName(String farmname, Integer id, Integer clientId, String cropFamilyIds){
        ContentValues values = new ContentValues();
        values.put(KEY_FARMNAME, farmname);
        values.put(KEY_CLIENT_ID, clientId);
        values.put(KEY_FARMNAME_CROP_FAMILY_ID, cropFamilyIds);
        if(id!=null) {
            values.put(KEY_FARMNAME_ID, id);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        long farm_id = -1;
        if (!isFarmInDB(id, db)) {
            farm_id = db.insert(TABLE_FARMNAMES, null, values);
        } else {
            farm_id = db.update(TABLE_FARMNAMES, values,  KEY_FARMNAME_ID + " = ?", new String[]{id.toString()});
        }
        db.close();
        return farm_id;
    }

    public void deleteFarm(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FARMNAMES, KEY_FARMNAME_ID + " = ?", new String[]{id.toString()});
        db.close();
    }

    public void deleteAllBoundariesThatBelongToFarm(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOUNDARIES, KEY_BOUNDARY_FARM_ID + " = ?", new String[]{id.toString()});
        db.close();
    }

    private boolean isFarmInDB(Integer id, SQLiteDatabase db){
        String sql ="SELECT * FROM " + TABLE_FARMNAMES + " WHERE " + KEY_FARMNAME_ID + " LIKE " + id.toString();
        Cursor cursor= db.rawQuery(sql,null);
        boolean found = cursor.getCount()>0;
        cursor.close();
        return found;
    }

    public void addOfflineFarm(String farmName, int clientId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FARMNAME, farmName);
        values.put(KEY_CLIENT_ID, clientId);
        long primary_key = db.insert(TABLE_FARMNAMES, null, values);
        int tempId = (int) primary_key*-1;
        ContentValues idValue = new ContentValues();
        idValue.put(KEY_FARMNAME_ID, tempId);
        db.update(TABLE_FARMNAMES, idValue, KEY_ID + " = ?", new String[]{String.valueOf(primary_key)});
        db.close();
    }

    public JSONArray getOfflineFarms(int clientId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_FARMNAMES, FARMNAME_COLUMNS,
                KEY_FARMNAME_ID + " < 0 AND " + KEY_CLIENT_ID + " = " + clientId,
                new String[]{}, null, null, null, null);
        JSONArray array = new JSONArray();
        if (c.moveToFirst()) {
            do{
                JSONObject farm = new JSONObject();
                try {
                    farm.put("name", c.getString(c.getColumnIndex(KEY_FARMNAME)));
                    farm.put("primary_key_id", c.getInt(c.getColumnIndex(KEY_ID)));
                    farm.put("client_id", c.getInt(c.getColumnIndex(KEY_CLIENT_ID)));
                    farm.put("temp_id", c.getInt(c.getColumnIndex(KEY_FARMNAME_ID)));
                    array.put(farm);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while ( c.moveToNext());
        }
        c.close();
        db.close();
        return array;
    }

    /**
     * Update the farm id in local database
     * @param id Int new farm id
     */
    public void updateFarmNameId(Integer id, Integer primaryKey) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FARMNAME_ID, id);
        db.update(TABLE_FARMNAMES, values, KEY_ID + " = ? ", new String[]{primaryKey.toString()});
        db.close();
    }

    public int getFarmClientId(Integer farmId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql ="SELECT " + KEY_CLIENT_ID +  " FROM " + TABLE_FARMNAMES + " WHERE " + KEY_FARMNAME_ID + " LIKE " + farmId.toString();
        Cursor c = db.rawQuery(sql,null);
        int client_id = -1;
        if (c.moveToFirst()) {
            client_id = c.getInt(c.getColumnIndex(KEY_CLIENT_ID));
        }
        c.close();
        db.close();
        return client_id;
    }

    /**
     * Get all crop types in local database
     * @return String List of crop type names
     */
    public List<String> getAllCropTypes(){
        List<String> croptypes =  new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CROPTYPES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                croptypes.add(c.getString(c.getColumnIndex(KEY_CROPTYPE)));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return croptypes;
    }

    /**
     * Get a crop type id from database given the crop name
     * @param croptype String name of crop type
     * @return Int croptype id
     */

    public int getCropTypeId(String croptype){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + KEY_ID + " FROM " + TABLE_CROPTYPES + " WHERE " + KEY_CROPTYPE + " LIKE '" + croptype + "'";
        Log.d(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        int id = c.getInt(c.getColumnIndex(KEY_ID));
        c.close();
        db.close();
        return id;
    }

    /**
     * Check if crop type of given name is currently in the database
     * @param croptypeId String id of crop type
     * @return Boolean True if in database
     */

    private boolean isCropTypeInDb(Integer croptypeId, SQLiteDatabase db){
        String sql ="SELECT * FROM " + TABLE_CROPTYPES + " WHERE " + KEY_CROPTYPE_ID + " LIKE " + croptypeId.toString();
        Cursor cursor= db.rawQuery(sql,null);
        boolean found = cursor.getCount()>0;
        cursor.close();
        return found;
    }

    /**
     * Add a new crop type to local database
     * @param croptype String name of crop type
     * @param id Int Aeroview id or null if crop type is being created locally
     * @return Long primary key id from database table
     */

    public long createCropType(String croptype, Integer id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CROPTYPE, croptype);
        if(id!=null) {
            values.put(KEY_ID, id);
        }
        long croptype_id = -1;
        if (!isCropTypeInDb(id, db)) {
            croptype_id = db.insert(TABLE_CROPTYPES, null, values);
        }
        db.close();
        return croptype_id;
    }

    public void createCropFamily(String cropFamilyName, Integer cropFamilyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CROP_FAMILY_NAME, cropFamilyName);
        values.put(KEY_CROP_FAMILY_ID, cropFamilyId);

        if (!isCropFamilyInDb(cropFamilyId, db)) {
            db.insert(TABLE_CROP_FAMILIES, null, values);
        }
        db.close();
    }

    public List<NameWithId> getCropFamilies() {
        List<NameWithId> cropFamilies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_CROP_FAMILIES;
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                String name = c.getString(c.getColumnIndex(KEY_CROP_FAMILY_NAME));
                String id = c.getString(c.getColumnIndex(KEY_CROP_FAMILY_ID));
                NameWithId cropFamily = new NameWithId(name, id);
                cropFamilies.add(cropFamily);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return cropFamilies;
    }

    private boolean isCropFamilyInDb(Integer cropFamilyId, SQLiteDatabase db){
        String sql ="SELECT * FROM " + TABLE_CROP_FAMILIES + " WHERE " + KEY_CROP_FAMILY_ID + " LIKE " + cropFamilyId.toString();
        Cursor cursor= db.rawQuery(sql,null);
        boolean found = cursor.getCount()>0;
        cursor.close();
        return found;
    }


    /**
     * Update the crop type id of locally created crop types with crop type id from the AeroView database
     * @param croptype String name of crop type
     * @param id Int crop type id
     */

    public void updateCropTypes(String croptype, int id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CROPTYPE_ID, id);
        db.update(TABLE_CROPTYPES, values, KEY_CROPTYPE + " = ?", new String[]{croptype});
        db.close();
    }


    public void resetDisplayBoundaries(SQLiteDatabase db){
        db = SQLiteDatabaseHandler.this.getWritableDatabase();
        String query = "UPDATE " + TABLE_BOUNDARIES + " SET " + KEY_DISPLAY + " = 0";
        db.execSQL(query);
    }

    public void addBoundaryDetail(BoundaryDetail boundaryDetail){
        SQLiteDatabase db_write = SQLiteDatabaseHandler.this.getWritableDatabase();

            if (!isBoundaryInDB(boundaryDetail.getBoundaryId(), db_write)) {
                ContentValues values = new ContentValues();
                values.put(KEY_NAME, boundaryDetail.getName());
                values.put(KEY_BOUNDARY_ID, boundaryDetail.getBoundaryId());
                values.put(KEY_POINTS, boundaryDetail.getPoints());
                values.put(KEY_ANGLE, boundaryDetail.getAngle());
                values.put(KEY_OVERLAP, boundaryDetail.getOverlap());
                values.put(KEY_SIDELAP, boundaryDetail.getSidelap());
                values.put(KEY_ALTITUDE, boundaryDetail.getAltitude());
                values.put(KEY_SPEED, boundaryDetail.getSpeed());
                values.put(KEY_DISPLAY, boundaryDetail.isDisplay());
                values.put(KEY_CAMERA, boundaryDetail.getCamera());
                values.put(KEY_BOUNDARY_FARM_ID, boundaryDetail.getFarmId());
                // insert
                db_write.insert(TABLE_BOUNDARIES, null, values);

            }
        db_write.close();
    }

    public BoundaryDetail getBoundaryDetail(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOUNDARIES, BOUNDARY_TABLE_COLUMNS, "boundary_id = ?",
                new String[] {id}, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        BoundaryDetail boundaryDetail = new BoundaryDetail();
        boundaryDetail.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
        boundaryDetail.setBoundaryId(cursor.getString(cursor.getColumnIndex(KEY_BOUNDARY_ID)));
        boundaryDetail.setPoints(cursor.getString(cursor.getColumnIndex(KEY_POINTS)));
        boundaryDetail.setAngle((double)cursor.getInt(cursor.getColumnIndex(KEY_ANGLE)));
        boundaryDetail.setOverlap((double)cursor.getInt(cursor.getColumnIndex(KEY_OVERLAP)));
        boundaryDetail.setSidelap((double)cursor.getInt(cursor.getColumnIndex(KEY_SIDELAP)));
        boundaryDetail.setAltitude((double)cursor.getInt(cursor.getColumnIndex(KEY_ALTITUDE)));
        boundaryDetail.setSpeed((double)cursor.getInt(cursor.getColumnIndex(KEY_SPEED)));
        boundaryDetail.setClientId(cursor.getInt(cursor.getColumnIndex(KEY_CLIENT_ID)));
        boundaryDetail.setDisplay(cursor.getInt(cursor.getColumnIndex(KEY_DISPLAY)) == 1);
        boundaryDetail.setCamera(cursor.getString(cursor.getColumnIndex(KEY_CAMERA)));
        boundaryDetail.setFarmId(cursor.getInt(cursor.getColumnIndex(KEY_BOUNDARY_FARM_ID)));
        cursor.close();
        db.close();
        return boundaryDetail;
    }

    public List<BoundaryDetail> getBoundaryDetailsForFarmIds(String activeFarmIds){
        SQLiteDatabase db = this.getReadableDatabase();
        List<BoundaryDetail> boundaryDetails = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_BOUNDARIES + " WHERE " + KEY_BOUNDARY_FARM_ID + " IN (" + activeFarmIds + ") ";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                BoundaryDetail boundaryDetail = new BoundaryDetail();
                boundaryDetail.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                boundaryDetail.setBoundaryId(cursor.getString(cursor.getColumnIndex(KEY_BOUNDARY_ID)));
                boundaryDetail.setPoints(cursor.getString(cursor.getColumnIndex(KEY_POINTS)));
                boundaryDetail.setAngle((double)cursor.getInt(cursor.getColumnIndex(KEY_ANGLE)));
                boundaryDetail.setOverlap((double)cursor.getInt(cursor.getColumnIndex(KEY_OVERLAP)));
                boundaryDetail.setSidelap((double)cursor.getInt(cursor.getColumnIndex(KEY_SIDELAP)));
                boundaryDetail.setAltitude((double)cursor.getInt(cursor.getColumnIndex(KEY_ALTITUDE)));
                boundaryDetail.setSpeed((double)cursor.getInt(cursor.getColumnIndex(KEY_SPEED)));
                boundaryDetail.setClientId(cursor.getInt(cursor.getColumnIndex(KEY_CLIENT_ID)));
                boundaryDetail.setDisplay(cursor.getInt(cursor.getColumnIndex(KEY_DISPLAY)) == 1);
                boundaryDetail.setFarmId(cursor.getInt(cursor.getColumnIndex(KEY_BOUNDARY_FARM_ID)));
                boundaryDetails.add(boundaryDetail);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return boundaryDetails;
    }

    public void addBoundaryDetailList(List<BoundaryDetail> boundaryDetailList){
        new AddBoundaryDetailTask(boundaryDetailList).execute();
    }

    public String addOfflineBoundaryDetail(BoundaryDetail boundaryDetail){
        SQLiteDatabase db = SQLiteDatabaseHandler.this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, boundaryDetail.getName());
        values.put(KEY_POINTS, boundaryDetail.getPoints());
        values.put(KEY_ANGLE, boundaryDetail.getAngle());
        values.put(KEY_OVERLAP, boundaryDetail.getOverlap());
        values.put(KEY_SIDELAP, boundaryDetail.getSidelap());
        values.put(KEY_ALTITUDE, boundaryDetail.getAltitude());
        values.put(KEY_SPEED, boundaryDetail.getSpeed());
        values.put(KEY_CLIENT_ID, boundaryDetail.getClientId());
        values.put(KEY_DISPLAY, boundaryDetail.isDisplay() ? 1 : 0);
        values.put(KEY_BOUNDARY_FARM_ID, boundaryDetail.getFarmId());
        values.put(KEY_BOUNDARY_CROPTYPE_ID, boundaryDetail.getCropTypeId());
        long primary_key = db.insert(TABLE_BOUNDARIES, null, values);

        String temp_id = (String.valueOf(primary_key) + "_temp");

        ContentValues idValue = new ContentValues();
        idValue.put(KEY_BOUNDARY_ID, temp_id);
        db.update(TABLE_BOUNDARIES, // table
                idValue, // column/value
                KEY_ID + " = ?", // selections
                new String[]{String.valueOf(primary_key)});

        db.close();
        return temp_id;

    }

    public void addRequestToOfflineBoundary(String id, String json){
        SQLiteDatabase db = SQLiteDatabaseHandler.this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_REQUEST, json);
        int i = db.update(TABLE_BOUNDARIES, // table
                values, // column/value
                "boundary_id = ?", // selections
                new String[]{id});

    }

    private int updateBoundaryDetail(BoundaryDetail boundaryDetail, SQLiteDatabase db){

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, boundaryDetail.getName());
        values.put(KEY_BOUNDARY_ID, boundaryDetail.getBoundaryId());
        values.put(KEY_ANGLE, (int) boundaryDetail.getAngle());
        values.put(KEY_OVERLAP,(int) boundaryDetail.getOverlap());
        values.put(KEY_SIDELAP, (int) boundaryDetail.getSidelap());
        values.put(KEY_ALTITUDE, (int) boundaryDetail.getAltitude());
        values.put(KEY_SPEED, (int) boundaryDetail.getSpeed());
        values.put(KEY_POINTS, boundaryDetail.getPoints());
        values.put(KEY_CLIENT_ID, boundaryDetail.getClientId());
        values.put(KEY_DISPLAY, boundaryDetail.isDisplay() ? 1 : 0);
        values.put(KEY_CAMERA, boundaryDetail.getCamera());
        values.put(KEY_BOUNDARY_FARM_ID, boundaryDetail.getFarmId());

        int i = db.update(TABLE_BOUNDARIES, // table
                values, // column/value
                "boundary_id = ?", // selections
                new String[]{String.valueOf(boundaryDetail.getBoundaryId())});
        return i;
    }

    private void updateBoundaryPoints(BoundaryDetail boundaryDetail, SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, boundaryDetail.getName());
        values.put(KEY_CLIENT_ID, boundaryDetail.getClientId());
        values.put(KEY_POINTS, boundaryDetail.getPoints());
        values.put(KEY_DISPLAY, boundaryDetail.isDisplay() ? 1 : 0);
        values.put(KEY_BOUNDARY_FARM_ID, boundaryDetail.getFarmId());

        int i = db.update(TABLE_BOUNDARIES, // table
                values, // column/value
                "boundary_id = ?", // selections
                new String[]{String.valueOf(boundaryDetail.getBoundaryId())});
    }

    public void runUpdateTask(BoundaryDetail boundaryDetail){
        new UpdateBoundaryDetailTask(boundaryDetail).execute();
    }

    private boolean isBoundaryInDB(String id, SQLiteDatabase db){
        String sql ="SELECT * FROM " + TABLE_BOUNDARIES + " WHERE boundary_id LIKE " + id;
        Cursor cursor= db.rawQuery(sql,null);
        boolean found = cursor.getCount()>0;
        cursor.close();
        return found;
    }

    public void removeRequest(String id) {
        SQLiteDatabase db = SQLiteDatabaseHandler.this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_REQUEST, "");
        db.update(TABLE_BOUNDARIES, values, KEY_BOUNDARY_ID + " = ?", new String[] {id} );
        db.close();
    }

    public void updateOfflineBoundary(String boundaryTempId, String boundaryId, String farmId) {
        SQLiteDatabase db = SQLiteDatabaseHandler.this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BOUNDARY_ID, boundaryId);
        values.put(KEY_BOUNDARY_FARM_ID, farmId);
        db.update(TABLE_BOUNDARIES, values, KEY_BOUNDARY_ID + " = ?", new String[] {boundaryTempId});
        db.close();
    }

    private class UpdateBoundaryDetailTask extends AsyncTask<Void, Void, Void>{

        private BoundaryDetail boundaryDetail;

        private UpdateBoundaryDetailTask(BoundaryDetail boundaryDetail){
            this.boundaryDetail = boundaryDetail;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            SQLiteDatabase db = SQLiteDatabaseHandler.this.getWritableDatabase();
            updateBoundaryDetail(boundaryDetail,db);
            db.close();
            return null;
        }
    }

    private class AddBoundaryDetailTask extends AsyncTask<Void, Void, Boolean>{

        private List<BoundaryDetail> boundaryDetailList;

        public AddBoundaryDetailTask(List<BoundaryDetail> boundaryDetailList){
            this.boundaryDetailList = boundaryDetailList;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            SQLiteDatabase db_write = SQLiteDatabaseHandler.this.getWritableDatabase();
            resetDisplayBoundaries(db_write);
            for (BoundaryDetail boundaryDetail : boundaryDetailList) {
                if (!isBoundaryInDB(boundaryDetail.getBoundaryId(), db_write)) {
                    ContentValues values = new ContentValues();
                    values.put(KEY_NAME, boundaryDetail.getName());
                    values.put(KEY_BOUNDARY_ID, boundaryDetail.getBoundaryId());
                    values.put(KEY_POINTS, boundaryDetail.getPoints());
                    values.put(KEY_ANGLE, boundaryDetail.getAngle());
                    values.put(KEY_OVERLAP, boundaryDetail.getOverlap());
                    values.put(KEY_SIDELAP, boundaryDetail.getSidelap());
                    values.put(KEY_ALTITUDE, boundaryDetail.getAltitude());
                    values.put(KEY_SPEED, boundaryDetail.getSpeed());
                    values.put(KEY_CLIENT_ID, boundaryDetail.getClientId());
                    values.put(KEY_DISPLAY, boundaryDetail.isDisplay() ? 1 : 0);
                    values.put(KEY_BOUNDARY_FARM_ID, boundaryDetail.getFarmId());

                    // insert
                    db_write.insert(TABLE_BOUNDARIES, null, values);

                } else{
                    updateBoundaryPoints(boundaryDetail, db_write);
                }

            }
            db_write.close();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success){
            if(success){
                new AeroviewPolygons(context).addPolygonsToMap();
                //Toast.makeText(context, "Boundaries Successfully Updated" , Toast.LENGTH_LONG).show();
            }
        }
    }
}
