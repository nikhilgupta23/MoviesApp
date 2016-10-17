package com.example.nikhilgupta.moviesapp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MovieDetailsActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
            Movie movie = getIntent().getParcelableExtra(MovieDetailsFragment.MOVIE_ITEM);

            Bundle args = new Bundle();
            args.putParcelable(MovieDetailsFragment.MOVIE_ITEM, movie);

            MovieDetailsFragment movieDetailsFragment = new MovieDetailsFragment();
            movieDetailsFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.details_container, movieDetailsFragment)
                    .commit();
        }
    }



}