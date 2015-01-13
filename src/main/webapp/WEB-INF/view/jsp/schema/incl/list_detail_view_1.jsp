<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<h4>Crosswalks from this schema</h4>
<c:choose>
	<c:when
		test="${selectedSchemaMappingsFrom != null && fn:length(selectedSchemaMappingsFrom)>0}">
		<ul>
			<c:forEach items="${selectedSchemaMappingsFrom}" var="mapping">
				<li><a href="<s:url value="/mapping/view?mapping=${mapping.id}" />">to ${mapping.target.name}</a></li>
			</c:forEach>
		</ul>
	</c:when>
	<c:otherwise>
		<p style="font-style: italic;">This schema has not been used as crosswalk source yet!</p>
	</c:otherwise>
</c:choose>
<h4>Crosswalks to this schema</h4>
<c:choose>
	<c:when
		test="${selectedSchemaMappingsTo != null && fn:length(selectedSchemaMappingsTo)>0}">
		<ul>
			<c:forEach items="${selectedSchemaMappingsTo}" var="mapping">
				<li><a href="<s:url value="/mapping/view?mapping=${mapping.id}" />">from ${mapping.source.name}</a></li>
			</c:forEach>
		</ul>
	</c:when>
	<c:otherwise>
		<p style="font-style: italic;">This schema has not been used as crosswalk target yet!</p>
	</c:otherwise>
</c:choose>