package com.jtagger.mp3.id3;

import com.jtagger.utils.IntegerUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

import static com.jtagger.mp3.id3.AttachedPictureFrame.DESCRIPTION_MAX_LENGTH;
import static com.jtagger.mp3.id3.TextEncoding.ENCODING_UTF_16;
import static com.jtagger.mp3.id3.TextEncoding.ENCODING_UTF_16_BE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

/*
<Header for 'Synchronised lyrics/text', ID: "SYLT">
     Text encoding        $xx
     Language             $xx xx xx
     Time stamp format    $xx
     Content type         $xx
     Content descriptor   <text string according to encoding> $00 (00)

   Content type:   $00 is other
                   $01 is lyrics
                   $02 is text transcription
                   $03 is movement/part name (e.g. "Adagio")
                   $04 is events (e.g. "Don Quijote enters the stage")
                   $05 is chord (e.g. "Bb F Fsus")
                   $06 is trivia/'pop up' information
                   $07 is URLs to webpages
                   $08 is URLs to images
 */

public class SynchronisedLyricsFrameParser extends ContentDescriptionFrameParser<SynchronisedLyricsFrame> {

    private HashMap<Integer, String> parseSynchronisedLyrics(byte[] synchLyrics, byte encoding) {

        boolean isUTF16 = ENCODING_UTF_16 == encoding || ENCODING_UTF_16_BE == encoding;
        Charset charset = TextEncoding.getCharset(encoding);
        HashMap<Integer, String> lyricsMap = new HashMap<>();

        int startIndex   = 0;
        final int offset = 5 - (!isUTF16 ? 1 : 0);

        for (int i = 0; i < synchLyrics.length - offset; i++) {

            if ((isUTF16 && synchLyrics[i] == '\0' && synchLyrics[i + 1] == '\0') ||
                (synchLyrics[i] == '\0' && !isUTF16))
            {
                byte[] timestampBytes = new byte[] {
                        synchLyrics[i + 2 - (isUTF16 ? 0 : 1)], synchLyrics[i + 3 - (isUTF16 ? 0 : 1)],
                        synchLyrics[i + 4 - (isUTF16 ? 0 : 1)], synchLyrics[i + 5 - (isUTF16 ? 0 : 1)]
                };

                int timestamp = IntegerUtils.toUInt32BE(timestampBytes);
                lyricsMap.put(
                        timestamp, new String(Arrays.copyOfRange(synchLyrics, startIndex, i), charset
                ));

                i = i + 6 - (isUTF16 ? 0 : 1);
                startIndex = i;
            }
        }
        return lyricsMap;
    }

    @Override
    public SynchronisedLyricsFrame parse(String identifier, FrameHeader frameHeader, byte[] frameData, TagHeader tagHeader) {

        byte[] synchLyrics;
        byte encoding;
        byte timestampFormat;
        byte contentType;

        String lang;
        String description;

        encoding        = frameData[position++];
        lang            = new String(Arrays.copyOfRange(frameData, position, position + 3), ISO_8859_1); position += 3;
        timestampFormat = frameData[position++];
        contentType     = frameData[position++];
        description     = parseContentDescription(frameData, TextEncoding.getCharset(encoding));
        synchLyrics     = Arrays.copyOfRange(frameData, position, frameData.length);

        return SynchronisedLyricsFrame.newBuilder()
                .setEncoding(encoding)
                .setHeader(frameHeader)
                .setLanguage(lang)
                .setDescription(description)
                .setContentType(contentType)
                .setTimestampFormat(timestampFormat)
                .setLyrics(parseSynchronisedLyrics(synchLyrics, encoding))
                .build(frameData);
    }
}
