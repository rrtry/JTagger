package com.jtagger.mp3.id3;

public class UnsupportedFrameTypeException extends Exception {

    public UnsupportedFrameTypeException(String errorMessage) {
        super(errorMessage);
    }
}
