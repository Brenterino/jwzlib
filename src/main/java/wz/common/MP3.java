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
package wz.common;

import java.util.Arrays;

/**
 *
 * @author Brenterino
 */
public final class MP3 {

	private int length;
	private byte[] data;
	private byte[] header;

	public MP3(int len, byte[] h, byte[] d) {
		data = d;
		header = h;
		length = len;
	}

	public int getLength() {
		return length;
	}

	public byte[] getData() {
		return data;
	}

	public byte[] getHeader() {
		return header;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 71 * hash + this.length;
		hash = 71 * hash + Arrays.hashCode(this.data);
		hash = 71 * hash + Arrays.hashCode(this.header);
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MP3) {
			MP3 other = (MP3) o;
			return other.length == length && Arrays.equals(other.header , header)
					&& Arrays.equals(other.data, data);
		}
		return false;
	}
}
