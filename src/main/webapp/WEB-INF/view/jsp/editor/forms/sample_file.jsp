<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url value="${actionPath}" var="saveUrl" />
<form method="POST" action="${saveUrl}" class="form-horizontal" >
	<div class="form-header">
		<h3 id="form-header-title"><s:message code="~de.unibamberg.minf.dme.button.import_sample_file" /></h3>
		<input type="hidden" name="schemaId" value="${schema.id}">
	</div>
	<div class="form-content">
		<div class="form-group">
			<label class="col-sm-4 control-label" for="schema_source"><s:message code="~de.unibamberg.minf.dme.editor.sample.file" />:</label>
			<div class="col-sm-8">
				<input type="hidden" name="inputType" value="${inputType}" />
				<input type="hidden" name="file.id" id="file.id" />
				<input id="upload_source" type="file" name="file" />
			</div>
		</div>
	</div>
	<div class="form-footer control-group">
		<div class="controls">
			<button class="btn btn-default cancel form-btn-cancel" type="reset"><s:message code="~de.unibamberg.minf.common.link.cancel" /></button>
			<button id="btn-submit-schema-elements" class="btn btn-primary start form-btn-submit" disabled="disabled" type="submit"><s:message code="~de.unibamberg.minf.dme.editor.actions.execute" /></button>
		</div>
	</div>
</form>
