package com.jtagger.mp3.id3;

import com.jtagger.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.jtagger.mp3.id3.ID3SynchSafeInteger.toSynchSafeInteger;
import static com.jtagger.utils.IntegerUtils.fromUInt32BE;

public class FrameHeader implements Component {

    public static final byte STATUS_MESSAGE_FLAGS = 0; // ID3V2.3 0x1F, ID3V2.4 0x8F
    public static final byte ENCODING_FLAGS = 1; // ID3V2.3 0x1F, ID32.4 0xB0

    public static final int FRAME_HEADER_LENGTH        = 10;
    public static final int FRAME_HEADER_ID_LENGTH     = 4;
    public static final int FRAME_HEADER_SIZE_OFFSET   = 4;
    public static final int FRAME_HEADER_SIZE_LENGTH   = 4;
    public static final int FRAME_HEADER_FLAGS_OFFSET  = 8;
    public static final int FRAME_HEADER_FLAGS_LENGTH  = 2;
    public static final int FRAME_HEADER_DATA_OFFSET   = 10;

    public static final int FLAG_TAG_ALTER_PRESERVATION  = 0x80;
    public static final int FLAG_FILE_ALTER_PRESERVATION = 0x40;
    public static final int FLAG_READ_ONLY               = 0x20;
    public static final int FLAG_COMPRESSED              = 0x80;
    public static final int FLAG_ENCRYPTED               = 0x40;
    public static final int FLAG_GROUPING_IDENTITY       = 0x20;
    public static final int FLAG_DATA_LENGTH             = 0x01;
    public static final int FLAG_UNSYNCH                 = 0x02;

    private String identifier;
    private int frameSize;

    private byte version = ID3V2Tag.ID3V2_3;
    private byte groupingIdentity = 0x00;

    private byte[] flags = new byte[] { 0x00, 0x00 };
    private byte[] headerBytes;

    @Override
    public String toString() {
        return String.format("ID: %s, FRAME SIZE: %s", getIdentifier(), getFrameSize());
    }

    @Override
    public byte[] getBytes() {
        return headerBytes;
    }

    public byte getVersion() {
        return version;
    }

    @Override
    public byte[] assemble(byte version) {

        byte[] header = new byte[FRAME_HEADER_LENGTH];
        byte[] identifierBytes = identifier.getBytes(StandardCharsets.ISO_8859_1);
        byte[] frameSizeBytes = getFrameSizeBytes(version);
        byte[] flags = getFlags();

        System.arraycopy(identifierBytes, 0, header, 0, identifierBytes.length);
        System.arraycopy(frameSizeBytes, 0, header, identifierBytes.length, frameSizeBytes.length);
        System.arraycopy(flags, 0, header, identifierBytes.length + frameSizeBytes.length, flags.length);

        this.headerBytes = header;
        return header;
    }

    public void setIdentifier(String identifier, byte version) {

        String[] frames;

        if (version == ID3V2Tag.ID3V2_3) frames = AbstractFrame.V2_3_FRAMES;
        else if (version == ID3V2Tag.ID3V2_4) frames = AbstractFrame.V2_4_FRAMES;
        else throw new IllegalArgumentException("Unsupported tag version: " + version);

        if (Arrays.stream(frames).noneMatch(identifier::equals)) {
            throw new IllegalArgumentException("Unknown frame identifier: " + identifier);
        }
        this.identifier = identifier;
    }

    public void setTagVersion(byte version) {
        if (version != ID3V2Tag.ID3V2_4 &&
            version != ID3V2Tag.ID3V2_3)
        {
            throw new IllegalArgumentException("Invalid version number: " + version);
        }
        this.version = version;
    }

    private void addDataLengthIndicator(int frameLength, byte index, byte[] fields) {

        byte[] lengthIndicator;

        if (version == ID3V2Tag.ID3V2_4) {
            lengthIndicator = fromUInt32BE(toSynchSafeInteger(frameLength));
        } else {
            lengthIndicator = fromUInt32BE(frameLength);
        }
        System.arraycopy(lengthIndicator, 0, fields, index, lengthIndicator.length);
    }

    public byte[] buildFlagFields(int frameSize) {

        int length = getFrameDataOffset();
        final byte dataLengthIndicator = 4;

        if (length > 0) {

            byte[] bytes = new byte[length];
            byte index = 0;

            final boolean hasLengthIndicator = isDataLengthIndicatorPresent() || isFrameCompressed();
            final boolean hasGroupIdentity   = isFrameInGroup();
            final byte    groupingIdentity   = getGroupingIdentity();

            if (version == ID3V2Tag.ID3V2_4) {

                if (hasGroupIdentity) {
                    bytes[index++] = groupingIdentity;
                }
                if (hasLengthIndicator) {
                    addDataLengthIndicator(frameSize, index, bytes);
                }

            } else if (version == ID3V2Tag.ID3V2_3) {

                if (hasLengthIndicator) {
                    addDataLengthIndicator(frameSize, index, bytes);
                    index += dataLengthIndicator;
                }
                if (hasGroupIdentity) {
                    bytes[index] = groupingIdentity;
                }
            }
            return bytes;
        }
        return new byte[0];
    }

