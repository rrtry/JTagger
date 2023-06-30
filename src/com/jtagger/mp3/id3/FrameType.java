package com.jtagger.mp3.id3;

public enum FrameType {

    PICTURE, TEXT, COMMENT, TIMESTAMP, S_LYRICS, U_LYRICS;

    public static FrameType fromIdentifier(String id) {

        if (id.equals(AbstractFrame.PICTURE))    return PICTURE;
        if (id.equals(AbstractFrame.COMMENT))    return COMMENT;
        if (id.equals(AbstractFrame.U_LYRICS))   return U_LYRICS;
        if (id.equals(AbstractFrame.S_LYRICS))   return S_LYRICS;
        if (TimestampFrame.isTimestampFrame(id)) return TIMESTAMP;
        if (id.charAt(0) == 'T') return TEXT;

        return null;
    }
}
