/*
 * Tag library for generating HTML forms like those of Hudson.
 */
package org.kohsuke.scotland.extensibility;

import org.kohsuke.scotland.core.FormTags;

/*
  Outer most tag for creating a heterogeneous list, where the user can add different contents.

  Mandatory attributes:
    name:         form name that receives an array for all the items in the heterogeneous list.
    items:        existing items to be displayed
    descriptors:  all types that the user can add.

 Optional attributes:
   addCaption:    caption of the 'add' button.
   deleteCaption: caption of the 'delete' button.
   targetType:    the type for which descriptors will be configured.
                  default to ${it.class} (optional)
   hasHeader:     for each item, add a caption from descriptor.getDisplayName().
                  this also activates D&D (where the header is a grip), and help text support.

 See Descriptor.newInstancesFromHeteroList for how to parse the submission.
*/
def heteroList(String name, Class targetType, boolean hasHeader, Collection descriptors, Collection items) {
    FormTags f = taglib(FormTags);
    if(targetType==null)
        targetType = my.class;

    // render one config page
    def render = { Descriptor descriptor, Describable instance ->
        TABLE {
            def help = descriptor.helpFile;
            if(hasHeader) {
                TR {
                    TD(COLSPAN:3) {
                        DIV(CLASS:"dd-handle") {
                            B(descriptor.displayName)
                        }
                    }
                    if(help!=null) {
                        TD {
                            A(HREF:"#",CLASS:"help-button",HELPURL:"${rootURL}${help}") {
                                // TODO: use the img function
                                IMG(SRC:"${imagesURL}/16x16/help.gif")
                            }
                        }
                    }
                }
                if(help!=null)  f.helpArea();
            }
            context.setVariable('descriptor',descriptor);
            context.setVariable('it',instance);
            include(descriptor.clazz,"config.groovy");
            f.block {
                DIV(ALIGN:"right") {
                    f.repeatableDeleteButton()
                }
            }
        }
        INPUT(TYPE:"hidden",NAME:"kind",VALUE:"${descriptor.class.name}")
    };

    DIV(CLASS:"hetero-list-container ${hasHeader?'with-drag-drop':''}") {
        // display existing items
        for( i in items ) {
            DIV(CLASS:"repeated-chunk",NAME:name) {
                render(i.descriptor,i);
            }
        }

        DIV(CLASS:"repeatable-insertion-point");

        DIV(CLASS:"prototypes",STYLE:"display:none") {
            // render one prototype for each type
            for( Descriptor d in descriptors ) {
                DIV(NAME:name,TITLE:d.displayName/*,TOOLTIP:d.tooltip*/) {
                    render(d,null);
                }
            }
        }

        DIV {
            INPUT(TYPE:"button", VALUE:"Add", CLASS:"hetero-list-add");
        }
    }
}
