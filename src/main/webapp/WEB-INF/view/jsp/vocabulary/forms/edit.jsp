<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />
<sf:form method="POST" action="${saveUrl}" modelAttribute="vocabulary" class="form-horizontal" >
	<div class="form-header">
		<c:choose>
			<c:when test="${vocabulary.id!=null && vocabulary.id!=''}">
				<h3 id="form-header-title"><s:message code="~de.unibamberg.minf.dme.form.vocabulary.edit" /></h3>
			</c:when>
			<c:otherwise>
				<h3 id="form-header-title"><s:message code="~de.unibamberg.minf.dme.form.vocabulary.create" /></h3>
			</c:otherwise>
		</c:choose>		
		<sf:hidden path="id" />
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="control-label col-sm-3" for="vocabulary_label"><s:message code="~de.unibamberg.minf.dme.vocabulary.model.label" />:</label>
			<div class="col-sm-9">
				<sf:input path="label" class="form-control" id="vocabulary_label" />
				<sf:errors path="label" cssClass="error" />
			</div>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-3" for="vocabulary_description"><s:message code="~de.unibamberg.minf.dme.vocabulary.model.description" />:</label>
			<div class="col-sm-9">
				<sf:textarea path="description" class="form-control" rows="4" id="vocabulary_description" />
			</div>
		</div>
	</div>
	<div class="form-footer form-group">
		<div class="col-sm-12">
			<button class="btn btn-default cancel form-btn-cancel" type="reset"><s:message code="~de.unibamberg.minf.common.link.cancel" /></button>
			<button class="btn btn-primary start form-btn-submit" type="submit"><s:message code="~de.unibamberg.minf.common.link.save" /></button>
		</div>
	</div>
</sf:form>
