<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />
<sf:form method="POST" action="${saveUrl}" modelAttribute="schema" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title">~ Edit schema</h3>
		<sf:hidden path="id" />
	</div>
	<div class="form-content">
		<div class="control-group">
			<label class="control-label" for="schema_label">~ Label:</label>
			<div class="controls">
				<sf:input path="label" class="form-control" id="schema_label" />
				<sf:errors path="label" cssClass="error" />
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="schema_description">~Description:</label>
			<div class="controls">
				<sf:textarea path="description" class="form-control" rows="4" id="schema_description" />
			</div>
		</div>
	</div>
	<div class="form-footer control-group">
		<div class="controls">
			<button class="btn btn-default cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.view.common.cancel" /></button>
			<button class="btn btn-primary start form-btn-submit" type="submit"><s:message code="~eu.dariah.de.minfba.common.view.common.save" /></button>
		</div>
	</div>
</sf:form>
