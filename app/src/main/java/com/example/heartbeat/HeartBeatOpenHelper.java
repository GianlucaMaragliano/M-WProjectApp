package com.example.heartbeat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class HeartBeatOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 14;
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
            + SONG_KEY_ID + " TEXT PRIMARY KEY, "
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
    public static final String WORKOUT_KEY_SONG_ID = "songId";
    public static final String WORKOUT_KEY_AVG_HEART_RATE = "avgHeartRate";
    public static final String WORKOUT_KEY_SONG_DISTANCE = "songRunDistance";

    public static final String CREATE_WORKOUT_HISTORY_TABLE_SQL = "CREATE TABLE " + WORKOUT_HISTORY_TABLE_NAME + " ("
            + WORKOUT_KEY_ID + " TEXT, "
            + WORKOUT_KEY_DATE + " TEXT, "
            + WORKOUT_KEY_TIMESTAMP + " TEXT, "
            + WORKOUT_KEY_SONG_ID + " TEXT, "
            + WORKOUT_KEY_AVG_HEART_RATE +  " TEXT, "
            + WORKOUT_KEY_SONG_DISTANCE + " INTEGER);";

    public HeartBeatOpenHelper(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
//    #############################################################################################################
    public void insertSong(String title, String artist, String genre, int bpm, String audioFilePath) {
        // Generate a unique ID for the song
        String songId = UUID.randomUUID().toString();

        SQLiteDatabase database = this.getWritableDatabase();

        String insertQuery = "INSERT INTO " + SONGS_TABLE_NAME + " ("
                + SONG_KEY_ID + ", "
                + SONG_KEY_TITLE + ", "
                + SONG_KEY_ARTIST + ", "
                + SONG_KEY_GENRE + ", "
                + SONG_KEY_BPM + ", "
                + SONG_KEY_AUDIO_PATH + ") VALUES (?, ?, ?, ?, ?, ?)";
        database.execSQL(insertQuery, new Object[]{songId, title, artist, genre, bpm, audioFilePath});

        database.close();
    }

    // Method for inserting a song into workout history
    public void insertWorkoutSong(String workoutId, String timestamp, String date, String songId, double avgBpm, double runDistance) {
        SQLiteDatabase database = this.getWritableDatabase();

        String insertQuery = "INSERT INTO " + WORKOUT_HISTORY_TABLE_NAME + " ("
                + WORKOUT_KEY_ID + ", "
                + WORKOUT_KEY_DATE + ", "
                + WORKOUT_KEY_TIMESTAMP + ", "
                + WORKOUT_KEY_SONG_ID + ", "
                + WORKOUT_KEY_AVG_HEART_RATE + ", "
                + WORKOUT_KEY_SONG_DISTANCE + ") VALUES (?, ?, ?, ?, ?, ?)";
        database.execSQL(insertQuery, new Object[]{workoutId, date, timestamp, songId, avgBpm, runDistance});
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
                row.put("songId", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_SONG_ID)));
                row.put("avgHeartRate", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_AVG_HEART_RATE)));
                row.put("distance", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_SONG_DISTANCE)));
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
                row.put("songId", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_SONG_ID)));
                row.put("avgHeartRate", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_AVG_HEART_RATE)));
                row.put("distance", cursor.getString(cursor.getColumnIndexOrThrow(WORKOUT_KEY_SONG_DISTANCE)));
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
                    song.put("id", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_ID)));
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
                    song.put("id", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_ID)));
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

    public Map<String, String> getSongById(String songId) {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        Map<String, String> song = new HashMap<>();

        try {
            database = this.getReadableDatabase();
            String query = "SELECT * FROM " + SONGS_TABLE_NAME + " WHERE " + SONG_KEY_ID + " = ?";
            cursor = database.rawQuery(query, new String[]{songId});

            if (cursor.moveToFirst()) {
                song.put("id", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_ID)));
                song.put("title", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_TITLE)));
                song.put("artist", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_ARTIST)));
                song.put("genre", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_GENRE)));
                song.put("bpm", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_BPM)));
                song.put("audioPath", cursor.getString(cursor.getColumnIndexOrThrow(SONG_KEY_AUDIO_PATH)));
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
        return song;
    }

    // Based on a date range returns all songs played, sorted by the number of times they were played
    public List<Map<String, String>> getMostPlayedSongs(String startDate, String endDate) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT songId, COUNT(songId) as count FROM history_workout WHERE date BETWEEN ? AND ? GROUP BY songId ORDER BY count DESC", new String[]{startDate, endDate});

        List<Map<String, String>> mostPlayedSongs = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Map<String, String> song = new HashMap<>();
                song.put("songId", cursor.getString(cursor.getColumnIndexOrThrow("songId")));
                song.put("count", cursor.getString(cursor.getColumnIndexOrThrow("count")));
                mostPlayedSongs.add(song);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        Log.d("DBQuery", "Most played songs: " + mostPlayedSongs);
        return mostPlayedSongs;
    }



    // Based on the data range returns the total distance run in the week
    public String getWeekTotalDistance(String startDate, String endDate) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT SUM(songRunDistance) as totalDistance FROM history_workout WHERE date BETWEEN ? AND ?", new String[]{startDate, endDate});

        String totalDistance = null;
        if (cursor.moveToFirst()) {
            totalDistance = cursor.getString(cursor.getColumnIndexOrThrow("totalDistance"));
        }
        cursor.close();
        Log.d("DBQuery", "Total distance run in the week: " + totalDistance);
        if (totalDistance == null) {
            return "0";
        }
        return totalDistance;
    }

    // Based on the data range returns the average heart rate in the week
    public String getWeekAvgHeartRate(String startDate, String endDate) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT AVG(avgHeartRate) as avgHeartRate FROM history_workout WHERE date BETWEEN ? AND ?", new String[]{startDate, endDate});

        String avgHeartRate = null;
        if (cursor.moveToFirst()) {
            avgHeartRate = cursor.getString(cursor.getColumnIndexOrThrow("avgHeartRate"));
        }
        Log.d("DBQuery", "Average heart rate in the week: " + avgHeartRate);
        cursor.close();
        if (avgHeartRate == null) {
            return "0";
        }
        return avgHeartRate;
    }

    // Based on the timestamps in a date range return the most usual range time where workouts were done (e.g. 9:00 - 11:00)
    public String getMostUsualTimeRange(String startDate, String endDate) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT strftime('%H', timestamp) as hour, COUNT(strftime('%H', timestamp)) as count FROM history_workout WHERE date BETWEEN ? AND ? GROUP BY hour ORDER BY count DESC LIMIT 1", new String[]{startDate, endDate});

        String mostUsualTime = null;
        if (cursor.moveToFirst()) {
            mostUsualTime = cursor.getString(cursor.getColumnIndexOrThrow("hour")) + ":00 - " + cursor.getString(cursor.getColumnIndexOrThrow("hour")) + ":59";
        }
        Log.d("DBQuery", "Most usual workout time: " + mostUsualTime);
        cursor.close();
        if (mostUsualTime == null) {
            return "No Workouts";
        }
        return mostUsualTime;
    }

    // Based on the data range return the total number of hours, hours and minutes, or minutes of workout
    public String getTotalWorkoutTime(String startDate, String endDate) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT SUM(strftime('%s', timestamp) - strftime('%s', '00:00:00')) as totalSeconds FROM history_workout WHERE date BETWEEN ? AND ?", new String[]{startDate, endDate});

        String totalSeconds = null;
        if (cursor.moveToFirst()) {
            totalSeconds = cursor.getString(cursor.getColumnIndexOrThrow("totalSeconds"));
        }
        Log.d("DBQuery", "Total workout time: " + totalSeconds);
        cursor.close();
        if (totalSeconds == null) {
            return "0";
        }
        database.close();
        return totalSeconds;
    }

    // Based on date range return workouts ids  and dates of the week
    public List<Pair<String, String>> getWorkoutIdsAndDatesByDateRange(String startDate, String endDate) {
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(
                "SELECT DISTINCT id, date FROM history_workout WHERE date BETWEEN ? AND ?",
                new String[]{startDate, endDate}
        );

        List<Pair<String, String>> workoutData = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                workoutData.add(new Pair<>(id, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return workoutData;
    }


//    #############################################################################################################


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(CREATE_SONGS_TABLE_SQL);

        sqLiteDatabase.execSQL(CREATE_WORKOUT_HISTORY_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SONGS_TABLE_NAME);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WORKOUT_HISTORY_TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}
