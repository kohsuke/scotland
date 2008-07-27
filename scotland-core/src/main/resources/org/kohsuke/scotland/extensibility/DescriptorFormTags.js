// @include org.kohsuke.scotland.behavior
// @include org.kohsuke.scotland.yui.button
// @include org.kohsuke.scotland.yui.menu

Behaviour.register({
    "DIV.hetero-list-container" : function(e) {
        // components for the add button
        var menu = document.createElement("SELECT");
        var btn = findElementsBySelector(e,"INPUT.hetero-list-add")[0];
        YAHOO.util.Dom.insertAfter(menu,btn);

        var prototypes = e.lastChild;
        while(!Element.hasClassName(prototypes,"prototypes"))
            prototypes = prototypes.previousSibling;
        var insertionPoint = prototypes.previousSibling;    // this is where the new item is inserted.

        // extract templates
        var templates = []; var i=0;
        for(var n=prototypes.firstChild;n!=null;n=n.nextSibling,i++) {
            var name = n.getAttribute("name");
            var tooltip = n.getAttribute("tooltip");
            menu.options[i] = new Option(n.getAttribute("title"),""+i);
            templates.push({html:n.innerHTML, name:name, tooltip:tooltip});
        }
        Element.remove(prototypes);

        // D&D support
        function prepareDD(e) {
            var dd = new DragDrop(e);
            var h = e;
            // locate a handle
            while(!Element.hasClassName(h,"dd-handle"))
                h = h.firstChild;
            dd.setHandleElId(h);
        }
        var withDragDrop = Element.hasClassName(e,"with-drag-drop");
        if(withDragDrop) {
            for(e=e.firstChild; e!=null; e=e.nextSibling) {
                if(Element.hasClassName(e,"repeated-chunk"))
                    prepareDD(e);
            }
        }

        var menuButton = new YAHOO.widget.Button(btn, { type: "menu", menu: menu });
        menuButton.getMenu().clickEvent.subscribe(function(type,args,value) {
            var t = templates[parseInt(args[1].value)]; // where this args[1] comes is a real mystery

            var nc = document.createElement("div");
            nc.className = "repeated-chunk";
            nc.setAttribute("name",t.name);
            nc.innerHTML = t.html;
            insertionPoint.parentNode.insertBefore(nc, insertionPoint);
            if(withDragDrop)    prepareDD(nc);

            Behaviour.applySubtree(nc);
        });

        menuButton.getMenu().renderEvent.subscribe(function(type,args,value) {
            // hook up tooltip for menu items
            var items = menuButton.getMenu().getItems();
            for(i=0; i<items.length; i++) {
                var t = templates[i].tooltip;
                if(t!=null)
                    applyTooltip(items[i].element,t);
            }
        });
    },

    "DIV.repeated-container" : function(e) {
        // compute the insertion point
        var ip = e.lastChild;
        while (!Element.hasClassName(ip, "repeatable-insertion-point"))
            ip = ip.previousSibling;
        // set up the logic
        object(repeatableSupport).init(e, e.firstChild, ip);
    }
});

