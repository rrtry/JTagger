package com.jtagger.mp3;

import com.jtagger.StreamInfoParser;
import com.jtagger.mp3.id3.ID3V1Tag;
import com.jtagger.mp3.id3.ID3V2Tag;

import java.io.IOException;
import java.util.Arrays;

import com.jtagger.FileWrapper;

import static com.jtagger.mp3.MpegFrameHeader.CHANNEL_MODE_SINGLE_CHANNEL;
import static com.jtagger.mp3.MpegFrameHeader.MPEG_VERSION_1;
import static com.jtagger.mp3.MpegFrameParser.getFrameSize;
import static com.jtagger.mp3.XingHeader.*;
import static com.jtagger.mp3.XingHeader.FLAG_QUALITY;
import static com.jtagger.utils.IntegerUtils.*;
import static com.jtagger.utils.IntegerUtils.toUInt16BE;
import static java.lang.Byte.toUnsignedInt;
import static java.lang.Float.intBitsToFloat;
import static java.lang.Integer.toUnsignedLong;

public class MpegStreamInfoParser implements StreamInfoParser<MpegStreamInfo> {

    private ID3V2Tag tag;

    public MpegStreamInfoParser() {

    }

    public MpegStreamInfoParser(ID3V2Tag tag) {
        this.tag = tag;
    }

    private LAMEHeader parseLAMEHeader(byte[] frame, int offset) {

        if (offset + 9 >= frame.length) {
            return null;
        }

        String version = new String(Arrays.copyOfRange(frame, offset, offset += 9));
        int tagVersion = frame[offset] >> 4;
        int vbrMethod  = frame[offset++] & 0xF;
        int lowpassFlt = toUnsignedInt(frame[offset++]);

        long replayGain   = toUInt64BE(Arrays.copyOfRange(frame, offset, offset += 8));
        int encodingFlags = frame[offset] >> 4;
        int lameAthType   = frame[offset++] & 0xF;
        int bitrate       = toUnsignedInt(frame[offset++]);
        int encoderDelays = toUInt24BE(Arrays.copyOfRange(frame, offset, offset += 3));
        int startDelay    = encoderDelays >> 12;
        int endDelay      = encoderDelays & 0xFFF;
        int miscellaneous = toUnsignedInt(frame[offset++]);
        int mp3Gain       = toUnsignedInt(frame[offset++]);
        int surroundInfo  = Short.toUnsignedInt(toUInt16BE(Arrays.copyOfRange(frame, offset, offset += 2)));

        long lengthBytes = toUnsignedLong(toUInt32BE(Arrays.copyOfRange(frame, offset, offset += 4)));
        int musicCRC     = Short.toUnsignedInt(toUInt16BE(Arrays.copyOfRange(frame, offset, offset += 2)));
        int infoCRC      = Short.toUnsignedInt(toUInt16BE(Arrays.copyOfRange(frame, offset, offset += 2)));

        return new LAMEHeader(
                version,
                tagVersion,
                vbrMethod,
                lowpassFlt,
                replayGain,
                encodingFlags,
                lameAthType,
                bitrate,
                new int[] { startDelay, endDelay },
                miscellaneous,
                mp3Gain,
                surroundInfo,
                lengthBytes,
                musicCRC,
                infoCRC
        );
    }

    private XingHeader parseXingHeader(MpegFrame frame) {

        MpegFrameHeader header = frame.getMpegHeader();
        byte[] frameBody       = frame.getFrameBody();

        final byte channelMode = header.getChannelMode();
        int offset;

        if (channelMode != CHANNEL_MODE_SINGLE_CHANNEL) {
            offset = header.getVersion() == MPEG_VERSION_1 ? 32 : 17;
        } else {
            offset = header.getVersion() == MPEG_VERSION_1 ? 17 : 9;
        }

        byte[] magic = Arrays.copyOfRange(frameBody, offset, offset + 4);
        if (!Arrays.equals(magic, XingHeader.XING_VBR_MAGIC) &&
                !Arrays.equals(magic, XingHeader.XING_CBR_MAGIC))
        {
            return null;
        }

        offset += 4;
        int flags = toUInt32BE(Arrays.copyOfRange(frameBody, offset, offset + 4)); offset += 4;

        int totalFrames;
        int totalBytes;
        int quality;
        byte[] toc;

        XingHeader.Builder builder = XingHeader.newBuilder();
        builder = builder.setMagic(magic);
        builder = builder.setFlags(flags);

        if ((flags & FLAG_FRAMES) != 0x0) {
            totalFrames = toUInt32BE(Arrays.copyOfRange(frameBody, offset, offset + 4)); offset += 4;
            builder     = builder.setTotalFrames(totalFrames);
        }
        if ((flags & FLAG_BYTES) != 0x0) {
            totalBytes  = toUInt32BE(Arrays.copyOfRange(frameBody, offset, offset + 4)); offset += 4;
            builder     = builder.setTotalBytes(totalBytes);
        }
        if ((flags & FLAG_TOC) != 0x0) {
            toc = Arrays.copyOfRange(frameBody, offset, offset += 100);
            builder = builder.setTableOfContents(toc);
        }
        if ((flags & FLAG_QUALITY) != 0x0) {
            quality = toUInt32BE(Arrays.copyOfRange(frameBody, offset, offset + 4)); offset += 4;
            builder = builder.setQualityIndicator(quality);
        }

        XingHeader xingHeader = builder.build();
        LAMEHeader lameHeader = parseLAMEHeader(frameBody, offset);
        if (lameHeader != null) {
            xingHeader.setLAMEHeader(lameHeader);
        }
        return xingHeader;
    }

