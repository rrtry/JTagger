package com.jtagger.mp4;

public class MdhdAtom extends MP4Atom {

    private final long dateCreated;
    private final long dateModified;
    private final long duration;
    private final long timescale;

    private final byte version;

    public MdhdAtom(String type, byte[] data,
                    long dateCreated, long dateModified,
                    long duration, long timescale, byte version)
    {
        super(type, data);
        this.dateCreated  = dateCreated;
        this.dateModified = dateModified;
        this.duration     = duration;
        this.timescale    = timescale;
        this.version      = version;
    }

    public MdhdAtom(String type, long dateCreated, long dateModified,
                    long duration, long timescale, byte version)
    {
        super(type);
        this.dateCreated  = dateCreated;
        this.dateModified = dateModified;
        this.duration     = duration;
        this.timescale    = timescale;
        this.version      = version;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public long getDateModified() {
        return dateModified;
    }

    public long getDuration() {
        return duration;
    }

    public long getTimescale() {
        return timescale;
    }
}
