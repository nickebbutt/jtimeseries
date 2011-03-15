package com.od.jtimeseries.util.identifiable;

import com.od.jtimeseries.util.identifiable.IdentifiablePathUtils;

import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
* User: Nick Ebbutt
* Date: 09-Jun-2010
* Time: 09:45:59
*/
public class PathParser {

    private StringBuilder path;

    public PathParser(String path) {
        this.path = new StringBuilder(path);
    }

    public String removeFirstNode() {
        String result;
        if ( path.length() == 0 ) {
            throw new UnsupportedOperationException("Cannot remove a node from an empty path");
        }
        int index = path.indexOf(IdentifiablePathUtils.NAMESPACE_SEPARATOR);
        if ( index == -1) {
            result = path.toString();
            path.setLength(0);
        } else {
            result = path.substring(0, index);
            path.delete(0, index + 1);
        }
        return result;
    }

    public String removeLastNode() {
        String result;
        if ( path.length() == 0 ) {
            throw new UnsupportedOperationException("Cannot remove a node from an empty path");
        }
        int index = path.lastIndexOf(IdentifiablePathUtils.NAMESPACE_SEPARATOR);
        if ( index == -1 ) {
            result = path.toString();
            path.setLength(0);
        } else {
            result = path.substring(index + 1, path.length());
            path.delete(index, path.length());
        }
        return result;
    }


    public String getRemainingPath() {
        return path.toString();
    }

    public boolean isSingleNode() {
        int index = path.indexOf(IdentifiablePathUtils.NAMESPACE_SEPARATOR);
        return index == -1 && path.length() > 0;
    }

    public boolean isEmpty() {
        return path.toString().trim().length() == 0;
    }

    public static LinkedList<String> splitPath(String path) {
        LinkedList<String> l = new LinkedList<String>();
        StringTokenizer st = new StringTokenizer(path, IdentifiablePathUtils.NAMESPACE_SEPARATOR);
        while(st.hasMoreTokens()) {
            l.add(st.nextToken());
        }
        return l;
    }
}