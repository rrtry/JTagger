package com.rrtry.mpeg;

import com.rrtry.StreamInfoParser;
import com.rrtry.mpeg.id3.ID3V2Tag;

import java.io.RandomAccessFile;

public class MpegStreamInfoParser implements StreamInfoParser<MpegStreamInfo> {

    private ID3V2Tag tag;

    public MpegStreamInfoParser() {

    }

    public MpegStreamInfoParser(ID3V2Tag tag) {
        this.tag = tag;
    }

    @Override
    public MpegStreamInfo parseStreamInfo(RandomAccessFile file) {

        MpegFrameHeaderParser mpegHeaderParser = new MpegFrameHeaderParser(tag);
        XingHeaderParser xingHeaderParser      = new XingHeaderParser();
        VBRIHeaderParser vbriHeaderParser      = new VBRIHeaderParser();

        MpegFrameHeader mpegHeader = mpegHeaderParser.parse(file);
        XingHeader xingHeader      = xingHeaderParser.parse(file, mpegHeader);
        VBRIHeader vbriHeader      = vbriHeaderParser.parse(file, mpegHeader);

        MpegStreamInfo.Builder builder = MpegStreamInfo.newBuilder();
        builder = builder.setMpegHeader(mpegHeader);

        if (xingHeader != null) builder = builder.setXingHeader(xingHeader);
        if (vbriHeader != null) builder = builder.setVBRIHeader(vbriHeader);

        MpegStreamInfo mpegStreamInfo = builder.build();

        int duration;
        if (mpegStreamInfo.isVBR()) {

            VBRHeader vbrHeader = xingHeader != null ? xingHeader : vbriHeader;
            if (vbrHeader == null) return null;

            int samplesPerFrame = mpegHeader.getSamplesPerFrame();
            int sampleRate      = mpegHeader.getSampleRate();
            int totalFrames     = vbrHeader.getTotalFrames();

            duration = (samplesPerFrame * totalFrames) / sampleRate;
        } else {
            duration = mpegHeaderParser.getDuration(file);
        }

        mpegStreamInfo.setDuration(duration);
        return mpegStreamInfo;
    }
}
