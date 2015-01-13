<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<table class="table table-condensed">
	<tbody>
		<tr>
			<th><s:message code="schemaRegistry.name" /></th>
			<td id="model_detail_id${selectedSchema.id}.name"><strong><c:out value="${selectedSchema.name}" /></strong></td>
		</tr>
		
		<c:set var="msg" value="${selectedSchema.message}"/>  
		<c:if test="${not empty selectedSchema.message}">
			<tr>	
				<th><s:message code="schemaRegistry.message" /></th>
				<td class="error" id="model_id${selectedSchema.id}.message"><c:out value="${selectedSchema.message}" /></td>
			</tr>
		</c:if>
		
		<tr>
			<th><s:message code="schemaRegistry.type" /></th>
			<td id="model_detail_id${selectedSchema.id}.type"><c:out value="${selectedSchema.type}" /></td>
		</tr>
		
		<tr>
			<th><s:message code="schemaRegistry.modified" /></th>
			<td id="model_detail_id${selectedSchema.id}.modified"><c:out value="${selectedSchema.modified}" /></td>
		</tr>
		<tr>
			<th><s:message code="schemaRegistry.source" /></th>
			<td id="model_id${selectedSchema.id}.sourceShort"><c:out value="${selectedSchema.sourceShort}" /></td>
		</tr>
		<tr>	
			<th><s:message code="schemaRegistry.description" /></th>
			<td id="model_id${selectedSchema.id}.description"><c:out value="${selectedSchema.description}" /></td>
		</tr>
	</tbody>
</table>