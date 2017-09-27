<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>

<s:url value="${actionPath}" var="saveUrl" />

<sf:form method="POST" action="${saveUrl}" modelAttribute="session" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~de.unibamberg.minf.dme.button.save_session" /></h3>
		<sf:hidden path="entityId" />	
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="col-sm-4 control-label"><s:message code="~de.unibamberg.minf.dme.model.session.label" />:</label>
			<div class="col-sm-8">
				<sf:input class="form-control" path="label" />
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