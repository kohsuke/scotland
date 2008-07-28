// structured form submission
// @include org.kohsuke.scotland.behavior

Behaviour.register({
    "FORM.structured-form" : function(form) {
        // add the hidden 'json' input field, which receives the form structure in JSON
        var div = document.createElement("div");
        div.innerHTML = "<input type=hidden name=json value=init>";
        form.appendChild(div);

        form.onsubmit = function() { buildFormTree(this) };
        form = null; // memory leak prevention
    }
});

function xor(a,b) {
    // convert both values to boolean by '!' and then do a!=b
    return !a != !b;
}

//
// structured form submission handling
//   see http://hudson.gotdns.com/wiki/display/HUDSON/Structured+Form+Submission
function buildFormTree(form) {
    try {
        // I initially tried to use an associative array with DOM elemnets as keys
        // but that doesn't seem to work neither on IE nor Firefox.
        // so I switch back to adding a dynamic property on DOM.
        form.formDom = {}; // root object

        var doms = []; // DOMs that we added 'formDom' for.
        doms.push(form);

        function shortenName(name) {
            // [abc.def.ghi] -> abc.def.ghi
            if(name.startsWith('['))
                return name.substring(1,name.length-1);

            // abc.def.ghi -> ghi
            var idx = name.lastIndexOf('.');
            if(idx>=0)  name = name.substring(idx+1);
            return name;
        }

        function addProperty(parent,name,value) {
            name = shortenName(name);
            if(parent[name]!=null) {
                if(parent[name].push==null) // is this array?
                    parent[name] = [ parent[name] ];
                parent[name].push(value);
            } else {
                parent[name] = value;
            }
        }

        // find the grouping parent node, which will have @name.
        // then return the corresponding object in the map
        function findParent(e) {
            while(e!=form) {
                e = e.parentNode;

                // this is used to create a group where no single containing parent node exists,
                // like <optionalBlock>
                var nameRef = e.getAttribute("nameRef");
                if(nameRef!=null)
                    e = $(nameRef);

                if(e.getAttribute("field-disabled")!=null)
                    return {};  // this field shouldn't contribute to the final result

                var name = e.getAttribute("name");
                if(name!=null) {
                    if(e.tagName=="INPUT" && !xor(e.checked,Element.hasClassName(e,"negative")))
                        return {};  // field is not active

                    var m = e.formDom;
                    if(m==null) {
                        // this is a new grouping node
                        doms.push(e);
                        e.formDom = m = {};
                        addProperty(findParent(e), name, m);
                    }
                    return m;
                }
            }

            return form.formDom; // guaranteed non-null
        }

        var jsonElement = null;

        for( var i=0; i<form.elements.length; i++ ) {
            var e = form.elements[i];
            if(e.name=="json") {
                jsonElement = e;
                continue;
            }
            if(e.tagName=="FIELDSET")
                continue;
            if(e.tagName=="SELECT" && e.multiple) {
                var values = [];
                for( var o=0; o<e.options.length; o++ ) {
                    var opt = e.options.item(o);
                    if(opt.selected)
                        values.push(opt.value);
                }
                addProperty(findParent(e),e.name,values);
                continue;
            }

            var p;
            var type = e.getAttribute("type");
            if(type==null)  type="";
            switch(type.toLowerCase()) {
            case "button":
            case "submit":
                break;
            case "checkbox":
                p = findParent(e);
                var checked = xor(e.checked,Element.hasClassName(e,"negative"));
                if(!e.groupingNode)
                    addProperty(p, e.name, checked);
                else {
                    if(checked)
                        addProperty(p, e.name, e.formDom = {});
                }
                break;
            case "radio":
                if(!e.checked)  break;
                if(e.groupingNode) {
                    p = findParent(e);
                    addProperty(p, e.name, e.formDom = { value: e.value });
                    break;
                }

                // otherwise fall through
            default:
                p = findParent(e);
                addProperty(p, e.name, e.value);
                break;
            }
        }

        jsonElement.value = Object.toJSON(form.formDom);

        // clean up
        for( i=0; i<doms.length; i++ )
            doms[i].formDom = null;


        return jsonElement.value;
    } catch(e) {
        alert(e);
    }
}
