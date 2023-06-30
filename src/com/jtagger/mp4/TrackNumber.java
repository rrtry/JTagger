package com.jtagger.mp4;

class TrackNumber {

    final int trackPos;
    final int totalTracks;

    public TrackNumber(int trackPos, int totalTracks) {
        this.trackPos = trackPos;
        this.totalTracks = totalTracks;
    }

    @Override
    public String toString() {
        return trackPos + "/" + totalTracks;
    }
}
