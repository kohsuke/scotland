// @include org.kohsuke.stapler.framework.prototype.prototype

function toggleDirTree(node) {
    var ul = node.parentNode.parentNode;
    var selfUrl = ul.getAttribute("SELFURL");

    var src = node.getAttribute("src");

    function changeImageTo(name) {
        node.src = src.substring(0,src.length-6)+name;
    }

    if(src.endsWith("cn.gif")) {
        new Ajax.Request(selfUrl+"/ajax", {
            method: "post",
            parameters: "parent=" + node.getAttribute("PATH"),
            onSuccess: function(rsp) {
                changeImageTo("co.gif");
                Element.insert(node,{after:rsp.responseText});
            }
        });
    } else
    if(src.endsWith("co.gif")) {
        Element.remove(node.nextSibling);
        changeImageTo("cn.gif");
    }
}