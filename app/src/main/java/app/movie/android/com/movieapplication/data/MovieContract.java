package app.movie.android.com.movieapplication.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by basma on 25/11/16.
 */

public class MovieContract {
    public static final String CONTENT_AUTHORITY = "app.movie.android.com.movieapplication";
    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // The possible PATH appeneded to BASE_CONTENT_URI for accessing the DB, by content provider.
    public static final String PATH_MOVIE = "movie";

    /* Inner class that defines the table contents of the movie table */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();
        // cursor that return more than one item
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        // cursor that return only a single item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        // Table name
        public static final String TABLE_NAME = "movie";
        // Column name for the movie_title
        public static final String MOVIE_TITLE = "movie_title";
        // Column name for the poster_path
        public static final String POSTER_PATH = "poster_path";
        // Column name for the release_date
        public static final String RELEASE_DATE = "release_date";
        // Column for the overview
        public static final String OVERVIEW = "overview";
        // Column for the rate
        public static final String RATE = "rate";

    }
}
