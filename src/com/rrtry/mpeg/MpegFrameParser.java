package com.rrtry.mpeg;

import com.rrtry.mpeg.id3.ID3V2Tag;
import com.rrtry.mpeg.id3.TagHeader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.rrtry.mpeg.MpegFrameHeader.*;
import static java.lang.Byte.toUnsignedInt;

public class MpegFrameParser {

    private static final HashMap<Byte, List<Boolean>> MODE_EXTENSION    = new HashMap<>();
    private static final HashMap<Byte, List<Integer>> SAMPLE_RATE       = new HashMap<>();
    private static final HashMap<Byte, List<Integer>> BITRATE           = new HashMap<>();
    private static final HashMap<Byte, List<Integer>> SAMPLES_PER_FRAME = new HashMap<>();

    static {

        SAMPLES_PER_FRAME.put(MPEG_LAYER_1, Arrays.asList(384, -1, 384, 384));
        SAMPLES_PER_FRAME.put(MPEG_LAYER_2, Arrays.asList(1152, -1, 1152, 1152));
        SAMPLES_PER_FRAME.put(MPEG_LAYER_3, Arrays.asList(576, -1, 576, 1152));

        MODE_EXTENSION.put((byte) 0x00, Arrays.asList(false, false));
        MODE_EXTENSION.put((byte) 0x01, Arrays.asList(true, false));
        MODE_EXTENSION.put((byte) 0x02, Arrays.asList(false, true));
        MODE_EXTENSION.put((byte) 0x03, Arrays.asList(true, true));

        SAMPLE_RATE.put((byte) 0x00, Arrays.asList(11025, -1, 22050, 44100));
        SAMPLE_RATE.put((byte) 0x01, Arrays.asList(12000, -1, 24000, 48000));
        SAMPLE_RATE.put((byte) 0x02, Arrays.asList(8000,  -1, 16000, 32000));

        BITRATE.put((byte) 0x0, Arrays.asList(0, 0, 0, 0, 0)); // VBR
        BITRATE.put((byte) 0x1, Arrays.asList(32,32,32,32,8));
        BITRATE.put((byte) 0x2, Arrays.asList(64,48,40,48,16));
        BITRATE.put((byte) 0x3, Arrays.asList(96,56,48,56,24));
        BITRATE.put((byte) 0x4, Arrays.asList(128,64,56,64,32));
        BITRATE.put((byte) 0x5, Arrays.asList(160,80,64,80,40));
        BITRATE.put((byte) 0x6, Arrays.asList(192,96,80,96,48));
        BITRATE.put((byte) 0x7, Arrays.asList(224,112,96,112,56));
        BITRATE.put((byte) 0x8, Arrays.asList(256,128,112,128,64));
        BITRATE.put((byte) 0x9, Arrays.asList(288,160,128,144,80));
        BITRATE.put((byte) 0xA, Arrays.asList(320,192,160,160,96));
        BITRATE.put((byte) 0xB, Arrays.asList(352,224,192,176,112));
        BITRATE.put((byte) 0xC, Arrays.asList(384,256,224,192,128));
        BITRATE.put((byte) 0xD, Arrays.asList(416,320,256,224,144));
        BITRATE.put((byte) 0xE, Arrays.asList(448,384,320,256,160));
        BITRATE.put((byte) 0xF, Arrays.asList(-1, -1, -1, -1, -1)); // BAD
    }

    private ID3V2Tag id3V2Tag;
    private MpegFrame mpegFrame;

    public MpegFrameParser() {
        /* empty constructor */
    }

    public MpegFrameParser(ID3V2Tag tag) {
        this.id3V2Tag = tag;
    }

    public MpegFrame getMpegFrame() {
        return mpegFrame;
    }

    private static boolean isSync(byte x, byte y) {
        return toUnsignedInt(x) == 0xFF &&
               toUnsignedInt(y) >> 4 == 0xF;
    }

    private static boolean isSync(int x, int y) {
        return x == 0xFF && y >> 4 == 0xF;
    }

