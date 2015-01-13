<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<s:url value="/mapping/edit?mapping=${mapping.id}" var="edit_url" />
<s:url value="/mapping/analyze?mapping=${mapping.id}" var="analyze_url" />
<s:url value="/mapping/delete?mapping=${mapping.id}" var="delete_url" />
<s:url value="/mapping?mapping=${mapping.id}&v=${view}" var="view_url" />

<c:set var="code" value="${mapping.state}"/>  
<tr id="model_id_${mapping.id}" <c:if test="${code==9}">class="error"</c:if>>
	<td id="model_id_${mapping.id}.lookupTimestamp" class="hide"><joda:format value="${mapping.modified}" pattern="yyyyMMddHHmmssSSSZ" /></td>
	<td id="model_id_${mapping.id}.state">
		<i id="model_id_${mapping.id}.state.0" class="icon-refresh icon-black <c:if test="${mapping.state != 0}">hidden</c:if>"></i>
		<i id="model_id_${mapping.id}.state.1" class="icon-refresh icon-black <c:if test="${mapping.state != 1}">hidden</c:if>"></i>
		<i id="model_id_${mapping.id}.state.2" class="icon-ok icon-black <c:if test="${mapping.state != 2}">hidden</c:if>"></i>
		<i id="model_id_${mapping.id}.state.9" class="icon-warning-sign icon-black <c:if test="${mapping.state != 9}">hidden</c:if>"></i>
	</td>
	<td id="model_id_${mapping.id}.name">
		<a href="${view_url}"><c:out value="${mapping.source.name}" /> &rarr; <c:out value="${mapping.target.name}" /> (<c:out value="${mapping.id}" />)</a>
	</td>
	<td id="model_id_${mapping.id}.mappingCells">
		<a href="${view_url}"><c:out value="${fn:length(mapping.mappingCells)}" /></a>
	</td>
	<td id="model_id_${mapping.id}.modified">
		<a href="${view_url}"><joda:format value="${mapping.modified}" style="SM" locale="${pageContext.response.locale}" /></a>
	</td>
	<td id="model_id_${mapping.id}.actions" class="<c:choose><c:when test="${mapping.isLocked}">actionsLocked</c:when><c:otherwise>actions</c:otherwise></c:choose>">
		<div class="modelLoading">
			<i class="icon-refresh icon-black"></i> Busy...
		</div>
		<div class="modelActions">
			<a data-placement="top" rel="tooltip" title="Delete crosswalk" onclick="if(confirm('<s:message code="~crosswalkRegistry.action.delete" />')){ deleteMapping(${mapping.id}); return false;}" href="#">
				<i class="icon-remove icon-black"></i>
			</a>
			<a data-placement="top" rel="tooltip" href="${analyze_url}" title="View crosswalk">
				<i class="icon-wrench icon-black"></i>
			</a>
			<!-- <button data-toggle="dropdown" class="btn btn-small dropdown-toggle">
				<i class="icon-pencil icon-black"></i> Actions <span class="caret"></span>
			</button>
			<ul class="dropdown-menu">
				<li>
					<a onclick="if(confirm('<s:message code="~crosswalkRegistry.action.delete" />')){ deleteMapping(${mapping.id}); return false;}" href="#">
						<i class="icon-remove icon-black"></i> Remove crosswalk
					</a>
				</li>
				<li>
					<a href="${analyze_url}">
						<i class="icon-wrench icon-black"></i> View crosswalk
					</a>
				</li>
			</ul> -->
		</div>
	</td>
</tr>


