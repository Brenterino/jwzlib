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

/**
 *
 * @author Brenterino
 */
public final class WzHeader {

    public String IDENT = null;
    public String COPYRIGHT = null;
    public int FILE_SIZE = -1;
    public int FILE_START = -1;

    public WzHeader() {
    }

    public static WzHeader getDefault() {
        WzHeader h = new WzHeader();
        h.IDENT = "PKG1";
        h.COPYRIGHT = "Package file v1.0 Copyright 2002 Wizet, ZMS";
        h.FILE_START = 60;
        h.FILE_SIZE = 0;
        return h;
    }
}
