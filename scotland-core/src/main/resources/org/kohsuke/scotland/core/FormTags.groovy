/*
 * Tag library for generating HTML forms like those of Hudson.
 */


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