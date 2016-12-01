package app.movie.android.com.movieapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import app.movie.android.com.movieapplication.data.MovieContract;
import app.movie.android.com.movieapplication.data.MovieDbHelper;

/**
 * Created by basma on 25/11/16.
 */

public class DBLoadStore {
    private SQLiteDatabase db;
    private Context context;
    public DBLoadStore (Context context) {
        this.context = context;
    }
    public boolean movieExist(String movie_title) {
        db = new MovieDbHelper(context).getReadableDatabase();
        Cursor moviesCur = db.query(
                MovieContract.MovieEntry.TABLE_NAME,
                new String[] {"movie_title"}, // Select all columns
                null,
                null,
                null,
                null,
                null
        );
        if (moviesCur.moveToFirst()) {
            do {
                if (moviesCur.getString(moviesCur.getColumnIndex("movie_title")).
                        equals(movie_title)) {
                    return true;
                }

            } while(moviesCur.moveToNext());
        }
        return false;
    }
    // Updates all the movie details in the DB
    // Except for the title, as it's already the same.
    public void updateRecord(Movie newMovie) {
        String title = newMovie.getMovieOriginalTitle();
        double rate = newMovie.getVoteAverage();
        String posterPath = newMovie.getPosterPath();
        String releaseDate = newMovie.getReleaseDate();
        String customOverview = newMovie.getPlotSynopsis();
        //escape single quote in overview.
        if(customOverview.contains("'")) {
            customOverview = customOverview.replace("'", "''");
        }
        final String UPDATE_STATEMENT = "UPDATE " +
                MovieContract.MovieEntry.TABLE_NAME +" SET "+
                MovieContract.MovieEntry.POSTER_PATH+"='"+posterPath+"'"+", "+
                MovieContract.MovieEntry.OVERVIEW+"='"+customOverview +"'"+", "+
                MovieContract.MovieEntry.RELEASE_DATE+"='"+releaseDate+"'"+", "+
                MovieContract.MovieEntry.RATE+"="+rate+
                " WHERE "+ MovieContract.MovieEntry.MOVIE_TITLE+ "='" +title+"'"+
                ";" ;

        db.execSQL(UPDATE_STATEMENT);
    }

    public void insertMovie(Movie selectedMovie) {
        // If the movie exist, then update the record
        if(movieExist(selectedMovie.getMovieOriginalTitle())) {
            updateRecord(selectedMovie);
            return;
        }
        // get a writable db instance
        db = new MovieDbHelper(context).getWritableDatabase();

        String title = selectedMovie.getMovieOriginalTitle();
        double rate = selectedMovie.getVoteAverage();
        String posterPath = selectedMovie.getPosterPath();
        String releaseDate = selectedMovie.getReleaseDate();
        String overview = selectedMovie.getPlotSynopsis();
        //escape single quote in overview.
        if(overview.contains("'")) {
            overview = overview.replace("'", "''");
        }
        if(title.contains("'")) {
            title = title.replace("'", "''");
        }
        if(posterPath.contains("'")) {
            posterPath = posterPath.replace("'", "''");
        }
        if(releaseDate.contains("'")) {
            releaseDate = releaseDate.replace("'", "''");
        }
        final String INSERT_STATEMENT = "INSERT INTO "+
                MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry.MOVIE_TITLE + "," +
                MovieContract.MovieEntry.OVERVIEW + "," +
                MovieContract.MovieEntry.RATE + "," +
                MovieContract.MovieEntry.RELEASE_DATE + "," +
                MovieContract.MovieEntry.POSTER_PATH +
                ") " + "VALUES" + " (" +
                "'" +title+ "'" + "," + "'" +overview+ "'" + "," +
                rate + "," + "'" +releaseDate+ "'" + "," +
                "'" +posterPath+ "'" + ")"  + ";";
        db.execSQL(INSERT_STATEMENT);
    }

    public ArrayList<Movie> getMovies() {
        // get a readable db instance
        db = new MovieDbHelper(context).getReadableDatabase();
        ArrayList<Movie> favouruiteMovies = new ArrayList<>();
        Cursor moviesCur = db.query(
                MovieContract.MovieEntry.TABLE_NAME,
                null, // Select all columns
                null,
                null,
                null,
                null,
                null
        );
        if(moviesCur.moveToFirst()) {
            do {
                Movie m = new Movie();
                /* Set all the movie attributes */
                m.setVoteAverage(moviesCur.getDouble(
                        moviesCur.getColumnIndex("rate")));
                m.setMovieOriginalTitle(moviesCur.getString(
                        moviesCur.getColumnIndex("movie_title")));
                m.setReleaseDate(moviesCur.getString(
                        moviesCur.getColumnIndex("release_date")));
                m.setPosterPath(moviesCur.getString(
                        moviesCur.getColumnIndex("poster_path")));
                m.setPlotSynopsis(moviesCur.getString(
                        moviesCur.getColumnIndex("overview")));

                // Finally add movies to favourite list
                favouruiteMovies.add(m);

            } while(moviesCur.moveToNext());
        }

        if(moviesCur != null && !moviesCur.isClosed()) {
            moviesCur.close();
        }
        return favouruiteMovies;

    }
}
