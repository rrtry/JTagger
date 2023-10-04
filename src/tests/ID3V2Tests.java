package tests;

import com.jtagger.AttachedPicture;
import com.jtagger.mp3.id3.*;
import com.jtagger.utils.ImageReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.Year;
import java.util.Arrays;
import java.util.HashMap;

import static com.jtagger.AttachedPicture.*;
import static com.jtagger.mp3.id3.AbstractFrame.S_LYRICS;
import static com.jtagger.mp3.id3.ID3V2Tag.ID3V2_3;
import static com.jtagger.mp3.id3.ID3V2Tag.ID3V2_4;
import static com.jtagger.mp3.id3.SynchronisedLyricsFrame.CONTENT_TYPE_LYRICS;
import static com.jtagger.mp3.id3.SynchronisedLyricsFrame.CONTENT_TYPE_TRANSCRIPTION;
import static com.jtagger.mp3.id3.TextEncoding.*;

public class ID3V2Tests {

    @Test
    public void testAttachedPictureFrameThumbnailsEqual() {

        AttachedPictureFrame pngIcon = AttachedPictureFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("APIC", ID3V2_4))
                .setMimeType("image/png")
                .setPictureType(PICTURE_TYPE_PNG_ICON)
                .setDescription("Png icon")
                .setEncoding(ENCODING_UTF_8)
                .setPictureData(new byte[0])
                .build(ID3V2_4);

