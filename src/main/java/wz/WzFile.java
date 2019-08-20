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

import java.util.Map;
import java.util.Objects;
import wz.common.WzHeader;
import wz.io.WzInputStream;

/**
 *
 * @author Brenterino
 */
public final class WzFile extends WzObject<WzFile, WzObject<?, ?>> {

	private String name;
	private short version;
	private WzDirectory root;

	private WzFile() {
	}

	public WzFile(String wz, short ver) {
		name = wz;
		version = ver;
	}

	@Override
	public void parse(WzInputStream in) {
		WzHeader h = new WzHeader();
		h.IDENT = in.readStringByLen(4);
		h.FILE_SIZE = in.readInteger();
		in.skip(4); // just going to be 0
		h.FILE_START = in.readInteger();
		h.COPYRIGHT = in.readNullTerminatedString();
		in.setHeader(h);
		in.readShort(); // enc ver
		in.setHash(getVersionHash(version));
		root = new WzDirectory(name, h.FILE_START + 2, h.FILE_SIZE, 0);
		root.parse(in);
	}

	public int getVersionHash(int ver) {
		int ret = 0;
		String v = String.valueOf(ver);
		for (int i = 0; i < v.length(); i++) {
			ret *= 32;
			ret += (int) v.charAt(i);
			ret += 1;
		}
		return ret & 0xFFFFFFFF;
	}

	public WzDirectory getRoot() {
		return root;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Map<String, WzObject<?, ?>> getChildren() {
		return root.getChildren();
	}

	@Override
	public void addChild(WzObject<?, ?> o) {
	}

	@Override
	public int compareTo(WzFile o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof WzFile) {
			WzFile other = (WzFile) o;
			return other.name.equals(name) && other.version == version;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + Objects.hashCode(this.name);
		hash = 53 * hash + this.version;
		return hash;
	}
}
