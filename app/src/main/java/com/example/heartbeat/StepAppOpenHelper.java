package com.example.heartbeat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class StepAppOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "heartbeat";
    public static final String TABLE_NAME = "num_steps";
    public static final String KEY_ID = "id";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_DAY = "day";
    public static final String KEY_HOUR = "hour";
    public static final String CREATE_TABLE_SQL = "CREATE TABLE  " + TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY, " +
            KEY_DAY + " TEXT, " + KEY_HOUR + "  TEXT, " + KEY_TIMESTAMP + "  TEXT);";


    public static final String SONGS_TABLE_NAME = "songs";
    public static final String SONG_KEY_ID = "id";
    public static final String SONG_KEY_TITLE = "title";
    public static final String SONG_KEY_ARTIST = "artist";
    public static final String SONG_KEY_GENRE = "genre";
    public static final String SONG_KEY_BPM = "bpm";
    public static final String SONG_KEY_AUDIO_PATH = "audio_file_path";

    public static final String CREATE_SONGS_TABLE_SQL = "CREATE TABLE " + SONGS_TABLE_NAME + " ("
            + SONG_KEY_ID + " INTEGER PRIMARY KEY, "
            + SONG_KEY_TITLE + " TEXT, "
            + SONG_KEY_ARTIST + " TEXT, "
            + SONG_KEY_GENRE + " TEXT, "
            + SONG_KEY_BPM + " INTEGER, "
            + SONG_KEY_AUDIO_PATH + " TEXT);";

    public StepAppOpenHelper (Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

        // Load all records in the database
    public static void loadRecords(Context context){
        List<String> dates = new LinkedList<String>();
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        String [] columns = new String [] {StepAppOpenHelper.KEY_TIMESTAMP};
        Cursor cursor = database.query(StepAppOpenHelper.TABLE_NAME, columns, null, null, StepAppOpenHelper.KEY_TIMESTAMP,
                null, null );

        // iterate over returned elements
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            dates.add(cursor.getString(0));
            cursor.moveToNext();
        }
        database.close();

        Log.d("STORED TIMESTAMPS: ", String.valueOf(dates));
    }

    // load records from a single day
    public static Integer loadSingleRecord(Context context, String date){
        List<String> steps = new LinkedList<String>();
        // Get the readable database
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        String where = StepAppOpenHelper.KEY_DAY + " = ?";
        String [] whereArgs = { date };

        Cursor cursor = database.query(StepAppOpenHelper.TABLE_NAME, null, where, whereArgs, null,
                null, null );

        // iterate over returned elements
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            steps.add(cursor.getString(0));
            cursor.moveToNext();
        }
        database.close();

        Integer numSteps = steps.size();
        Log.d("STORED STEPS TODAY: ", String.valueOf(numSteps));
        return numSteps;
    }

    public static void deleteRecords (Context context) {
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        int numberDeletedRecords = 0;

        numberDeletedRecords = database.delete(StepAppOpenHelper.TABLE_NAME, null, null);
        database.close();

        Toast.makeText(context, "Deleted + "+ String.valueOf(numberDeletedRecords) + " steps", Toast.LENGTH_LONG).show();

    }



    public static Map<Integer, Integer> loadStepsByHour(Context context, String date){
        // 1. Define a map to store the hour and number of steps as key-value pairs
        Map<Integer, Integer>  map = new HashMap<>();

        // 2. Get the readable database
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        // 3. Define the query to get the data
        Cursor cursor = database.rawQuery("SELECT hour, COUNT(*)  FROM num_steps " +
                "WHERE day = ? GROUP BY hour ORDER BY  hour ASC ", new String [] {date});

        // 4. Iterate over returned elements on the cursor
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            Integer tmpKey = Integer.parseInt(cursor.getString(0));
            Integer tmpValue = Integer.parseInt(cursor.getString(1));

            //2. Put the data from the database into the map
            map.put(tmpKey, tmpValue);


            cursor.moveToNext();
        }

        // 5. Close the cursor and database
        cursor.close();
        database.close();

        // 6. Return the map with hours and number of steps
        return map;
    }


    public static Map<String, Integer> loadStepsByDay(Context context, String date){
        // 1. Define a map to store the day and number of steps as key-value pairs
        Map<String, Integer>  map = new TreeMap<>();

        // 2. Get the readable database
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        // 3. Define the query to get the data
        Cursor cursor = database.rawQuery("SELECT day, COUNT(*)  FROM num_steps " +
                "GROUP BY day ORDER BY  day ASC ", null);
        // 4. Iterate over returned elements on the cursor
        if (cursor.moveToFirst()) {
            do {
                String day = cursor.getString(0);
                int stepsCount = cursor.getInt(1);

                Log.d("STORED STEPS", day + ": " + stepsCount);

                // 5. Put the data from the database into the map
                map.put(day, stepsCount);
            } while (cursor.moveToNext());
        }

        // 5. Close the cursor and database
        cursor.close();
        database.close();

        // 6. Return the map with days and number of stepss
        return map;
    }

