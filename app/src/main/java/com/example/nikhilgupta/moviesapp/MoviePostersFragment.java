package com.example.nikhilgupta.moviesapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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

/**
 * Created by Nikhil Gupta on 25-09-2016.
 */
public class MoviePostersFragment extends Fragment {

    OnMovieSelectedListener MovieListener;
    ProgressDialog mProgressDialog;

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

        if(isOnline() == false) {
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

        new FetchMoviesTask().execute(pref, getString(R.string.api_base_url), getString(R.string.api_key));

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.movie_posters_fragment, container, false);
    }

    class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

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
            Movie[] fetchedMovies=movies;
            if(movies!=null) {
                ArrayList<String> paths = new ArrayList<>();
                for (int k = 0; k < fetchedMovies.length; k++) {
                    paths.add(fetchedMovies[k].poster_path);
                }
                GridView gridview = (GridView) getView().findViewById(R.id.gridView);
                gridview.setAdapter(new GridViewAdapter(getActivity(), R.id.gridView, paths));
                gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v,
                                            int position, long id) {
                        MovieListener.onMovieSelected(movies, position);
                    }
                });
            }
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

                results[i] = new Movie(title, overview, poster_path, release_Date, vote_average);
            }
            return results;
        }
    }
}