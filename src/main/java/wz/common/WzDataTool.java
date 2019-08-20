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

import java.awt.Point;
import wz.WzObject;
import wz.WzProperty;
import wz.WzProperty.Type;

/**
 *
 * @author Brenterino
 */
public final class WzDataTool {

	private WzDataTool() {
	}

	private static WzProperty getProperty(WzObject o, Type p) {
		if (o != null) {
			if (o instanceof WzProperty) {
				WzProperty prop = (WzProperty) o;
				if (prop.getPropertyType().equals(p)) {
					return prop;
				} else if (prop.getPropertyType().equals(Type.UOL)) {
					WzProperty link = (WzProperty) prop.resolveLink((String) prop.getValue());
					while (link != null && link.getPropertyType().equals(Type.UOL)) {
						link = (WzProperty) link.getValue();
					}
					if (link != null && link.getPropertyType().equals(p)) {
						return link;
					}
				}
			}
		}
		return null;
	}

	public static short getShort(WzProperty obj, short def) {
		if (obj != null && obj.getPropertyType().equals(Type.UNSIGNED_SHORT)) {
			return (Short) obj.getValue();
		}
		return def;
	}

	public static short getShort(WzObject src, String path, short def) {
		return getShort(getProperty(src.getChildByPath(path), Type.UNSIGNED_SHORT), def);
	}

	public static float getFloat(WzProperty obj, float def) {
		if (obj != null && obj.getPropertyType().equals(Type.BYTE_FLOAT)) {
			return (Float) obj.getValue();
		}
		return def;
	}

	public static float getFloat(WzObject src, String path, float def) {
		return getFloat(getProperty(src.getChildByPath(path), Type.BYTE_FLOAT), def);
	}

	public static int getInteger(WzProperty obj, int def) {
		if (obj != null && obj.getPropertyType().equals(Type.COMPRESSED_INTEGER)) {
			return (Integer) obj.getValue();
		}
		return def;
	}

	public static int getInteger(WzObject src, String path, int def) {
		return getInteger(getProperty(src.getChildByPath(path), Type.COMPRESSED_INTEGER), def);
	}

	public static double getDouble(WzProperty obj, double def) {
		if (obj != null && obj.getPropertyType().equals(Type.DOUBLE)) {
			return (Double) obj.getValue();
		}
		return def;
	}

	public static double getDouble(WzObject src, String path, double def) {
		return getDouble(getProperty(src.getChildByPath(path), Type.DOUBLE), def);
	}

	public static String getString(WzProperty obj, String def) {
		if (obj != null && obj.getPropertyType().equals(Type.STRING)) {
			return (String) obj.getValue();
		}
		return def;
	}

	public static String getString(WzObject src, String path, String def) {
		return getString(getProperty(src.getChildByPath(path), Type.STRING), def);
	}

	public static Point getVector(WzProperty obj, Point def) {
		if (obj != null && obj.getPropertyType().equals(Type.VECTOR)) {
			return (Point) obj.getValue();
		}
		return def;
	}

	public static Point getVector(WzObject src, String path, Point def) {
		return getVector(getProperty(src.getChildByPath(path), Type.VECTOR), def);
	}

	public static int getIntegerConvert(WzProperty obj, int def) {
		String s = getString(obj, null);
		if (s == null) {
			return getInteger(obj, def);
		}
		return Integer.valueOf(s);
	}

	public static int getIntegerConvert(WzObject src, String path, int def) {
		WzObject obj = src.getChildByPath(path);
		if (obj instanceof WzProperty) {
			return getIntegerConvert((WzProperty) obj, def);
		}
		return def;
	}

	public static boolean getBoolean(WzObject src, String path, boolean def) {
		int intVal = getInteger(src, path, -1);
		if (intVal == -1) {
			short sVal = getShort(src, path, (short) -1);
			if (sVal != -1) {
				return sVal > 0;
			}
		} else {
			return intVal > 0;
		}
		return def;
	}

	public static PNG getPNG(WzProperty obj, PNG def) {
		if (obj != null && obj.getPropertyType().equals(Type.CANVAS)) {
			return (PNG) obj.getValue();
		}
		return def;
	}

	public static PNG getPNG(WzObject src, String path, PNG def) {
		return getPNG(getProperty(src.getChildByPath(path), Type.CANVAS), def);
	}

	public static MP3 getMP3(WzProperty obj, MP3 def) {
		if (obj != null && obj.getPropertyType().equals(Type.SOUND)) {
			return (MP3) obj.getValue();
		}
		return def;
	}

	public static MP3 getMP3(WzObject src, String path, MP3 def) {
		return getMP3(getProperty(src.getChildByPath(path), Type.SOUND), def);
	}
}
