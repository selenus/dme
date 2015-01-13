<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:choose>
	<c:when test="${(annotation!=null) && (fn:length(annotation)>0)}">
		<c:forEach items="${annotations}" var="annotation">
			<div class="user-annotation-entry span4 well well-small">
				<img class="pull-left" style="margin-top: 8px" src="<s:url value="/resources/img/User.png" />" width="48" height="48" />
				<div style="margin-left: 65px;">
					<span style="font-style: italic; font-size: 85%;"><joda:format value="${annotation.created}" style="SM" /></span><br />
					<span style="font-weight: bold; font-size: 105%;">${annotation.user.commonName}</span><br />
					<s:message code="~useraction.entry.action_type.${annotation.actionType}" />
					<s:message code="~${annotation.annotatedObjectType}" /> (id: ${annotation.annotatedObjectId})<br>
					<span style="font-style: italic">${annotation.comment}</span>
				</div>
			</div>
		</c:forEach>
	</c:when>
	<c:otherwise>
		No annotations for this object yet.
	</c:otherwise>
</c:choose>

