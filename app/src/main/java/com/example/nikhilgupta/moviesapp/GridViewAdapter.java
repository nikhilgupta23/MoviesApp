package com.example.nikhilgupta.moviesapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Nikhil Gupta on 23-09-2016.
 */
public class GridViewAdapter extends ArrayAdapter<Movie> {
    private Context context;
    private ArrayList<Movie> data = new ArrayList<>();

    public GridViewAdapter(Context context, int layoutResourceId, ArrayList<Movie> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.data = data;
    }

    public static String computeWidth(int width) {
        String widthPath;

        if (width <= 92)
            widthPath = "/w92";
        else if (width <= 154)
            widthPath = "/w154";
        else if (width <= 185)
            widthPath = "/w185";
        else if (width <= 342)
            widthPath = "/w342";
        else if (width <= 500)
            widthPath = "/w500";
        else
            widthPath = "/w780";

        return widthPath;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.grid_item_layout, null);
        }

        ImageView imageView = (ImageView) row.findViewById(R.id.image);
        TextView movieTitle = (TextView) row.findViewById(R.id.poster_title);
        Picasso.with(context)
                .load("http://image.tmdb.org/t/p" + computeWidth(imageView.getMaxWidth()) + data.get(position).poster_path)
                .into(imageView);
        movieTitle.setText(data.get(position).title);
        return row;
    }

}
