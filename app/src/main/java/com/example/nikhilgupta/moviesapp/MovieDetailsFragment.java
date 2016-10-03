package com.example.nikhilgupta.moviesapp;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class MovieDetailsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_details,
                container, false);
        return view;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void setNewPage(Movie movie)
    {
        (getActivity().findViewById(R.id.overlay)).setVisibility(View.GONE);
        ((TextView)getActivity().findViewById(R.id.movie_title)).setText(movie.title);
        ((TextView)getActivity().findViewById(R.id.movie_overview)).setText(movie.overview);
        ((TextView)getActivity().findViewById(R.id.movie_releasedate)).setText(getString(R.string.release)+ " " + movie.release_Date);
        ((TextView)getActivity().findViewById(R.id.movie_rating)).setText(getString(R.string.rating) + " " + movie.vote_average);
        ImageView imageView = ((ImageView)getActivity().findViewById(R.id.movie_poster));
        Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w500"+movie.poster_path).into(imageView);
        if(isOnline() == false) {
            Toast.makeText(getActivity(),getString(R.string.internet_error_toast_message),Toast.LENGTH_LONG).show();
        }
    }
}