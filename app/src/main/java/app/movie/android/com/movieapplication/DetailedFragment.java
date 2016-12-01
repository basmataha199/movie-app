package app.movie.android.com.movieapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
 * Created by basma on 01/12/16.
 */
public class DetailedFragment extends Fragment {
    private FetchMovieReview fetchReviews;
    private FetchMovieTrailers fetchVideos;
    private final String PATH_BASE_URI = "http://image.tmdb.org/t/p/";
    private final String THUMB_SIZE = "w342";
    private boolean noNetWork;
    public int selected_movie_id;
    private ListView reviewsList;
    private ListView videosList;

    private ReviewsAdapter reviewsAdapter;
    private VideosAdapter videosAdapter;
    public DetailedFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = getActivity().getIntent();
        Bundle args = getArguments();
        boolean favourite = false;

        if(intent != null && intent.hasExtra("Favourite")) {
            favourite = (boolean) intent.getSerializableExtra("Favourite");
        }
        else if(args != null) {
            favourite = (boolean) args.getSerializable("Favourite");
        }

        if(!favourite) {
            updateMovieReview();
            updateMovieVideos();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.fragment_detailed, container, false);

        // If, Detailed fragment is called via intent,
        // inspect the passed data from the caller class
        Intent intent = getActivity().getIntent();

        // Else if Detail Fragment is created using Fragment transaction
        //in 2-pane mode, then get the passed bundle arguments.
        Bundle args = getArguments();

