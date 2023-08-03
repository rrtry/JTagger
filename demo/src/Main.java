import com.jtagger.*;
import com.jtagger.mp3.MpegFile;
import com.jtagger.mp3.MpegFrameHeader;
import com.jtagger.mp3.MpegStreamInfo;
import com.jtagger.mp3.id3.*;
import com.jtagger.utils.IntegerUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

import static java.lang.Byte.toUnsignedInt;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Main {

    private static void getFrames(File fileObj) throws IOException {

        MpegFile mpegFile = new MpegFile();
        mpegFile.scan(fileObj);

        ID3V2Tag tag = mpegFile.getTag();
        for (AbstractFrame frame : tag.getFrames()) {
            System.out.println(frame.toString());
        }
    }

    private static void addSynchLyrics(File fileObj, HashMap<Integer, String> lyrics) throws IOException {

        MpegFile mpegFile = new MpegFile();
        mpegFile.scan(fileObj);

        ID3V2Tag tag = mpegFile.getTag();
        SynchronisedLyricsFrame lyricsFrame = SynchronisedLyricsFrame.createInstance(
                lyrics, "ENG",  tag.getVersion()
        );

        tag.setFrame(lyricsFrame);

        mpegFile.setTag(tag);
        mpegFile.save();
        mpegFile.close();
    }

    private static void removeSynchLyrics(File fileObj) throws IOException {

        MpegFile mpegFile = new MpegFile();
        mpegFile.scan(fileObj);

        ID3V2Tag tag = mpegFile.getTag();
        tag.removeFrame(AbstractFrame.U_LYRICS);

        mpegFile.setTag(tag);
        mpegFile.save();
        mpegFile.close();
    }

    private static void setAlbumCoverFromArray(File fileObj, byte[] bytes) throws IOException {

        MediaFile mediaFile = new MediaFile();
        mediaFile.scan(fileObj);

        AbstractTag tag         = mediaFile.getTag();
        AttachedPicture picture = new AttachedPicture();

        picture.setPictureData(bytes);
        tag.setPictureField(picture);

        mediaFile.setTag(tag);
        mediaFile.save();
        mediaFile.close();
    }

    private static void setAlbumCoverFromFile(File fileObj, String[] args) throws IOException {

        MediaFile mediaFile = new MediaFile();
        mediaFile.scan(fileObj);

        AbstractTag tag         = mediaFile.getTag();
        AttachedPicture picture = new AttachedPicture();

        picture.setPictureData(new File(args[1]));
        tag.setPictureField(picture);

        mediaFile.setTag(tag);
        mediaFile.save();
        mediaFile.close();
    }

    private static void setAlbumCoverFromURL(File fileObj, String[] args) throws IOException {

        MediaFile mediaFile = new MediaFile();
        mediaFile.scan(fileObj);

        AbstractTag tag         = mediaFile.getTag();
        AttachedPicture picture = new AttachedPicture();

        picture.setPictureData(new URL(args[1]));
        tag.setPictureField(picture);

        mediaFile.setTag(tag);
        mediaFile.save();
        mediaFile.close();
    }

    private static void copyTag(File from, File to) throws IOException {

        MediaFile mediaFile = new MediaFile();
        mediaFile.scan(from);

        AbstractTag tag = mediaFile.getTag();
        mediaFile.scan(to);

        mediaFile.setTag(tag);
        mediaFile.save();
        mediaFile.close();
    }

    private static void writeTag(
            File fileObj,
            HashMap<String, String> fields,
            AttachedPicture picture) throws IOException
    {

        MediaFile mediaFile = new MediaFile<>();
        mediaFile.scan(fileObj);

        Tag tag = new Tag();
        for (String field : fields.keySet()) {
            tag.setStringField(field, fields.get(field));
        }

        if (picture != null) {
            tag.setPictureField(picture);
        }

        mediaFile.setTag(tag);
        mediaFile.save();
        mediaFile.close();
    }

    private static void removeTag(File fileObj) throws IOException {

        MediaFile mediaFile = new MediaFile<>();
        mediaFile.scan(fileObj);

        mediaFile.removeTag();
        mediaFile.save();
        mediaFile.close();
    }

    private static void parseMediaFile(File fileObj) throws IOException {

        System.out.println(fileObj.getName());
        MediaFile mediaFile = new MediaFile<>();
        mediaFile.scan(fileObj);

        AbstractTag tag       = mediaFile.getTag();
        StreamInfo streamInfo = mediaFile.getStreamInfo();

        if (tag != null) {
            for (String field : AbstractTag.FIELDS) {

                if (field.equals(AbstractTag.PICTURE)) continue;
                String value = tag.getStringField(field);

                if (value == null) continue;
                if (value.isEmpty()) continue;

                try {
                    if (field.equals(AbstractTag.ID3_GENRE)) {
                        System.out.printf("\t%s =  %s\n", field, ID3V1Tag.GENRES[Integer.parseInt(value)]);
                    }
                } catch (NumberFormatException ignored) {

                }
                System.out.printf("\t%s = %s\n", field, value);
            }
        }

        if (streamInfo == null) {
            System.err.println(fileObj.getName() + " is invalid");
            return;
        }

        System.out.println();
        int duration = streamInfo.getDuration();

        System.out.printf("\tDuration: %02d:%02d\n", duration / 60, duration % 60);
        System.out.printf("\tSampleRate: %d HZ\n", streamInfo.getSampleRate());
        System.out.printf("\tBitRate: %d kbps\n", streamInfo.getBitrate());
        System.out.printf("\tChannels: %d\n", streamInfo.getChannelCount());

        mediaFile.close();
    }

    private static void parseFiles(File directory, boolean recursive) throws IOException {

        File[] files = directory.listFiles();
        assert files != null;

        for (File file : files) {
            if (file.isFile()) {
                parseMediaFile(file);
            }
            else if (file.isDirectory() && recursive) parseFiles(file, true);
        }
    }

    private static void clearAllTags(File directory) throws IOException {

        if (!directory.isDirectory()) {
            System.err.println("Specify a directory");
            return;
        }

        File[] files = directory.listFiles();
        assert files != null;

        MediaFile<AbstractTag, StreamInfo> mediaFile = new MediaFile<>();
        for (File file : files) {
            mediaFile.scan(file);
            mediaFile.removeTag();
            mediaFile.save();
            mediaFile.close();
        }
    }

    private static void copyTagFrom(File directory, File targetFile) throws IOException {

        if (!directory.isDirectory() || !targetFile.isFile()) {
            System.out.println("JTagger usage: <dir> <file>");
            return;
        }

        MediaFile<AbstractTag, StreamInfo> mediaFile;
        AbstractTag tag;

        mediaFile = new MediaFile<>();
        mediaFile.scan(targetFile);
        tag = mediaFile.getTag();

        File[] files = directory.listFiles();
        assert files != null;
        assert tag   != null;

        for (File file : files) {
            mediaFile.scan(file);
            mediaFile.setTag(tag);
            mediaFile.save();
            mediaFile.close();
        }
    }

    public static void main(String[] args) throws IOException {
        parseMediaFile(new File(args[0]));
    }
}