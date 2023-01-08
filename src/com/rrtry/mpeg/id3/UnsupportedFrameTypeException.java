package com.rrtry.mpeg.id3;

public class UnsupportedFrameTypeException extends Exception {

    public UnsupportedFrameTypeException(String errorMessage) {
        super(errorMessage);
    }
}
