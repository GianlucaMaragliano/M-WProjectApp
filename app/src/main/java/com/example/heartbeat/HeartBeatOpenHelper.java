package com.example.heartbeat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class HeartBeatOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_NAME = "heartbeat";

    public static final String SONGS_TABLE_NAME = "songs";
    public static final String SONG_KEY_ID = "id";
    public static final String SONG_KEY_TITLE = "title";
    public static final String SONG_KEY_ARTIST = "artist";
    public static final String SONG_KEY_GENRE = "genre";
    public static final String SONG_KEY_BPM = "bpm";
    public static final String SONG_KEY_AUDIO_PATH = "audio_file_path";
    public static final String SONG_KEY_DURATION = "duration";


    public static final String CREATE_SONGS_TABLE_SQL = "CREATE TABLE " + SONGS_TABLE_NAME + " ("
            + SONG_KEY_ID + " INTEGER PRIMARY KEY, "
            + SONG_KEY_TITLE + " TEXT, "
            + SONG_KEY_ARTIST + " TEXT, "
            + SONG_KEY_GENRE + " TEXT, "
            + SONG_KEY_BPM + " INTEGER, "
            + SONG_KEY_AUDIO_PATH + " TEXT,"
            + SONG_KEY_DURATION + " INTEGER);";

    // The new table for workout history has to contain different dates and based on the date contains a number of songs
    public static final String WORKOUT_HISTORY_TABLE_NAME = "history_workout";
    public static final String WORKOUT_KEY_ID = "id";
    public static final String WORKOUT_KEY_DATE = "date";
    public static final String WORKOUT_KEY_TIMESTAMP = "timestamp";
    public static final String WORKOUT_KEY_SONG_TITLE = "songTitle";
    public static final String WORKOUT_KEY_SONG_ARTIST = "songArtist";
    public static final String WORKOUT_KEY_SONG_BPM = "songBpm";

    public static final String CREATE_WORKOUT_HISTORY_TABLE_SQL = "CREATE TABLE " + WORKOUT_HISTORY_TABLE_NAME + " ("
            + WORKOUT_KEY_ID + " TEXT, "
            + WORKOUT_KEY_DATE + " TEXT, "
            + WORKOUT_KEY_TIMESTAMP + " TEXT, "
            + WORKOUT_KEY_SONG_TITLE + " TEXT, "
            + WORKOUT_KEY_SONG_ARTIST + " TEXT, "
            + WORKOUT_KEY_SONG_BPM + " INTEGER);";

    public static final String WORKOUT_EXERCISES_TABLE_NAME = "workout_exercises";
    public static final String EXERCISE_KEY_ID = "id";
    public static final String EXERCISE_KEY_WORKOUT_ID = "workoutId";
    public static final String EXERCISE_KEY_SONG_ID = "songId";
    public static final String EXERCISE_KEY_EXERCISE_NAME = "exerciseName";
    public static final String EXERCISE_KEY_SET_COUNT = "setCount";
    public static final String EXERCISE_KEY_REP_COUNT = "repCount";

    public static final String CREATE_WORKOUT_EXERCISES_TABLE_SQL = "CREATE TABLE " + WORKOUT_EXERCISES_TABLE_NAME + " ("
            + EXERCISE_KEY_ID + " INTEGER PRIMARY KEY, "
            + EXERCISE_KEY_WORKOUT_ID + " TEXT, "
            + EXERCISE_KEY_SONG_ID + " INTEGER, "
            + EXERCISE_KEY_EXERCISE_NAME + " TEXT, "
            + EXERCISE_KEY_SET_COUNT + " INTEGER, "
            + EXERCISE_KEY_REP_COUNT + " INTEGER, "
            + "FOREIGN KEY (" + EXERCISE_KEY_SONG_ID + ") REFERENCES " + SONGS_TABLE_NAME + "(" + SONG_KEY_ID + "));";



    public HeartBeatOpenHelper(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
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

    // Method for inserting a song into workout history
    public void insertWorkoutSong(String workoutId, String timestamp, String date, String songTitle, String songArtist, double avgBpm) {
        SQLiteDatabase database = this.getWritableDatabase();

        String insertQuery = "INSERT INTO " + WORKOUT_HISTORY_TABLE_NAME + " ("
                + WORKOUT_KEY_ID + ", "
                + WORKOUT_KEY_DATE + ", "
                + WORKOUT_KEY_TIMESTAMP + ", "
                + WORKOUT_KEY_SONG_TITLE + ", "
                + WORKOUT_KEY_SONG_ARTIST + ", "
                + WORKOUT_KEY_SONG_BPM + ") VALUES (?, ?, ?, ?, ?, ?)";
        database.execSQL(insertQuery, new Object[]{workoutId, date, timestamp, songTitle, songArtist, avgBpm});
        database.close();
    }

    public List<Map<String, String>> getWorkoutSongsByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM history_workout WHERE date = ?", new String[]{date});
        Log.d("DBQuery", "Row count: " + cursor.getCount());

        List<Map<String, String>> result = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Map<String, String> row = new HashMap<>();
                row.put("id", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_ID)));
                row.put("timestamp", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_TIMESTAMP)));
                row.put("title", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_SONG_TITLE)));
                row.put("artist", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_SONG_ARTIST)));
                row.put("bpm", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_SONG_BPM)));
                result.add(row);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public List<Map<String, String>> getWorkoutSongsByWorkoutId(String workoutId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM history_workout WHERE id = ?", new String[]{workoutId});
        Log.d("DBQuery", "Row count: " + cursor.getCount());

        List<Map<String, String>> result = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Map<String, String> row = new HashMap<>();
                row.put("timestamp", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_TIMESTAMP)));
                row.put("title", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_SONG_TITLE)));
                row.put("artist", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_SONG_ARTIST)));
                row.put("bpm", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_SONG_BPM)));
                result.add(row);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public void deleteWorkoutHistory() {
        // clean the workout history table
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(WORKOUT_HISTORY_TABLE_NAME, null, null);
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

    public void insertWorkoutExercise(String workoutId, String songId, String exerciseName, int setCount, int repCount) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EXERCISE_KEY_WORKOUT_ID, workoutId);
        values.put(EXERCISE_KEY_SONG_ID, songId);
        values.put(EXERCISE_KEY_EXERCISE_NAME, exerciseName);
        values.put(EXERCISE_KEY_SET_COUNT, setCount);
        values.put(EXERCISE_KEY_REP_COUNT, repCount);

        long result = database.insert(WORKOUT_EXERCISES_TABLE_NAME, null, values);
        if (result == -1) {
            Log.e("HeartBeatOpenHelper", "Failed to insert exercise");
        } else {
            Log.d("HeartBeatOpenHelper", "Exercise inserted successfully with ID: " + result);
        }
    }

    public List<Map<String, String>> getExercisesByWorkoutId(String workoutId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + WORKOUT_EXERCISES_TABLE_NAME + " WHERE " + EXERCISE_KEY_WORKOUT_ID + " = ?", new String[]{workoutId});

        List<Map<String, String>> result = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Map<String, String> row = new HashMap<>();
                row.put("songId", cursor.getString(cursor.getColumnIndexOrThrow(EXERCISE_KEY_SONG_ID)));
                row.put("exerciseName", cursor.getString(cursor.getColumnIndexOrThrow(EXERCISE_KEY_EXERCISE_NAME)));
                row.put("setCount", cursor.getString(cursor.getColumnIndexOrThrow(EXERCISE_KEY_SET_COUNT)));
                row.put("repCount", cursor.getString(cursor.getColumnIndexOrThrow(EXERCISE_KEY_REP_COUNT)));
                result.add(row);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }




//    #############################################################################################################


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(CREATE_SONGS_TABLE_SQL);

        sqLiteDatabase.execSQL(CREATE_WORKOUT_HISTORY_TABLE_SQL);

        sqLiteDatabase.execSQL(CREATE_WORKOUT_EXERCISES_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SONGS_TABLE_NAME);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WORKOUT_HISTORY_TABLE_NAME);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WORKOUT_EXERCISES_TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}
