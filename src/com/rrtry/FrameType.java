package com.rrtry;

public enum FrameType {

    PICTURE, TEXT, COMMENT;

    public static FrameType fromIdentifier(String id) {

        if (id.equals("APIC")) return PICTURE;
        if (id.equals("COMM")) return COMMENT;
        if (id.charAt(0) == 'T') return TEXT;

        return null;
    }
}