    public void setFlags(byte[] flags) {
        if (flags.length != 2) throw new IllegalArgumentException("Flags array must contain two bytes");
        if (areIllegalFlagsSet(flags, version)) throw new IllegalArgumentException("Unknown flag was set");
        this.flags = flags;
    }

    public byte getFrameDataOffset() {

        byte offset = 0;

        if (isFrameInGroup()) {
            offset += 1;
        }
        if (isDataLengthIndicatorPresent() || isFrameCompressed()) {
            offset += 4;
        }

        return offset;
    }

    private static boolean areIllegalFlagsSet(byte[] flags, byte version) {
        return isIllegalStatusFlagSet(flags[STATUS_MESSAGE_FLAGS], version) &&
                isIllegalEncodingFlagSet(flags[ENCODING_FLAGS], version);
    }

    private static boolean isIllegalEncodingFlagSet(byte flags, byte version) {

        final int illegalFlags;

        if (version == ID3V2Tag.ID3V2_4) {
            illegalFlags = (FLAG_COMPRESSED >> 4 |
                    FLAG_ENCRYPTED >> 4  |
                    FLAG_GROUPING_IDENTITY << 1 |
                    FLAG_DATA_LENGTH |
                    FLAG_UNSYNCH) ^ 0xFF;
        } else {
            illegalFlags = (FLAG_COMPRESSED |
                    FLAG_ENCRYPTED  |
                    FLAG_GROUPING_IDENTITY) ^ 0xFF;
        }
        return (flags & illegalFlags) != 0;
    }

    private static boolean isIllegalStatusFlagSet(byte flags, byte version) {

        final int illegalFlags;

        if (version == ID3V2Tag.ID3V2_4) {
            illegalFlags = (FLAG_TAG_ALTER_PRESERVATION >> 1  |
                            FLAG_FILE_ALTER_PRESERVATION >> 1 |
                            FLAG_READ_ONLY >> 1) ^ 0xFF;

        } else {
            illegalFlags = (FLAG_TAG_ALTER_PRESERVATION |
                            FLAG_FILE_ALTER_PRESERVATION |
                            FLAG_READ_ONLY) ^ 0xFF;
        }
        return (flags & illegalFlags) != 0;
    }

    public static int getFlag(byte flagType, int flag, byte version) {
        if (version != ID3V2Tag.ID3V2_4) {
            return flag;
        }

        if (flag == FLAG_DATA_LENGTH || flag == FLAG_UNSYNCH) {
            return flag;
        }
        if (flag == FLAG_GROUPING_IDENTITY) {
            return flag << 1;
        }

        if (flagType == STATUS_MESSAGE_FLAGS) {
            flag >>= 1;
        } else if (flagType == ENCODING_FLAGS) {
            flag >>= 4;
        }
        return flag;
    }

    private boolean isFlagSet(byte index, int flag) {
        return (flags[index] & getFlag(index, flag, version)) != 0;
    }

    private void setFlag(byte index, boolean set, int flag) {

        int bit = getFlag(index, flag, version);

        if (set) flags[index] |= bit;
        else flags[index] &= ~bit;
    }

    public boolean isTagAlterPreservationSet() {
        return isFlagSet(STATUS_MESSAGE_FLAGS, FLAG_TAG_ALTER_PRESERVATION);
    }

    public boolean isFileAlterPreservationSet() {
        return isFlagSet(STATUS_MESSAGE_FLAGS, FLAG_FILE_ALTER_PRESERVATION);
    }

    public boolean isFrameReadOnly() {
        return isFlagSet(STATUS_MESSAGE_FLAGS, FLAG_READ_ONLY);
    }

    public boolean isFrameUnsynch() {
        if (version != ID3V2Tag.ID3V2_4) return false;
        return isFlagSet(ENCODING_FLAGS, FLAG_UNSYNCH);
    }

    public boolean isFrameEncrypted() {
        return isFlagSet(ENCODING_FLAGS, FLAG_ENCRYPTED);
    }

    public boolean isFrameCompressed() {
        return isFlagSet(ENCODING_FLAGS, FLAG_COMPRESSED);
    }

    public boolean isFrameInGroup() {
        return isFlagSet(ENCODING_FLAGS, FLAG_GROUPING_IDENTITY);
    }

    public boolean isDataLengthIndicatorPresent() {
        if (version != ID3V2Tag.ID3V2_4) return false;
        return isFlagSet(ENCODING_FLAGS, FLAG_DATA_LENGTH);
    }

