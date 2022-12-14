package com.rrtry.mpeg.id3;

public class FrameParserFactory {

    @SuppressWarnings("rawtypes")
    public static FrameBodyParser getParser(FrameType frameType) {

        FrameBodyParser parser;

        switch (frameType) {
            case TIMESTAMP: parser = new TimestampFrameParser(); break;
            case TEXT:      parser = new TextFrameParser(); break;
            case PICTURE:   parser = new AttachedPictureFrameParser(); break;
            case COMMENT:   parser = new CommentFrameParser(); break;
            default: throw new IllegalArgumentException("Frame not supported");
        }

        return parser;
    }
}
