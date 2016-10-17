package com.example.nikhilgupta.moviesapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MovieDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private TextView mMovieTitleTextView;
    private TextView mMovieOverviewTextView;
    private TextView mMovieReleaseDateTextView;
    private TextView mMovieRatingTextView;
    private TextView mMovieFavoriteAddedOnTextView;
    private ImageView mMoviePosterImageView;
    private CheckBox mMovieFavoriteCheckBox;
    private ListView mMovieVideosListView;
    private ListView mMovieReviewsListView;
    private static final int FAVORITE_LOADER = 5;

    private int mCurrentMovieId;

    private Uri mUri;

    public static final String MOVIE_ITEM = "movie";

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

    private final String LOG_TAG = MovieDetailsFragment.class.getSimpleName();

    CompoundButton.OnCheckedChangeListener favoriteChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            try {
                if (isChecked) {
                    ContentValues favoriteValues = new ContentValues();
                    favoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID, mCurrentMovieId);
                    getContext().getContentResolver().insert(
                            FavoritesContract.FavoritesEntry.CONTENT_URI,
                            favoriteValues
                    );
                    getLoaderManager().restartLoader(FAVORITE_LOADER, null, MovieDetailsFragment.this);
                } else {
                    getContext().getContentResolver().delete(FavoritesContract.FavoritesEntry.CONTENT_URI,
                            FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{Integer.toString(mCurrentMovieId)});
                    getLoaderManager().restartLoader(FAVORITE_LOADER, null, MovieDetailsFragment.this);
                }
            } catch (SQLException e) {
                Log.e(LOG_TAG, "Error ", e);
            }
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    FAVORITE_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieFavoriteCheckBox.setOnCheckedChangeListener(null);
        if (data == null || data.getCount() == 0) {
            mMovieFavoriteCheckBox.setChecked(false);
            mMovieFavoriteAddedOnTextView.setText(getString(R.string.add_to_favorites));
        } else if (data != null && data.moveToFirst()) {
            mMovieFavoriteCheckBox.setChecked(true);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            java.util.Date dt = null;
            try {
                dt = sdf.parse(data.getString(COL_ADDED_ON));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (dt != null) {
                long ts = System.currentTimeMillis();
                Date localTime = new Date(ts);
                Date fromGmt = new Date(dt.getTime() + TimeZone.getDefault().getOffset(localTime.getTime()));
                SimpleDateFormat date = new SimpleDateFormat("MM-dd-yyyy");
                SimpleDateFormat time = new SimpleDateFormat("HH:mm");
                mMovieFavoriteAddedOnTextView.setText(getString(R.string.added_on) + " " +
                        date.format(fromGmt) + " " +
                        getString(R.string.at) + " " +
                        time.format(fromGmt));
            }
        }
        mMovieFavoriteCheckBox.setOnCheckedChangeListener(favoriteChangeListener);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FAVORITE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        Movie movie = null;
        if (arguments != null) {
            movie = arguments.getParcelable(MOVIE_ITEM);
        }

        View view = inflater.inflate(R.layout.fragment_movie_details,
                container, false);
        mMovieTitleTextView = ((TextView) view.findViewById(R.id.movie_title));
        mMovieOverviewTextView = ((TextView) view.findViewById(R.id.movie_overview));
        mMovieReleaseDateTextView = ((TextView) view.findViewById(R.id.movie_releasedate));
        mMovieRatingTextView = ((TextView) view.findViewById(R.id.movie_rating));
        mMovieFavoriteAddedOnTextView = ((TextView) view.findViewById(R.id.movie_favorite_added_on));
        mMoviePosterImageView = ((ImageView) view.findViewById(R.id.movie_poster));
        mMovieFavoriteCheckBox = ((CheckBox) view.findViewById(R.id.movie_favorite_toggle_button));
        mMovieFavoriteCheckBox.setOnCheckedChangeListener(favoriteChangeListener);
        mMovieReviewsListView = ((ListView) view.findViewById(R.id.reviews_list_view));
        mMovieVideosListView = ((ListView) view.findViewById(R.id.videos_list_view));

        if (movie != null) {
            setNewPage(movie);
        }
        return view;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void setNewPage(Movie movie) {
        try {
            mMovieTitleTextView.setText(movie.title);
            mMovieOverviewTextView.setText(movie.overview);
            mMovieReleaseDateTextView.setText(getString(R.string.release) + " " + movie.release_Date);
            mMovieRatingTextView.setText(getString(R.string.rating) + " " + movie.vote_average);
            Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w500" + movie.poster_path).into(mMoviePosterImageView);

            mCurrentMovieId = movie.movie_id;
            mUri = FavoritesContract.FavoritesEntry.buildFavoritesUri(mCurrentMovieId);
            if (isOnline() == false) {
                Toast.makeText(getActivity(), getString(R.string.internet_error_toast_message), Toast.LENGTH_LONG).show();
            }
            new FetchVideosAndReviewsTask().execute(Integer.toString(mCurrentMovieId), getString(R.string.api_base_url), getString(R.string.api_key));
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Error", e);
        }

    }

    class FetchVideosAndReviewsTask extends AsyncTask<String, Void, VideosAndReviews> {
        private final String LOG_TAG = FetchVideosAndReviewsTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(final VideosAndReviews videosAndReviews) {
            Review[] fetchedReviews = videosAndReviews.reviews;
            final Video[] fetchedVideos = videosAndReviews.videos;
            if (fetchedVideos.length != 0) {
                mMovieVideosListView.setAdapter(new VideosListViewAdapter(getActivity(),
                        R.id.videos_list_view,
                        new ArrayList<Video>(Arrays.asList(fetchedVideos))));
                mMovieVideosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v,
                                            int position, long id) {
                        String videoId = fetchedVideos[position].key;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
                        intent.putExtra("VIDEO_ID", videoId);
                        startActivity(intent);
                    }
                });
                setListViewHeightBasedOnItems(mMovieVideosListView);
            }
            if (fetchedReviews.length != 0) {
                mMovieReviewsListView.setAdapter(new ReviewsListViewAdapter(getActivity(),
                        R.id.reviews_list_view,
                        new ArrayList<Review>(Arrays.asList(fetchedReviews))));
            }
        }

        /**
         * Sets ListView height dynamically based on the height of the items.
         *
         * @param listView to be resized
         * @return true if the listView is successfully resized, false otherwise
         */
        public boolean setListViewHeightBasedOnItems(ListView listView) {

            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter != null) {

                int numberOfItems = listAdapter.getCount();

                // Get total height of all items.
                int totalItemsHeight = 0;
                for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                    View item = listAdapter.getView(itemPos, null, listView);
                    item.measure(0, 0);
                    totalItemsHeight += item.getMeasuredHeight();
                }

                // Get total height of all item dividers.
                int totalDividersHeight = listView.getDividerHeight() *
                        (numberOfItems - 1);

                // Set list height.
                ViewGroup.LayoutParams params = listView.getLayoutParams();
                params.height = totalItemsHeight + totalDividersHeight;
                listView.setLayoutParams(params);
                listView.requestLayout();

                return true;

            } else {
                return false;
            }

        }

        @Override
        protected VideosAndReviews doInBackground(String... params) {
            return getInfo(params[0], params[1], params[2]);
        }

        String makeRequest(Uri builtUri) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
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
                return buffer.toString();
            } catch (Exception e) {
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

        VideosAndReviews getInfo(String movie_id, String base, String key) {
            Video[] videos;
            Review[] reviews;

            String base_url = base + movie_id + "/reviews?";
            Uri builtReviewsUri = Uri.parse(base_url).buildUpon().appendQueryParameter("api_key", key).build();
            base_url = base + movie_id + "/videos?";
            Uri builtVideosUri = Uri.parse(base_url).buildUpon().appendQueryParameter("api_key", key).build();
            try {
                videos = getVideosDataFromJson(makeRequest(builtVideosUri));
                reviews = getReviewsDataFromJson(makeRequest(builtReviewsUri));
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            }
            return new VideosAndReviews(videos, reviews);
        }

        private Review[] getReviewsDataFromJson(String reviewJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String AUTHOR = "author";
            final String CONTENT = "content";
            final String REVIEWS = "results";

            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray reviewsArray = reviewJson.getJSONArray(REVIEWS);

            Review[] results = new Review[reviewsArray.length()];

            for (int i = 0; i < results.length; i++) {
                // Get the JSON object representing the day
                JSONObject review = reviewsArray.getJSONObject(i);

                String author = review.getString(AUTHOR);
                String content = review.getString(CONTENT);

                results[i] = new Review(author, content);
            }
            return results;
        }

        private Video[] getVideosDataFromJson(String videoJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String NAME = "name";
            final String TYPE = "type";
            final String KEY = "key";
            final String SITE = "site";
            final String VIDEOS = "results";

            JSONObject videoJson = new JSONObject(videoJsonStr);
            JSONArray videosArray = videoJson.getJSONArray(VIDEOS);

            Video[] results = new Video[videosArray.length()];

            int j = 0;
            for (int i = 0; i < results.length; i++) {
                // Get the JSON object representing the day
                JSONObject video = videosArray.getJSONObject(i);

                String name = video.getString(NAME);
                String type = video.getString(TYPE);
                String key = video.getString(KEY);

                if (video.getString(SITE).equals("YouTube"))
                    results[j++] = new Video(name, type, key);
            }
            return results;
        }

    }
}