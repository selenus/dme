<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>

<c:forEach items="${loggingEvents}" var="loggingEvent">
	<tr id="logEntry_${loggingEvent.id}"
	<c:choose>
		<c:when test="${loggingEvent.level == 'ERROR'}">class="error"</c:when>
		<c:when test="${loggingEvent.level == 'WARN'}">class="warning"</c:when>
	</c:choose>
	>
		<td>${loggingEvent.level}</td>
		<td><joda:format value="${loggingEvent.dateTime}" style="SM" /></td>
		<td class="force-wrapped-cell">
			${loggingEvent.className} (${loggingEvent.methodName}@${loggingEvent.lineNumber}):<br />
			${loggingEvent.message}
			<c:if test="${not empty loggingEvent.stackTrace}">
				<br/>${loggingEvent.stackTrace}
			</c:if>
		</td>
	</tr>
</c:forEach>