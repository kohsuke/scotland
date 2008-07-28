/*
 * Tag library for generating HTML forms like those of Hudson.
 */
package org.kohsuke.scotland.core;

/**
 * Generates an text box.
 */
def textBox(String field) {
    textBox(field,Collections.EMPTY_MAP)
}

// TODO: this method depends on the extensibility support. modularize.
def textBox(String field, Map args) {
    String checkUrl= args.checkUrl;
    if(checkUrl==null)
        checkUrl = descriptor.getCheckUrl(field);
    INPUT([CLASS:"setting-input ${checkUrl!=null?'validated':''}",
          NAME:field,
          VALUE:my?."${field}", // TODO: default value
          TYPE:"text",
          checkUrl:checkUrl]+args)
}

def entry(args,body) {
    TR {
        TD(CLASS:"setting-leftspace"," ")
        TD(CLASS:"setting-name",args.name)
        TD(CLASS:"setting-body",body)
        if(args.help!=null)
            TD(CLASS:"setting-help")
    }
}

def block(body) {
    TR {
        TD(COLSPAN:3,body)
    }
}

def repeatableDeleteButton() {
    repeatableDeleteButton("Delete")
}

def repeatableDeleteButton(value) {
    INPUT(TYPE:"button",VALUE:value,CLASS:"repeatable-delete")
}

def helpArea() {
    TR(CLASS:"help-area") {
        TD()
        TD(COLSPAN:2) {
            DIV(CLASS:"help", "Loading...")
        }
        TD()
    }
}

/**
 * Generates a FORM element that submits the end result via
 * structured form submission.
 */
def structuredForm(args,body) {
    adjunct("org.kohsuke.scotland.core.StructuredForm")
    FORM([CLASS:"structured-form",METHOD:"post"]+args,body)
}