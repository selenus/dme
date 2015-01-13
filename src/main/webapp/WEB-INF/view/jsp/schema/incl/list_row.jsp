<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<s:url value="/schema/edit?schema=${schema.id}&v=${view}" var="edit_url" />
<s:url value="/schema/delete?schema=${schema.id}&v=${view}" var="delete_url" />
<s:url value="/schema?schema=${schema.id}&v=${view}" var="view_url" />

<c:set var="code" value="${schema.state}"/>  
<tr id="model_id_${schema.id}" 
	class="<c:if test="${(selectedSchema != null) && (schema.id == selectedSchema.id)}">selected </c:if>
		   <c:if test="${schema.state == 9}">error</c:if>">
	<td id="model_id_${schema.id}.lookupTimestamp" class="hide"><joda:format value="${schema.modified}" pattern="yyyyMMddHHmmssSSSZ" /></td>
	<td id="model_id_${schema.id}.state">
		<i id="model_id_${schema.id}.state.0" class="icon-refresh icon-black <c:if test="${schema.state != 0}">hidden</c:if>"></i>
		<i id="model_id_${schema.id}.state.1" class="icon-refresh icon-black <c:if test="${schema.state != 1}">hidden</c:if>"></i>
		<i id="model_id_${schema.id}.state.2" class="icon-ok icon-black <c:if test="${schema.state != 2}">hidden</c:if>"></i>
		<i id="model_id_${schema.id}.state.9" class="icon-warning-sign icon-black <c:if test="${schema.state != 9}">hidden</c:if>"></i>
	</td>
	<td style="white-space: normal;" id="model_id_${schema.id}.name">
		<a href="${view_url}"><c:out value="${schema.name}" /> (<c:out value="${schema.id}" />)</a>
	</td>
	<td id="model_id_${schema.id}.type">
		<a href="${view_url}"><c:out value="${schema.type}" /></a>
	</td>
	<td id="model_id_${schema.id}.modified">
		<a href="${view_url}"><joda:format value="${schema.modified}" style="SM" /></a>
	</td>
	<td id="model_id_${schema.id}.actions" class="<c:choose><c:when test="${schema.isLocked}">actionsLocked</c:when><c:otherwise>actions</c:otherwise></c:choose>">
		<div class="modelLoading">
			<i class="icon-refresh icon-black"></i> Busy...
		</div>	
		<div class="modelActions">
			<a data-placement="top" rel="tooltip" title="Delete schema" onclick="if(confirm('<s:message code="schemaRegistry.action.delete" />')){ deleteSchema(${schema.id}); return false;}" href="#">
				<i class="icon-remove icon-black"></i>
			</a>
			<a data-placement="top" rel="tooltip" onclick="editSchema(${schema.id}); return false;" href="${edit_url}" title="Edit schema">
				<i class="icon-edit icon-black"></i>
			</a>
		</div>
	</td>
</tr>