    private int getSyncOffset(RandomAccessFile file) {
        try {

            if (id3V2Tag == null) {
                return 0;
            }

            int startPosition = 0;
            TagHeader header = id3V2Tag.getTagHeader();
            startPosition += header.getTagSize() + 10;

            if (header.hasFooter()) {
                startPosition += 10;
            }

            byte[] buffer = new byte[1024];
            file.seek(startPosition);

            byte previousByte   = 0;
            long previousOffset = 0;

            outer:
            while (true) {

                long position = file.getFilePointer();
                int read      = file.read(buffer);

                if (read == -1) {
                    break;
                }
                if (isSync(previousByte, buffer[0])) {
                    startPosition = (int) previousOffset;
                    break;
                }

                previousByte   = buffer[buffer.length - 1];
                previousOffset = position;

                for (int i = 0; i < buffer.length - 1; i++) {
                    if (isSync(buffer[i], buffer[i + 1]))
                    {
                        startPosition = (int) (position + i);
                        break outer;
                    }
                }
            }

            return startPosition;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // (12 * BitRate / SampleRate + Padding) * 4 - layer 1
    // 144 * BitRate / SampleRate + Padding - layer 2, 3
    public void parseFrame(RandomAccessFile file) {
        try {

            int offset = getSyncOffset(file);

            MpegFrameHeader header = parseFrameHeader(file, offset);
            byte[] frameData;

            if (header == null) return;
            int frameLength;

            final byte layer     = header.getLayer();
            final int bitRate    = header.getBitrate();
            final int sampleRate = header.getSampleRate();
            final int padding    = header.getPadding();

            frameLength = (layer == MPEG_LAYER_1) ?
                    (12 * bitRate * 1000 / sampleRate + padding) * 4 :
                    (144 * bitRate * 1000 / sampleRate + padding);

            frameData = new byte[frameLength];
            file.read(frameData, 0, frameData.length);
            mpegFrame = new MpegFrame(header, frameData);

        } catch (IOException ignored) {

        }
    }

    private MpegFrameHeader parseFrameHeader(RandomAccessFile file, int offset) {
        try {

            int[] header = new int[4];

            file.seek(offset);
            header[0] = file.readUnsignedByte();
            header[1] = file.readUnsignedByte();
            header[2] = file.readUnsignedByte();
            header[3] = file.readUnsignedByte();

            if (!isSync(header[0], header[1])) {
                return null;
            }

            byte index = 0;

            byte bitrateIndex = (byte) (header[2] >> 4);
            byte emphasis     = (byte) (header[3] << 6);
            byte channelMode  = (byte) (header[3] >> 6);

            byte version        = (byte) ((header[1] >> 3) & 0x3);
            byte layer          = (byte) ((header[1] >> 1) & 0x3);
            byte sampleRateBits = (byte) ((header[2] >> 2) & 0x3);
            byte modeExtension  = (byte) ((header[3] >> 4) & 0x3);

            boolean isProtected   = (header[1] & 0x1) == 0;
            boolean isPadded      = ((header[2] >> 1) & 0x1)  != 0;
            boolean isCopyrighted = ((header[3] >> 3) & 0x1)  != 0;
            boolean isOriginal    = ((header[3] >> 2) & 0x1)  != 0;

            boolean isIntensityStereo   = MODE_EXTENSION.get(modeExtension).get(0);
            boolean isMidSideStereo     = MODE_EXTENSION.get(modeExtension).get(1);

            if (version == MPEG_VERSION_1) {

                if (layer == MPEG_LAYER_1) index = 0;
                if (layer == MPEG_LAYER_2) index = 1;
                if (layer == MPEG_LAYER_3) index = 2;
            }
            if (version == MPEG_VERSION_2 || version == MPEG_VERSION_2_5) {

                if (layer == MPEG_LAYER_1) index = 3;
                if (layer == MPEG_LAYER_2 || layer == MPEG_LAYER_3) index = 4;
            }

            int bitrate         = BITRATE.get(bitrateIndex).get(index);
            int sampleRate      = SAMPLE_RATE.get(sampleRateBits).get(version);
            int samplesPerFrame = SAMPLES_PER_FRAME.get(layer).get(version);

            return new MpegFrameHeader(
                    version,
                    layer,
                    channelMode,
                    emphasis,
                    isProtected,
                    isCopyrighted,
                    isOriginal,
                    isPadded,
                    isIntensityStereo,
                    isMidSideStereo,
                    bitrate,
                    sampleRate,
                    samplesPerFrame
            );

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
