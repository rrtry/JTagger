package com.jtagger.mp3.id3;

public enum FrameType {

    PICTURE, TEXT, COMMENT, S_LYRICS, U_LYRICS, CUSTOM;

    public static FrameType fromIdentifier(String id) {

        if (id.equals(AbstractFrame.PICTURE))    return PICTURE;
        if (id.equals(AbstractFrame.COMMENT))    return COMMENT;
        if (id.equals(AbstractFrame.U_LYRICS))   return U_LYRICS;
        if (id.equals(AbstractFrame.S_LYRICS))   return S_LYRICS;
        if (id.equals(AbstractFrame.CUSTOM))     return CUSTOM;
        if (id.charAt(0) == 'T') return TEXT;

        return null;
    }
}
