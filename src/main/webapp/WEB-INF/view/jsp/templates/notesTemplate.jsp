<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<div id="top_messages">
	<ul>
		<c:if test="${not empty sessionScope.messages}">
			<c:catch>
				<c:forEach items="${sessionScope.messages}" var="message">
					<c:choose>
						<c:when test="${not empty message.code}">
							<li><s:message code="${message.code}" arguments="${message.arguments}" /></li>
						</c:when>
						<c:otherwise>
							<li>${message.message}</li>
						</c:otherwise>
					</c:choose>
				</c:forEach>
			</c:catch>
		</c:if>
	</ul>
</div>

<c:remove var="messages" scope="session" />


<div style="float: left">&nbsp;</div>