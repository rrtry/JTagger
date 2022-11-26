package com.rrtry;

/*
    <Header for 'Comment', ID: "COMM">
     Text encoding          $xx
     Language               $xx xx xx
     Short content descrip. <text string according to encoding> $00 (00)
     The actual text        <full text string according to encoding>
*/

import java.nio.charset.Charset;
import java.util.Arrays;

public class CommentFrameParser implements FrameBodyParser<CommentFrame> {

    private int position;
    public final int ENCODING_OFFSET = 0;
    public final int LANGUAGE_OFFSET = 1;
    public final int LANGUAGE_LENGTH = 3;
    public final int DESCRIPTION_OFFSET = 4;

    private String parseComment(byte[] frameData, Charset charset) {
        return new String(
                Arrays.copyOfRange(frameData, ++position, frameData.length),
                charset
        ).replace("\0", "");
    }

    private String parseCommentDescription(byte[] frameData, Charset charset) {

        final int from = DESCRIPTION_OFFSET;
        position = from;
        int step = 1;

        if (TextEncoding.isUTF16(charset)) step++;
        if (TextEncoding.hasByteOrderMark(frameData, position)) position += TextEncoding.UTF_16_BOM_LENGTH;

        while (frameData[position] != '\0') {
            position += step;
        }

        final String description = new String(Arrays.copyOfRange(frameData, from, position), charset);
        if (TextEncoding.isUTF16(charset)) position += 1;
        return description.replace("\0", "");
    }

    private String parseLanguageCode(byte[] frameData) {
        return new String(Arrays.copyOfRange(frameData, LANGUAGE_OFFSET, LANGUAGE_OFFSET + LANGUAGE_LENGTH));
    }

    @Override
    public CommentFrame parse(TagHeader tagHeader, FrameHeader frameHeader, byte[] frameData) {

        byte encoding = frameData[ENCODING_OFFSET];
        Charset charset = TextEncoding.getCharset(encoding);

        String language = parseLanguageCode(frameData);
        String description = parseCommentDescription(frameData, charset);
        String comment = parseComment(frameData, charset);

        return CommentFrame.newBuilder()
                .setEncoding(encoding)
                .setHeader(frameHeader)
                .setLanguage(language)
                .setDescription(description)
                .setComment(comment)
                .build(frameData);
    }
}
