package com.jtagger.mp3.id3;

/*
    <Header for 'Comment', ID: "COMM">
     Text encoding          $xx
     Language               $xx xx xx
     Short content descrip. <text string according to encoding> $00 (00)
     The actual text        <full text string according to encoding>
*/

import java.nio.charset.Charset;
import java.util.Arrays;

public class CommentFrameParser extends ContentDescriptionFrameParser<CommentFrame> {

    public final int ENCODING_OFFSET    = 0;
    public final int LANGUAGE_OFFSET    = 1;
    public final int LANGUAGE_LENGTH    = 3;
    public final int DESCRIPTION_OFFSET = 4;

    @Override
    protected String parseContentDescription(byte[] frameData, Charset charset) {
        position = DESCRIPTION_OFFSET;
        return super.parseContentDescription(frameData, charset);
    }

    protected String parseComment(byte[] frameData, Charset charset) {
        return new String(
                Arrays.copyOfRange(frameData, position, frameData.length),
                charset
        ).trim();
    }

    protected String parseLanguageCode(byte[] frameData) {
        return new String(Arrays.copyOfRange(frameData, LANGUAGE_OFFSET, LANGUAGE_OFFSET + LANGUAGE_LENGTH));
    }

    @Override
    public CommentFrame parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader) {

        byte encoding   = frameData[ENCODING_OFFSET];
        Charset charset = TextEncoding.getCharset(encoding);

        String language    = parseLanguageCode(frameData);
        String description = parseContentDescription(frameData, charset);
        String comment     = parseComment(frameData, charset);

        return CommentFrame.newBuilder()
                .setEncoding(encoding)
                .setHeader(frameHeader)
                .setLanguage(language)
                .setDescription(description)
                .setText(comment)
                .build(frameData);
    }
}
