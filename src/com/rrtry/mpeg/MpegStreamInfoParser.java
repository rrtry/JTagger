package com.rrtry.mpeg;

import com.rrtry.StreamInfoParser;
import com.rrtry.mpeg.id3.ID3V2Tag;

import java.io.RandomAccessFile;
import java.util.ArrayList;

public class MpegStreamInfoParser implements StreamInfoParser<MpegStreamInfo> {

    private ID3V2Tag tag;

    public MpegStreamInfoParser() {

    }

    public MpegStreamInfoParser(ID3V2Tag tag) {
        this.tag = tag;
    }

    @Override
    public MpegStreamInfo parseStreamInfo(RandomAccessFile file) {

        MpegFrameParser mpegFrameParser   = new MpegFrameParser(tag);
        XingHeaderParser xingHeaderParser = new XingHeaderParser();

        mpegFrameParser.parseFrames(file);
        ArrayList<MpegFrame> frames = mpegFrameParser.getFrames();
        MpegFrame mpegFrame         = frames.get(0);

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

        int duration;
        if (mpegStreamInfo.isVBR()) {

            VBRHeader vbrHeader = xingHeader != null ? xingHeader : vbriHeader;
            if (vbrHeader == null) return null;

            int samplesPerFrame = mpegHeader.getSamplesPerFrame();
            int sampleRate      = mpegHeader.getSampleRate();
            int totalFrames     = vbrHeader.getTotalFrames();

            duration = (samplesPerFrame * totalFrames) / sampleRate;
        } else {
            duration = mpegFrameParser.getTotalDuration();
        }

        mpegStreamInfo.setDuration(duration);
        return mpegStreamInfo;
    }
}
