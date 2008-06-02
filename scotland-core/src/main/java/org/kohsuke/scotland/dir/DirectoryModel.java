package org.kohsuke.scotland.dir;

import java.util.Collection;

/**
 * Provides the model information to be rendered into HTML.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class DirectoryModel<T> {
    /**
     * Base URL of all the directory links.
     */
    public final String baseUrl;

    /**
     * URL to the {@link DirectoryModel} object itself.
     * This is used later for AJAX calls to expand sub-trees.
     */
    public final String selfUrl;

    /**
     * Equivalent of the "current directory"
     */
    public final T current;

    protected DirectoryModel(T current, String baseUrl, String selfUrl) {
        this.current = current;
        this.baseUrl = baseUrl;
        this.selfUrl = selfUrl;
    }

    public abstract String getName(T node);
    public abstract String getUrl(T node);
    public abstract Collection<T> getChildren(T parent);
    public abstract T getChild(T parent, String name);

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
