<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<table class="listing_detail_container">
	<tbody>		
		<c:set var="msg" value="${selectedMapping.message}"/>  
		<c:if test="${not empty selectedMapping.message}">
			<tr>	
				<th><s:message code="crosswalkRegistry.message" /></th>
				<td class="error" id="model_id${selectedMapping.id}.message"><c:out value="${selectedMapping.message}" /></td>
			</tr>
		</c:if>
				
		<tr>
			<th><s:message code="crosswalkRegistry.modified" /></th>
			<td id="model_detail_id${selectedMapping.id}.modified"><c:out value="${selectedMapping.modified}" /></td>
		</tr>
		<tr>
			<th><s:message code="crosswalkRegistry.source" /></th>
			<td id="model_id${selectedMapping.id}.source">
				<a href="<s:url value="/schema/view?schema=${selectedMapping.source.id}" />"><c:out value="${selectedMapping.source.name}" /></a>
			</td>
		</tr>
		<tr>	
			<th><s:message code="crosswalkRegistry.target" /></th>
			<td id="model_id${selectedMapping.id}.target">
				<a href="<s:url value="/schema/view?schema=${selectedMapping.target.id}" />"><c:out value="${selectedMapping.target.name}" /></a>
			</td>
		</tr>
	</tbody>
</table>