package com.example.baitaptuan14_signuploginanduploadvideo;

public class VideoModel {
    String likes;
    String dislikes;
    String url;

    public VideoModel(String likes, String dislikes, String url) {
        this.likes = likes;
        this.dislikes = dislikes;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public VideoModel() {
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getDislikes() {
        return dislikes;
    }

    public void setDislikes(String dislikes) {
        this.dislikes = dislikes;
    }
}
