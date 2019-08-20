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

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;

/**
 *
 * @author Brenterino
 */
@Deprecated
public final class WzAsynchInputStream extends WzInputStream {

    private final AsynchronousFileChannel fc;

    public WzAsynchInputStream(Path p) throws Exception {
        fc = AsynchronousFileChannel.open(p);
    }

    @Override
    public int read(int off) {
        ByteBuffer buf = ByteBuffer.allocate(1);
        try {
            if (fc.read(buf, off).get() != 1) {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
        return 0xFF & buf.get(0);
    }

    @Override
    public byte readByte(int off) {
        ByteBuffer buf = ByteBuffer.allocate(1);
        try {
            if (fc.read(buf, off).get() != 1) {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
        return buf.get(0);
    }
}
