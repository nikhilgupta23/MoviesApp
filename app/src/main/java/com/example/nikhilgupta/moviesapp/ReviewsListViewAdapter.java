package com.example.nikhilgupta.moviesapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Nikhil Gupta on 15-10-2016.
 */
public class ReviewsListViewAdapter extends ArrayAdapter<Review> {

    private ArrayList<Review> data;

    public ReviewsListViewAdapter(Context context, int resource, ArrayList<Review> objects) {
        super(context, resource, objects);
        data = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.review_item_layout, null);
        }
        TextView reviewAuthor = (TextView) row.findViewById(R.id.review_author);
        TextView reviewContent = (TextView) row.findViewById(R.id.review_content);
        reviewAuthor.setText(data.get(position).author);
        reviewContent.setText(data.get(position).content);
        return row;
    }
}
