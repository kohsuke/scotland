// @include org.kohsuke.stapler.framework.prototype.prototype

function toggleDirTree(node) {
    var li = node.parentNode;
    var ul = li.parentNode;
    var selfUrl = ul.getAttribute("SELFURL");

    var src = node.getAttribute("src");

    function changeImageTo(name) {
        node.src = src.substring(0,src.length-6)+name;
    }

    if(src.endsWith("cn.gif")) {
        new Ajax.Request(selfUrl+"/ajax", {
            method: "post",
            parameters: {path:li.getAttribute("PATH")},
            onSuccess: function(rsp) {
                changeImageTo("on.gif");
                Element.insert(li,{after:rsp.responseText});
            }
        });
    } else
    if(src.endsWith("on.gif")) {
        Element.remove(li.nextSibling);
        changeImageTo("cn.gif");
    }
}