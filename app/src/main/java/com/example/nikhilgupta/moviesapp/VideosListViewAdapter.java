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
public class VideosListViewAdapter extends ArrayAdapter<Video> {

    private ArrayList<Video> data;

    public VideosListViewAdapter(Context context, int resource, ArrayList<Video> objects) {
        super(context, resource, objects);
        data = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.video_item_layout, null);
        }
        TextView videoTitle = (TextView) row.findViewById(R.id.video_title);
        videoTitle.setText(data.get(position).name);
        return row;
    }
}