    public void setTagAlterPreservation(boolean discardFrame) {
        setFlag(STATUS_MESSAGE_FLAGS, discardFrame, FLAG_TAG_ALTER_PRESERVATION);
    }

    public void setFileAlterPreservation(boolean discardFrame) {
        setFlag(STATUS_MESSAGE_FLAGS, discardFrame, FLAG_FILE_ALTER_PRESERVATION);
    }

    public void setFrameReadOnly(boolean isReadOnly) {
        setFlag(STATUS_MESSAGE_FLAGS, isReadOnly, FLAG_READ_ONLY);
    }

    public void setFrameEncrypted(boolean encrypted) {
        setFlag(ENCODING_FLAGS, encrypted, FLAG_ENCRYPTED);
    }

    public void setFrameCompressed(boolean compressed) {
        setFlag(ENCODING_FLAGS, compressed, FLAG_COMPRESSED);
        if (version == ID3V2Tag.ID3V2_4) {
            setFlag(ENCODING_FLAGS, compressed, FLAG_DATA_LENGTH);
        }
    }

    public void removeGroupingIdentity() {
        this.groupingIdentity = 0x00;
        setFlag(ENCODING_FLAGS, false, FLAG_GROUPING_IDENTITY);
    }

    public void setFrameGroupingIdentity(byte groupIdentity) {
        this.groupingIdentity = groupIdentity;
        setFlag(ENCODING_FLAGS, true, FLAG_GROUPING_IDENTITY);
    }

    public void setFrameUnsynch(boolean unsynch) {
        if (version == ID3V2Tag.ID3V2_4) {
            setFlag(ENCODING_FLAGS, unsynch, FLAG_UNSYNCH);
        }
    }

    public void setDataLengthIndicator(boolean indicator) {
        if (version == ID3V2Tag.ID3V2_4) {
            setFlag(ENCODING_FLAGS, indicator, FLAG_DATA_LENGTH);
        }
    }

    public byte getGroupingIdentity() { return groupingIdentity; }

    public String getIdentifier() { return identifier; }
    public int getFrameSize() { return frameSize; }
    public byte[] getFlags() { return flags; }

    private byte[] getFrameSizeBytes(byte version) {
        if (version == ID3V2Tag.ID3V2_3) return fromUInt32BE(frameSize);
        if (version == ID3V2Tag.ID3V2_4) return fromUInt32BE(toSynchSafeInteger(frameSize));
        throw new IllegalArgumentException("Unsupported version: " + version);
    }

    public static Builder newBuilder(byte version) {

        Builder builder = new FrameHeader().new Builder();
        builder = builder.setVersion(version);

        return builder;
    }

    public static Builder newBuilder(FrameHeader header) {
        return header.new Builder();
    }

    public static FrameHeader createFrameHeader(String id, byte version) {
        return FrameHeader.newBuilder(version)
                .setIdentifier(id, version)
                .build(version);
    }

    public class Builder {

        public Builder setIdentifier(String identifier, byte version) {
            FrameHeader.this.setIdentifier(identifier, version);
            return this;
        }

        public Builder setVersion(byte version) {
            FrameHeader.this.setTagVersion(version);
            return this;
        }

        public Builder setFlags(byte[] flags) {
            FrameHeader.this.setFlags(flags);
            return this;
        }

        public Builder setTagAlterPreservation(boolean discardFrame) {
            FrameHeader.this.setTagAlterPreservation(discardFrame);
            return this;
        }

        public Builder setFileAlterPreservation(boolean discardFrame) {
            FrameHeader.this.setFileAlterPreservation(discardFrame);
            return this;
        }

        public Builder setReadOnly(boolean isReadOnly) {
            FrameHeader.this.setFrameReadOnly(isReadOnly);
            return this;
        }

        public Builder setEncrypted(boolean encrypted) {
            FrameHeader.this.setFrameEncrypted(encrypted);
            return this;
        }

        public Builder setCompressed(boolean compressed) {
            FrameHeader.this.setFrameCompressed(compressed);
            return this;
        }

        public Builder removeGroupingIdentity() {
            FrameHeader.this.removeGroupingIdentity();
            return this;
        }

        public Builder setGroupingIdentity(byte groupingIdentity) {
            FrameHeader.this.setFrameGroupingIdentity(groupingIdentity);
            return this;
        }

        public Builder setFrameSize(int frameSize) {
            FrameHeader.this.frameSize = frameSize;
            return this;
        }

        public FrameHeader buildExisting(byte[] data) {
            FrameHeader.this.headerBytes = data;
            return FrameHeader.this;
        }

        public FrameHeader build(byte version) {
            assemble(version);
            return FrameHeader.this;
        }
    }
}
