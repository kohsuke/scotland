package org.kohsuke.scotland.extensibility;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.scotland.xstream.XmlFile;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Metadata about a configurable instance.
 *
 * <p>
 * {@link Descriptor} is an object that has metadata about a {@link Describable}
 * object, and also serves as a factory (in a way this relationship is similar
 * to {@link Object}/{@link Class} relationship.
 *
 * A {@link Descriptor}/{@link Describable}
 * combination is used throughout in Hudson to implement a
 * configuration/extensibility mechanism.
 *
 * <p>
 * {@link Descriptor} also usually have its associated views.
 *
 *
 * <h2>Persistence</h2>
 * <p>
 * {@link Descriptor} can persist data just by storing them in fields.
 * However, it is the responsibility of the derived type to properly
 * invoke {@link #save()} and {@link #load()}.
 *
 * @author Kohsuke Kawaguchi
 * @see Describable
 */
public abstract class Descriptor<T extends Describable> {
    /**
     * The class being described by this descriptor.
     */
    public transient final Class<? extends T> clazz;

    private transient final Map<String, Method> checkMethods = new ConcurrentHashMap<String,Method>();

    private transient final File dir;

    protected Descriptor(File dir, Class<? extends T> clazz) {
        this.dir = dir;
        this.clazz = clazz;
        ALL.add(this);
        // doing this turns out to be very error prone,
        // as field initializers in derived types will override values.
        // load();
    }

    /**
     * Human readable name of this kind of configurable object.
     */
    public abstract String getDisplayName();

    /**
     * If the field "xyz" of a {@link Describable} has the corresponding "doCheckXyz" method,
     * return the form-field validation string. Otherwise null.
     * <p>
     * This method is used to hook up the form validation method to
     */
    public String getCheckUrl(String fieldName) {
        String capitalizedFieldName = StringUtils.capitalize(fieldName);

        Method method = checkMethods.get(fieldName);
        if(method==null) {
            method = NONE;
            String methodName = "doCheck"+ capitalizedFieldName;
            for( Method m : getClass().getMethods() ) {
                if(m.getName().equals(methodName)) {
                    method = m;
                    break;
                }
            }
            checkMethods.put(fieldName,method);
        }

        if(method==NONE)
            return null;

        return '\''+ Stapler.getCurrentRequest().getContextPath()+"/descriptor/"+clazz.getName()+"/check"+capitalizedFieldName+"?value='+encode(this.value)";
    }

    /**
     * Creates a configured instance from the submitted form.
     *
     * <p>
     * The default implementation of this method does the following:
     * <pre>
     * req.bindJSON(clazz,formData);
     * </pre>
     * <p>
     * ... which performs the databinding on the constructor of {@link #clazz}.
     *
     * @param req
     *      Always non-null. This object includes represents the entire submisison.
     * @param formData
     *      The JSON object that captures the configuration data for this {@link Descriptor}.
     *      See http://hudson.gotdns.com/wiki/display/HUDSON/Structured+Form+Submission
     *
     * @throws FormException
     *      Signals a problem in the submitted form.
     */
    public T newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        return req.bindJSON(clazz,formData);
    }

    /**
     * Returns the resource path to the help screen HTML, if any.
     *
     * <p>
     * This value is relative to the context root of Hudson, so normally
     * the values are something like <tt>"/plugin/emma/help.html"</tt> to
     * refer to static resource files in a plugin, or <tt>"/publisher/EmmaPublisher/abc"</tt>
     * to refer to Jelly script <tt>abc.jelly</tt> or a method <tt>EmmaPublisher.doAbc()</tt>.
     *
     * @return
     *      null to indicate that there's no help.
     */
    public String getHelpFile() {
        return null;
    }

    /**
     * Checks if the given object is created from this {@link Descriptor}.
     */
    public final boolean isInstance( T instance ) {
        return clazz.isInstance(instance);
    }

    /**
     * Invoked when the global configuration page is submitted.
     *
     * Can be overriden to store descriptor-specific information.
     *
     * @return false
     *      to keep the client in the same config page.
     */
    public boolean configure( StaplerRequest req ) throws FormException {
        return true;
    }

    public String getConfigPage() {
        return getViewPage(clazz, "config.jelly");
    }

    public String getGlobalConfigPage() {
        return getViewPage(clazz, "global.jelly");
    }

    protected final String getViewPage(Class<?> clazz, String pageName) {
        return '/'+ clazz.getName().replace('.','/').replace('$','/')+"/"+ pageName;
    }


    /**
     * Saves the configuration info to the disk.
     */
    protected synchronized void save() {
        try {
            getConfigFile().write(this);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save "+getConfigFile(),e);
        }
    }

    /**
     * Loads the data from the disk into this object.
     *
     * <p>
     * The constructor of the derived class must call this method.
     * (If we do that in the base class, the derived class won't
     * get a chance to set default values.)
     */
    protected synchronized void load() {
        XmlFile file = getConfigFile();
        if(!file.exists())
            return;

        try {
            file.unmarshal(this);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load "+file, e);
        }
    }

    private XmlFile getConfigFile() {
        return new XmlFile(new File(dir,clazz.getName()+".xml"));
    }

    // to work around warning when creating a generic array type
    public static <T> T[] toArray( T... values ) {
        return values;
    }

    public static <T> List<T> toList( T... values ) {
        final ArrayList<T> r = new ArrayList<T>();
        for (T v : values)
            r.add(v);
        return r;
    }

    public static <T extends Describable>
    Map<Descriptor<T>,T> toMap(Iterable<T> describables) {
        Map<Descriptor<T>,T> m = new LinkedHashMap<Descriptor<T>,T>();
        for (T d : describables) {
            m.put(d.getDescriptor(),d);
        }
        return m;
    }

    /**
     * Used to build {@link Describable} instance list from &lt;f:hetero-list> tag.
     *
     * @param req
     *      Request that represents the form submission.
     * @param formData
     *      Structured form data that represents the contains data for the list of describables.
     * @param key
     *      The JSON property name for 'formData' that represents the data for the list of describables.
     * @param descriptors
     *      List of descriptors to create instances from.
     * @return
     *      Can be empty but never null.
     */
    public static <T extends Describable>
    List<T> newInstancesFromHeteroList(StaplerRequest req, JSONObject formData, String key,
                Collection<? extends Descriptor<T>> descriptors) throws FormException {

        List<T> items = new ArrayList<T>();

        if(!formData.has(key))   return items;
        JSONArray a = JSONArray.fromObject(formData.get(key));

        for (Object o : a) {
            JSONObject jo = (JSONObject)o;
            String kind = jo.getString("kind");
            items.add(find(descriptors,kind).newInstance(req,jo));
        }

        return items;
    }

    /**
     * Finds a descriptor from a collection by its class name.
     */
    public static <T extends Descriptor> T find(Collection<? extends T> list, String className) {
        for (T d : list) {
            if(d.getClass().getName().equals(className))
                return d;
        }
        return null;
    }

    public static final class FormException extends Exception {
        private final String formField;

        public FormException(String message, String formField) {
            super(message);
            this.formField = formField;
        }

        public FormException(String message, Throwable cause, String formField) {
            super(message, cause);
            this.formField = formField;
        }

        public FormException(Throwable cause, String formField) {
            super(cause);
            this.formField = formField;
        }

        /**
         * Which form field contained an error?
         */
        public String getFormField() {
            return formField;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(Descriptor.class.getName());

    /**
     * All the live instances of {@link Descriptor}.
     * {@link Descriptor}s are all supposed to have the singleton semantics, so
     * this shouldn't cause a memory leak.
     */
    public static final CopyOnWriteArrayList<Descriptor> ALL = new CopyOnWriteArrayList<Descriptor>();

    /**
     * Used in {@link #checkMethods} to indicate that there's no check method.
     */
    private static final Method NONE;

    static {
        try {
            NONE = Object.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
            throw new AssertionError();
        }
    }
}
