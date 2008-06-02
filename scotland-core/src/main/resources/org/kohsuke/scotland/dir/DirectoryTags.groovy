/**
 * Tag library for generating a directory listing in AJAX.
 *
 * @author Kohsuke Kawaguchi
 */
package org.kohsuke.scotland.dir;

/**
 * Lists up one level and let more to be expanded dynamically.
 */
void list(String baseUrl, DirectoryModel model, parent) {
    children = model.getChildren(parent);
    if(!children.isEmpty()) {
        UL(CLASS:"dirlist") {
            children.each { child ->
                LI {
                    String url = baseUrl+'/';
                    while(true) {
                        url += model.getUrl(child)+'/';
                        A(HREF:url,model.getName(child));
                        next = model.collapse(child);
                        if(next==null)    break;
                        text('/')
                        child=next
                    }
                }
            }
        }
    }
}