        AttachedPictureFrame jpegIcon = AttachedPictureFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("APIC", ID3V2_4))
                .setMimeType("image/jpeg")
                .setPictureType(PICTURE_TYPE_OTHER_ICON)
                .setDescription("Jpeg icon")
                .setEncoding(ENCODING_UTF_8)
                .setPictureData(new byte[0])
                .build(ID3V2_4);

        Assertions.assertEquals(pngIcon, jpegIcon);
    }

    @Test
    public void testRemoveAttachedPictureFrames() {

        ID3V2Tag tag = new ID3V2Tag();
        tag.setTagHeader(new TagHeader());

        AttachedPictureFrame frontCover = AttachedPictureFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("APIC", ID3V2_4))
                .setMimeType("image/jpeg")
                .setPictureType(PICTURE_TYPE_FRONT_COVER)
                .setDescription("Front cover")
                .setEncoding(ENCODING_UTF_8)
                .setPictureData(new byte[0])
                .build(ID3V2_4);

        AttachedPictureFrame backCover = AttachedPictureFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("APIC", ID3V2_4))
                .setMimeType("image/jpeg")
                .setPictureType(PICTURE_TYPE_BACK_COVER)
                .setDescription("Back cover")
                .setEncoding(ENCODING_UTF_8)
                .setPictureData(new byte[0])
                .build(ID3V2_4);

        final String frontCoverKey = "APIC:Front cover";
        final String backCoverKey  = "APIC:Back cover";

        tag.setFrame(frontCover);
        tag.setFrame(backCover);

        Assertions.assertNotNull(tag.getFrame(frontCoverKey));
        Assertions.assertNotNull(tag.getFrame(backCoverKey));

        tag.removeFrame(frontCoverKey);
        tag.removeFrame(backCoverKey);

        Assertions.assertNull(tag.getFrame(frontCoverKey));
        Assertions.assertNull(tag.getFrame(backCoverKey));
    }

    @Test
    public void testTagVersionMigration() {

        ID3V2Tag tag = new ID3V2Tag();
        tag.setTagHeader(new TagHeader());

        LocalDateTime timestamp = LocalDateTime.of(
                2005, 1, 18, 17, 0, 0
        );

        TimestampFrame timestampFrame = TimestampFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader(AbstractFrame.RECORDING_TIME, ID3V2_4))
                .setDateTime(timestamp)
                .build(ID3V2_4);

        tag.setFrame(timestampFrame);
        ID3V2Tag converted = ID3V2Tag.convertID3V2Tag(tag, ID3V2_3);

        YearFrame year = converted.getFrame(AbstractFrame.YEAR);
        DateFrame date = converted.getFrame(AbstractFrame.DATE);
        TimeFrame time = converted.getFrame(AbstractFrame.TIME);

        Assertions.assertEquals(time.getFrameData(), "1700");
        Assertions.assertEquals(date.getDate(), MonthDay.of(1, 18));
        Assertions.assertEquals(year.getYear(), Year.of(2005));
        Assertions.assertNull(converted.getFrame(AbstractFrame.RECORDING_TIME));

        converted      = ID3V2Tag.convertID3V2Tag(tag, ID3V2_4);
        timestampFrame = converted.getFrame(AbstractFrame.RECORDING_TIME);

        Assertions.assertEquals(timestampFrame.getTimestamp(), timestamp);
        Assertions.assertNull(converted.getFrame(AbstractFrame.YEAR));
        Assertions.assertNull(converted.getFrame(AbstractFrame.DATE));
        Assertions.assertNull(converted.getFrame(AbstractFrame.TIME));
    }

    @Test
    public void testTextFrameInvalidId() {
        try {

            TextFrame.newBuilder()
                    .setHeader(FrameHeader.createFrameHeader("TITL", ID3V2_4))
                    .setEncoding(ENCODING_LATIN_1)
                    .setText("TITLE")
                    .build(ID3V2_4);

        } catch (IllegalArgumentException e) {
            Assertions.assertNotNull(e);
        }
    }

    @ParameterizedTest
    @ValueSource(bytes = { ENCODING_UTF_16, ENCODING_UTF_16_BE, ENCODING_UTF_8, ENCODING_LATIN_1 })
    public void testTextFrame(byte encoding) {

        TextFrame titleFrame = TextFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("TIT2", ID3V2_4))
                .setEncoding(encoding)
                .setText("TITLE")
                .build(ID3V2_4);

        TextFrameParser parser = new TextFrameParser();
        TextFrame parsedFrame = parser.parse(
                "TIT2", titleFrame.getHeader(), titleFrame.getBytes(), new TagHeader()
        );

        Assertions.assertEquals(titleFrame.getEncoding(), parsedFrame.getEncoding());
        Assertions.assertEquals(titleFrame.getText(), parsedFrame.getText());
    }

    @Test
    public void testLyricsFrameEquality() {

        CommentFrame comment = CommentFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("COMM", ID3V2_4))
                .setDescription("MUSICBRAINZ")
                .setLanguage("eng")
                .setText("MUSICBRAINZ METADATA")
                .build(ID3V2_4);

        UnsynchronisedLyricsFrame lyrics = (UnsynchronisedLyricsFrame) UnsynchronisedLyricsFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("USLT", ID3V2_4))
                .setDescription("MUSICBRAINZ")
                .setLanguage("eng")
                .setText("MUSICBRAINZ METADATA")
                .build(ID3V2_4);

        Assertions.assertNotEquals(comment, lyrics);
    }

    @Test
    public void testCommentFramesNotEqual() {

        CommentFrame musicbrainzComment = CommentFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("COMM", ID3V2_4))
                .setDescription("MUSICBRAINZ")
                .setLanguage("eng")
                .setText("MUSICBRAINZ METADATA")
                .build(ID3V2_4);

        CommentFrame discogsComment = CommentFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("COMM", ID3V2_4))
                .setDescription("DISCOGS")
                .setLanguage("eng")
                .setText("DISCOGS METADATA")
                .build(ID3V2_4);

        Assertions.assertNotEquals(musicbrainzComment, discogsComment);
    }

    @Test
    public void testSynchronisedLyricsFramesNotEqual() {

        SynchronisedLyricsFrame lyrics = SynchronisedLyricsFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("SYLT", ID3V2_4))
                .setDescription("LYRICS")
                .setContentType(CONTENT_TYPE_LYRICS)
                .setLanguage("eng")
                .setLyrics(new HashMap<>())
                .build(ID3V2_4);

        SynchronisedLyricsFrame transcript = SynchronisedLyricsFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("SYLT", ID3V2_4))
                .setDescription("LYRICS")
                .setContentType(CONTENT_TYPE_TRANSCRIPTION)
                .setLanguage("rus")
                .setLyrics(new HashMap<>())
                .build(ID3V2_4);

        Assertions.assertNotEquals(lyrics, transcript);
    }

    @Test
    public void testAttachedPictureFramesNotEqual() {

        AttachedPictureFrame frontCover = AttachedPictureFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("APIC", ID3V2_4))
                .setMimeType("image/jpeg")
                .setDescription("Front cover")
                .setEncoding(ENCODING_UTF_8)
                .setPictureType(PICTURE_TYPE_FRONT_COVER)
                .setPictureData(new byte[] { 0x00, 0x00, 0x00 })
                .build(ID3V2_4);

        AttachedPictureFrame backCover = AttachedPictureFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("APIC", ID3V2_4))
                .setMimeType("image/jpeg")
                .setDescription("Back cover")
                .setEncoding(ENCODING_UTF_8)
                .setPictureType(PICTURE_TYPE_BACK_COVER)
                .setPictureData(new byte[] { 0x00, 0x00, 0x00 })
                .build(ID3V2_4);

        Assertions.assertNotEquals(frontCover, backCover);
    }

    @Test
    public void testCommentFramesEqual() {

        CommentFrame musicbrainzComment = CommentFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("COMM", ID3V2_4))
                .setDescription("METADATA")
                .setLanguage("eng")
                .setText("MUSICBRAINZ ID")
                .build(ID3V2_4);

        CommentFrame discogsComment = CommentFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("COMM", ID3V2_4))
                .setDescription("METADATA")
                .setLanguage("eng")
                .setText("DISCOGS ID")
                .build(ID3V2_4);

        Assertions.assertEquals(musicbrainzComment, discogsComment);
    }

    @Test
    public void testSynchronisedLyricsFramesEqual() {

        SynchronisedLyricsFrame lyrics = SynchronisedLyricsFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("SYLT", ID3V2_4))
                .setDescription("LYRICS")
                .setContentType(CONTENT_TYPE_LYRICS)
                .setLanguage("eng")
                .setLyrics(new HashMap<>())
                .build(ID3V2_4);

        SynchronisedLyricsFrame transcript = SynchronisedLyricsFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("SYLT", ID3V2_4))
                .setDescription("LYRICS")
                .setContentType(CONTENT_TYPE_TRANSCRIPTION)
                .setLanguage("eng")
                .setLyrics(new HashMap<>())
                .build(ID3V2_4);

        Assertions.assertEquals(lyrics, transcript);
    }

    @Test
    public void testAttachedPictureFramesEqual() {

        AttachedPictureFrame frontCover = AttachedPictureFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("APIC", ID3V2_4))
                .setMimeType("image/jpeg")
                .setDescription("Album cover")
                .setEncoding(ENCODING_UTF_8)
                .setPictureType(PICTURE_TYPE_FRONT_COVER)
                .setPictureData(new byte[] { 0x00, 0x00, 0x00 })
                .build(ID3V2_4);

        AttachedPictureFrame backCover = AttachedPictureFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("APIC", ID3V2_4))
                .setMimeType("image/jpeg")
                .setDescription("Album cover")
                .setEncoding(ENCODING_UTF_8)
                .setPictureType(PICTURE_TYPE_FRONT_COVER)
                .setPictureData(new byte[] { 0x00, 0x00, 0x00 })
                .build(ID3V2_4);

        Assertions.assertEquals(frontCover, backCover);
    }

    @Test
    public void testSynchSafeInteger() {
        Assertions.assertEquals(383, ID3SynchSafeInteger.toSynchSafeInteger(255));
        Assertions.assertEquals(255, ID3SynchSafeInteger.fromSynchSafeInteger(383));
    }

    @Test
    public void testSynchSafeBuffer() {

        byte[] synch   = new byte[] { (byte) 0xFF, (byte) 0xE0, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x01 };
        byte[] unsynch = UnsynchronisationUtils.toUnsynch(synch);

        Assertions.assertFalse(Arrays.equals(synch, unsynch));
        Assertions.assertArrayEquals(synch, UnsynchronisationUtils.fromUnsynch(unsynch));
    }

    @ParameterizedTest
    @ValueSource(bytes = { ENCODING_UTF_16, ENCODING_UTF_16_BE, ENCODING_UTF_8, ENCODING_LATIN_1 })
    public void testCompressedAttachedPictureFrame(byte encoding) {

        byte[] pictureBytes = ImageReader.readFromFile(new File("/home/rrtry/IdeaProjects/JTagger/tests/files/apic_test.jpg"));
        FrameHeader frameHeader = FrameHeader.newBuilder(ID3V2_4)
                .setIdentifier("APIC", ID3V2_4)
                .setCompressed(true)
                .build(ID3V2_4);

        AttachedPictureFrame pictureFrame = AttachedPictureFrame.newBuilder()
                .setHeader(frameHeader)
                .setEncoding(encoding)
                .setDescription("Front cover")
                .setMimeType("image/jpeg")
                .setPictureType(PICTURE_TYPE_FRONT_COVER)
                .setPictureData(pictureBytes)
                .build(ID3V2_4);

        Assertions.assertTrue(pictureFrame.getHeader().isFrameCompressed());

        byte[] frameBytes = pictureFrame.getBytes();
        frameBytes = AbstractFrame.decompressFrame(
                Arrays.copyOfRange(frameBytes, frameHeader.getFrameDataOffset(), frameBytes.length)
        );

        AttachedPictureFrameParser parser = new AttachedPictureFrameParser();
        AttachedPictureFrame parsedFrame  = parser.parse("APIC", pictureFrame.getHeader(), frameBytes, new TagHeader());

        Assertions.assertEquals("APIC", parsedFrame.getIdentifier());
        Assertions.assertEquals(encoding, parsedFrame.getEncoding());
        Assertions.assertEquals("Front cover", parsedFrame.getDescription());
        Assertions.assertEquals("image/jpeg", parsedFrame.getMimeType());
        Assertions.assertEquals(PICTURE_TYPE_FRONT_COVER, parsedFrame.getPictureType());
        Assertions.assertArrayEquals(pictureBytes, parsedFrame.getPictureData());
    }

    @ParameterizedTest
    @ValueSource(bytes = { ENCODING_UTF_16, ENCODING_UTF_16_BE, ENCODING_UTF_8, ENCODING_LATIN_1 })
    public void testAttachedPictureFrame(byte encoding) {

        byte[] pictureBytes = ImageReader.readFromFile(new File("/home/rrtry/IdeaProjects/JTagger/tests/files/apic_test.jpg"));
        AttachedPictureFrame pictureFrame = AttachedPictureFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("APIC", ID3V2_4))
                .setEncoding(encoding)
                .setDescription("Front cover")
                .setMimeType("image/jpeg")
                .setPictureType(PICTURE_TYPE_FRONT_COVER)
                .setPictureData(pictureBytes)
                .build(ID3V2_4);

        AttachedPictureFrameParser parser = new AttachedPictureFrameParser();
        AttachedPictureFrame parsedFrame  = parser.parse("APIC", pictureFrame.getHeader(), pictureFrame.getBytes(), new TagHeader());

        Assertions.assertEquals("APIC", parsedFrame.getIdentifier());
        Assertions.assertEquals(encoding, parsedFrame.getEncoding());
        Assertions.assertEquals("Front cover", parsedFrame.getDescription());
        Assertions.assertEquals("image/jpeg", parsedFrame.getMimeType());
        Assertions.assertEquals(PICTURE_TYPE_FRONT_COVER, parsedFrame.getPictureType());
        Assertions.assertArrayEquals(pictureBytes, parsedFrame.getPictureData());
    }

    @ParameterizedTest
    @ValueSource(bytes = { ENCODING_UTF_16, ENCODING_UTF_16_BE, ENCODING_UTF_8, ENCODING_LATIN_1 })
    public void testCompressedCommentFrame(byte encoding) {

        FrameHeader frameHeader = FrameHeader.newBuilder(ID3V2_4)
                .setCompressed(true)
                .setIdentifier("COMM", ID3V2_4)
                .build(ID3V2_4);

        CommentFrame commentFrame = CommentFrame.newBuilder()
                .setHeader(frameHeader)
                .setEncoding(encoding)
                .setDescription("Test description")
                .setText("Test comment")
                .setLanguage("eng")
                .build(ID3V2_4);

        Assertions.assertTrue(commentFrame.getHeader().isFrameCompressed());

        byte[] frameBytes = commentFrame.getBytes();
        frameBytes = AbstractFrame.decompressFrame(
                Arrays.copyOfRange(frameBytes, frameHeader.getFrameDataOffset(), frameBytes.length)
        );

        CommentFrameParser parser = new CommentFrameParser();
        CommentFrame parsedFrame  = parser.parse("COMM", commentFrame.getHeader(), frameBytes, new TagHeader());

        Assertions.assertEquals(commentFrame.getIdentifier(), parsedFrame.getIdentifier());
        Assertions.assertEquals(commentFrame.getEncoding(), parsedFrame.getEncoding());
        Assertions.assertEquals(commentFrame.getDescription(), parsedFrame.getDescription());
        Assertions.assertEquals(commentFrame.getLanguage(), parsedFrame.getLanguage());
        Assertions.assertEquals(commentFrame.getText(), parsedFrame.getText());
        Assertions.assertEquals(ID3V2_4, parsedFrame.getHeader().getVersion());
    }

    @ParameterizedTest
    @ValueSource(bytes = { ENCODING_UTF_16, ENCODING_UTF_16_BE, ENCODING_UTF_8, ENCODING_LATIN_1 })
    public void testCommentFrame(byte encoding) {

        CommentFrame commentFrame = CommentFrame.newBuilder()
                .setHeader(FrameHeader.createFrameHeader("COMM", ID3V2_4))
                .setEncoding(encoding)
                .setDescription("Test description")
                .setText("Test comment")
                .setLanguage("eng")
                .build(ID3V2_4);

        CommentFrameParser parser = new CommentFrameParser();
        CommentFrame parsedFrame  = parser.parse("COMM", commentFrame.getHeader(), commentFrame.getBytes(), new TagHeader());

        Assertions.assertEquals(commentFrame.getIdentifier(), parsedFrame.getIdentifier());
        Assertions.assertEquals(commentFrame.getEncoding(), parsedFrame.getEncoding());
        Assertions.assertEquals(commentFrame.getDescription(), parsedFrame.getDescription());
        Assertions.assertEquals(commentFrame.getLanguage(), parsedFrame.getLanguage());
        Assertions.assertEquals(commentFrame.getText(), parsedFrame.getText());
        Assertions.assertEquals(ID3V2_4, parsedFrame.getHeader().getVersion());
    }

    @Test
    public void testCommentFrameIdentifier() {
        try {

            CommentFrame.newBuilder()
                    .setHeader(FrameHeader.createFrameHeader("SYLT", ID3V2_4));

        } catch (IllegalArgumentException e) {
            Assertions.assertNotNull(e);
        }
    }

    @Test
    public void testCommentFrameLanguage() {
        try {

            CommentFrame.newBuilder()
                    .setLanguage("LANGUAGE");

        } catch (IllegalArgumentException e) {
            Assertions.assertNotNull(e);
        }
    }

    @Test
    public void testCommentFrameDescription() {
        try {

            CommentFrame.newBuilder()
                    .setDescription("A".repeat(65));

        } catch (IllegalArgumentException e) {
            Assertions.assertNotNull(e);
        }
    }

    @ParameterizedTest
    @ValueSource(bytes = { ENCODING_UTF_16, ENCODING_UTF_16_BE, ENCODING_UTF_8 })
    public void testCompressedSynchronisedLyricsFrame(byte encoding) throws IOException {

        HashMap<Integer, String> lyrics = LRCParser.parseSynchronisedLyrics(
                new File("/home/rrtry/IdeaProjects/JTagger/tests/files/test.lrc")
        );

        FrameHeader header = FrameHeader.newBuilder(ID3V2_4)
                .setIdentifier("SYLT", ID3V2_4)
                .setCompressed(true)
                .build(ID3V2_4);

        SynchronisedLyricsFrame frame = SynchronisedLyricsFrame.newBuilder()
                .setHeader(header)
                .setEncoding(encoding)
                .setDescription("Synchronised lyrics")
                .setLanguage("eng")
                .setLyrics(lyrics)
                .build(ID3V2_4);

        Assertions.assertTrue(frame.getHeader().isFrameCompressed());

        byte[] frameBytes = frame.getBytes();
        frameBytes = AbstractFrame.decompressFrame(
                Arrays.copyOfRange(frameBytes, header.getFrameDataOffset(), frameBytes.length)
        );

        Assertions.assertEquals(lyrics, frame.getFrameData());
        SynchronisedLyricsFrameParser parser = new SynchronisedLyricsFrameParser();
        SynchronisedLyricsFrame parsedFrame  = parser.parse(S_LYRICS, header, frameBytes, new TagHeader());

        Assertions.assertEquals("Synchronised lyrics", parsedFrame.getDescription());
        Assertions.assertEquals("eng", parsedFrame.getLanguage());
        Assertions.assertEquals(frame.getFrameData(), parsedFrame.getFrameData());
    }

    @ParameterizedTest
    @ValueSource(bytes = { ENCODING_UTF_16, ENCODING_UTF_16_BE, ENCODING_UTF_8 })
    public void testSynchronisedLyricsFrame(byte encoding) throws IOException {

        HashMap<Integer, String> lyrics = LRCParser.parseSynchronisedLyrics(
                new File("/home/rrtry/IdeaProjects/JTagger/tests/files/test.lrc")
        );

        FrameHeader header = FrameHeader.createFrameHeader(S_LYRICS, ID3V2_4);
        SynchronisedLyricsFrame frame = SynchronisedLyricsFrame.newBuilder()
                .setHeader(header)
                .setEncoding(encoding)
                .setDescription("Synchronised lyrics")
                .setLanguage("eng")
                .setLyrics(lyrics)
                .build(ID3V2_4);

        byte[] frameBytes = frame.getBytes();

        Assertions.assertEquals(lyrics, frame.getFrameData());
        SynchronisedLyricsFrameParser parser = new SynchronisedLyricsFrameParser();
        SynchronisedLyricsFrame parsedFrame  = parser.parse(S_LYRICS, header, frameBytes, new TagHeader());

        Assertions.assertEquals("Synchronised lyrics", parsedFrame.getDescription());
        Assertions.assertEquals("eng", parsedFrame.getLanguage());
        Assertions.assertEquals(frame.getFrameData(), parsedFrame.getFrameData());
    }

    @Test
    public void testSynchronisedLyricsFrameIdentifier() {
        try {

            SynchronisedLyricsFrame.newBuilder()
                    .setHeader(FrameHeader.createFrameHeader("COMM", ID3V2_4));

        } catch (IllegalArgumentException e) {
            Assertions.assertNotNull(e);
        }
    }

    @Test
    public void testSynchronisedLyricsFrameLanguage() {
        try {

            SynchronisedLyricsFrame.newBuilder()
                    .setLanguage("LANGUAGE");

        } catch (IllegalArgumentException e) {
            Assertions.assertNotNull(e);
        }
    }

    @Test
    public void testSynchronisedLyricsFrameDescription() {
        try {

            SynchronisedLyricsFrame.newBuilder()
                    .setDescription("A".repeat(65));

        } catch (IllegalArgumentException e) {
            Assertions.assertNotNull(e);
        }
    }
}
