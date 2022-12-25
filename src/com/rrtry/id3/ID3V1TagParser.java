package com.rrtry.id3;

import com.rrtry.TagParser;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.rrtry.id3.ID3V1Tag.*;

public class ID3V1TagParser implements TagParser<ID3V1Tag> {

    public ID3V1Tag parse(RandomAccessFile file) throws IOException {

        byte[] buffer = new byte[128];

        file.seek( file.length() - 128);
        file.read(buffer, 0, buffer.length);

        String id = new String(
                Arrays.copyOfRange(buffer, 0, 3),
                StandardCharsets.ISO_8859_1
        );
        if (!id.equals(ID)) return null; // tag is not present

        byte version = ID3V1;
        byte commentLength = 30;

        if (buffer[COMMENT_OFFSET + 28] == 0x00 && buffer[TRACK_NUMBER] != 0x00) {
            version = ID3V1_1;
            commentLength = 28;
        }

        String title   = new String(Arrays.copyOfRange(buffer, TITLE_OFFSET, TITLE_OFFSET + 30)).replace("\0", "");
        String artist  = new String(Arrays.copyOfRange(buffer, ARTIST_OFFSET, ARTIST_OFFSET + 30)).replace("\0", "");
        String album   = new String(Arrays.copyOfRange(buffer, ALBUM_OFFSET, ALBUM_OFFSET + 30)).replace("\0", "");
        String year    = new String(Arrays.copyOfRange(buffer, YEAR_OFFSET, YEAR_OFFSET + 4)).replace("\0", "");
        String comment = new String(Arrays.copyOfRange(buffer, COMMENT_OFFSET, COMMENT_OFFSET + commentLength)).replace("\0", "");
        int genre      = ((int) buffer[GENRE_OFFSET]) & 0xFF;

        ID3V1Tag.Builder builder = ID3V1Tag.newBuilder()
                .setVersion(version)
                .setTitle(title)
                .setArtist(artist)
                .setAlbum(album)
                .setGenre(genre)
                .setYear(year)
                .setComment(comment);

        if (version == ID3V1_1) {
            int trackNumber = ((int) buffer[TRACK_NUMBER]) & 0xFF;
            builder = builder.setAlbumTrack(trackNumber);
        }
        return builder.buildExisting(buffer);
    }
}
