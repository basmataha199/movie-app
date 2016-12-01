package app.movie.android.com.movieapplication.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by basma on 25/11/16.
 */

public class MovieDbHelper extends SQLiteOpenHelper {
    // CHANGES IF THE DATA BASE SCHEMA VERSION CHANGED.
    private static final int DATABASE_VERSION = 7;
    private static final String DB_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " +
                MovieContract.MovieEntry.TABLE_NAME + " (" +
                // Unique keys is generated in order,
                // as for favorite movies the user will want them *following*,
                // so the movie data should be sorted accordingly.
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // Define table columns names and data types
                MovieContract.MovieEntry.MOVIE_TITLE + " VARCHAR NOT NULL, " +
                MovieContract.MovieEntry.RATE + " REAL NOT NULL, " +
                MovieContract.MovieEntry.RELEASE_DATE + " VARCHAR NOT NULL, " +
                MovieContract.MovieEntry.OVERVIEW + " VARCHAR NOT NULL, " +
                MovieContract.MovieEntry.POSTER_PATH + " VARCHAR NOT NULL" +
                ");";
        Log.d("Details", SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