        if(intent != null && intent.hasExtra("Selected Movie")) { // Normal mobile
            final Movie selectedMovie = (Movie) intent.getSerializableExtra("Selected Movie");
            triggerDetails(rootView, selectedMovie);
        }
        else if(args != null){ // Ttablet, 2-Pane mode.
            final Movie selectedMovie = (Movie) args.getSerializable(Movie.MOVIE_TAG);
            triggerDetails(rootView, selectedMovie);
        }
        return rootView;
    }
    public void triggerDetails(View rootView, final Movie selectedMovie) {
        selected_movie_id = selectedMovie.getId();
        Log.d("Details","*****In Details*******Movie Title" +
                selectedMovie.getMovieOriginalTitle()+"\n");
        /**********************************************************************/

        addDetails(selectedMovie, rootView);

        if(noNetWork) { // Check network availability
            Toast.makeText(getActivity(), "No network available, to get reviews!",
                    Toast.LENGTH_LONG).show();
        }

        ImageButton favorite = (ImageButton) rootView.findViewById(R.id.fav_but);
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Insert into DB
                DBLoadStore db_insert = new DBLoadStore(getActivity());
                db_insert.insertMovie(selectedMovie);

                // Achknowledge assertion to user
                Toast.makeText(getActivity(), "Movie saved successfully.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    /*For checking if server is online*/
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().
                        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    public void addDetails(Movie selectedMovie, View rootView) {
        /** Add Selected movie title view **/
        TextView title = (TextView)  rootView.findViewById(R.id.movie_title);
        title.setText(selectedMovie.getMovieOriginalTitle());
        /** Add Selected movie poster view **/
        ImageView posterView = (ImageView) rootView.findViewById(R.id.poster);
        Picasso.with(getActivity()).load(
                PATH_BASE_URI + THUMB_SIZE + "/" +
                        selectedMovie.getPosterPath()).
                into(posterView);
                /* Now add the details required */
        TextView overview = (TextView)  rootView.findViewById(R.id.overview);
        overview.setText(selectedMovie.getPlotSynopsis());
        TextView rate = (TextView)  rootView.findViewById(R.id.rate);
        rate.setText(rate.getText() + String.valueOf(selectedMovie.getVoteAverage()));
        TextView release_date = (TextView)  rootView.findViewById(R.id.release_date);
        release_date.setText(release_date.getText() + selectedMovie.getReleaseDate());

        /******* Now add the reviews and trailers *******/
        reviewsList = (ListView) rootView.findViewById(R.id.reviews_list);
        videosList = (ListView) rootView.findViewById(R.id.trailers_list);
    }

    public void updateMovieReview() {
        if(!isOnline()) {
            noNetWork = true;
            return; // Don't fetch form web.
        }
        fetchReviews = new FetchMovieReview();
        fetchReviews.execute(String.valueOf(selected_movie_id));
    }

    public void updateMovieVideos() {
        if(!isOnline()) {
            noNetWork = true;
            return; // Don't fetch form web.
        }
        fetchVideos = new FetchMovieTrailers();
        fetchVideos.execute(String.valueOf(selected_movie_id));
    }

    public class FetchMovieReview extends AsyncTask<String, String, ArrayList<Review>> {
        /* Constant Strings to add to the URL*/
        private final String LOG_TAG = DetailedFragment.class.getSimpleName();
        private String MOVIE_BASE_URL =
                "https://api.themoviedb.org/3/movie/";
        private final String APIKEY_PARAM = "api_key";
        private final String REVIEWS = "reviews";
        private final String MY_API_KEY = "fd9ddd12e5059662b34bf5b39867fb5a";
        @Override
        protected void onPostExecute(ArrayList<Review> reviews) {
            if(reviews != null) {
                reviewsAdapter = new ReviewsAdapter(getActivity(),
                        reviews);
                // populate the listView
                reviewsList.setAdapter(reviewsAdapter);
            } else {
                Log.d("Details", "Empty review*****");
            }
        }

        @Override
        protected ArrayList<Review> doInBackground(String... params) {
            int movie_id = Integer.parseInt(params[0]);

            MOVIE_BASE_URL += movie_id +"/"+ REVIEWS + "?";

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
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
                return getMovieReviewFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSon Error", e);
            }
            return null;
        }

        private ArrayList<Review> getMovieReviewFromJson(String movieJsonStr)
                throws JSONException{
            // Extract the review results
            final String RESULTED_MOVIE_LIST = "results";
            final String AUTHOR = "author";
            final String CONTENT = "content";
            /**
             * Now we make a JSONObject from the fetched data
             * The JSONArray Contains the resulted movies as a list
             */
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray moviesArray = movieJson.getJSONArray(RESULTED_MOVIE_LIST);

            ArrayList<Review> result = new ArrayList<>();

            for(int i = 0; i < moviesArray.length(); ++i) {
                // get JSONObject representing the current movie being parsed
                JSONObject movieObject = moviesArray.getJSONObject(i);
                result.add(new Review(movieObject.getString(AUTHOR),
                        movieObject.getString(CONTENT)));
            }

            return result;
        }
    }

    public class FetchMovieTrailers extends AsyncTask<String, String, ArrayList<Video>> {
        /* Constant Strings to add to the URL*/
        private final String LOG_TAG = DetailedFragment.class.getSimpleName();
        private String MOVIE_BASE_URL =
                "https://api.themoviedb.org/3/movie/";
        private final String APIKEY_PARAM = "api_key";
        private final String VIDEOS = "videos";
        private final String MY_API_KEY = "fd9ddd12e5059662b34bf5b39867fb5a";
        @Override
        protected void onPostExecute(ArrayList<Video> videos) {
            if(videos != null) {
                videosAdapter = new VideosAdapter(getActivity(), videos);
                videosList.setAdapter(videosAdapter);
            } else {
                Log.d("Details", "Empty video*****");
            }
        }

        @Override
        protected ArrayList<Video> doInBackground(String... params) {
            int movie_id = Integer.parseInt(params[0]);
            MOVIE_BASE_URL += movie_id +"/"+ VIDEOS + "?";

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
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
                return getMovieReviewFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSon Error", e);
            }
            return null;
        }

        private ArrayList<Video> getMovieReviewFromJson(String movieJsonStr)
                throws JSONException{
            // Extract the review results
            final String RESULTED_MOVIE_LIST = "results";
            final String KEY = "key";
            final String NAME = "name";
            /**
             * Now we make a JSONObject from the fetched data
             * The JSONArray Contains the resulted movies as a list
             */
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray moviesArray = movieJson.getJSONArray(RESULTED_MOVIE_LIST);

            ArrayList<Video> result = new ArrayList<>();

            for(int i = 0; i < moviesArray.length(); ++i) {
                // get JSONObject representing the current movie being parsed
                JSONObject movieObject = moviesArray.getJSONObject(i);
                result.add(new Video(movieObject.getString(NAME),movieObject.getString(KEY)));
            }

            return result;
        }
    }
}