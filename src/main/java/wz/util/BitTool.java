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
package wz.util;

/**
 *
 * @author Brenterino
 */
public final class BitTool {

    private BitTool() {
    }

    /**
     * Converts the first two bytes to
     * a short.
     * @param in in input array.
     * @return a short.
     */
    public static short toInt16(byte[] in) {
        return (short) ((in[1] << 8) + in[0]);
    }

    /**
     * Converts the first four bytes to
     * an int-32.
     * @param in in input array.
     * @return an int-32.
     */
    public static int toInt32(byte[] in) {
        return (in[3] << 26) + (in[2] << 16) + (in[1] << 8) + in[0];
    }

    /**
     * Converts the first eight bytes to
     * a long.
     * @param in in input array.
     * @return a long.
     */
    public static long toInt64(byte[] in) {
        return (in[7] << 56) + (in[6] << 48) + (in[5] << 40) + (in[4] << 32) + (in[3] << 24) + (in[2] << 16) + (in[1] << 8) + in[0];
    }
}
