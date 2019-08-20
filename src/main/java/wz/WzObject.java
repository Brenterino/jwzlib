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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import wz.io.WzInputStream;

/**
 * The parent representative of all entities that exist inside a WZ-formatted
 * file. <BR> Element E represent this object's core type and Element T
 * represents the contained element types.
 *
 * @author Brenterino
 */
public abstract class WzObject<E extends WzObject, T extends WzObject<?, ?>> implements Comparable<E>, Iterable<T> {

    private WzObject par = null;

    public WzObject getParent() {
        return par;
    }

    public void setParent(WzObject o) {
        par = o;
    }
    
    public E resolve() {
        return (E) this;
    }

    public String getFullPath() {
        return getFullPath(new StringBuilder(getName()));
    }

    private String getFullPath(StringBuilder sb) {
        WzObject cPar = par;
        while (cPar != null) {
            sb.insert(0, "/");
            sb.insert(0, cPar.getName());
            cPar = cPar.getParent();
        }
        return sb.toString();
    }

    public WzObject getChildByPath(String name) {
        String[] split = name.split("/");
        WzObject cur = this;
        for (String child : split) {
            cur = cur.getChild(child);
            if (cur == null) {
                break;
            }
        }
        return cur;
    }

    public T getChild(String name) {
        if (getChildren() != null) {
            return getChildren().get(name);
        }
        return null;
    }

    // UOL-specific
    public WzProperty<?> resolveLink(String path) {
        String[] split = path.split("/");
        WzObject cur = getParent(); // supposed to be parent (?)
        for (String s : split) {
            if (s.equals("..")) {
                cur = cur.getParent();
            } else {
                cur = cur.getChild(s);
            }
        }
        return (WzProperty<?>) cur;
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    public abstract void parse(WzInputStream in);

    public abstract String getName();

    public abstract void addChild(T o);

    public abstract Map<String, T> getChildren();

    @Override
    public abstract int compareTo(E o);

    @Override
    public final Iterator<T> iterator() {
        Collection<T> children = getChildren().values();
        if (children != null) {
            return children.iterator();
        }
        return Collections.emptyIterator();
    }
}
