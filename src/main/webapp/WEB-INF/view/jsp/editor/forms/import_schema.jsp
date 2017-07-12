<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />
<form method="POST" action="${saveUrl}" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.button.import" /></h3>
		<input type="hidden" name="schemaId" value="${schema.id}">
		<c:if test="${elementId}">
			<input type="hidden" name="elementId" id="elementId" />
		</c:if>
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="col-sm-4 control-label" for="schema_source"><s:message code="~eu.dariah.de.minfba.schereg.model.schema.source" />:</label>
			<div class="col-sm-8">
				<input type="hidden" name="file.id" id="file.id" />
				<input id="schema_source" type="file" name="file" />
			</div>
		</div>
		<div class="form-group">
			<label class="col-sm-4 control-label" for="schema_root"><s:message code="~eu.dariah.de.minfba.schereg.model.schema.root_element" />:</label>
			<div class="col-sm-8">
				<!--<select id="schema_root" name="schema_root" class="form-control" disabled="disabled"></select>-->
				<input type="text" class="form-control typeahead" id="schema_root" disabled="disabled" />
				<input type="hidden" name="schema_root_qn" id="schema_root_qn" />
			</div>
		</div>
	</div>
	<div class="form-footer control-group">
		<div class="controls">
			<button class="btn btn-default cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.cancel" /></button>
			<button id="btn-submit-schema-elements" class="btn btn-primary start form-btn-submit" disabled="disabled" type="submit"><s:message code="~eu.dariah.de.minfba.common.link.save" /></button>
		</div>
	</div>
</form>
