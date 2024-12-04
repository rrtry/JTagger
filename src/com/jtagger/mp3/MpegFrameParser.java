package com.jtagger.mp3;

import com.jtagger.mp3.id3.ID3V2Tag;
import com.jtagger.mp3.id3.TagHeader;

import java.io.IOException;
import com.jtagger.FileWrapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.jtagger.mp3.MpegFrameHeader.*;
import static java.lang.Byte.toUnsignedInt;

public class MpegFrameParser {

    private static final int[][] MODE_EXTENSION;
    private static final int[][] SAMPLE_RATE;
    private static final int[][] BITRATE;
    private static final int[][] SAMPLES_PER_FRAME;

    static {
        SAMPLES_PER_FRAME = new int[][] {
                {0, 0, 0, 0},
                {576, -1, 576, 1152},
                {1152, -1, 1152, 1152},
                {384, -1, 384, 384}
        };
        MODE_EXTENSION = new int[][] {
                {0, 0},
                {1, 0},
                {0, 1},
                {1, 1}
        };
        SAMPLE_RATE = new int[][] {
                {11025, -1, 22050, 44100},
                {12000, -1, 24000, 48000},
                {8000,  -1, 16000, 32000}
        };
        BITRATE = new int[][] {
                {0, 0, 0, 0, 0},
                {32,32,32,32,8},
                {64,48,40,48,16},
                {96,56,48,56,24},
                {128,64,56,64,32},
                {160,80,64,80,40},
                {192,96,80,96,48},
                {224,112,96,112,56},
                {256,128,112,128,64},
                {288,160,128,144,80},
                {320,192,160,160,96},
                {352,224,192,176,112},
                {384,256,224,192,128},
                {416,320,256,224,144},
                {448,384,320,256,160},
                {-1, -1, -1, -1, -1}
        };
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

    private static int[] getValues(int index, int[][] array) {
        return index >= 0 && index < array.length ? array[index] : null;
    }

    private static boolean isSync(byte x, byte y) {
        return toUnsignedInt(x) == 0xFF &&
                (toUnsignedInt(y) >> 5) == 0x7;
    }

    private static boolean isSync(int x, int y) {
        return x == 0xFF && (y >> 5) == 0x7;
    }

    public static int getSyncOffset(FileWrapper file, ID3V2Tag id3V2Tag) {
        try {

            int startPosition = 0;
            if (id3V2Tag != null) {

                TagHeader header = id3V2Tag.getTagHeader();
                startPosition += header.getTagSize() + 10;

                if (header.hasFooter()) {
                    startPosition += 10;
                }
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
                previousOffset = file.getFilePointer();

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
    public void parseFrame(FileWrapper file) {
        try {

            int offset = getSyncOffset(file, id3V2Tag);

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

    private MpegFrameHeader parseFrameHeader(FileWrapper file, int offset) {
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

            boolean isIntensityStereo = false;
            boolean isMidSideStereo   = false;

            if (channelMode == CHANNEL_MODE_JOIN_STEREO) {

                int[] modeExtValues = getValues(modeExtension, MODE_EXTENSION);
                if (modeExtValues == null) return null;

                isIntensityStereo = modeExtValues[0] == 1;
                isMidSideStereo   = modeExtValues[1] == 1;
            }
            if (version == MPEG_VERSION_1) {

                if (layer == MPEG_LAYER_1) index = 0;
                if (layer == MPEG_LAYER_2) index = 1;
                if (layer == MPEG_LAYER_3) index = 2;

            } else if (version == MPEG_VERSION_2 || version == MPEG_VERSION_2_5) {

                if (layer == MPEG_LAYER_1) index = 3;
                if (layer == MPEG_LAYER_2 || layer == MPEG_LAYER_3) index = 4;

            } else {
                return null; // invalid version
            }

            int[] bitrateValues         = getValues(bitrateIndex, BITRATE);
            int[] sampleRateValues      = getValues(sampleRateBits, SAMPLE_RATE);
            int[] samplesPerFrameValues = getValues(layer, SAMPLES_PER_FRAME);

            if (bitrateValues == null) return null;
            if (sampleRateValues == null) return null;
            if (samplesPerFrameValues == null) return null;

            int bitrate         = bitrateValues[index];
            int sampleRate      = sampleRateValues[version];
            int samplesPerFrame = samplesPerFrameValues[version];

            if (bitrate == -1) {
                return null; // bad value;
            }
            if (layer == MPEG_LAYER_2) {

                boolean isStereo           = channelMode == CHANNEL_MODE_STEREO;
                boolean isDualChannel      = channelMode == CHANNEL_MODE_DUAL_CHANNEL;
                boolean isSingleChannel    = channelMode == CHANNEL_MODE_SINGLE_CHANNEL;
                boolean illegalChannelMode = isStereo || isIntensityStereo || isDualChannel;

                List<Integer> restrictedValues = List.of(32, 48, 56, 80);
                if (restrictedValues.contains(bitrate) && illegalChannelMode) return null;

                restrictedValues   = List.of(224, 256, 320, 384);
                illegalChannelMode = isSingleChannel;

                if (restrictedValues.contains(bitrate) && illegalChannelMode) return null;
            }

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
