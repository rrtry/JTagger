import com.jtagger.*;
import com.jtagger.mp3.MpegFile;
import com.jtagger.mp3.id3.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

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

    private static boolean parseMediaFile(File fileObj) throws IOException {

        MediaFile mediaFile = new MediaFile<>();
        mediaFile.scan(fileObj);

        AbstractTag tag       = mediaFile.getTag();
        StreamInfo streamInfo = mediaFile.getStreamInfo();
        System.out.println(fileObj.getName());

        if (tag == null) {
            return false;
        }

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

        AttachedPicture picture = tag.getPictureField();
        if (picture != null) {

            final String mimeType  = picture.getMimeType();
            final String extension = "." + mimeType.substring(mimeType.indexOf("/") + 1);
            final String path      = System.getProperty("user.home") + File.separator + "cover" + extension;

            FileOutputStream out = new FileOutputStream(path);
            out.write(picture.getPictureData());
            out.close();
        }

        if (streamInfo == null) {
            return false;
        }

        System.out.println();
        int duration = streamInfo.getDuration();

        System.out.printf("\tDuration: %02d:%02d\n", duration / 60, duration % 60);
        System.out.printf("\tSampleRate: %d HZ\n", streamInfo.getSampleRate());
        System.out.printf("\tBitRate: %d kbps\n", streamInfo.getBitrate());
        System.out.printf("\tChannels: %d\n", streamInfo.getChannelCount());

        mediaFile.close();
        return true;
    }

    private static void parseFiles(File directory) throws IOException {

        File[] files = directory.listFiles();
        assert files != null;

        for (File file : files) {
            if (file.isFile()) parseMediaFile(file);
            else parseFiles(file);
        }
    }

    public static void main(String[] args) throws IOException {

        File mp3Dir = new File(args[0]);
        File m4aDir = new File(args[1]);

        if (!mp3Dir.isDirectory() || !m4aDir.isDirectory()) {
            System.err.println("Specify a directory");
            return;
        }

        File[] mp3Files = mp3Dir.listFiles();
        File[] m4aFiles = m4aDir.listFiles();

        assert mp3Files != null;
        assert m4aFiles != null;

        System.out.println(mp3Files.length);
        System.out.println(m4aFiles.length);

        MediaFile mediaFile = new MediaFile();
        AbstractTag tag;
        AttachedPicture picture;

        for (File m4a : m4aFiles) {

            int index      = m4a.getName().indexOf(".");
            String m4aName = m4a.getName().substring(0, index);

            for (File mp3 : mp3Files) {

                index = mp3.getName().indexOf(".");
                String mp3Name = mp3.getName().substring(0, index);

                if (m4aName.equals(mp3Name)) {

                    System.out.println(m4a.getName());
                    mediaFile.scan(mp3);
                    tag = mediaFile.getTag();
                    if (tag == null) break;

                    picture = tag.getPictureField();
                    if (picture == null) break;

                    mediaFile.scan(m4a);
                    tag = mediaFile.getTag();
                    tag.setPictureField(picture);

                    mediaFile.setTag(tag);
                    mediaFile.save();
                    mediaFile.close();
                }
            }
        }
    }
}