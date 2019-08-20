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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Brenterino
 */
public final class GenericUtil {

    private GenericUtil() {
    }

    // probably better way of doing this
    public static Map combineMaps(Map m1, Map m2) {
        HashMap ret = new HashMap();
        ret.putAll(m1);
        ret.putAll(m2);
        return ret;
    }
}
