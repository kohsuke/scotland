package org.kohsuke.scotland.extensibility;

import net.sf.json.JSONObject;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.ArrayList;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.scotland.extensibility.Descriptor.FormException;

/**
 * List of {@link Descriptor}s.
 *
 * <p>
 * This class is really just a list but also defines
 * some Hudson specific methods that operate on
 * {@link Descriptor} list.
 *
 * @author Kohsuke Kawaguchi
 */
public final class DescriptorList<T extends Describable> extends CopyOnWriteArrayList<Descriptor<? extends T>> {
    public DescriptorList(Descriptor<T>... descriptors) {
        super(descriptors);
    }

    /**
     * Creates a new instance of a {@link Describable}
     * from the structured form submission data posted
     * by a radio button group.
     */
    public T newInstanceFromRadioList(JSONObject config) throws FormException {
        if(config.isNullObject())
            return null;    // none was selected
        int idx = config.getInt("value");
        return get(idx).newInstance(Stapler.getCurrentRequest(),config);
    }

    public T newInstanceFromRadioList(JSONObject parent, String name) throws FormException {
        return newInstanceFromRadioList(parent.getJSONObject(name));
    }

    /**
     * No-op method used to force the class initialization of the given class.
     * The class initialization in turn is expected to put the descriptor
     * into the {@link DescriptorList}.
     *
     * <p>
     * This is necessary to resolve the class initialization order problem.
     * Often a {@link DescriptorList} is defined in the base class, and
     * when it tries to initialize itself by listing up descriptors of known
     * sub-classes, they might not be available in time.
     */
    public void load(Class<? extends Describable> c) {
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);  // Can't happen
        }
    }

    /**
     * Obtains a sublist by specifying the lower bound type.
     */
    public <U extends T> List<Descriptor<? extends U>> subList(Class<U> subtype) {
        List<Descriptor<? extends U>> r = new ArrayList<Descriptor<? extends U>>();
        for (Descriptor<? extends T> d : this) {
            if(subtype.isAssignableFrom(d.clazz))
                r.add(d.as(subtype));
        }
        return r;
    }
}
