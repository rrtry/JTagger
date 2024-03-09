package com.jtagger.mp4;

import com.jtagger.Component;
import com.jtagger.utils.IntegerUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.jtagger.mp4.MP4.DUPLICATES;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class MP4Atom implements Component {

    private int atomStart;
    private int atomEnd;

    private final String type;
    private int size;
    protected byte[] data;

    private MP4Atom parentAtom;
    private ArrayList<MP4Atom> children = new ArrayList<>();

    public MP4Atom(String type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public MP4Atom(String type, int size) {
        this.type = type;
        this.size = size;
    }

    public MP4Atom(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MP4Atom atom = (MP4Atom) obj;
        if (DUPLICATES.contains(atom.getType()) || DUPLICATES.contains(getType())) {
            return false;
        }
        return getType().equals(atom.getType());
    }

    @Override
    public byte[] assemble(byte version) {

        if (!type.equals("ilst")) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {

            for (MP4Atom atom : children) {
                out.write(atom.assemble());
            }

        } catch (IOException e) {
            throw new IllegalStateException("Failed to write atom data");
        }

        byte[] outBuffer = out.toByteArray();
        byte[] atomData  = new byte[8 + outBuffer.length];
        System.arraycopy(IntegerUtils.fromUInt32BE(atomData.length), 0, atomData, 0, 4);
        System.arraycopy(type.getBytes(ISO_8859_1), 0, atomData, 4, 4);
        System.arraycopy(outBuffer, 0, atomData, 8, outBuffer.length);

        data = atomData;
        return data;
    }

    @Override
    public byte[] getBytes() {
        return getData();
    }

    public int getStart() {
        return atomStart;
    }

    public int getEnd() {
        return atomEnd;
    }

    public int getSize() {
        return data == null ? size : data.length;
    }

    public MP4Atom getParent() {
        return parentAtom;
    }

    public String getType() {
        return type;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public <T extends MP4Atom> ArrayList<T> getChildren() {
        return (ArrayList<T>) children;
    }

    public void setParent(MP4Atom parentAtom) {
        this.parentAtom = parentAtom;
    }

    public void setStart(int atomStart) {
        this.atomStart = atomStart;
    }

    public void setEnd(int atomEnd) {
        this.atomEnd = atomEnd;
    }

    @SuppressWarnings("unchecked")
    public <T extends MP4Atom> void setChildren(ArrayList<T> childrenList) {
        if (childrenList.isEmpty()) {
            removeChildren(); return;
        }
        this.children = (ArrayList<MP4Atom>) childrenList;
    }

    public void appendChild(MP4Atom atom) {
        if (!children.contains(atom)) children.add(atom);
    }

    public void removeChildren() {

        this.children = new ArrayList<>();
        data = new byte[8];

        System.arraycopy(IntegerUtils.fromUInt32BE(data.length), 0, data, 0, 4);
        System.arraycopy(type.getBytes(ISO_8859_1), 0, data, 4, 4);
    }

    public byte[] getData() {
        return data;
    }
}
