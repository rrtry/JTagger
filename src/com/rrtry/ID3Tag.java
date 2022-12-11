package com.rrtry;

public interface ID3Tag {

    String getTitle();
    String getArtist();
    String getAlbum();
    String getYear();

    byte getVersion();

    void setTitle(String title);
    void setArtist(String artist);
    void setAlbum(String album);
    void setYear(String year);
    void setVersion(byte version);

    boolean removeTitle();
    boolean removeArtist();
    boolean removeAlbum();
    boolean removeYear();

}
