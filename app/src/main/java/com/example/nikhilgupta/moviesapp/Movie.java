package com.example.nikhilgupta.moviesapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Nikhil Gupta on 25-09-2016.
 */
public class Movie implements Parcelable{

    String overview;
    String release_Date;
    String title;
    String poster_path;
    double vote_average;

    Movie(String title, String overview, String poster_path, String release_Date, double vote_average)
    {
        this.overview = overview;
        this.title = title;
        this.release_Date = release_Date;
        this.poster_path = poster_path;
        this.vote_average = vote_average;
    }

    protected Movie(Parcel in) {
        overview = in.readString();
        release_Date = in.readString();
        title = in.readString();
        poster_path = in.readString();
        vote_average = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(overview);
        dest.writeString(release_Date);
        dest.writeString(title);
        dest.writeString(poster_path);
        dest.writeDouble(vote_average);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
