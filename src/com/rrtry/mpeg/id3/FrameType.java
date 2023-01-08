package com.rrtry.mpeg.id3;

public enum FrameType {

    PICTURE, TEXT, COMMENT, TIMESTAMP;

    public static FrameType fromIdentifier(String id) {

        if (id.equals(AbstractFrame.PICTURE)) return PICTURE;
        if (id.equals(AbstractFrame.COMMENT)) return COMMENT;
        if (TimestampFrame.isTimestampFrame(id)) return TIMESTAMP;
        if (id.charAt(0) == 'T') return TEXT;

        return null;
    }
}
