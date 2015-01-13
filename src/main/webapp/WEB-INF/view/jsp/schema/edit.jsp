<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="/schema" var="back_url" />
<div id="content">
	<h2>SR</h2>
	<h4>
		<img src="<s:url value="/resources/img/arrow_undo.png" />" height="16" width="16" alt="<s:message code="schemaRegistry.new.cancel" />" />
		<a href="${back_url}"><s:message code="schemaRegistry.new.cancel" /></a>
	</h4>
	<%@ include file="incl/edit_form.jsp" %>
</div>