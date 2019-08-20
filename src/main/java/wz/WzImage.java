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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import wz.io.WzInputStream;
import wz.io.WzMappedInputStream;

/**
 *
 * @author Brenterino
 */
public final class WzImage extends WzObject<WzImage, WzProperty<?>> {

	private String name;
	private int offset;
	private int checksum;
	private int blocksize;
	private boolean parsed;
	private WzInputStream store;
	private boolean alone = false;
	private final HashMap<String, WzProperty<?>> children;

	public WzImage(String n, WzInputStream in) {
		this(n, 4, 0, 0);
		store = in;
		alone = true;
	}

	public WzImage(String n, int off, int sz, int cs) {
		name = n;
		children = new HashMap<>();
		checksum = cs;
		blocksize = sz;
		offset = off;
	}

	public void setStoredInput(WzInputStream in) {
		store = in;
	}

	@Override
	public void parse(WzInputStream in) {
		if (parsed) {
			return;
		}
		if (alone) {
			in.setHash(in.readInteger());
		} else {
			in.seek(offset);
		}
		byte b = in.readByte();
		if (b != 0x73 || !in.readString().equals("Property") || in.readShort() != 0) {
			System.out.printf("Incorrect offset detected for Image %s: %s%n", getName(), offset);
			return;
		}
		try {
			WzProperty.parse(in, offset, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		parsed = true;
	}

	public void exportImage(Path dst) throws IOException {
		if (!alone && store != null) {
			ByteBuffer data = null;
			if (store instanceof WzMappedInputStream) {
				WzMappedInputStream in = (WzMappedInputStream) store;
				data = in.subMap(offset, blocksize);
			}
			File f = dst.toFile();
			if (f.exists()) {
				f.delete();
			}
			f.createNewFile();
			FileOutputStream fout = new FileOutputStream(f);
			ByteBuffer bb = ByteBuffer.allocate(4);
			bb.order(ByteOrder.LITTLE_ENDIAN).putInt(store.getHash());
			fout.write(bb.array());
			if (data != null) {
				byte[] raw = new byte[data.remaining()];
				data.get(raw, 0, data.remaining());
				fout.write(raw);
				fout.flush();
				raw = null;
			} else {
				store.seek(offset);
				fout.write(store.readBytes(blocksize));
				fout.flush();
			}
			fout.close();
			bb = null;
			data = null;
		}
	}

	@Override
	public String getName() {
		return name;
	}

	public int getChecksum() {
		return checksum;
	}

	public int getBlocksize() {
		return blocksize;
	}

	public void unparse() {
		if (parsed) {
			children.clear();
			parsed = false;
		}
	}

	public void forceUnknownHash() { // for images that do not have the hash value included
		alone = false;
		offset = 0;
	}

	@Override
	public WzProperty<?> getChild(String name)
	{
		return getChildren().get(name);
	}

	@Override
	public Map<String, WzProperty<?>> getChildren() {
		if (!parsed) {
			parse(store);
		}
		return children;
	}

	@Override
	public void addChild(WzProperty<?> o) {
		o.setParent(this);
		children.put(o.getName(), o);
	}

	@Override
	public int compareTo(WzImage o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof WzImage) {
			WzImage other = (WzImage) o;
			return other.name.equals(name) && other.blocksize == blocksize && other.checksum == checksum
					&& other.offset == offset;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + Objects.hashCode(this.name);
		hash = 97 * hash + this.offset;
		hash = 97 * hash + this.checksum;
		hash = 97 * hash + this.blocksize;
		hash = 97 * hash + (this.parsed ? 1 : 0);
		return hash;
	}
}
