package com.example.nikhilgupta.moviesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MoviePostersFragment.OnMovieSelectedListener {

    String storedPref;
    boolean mTwoPane;
    final String MOVIE_DETAIL_FRAGMENT_TAG = "MDFTAG";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        storedPref = sharedPref.getString(getString(R.string.sort_order_key), getString(R.string.sort_order_value_mostpopular));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String pref = sharedPref.getString(getString(R.string.sort_order_key), getString(R.string.sort_order_value_mostpopular));

        if(!storedPref.equals(pref)) {
            recreate();
            }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.details_container) != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }
    }

    MovieDetailsFragment details;
    @Override
    public void onMovieSelected(Movie[] movies, int i) {

        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(MovieDetailsFragment.MOVIE_ITEM, movies[i]);

            MovieDetailsFragment movieDetailsFragment = new MovieDetailsFragment();
            movieDetailsFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_container, movieDetailsFragment, MOVIE_DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this , MovieDetailsActivity.class);
            intent.putExtra(MovieDetailsFragment.MOVIE_ITEM, movies[i]);
            startActivity(intent);
        }
    }
}