    private VBRIHeader parseVBRIHeader(MpegFrame frame) {

        int offset    = 0;
        byte[] header = Arrays.copyOfRange(frame.getFrameBody(), 32, 32 + 18);
        byte[] magic  = Arrays.copyOfRange(header, offset, offset += 4);

        if (!Arrays.equals(magic, VBRIHeader.VBRI_MAGIC)) {
            return null;
        }

        short version   = toUInt16BE(Arrays.copyOfRange(header, offset, offset += 2));
        float delay     = intBitsToFloat(toUInt16BE(Arrays.copyOfRange(header, offset, offset += 2)));
        short quality   = toUInt16BE(Arrays.copyOfRange(header, offset, offset += 2));
        int totalBytes  = toUInt32BE(Arrays.copyOfRange(header, offset, offset += 4));
        int totalFrames = toUInt32BE(Arrays.copyOfRange(header, offset, offset += 4));

        return new VBRIHeader(
                version,
                quality,
                delay,
                totalBytes,
                totalFrames
        );
    }

    private static int getBitrate(
            int audioBytes,
            int sampleRate,
            int samples)
    {
        return (int) Math.rint((float) audioBytes * 8f * sampleRate / (float) samples);
    }

    private static int getDuration(
            int samples,
            int sampleRate)
    {
        return (int) Math.rint((float) samples / sampleRate);
    }

    @Override
    public MpegStreamInfo parseStreamInfo(FileWrapper file) throws IOException {

        MpegFrameParser mpegFrameParser = new MpegFrameParser();
        int syncPosition = mpegFrameParser.parseFrame(file, tag);
        if (syncPosition == -1)
            return null;

        MpegFrame mpegFrame = mpegFrameParser.getMpegFrame();
        if (mpegFrame == null)
            return null;

        MpegFrameHeader mpegHeader = mpegFrame.getMpegHeader();
        XingHeader xingHeader      = parseXingHeader(mpegFrame);
        VBRIHeader vbriHeader      = null;

        if (xingHeader == null)
            vbriHeader = parseVBRIHeader(mpegFrame);

        MpegStreamInfo.Builder builder = MpegStreamInfo.newBuilder();
        builder = builder.setMpegHeader(mpegFrame.getMpegHeader());
        if (xingHeader != null) builder = builder.setXingHeader(xingHeader);
        if (vbriHeader != null) builder = builder.setVBRIHeader(vbriHeader);

        MpegStreamInfo mpegStreamInfo = builder.build();
        int bitrate = mpegHeader.getBitrate() * 1000;
        float duration = ((float) file.length() - syncPosition) * 8f / bitrate;

        int samplesFrame = mpegHeader.getSamplesPerFrame();
        int sampleRate   = mpegHeader.getSampleRate();

        if (xingHeader != null) {

            int samples    = samplesFrame * xingHeader.getTotalFrames();
            int audioBytes = xingHeader.getTotalBytes() - getFrameSize(mpegHeader);

            bitrate = getBitrate(audioBytes, sampleRate, xingHeader.getTotalFrames() * samplesFrame);
            LAMEHeader lame = xingHeader.getLAMEHeader();

            if (lame != null) {
                samples -= lame.getEncoderDelays()[0];
                samples -= lame.getEncoderDelays()[1];
                samples = Math.max(0, samples);
            }
            duration = getDuration(samples, sampleRate);
        }
        else if (vbriHeader != null) {
            bitrate  = getBitrate(vbriHeader.getTotalBytes() - syncPosition + 4, sampleRate, vbriHeader.getTotalFrames() * samplesFrame);
            duration = getDuration(vbriHeader.getTotalFrames() * samplesFrame, sampleRate);
        }
        mpegStreamInfo.setBitrate(bitrate / 1000);
        mpegStreamInfo.setDuration((int) duration);
        return mpegStreamInfo;
    }
}
