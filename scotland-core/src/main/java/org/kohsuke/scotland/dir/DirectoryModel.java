package org.kohsuke.scotland.dir;

import java.util.Collection;

/**
 * Provides the model information to be rendered into HTML.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class DirectoryModel<T> {
    public abstract String getName(T node);
    public abstract String getUrl(T node);

    public abstract Collection<T> getChildren(T parent);
    public int getChildCount(T parent) {
        return getChildren(parent).size();
    }
    public T collapse(T parent) {
        Collection<T> children = getChildren(parent);
        if(children.size()==1)  return children.iterator().next();
        return null;
    }

    /**
     * Collapses all the way.
     */
    public T collapseStar(T parent) {
        T o=parent;
        while(true) {
            T n = collapse(o);
            if(n==null)     return o;
            o = n;
        }
    }
}
