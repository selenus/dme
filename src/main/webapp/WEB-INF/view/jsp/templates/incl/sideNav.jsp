<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="tpl" tagdir="/WEB-INF/tags" %>

<c:forEach items="${_nav.rootItems}" var="_navItem">
	<c:if test="${_navItem.active || _navItem.childActive}">
		<div class="list-group nav active">
			<h4>
            	<c:if test="${_navItem.glyphicon!=null && fn:length(_navItem.glyphicon)>0}">
					<span class="${_navItem.glyphicon}"></span>&nbsp;
				</c:if>
				${_navItem.displayCode}
		  	</h4>
            <tpl:sideNav navItem="${_navItem}" />
		</div>
	</c:if>
</c:forEach>