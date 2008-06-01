<%--
  see tasks.tag
--%>
<%@attribute name="href" required="true" %>
<%@attribute name="icon" required="true" %>
<%@attribute name="title" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/functions" %>
<li>
  <a href="${href}">
    <img src="${rootURL}/${icon}"
  ></a>
  <a href="${href}">
    <c:choose>
      <c:when test="${f:endsWith(pageContext.request.requestURL,href)}">
        <b>${title}</b>
      </c:when>
      <c:otherwise>
        ${title}
      </c:otherwise>
    </c:choose>
  </a>
</li>
