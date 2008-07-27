// @include org.kohsuke.scotland.behavior
// @include org.kohsuke.scotland.yui.button
// @include org.kohsuke.scotland.yui.animation
// @include org.kohsuke.scotland.yui.dragdrop
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

    // button to add a new repeatable block
    "INPUT.repeatable-add" : function(e) {
        makeButton(e,function(e) {
            repeatableSupport.onAdd(e.target);
        });
        e = null; // avoid memory leak
    },

    "INPUT.repeatable-delete" : function(e) {
        makeButton(e,function(e) {
            repeatableSupport.onDelete(e.target);
        });
        e = null; // avoid memory leak
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


/*
    D&D implementation for heterogeneous list.
 */
var DragDrop = function(id, sGroup, config) {
    DragDrop.superclass.constructor.apply(this, arguments);
};

(function() {
    var Dom = YAHOO.util.Dom;
    var Event = YAHOO.util.Event;
    var DDM = YAHOO.util.DragDropMgr;

    YAHOO.extend(DragDrop, YAHOO.util.DDProxy, {
        startDrag: function(x, y) {
            var el = this.getEl();

            this.resetConstraints();
            this.setXConstraint(0,0);    // D&D is for Y-axis only

            // set Y constraint to be within the container
            var totalHeight = el.parentNode.offsetHeight;
            var blockHeight = el.offsetHeight;
            this.setYConstraint(el.offsetTop, totalHeight-blockHeight-el.offsetTop);

            el.style.visibility = "hidden";

            this.goingUp = false;
            this.lastY = 0;
        },

        endDrag: function(e) {
            var srcEl = this.getEl();
            var proxy = this.getDragEl();

            // Show the proxy element and animate it to the src element's location
            Dom.setStyle(proxy, "visibility", "");
            var a = new YAHOO.util.Motion(
                proxy, {
                    points: {
                        to: Dom.getXY(srcEl)
                    }
                },
                0.2,
                YAHOO.util.Easing.easeOut
            )
            var proxyid = proxy.id;
            var thisid = this.id;

            // Hide the proxy and show the source element when finished with the animation
            a.onComplete.subscribe(function() {
                    Dom.setStyle(proxyid, "visibility", "hidden");
                    Dom.setStyle(thisid, "visibility", "");
                });
            a.animate();
        },

        onDrag: function(e) {

            // Keep track of the direction of the drag for use during onDragOver
            var y = Event.getPageY(e);

            if (y < this.lastY) {
                this.goingUp = true;
            } else if (y > this.lastY) {
                this.goingUp = false;
            }

            this.lastY = y;
        },

        onDragOver: function(e, id) {
            var srcEl = this.getEl();
            var destEl = Dom.get(id);

            // We are only concerned with list items, we ignore the dragover
            // notifications for the list.
            if (destEl.nodeName == "DIV" && Dom.hasClass(destEl,"repeated-chunk")) {
                var p = destEl.parentNode;

                // if going up, insert above the target element
                p.insertBefore(srcEl, this.goingUp?destEl:destEl.nextSibling);

                DDM.refreshCache();
            }
        }
    });
})();




// code for supporting repeatable.jelly
var repeatableSupport = {
    // set by the inherited instance to the insertion point DIV
    insertionPoint: null,

    // HTML text of the repeated chunk
    blockHTML: null,

    // containing <div>.
    container: null,

    // block name for structured HTML
    name : null,

    // do the initialization
    init : function(container,master,insertionPoint) {
        this.container = $(container);
        this.container.tag = this;
        master = $(master);
        this.blockHTML = master.innerHTML;
        master.parentNode.removeChild(master);
        this.insertionPoint = $(insertionPoint);
        this.name = master.getAttribute("name");
        this.update();
    },

    // insert one more block at the insertion position
    expand : function() {
        // importNode isn't supported in IE.
        // nc = document.importNode(node,true);
        var nc = document.createElement("div");
        nc.className = "repeated-chunk";
        nc.setAttribute("name",this.name);
        nc.innerHTML = this.blockHTML;
        this.insertionPoint.parentNode.insertBefore(nc, this.insertionPoint);

        Behaviour.applySubtree(nc);
        this.update();
    },

    // update CSS classes associated with repeated items.
    update : function() {
        var children = [];
        for( var n=this.container.firstChild; n!=null; n=n.nextSibling )
            if(Element.hasClassName(n,"repeated-chunk"))
                children.push(n);

        if(children.length==0) {
            // noop
        } else
        if(children.length==1) {
            children[0].className = "repeated-chunk first last only";
        } else {
            children[0].className = "repeated-chunk first";
            for(var i=1; i<children.length-1; i++)
                children[i].className = "repeated-chunk middle";
            children[children.length-1].className = "repeated-chunk last";
        }
    },

    // these are static methods that don't rely on 'this'

    // called when 'delete' button is clicked
    onDelete : function(n) {
        while (!Element.hasClassName(n,"repeated-chunk"))
            n = n.parentNode;

        var p = n.parentNode;
        p.removeChild(n);
        p.tag.update();
    },

    // called when 'add' button is clicked
    onAdd : function(n) {
        while(n.tag==null)
            n = n.parentNode;
        n.tag.expand();
    }
};
