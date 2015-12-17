package jinyoung.com.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

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

import jinyoung.com.popularmovies.models.Movie;
import jinyoung.com.popularmovies.models.MovieAdapter;

public class MainActivityFragment extends Fragment {

    private ArrayAdapter<Movie> mMovieListAdapter;

    private static final String API_KEY = "api_key";
    private static final String SORT_BY = "sort_by";

    private ArrayList<Movie> movieList;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mMovieListAdapter = new MovieAdapter(getActivity(), movieList);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.movies_grid);
        gridView.setAdapter(mMovieListAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = mMovieListAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, movie);
                startActivity(intent);
            }
        });

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
            movieList = new ArrayList<Movie>();
        }
        else {
            movieList = savedInstanceState.getParcelableArrayList("movies");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh : {
                updateMovie();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("movies", movieList);
        super.onSaveInstanceState(outState);
    }

    private void updateMovie() {
        FetchMovieTask movieTask = new FetchMovieTask();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = sharedPref.getString(getString(R.string.pref_sort_criteria_key),
                getString(R.string.pref_sort_criteria_popular_key));

        movieTask.execute(sortBy);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(movieList == null || movieList.isEmpty()) {
            updateMovie();
        }
    }

    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            String sortBy = "popularity.desc";

            // we assume that first parameter is always sorting criteria
            if (params.length > 0) {
                sortBy = params[0];
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieStr = null;

            try {
                final String MOVEDB_BASE_URL = "https://api.themoviedb.org/3/discover/movie";

                Uri builtUri = Uri.parse(MOVEDB_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, BuildConfig.THEMOVDB_API_KEY)
                        .appendQueryParameter(SORT_BY, sortBy)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.d(LOG_TAG, "Start loading data from the movie db: " + url.toString());
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
                movieStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
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
            Log.d(LOG_TAG, "Successfully done data loading from the movie db");
            try {

                return parseMovieData(movieStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> result) {
            if (result != null) {
                movieList = result;
                mMovieListAdapter.clear();
                mMovieListAdapter.addAll(movieList);
            }
        }

        private ArrayList<Movie> parseMovieData(String movieJsonString) throws JSONException {
            JSONObject moviesJson = new JSONObject(movieJsonString);
            JSONArray moviesArray = moviesJson.getJSONArray("results");

            ArrayList<Movie> result = new ArrayList<>();
            for(int i = 0; i < moviesArray.length(); i++) {
                JSONObject movie = moviesArray.getJSONObject(i);
                Movie m = new Movie(movie.getInt("id"), movie.getString("title"),movie.getString("poster_path"));
                m.setReleaseDate(movie.getString("release_date"));
                m.setRate(movie.getString("vote_average"));
                m.setSynopsis(movie.getString("overview"));
                result.add(m);
            }
            return result;
        }
    }
}
