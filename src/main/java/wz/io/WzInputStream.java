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
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;
import wz.common.WzHeader;
import wz.common.WzTool;

/**
 *
 * @author Brenterino
 */
public abstract class WzInputStream {

	protected int hash;
	protected byte[] key;
	protected WzHeader header;
	protected ThreadLocal<AtomicInteger> positions =
			new ThreadLocal<AtomicInteger>() {

				@Override
				protected AtomicInteger initialValue() {
					return new AtomicInteger(0);
				}
			};

	protected WzInputStream() {
	}

	public void setHash(int h) {
		hash = h;
	}

	public int getHash() {
		return hash;
	}

	public void setHeader(WzHeader head) {
		header = head;
	}

	public WzHeader getHeader() {
		return header;
	}

	public void setKey(byte[] data) {
		key = data;
	}

	public byte[] getKey() {
		return key;
	}

	public final Integer getPosition() {
		return positions.get().get();
	}

	public final int skip(int inc) {
		return positions.get().getAndAdd(inc);
	}

	public final void seek(int off) {
		positions.get().set(off);
	}

	public final void removePosition() {
		positions.remove();
	}

	public abstract int read(int off);
	public abstract byte readByte(int off);

	public int read() {
		return read(skip(1));
	}

	public byte readByte() {
		return readByte(skip(1));
	}

	public short readShort() {
		return (short) (read() + (read() << 8));
	}

	public int readInteger() {
		return read() + (read() << 8) + (read() << 16) + (read() << 24);
	}

	public long readLong() {
		return (long) read()        +
			  ((long) read() << 8)  +
			  ((long) read() << 16) +
			  ((long) read() << 24) +
			  ((long) read() << 32) +
			  ((long) read() << 40) +
			  ((long) read() << 48) +
			  ((long) read() << 56);
	}

	public char readChar() {
		return (char) readShort();
	}

	public float readFloat() {
		return Float.intBitsToFloat(readInteger());
	}

	public double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	public int readCompressedInteger() {
		byte b = readByte();
		if (b == -128) {
			return readInteger();
		}
		return (int) b;
	}

	public long readCompressedLong() {
		byte b = readByte();
		if (b == -128) {
			return readLong();
		}
		return (long) b;
	}

	public double readCompressedDouble() {
		byte b = readByte();
		if (b == -128) {
			return readDouble();
		}
		return (double) b;
	}

	public byte[] readBytes(int len) {
		byte[] ret = new byte[len];
		for (int i = 0; i < len; i++) {
			ret[i] = readByte();
		}
		return ret;
	}

	public String readStringByLen(int len) {
		return new String(readBytes(len));
	}

	public String readNullTerminatedString() {
		StringBuilder ret = new StringBuilder();
		byte cur = readByte();
		while (cur != 0) {
			ret.append((char) cur);
			cur = readByte();
		}
		return ret.toString();
	}

	/**
	 * Reads an encrypted string.
	 *
	 * @return the decrypted string read.
	 */
	public String readString() {
		int strLength;
		byte b = readByte();
		if (b == 0x00) {
			return "";
		}
		if (b > 0x00) {
			if (b == 0x7F) {
				strLength = readInteger();
			} else {
				strLength = (int) b;
			}
			if (strLength < 0) {
				return "";
			}
			return decryptUnicodeStr(readBytes(strLength * 2));
		} else {
			if (b == -128) {
				strLength = readInteger();
			} else {
				strLength = -b;
			}
			if (strLength < 0) {
				return "";
			}
			return decryptAsciiStr(readBytes(strLength));
		}
	}

	/**
	 * Decrypts an ASCII string.
	 *
	 * @param str the bytes that are used to create the decrypted string.
	 * @return the decrypted string.
	 */
	public String decryptAsciiStr(byte[] str) {
		byte a = (byte) 0xAA;
		for (int i = 0; i < str.length; i++) {
			byte keyNum = key == null ? 0 : key[i];
			str[i] = (byte) (str[i] ^ a ^ keyNum);
			a++; // changes the bits that get flipped
		}
		return new String(str);
	}

	/**
	 * Decrypts a Unicode string.
	 *
	 * @param str the bytes that are used to create the decrypted string.
	 * @return the decrypted string.
	 */
	public String decryptUnicodeStr(byte[] str) {
		int invFlip = 0xAAAA;
		char[] charRet = new char[str.length / 2];
		for (int i = 0; i < str.length; i++) {
			str[i] = (byte) (str[i] ^ key[i]);
		}
		for (int i = 0; i < (str.length / 2); i++) {
			char toXor = (char) ((str[i] << 8) | str[i + 1]);
			charRet[i] = (char) (toXor ^ invFlip);
			invFlip++; // changes the bits that get flipped
		}
		return String.valueOf(charRet);
	}

	public int readOffset() {
		int off = 0xFFFFFFFF & getPosition(); // current position
		off -= header.FILE_START; // subtract file start from position (offset relative to file start)
		off = ~off; // 1s compliment of the relative offset
		off *= hash; // multiplies 1s compliment by the hash
		off -= 0x581C3F6D; // subtract offset constant from the offset
		off = WzTool.rotateLeft(off, (byte) (off & 0x1F)); // rotates the offset in bits to the left by the value of the 7 least significant bits of the integer offset
		off ^= readInteger(); // XOR with the integer value at the current position
		off &= 0xFFFFFFFF; // java unsigned magic
		off += header.FILE_START * 2; // add the file's starting offset twice
		return off;
	}

	public String readStringBlock(int offset) {
		int s = read();
		switch (s) {
			case 0x00:
			case 0x73:
				return readString();
			case 0x01:
			case 0x1B:
				return readString(offset + readInteger());
			default:
				return Integer.toHexString(s);
		}
	}

	public String readString(int off) {
		return readString(off, false);
	}

	public String readString(int off, boolean rb) {
		int coff = getPosition();
		seek(off);
		if (rb) {
			readByte();
		}
		String ret = readString();
		seek(coff);
		return ret;
	}

	public boolean readBool() {
		return readByte() != 0;
	}

	public byte[] decodeBuffer(int len) {
		byte[] ret;
		byte[] input = readBytes(len);

		ByteBuffer in = ByteBuffer.wrap(input);

		in.order(ByteOrder.LITTLE_ENDIAN);

		ByteBuffer out = ByteBuffer.allocate(len);

		int blockSize;
		while (in.remaining() > 0) {
			blockSize = in.getInt();

			if (blockSize > in.remaining() || blockSize < 0) {
				System.out.println("Critical Error - Block Size for Reading Buffer is Wrong: " + blockSize);
				break;
			}

			for (int i = 0; i < blockSize; i++) {
				out.put((byte) (in.get() ^ key[i]));
			}
		}

		ret = new byte[out.position()];

		out.rewind();

		out.get(ret);

		return ret;
	}
}
