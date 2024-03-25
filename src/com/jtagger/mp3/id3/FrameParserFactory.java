package com.jtagger.mp3.id3;

public class FrameParserFactory {

    @SuppressWarnings("rawtypes")
    public static FrameBodyParser getParser(FrameType frameType) {

        FrameBodyParser parser;

        switch (frameType) {
            case TEXT:      parser = new TextFrameParser();                 break;
            case PICTURE:   parser = new AttachedPictureFrameParser();      break;
            case COMMENT:   parser = new CommentFrameParser();              break;
            case U_LYRICS:  parser = new UnsynchronisedLyricsFrameParser(); break;
            case S_LYRICS:  parser = new SynchronisedLyricsFrameParser();   break;
            case CUSTOM:    parser = new UserDefinedTextInfoFrameParser();  break;
            default: throw new IllegalArgumentException("Frame not supported");
        }

        return parser;
    }
}
