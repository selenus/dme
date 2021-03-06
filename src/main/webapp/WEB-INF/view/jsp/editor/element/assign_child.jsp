<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />

<form method="POST" action="${saveUrl}" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~de.unibamberg.minf.dme.form.element.assign_child.title" /></h3>	
		<input type="hidden" id="child-element-id" />
	</div>
	<div class="form-content">
		<fieldset>
			<div class="form-group">
				<label class="col-sm-4 control-label" for="child-element"><s:message code="~de.unibamberg.minf.dme.form.element.assign_child.search_element" />:</label>
				<div class="col-sm-8">
					<input type="text" class="form-control typeahead" id="child-element" />
				</div>
			</div>
			<div class="form-group">
				<label class="col-sm-4 control-label" for="child-element"><s:message code="~de.unibamberg.minf.dme.form.element.assign_child.chosen_element" />:</label>
				<div class="col-sm-8">
					<input type="text" class="form-control" disabled id="element-name" />
				</div>
			</div>
			<div class="form-group">
				<div class="col-sm-8 col-sm-offset-4">
					<input type="text" class="form-control" disabled id="element-id-display" />
					<input type="hidden" id="element-id" name="element-id" />
				</div>
			</div>		
		</fieldset>
	</div>
	<div class="form-footer">
		<div class="controls">
			<button class="btn btn-default btn-sm cancel form-btn-cancel" type="reset"><s:message code="~de.unibamberg.minf.common.link.cancel" /></button>
			<button class="btn btn-primary btn-sm start form-btn-submit" type="submit"><s:message code="~de.unibamberg.minf.common.link.save" /></button>
		</div>
	</div>
</form>