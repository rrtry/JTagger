package com.jtagger;

import com.jtagger.flac.FlacTagEditor;
import com.jtagger.mp3.MpegStreamInfo;
import com.jtagger.mp3.MpegStreamInfoParser;
import com.jtagger.mp3.id3.ID3V2Tag;
import com.jtagger.mp3.id3.ID3V2TagEditor;
import com.jtagger.mp4.MP4Editor;
import com.jtagger.ogg.OggTagEditor;
import com.jtagger.ogg.opus.OggOpusTagEditor;
import com.jtagger.ogg.vorbis.OggVorbisTagEditor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.jtagger.ogg.opus.OggOpusParser.OPUS_IDENTIFICATION_HEADER_MAGIC;
import static com.jtagger.ogg.vorbis.VorbisHeader.VORBIS_HEADER_MAGIC;
import static com.jtagger.MediaFile.FileContentTypeDetector.*;

public class MediaFile<T extends AbstractTag, I extends StreamInfo> implements AutoCloseable {

    protected AbstractTagEditor<T> tagEditor;
    protected StreamInfoParser<I> streamInfoParser;
    protected I streamInfo;

    private final FileContentTypeDetector contentTypeDetector = new FileContentTypeDetector();

    public void scan(File fileObj) throws IOException {

        if (tagEditor != null) close();
        if (!fileObj.isFile()) return;

        RandomAccessFile file = new RandomAccessFile(fileObj.getAbsolutePath(), "rw");
        String mimeType       = contentTypeDetector.getFileContentType(file);

        if (mimeType == null) {
            file.close();
            return;
        }

        tagEditor = getEditor(mimeType);
        if (tagEditor == null) return;
        tagEditor.load(file, mimeType);

        if (streamInfo == null) {
            streamInfoParser = getParser(mimeType);
            streamInfo       = streamInfoParser.parseStreamInfo(file);
        }
    }

    @SuppressWarnings("rawtypes")
    protected AbstractTagEditor getEditor(String mimeType) {
        if (mimeType.equals(MPEG_MIME_TYPE))       return new ID3V2TagEditor();
        if (mimeType.equals(FLAC_MIME_TYPE))       return new FlacTagEditor();
        if (mimeType.equals(OGG_VORBIS_MIME_TYPE)) return new OggVorbisTagEditor();
        if (mimeType.equals(OGG_OPUS_MIME_TYPE))   return new OggOpusTagEditor();
        if (mimeType.equals(M4A_MIME_TYPE))        return new MP4Editor();
        return null;
    }

    @SuppressWarnings("rawtypes")
    protected StreamInfoParser getParser(String mimeType) {
        if (mimeType.equals(MPEG_MIME_TYPE))       return new MpegStreamInfoParser((ID3V2Tag) getTag());
        if (mimeType.equals(FLAC_MIME_TYPE))       return ((FlacTagEditor) tagEditor).getParser();
        if (mimeType.equals(M4A_MIME_TYPE))        return ((MP4Editor) tagEditor).getParser();
        if (mimeType.equals(OGG_VORBIS_MIME_TYPE)) return (StreamInfoParser) ((OggTagEditor) tagEditor).getParser();
        if (mimeType.equals(OGG_OPUS_MIME_TYPE))   return (StreamInfoParser) ((OggTagEditor) tagEditor).getParser();
        return null;
    }

    public void setTag(T tag) {
        tagEditor.setTag(tag);
    }

    public void removeTag() {
        tagEditor.removeTag();
    }

    public T getTag() {
        return tagEditor != null ? tagEditor.getTag() : null;
    }

    public I getStreamInfo() {
        return streamInfo;
    }

    public void save() throws IOException {
        tagEditor.commit();
    }

    @Override
    public void close() throws IOException {
        if (tagEditor != null) tagEditor.release();
        tagEditor  = null;
        streamInfo = null;
    }

    public class FileContentTypeDetector {

        public static final String MPEG_MIME_TYPE       = "audio/mpeg";
        public static final String FLAC_MIME_TYPE       = "audio/flac";
        public static final String OGG_VORBIS_MIME_TYPE = "audio/x-vorbis+ogg";
        public static final String OGG_OPUS_MIME_TYPE   = "audio/x-opus+ogg";
        public static final String M4A_MIME_TYPE        = "audio/m4a";

        private FileContentTypeDetector() {

        }

        @SuppressWarnings("unchecked")
        public String getFileContentType(RandomAccessFile file) throws IOException {

            final String oggMagic    = "OggS";
            final String id3Magic    = "ID3";
            final String flacMagic   = "fLaC";
            final String m4aMagic    = "ftypM4A";
            final String[] mpegMagic = new String[] { "ÿû", "ÿó", "ÿò" };

            byte[] buffer = new byte[20];
            file.read(buffer);
            file.seek(0);

            final String signature = new String(buffer, StandardCharsets.ISO_8859_1);
            if (signature.startsWith(id3Magic))  return FileContentTypeDetector.MPEG_MIME_TYPE;
            if (signature.startsWith(flacMagic)) return FileContentTypeDetector.FLAC_MIME_TYPE;
            if (signature.contains(m4aMagic))    return FileContentTypeDetector.M4A_MIME_TYPE;

            if (Arrays.asList(mpegMagic).contains(signature)) {
                return FileContentTypeDetector.MPEG_MIME_TYPE;
            }
            if (signature.startsWith(oggMagic)) {

                file.seek(26);
                int segments = file.readUnsignedByte();
                if (segments > 255 || segments < 0) return null;

                file.skipBytes(segments); // skip segments
                byte[] headerMagic = new byte[OPUS_IDENTIFICATION_HEADER_MAGIC.length];
                file.read(headerMagic);

                if (Arrays.equals(headerMagic, OPUS_IDENTIFICATION_HEADER_MAGIC)) {
                    file.seek(0); return FileContentTypeDetector.OGG_OPUS_MIME_TYPE;
                }

                file.seek(file.getFilePointer() - headerMagic.length + 1); // +1 skips header type
                headerMagic = new byte[VORBIS_HEADER_MAGIC.length];
                file.read(headerMagic);

                if (Arrays.equals(headerMagic, VORBIS_HEADER_MAGIC)) {
                    file.seek(0); return FileContentTypeDetector.OGG_VORBIS_MIME_TYPE;
                }
            }

            MpegStreamInfoParser parser   = new MpegStreamInfoParser();
            MpegStreamInfo mpegStreamInfo = parser.parseStreamInfo(file);

            if (mpegStreamInfo != null) {
                streamInfo       = (I) mpegStreamInfo;
                streamInfoParser = (StreamInfoParser<I>) parser;
                file.seek(0); return FileContentTypeDetector.MPEG_MIME_TYPE;
            }
            return null;
        }
    }
}
