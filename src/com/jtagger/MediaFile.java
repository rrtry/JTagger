package com.jtagger;

import com.jtagger.flac.FlacParser;
import com.jtagger.flac.FlacTag;
import com.jtagger.flac.FlacTagEditor;
import com.jtagger.mp3.MpegStreamInfoParser;
import com.jtagger.mp3.id3.ID3V2Tag;
import com.jtagger.mp3.id3.ID3V2TagEditor;
import com.jtagger.mp4.MP4;
import com.jtagger.mp4.MP4Editor;
import com.jtagger.mp4.MP4Parser;
import com.jtagger.ogg.OggTagEditor;
import com.jtagger.ogg.opus.OggOpusParser;
import com.jtagger.ogg.opus.OggOpusTagEditor;
import com.jtagger.ogg.vorbis.OggVorbisParser;
import com.jtagger.ogg.vorbis.OggVorbisTagEditor;
import com.jtagger.ogg.vorbis.VorbisComments;
import com.jtagger.utils.FileContentTypeDetector;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.jtagger.utils.FileContentTypeDetector.*;

public class MediaFile<T extends AbstractTag, I extends StreamInfo> {

    protected RandomAccessFile file;

    protected AbstractTagEditor<T> tagEditor;
    protected StreamInfoParser<I> streamInfoParser;

    protected T tag;
    protected I streamInfo;

    public void scan(File fileObj) throws IOException {

        if (file != null) file.close();
        if (!fileObj.exists()) return;

        file = new RandomAccessFile(fileObj.getAbsolutePath(), "rw");
        String mimeType = FileContentTypeDetector.getFileContentType(file);

        if (mimeType == null) {
            file.close();
            return;
        }

        tagEditor = getEditor(mimeType);
        if (tagEditor == null) {
            return;
        }

        tagEditor.load(file, mimeType);
        tag              = tagEditor.getTag();
        streamInfoParser = getParser(mimeType);
        streamInfo       = streamInfoParser.parseStreamInfo(file);
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
        if (mimeType.equals(MPEG_MIME_TYPE))       return new MpegStreamInfoParser((ID3V2Tag) tag);
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
        return tag;
    }

    public I getStreamInfo() {
        return streamInfo;
    }

    public void save() throws IOException {
        tagEditor.commit();
    }

    public void close() throws IOException {
        tagEditor.release();
    }
}
