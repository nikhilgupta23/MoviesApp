package com.example.nikhilgupta.moviesapp;

/**
 * Created by Nikhil Gupta on 14-10-2016.
 */

public class VideosAndReviews {
    Review[] reviews;
    Video[] videos;

    VideosAndReviews(Video[] videos, Review[] reviews) {
        this.reviews = reviews;
        this.videos = videos;
    }
}

class Review {
    String author;
    String content;

    Review(String author, String content) {
        this.author = author;
        this.content = content;
    }
}

class Video {
    String name;
    String type;
    String key;

    Video(String name, String type, String key) {
        this.name = name;
        this.type = type;
        this.key = key;
    }
}
