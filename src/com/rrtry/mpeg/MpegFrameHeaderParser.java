package com.rrtry.mpeg;

import com.rrtry.mpeg.id3.ID3V2Tag;
import com.rrtry.mpeg.id3.TagHeader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.rrtry.mpeg.MpegFrameHeader.*;

public class MpegFrameHeaderParser {

    private static final HashMap<Byte, List<Boolean>> MODE_EXTENSION = new HashMap<>();
    private static final HashMap<Byte, List<Integer>> SAMPLE_RATE    = new HashMap<>();
    private static final HashMap<Byte, List<Integer>> BITRATE        = new HashMap<>();

    static {

        MODE_EXTENSION.put((byte) 0x00, Arrays.asList(false, false));
        MODE_EXTENSION.put((byte) 0x01, Arrays.asList(true, false));
        MODE_EXTENSION.put((byte) 0x02, Arrays.asList(false, true));
        MODE_EXTENSION.put((byte) 0x03, Arrays.asList(true, true));

        SAMPLE_RATE.put((byte) 0x00, Arrays.asList(11025, 0, 22050, 44100));
        SAMPLE_RATE.put((byte) 0x01, Arrays.asList(12000, 0, 24000, 48000));
        SAMPLE_RATE.put((byte) 0x02, Arrays.asList(8000,  0, 16000, 32000));

        BITRATE.put((byte) 0x0, Arrays.asList(0, 0, 0, 0, 0));
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
        BITRATE.put((byte) 0xF, Arrays.asList(-1, -1, -1, -1, -1));
    }

    private ID3V2Tag id3V2Tag;

    public MpegFrameHeaderParser() {
        /* empty constructor */
    }

    public MpegFrameHeaderParser(ID3V2Tag tag) {
        this.id3V2Tag = tag;
    }

    public MpegFrameHeader parse(RandomAccessFile file) {
        try {

            int frameOffset = 0;
            if (id3V2Tag != null) {

                TagHeader header = id3V2Tag.getTagHeader();
                frameOffset += header.getTagSize() + 10;

                if (header.hasFooter()) {
                    frameOffset += 10;
                }
            }

            byte[] header = new byte[4];

            file.seek(frameOffset);
            file.read(header, 0, header.length);

            byte index = 0;

            byte bitrateIndex  = (byte) (header[2] >> 4);
            byte emphasis      = (byte) (header[3] << 6);
            byte channelMode   = (byte) (header[3] >> 6);

            byte version        = (byte) (header[1] & 0x18);
            byte layer          = (byte) (header[1] & 0x6);
            byte sampleRateBits = (byte) (header[2] & 0xC);
            byte modeExtension  = (byte) (header[3] & 0x30);

            boolean isProtected         = (header[1] & 0x01) == 0;
            boolean isPadded            = (header[2] & 0x2)  != 0;
            boolean isCopyrighted       = (header[3] & 0x8)  != 0;
            boolean isOriginal          = (header[3] & 0x4)  != 0;

            boolean isIntensityStereo   = MODE_EXTENSION.get(modeExtension).get(0);
            boolean isMidSideStereo     = MODE_EXTENSION.get(modeExtension).get(1);

            if (version == MPEG_VERSION_1) {

                if (layer == MPEG_LAYER_1) index = 0;
                if (layer == MPEG_LAYER_2) index = 1;
                if (layer == MPEG_LAYER_3) index = 2;
            }
            if (version == MPEG_VERSION_2) {

                if (layer == MPEG_LAYER_1) index = 3;
                if (layer == MPEG_LAYER_2) index = 4;
                if (layer == MPEG_LAYER_3) index = 5;
            }

            int bitrate = BITRATE.get(bitrateIndex).get(index);
            int sampleRate = SAMPLE_RATE.get(sampleRateBits).get(version);

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
                    sampleRate
            );
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

}
