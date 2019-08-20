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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import wz.io.WzInputStream;
import wz.util.GenericUtil;

/**
 *
 * @author Brenterino
 */
public final class WzDirectory extends WzObject<WzDirectory, WzObject<?, ?>> {

	private String name;
	private int checksum;
	private int blocksize;
	private int offset = 0;
	private Map<String, WzObject<?, ?>> combined;
	private final HashMap<String, WzObject<WzImage, ?>> images;
	private final HashMap<String, WzObject<WzDirectory, ?>> directories;

	public WzDirectory(String n) {
		directories = new LinkedHashMap<>();
		images = new LinkedHashMap<>();
		name = n;
	}

	public WzDirectory(String n, int off, int size, int cs) {
		directories = new LinkedHashMap<>();
		images = new HashMap<>();
		name = n;
		offset = off;
		checksum = cs;
		blocksize = size;
	}

	@Override
	public void parse(WzInputStream in) {
		in.seek(offset);
		int count = in.readCompressedInteger();
		for (int i = 0; i < count; i++) {
			byte t = in.readByte();
			String n;
			int fsize, cs;
			int rem;
			if (t == 1) {
				in.readInteger();
				in.readShort();
				in.readInteger();
				continue;
			} else if (t == 2) {
				int sOff = in.readInteger();
				rem = in.getPosition();
				in.seek(sOff + in.getHeader().FILE_START);
				t = in.readByte();
				n = in.readString();
			} else if (t == 3 || t == 4) {
				n = in.readString();
				rem = in.getPosition();
			} else {
				System.out.printf("parent=%s,name=%s,offset=%s,pos=%s,t=%s%n",
						getName(), "unk", offset, in.getPosition(), t);
				continue;
			}
			in.seek(rem);
			fsize = in.readCompressedInteger();
			cs = in.readCompressedInteger();
			int off = in.readOffset();
			switch (t) {
				case 0x03:
					WzDirectory d = new WzDirectory(n, off, fsize, cs);
					directories.put(d.getName(), d);
					d.setParent(this);
					break;
				default:
					WzImage img = new WzImage(n, off, fsize, cs);
					img.setStoredInput(in);
					images.put(img.getName(), img);
					img.setParent(this);
					break;
			}
		}
		for (WzObject<?, ?> d : directories.values()) {
			d.parse(in);
		}
	}

	public int getChecksum() {
		return checksum;
	}

	public int getBlocksize() {
		return blocksize;
	}

	@Override
	public String getName() {
		return name;
	}

	public Map<String, WzObject<WzImage, ?>> getImages() {
		return images;
	}

	public Map<String, WzObject<WzDirectory, ?>> getDirectories() {
		return directories;
	}

	public WzObject<?, ?> getImage(String name) {
		return images.get(name);
	}

	public WzObject<?, ?> getDirectory(String name) {
		return directories.get(name);
	}

	@Override
	public Map<String, WzObject<?, ?>> getChildren() {
		if (combined == null) {
			combined = GenericUtil.combineMaps(images, directories);
		}
		return combined;
	}

	@Override
	public int compareTo(WzDirectory o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void addChild(WzObject<?, ?> o) {
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof WzDirectory) {
			WzDirectory other = (WzDirectory) o;
			return other.blocksize == blocksize && other.checksum == checksum && other.name.equals(name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + Objects.hashCode(this.name);
		hash = 17 * hash + this.checksum;
		hash = 17 * hash + this.blocksize;
		hash = 17 * hash + this.offset;
		return hash;
	}
}
