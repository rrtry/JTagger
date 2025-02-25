package com.jtagger.mp3.id3;

import com.jtagger.TagParser;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.jtagger.mp3.id3.ID3V1Tag.*;
import static com.jtagger.mp3.id3.TextEncoding.ENCODING_LATIN_1;
import static com.jtagger.mp3.id3.TextEncoding.getString;
import static java.lang.Byte.toUnsignedInt;

public class ID3V1TagParser implements TagParser<ID3V1Tag> {

    public ID3V1Tag parseTag(RandomAccessFile file) {
        try {

            byte[] buffer = new byte[128];
            file.seek(file.length() - 128);
            file.readFully(buffer);

            String id = new String(
                    Arrays.copyOfRange(buffer, 0, 3),
                    StandardCharsets.ISO_8859_1
            );
            if (!id.equals(ID)) return null; // tag is not present

            byte version       = ID3V1;
            byte commentLength = 30;

            if (buffer[COMMENT_OFFSET + 28] == 0x00 && buffer[TRACK_NUMBER] != 0x00) {
                version       = ID3V1_1;
                commentLength = 28;
            }

            String title   = getString(buffer, TITLE_OFFSET,  30, ENCODING_LATIN_1);
            String artist  = getString(buffer, ARTIST_OFFSET, 30, ENCODING_LATIN_1);
            String album   = getString(buffer, ALBUM_OFFSET,  30, ENCODING_LATIN_1);
            String year    = getString(buffer, YEAR_OFFSET,    4, ENCODING_LATIN_1);
            String comment = getString(buffer, COMMENT_OFFSET, commentLength, ENCODING_LATIN_1);
            int genre      = toUnsignedInt(buffer[GENRE_OFFSET]);

            ID3V1Tag.Builder builder = ID3V1Tag.newBuilder()
                    .setVersion(version)
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbum(album)
                    .setGenre(genre)
                    .setYear(year)
                    .setComment(comment);

            if (version == ID3V1_1) {
                builder = builder.setAlbumTrack(toUnsignedInt(buffer[TRACK_NUMBER]));
            }
            return builder.build(version);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
