import com.rrtry.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Main {

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

        MediaFile mediaFile = new MediaFile<>();
        mediaFile.scan(fileObj);

        AbstractTag tag       = mediaFile.getTag();
        StreamInfo streamInfo = mediaFile.getStreamInfo();
        System.out.println(fileObj.getName());

        if (tag != null) {

            for (String field : AbstractTag.FIELDS) {

                if (field.equals(AbstractTag.PICTURE)) continue;
                String value = tag.getStringField(field);

                if (value == null) continue;
                if (value.isEmpty()) continue;

                System.out.printf("\t%s=%s\n", field, value);
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
        }

        if (streamInfo == null) return;

        System.out.println();
        int duration = streamInfo.getDuration();

        System.out.printf("\tDuration: %02d:%02d\n", duration / 60, duration % 60);
        System.out.printf("\tSampleRate: %d HZ\n", streamInfo.getSampleRate());
        System.out.printf("\tBitRate: %d kbps\n", streamInfo.getBitrate());
        System.out.printf("\tChannels: %d\n", streamInfo.getChannelCount());

        mediaFile.close();
    }

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.out.println("Usage: Main <file_path>");
            return;
        }

        File file = new File(args[0]);
        if (!file.isFile()) return;
        parseMediaFile(file);
    }
}