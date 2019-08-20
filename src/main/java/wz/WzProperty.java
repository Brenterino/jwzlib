/*
    This file is part of JWzLib: MapleStory WZ File Parser
    Copyright (C) 2019  Brenterino <brent@zygon.dev>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package wz;

import java.awt.Point;
import java.util.HashMap;
import java.util.Objects;

import wz.common.MP3;
import wz.common.PNG;
import wz.io.WzInputStream;

/**
 *
 * @author Brenterino
 */
public final class WzProperty<E> extends WzObject<WzProperty<E>, WzProperty<?>> {

    private E value;
    private Type pType;
    private String name;
    private int blocksize;
    private int offset;
    private HashMap<String, WzProperty<?>> children;

    public WzProperty(String n, E val, Type p) {
        this(n, val, p, false);
    }

    public WzProperty(String n, E val, Type p, boolean contain) {
        name = n;
        value = val;
        pType = p;
        if (contain) {
            children = new HashMap<>();
        }
    }

    public int getBlocksize() {
        return blocksize;
    }

    public void setBlocksize(int sz) {
        blocksize = sz;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public E getValue() {
        return value;
    }

    public void setValue(E ele) {
        value = ele;
    }

    @Override
    public void parse(WzInputStream in) {
    }

    public static void parse(WzInputStream in, int offset, WzObject<?, WzProperty<?>> parent) {
        int count = in.readCompressedInteger();
        for (int i = 0; i < count; i++) {
            String name = in.readStringBlock(offset);
            int t = in.read();
            switch (t) {
                case 0x00:
                    parent.addChild(new WzProperty<>(name, null, Type.NULL));
                    break;
                case 0x02:
                case 0x0B:
                    parent.addChild(new WzProperty<>(name, in.readShort(), Type.UNSIGNED_SHORT));
                    break;
                case 0x03:
                    parent.addChild(new WzProperty<>(name, in.readCompressedInteger(), Type.COMPRESSED_INTEGER));
                    break;
                case 0x04:
                    int s = in.read();
                    switch (s) {
                        case 0x00:
                            parent.addChild(new WzProperty<>(name, 0.0F, Type.BYTE_FLOAT));
                            break;
                        case 0x80:
                            parent.addChild(new WzProperty<>(name, in.readFloat(), Type.BYTE_FLOAT));
                            break;
                        default:
                            break;
                    }
                    break;
                case 0x05:
                    parent.addChild(new WzProperty<>(name, in.readDouble(), Type.DOUBLE));
                    break;
                case 0x08:
                    parent.addChild(new WzProperty<>(name, in.readStringBlock(offset), Type.STRING));
                    break;
                case 0x09:
                    int bsize = in.readInteger();
                    int eob = bsize + in.getPosition();
                    WzProperty<?> extended = parseExtended(in, name, offset);
                    if (extended != null) {
                        extended.setBlocksize(bsize);
                        parent.addChild(extended);
                    }
                    in.seek(eob);
                    break;
                case 0x14:
                    parent.addChild(new WzProperty<>(name, in.readCompressedLong(), Type.COMPRESSED_LONG));
                    break;
                default:
                    WzObject<?, ?> ancient = parent.getParent();
                    System.out.printf("ancient=%s,parent=%s,name=%s,offset=%s,pos=%s,t=%s%n",
                            ancient, parent.getName(), name, offset, in.getPosition(), t);
                    break;
            }
        }
    }

    private static WzProperty<?> parseExtended(WzInputStream in, String name, int offset) {
        String iname = in.readStringBlock(offset);
        WzProperty<?> child = null;
        if (!iname.isEmpty()) {
            switch (iname.charAt(0)) {
                case 'P':
                    child = new WzProperty<>(name, null, Type.SUB_PROPERTY, true);
                    in.skip(2);
                    parse(in, offset, child);
                    break;
                case 'C':
                    WzProperty<PNG> canvas = new WzProperty<>(name, null, Type.CANVAS, true);
                    canvas.setOffset(in.getPosition());
                    in.skip(1);
                    if (in.read() == 1) {
                        in.skip(2);
                        parse(in, offset, canvas);
                    }
                    int width = in.readCompressedInteger();
                    int height = in.readCompressedInteger();
                    int format = in.readCompressedInteger() + in.readByte();
                    in.skip(4);
                    int len = in.readInteger();
                    in.skip(1); // nowrap dummy byte
                    int zHeader = in.readShort() & 0xFFFF;
                    in.skip(-2); // rewind to include the zHeader in the data buffer

                    // Checks to see if this is a valid zlib header, we will swallow it
                    // if it's a legit header, we can move on without decoding the image
                    // buffer
                    boolean requiresDecryption = zHeader != 0x9C78 && zHeader != 0xDA78 && zHeader != 0x0178 && zHeader != 0x5E78;

                    byte[] data = null;
                    if (requiresDecryption) {

                        int blockSize = in.readInteger();

                        in.skip(blockSize); // skip first block, most likely header data

                        data = in.decodeBuffer(len - blockSize);
                    } else {
                        data = in.readBytes(len);
                    }

                    canvas.setValue(new PNG(width, height, format, data));

                    child = canvas;
                    break;
                case 'S':
                    switch (iname.charAt(1)) {
                        case 'h':
                            if (iname.equals("Shape2D#Vector2D")) {
                                Point vector = new Point();
                                vector.x = in.readCompressedInteger();
                                vector.y = in.readCompressedInteger();
                                child = new WzProperty<>(name, vector, Type.VECTOR);
                            } else {
                                child = new WzProperty<>(name, null, Type.CONVEX, true);
                                int ccount = in.readCompressedInteger();
                                for (int x = 0; x < ccount; x++) {
                                    child.addChild(parseExtended(in, name, offset));
                                }
                            }
                            break;
                        case 'o':
                            in.skip(1);
                            int sdl = in.readCompressedInteger();
                            int length = in.readCompressedInteger();
                            byte[] header = in.readBytes(82);
                            byte[] mp3 = in.readBytes(sdl - 82);
                            child = new WzProperty<>(name, new MP3(length, header, mp3), Type.SOUND);
                            break;
                        default:
                            break;
                    }
                    break;
                case 'U':
                    in.skip(1);
                    int ut = in.read();
                    String link = null;
                    switch (ut) {
                        case 0x00:
                            link = in.readString();
                            break;
                        case 0x01:
                            link = in.readString(offset + in.readInteger());
                            break;
                        default:
                            break;
                    }
                    if (link != null) {
                        child = new WzProperty<>(name, link, Type.UOL);
                    }
                    break;
                default:
                    System.out.printf("Unknown extended property type found %s.%n", iname);
                    break;
            }
        }
        return child;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public HashMap<String, WzProperty<?>> getChildren() {
        return children;
    }

    @Override
    public int compareTo(WzProperty<E> o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Type getPropertyType() {
        return pType;
    }

    @Override
    public void addChild(WzProperty<?> o) {
        if (children != null) {
            o.setParent(this);
            children.put(o.getName(), o);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WzProperty) {
            WzProperty<?> other = (WzProperty<?>) o;
            return other.pType.equals(pType) && other.name.equals(name)
                    && other.value.equals(value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.pType != null ? this.pType.hashCode() : 0);
        hash = 37 * hash + Objects.hashCode(this.name);
        return hash;
    }

    public static enum Type {

        NULL,
        UNSIGNED_SHORT,
        COMPRESSED_INTEGER,
        COMPRESSED_LONG,
        BYTE_FLOAT,
        DOUBLE,
        STRING,
        SUB_PROPERTY,
        CANVAS,
        VECTOR,
        CONVEX,
        SOUND,
        UOL,
        PNG;
    }
}
