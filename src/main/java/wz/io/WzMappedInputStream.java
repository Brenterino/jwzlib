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
package wz.io;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

/**
 *
 * @author Brenterino
 */
public final class WzMappedInputStream extends WzInputStream {

	private ByteBuffer data;
	private RandomAccessFile f;

	public WzMappedInputStream(Path p) {
		try {
			f = new RandomAccessFile(p.toFile(), "r");
			data = f.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, f.length()).order(ByteOrder.LITTLE_ENDIAN);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int read(int off) {
		try {
			return 0xFF & data.get(off);
		} catch (Exception e) {
			System.out.println("Tried seeking to invalid offset: " + off);
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public byte readByte(int off) {
		return data.get(off);
	}

	public ByteBuffer subMap(int off, int length) throws IOException {
		return f.getChannel().map(FileChannel.MapMode.READ_ONLY, off, length);
	}
}
