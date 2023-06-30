package com.jtagger.mp3;

import com.jtagger.StreamInfoParser;
import com.jtagger.mp3.id3.ID3V1Tag;
import com.jtagger.mp3.id3.ID3V2Tag;

import java.io.IOException;
import java.io.RandomAccessFile;

public class MpegStreamInfoParser implements StreamInfoParser<MpegStreamInfo> {

    private ID3V2Tag tag;

    public MpegStreamInfoParser() {

    }

    public MpegStreamInfoParser(ID3V2Tag tag) {
        this.tag = tag;
    }

    private static boolean isID3V1Present(RandomAccessFile file) throws IOException {

        byte[] magic = new byte[3];
        file.seek(file.length() - 128);
        file.read(magic, 0, magic.length);

        return ID3V1Tag.ID.equals(new String(magic));
    }

    @Override
    public MpegStreamInfo parseStreamInfo(RandomAccessFile file) throws IOException {

        MpegFrameParser mpegFrameParser   = new MpegFrameParser(tag);
        XingHeaderParser xingHeaderParser = new XingHeaderParser();

        mpegFrameParser.parseFrame(file);

        MpegFrame mpegFrame        = mpegFrameParser.getMpegFrame();
        MpegFrameHeader mpegHeader = mpegFrame.getMpegHeader();
        XingHeader xingHeader      = xingHeaderParser.parse(mpegFrame);
        VBRIHeader vbriHeader      = null;

        if (xingHeader == null) {
            VBRIHeaderParser vbriHeaderParser = new VBRIHeaderParser();
            vbriHeader = vbriHeaderParser.parse(mpegFrame);
        }

        MpegStreamInfo.Builder builder = MpegStreamInfo.newBuilder();
        builder = builder.setMpegHeader(mpegFrame.getMpegHeader());

        if (xingHeader != null) builder = builder.setXingHeader(xingHeader);
        if (vbriHeader != null) builder = builder.setVBRIHeader(vbriHeader);

        MpegStreamInfo mpegStreamInfo = builder.build();

        final int duration;
        final int samplesPerFrame = mpegHeader.getSamplesPerFrame();
        final int sampleRate      = mpegHeader.getSampleRate();

        if (mpegStreamInfo.isVBR()) {

            VBRHeader vbrHeader = xingHeader != null ? xingHeader : vbriHeader;
            if (vbrHeader == null) return null;
            duration = (samplesPerFrame * vbrHeader.getTotalFrames()) / sampleRate;

        } else {

            int tagSize = tag != null ? tag.getTagHeader().getTagSize() + 10 : 0;
            if (tagSize != 0 && tag.getTagHeader().hasFooter()) {
                tagSize += 10;
            }
            if (isID3V1Present(file)) {
                tagSize += 128;
            }

            int length      = (int) file.length() - tagSize;
            int frameLength = mpegFrame.getFrameBody().length;
            float frameTime = (float) mpegHeader.getSamplesPerFrame() / mpegHeader.getSampleRate();

            duration = (int) ((length / frameLength) * frameTime);
        }

        mpegStreamInfo.setDuration(duration);
        return mpegStreamInfo;
    }
}
