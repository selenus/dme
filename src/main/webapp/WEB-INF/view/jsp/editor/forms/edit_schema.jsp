<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />
<sf:form method="POST" action="${saveUrl}" modelAttribute="schema" class="form-horizontal" >
	<div class="form-header">
		<c:choose>
			<c:when test="${schema.id!=null && schema.id!=''}">
				<h3 id="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.form.schema.edit" /></h3>
			</c:when>
			<c:otherwise>
				<h3 id="form-header-title"><s:message code="~eu.dariah.de.minfba.schereg.form.schema.create" /></h3>
			</c:otherwise>
		</c:choose>		
		<sf:hidden path="id" />
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="control-label col-sm-3" for="schema_label"><s:message code="~eu.dariah.de.minfba.schereg.model.schema.label" />:</label>
			<div class="col-sm-9">
				<sf:input path="label" class="form-control" id="schema_label" />
				<sf:errors path="label" cssClass="error" />
			</div>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="schema_description"><s:message code="~eu.dariah.de.minfba.schereg.model.schema.description" />:</label>
			<div class="col-sm-9">
				<sf:textarea path="description" class="form-control" rows="4" id="schema_description" />
			</div>
		</div>
		<div class="form-group">
			<div class="col-sm-offset-3 col-sm-9">
				<label>
					<input type="checkbox" name="readOnly" id="readOnly"<c:if test="${readOnly}"> checked="checked"</c:if>> <s:message code="~eu.dariah.de.minfba.common.model.readonly" />
				</label>
			</div>
		</div>	
	</div>
	<div class="form-footer form-group">
		<div class="col-sm-12">
			<button class="btn btn-default cancel form-btn-cancel" type="reset"><s:message code="~eu.dariah.de.minfba.common.link.cancel" /></button>
			<button class="btn btn-primary start form-btn-submit" type="submit"><s:message code="~eu.dariah.de.minfba.common.link.save" /></button>
		</div>
	</div>
</sf:form>