//    #############################################################################################################
    public void insertSong(String title, String artist, String genre, int bpm, String audioFilePath) {
        SQLiteDatabase database = this.getWritableDatabase();

        String insertQuery = "INSERT INTO " + SONGS_TABLE_NAME + " ("
                + SONG_KEY_TITLE + ", "
                + SONG_KEY_ARTIST + ", "
                + SONG_KEY_GENRE + ", "
                + SONG_KEY_BPM + ", "
                + SONG_KEY_AUDIO_PATH + ") VALUES (?, ?, ?, ?, ?)";
        database.execSQL(insertQuery, new Object[]{title, artist, genre, bpm, audioFilePath});

        database.close();
    }

    public void deleteSong(int songId) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(SONGS_TABLE_NAME, SONG_KEY_ID + " = ?", new String[]{String.valueOf(songId)});
        database.close();
    }

    public List<Map<String, String>> getAllSongs() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        List<Map<String, String>> songs = new LinkedList<>();

        try {
            database = this.getReadableDatabase();
            String query = "SELECT * FROM " + SONGS_TABLE_NAME;
            cursor = database.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    Map<String, String> song = new HashMap<>();
                    song.put("title", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_TITLE)));
                    song.put("artist", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_ARTIST)));
                    song.put("genre", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_GENRE)));
                    song.put("bpm", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_BPM)));
                    song.put("audioPath", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_AUDIO_PATH)));
                    songs.add(song);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (database != null && database.isOpen()) {
                database.close();
            }
        }

        return songs;
    }


    public List<Map<String, String>> getSongsByBpmRange(int minBpm, int maxBpm) {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        List<Map<String, String>> songs = new LinkedList<>();

        try {
            database = this.getReadableDatabase();

            // Define the query
            String query = "SELECT * FROM " + SONGS_TABLE_NAME + " WHERE " + SONG_KEY_BPM + " BETWEEN ? AND ?";
            cursor = database.rawQuery(query, new String[]{String.valueOf(minBpm), String.valueOf(maxBpm)});

            // Iterate through the results
            if (cursor.moveToFirst()) {
                do {
                    Map<String, String> song = new HashMap<>();
                    song.put("title", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_TITLE)));
                    song.put("artist", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_ARTIST)));
                    song.put("genre", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_GENRE)));
                    song.put("bpm", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_BPM)));
                    song.put("audioPath", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_AUDIO_PATH)));
                    songs.add(song);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Always close cursor and database to prevent memory leaks
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            if (database != null && database.isOpen()) {
                database.close();
            }
        }

        return songs;
    }


//    #############################################################################################################


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(CREATE_TABLE_SQL);

        sqLiteDatabase.execSQL(CREATE_SONGS_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
