<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />

<sf:form method="POST" action="${saveUrl}" modelAttribute="grammar" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~de.unibamberg.minf.dme.form.grammar.create" /></h3>	
		<sf:hidden path="id" />
		<sf:hidden path="entityId" />
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="col-sm-3 control-label" for="grammar_name"><s:message code="~de.unibamberg.minf.dme.model.grammar.name" />:</label>
			<div class="col-sm-8">
				<sf:input path="name" class="form-control" id="grammarImpl_name" />
				<sf:errors path="name" cssClass="error" />
			</div>
		</div>		
	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~de.unibamberg.minf.common.link.cancel" /></button>
			<button class="btn btn-primary btn-sm start form-btn-submit" type="submit"><s:message code="~de.unibamberg.minf.common.link.save" /></button>
		</div>
	</div>
</sf:form>