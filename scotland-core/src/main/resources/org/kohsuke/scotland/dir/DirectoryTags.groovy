/**
 * Tag library for generating a directory listing in AJAX.
 *
 * @author Kohsuke Kawaguchi
 */
package org.kohsuke.scotland.dir;

/**
 * Lists up one level and let more to be expanded dynamically.
 */
void list(DirectoryModel model, parent) {
    children = model.getChildren(parent);
    if(!children.isEmpty()) {
        UL(CLASS:"dirlist",SELFURL:model.selfUrl) {
            children.each { child ->
                // compute the last node and its path
                String url = "";
                def last = child;
                while(true) {
                    if(url.length()>0)  url+='/';
                    url += model.getUrl(child);
                    next = model.collapse(last);
                    if(next==null)    break;
                    last=next
                }

                LI(PATH:url) {
                    img(model.getChildCount(last)==0,false);

                    url = model.baseUrl+'/';
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

private void img(boolean leaf, boolean open) {
    def url = res(DirectoryTags, "${open ? 'o' : 'c'}${leaf ? 'l' : 'n'}.gif")
    if(leaf)
        IMG(SRC:url)
    else
        IMG(SRC:url, CLASS:"clickable", ONCLICK:"toggleDirTree(this)");
}