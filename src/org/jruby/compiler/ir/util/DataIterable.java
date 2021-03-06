/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jruby.compiler.ir.util;

import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author enebo
 */
public class DataIterable<T extends DataInfo> implements Iterable<T> {
    private Set<Edge<T>> edges;
    private Object type;
    private boolean negate;
    
    public DataIterable(Set<Edge<T>> edges, Object type) {
        this(edges, type, false);
        
    }
    
    public DataIterable(Set<Edge<T>> edges, Object type, boolean negate) {
        this.edges = edges;
        this.type = type;
        this.negate = negate;
    }

    public Iterator<T> iterator() {
        return new DataIterator<T>(edges, type, negate);
    }
}
