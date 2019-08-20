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

import wz.io.WzInputStream;
import wz.util.AES;
import wz.util.BitTool;

/**
 *
 * @author Brenterino
 */
public final class WzTool {

	private static final AES c = new AES();
	private static final byte[] AES_KEY = new byte[]{
		(byte) 0x13, 0x00, 0x00, 0x00,
		(byte) 0x08, 0x00, 0x00, 0x00,
		(byte) 0x06, 0x00, 0x00, 0x00,
		(byte) 0xB4, 0x00, 0x00, 0x00,
		(byte) 0x1B, 0x00, 0x00, 0x00,
		(byte) 0x0F, 0x00, 0x00, 0x00,
		(byte) 0x33, 0x00, 0x00, 0x00,
		(byte) 0x52, 0x00, 0x00, 0x00
	};

	private WzTool() {
	}

	/**
	 * Fetches an IV by WzVersion.
	 *
	 * @param v the version of the selected IV.
	 * @return the IV corresponding to this specific WzVersion.
	 */
	public static byte[] getIvByVersion(WzVersion v) {
		switch (v) {
			case GMS:
				return new byte[]{0x4D, 0x23, (byte) 0xC7, 0x2B,
							0x4D, 0x23, (byte) 0xC7, 0x2B,
							0x4D, 0x23, (byte) 0xC7, 0x2B,
							0x4D, 0x23, (byte) 0xC7, 0x2B};
			case EMS:
				return new byte[]{(byte) 0xB9, 0x7D, 0x63, (byte) 0xE9,
							(byte) 0xB9, 0x7D, 0x63, (byte) 0xE9,
							(byte) 0xB9, 0x7D, 0x63, (byte) 0xE9,
							(byte) 0xB9, 0x7D, 0x63, (byte) 0xE9};
			default:
				return new byte[16];
		}
	}

	/**
	 * Rotates an uint (as a long) to the left.
	 *
	 * @param x the uint to rotate.
	 * @param n how many bytes to rotate.
	 * @return the rotated uint.
	 */
	public static long rotateLeft(long x, byte n) {
		return (((x) << (n)) | ((x) >>> (32 - (n))));
	}

	public static int rotateLeft(int x, byte n) {
		return (x << n) | (x >>> (32 - n));
	}

	public static long rotateRight(long x, byte n) {
		return (((x) >>> (n)) | ((x) << ((32) - (n))));
	}

	/**
	 * Determines whether or not the specific InputStream loaded is a List.wz
	 * file.
	 *
	 * @param in the InputStream to determine whether it reads from the List.wz
	 * file.
	 * @return whether or not the file is a list.
	 */
	public static boolean isList(WzInputStream in) {
		in.seek(0);
		return in.readInteger() != 0x31474B50;
	}

	/**
	 * Generates a key using an IV.
	 *
	 * @param iv IV used to generate a WZ key.
	 * @return the WZ key generated.
	 */
	public static byte[] generateKey(byte[] iv) {
		return generateKey(iv, AES_KEY);
	}

	public static byte[] generateKey(WzVersion v) {
		return generateKey(WzTool.getIvByVersion(v), AES_KEY);
	}

	/**
	 * Generates a key using an IV and AES key.
	 *
	 * @param iv the IV used to generate the key.
	 * @param aeskey the AES key used to generated the key.
	 * @return the WZ key generated.
	 */
	public static byte[] generateKey(byte[] iv, byte[] aeskey) {
		byte[] ret = new byte[0x200000];
		if (BitTool.toInt32(iv) == 0) {
			return ret;
		}
		c.setKey(aeskey);
		for (int i = 0; i < (0xFFFF / 16); i++) {
			iv = c.encrypt(iv);
			System.arraycopy(iv, 0, ret, (i * 16), 16);
		}
		iv = c.encrypt(iv);
		System.arraycopy(iv, 0, ret, 65520, 15);
		return ret;
	}
}
