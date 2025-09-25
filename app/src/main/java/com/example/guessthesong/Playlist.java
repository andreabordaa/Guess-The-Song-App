package com.example.guessthesong;

public class Playlist {
    String playlistName, imageUrl;

    public Playlist(String playlistName, String imageUrl) {
        this.playlistName = playlistName;
        this.imageUrl = imageUrl;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
