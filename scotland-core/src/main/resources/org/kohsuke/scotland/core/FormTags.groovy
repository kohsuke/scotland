/*
 * Tag library for generating HTML forms like those of Hudson.
 */
package org.kohsuke.scotland.core;

/**
 * Generates an text box.
 */
def textBox(String name) {
    INPUT(TYPE:"text",NAME:name,VALUE:my."${name}")
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