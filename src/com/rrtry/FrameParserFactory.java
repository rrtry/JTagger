package com.rrtry;

public class FrameParserFactory {

    @SuppressWarnings("rawtypes")
    public static FrameBodyParser getParser(FrameType frameType) {

        FrameBodyParser parser;

        switch (frameType) {
            case TEXT: parser = new TextFrameParser(); break;
            case PICTURE: parser = new AttachedPictureFrameParser(); break;
            case COMMENT: parser = new CommentFrameParser(); break;
            default: throw new IllegalArgumentException("Frame not supported");
        }

        return parser;
    }
}
