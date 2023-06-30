package com.jtagger.mp3.id3;

import java.nio.charset.Charset;

public class UnsychronisedLyricsFrameParser extends CommentFrameParser {

    @Override
    public UnsynchronisedLyricsFrame parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader) {

        byte encoding = frameData[ENCODING_OFFSET];

        Charset charset    = TextEncoding.getCharset(encoding);
        String language    = parseLanguageCode(frameData);
        String description = parseCommentDescription(frameData, charset);
        String comment     = parseComment(frameData, charset);

        return (UnsynchronisedLyricsFrame) UnsynchronisedLyricsFrame.newBuilder()
                .setEncoding(encoding)
                .setHeader(frameHeader)
                .setLanguage(language)
                .setDescription(description)
                .setText(comment)
                .build(frameData);
    }
}
