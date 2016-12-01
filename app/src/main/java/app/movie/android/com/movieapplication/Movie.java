package app.movie.android.com.movieapplication;

import java.io.Serializable;

/**
 * Created by basma on 21/10/16.
 */

public class Movie implements Serializable{
    private String posterPath; // named backdrop_path in the api
    private double voteAverage; // User rating
    private String plotSynopsis; // Overview of the movie
    private String releaseDate;
    private String movieOriginalTitle; //original_title of the movie.
    private int id;
    private static final long serialVersionUID = -2163051469151804394L;
    public static final String MOVIE_TAG = "MOVIE_TAG";
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public void setPlotSynopsis(String plotSynopsis) {
        this.plotSynopsis = plotSynopsis;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setMovieOriginalTitle(String movieOriginalTitle) {
        this.movieOriginalTitle = movieOriginalTitle;
    }
    public String getPosterPath() {
        return posterPath;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getMovieOriginalTitle() {
        return movieOriginalTitle;
    }
}
