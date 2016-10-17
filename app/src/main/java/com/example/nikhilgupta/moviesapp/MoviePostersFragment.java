package com.example.nikhilgupta.moviesapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import java.util.Arrays;

/**
 * Created by Nikhil Gupta on 25-09-2016.
 */
public class MoviePostersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    OnMovieSelectedListener MovieListener;
    private int mPosition;
    private static final int FAVORITE_LOADER = 0;
    private static final String SELECTED_KEY = "selected_key";

    private static final String[] FAVORITE_COLUMNS = {
            FavoritesContract.FavoritesEntry._ID,
            FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID,
            FavoritesContract.FavoritesEntry.COLUMN_ADDED_ON
    };

    // These indices are tied to FAVORITE_COLUMNS. If FAVORITE_COLUMNS changes, these
    // must change.
    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_ADDED_ON = 2;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Uri favoritesUri = FavoritesContract.FavoritesEntry.CONTENT_URI;
        return new android.support.v4.content.CursorLoader(
                getActivity(),
                favoritesUri,
                FAVORITE_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        String favorite_movie_id[] = new String[cursor.getCount()];
        int index = 0;
        if (cursor.getCount() != 0) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    favorite_movie_id[index] = cursor.getString(COL_MOVIE_ID);
                    cursor.moveToNext();
                    index++;
                }
            }
        }
        //cursor.close();
        new FetchFavoritesTask().execute(favorite_movie_id,
                new String[]{getString(R.string.api_base_url)},
                new String[]{getString(R.string.api_key)});
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    // Container Activity must implement this interface
    public interface OnMovieSelectedListener {
        public void onMovieSelected(Movie[] movies, int i);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MovieListener = (OnMovieSelectedListener) context;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String pref = sharedPref.getString(getString(R.string.sort_order_key), getString(R.string.sort_order_value_mostpopular));

        if (!isOnline()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(R.string.internet_error_dialog_message)
                    .setTitle(R.string.internet_error_dialog_title);
            builder.setPositiveButton(R.string.internet_error_dialog_refresh, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    getActivity().recreate();
                }
            });
            builder.setNegativeButton(R.string.internet_error_dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        View view = inflater.inflate(R.layout.movie_posters_fragment, container, false);

        if (!pref.equals(getString(R.string.sort_order_value_favorites))) {
            new FetchMoviesTask().execute(pref, getString(R.string.api_base_url), getString(R.string.api_key));
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String pref = sharedPref.getString(getString(R.string.sort_order_key), getString(R.string.sort_order_value_mostpopular));

        if (pref.equals(getString(R.string.sort_order_value_favorites))) {
            getLoaderManager().initLoader(FAVORITE_LOADER, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMessage(getString(R.string.loading_message));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(final Movie[] movies) {
            Movie[] fetchedMovies = movies;
            if (movies != null) {
                GridView gridview = (GridView) getView().findViewById(R.id.gridView);
                gridview.setAdapter(new GridViewAdapter(getActivity(), R.id.gridView, new ArrayList<Movie>(Arrays.asList(fetchedMovies))));
                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v,
                                            int position, long id) {
                        MovieListener.onMovieSelected(movies, position);
                        mPosition = position;
                    }
                });
            }
            GridView gridView = (GridView) (getActivity().findViewById(R.id.gridView));
            gridView.smoothScrollToPosition(mPosition);
            mProgressDialog.dismiss();
        }

        @Override
        protected Movie[] doInBackground(String... params) {
            return getInfo(params[0], params[1], params[2]);
        }

        Movie[] getInfo(String pref, String base, String key) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                String base_url = base + pref + "?";
                Uri builtUri = Uri.parse(base_url).buildUpon().appendQueryParameter("api_key", key).build();

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
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                return getDataFromJson(buffer.toString());
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } catch (JSONException e) {
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
        }


        private Movie[] getDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MOVIES = "results";
            final String OVERVIEW = "overview";
            final String VOTE_AVERAGE = "vote_average";
            final String RELEASE_DATE = "release_date";
            final String POSTER_PATH = "poster_path";
            final String TITLE = "original_title";
            final String MOVIE_ID = "id";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray moviesArray = movieJson.getJSONArray(MOVIES);

            Movie[] results = new Movie[moviesArray.length()];

            for (int i = 0; i < results.length; i++) {
                // Get the JSON object representing the day
                JSONObject movie = moviesArray.getJSONObject(i);

                String overview = movie.getString(OVERVIEW);
                String release_Date = movie.getString(RELEASE_DATE);
                String title = movie.getString(TITLE);
                String poster_path = movie.getString(POSTER_PATH);
                double vote_average = movie.getDouble(VOTE_AVERAGE);
                int movie_id = movie.getInt(MOVIE_ID);

                results[i] = new Movie(title, overview, poster_path, release_Date, vote_average, movie_id);
            }
            return results;
        }
    }

    class FetchFavoritesTask extends AsyncTask<String[], Void, Movie[]> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMessage(getString(R.string.loading_message));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(final Movie[] movies) {
            ArrayList<Movie> fetchedMovies = new ArrayList<>();
            for (int j = 0; j < movies.length; j++) {
                if (movies[j] != null) {
                    fetchedMovies.add(movies[j]);
                }
            }

            if (movies != null) {
                GridView gridview = null;
                // FetchFavoritesTask may be called from OnLoadFinished before the view is populated
                // in onCreateView. In such cases, keep on looping until the view is populated.
                while (gridview == null) {
                    gridview = (GridView) getView().findViewById(R.id.gridView);
                }
                gridview.setAdapter(new GridViewAdapter(getActivity(),
                        R.id.gridView,
                        fetchedMovies));
                Movie[] fetchedMoviesArray = new Movie[fetchedMovies.size()];
                fetchedMoviesArray = fetchedMovies.toArray(fetchedMoviesArray);
                final Movie[] finalFetchedMoviesArray = fetchedMoviesArray;
                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v,
                                            int position, long id) {
                        MovieListener.onMovieSelected(finalFetchedMoviesArray, position);
                    }
                });
            }
            mProgressDialog.dismiss();
        }

        @Override
        protected Movie[] doInBackground(String[]... params) {
            return getInfo(params[0], params[1][0], params[2][0]);
        }

        Movie[] getInfo(String favorite_movie_id[], String base, String key) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            Movie[] favoriteMovies = new Movie[favorite_movie_id.length];
            for (int k = 0; k < favorite_movie_id.length; k++) {
                try {

                    String base_url = base + favorite_movie_id[k] + "?";
                    Uri builtUri = Uri.parse(base_url).buildUpon().appendQueryParameter("api_key", key).build();

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
                        continue;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        continue;
                    }

                    favoriteMovies[k] = getDataFromJson(buffer.toString());
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error ", e);
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
            }
            return favoriteMovies;
        }

        private Movie getDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OVERVIEW = "overview";
            final String VOTE_AVERAGE = "vote_average";
            final String RELEASE_DATE = "release_date";
            final String POSTER_PATH = "poster_path";
            final String TITLE = "original_title";
            final String MOVIE_ID = "id";

            // Get the JSON object representing the movie
            JSONObject movie = new JSONObject(movieJsonStr);

            String overview = movie.getString(OVERVIEW);
            String release_Date = movie.getString(RELEASE_DATE);
            String title = movie.getString(TITLE);
            String poster_path = movie.getString(POSTER_PATH);
            double vote_average = movie.getDouble(VOTE_AVERAGE);
            int movie_id = movie.getInt(MOVIE_ID);

            return new Movie(title, overview, poster_path, release_Date, vote_average, movie_id);
        }
    }
}