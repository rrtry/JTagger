package com.jtagger.mp4;

import com.jtagger.Component;
import com.jtagger.utils.IntegerUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class MP4Atom implements Component {

    public static int PADDING = 1024 * 10; // 10K
    private static final Set<String> DUPLICATES = new HashSet<>();

    private long atomStart;
    private long atomEnd;

    private final String type;
    protected byte[] data;

    private MP4Atom parentAtom;
    private ArrayList<MP4Atom> children = new ArrayList<>();

    private boolean assembled = false;

    static {
        DUPLICATES.add("mdat");
        DUPLICATES.add("moof");
    }

    public MP4Atom(String type, byte[] data) {
        this.type = type;
        this.data = data;
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
        if (hasChildren() && !assembled) {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            boolean isMeta = type.equals("meta");

            try {

                MP4Atom currentAtom;
                MP4Atom nextAtom;

                for (int i = 0; i < children.size(); i++) {
                    currentAtom = children.get(i);
                    if ((i + 1) < children.size()) {
                        nextAtom = children.get(i + 1);
                        if (!currentAtom.getType().equals("free") && nextAtom.getType().equals("free")) {
                            // there's 'free' atom after currentAtom, use padding space
                            final int delta       = currentAtom.getData().length - currentAtom.assemble().length;
                            final int freeSize    = nextAtom.getData().length;
                            final int paddingSize = freeSize + delta;

                            if (currentAtom.getType().equals("ilst")) {
                                System.out.println("resizing padding");
                                System.out.println("MP4Atom.assemble: delta -> " + delta);
                                System.out.println("MP4Atom.assemble: freeSize -> " + freeSize);
                                System.out.println("MP4Atom.assemble: paddingSie -> " + paddingSize);
                            }
                            out.write(currentAtom.getData());
                            if (delta != 0) {
                                if ((paddingSize - 8) >= 0) {
                                    addPadding(out, paddingSize);
                                }
                                i++; // skip next 'free' atom
                            }
                            continue;
                        }
                        out.write(currentAtom.assemble());
                    }
                    else if (currentAtom.getType().equals("ilst")) {
                        // there's no 'free' atom after ilst, write padding manually
                        System.out.println("adding padding: size -> " + PADDING);
                        out.write(currentAtom.assemble());
                        addPadding(out, PADDING); // 10K
                    }
                    else {
                        out.write(currentAtom.assemble());
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to write atom data");
            }

            byte[] buffer = out.toByteArray();
            byte[] atom   = new byte[buffer.length + 8 + (isMeta ? 4 : 0)];
            System.arraycopy(IntegerUtils.fromUInt32BE(atom.length), 0, atom, 0, 4);
            System.arraycopy(type.getBytes(ISO_8859_1), 0, atom, 4, 4);

            if (isMeta) {
                System.out.println("MP4.assemble: meta size diff -> " + (data.length - atom.length));
                System.arraycopy(IntegerUtils.fromUInt32BE(0), 0, atom, 8, 4);
            }

            System.arraycopy(buffer, 0, atom, isMeta ? 12 : 8, buffer.length);
            this.data = atom;
            this.assembled = true;
            return atom;
        }
        return data;
    }

    @Override
    public byte[] getBytes() {
        return getData();
    }

    private static void addPadding(ByteArrayOutputStream out, int paddingSize) throws IOException {

        out.write(IntegerUtils.fromUInt32BE(paddingSize));
        out.write("free".getBytes(ISO_8859_1));

        for (int j = 0; j < (paddingSize - 8); j++) {
            out.write((byte) 0x0);
        }
    }

    public long getStart() {
        return atomStart;
    }

    public long getEnd() {
        return atomEnd;
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

    public void setStart(long atomStart) {
        this.atomStart = atomStart;
    }

    public void setEnd(long atomEnd) {
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

    void setData(byte[] data) {
        this.data = data;
    }
}
