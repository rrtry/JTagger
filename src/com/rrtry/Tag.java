package com.rrtry;

public interface Tag {

    String getTitle();
    String getArtist();
    String getAlbum();
    String getYear();

    void setTitle(String title);
    void setArtist(String artist);
    void setAlbum(String album);
    void setYear(String year);

    boolean removeTitle();
    boolean removeArtist();
    boolean removeAlbum();
    boolean removeYear();

}
