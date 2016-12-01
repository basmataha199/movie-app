package app.movie.android.com.movieapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment  {

    private ImageAdapter moviesAdapter;
    private FetchMoviesData fetchMovies;
    private GridView moviesView;
    private boolean noNetWork, favourite;
    public MainActivityFragment() {
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(int position, ImageAdapter moviesAdapter, boolean isFav);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        if(noNetWork) { // means no network available
            Toast.makeText(getActivity(), "No network available!",
                    Toast.LENGTH_LONG).show();
            return rootView;
        }
        moviesView = (GridView) rootView.findViewById(R.id.gridview_movie);
        moviesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ((Callback) getActivity()).
                        onItemSelected(position, moviesAdapter, favourite);
            }
        });

        return rootView;
    }

    private void updateMovies() {
        fetchMovies = new FetchMoviesData();
        SharedPreferences prefs = PreferenceManager.
                getDefaultSharedPreferences(getActivity());
        String sort_option = prefs.getString(
                getString(R.string.pref_sort_key),
                getString(R.string.pref_most_popular) //most popular is the default sorting
        );
        if(sort_option.equals("favourite")) {
            favourite = true;
            DBLoadStore getMovies = new DBLoadStore(getActivity());
            ArrayList<Movie> favouriteMovies = getMovies.getMovies();
            moviesAdapter = new ImageAdapter(getActivity(), favouriteMovies);
            moviesView.setAdapter(moviesAdapter);
        }
        else {
            favourite = false;
            if(!isOnline()) {
                noNetWork = true;
                return; // Don't fetch form web.
            }
            fetchMovies.execute(sort_option);
        }
    }
    /*For checking if server is online*/
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().
                        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    public class FetchMoviesData extends AsyncTask<String, String, ArrayList<Movie>> {
        /* Constant Strings to add to the URL*/
        private final String LOG_TAG = FetchMoviesData.class.getSimpleName();
        private String MOVIE_BASE_URL =
                "http://api.themoviedb.org/3/movie/";
        private final String APIKEY_PARAM = "api_key";

        private final String MY_API_KEY = "fd9ddd12e5059662b34bf5b39867fb5a";

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            if(movies != null ) {
                moviesAdapter = new ImageAdapter(getActivity(), movies);
                moviesView.setAdapter(moviesAdapter);
            }
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                Uri builtUri;
                if(params[0].equals("popularity")){
                    MOVIE_BASE_URL += "popular?";
                } else {
                    MOVIE_BASE_URL += "top_rated?";
                }
                builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                            .appendQueryParameter(APIKEY_PARAM, MY_API_KEY)
                            .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movies data,
                // there's no point in attempting to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSon Error", e);
            }
            return null;
        }

        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr)
            throws JSONException{
            // These are the names of the JSON objects that need to be extracted.
            final String RESULTED_MOVIE_LIST = "results";
            final String POSTER_PATH = "poster_path";
            final String VOTE_AVERAGE = "vote_average";
            final String PLOT_SYNOPSIS = "overview";
            final String RELEASE_DATE = "release_date";
            final String ORIGINAL_TITLE = "original_title";
            final String MOVIE_ID = "id";
            /**
             * Now we make a JSONObject from the fetched data
             * The JSONArray Contains the resulted movies as a list
             */
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray moviesArray = movieJson.getJSONArray(RESULTED_MOVIE_LIST);

            ArrayList<Movie> result = new ArrayList<>();

            for(int i = 0; i < moviesArray.length(); ++i) {
                // get JSONObject representing the current movie being parsed
                JSONObject movieObject = moviesArray.getJSONObject(i);
                Movie currentMovie = new Movie();

                currentMovie.setMovieOriginalTitle(movieObject.
                        getString(ORIGINAL_TITLE));
                currentMovie.setPlotSynopsis(movieObject.
                        getString(PLOT_SYNOPSIS));
                currentMovie.setPosterPath(movieObject.
                        getString(POSTER_PATH));
                currentMovie.setReleaseDate(movieObject.
                        getString(RELEASE_DATE));
                currentMovie.setVoteAverage(movieObject.
                        getDouble(VOTE_AVERAGE));
                currentMovie.setId(movieObject.
                        getInt(MOVIE_ID));

                result.add(currentMovie);
            }
            return result;
        }

    }
}
