package co.aerobotics.android.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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

    private static final int DATABASE_VERSION = 6;
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

    private static final String[] COLUMNS = {KEY_NAME, KEY_BOUNDARY_ID, KEY_POINTS,
            KEY_ANGLE, KEY_OVERLAP, KEY_SIDELAP, KEY_ALTITUDE, KEY_SPEED, KEY_REQUEST, KEY_CLIENT_ID, KEY_DISPLAY, KEY_CAMERA};

    private static final String TABLE_CROPTYPES = "croptypes";
    private static final String KEY_CROPTYPE = "croptype";
    private static final String KEY_CROPTYPE_ID = "croptype_id";
    private static final String[] CROPTYPE_COLUMNS = {KEY_ID, KEY_CROPTYPE, KEY_CROPTYPE_ID};

    private static final String TABLE_FARMNAMES = "farmnames";
    private static final String KEY_FARMNAME = "farmname";
    private static final String KEY_FARMNAME_ID = "farmname_id";
    private static final String[] FARMNAME_COLUMNS = {KEY_ID, KEY_FARMNAME, KEY_FARMNAME_ID};

    private static final String TABLE_MISSION_DETAILS = "mission_details";
    private static final String KEY_MISSION_WAYPOINTS = "mission_details_waypoints";
    private static final String KEY_MISSION_ALTITUDE = "mission_details_altitude";
    private static final String KEY_MISSION_IMAGE_DISTANCE = "mission_details_image_distance";
    private static final String KEY_MISSION_SPEED = "mission_details_speed";

    private static final String CREATE_REQUEST_TABLE = "CREATE TABLE " + TABLE_REQUESTS + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            KEY_REQUEST + " TEXT NOT NULL)";

    private static final String CREATE_CROPTYPE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CROPTYPES + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            KEY_CROPTYPE + " TEXT NOT NULL, " + KEY_CROPTYPE_ID + " INTEGER, " + "UNIQUE(" + KEY_CROPTYPE + ")" +  " ON CONFLICT IGNORE" + ")";

    private static final String CREATE_FARMNAME_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_FARMNAMES + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            KEY_FARMNAME + " TEXT NOT NULL, " + KEY_FARMNAME_ID + " INTEGER, " + KEY_CLIENT_ID + " INTEGER, "  + "UNIQUE(" + KEY_FARMNAME + ")" +  " ON CONFLICT IGNORE" + ")";

    private static final String CREATE_BOUNDARY_TABLE = "CREATE TABLE IF NOT EXISTS Boundaries (" + KEY_ID +  " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "boundary_id TEXT, " + KEY_POINTS + " TEXT, " + "name TEXT, " +
            "angle INTEGER, " + "overlap INTEGER, " + "sidelap INTEGER, "
            + "altitude INTEGER, " + "speed INTEGER, request TEXT, client_id INTEGER, display INTEGER, camera TEXT, UNIQUE(" + KEY_BOUNDARY_ID + ")" + " ON CONFLICT IGNORE)";
    private static final String CREATE_MISSION_DETAILS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MISSION_DETAILS + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            KEY_MISSION_WAYPOINTS + " TEXT NOT NULL, " + KEY_MISSION_ALTITUDE + " REAL, " + KEY_MISSION_IMAGE_DISTANCE + " REAL, " + KEY_MISSION_SPEED + " REAL) ";

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

    public long addRequest(String json){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_REQUEST, json);
        long key = db.insert(TABLE_REQUESTS, null, values);
        db.close();
        return key;
    }

    public List<String> getAllRequests(){
        List<String> requests = new ArrayList<>();
        String query = "SELECT "+ KEY_REQUEST + " FROM " + TABLE_BOUNDARIES + " WHERE " + KEY_REQUEST + " IS NOT NULL";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        if(c.moveToFirst()){
            do{
                requests.add(c.getString(c.getColumnIndex(KEY_REQUEST)));
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return requests;
    }

    /**
     * Return a String list of all the farms in local database
     * @return List<String> farmNames</>
     */

    public List<String> getAllFarmNames(Integer clientId){
        List<String> farmNames = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_FARMNAMES + " WHERE " + KEY_CLIENT_ID + " = " + clientId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                farmNames.add(c.getString(c.getColumnIndex(KEY_FARMNAME)));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return farmNames;
    }

    /**
     * Return the farm id from database given the farm name. The id will either be the AeroView
     * or a temp id if the farm hasn't yet been added to the AeroView Database
     * @param farmName String farmName
     * @return Int farmId
     */

    public int getFarmNameId(String farmName, Integer clientId){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_FARMNAMES + " WHERE " + KEY_FARMNAME + " LIKE '" + farmName + "'" + " AND " +  KEY_CLIENT_ID + " = " + clientId;
        Cursor c = db.query(TABLE_FARMNAMES, FARMNAME_COLUMNS, KEY_FARMNAME + " LIKE ? ", new String[]{farmName}, null, null, null, null);

        Log.d(LOG, selectQuery);

        if (c != null)
            c.moveToFirst();

        Integer id = c.getInt(c.getColumnIndex(KEY_FARMNAME_ID));
        Log.d(LOG, String.valueOf(id));
        if(id == 0){
            id = c.getInt(c.getColumnIndex(KEY_ID));
        }
        c.close();
        db.close();
        return id;
    }

    /**
     * Add a new farm name to the local database
     * @param farmname String farmName
     * @param id Int farmId or null if it is a new farm created locally
     * @return Long primary key id
     */

    public long createFarmName(String farmname, Integer id, Integer clientId){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FARMNAME, farmname);
        values.put(KEY_CLIENT_ID, clientId);
        if(id!=null) {
            values.put(KEY_FARMNAME_ID, id);
        }
        long farm_id = db.insert(TABLE_FARMNAMES, null, values);
        db.close();
        return farm_id;
    }

    /**
     * Update the farm id in local database
     * @param farmname String name of farm
     * @param id Int new farm id
     */
    public void updateFarmNameId(String farmname, Integer id, Integer clientId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FARMNAME_ID, id);
        db.update(TABLE_FARMNAMES, values, KEY_FARMNAME + " = ? AND " + KEY_CLIENT_ID + " = " + clientId , new String[]{farmname});
        db.close();
    }

    /**
     * Get all locally created farm names that have not been added to AeroView database yet
     * @return JSONArray [{name:"", id:""}, ...]
     */

    public JSONArray getLocalFarmNames(Integer clientId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_FARMNAMES, FARMNAME_COLUMNS,
                KEY_FARMNAME_ID + " IS NULL AND " + KEY_CLIENT_ID + " = " + clientId,
                new String[]{}, null, null, null, null);
        JSONArray array = new JSONArray();
        if(c.moveToFirst()){
            do{
                JSONObject nameIdPair = new JSONObject();
                try {
                    nameIdPair.put("name", c.getString(c.getColumnIndex(KEY_FARMNAME)));
                    nameIdPair.put("id", c.getInt(c.getColumnIndex(KEY_ID)));
                    array.put(nameIdPair);
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
     * @param croptype String name of crop type
     * @return Boolean True if in database
     */

    public boolean isCropTypeInTable(String croptype){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT COUNT(" + KEY_CROPTYPE + ") " + "FROM " + TABLE_CROPTYPES +
                " WHERE " + KEY_CROPTYPE + " LIKE " + "'" + croptype + "'";
        Cursor cursor= db.rawQuery(selectQuery,null);
        boolean found = cursor.getCount()>0;
        cursor.close();
        db.close();
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
        long croptype_id = db.insert(TABLE_CROPTYPES, null, values);
        db.close();
        return croptype_id;
    }

    /**
     * Get all new crop types that have been created locally and not been added to AeroView database
     * @return JSONArray [{name:"", id:""}, ...]
     */
    public JSONArray getLocalCropTypes(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_CROPTYPES, CROPTYPE_COLUMNS, KEY_CROPTYPE_ID + " IS NULL ", new String[]{}, null, null, null, null);
        JSONArray array = new JSONArray();

        if(c.moveToFirst()){
            do{
                JSONObject nameIdPair = new JSONObject();
                try {
                    nameIdPair.put("name", c.getString(c.getColumnIndex(KEY_CROPTYPE)));
                    nameIdPair.put("id", c.getString(c.getColumnIndex(KEY_ID)));
                    array.put(nameIdPair);
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

    private int getMaxId(String id, String table){
        String query = "SELECT MAX(" + id + ") " + "FROM " + table;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query,null);
        if(cursor!=null){
            cursor.moveToFirst();
        }
        int max = cursor.getInt(cursor.getColumnIndex(id));
        cursor.close();
        db.close();
        return max;
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
                // insert
                db_write.insert(TABLE_BOUNDARIES, null, values);

            }
        db_write.close();
    }

    public BoundaryDetail getBoundaryDetail(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOUNDARIES, COLUMNS, "boundary_id = ?",
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
        cursor.close();
        db.close();
        return boundaryDetail;
    }

    public List<BoundaryDetail> getAllBoundaryDetail(int client_id){
        SQLiteDatabase db = this.getReadableDatabase();
        List<BoundaryDetail> boundaryDetails = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_BOUNDARIES + " WHERE " + KEY_CLIENT_ID + " = " + client_id;

        Cursor cursor = db.rawQuery(query, null);
        int found = cursor.getCount();
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


    public void updateBoundaryId(String tempId, String aeroviewId){
        SQLiteDatabase db = SQLiteDatabaseHandler.this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BOUNDARY_ID, aeroviewId);
        db.update(TABLE_BOUNDARIES, values, KEY_BOUNDARY_ID + " = ?", new String[] {tempId});
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
