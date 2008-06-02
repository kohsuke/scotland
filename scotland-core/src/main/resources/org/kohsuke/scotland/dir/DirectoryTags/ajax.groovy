import org.kohsuke.scotland.dir.DirectoryTags

node = my.current;
request.getParameter("path").split('/').each { node = my.getChild(node,it); }

taglib(DirectoryTags).list(my,node);
